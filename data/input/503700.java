public class HttpPost extends HttpEntityEnclosingRequestBase {
    public final static String METHOD_NAME = "POST";
    public HttpPost() {
        super();
    }
    public HttpPost(final URI uri) {
        super();
        setURI(uri);
    }
    public HttpPost(final String uri) {
        super();
        setURI(URI.create(uri));
    }
    @Override
    public String getMethod() {
        return METHOD_NAME;
    }
}
