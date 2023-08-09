public final class Form22x extends InsnFormat {
    public static final InsnFormat THE_ONE = new Form22x();
    private Form22x() {
    }
    @Override
    public String insnArgString(DalvInsn insn) {
        RegisterSpecList regs = insn.getRegisters();
        return regs.get(0).regString() + ", " + regs.get(1).regString();
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
            (regs.size() == 2) &&
            unsignedFitsInByte(regs.get(0).getReg()) &&
            unsignedFitsInShort(regs.get(1).getReg());
    }
    @Override
    public InsnFormat nextUp() {
        return Form23x.THE_ONE;
    }
    @Override
    public void writeTo(AnnotatedOutput out, DalvInsn insn) {
        RegisterSpecList regs = insn.getRegisters();
        write(out,
              opcodeUnit(insn, regs.get(0).getReg()),
              (short) regs.get(1).getReg());
    }
}
