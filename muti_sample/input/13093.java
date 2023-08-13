public abstract class SPARCInstruction
                      extends AbstractInstruction
                      implements  SPARCOpcodes {
    public SPARCInstruction(String name) {
        super(name);
    }
    public int getSize() {
        return 4;
    }
    protected static String comma = ", ";
    protected static String spaces = "\t";
}
