public class RequestDefaultHeaders implements HttpRequestInterceptor {
    public RequestDefaultHeaders() {
        super();
    }
    public void process(final HttpRequest request, final HttpContext context) 
            throws HttpException, IOException {
        if (request == null) {
            throw new IllegalArgumentException("HTTP request may not be null");
        }
        Collection<?> defHeaders = (Collection<?>) request.getParams().getParameter(
                ClientPNames.DEFAULT_HEADERS);
        if (defHeaders != null) {
            for (Object defHeader : defHeaders) {
                request.addHeader((Header) defHeader);
            }
        }
    }
}
