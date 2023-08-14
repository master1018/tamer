public final class JobPrioritySupported extends IntegerSyntax
    implements SupportedValuesAttribute {
    private static final long serialVersionUID = 2564840378013555894L;
    public JobPrioritySupported(int value) {
        super (value, 1, 100);
    }
    public boolean equals (Object object) {
        return (super.equals(object) &&
               object instanceof JobPrioritySupported);
    }
    public final Class<? extends Attribute> getCategory() {
        return JobPrioritySupported.class;
    }
    public final String getName() {
        return "job-priority-supported";
    }
}
