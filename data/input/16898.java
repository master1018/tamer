public class JobState extends EnumSyntax implements PrintJobAttribute {
    private static final long serialVersionUID = 400465010094018920L;
    public static final JobState UNKNOWN = new JobState(0);
    public static final JobState PENDING = new JobState(3);
    public static final JobState PENDING_HELD = new JobState(4);
    public static final JobState PROCESSING = new JobState (5);
    public static final JobState PROCESSING_STOPPED = new JobState (6);
    public static final JobState CANCELED = new JobState (7);
    public static final JobState ABORTED = new JobState (8);
    public static final JobState COMPLETED = new JobState (9);
    protected JobState(int value) {
        super (value);
    }
    private static final String[] myStringTable =
    {"unknown",
     null,
     null,
     "pending",
     "pending-held",
     "processing",
     "processing-stopped",
     "canceled",
     "aborted",
     "completed"};
    private static final JobState[] myEnumValueTable =
    {UNKNOWN,
     null,
     null,
     PENDING,
     PENDING_HELD,
     PROCESSING,
     PROCESSING_STOPPED,
     CANCELED,
     ABORTED,
     COMPLETED};
    protected String[] getStringTable() {
        return myStringTable;
    }
    protected EnumSyntax[] getEnumValueTable() {
        return myEnumValueTable;
    }
    public final Class<? extends Attribute> getCategory() {
        return JobState.class;
    }
    public final String getName() {
        return "job-state";
    }
}
