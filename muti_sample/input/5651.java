public class Compression extends EnumSyntax implements DocAttribute {
    private static final long serialVersionUID = -5716748913324997674L;
    public static final Compression NONE = new Compression(0);
    public static final Compression DEFLATE = new Compression(1);
    public static final Compression GZIP = new Compression(2);
    public static final Compression COMPRESS = new Compression(3);
    protected Compression(int value) {
        super(value);
    }
    private static final String[] myStringTable = {"none",
                                                   "deflate",
                                                   "gzip",
                                                   "compress"};
    private static final Compression[] myEnumValueTable = {NONE,
                                                           DEFLATE,
                                                           GZIP,
                                                           COMPRESS};
    protected String[] getStringTable() {
        return (String[])myStringTable.clone();
    }
    protected EnumSyntax[] getEnumValueTable() {
        return (EnumSyntax[])myEnumValueTable.clone();
    }
    public final Class<? extends Attribute> getCategory() {
        return Compression.class;
    }
    public final String getName() {
        return "compression";
    }
}
