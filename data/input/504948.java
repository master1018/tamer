public class JavaSource extends SimpleJavaFileObject {
    private String src;
    private final String sourceName;
    public JavaSource(String sourceName, String sourceCode) {
        super(URI.create("string:
                Kind.SOURCE);
        this.sourceName = sourceName;
        this.src = sourceCode;
    }
    @Override
    public String getName() {
       return sourceName;
    }
    public CharSequence getCharContent(boolean ignoreEncodingErrors) {
        return src;
    }
    public OutputStream openOutputStream() {
        throw new IllegalStateException();
    }
    public InputStream openInputStream() {
        return new ByteArrayInputStream(src.getBytes());
    }
}