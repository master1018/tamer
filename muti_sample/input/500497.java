public final class AttSourceFile extends BaseAttribute {
    public static final String ATTRIBUTE_NAME = "SourceFile";
    private final CstUtf8 sourceFile;
    public AttSourceFile(CstUtf8 sourceFile) {
        super(ATTRIBUTE_NAME);
        if (sourceFile == null) {
            throw new NullPointerException("sourceFile == null");
        }
        this.sourceFile = sourceFile;
    }
    public int byteLength() {
        return 8;
    }
    public CstUtf8 getSourceFile() {
        return sourceFile;
    }
}
