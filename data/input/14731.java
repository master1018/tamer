public final class JobPriority extends IntegerSyntax
    implements PrintRequestAttribute, PrintJobAttribute {
    private static final long serialVersionUID = -4599900369040602769L;
    public JobPriority(int value) {
        super (value, 1, 100);
    }
    public boolean equals(Object object) {
        return (super.equals (object) && object instanceof JobPriority);
    }
    public final Class<? extends Attribute> getCategory() {
        return JobPriority.class;
    }
    public final String getName() {
        return "job-priority";
    }
}
