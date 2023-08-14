public final class Severity extends EnumSyntax implements Attribute {
    private static final long serialVersionUID = 8781881462717925380L;
    public static final Severity REPORT = new Severity (0);
    public static final Severity WARNING = new Severity (1);
    public static final Severity ERROR = new Severity (2);
    protected Severity(int value) {
        super (value);
    }
    private static final String[] myStringTable = {
        "report",
        "warning",
        "error"
    };
    private static final Severity[] myEnumValueTable = {
        REPORT,
        WARNING,
        ERROR
    };
    protected String[] getStringTable() {
        return myStringTable;
    }
    protected EnumSyntax[] getEnumValueTable() {
        return myEnumValueTable;
    }
    public final Class<? extends Attribute> getCategory() {
        return Severity.class;
    }
    public final String getName() {
        return "severity";
    }
}
