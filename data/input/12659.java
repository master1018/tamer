public class SPARCFlushInstruction extends SPARCInstruction {
    final protected SPARCRegisterIndirectAddress addr;
    final String description;
    public SPARCFlushInstruction(SPARCRegisterIndirectAddress addr) {
        super("flush");
        this.addr = addr;
        description = initDescription();
    }
    private String initDescription() {
        StringBuffer buf = new StringBuffer();
        buf.append(getName());
        buf.append(spaces);
        buf.append(addr.toString());
        return buf.toString();
    }
    public String asString(long currentPc, SymbolFinder symFinder) {
        return description;
    }
}
