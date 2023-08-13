public final class JobKOctetsSupported extends SetOfIntegerSyntax
    implements SupportedValuesAttribute {
    private static final long serialVersionUID = -2867871140549897443L;
    public JobKOctetsSupported(int lowerBound, int upperBound) {
        super (lowerBound, upperBound);
        if (lowerBound > upperBound) {
            throw new IllegalArgumentException("Null range specified");
        } else if (lowerBound < 0) {
            throw new IllegalArgumentException
                ("Job K octets value < 0 specified");
        }
    }
    public boolean equals(Object object) {
        return (super.equals (object) &&
                object instanceof JobKOctetsSupported);
    }
    public final Class<? extends Attribute> getCategory() {
        return JobKOctetsSupported.class;
    }
    public final String getName() {
        return "job-k-octets-supported";
    }
}
