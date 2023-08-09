public final class ColorSupported extends EnumSyntax
    implements PrintServiceAttribute {
    private static final long serialVersionUID = -2700555589688535545L;
    public static final ColorSupported NOT_SUPPORTED = new ColorSupported(0);
    public static final ColorSupported SUPPORTED = new ColorSupported(1);
    protected ColorSupported(int value) {
        super (value);
    }
    private static final String[] myStringTable = {"not-supported",
                                                   "supported"};
    private static final ColorSupported[] myEnumValueTable = {NOT_SUPPORTED,
                                                              SUPPORTED};
    protected String[] getStringTable() {
        return myStringTable;
    }
    protected EnumSyntax[] getEnumValueTable() {
        return myEnumValueTable;
    }
    public final Class<? extends Attribute> getCategory() {
        return ColorSupported.class;
    }
    public final String getName() {
        return "color-supported";
    }
}
