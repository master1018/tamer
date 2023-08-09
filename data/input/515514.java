public final class Form23x extends InsnFormat {
    public static final InsnFormat THE_ONE = new Form23x();
    private Form23x() {
    }
    @Override
    public String insnArgString(DalvInsn insn) {
        RegisterSpecList regs = insn.getRegisters();
        return regs.get(0).regString() + ", " + regs.get(1).regString() +
            ", " + regs.get(2).regString();
    }
    @Override
    public String insnCommentString(DalvInsn insn, boolean noteIndices) {
        return "";
    }
    @Override
    public int codeSize() {
        return 2;
    }
    @Override
    public boolean isCompatible(DalvInsn insn) {
        RegisterSpecList regs = insn.getRegisters();
        return (insn instanceof SimpleInsn) &&
            (regs.size() == 3) &&
            unsignedFitsInByte(regs.get(0).getReg()) &&
            unsignedFitsInByte(regs.get(1).getReg()) &&
            unsignedFitsInByte(regs.get(2).getReg());
    }
    @Override
    public InsnFormat nextUp() {
        return Form32x.THE_ONE;
    }
    @Override
    public void writeTo(AnnotatedOutput out, DalvInsn insn) {
        RegisterSpecList regs = insn.getRegisters();
        write(out,
              opcodeUnit(insn, regs.get(0).getReg()),
              codeUnit(regs.get(1).getReg(), regs.get(2).getReg()));
    }
}
