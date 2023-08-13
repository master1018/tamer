public final class Form21h extends InsnFormat {
    public static final InsnFormat THE_ONE = new Form21h();
    private Form21h() {
    }
    @Override
    public String insnArgString(DalvInsn insn) {
        RegisterSpecList regs = insn.getRegisters();
        CstLiteralBits value = (CstLiteralBits) ((CstInsn) insn).getConstant();
        return regs.get(0).regString() + ", " + literalBitsString(value);
    }
    @Override
    public String insnCommentString(DalvInsn insn, boolean noteIndices) {
        RegisterSpecList regs = insn.getRegisters();
        CstLiteralBits value = (CstLiteralBits) ((CstInsn) insn).getConstant();
        return
            literalBitsComment(value,
                    (regs.get(0).getCategory() == 1) ? 32 : 64);
    }
    @Override
    public int codeSize() {
        return 2;
    }
    @Override
    public boolean isCompatible(DalvInsn insn) {
        RegisterSpecList regs = insn.getRegisters();
        if (!((insn instanceof CstInsn) &&
              (regs.size() == 1) &&
              unsignedFitsInByte(regs.get(0).getReg()))) {
            return false;
        }
        CstInsn ci = (CstInsn) insn;
        Constant cst = ci.getConstant();
        if (!(cst instanceof CstLiteralBits)) {
            return false;
        }
        CstLiteralBits cb = (CstLiteralBits) cst;
        if (regs.get(0).getCategory() == 1) {
            int bits = cb.getIntBits();
            return ((bits & 0xffff) == 0);
        } else {
            long bits = cb.getLongBits();
            return ((bits & 0xffffffffffffL) == 0);
        }
    }
    @Override
    public InsnFormat nextUp() {
        return Form31i.THE_ONE;
    }
    @Override
    public void writeTo(AnnotatedOutput out, DalvInsn insn) {
        RegisterSpecList regs = insn.getRegisters();
        CstLiteralBits cb = (CstLiteralBits) ((CstInsn) insn).getConstant();
        short bits;
        if (regs.get(0).getCategory() == 1) {
            bits = (short) (cb.getIntBits() >>> 16);
        } else {
            bits = (short) (cb.getLongBits() >>> 48);
        }
        write(out, opcodeUnit(insn, regs.get(0).getReg()), bits);
    }
}
