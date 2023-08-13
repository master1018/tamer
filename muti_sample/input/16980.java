public final class SPARCIllegalInstruction extends SPARCInstruction {
    final private int instruction;
    final private String description;
    public SPARCIllegalInstruction(int instruction) {
        super("illegal");
        this.instruction = instruction;
        description = "bad opcode - " + Integer.toHexString(instruction);
    }
    public String asString(long currentPc, SymbolFinder symFinder) {
        return description;
    }
    public int getInstruction() {
        return instruction;
    }
    public boolean isIllegal() {
        return true;
    }
}
