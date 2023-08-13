public final class OrientationRequested extends EnumSyntax
    implements DocAttribute, PrintRequestAttribute, PrintJobAttribute {
    private static final long serialVersionUID = -4447437289862822276L;
    public static final OrientationRequested
        PORTRAIT = new OrientationRequested(3);
    public static final OrientationRequested
        LANDSCAPE = new OrientationRequested(4);
    public static final OrientationRequested
        REVERSE_LANDSCAPE = new OrientationRequested(5);
    public static final OrientationRequested
        REVERSE_PORTRAIT = new OrientationRequested(6);
    protected OrientationRequested(int value) {
        super(value);
    }
    private static final String[] myStringTable = {
        "portrait",
        "landscape",
        "reverse-landscape",
        "reverse-portrait"
    };
    private static final OrientationRequested[] myEnumValueTable = {
        PORTRAIT,
        LANDSCAPE,
        REVERSE_LANDSCAPE,
        REVERSE_PORTRAIT
    };
    protected String[] getStringTable() {
        return myStringTable;
    }
    protected EnumSyntax[] getEnumValueTable() {
        return myEnumValueTable;
    }
    protected int getOffset() {
        return 3;
    }
    public final Class<? extends Attribute> getCategory() {
        return OrientationRequested.class;
    }
    public final String getName() {
        return "orientation-requested";
    }
}
