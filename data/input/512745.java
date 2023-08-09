public class RequestUserAgent implements HttpRequestInterceptor {
    public RequestUserAgent() {
        super();
    }
    public void process(final HttpRequest request, final HttpContext context) 
        throws HttpException, IOException {
        if (request == null) {
            throw new IllegalArgumentException("HTTP request may not be null");
        }
        if (!request.containsHeader(HTTP.USER_AGENT)) {
            String useragent = HttpProtocolParams.getUserAgent(request.getParams());
            if (useragent != null) {
                request.addHeader(HTTP.USER_AGENT, useragent);
            }
        }
    }
}
