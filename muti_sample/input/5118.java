public final class Copies extends IntegerSyntax
        implements PrintRequestAttribute, PrintJobAttribute {
    private static final long serialVersionUID = -6426631521680023833L;
    public Copies(int value) {
        super (value, 1, Integer.MAX_VALUE);
    }
    public boolean equals(Object object) {
        return super.equals (object) && object instanceof Copies;
    }
    public final Class<? extends Attribute> getCategory() {
        return Copies.class;
    }
    public final String getName() {
        return "copies";
    }
}
