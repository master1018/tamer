public final class Form22b extends InsnFormat {
    public static final InsnFormat THE_ONE = new Form22b();
    private Form22b() {
    }
    @Override
    public String insnArgString(DalvInsn insn) {
        RegisterSpecList regs = insn.getRegisters();
        CstLiteralBits value = (CstLiteralBits) ((CstInsn) insn).getConstant();
        return regs.get(0).regString() + ", " + regs.get(1).regString() +
            ", " + literalBitsString(value);
    }
    @Override
    public String insnCommentString(DalvInsn insn, boolean noteIndices) {
        CstLiteralBits value = (CstLiteralBits) ((CstInsn) insn).getConstant();
        return literalBitsComment(value, 8);
    }
    @Override
    public int codeSize() {
        return 2;
    }
    @Override
    public boolean isCompatible(DalvInsn insn) {
        RegisterSpecList regs = insn.getRegisters();
        if (!((insn instanceof CstInsn) &&
              (regs.size() == 2) &&
              unsignedFitsInByte(regs.get(0).getReg()) &&
              unsignedFitsInByte(regs.get(1).getReg()))) {
            return false;
        }
        CstInsn ci = (CstInsn) insn;
        Constant cst = ci.getConstant();
        if (!(cst instanceof CstLiteralBits)) {
            return false;
        }
        CstLiteralBits cb = (CstLiteralBits) cst;
        return cb.fitsInInt() && signedFitsInByte(cb.getIntBits());
    }
    @Override
    public InsnFormat nextUp() {
        return Form22s.THE_ONE;
    }
    @Override
    public void writeTo(AnnotatedOutput out, DalvInsn insn) {
        RegisterSpecList regs = insn.getRegisters();
        int value =
            ((CstLiteralBits) ((CstInsn) insn).getConstant()).getIntBits();
        write(out,
              opcodeUnit(insn, regs.get(0).getReg()),
              codeUnit(regs.get(1).getReg(), value & 0xff));
    }
}
