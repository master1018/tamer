public final class PrinterIsAcceptingJobs extends EnumSyntax
        implements PrintServiceAttribute {
    private static final long serialVersionUID = -5052010680537678061L;
    public static final PrinterIsAcceptingJobs
        NOT_ACCEPTING_JOBS = new PrinterIsAcceptingJobs(0);
    public static final PrinterIsAcceptingJobs
        ACCEPTING_JOBS = new PrinterIsAcceptingJobs(1);
    protected PrinterIsAcceptingJobs(int value) {
        super (value);
    }
    private static final String[] myStringTable = {
        "not-accepting-jobs",
        "accepting-jobs"
    };
    private static final PrinterIsAcceptingJobs[] myEnumValueTable = {
        NOT_ACCEPTING_JOBS,
        ACCEPTING_JOBS
    };
    protected String[] getStringTable() {
        return myStringTable;
    }
    protected EnumSyntax[] getEnumValueTable() {
        return myEnumValueTable;
    }
    public final Class<? extends Attribute> getCategory() {
        return PrinterIsAcceptingJobs.class;
    }
    public final String getName() {
        return "printer-is-accepting-jobs";
    }
}
