public final class JobMediaSheetsCompleted extends IntegerSyntax
        implements PrintJobAttribute {
    private static final long serialVersionUID = 1739595973810840475L;
    public JobMediaSheetsCompleted(int value) {
        super (value, 0, Integer.MAX_VALUE);
    }
    public boolean equals(Object object) {
        return (super.equals (object) &&
                object instanceof JobMediaSheetsCompleted);
    }
    public final Class<? extends Attribute> getCategory() {
        return JobMediaSheetsCompleted.class;
    }
    public final String getName() {
        return "job-media-sheets-completed";
    }
}
