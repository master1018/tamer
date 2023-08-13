public class JobSheets extends EnumSyntax
        implements PrintRequestAttribute, PrintJobAttribute {
    private static final long serialVersionUID = -4735258056132519759L;
    public static final JobSheets NONE = new JobSheets(0);
    public static final JobSheets STANDARD = new JobSheets(1);
    protected JobSheets(int value) {
        super (value);
    }
    private static final String[] myStringTable = {
        "none",
        "standard"
    };
    private static final JobSheets[] myEnumValueTable = {
        NONE,
        STANDARD
    };
    protected String[] getStringTable() {
        return (String[])myStringTable.clone();
    }
    protected EnumSyntax[] getEnumValueTable() {
        return (EnumSyntax[])myEnumValueTable.clone();
    }
    public final Class<? extends Attribute> getCategory() {
        return JobSheets.class;
    }
    public final String getName() {
        return "job-sheets";
    }
}
