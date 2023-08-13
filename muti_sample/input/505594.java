public final class DalvInsnList extends FixedSizeList {
    private final int regCount;
    public static DalvInsnList makeImmutable(ArrayList<DalvInsn> list,
            int regCount) {
        int size = list.size();
        DalvInsnList result = new DalvInsnList(size, regCount);
        for (int i = 0; i < size; i++) {
            result.set(i, list.get(i));
        }
        result.setImmutable();
        return result;
    }
    public DalvInsnList(int size, int regCount) {
        super(size);
        this.regCount = regCount;
    }
    public DalvInsn get(int n) {
        return (DalvInsn) get0(n);
    }
    public void set(int n, DalvInsn insn) {
        set0(n, insn);
    }
    public int codeSize() {
        int sz = size();
        if (sz == 0) {
            return 0;
        }
        DalvInsn last = get(sz - 1);
        return last.getNextAddress();
    }
    public void writeTo(AnnotatedOutput out) {
        int startCursor = out.getCursor();
        int sz = size();
        if (out.annotates()) {
            boolean verbose = out.isVerbose();
            for (int i = 0; i < sz; i++) {
                DalvInsn insn = (DalvInsn) get0(i);
                int codeBytes = insn.codeSize() * 2;
                String s;
                if ((codeBytes != 0) || verbose) {
                    s = insn.listingString("  ", out.getAnnotationWidth(),
                            true);
                } else {
                    s = null;
                }
                if (s != null) {
                    out.annotate(codeBytes, s);
                } else if (codeBytes != 0) {
                    out.annotate(codeBytes, "");
                }
            }
        }
        for (int i = 0; i < sz; i++) {
            DalvInsn insn = (DalvInsn) get0(i);
            try {
                insn.writeTo(out);
            } catch (RuntimeException ex) {
                throw ExceptionWithContext.withContext(ex,
                        "...while writing " + insn);
            }
        }
        int written = (out.getCursor() - startCursor) / 2;
        if (written != codeSize()) {
            throw new RuntimeException("write length mismatch; expected " +
                    codeSize() + " but actually wrote " + written);
        }
    }
    public int getRegistersSize() {
        return regCount;
    }
    public int getOutsSize() {
        int sz = size();
        int result = 0;
        for (int i = 0; i < sz; i++) {
            DalvInsn insn = (DalvInsn) get0(i);
            if (!(insn instanceof CstInsn)) {
                continue;
            }
            Constant cst = ((CstInsn) insn).getConstant();
            if (!(cst instanceof CstBaseMethodRef)) {
                continue;
            }
            boolean isStatic =
                (insn.getOpcode().getFamily() == DalvOps.INVOKE_STATIC);
            int count =
                ((CstBaseMethodRef) cst).getParameterWordCount(isStatic);
            if (count > result) {
                result = count;
            }
        }
        return result;
    }
    public void debugPrint(Writer out, String prefix, boolean verbose) {
        IndentingWriter iw = new IndentingWriter(out, 0, prefix);
        int sz = size();
        try {
            for (int i = 0; i < sz; i++) {
                DalvInsn insn = (DalvInsn) get0(i);
                String s;
                if ((insn.codeSize() != 0) || verbose) {
                    s = insn.listingString("", 0, verbose);
                } else {
                    s = null;
                }
                if (s != null) {
                    iw.write(s);
                }
            }
            iw.flush();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    public void debugPrint(OutputStream out, String prefix, boolean verbose) {
        Writer w = new OutputStreamWriter(out);
        debugPrint(w, prefix, verbose);
        try {
            w.flush();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
