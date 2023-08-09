public final class JobHoldUntil extends DateTimeSyntax
        implements PrintRequestAttribute, PrintJobAttribute {
    private static final long serialVersionUID = -1664471048860415024L;
    public JobHoldUntil(Date dateTime) {
        super (dateTime);
    }
    public boolean equals(Object object) {
        return (super.equals(object) && object instanceof JobHoldUntil);
    }
    public final Class<? extends Attribute> getCategory() {
        return JobHoldUntil.class;
    }
    public final String getName() {
        return "job-hold-until";
    }
}
