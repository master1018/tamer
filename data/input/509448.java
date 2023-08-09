public class BasicRequestLine implements RequestLine, Cloneable {
    private final ProtocolVersion protoversion;
    private final String method;
    private final String uri;
    public BasicRequestLine(final String method,
                            final String uri,
                            final ProtocolVersion version) {
        super();
        if (method == null) {
            throw new IllegalArgumentException
                ("Method must not be null.");
        }
        if (uri == null) {
            throw new IllegalArgumentException
                ("URI must not be null.");
        }
        if (version == null) {
            throw new IllegalArgumentException
                ("Protocol version must not be null.");
        }
        this.method = method;
        this.uri = uri;
        this.protoversion = version;
    }
    public String getMethod() {
        return this.method;
    }
    public ProtocolVersion getProtocolVersion() {
        return this.protoversion;
    }
    public String getUri() {
        return this.uri;
    }
    public String toString() {
        return BasicLineFormatter.DEFAULT
            .formatRequestLine(null, this).toString();
    }
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
