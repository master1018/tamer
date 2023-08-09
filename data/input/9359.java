public final class CopiesSupported extends SetOfIntegerSyntax
        implements SupportedValuesAttribute {
    private static final long serialVersionUID = 6927711687034846001L;
    public CopiesSupported(int member) {
        super (member);
        if (member < 1) {
            throw new IllegalArgumentException("Copies value < 1 specified");
        }
    }
    public CopiesSupported(int lowerBound, int upperBound) {
        super(lowerBound, upperBound);
        if (lowerBound > upperBound) {
            throw new IllegalArgumentException("Null range specified");
        } else if (lowerBound < 1) {
            throw new IllegalArgumentException("Copies value < 1 specified");
        }
    }
    public boolean equals(Object object) {
        return super.equals (object) && object instanceof CopiesSupported;
    }
    public final Class<? extends Attribute> getCategory() {
        return CopiesSupported.class;
    }
    public final String getName() {
        return "copies-supported";
    }
}
