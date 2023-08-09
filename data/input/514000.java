public final class Form22s extends InsnFormat {
    public static final InsnFormat THE_ONE = new Form22s();
    private Form22s() {
    }
    @Override
    public String insnArgString(DalvInsn insn) {
        RegisterSpecList regs = insn.getRegisters();
        CstLiteralBits value = (CstLiteralBits) ((CstInsn) insn).getConstant();
        return regs.get(0).regString() + ", " + regs.get(1).regString()
            + ", " + literalBitsString(value);
    }
    @Override
    public String insnCommentString(DalvInsn insn, boolean noteIndices) {
        CstLiteralBits value = (CstLiteralBits) ((CstInsn) insn).getConstant();
        return literalBitsComment(value, 16);
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
              unsignedFitsInNibble(regs.get(0).getReg()) &&
              unsignedFitsInNibble(regs.get(1).getReg()))) {
            return false;
        }
        CstInsn ci = (CstInsn) insn;
        Constant cst = ci.getConstant();
        if (!(cst instanceof CstLiteralBits)) {
            return false;
        }
        CstLiteralBits cb = (CstLiteralBits) cst;
        return cb.fitsInInt() && signedFitsInShort(cb.getIntBits());
    }
    @Override
    public InsnFormat nextUp() {
        return null;
    }
    @Override
    public void writeTo(AnnotatedOutput out, DalvInsn insn) {
        RegisterSpecList regs = insn.getRegisters();
        int value =
            ((CstLiteralBits) ((CstInsn) insn).getConstant()).getIntBits();
        write(out,
              opcodeUnit(insn,
                         makeByte(regs.get(0).getReg(), regs.get(1).getReg())),
              (short) value);
    }
}
