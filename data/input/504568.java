public final class Form11x extends InsnFormat {
    public static final InsnFormat THE_ONE = new Form11x();
    private Form11x() {
    }
    @Override
    public String insnArgString(DalvInsn insn) {
        RegisterSpecList regs = insn.getRegisters();
        return regs.get(0).regString();
    }
    @Override
    public String insnCommentString(DalvInsn insn, boolean noteIndices) {
        return "";
    }
    @Override
    public int codeSize() {
        return 1;
    }
    @Override
    public boolean isCompatible(DalvInsn insn) {
        RegisterSpecList regs = insn.getRegisters();
        return (insn instanceof SimpleInsn) &&
            (regs.size() == 1) &&
            unsignedFitsInByte(regs.get(0).getReg());
    }
    @Override
    public InsnFormat nextUp() {
        return null;
    }
    @Override
    public void writeTo(AnnotatedOutput out, DalvInsn insn) {
        RegisterSpecList regs = insn.getRegisters();
        write(out, opcodeUnit(insn, regs.get(0).getReg()));
    }
}
