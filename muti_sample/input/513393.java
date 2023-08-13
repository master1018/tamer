public final class Form3rc extends InsnFormat {
    public static final InsnFormat THE_ONE = new Form3rc();
    private Form3rc() {
    }
    @Override
    public String insnArgString(DalvInsn insn) {
        RegisterSpecList regs = insn.getRegisters();
        int size = regs.size();
        StringBuilder sb = new StringBuilder(30);
        sb.append("{");
        switch (size) {
            case 0: {
                break;
            }
            case 1: {
                sb.append(regs.get(0).regString());
                break;
            }
            default: {
                RegisterSpec lastReg = regs.get(size - 1);
                if (lastReg.getCategory() == 2) {
                    lastReg = lastReg.withOffset(1);
                }
                sb.append(regs.get(0).regString());
                sb.append("..");
                sb.append(lastReg.regString());
            }
        }
        sb.append("}, ");
        sb.append(cstString(insn));
        return sb.toString();
    }
    @Override
    public String insnCommentString(DalvInsn insn, boolean noteIndices) {
        if (noteIndices) {
            return cstComment(insn);
        } else {
            return "";
        }
    }
    @Override
    public int codeSize() {
        return 3;
    }
    @Override
    public boolean isCompatible(DalvInsn insn) {
        if (!(insn instanceof CstInsn)) {
            return false;
        }
        CstInsn ci = (CstInsn) insn;
        int cpi = ci.getIndex();
        if (! unsignedFitsInShort(cpi)) {
            return false;
        }
        Constant cst = ci.getConstant();
        if (!((cst instanceof CstMethodRef) ||
              (cst instanceof CstType))) {
            return false;
        }
        RegisterSpecList regs = ci.getRegisters();
        int sz = regs.size();
        if (sz == 0) {
            return true;
        }
        int first = regs.get(0).getReg();
        int next = first;
        if (!unsignedFitsInShort(first)) {
            return false;
        }
        for (int i = 0; i < sz; i++) {
            RegisterSpec one = regs.get(i);
            if (one.getReg() != next) {
                return false;
            }
            next += one.getCategory();
        }
        return unsignedFitsInByte(next - first);
    }
    @Override
    public InsnFormat nextUp() {
        return null;
    }
    @Override
    public void writeTo(AnnotatedOutput out, DalvInsn insn) {
        RegisterSpecList regs = insn.getRegisters();
        int sz = regs.size();
        int cpi = ((CstInsn) insn).getIndex();
        int firstReg;
        int count;
        if (sz == 0) {
            firstReg = 0;
            count = 0;
        } else {
            int lastReg = regs.get(sz - 1).getNextReg();
            firstReg = regs.get(0).getReg();
            count = lastReg - firstReg;
        }
        write(out,
              opcodeUnit(insn, count),
              (short) cpi,
              (short) firstReg);
    }
}
