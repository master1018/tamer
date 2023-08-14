public class SsaRenamer implements Runnable {
    private static final boolean DEBUG = false;
    private final SsaMethod ssaMeth;
    private int nextSsaReg;
    private final int ropRegCount;
    private final RegisterSpec[][] startsForBlocks;
    private final ArrayList<LocalItem> ssaRegToLocalItems;
    private IntList ssaRegToRopReg;
    public SsaRenamer(SsaMethod ssaMeth) {
        ropRegCount = ssaMeth.getRegCount();
        this.ssaMeth = ssaMeth;
        nextSsaReg = ropRegCount;
        startsForBlocks = new RegisterSpec[ssaMeth.getBlocks().size()][];
        ssaRegToLocalItems = new ArrayList<LocalItem>();
        if (DEBUG) {
            ssaRegToRopReg = new IntList(ropRegCount);
        }
        RegisterSpec[] initialRegMapping = new RegisterSpec[ropRegCount];
        for (int i = 0; i < ropRegCount; i++) {
            initialRegMapping[i] = RegisterSpec.make(i, Type.VOID);
            if (DEBUG) {
                ssaRegToRopReg.add(i);
            }
        }
        startsForBlocks[ssaMeth.getEntryBlockIndex()] = initialRegMapping;
    }
    public void run() {
        ssaMeth.forEachBlockDepthFirstDom(new SsaBasicBlock.Visitor() {
            public void visitBlock (SsaBasicBlock block,
                    SsaBasicBlock unused) {
                new BlockRenamer(block).process();
            }
        });
        ssaMeth.setNewRegCount(nextSsaReg);
        ssaMeth.onInsnsChanged();
        if (DEBUG) {
            System.out.println("SSA\tRop");
            int[] versions = new int[ropRegCount];
            int sz = ssaRegToRopReg.size();
            for (int i = 0; i < sz; i++) {
                int ropReg = ssaRegToRopReg.get(i);
                System.out.println(i + "\t" + ropReg + "["
                        + versions[ropReg] + "]");
                versions[ropReg]++;
            }
        }
    }
    private static  RegisterSpec[] dupArray(RegisterSpec[] orig) {
        RegisterSpec[] copy = new RegisterSpec[orig.length];
        System.arraycopy(orig, 0, copy, 0, orig.length);
        return copy;
    }
    private LocalItem getLocalForNewReg(int ssaReg) {
        if (ssaReg < ssaRegToLocalItems.size()) {
            return ssaRegToLocalItems.get(ssaReg);
        } else {
            return null;
        }
    }
    private void setNameForSsaReg(RegisterSpec ssaReg) {
        int reg = ssaReg.getReg();
        LocalItem local = ssaReg.getLocalItem();
        ssaRegToLocalItems.ensureCapacity(reg + 1);
        while (ssaRegToLocalItems.size() <= reg) {
            ssaRegToLocalItems.add(null);
        }
        ssaRegToLocalItems.set(reg, local);
    }
    private boolean isVersionZeroRegister(int ssaReg) {
        return ssaReg < ropRegCount;
    }
    private static boolean equalsHandlesNulls(Object a, Object b) {
        return a == b ||  (a != null && a.equals(b));
    }
    private class BlockRenamer implements SsaInsn.Visitor{
        private final SsaBasicBlock block;
        private final RegisterSpec[] currentMapping;
        private final HashSet<SsaInsn> movesToKeep;
        private final HashMap<SsaInsn, SsaInsn> insnsToReplace;
        private final RenamingMapper mapper;
        BlockRenamer(final SsaBasicBlock block) {
            this.block = block;
            currentMapping = startsForBlocks[block.getIndex()];
            movesToKeep = new HashSet<SsaInsn>();
            insnsToReplace = new HashMap<SsaInsn, SsaInsn>();
            mapper =  new RenamingMapper();
            startsForBlocks[block.getIndex()] = null;
        }
        private class RenamingMapper extends RegisterMapper {
            public RenamingMapper() {
            }
            @Override
            public int getNewRegisterCount() {
                return nextSsaReg;
            }
            @Override
            public RegisterSpec map(RegisterSpec registerSpec) {
                if (registerSpec == null) return null;
                int reg = registerSpec.getReg();
                if (DEBUG) {
                    RegisterSpec newVersion = currentMapping[reg];
                    if (newVersion.getBasicType() != Type.BT_VOID
                            && registerSpec.getBasicFrameType()
                                != newVersion.getBasicFrameType()) {
                        throw new RuntimeException(
                                "mapping registers of incompatible types! "
                                + registerSpec
                                + " " + currentMapping[reg]);
                    }
                }
                return registerSpec.withReg(currentMapping[reg].getReg());
            }
        }
        public void process() {
            block.forEachInsn(this);
            updateSuccessorPhis();
            ArrayList<SsaInsn> insns = block.getInsns();
            int szInsns = insns.size();
            for (int i = szInsns - 1; i >= 0 ; i--) {
                SsaInsn insn = insns.get(i);
                SsaInsn replaceInsn;
                replaceInsn = insnsToReplace.get(insn);
                if (replaceInsn != null) {
                    insns.set(i, replaceInsn);
                } else if (insn.isNormalMoveInsn()
                        && !movesToKeep.contains(insn)) {
                    insns.remove(i);
                }
            }
            boolean first = true;
            for (SsaBasicBlock child : block.getDomChildren()) {
                if (child != block) {
                    RegisterSpec[] childStart = first ? currentMapping
                        : dupArray(currentMapping);
                    startsForBlocks[child.getIndex()] = childStart;
                    first = false;
                }
            }
        }
        private void addMapping(int ropReg, RegisterSpec ssaReg) {
            int ssaRegNum = ssaReg.getReg();
            LocalItem ssaRegLocal = ssaReg.getLocalItem();
            currentMapping[ropReg] = ssaReg;
            for (int i = currentMapping.length - 1; i >= 0; i--) {
                RegisterSpec cur = currentMapping[i];
                if (ssaRegNum == cur.getReg()) {
                    currentMapping[i] = ssaReg;
                }
            }
            if (ssaRegLocal == null) {
                return;
            }
            setNameForSsaReg(ssaReg);
            for (int i = currentMapping.length - 1; i >= 0; i--) {
                RegisterSpec cur = currentMapping[i];
                if (ssaRegNum != cur.getReg()
                        && ssaRegLocal.equals(cur.getLocalItem())) {
                    currentMapping[i] = cur.withLocalItem(null);
                }
            }
        }
        public void visitPhiInsn(PhiInsn phi) {
            processResultReg(phi);
        }
        public void visitMoveInsn(NormalSsaInsn insn) {
            RegisterSpec ropResult = insn.getResult();
            int ropResultReg = ropResult.getReg();
            int ropSourceReg = insn.getSources().get(0).getReg();
            insn.mapSourceRegisters(mapper);
            int ssaSourceReg = insn.getSources().get(0).getReg();
            LocalItem sourceLocal
                = currentMapping[ropSourceReg].getLocalItem();
            LocalItem resultLocal = ropResult.getLocalItem();
            LocalItem newLocal
                = (resultLocal == null) ? sourceLocal : resultLocal;
            LocalItem associatedLocal = getLocalForNewReg(ssaSourceReg);
            boolean onlyOneAssociatedLocal
                    = associatedLocal == null || newLocal == null
                    || newLocal.equals(associatedLocal);
            RegisterSpec ssaReg
                    = RegisterSpec.makeLocalOptional(
                        ssaSourceReg, ropResult.getType(), newLocal);
            if (!Optimizer.getPreserveLocals() || (onlyOneAssociatedLocal
                    && equalsHandlesNulls(newLocal, sourceLocal))) {
                addMapping(ropResultReg, ssaReg);
            } else if (onlyOneAssociatedLocal && sourceLocal == null) {
                RegisterSpecList ssaSources = RegisterSpecList.make(
                        RegisterSpec.make(ssaReg.getReg(),
                                ssaReg.getType(), newLocal));
                SsaInsn newInsn
                        = SsaInsn.makeFromRop(
                            new PlainInsn(Rops.opMarkLocal(ssaReg),
                            SourcePosition.NO_INFO, null, ssaSources),block);
                insnsToReplace.put(insn, newInsn);
                addMapping(ropResultReg, ssaReg);
            } else {
                processResultReg(insn);
                movesToKeep.add(insn);
            }
        }
        public void visitNonMoveInsn(NormalSsaInsn insn) {
            insn.mapSourceRegisters(mapper);
            processResultReg(insn);
        }
        void processResultReg(SsaInsn insn) {
            RegisterSpec ropResult = insn.getResult();
            if (ropResult == null) {
                return;
            }
            int ropReg = ropResult.getReg();
            insn.changeResultReg(nextSsaReg);
            addMapping(ropReg, insn.getResult());
            if (DEBUG) {
                ssaRegToRopReg.add(ropReg);
            }
            nextSsaReg++;
        }
        private void updateSuccessorPhis() {
            PhiInsn.Visitor visitor = new PhiInsn.Visitor() {
                public void visitPhiInsn (PhiInsn insn) {
                    int ropReg;
                    ropReg = insn.getRopResultReg();
                    RegisterSpec stackTop = currentMapping[ropReg];
                    if (!isVersionZeroRegister(stackTop.getReg())) {
                        insn.addPhiOperand(stackTop, block);
                    }
                }
            };
            BitSet successors = block.getSuccessors();
            for (int i = successors.nextSetBit(0); i >= 0;
                    i = successors.nextSetBit(i + 1)) {
                SsaBasicBlock successor = ssaMeth.getBlocks().get(i);
                successor.forEachPhiInsn(visitor);
            }
        }
    }
}
