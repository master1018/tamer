 class MemoryByteCode extends SimpleJavaFileObject {
    private ByteArrayOutputStream baos;
    private final String name;
    public MemoryByteCode(String name) {
        super(URI.create("byte:
                Kind.CLASS);
        this.name = name;
    }
    @Override
    public String getName() {
        return name;
    }
    public CharSequence getCharContent(boolean ignoreEncodingErrors) {
        throw new IllegalStateException();
    }
    public OutputStream openOutputStream() {
        baos = new ByteArrayOutputStream();
        return baos;
    }
    public InputStream openInputStream() {
        throw new IllegalStateException();
    }
    public byte[] getBytes() {
        return baos.toByteArray();
    }
}
