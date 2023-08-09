public class RequestConnControl implements HttpRequestInterceptor {
    public RequestConnControl() {
        super();
    }
    public void process(final HttpRequest request, final HttpContext context) 
            throws HttpException, IOException {
        if (request == null) {
            throw new IllegalArgumentException("HTTP request may not be null");
        }
        if (!request.containsHeader(HTTP.CONN_DIRECTIVE)) {
            request.addHeader(HTTP.CONN_DIRECTIVE, HTTP.CONN_KEEP_ALIVE);
        }
    }
}
