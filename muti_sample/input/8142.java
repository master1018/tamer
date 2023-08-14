public final class JobKOctetsProcessed extends IntegerSyntax
        implements PrintJobAttribute {
    private static final long serialVersionUID = -6265238509657881806L;
    public JobKOctetsProcessed(int value) {
        super (value, 0, Integer.MAX_VALUE);
    }
    public boolean equals(Object object) {
        return(super.equals (object) &&
               object instanceof JobKOctetsProcessed);
    }
    public final Class<? extends Attribute> getCategory() {
        return JobKOctetsProcessed.class;
    }
    public final String getName() {
        return "job-k-octets-processed";
    }
}
