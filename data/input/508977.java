public class StreamSource implements Source {
    public static final String FEATURE =
        "http:
    public StreamSource() { }
    public StreamSource(InputStream inputStream) {
        setInputStream(inputStream);
    }
    public StreamSource(InputStream inputStream, String systemId) {
        setInputStream(inputStream);
        setSystemId(systemId);
    }
    public StreamSource(Reader reader) {
        setReader(reader);
    }
    public StreamSource(Reader reader, String systemId) {
        setReader(reader);
        setSystemId(systemId);
    }
    public StreamSource(String systemId) {
        this.systemId = systemId;
    }
    public StreamSource(File f) {
        setSystemId(f);
    }
    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }
    public InputStream getInputStream() {
        return inputStream;
    }
    public void setReader(Reader reader) {
        this.reader = reader;
    }
    public Reader getReader() {
        return reader;
    }
    public void setPublicId(String publicId) {
        this.publicId = publicId;
    }
    public String getPublicId() {
        return publicId;
    }
    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }
    public String getSystemId() {
        return systemId;
    }
    public void setSystemId(File f) {
        this.systemId = FilePathToURI.filepath2URI(f.getAbsolutePath());
    }
    private String publicId;
    private String systemId;
    private InputStream inputStream;
    private Reader reader;
}
