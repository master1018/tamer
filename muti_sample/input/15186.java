public final class NumberUp extends IntegerSyntax
    implements DocAttribute, PrintRequestAttribute, PrintJobAttribute {
    private static final long serialVersionUID = -3040436486786527811L;
    public NumberUp(int value) {
        super (value, 1, Integer.MAX_VALUE);
    }
    public boolean equals(Object object) {
        return (super.equals(object) && object instanceof NumberUp);
    }
    public final Class<? extends Attribute> getCategory() {
        return NumberUp.class;
    }
    public final String getName() {
        return "number-up";
    }
}
