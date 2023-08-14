public abstract class SPARCSpecialRegisterInstruction
                       extends SPARCInstruction
                       implements  SPARCSpecialRegisters {
    protected SPARCSpecialRegisterInstruction(String name) {
        super(name);
    }
    protected abstract String getDescription();
    public String asString(long currentPc, SymbolFinder symFinder) {
        return getDescription();
    }
    protected static String[] specialRegNames = new String[] {
       "%y",
       "%psr",
       "%wim",
       "%tbr",
       "%asr",
       "%fsr",
       "%csr",
       "%fq",
       "%cq"
    };
    protected static String getSpecialRegisterName(int index) {
       return specialRegNames[index];
    }
}
