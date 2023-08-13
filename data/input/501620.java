public final class Form51l extends InsnFormat {
    public static final InsnFormat THE_ONE = new Form51l();
    private Form51l() {
    }
    @Override
    public String insnArgString(DalvInsn insn) {
        RegisterSpecList regs = insn.getRegisters();
        CstLiteralBits value = (CstLiteralBits) ((CstInsn) insn).getConstant();
        return regs.get(0).regString() + ", " + literalBitsString(value);
    }
    @Override
    public String insnCommentString(DalvInsn insn, boolean noteIndices) {
        CstLiteralBits value = (CstLiteralBits) ((CstInsn) insn).getConstant();
        return literalBitsComment(value, 64);
    }
    @Override
    public int codeSize() {
        return 5;
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
        return (cst instanceof CstLiteral64);
    }
    @Override
    public InsnFormat nextUp() {
        return null;
    }
    @Override
    public void writeTo(AnnotatedOutput out, DalvInsn insn) {
        RegisterSpecList regs = insn.getRegisters();
        long value =
            ((CstLiteral64) ((CstInsn) insn).getConstant()).getLongBits();
        write(out,
              opcodeUnit(insn, regs.get(0).getReg()),
              (short) value,
              (short) (value >> 16),
              (short) (value >> 32),
              (short) (value >> 48));
    }
}
