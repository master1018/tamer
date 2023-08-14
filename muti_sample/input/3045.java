public final class JobName extends TextSyntax
        implements PrintRequestAttribute, PrintJobAttribute {
    private static final long serialVersionUID = 4660359192078689545L;
    public JobName(String jobName, Locale locale) {
        super (jobName, locale);
    }
    public boolean equals(Object object) {
        return (super.equals(object) && object instanceof JobName);
    }
    public final Class<? extends Attribute> getCategory() {
        return JobName.class;
    }
    public final String getName() {
        return "job-name";
    }
}
