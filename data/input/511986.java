public class HttpOptions extends HttpRequestBase {
    public final static String METHOD_NAME = "OPTIONS";
    public HttpOptions() {
        super();
    }
    public HttpOptions(final URI uri) {
        super();
        setURI(uri);
    }
    public HttpOptions(final String uri) {
        super();
        setURI(URI.create(uri));
    }
    @Override
    public String getMethod() {
        return METHOD_NAME;
    }
    public Set<String> getAllowedMethods(final HttpResponse response) {
        if (response == null) {
            throw new IllegalArgumentException("HTTP response may not be null");
        }
        HeaderIterator it = response.headerIterator("Allow");
        Set<String> methods = new HashSet<String>();
        while (it.hasNext()) {
            Header header = it.nextHeader();
            HeaderElement[] elements = header.getElements();
            for (HeaderElement element : elements) {
                methods.add(element.getName());
            }
        }
        return methods;
    }
}
