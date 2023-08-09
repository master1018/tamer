public class PDLOverrideSupported extends EnumSyntax
    implements PrintServiceAttribute {
    private static final long serialVersionUID = -4393264467928463934L;
    public static final PDLOverrideSupported
        NOT_ATTEMPTED = new PDLOverrideSupported(0);
    public static final PDLOverrideSupported
        ATTEMPTED = new PDLOverrideSupported(1);
    protected PDLOverrideSupported(int value) {
        super (value);
    }
    private static final String[] myStringTable = {
        "not-attempted",
        "attempted"
    };
    private static final PDLOverrideSupported[] myEnumValueTable = {
        NOT_ATTEMPTED,
        ATTEMPTED
    };
    protected String[] getStringTable() {
        return (String[])myStringTable.clone();
    }
    protected EnumSyntax[] getEnumValueTable() {
        return (EnumSyntax[])myEnumValueTable.clone();
    }
    public final Class<? extends Attribute> getCategory() {
        return PDLOverrideSupported.class;
    }
    public final String getName() {
        return "pdl-override-supported";
    }
}
