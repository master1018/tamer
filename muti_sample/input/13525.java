public final class JobImpressionsSupported extends SetOfIntegerSyntax
        implements SupportedValuesAttribute {
    private static final long serialVersionUID = -4887354803843173692L;
    public JobImpressionsSupported(int lowerBound, int upperBound) {
        super (lowerBound, upperBound);
        if (lowerBound > upperBound) {
            throw new IllegalArgumentException("Null range specified");
        } else if (lowerBound < 0) {
            throw new IllegalArgumentException(
                                         "Job K octets value < 0 specified");
        }
    }
    public boolean equals(Object object) {
        return (super.equals (object) &&
                object instanceof JobImpressionsSupported);
    }
    public final Class<? extends Attribute> getCategory() {
        return JobImpressionsSupported.class;
    }
    public final String getName() {
        return "job-impressions-supported";
    }
}
