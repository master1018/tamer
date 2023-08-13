public final class Sides extends EnumSyntax
    implements DocAttribute, PrintRequestAttribute, PrintJobAttribute {
    private static final long serialVersionUID = -6890309414893262822L;
    public static final Sides ONE_SIDED = new Sides(0);
    public static final Sides TWO_SIDED_LONG_EDGE = new Sides(1);
    public static final Sides TWO_SIDED_SHORT_EDGE = new Sides(2);
    public static final Sides DUPLEX = TWO_SIDED_LONG_EDGE;
    public static final Sides TUMBLE = TWO_SIDED_SHORT_EDGE;
    protected Sides(int value) {
        super (value);
    }
    private static final String[] myStringTable = {
        "one-sided",
        "two-sided-long-edge",
        "two-sided-short-edge"
    };
    private static final Sides[] myEnumValueTable = {
        ONE_SIDED,
        TWO_SIDED_LONG_EDGE,
        TWO_SIDED_SHORT_EDGE
    };
    protected String[] getStringTable() {
        return myStringTable;
    }
    protected EnumSyntax[] getEnumValueTable() {
        return myEnumValueTable;
    }
    public final Class<? extends Attribute> getCategory() {
        return Sides.class;
    }
    public final String getName() {
        return "sides";
    }
}
