public class PrintQuality extends EnumSyntax
    implements DocAttribute, PrintRequestAttribute, PrintJobAttribute {
    private static final long serialVersionUID = -3072341285225858365L;
    public static final PrintQuality DRAFT = new PrintQuality(3);
    public static final PrintQuality NORMAL = new PrintQuality(4);
    public static final PrintQuality HIGH = new PrintQuality(5);
    protected PrintQuality(int value) {
        super (value);
    }
    private static final String[] myStringTable = {
        "draft",
        "normal",
        "high"
    };
    private static final PrintQuality[] myEnumValueTable = {
        DRAFT,
        NORMAL,
        HIGH
    };
    protected String[] getStringTable() {
        return (String[])myStringTable.clone();
    }
    protected EnumSyntax[] getEnumValueTable() {
        return (EnumSyntax[])myEnumValueTable.clone();
    }
    protected int getOffset() {
        return 3;
    }
    public final Class<? extends Attribute> getCategory() {
        return PrintQuality.class;
    }
    public final String getName() {
        return "print-quality";
    }
}
