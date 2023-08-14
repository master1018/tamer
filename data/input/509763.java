public final class OutputFinisher {
    private final int unreservedRegCount;
    private ArrayList<DalvInsn> insns;
    private boolean hasAnyPositionInfo;
    private boolean hasAnyLocalInfo;
    private int reservedCount;
    public OutputFinisher(int initialCapacity, int regCount) {
        this.unreservedRegCount = regCount;
        this.insns = new ArrayList<DalvInsn>(initialCapacity);
        this.reservedCount = -1;
        this.hasAnyPositionInfo = false;
        this.hasAnyLocalInfo = false;
    }
    public boolean hasAnyPositionInfo() {
        return hasAnyPositionInfo;
    }
    public boolean hasAnyLocalInfo() {
        return hasAnyLocalInfo;
    }
    private static boolean hasLocalInfo(DalvInsn insn) {
        if (insn instanceof LocalSnapshot) {
            RegisterSpecSet specs = ((LocalSnapshot) insn).getLocals();
            int size = specs.size();
            for (int i = 0; i < size; i++) {
                if (hasLocalInfo(specs.get(i))) {
                    return true;
                }
            }
        } else if (insn instanceof LocalStart) {
            RegisterSpec spec = ((LocalStart) insn).getLocal();
            if (hasLocalInfo(spec)) {
                return true;
            }
        }
        return false;
    }
    private static boolean hasLocalInfo(RegisterSpec spec) {
        return (spec != null)
            && (spec.getLocalItem().getName() != null);
    }
    public HashSet<Constant> getAllConstants() {
        HashSet<Constant> result = new HashSet<Constant>(20);
        for (DalvInsn insn : insns) {
            addConstants(result, insn);
        }
        return result;
    }
    private static void addConstants(HashSet<Constant> result,
            DalvInsn insn) {
        if (insn instanceof CstInsn) {
            Constant cst = ((CstInsn) insn).getConstant();
            result.add(cst);
        } else if (insn instanceof LocalSnapshot) {
            RegisterSpecSet specs = ((LocalSnapshot) insn).getLocals();
            int size = specs.size();
            for (int i = 0; i < size; i++) {
                addConstants(result, specs.get(i));
            }
        } else if (insn instanceof LocalStart) {
            RegisterSpec spec = ((LocalStart) insn).getLocal();
            addConstants(result, spec);
        }
    }
    private static void addConstants(HashSet<Constant> result,
            RegisterSpec spec) {
        if (spec == null) {
            return;
        }
        LocalItem local = spec.getLocalItem();
        CstUtf8 name = local.getName();
        CstUtf8 signature = local.getSignature();
        Type type = spec.getType();
        if (type != Type.KNOWN_NULL) {
            result.add(CstType.intern(type));
        }
        if (name != null) {
            result.add(name);
        }
        if (signature != null) {
            result.add(signature);
        }
    }
    public void add(DalvInsn insn) {
        insns.add(insn);
        updateInfo(insn);
    }
    public void insert(int at, DalvInsn insn) {
        insns.add(at, insn);
        updateInfo(insn);
    }
    private void updateInfo(DalvInsn insn) {
        if (! hasAnyPositionInfo) {
            SourcePosition pos = insn.getPosition();
            if (pos.getLine() >= 0) {
                hasAnyPositionInfo = true;
            }
        }
        if (! hasAnyLocalInfo) {
            if (hasLocalInfo(insn)) {
                hasAnyLocalInfo = true;
            }
        }
    }
    public void reverseBranch(int which, CodeAddress newTarget) {
        int size = insns.size();
        int index = size - which - 1;
        TargetInsn targetInsn;
        try {
            targetInsn = (TargetInsn) insns.get(index);
        } catch (IndexOutOfBoundsException ex) {
            throw new IllegalArgumentException("too few instructions");
        } catch (ClassCastException ex) {
            throw new IllegalArgumentException("non-reversible instruction");
        }
        insns.set(index, targetInsn.withNewTargetAndReversed(newTarget));
    }
    public void assignIndices(DalvCode.AssignIndicesCallback callback) {
        for (DalvInsn insn : insns) {
            if (insn instanceof CstInsn) {
                assignIndices((CstInsn) insn, callback);
            }
        }
    }
    private static void assignIndices(CstInsn insn,
            DalvCode.AssignIndicesCallback callback) {
        Constant cst = insn.getConstant();
        int index = callback.getIndex(cst);
        if (index >= 0) {
            insn.setIndex(index);
        }
        if (cst instanceof CstMemberRef) {
            CstMemberRef member = (CstMemberRef) cst;
            CstType definer = member.getDefiningClass();
            index = callback.getIndex(definer);
            if (index >= 0) {
                insn.setClassIndex(index);
            }
        }
    }
    public DalvInsnList finishProcessingAndGetList() {
        if (reservedCount >= 0) {
            throw new UnsupportedOperationException("already processed");
        }
        InsnFormat[] formats = makeFormatsArray();
        reserveRegisters(formats);
        massageInstructions(formats);
        assignAddressesAndFixBranches();
        return DalvInsnList.makeImmutable(insns,
                reservedCount + unreservedRegCount);
    }
    private InsnFormat[] makeFormatsArray() {
        int size = insns.size();
        InsnFormat[] result = new InsnFormat[size];
        for (int i = 0; i < size; i++) {
            result[i] = insns.get(i).getOpcode().getFormat();
        }
        return result;
    }
    private void reserveRegisters(InsnFormat[] formats) {
        int oldReservedCount = (reservedCount < 0) ? 0 : reservedCount;
        for (;;) {
            int newReservedCount = calculateReservedCount(formats);
            if (oldReservedCount >= newReservedCount) {
                break;
            }
            int reservedDifference = newReservedCount - oldReservedCount;
            int size = insns.size();
            for (int i = 0; i < size; i++) {
                DalvInsn insn = insns.get(i);
                if (!(insn instanceof CodeAddress)) {
                    insns.set(i, insn.withRegisterOffset(reservedDifference));
                }
            }
            oldReservedCount = newReservedCount;
        }
        reservedCount = oldReservedCount;
    }
    private int calculateReservedCount(InsnFormat[] formats) {
        int size = insns.size();
        int newReservedCount = reservedCount;
        for (int i = 0; i < size; i++) {
            DalvInsn insn = insns.get(i);
            InsnFormat originalFormat = formats[i];
            InsnFormat newFormat = findFormatForInsn(insn, originalFormat);
            if (originalFormat == newFormat) {
                continue;
            }
            if (newFormat == null) {
                int reserve = insn.getMinimumRegisterRequirement();
                if (reserve > newReservedCount) {
                    newReservedCount = reserve;
                }
            }
            formats[i] = newFormat;
        }
        return newReservedCount;
    }
    private InsnFormat findFormatForInsn(DalvInsn insn, InsnFormat format) {
        if (format == null) {
            return format;
        }
        if (format.isCompatible(insn)) {
            return format;
        }
        Dop dop = insn.getOpcode();
        int family = dop.getFamily();
        for (;;) {
            format = format.nextUp();
            if ((format == null) ||
                    (format.isCompatible(insn) && 
                     (Dops.getOrNull(family, format) != null))) {
                break;
            }
        }
        return format;
    }
    private void massageInstructions(InsnFormat[] formats) {
        if (reservedCount == 0) {
            int size = insns.size();
            for (int i = 0; i < size; i++) {
                DalvInsn insn = insns.get(i);
                Dop dop = insn.getOpcode();
                InsnFormat format = formats[i];
                if (format != dop.getFormat()) {
                    dop = Dops.getOrNull(dop.getFamily(), format);
                    insns.set(i, insn.withOpcode(dop));
                }
            }
        } else {
            insns = performExpansion(formats);
        }
    }
    private ArrayList<DalvInsn> performExpansion(InsnFormat[] formats) {
        int size = insns.size();
        ArrayList<DalvInsn> result = new ArrayList<DalvInsn>(size * 2);
        for (int i = 0; i < size; i++) {
            DalvInsn insn = insns.get(i);
            Dop dop = insn.getOpcode();
            InsnFormat originalFormat = dop.getFormat();
            InsnFormat currentFormat = formats[i];
            DalvInsn prefix;
            DalvInsn suffix;
            if (currentFormat != null) {
                prefix = null;
                suffix = null;
            } else {
                prefix = insn.hrPrefix();
                suffix = insn.hrSuffix();
                insn = insn.hrVersion();
                originalFormat = insn.getOpcode().getFormat();
                currentFormat = findFormatForInsn(insn, originalFormat);
            }
            if (prefix != null) {
                result.add(prefix);
            }
            if (currentFormat != originalFormat) {
                dop = Dops.getOrNull(dop.getFamily(), currentFormat);
                insn = insn.withOpcode(dop);
            }
            result.add(insn);
            if (suffix != null) {
                result.add(suffix);
            }
        }
        return result;
    }
    private void assignAddressesAndFixBranches() {
        for (;;) {
            assignAddresses();
            if (!fixBranches()) {
                break;
            }
        }
    }
    private void assignAddresses() {
        int address = 0;
        int size = insns.size();
        for (int i = 0; i < size; i++) {
            DalvInsn insn = insns.get(i);
            insn.setAddress(address);
            address += insn.codeSize();
        }
    }
    private boolean fixBranches() {
        int size = insns.size();
        boolean anyFixed = false;
        for (int i = 0; i < size; i++) {
            DalvInsn insn = insns.get(i);
            if (!(insn instanceof TargetInsn)) {
                continue;
            }
            Dop dop = insn.getOpcode();
            InsnFormat format = dop.getFormat();
            TargetInsn target = (TargetInsn) insn;
            if (format.branchFits(target)) {
                continue;
            }
            if (dop.getFamily() == DalvOps.GOTO) {
                InsnFormat newFormat = findFormatForInsn(insn, format);
                if (newFormat == null) {
                    throw new UnsupportedOperationException("method too long");
                }
                dop = Dops.getOrNull(dop.getFamily(), newFormat);
                insn = insn.withOpcode(dop);
                insns.set(i, insn);
            } else {
                CodeAddress newTarget;
                try {
                    newTarget = (CodeAddress) insns.get(i + 1);
                } catch (IndexOutOfBoundsException ex) {
                    throw new IllegalStateException(
                            "unpaired TargetInsn (dangling)");
                } catch (ClassCastException ex) {
                    throw new IllegalStateException("unpaired TargetInsn");
                }
                TargetInsn gotoInsn =
                    new TargetInsn(Dops.GOTO, target.getPosition(),
                            RegisterSpecList.EMPTY, target.getTarget());
                insns.set(i, gotoInsn);
                insns.add(i, target.withNewTargetAndReversed(newTarget));
                size++;
                i++;
            }
            anyFixed = true;
        }
        return anyFixed;
    }
}
