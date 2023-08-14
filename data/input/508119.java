public class HttpHead extends HttpRequestBase {
    public final static String METHOD_NAME = "HEAD";
    public HttpHead() {
        super();
    }
    public HttpHead(final URI uri) {
        super();
        setURI(uri);
    }
    public HttpHead(final String uri) {
        super();
        setURI(URI.create(uri));
    }
    @Override
    public String getMethod() {
        return METHOD_NAME;
    }
}
