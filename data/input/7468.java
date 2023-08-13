public final class JobKOctets   extends IntegerSyntax
        implements PrintRequestAttribute, PrintJobAttribute {
    private static final long serialVersionUID = -8959710146498202869L;
    public JobKOctets(int value) {
        super (value, 0, Integer.MAX_VALUE);
    }
    public boolean equals(Object object) {
        return super.equals(object) && object instanceof JobKOctets;
    }
    public final Class<? extends Attribute> getCategory() {
        return JobKOctets.class;
    }
    public final String getName() {
        return "job-k-octets";
    }
}
