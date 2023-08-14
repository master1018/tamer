public final class NumberUpSupported    extends SetOfIntegerSyntax
        implements SupportedValuesAttribute {
     private static final long serialVersionUID = -1041573395759141805L;
    public NumberUpSupported(int[][] members) {
        super (members);
        if (members == null) {
            throw new NullPointerException("members is null");
        }
        int[][] myMembers = getMembers();
        int n = myMembers.length;
        if (n == 0) {
            throw new IllegalArgumentException("members is zero-length");
        }
        int i;
        for (i = 0; i < n; ++ i) {
            if (myMembers[i][0] < 1) {
                throw new IllegalArgumentException
                    ("Number up value must be > 0");
            }
        }
    }
    public NumberUpSupported(int member) {
        super (member);
        if (member < 1) {
            throw new IllegalArgumentException("Number up value must be > 0");
        }
    }
    public NumberUpSupported(int lowerBound, int upperBound) {
        super (lowerBound, upperBound);
        if (lowerBound > upperBound) {
            throw new IllegalArgumentException("Null range specified");
        } else if (lowerBound < 1) {
            throw new IllegalArgumentException
                ("Number up value must be > 0");
        }
    }
    public boolean equals(Object object) {
        return (super.equals (object) &&
                object instanceof NumberUpSupported);
    }
    public final Class<? extends Attribute> getCategory() {
        return NumberUpSupported.class;
    }
    public final String getName() {
        return "number-up-supported";
    }
}
