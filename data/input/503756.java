public final class Form32x extends InsnFormat {
    public static final InsnFormat THE_ONE = new Form32x();
    private Form32x() {
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
        return 3;
    }
    @Override
    public boolean isCompatible(DalvInsn insn) {
        RegisterSpecList regs = insn.getRegisters();
        return (insn instanceof SimpleInsn) &&
            (regs.size() == 2) &&
            unsignedFitsInShort(regs.get(0).getReg()) &&
            unsignedFitsInShort(regs.get(1).getReg());
    }
    @Override
    public InsnFormat nextUp() {
        return null;
    }
    @Override
    public void writeTo(AnnotatedOutput out, DalvInsn insn) {
        RegisterSpecList regs = insn.getRegisters();
        write(out,
              opcodeUnit(insn, 0),
              (short) regs.get(0).getReg(),
              (short) regs.get(1).getReg());
    }
}
