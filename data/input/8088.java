public final class NumberOfDocuments extends IntegerSyntax
    implements PrintJobAttribute {
    private static final long serialVersionUID = 7891881310684461097L;
    public NumberOfDocuments(int value) {
        super (value, 0, Integer.MAX_VALUE);
    }
    public boolean equals(Object object) {
        return (super.equals (object) &&
                object instanceof NumberOfDocuments);
    }
    public final Class<? extends Attribute> getCategory() {
        return NumberOfDocuments.class;
    }
    public final String getName() {
        return "number-of-documents";
    }
}
