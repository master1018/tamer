public final class PrinterState extends EnumSyntax
implements PrintServiceAttribute {
    private static final long serialVersionUID = -649578618346507718L;
    public static final PrinterState UNKNOWN = new PrinterState(0);
    public static final PrinterState IDLE = new PrinterState(3);
    public static final PrinterState PROCESSING = new PrinterState(4);
    public static final PrinterState STOPPED = new PrinterState(5);
    protected PrinterState(int value) {
        super (value);
    }
    private static final String[] myStringTable = {
        "unknown",
        null,
        null,
        "idle",
        "processing",
        "stopped"
    };
    private static final PrinterState[] myEnumValueTable = {
        UNKNOWN,
        null,
        null,
        IDLE,
        PROCESSING,
        STOPPED
    };
    protected String[] getStringTable() {
        return myStringTable;
    }
    protected EnumSyntax[] getEnumValueTable() {
        return myEnumValueTable;
    }
    public final Class<? extends Attribute> getCategory() {
        return PrinterState.class;
    }
    public final String getName() {
        return "printer-state";
    }
}
