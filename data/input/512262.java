public class HttpRequestHandlerRegistry implements HttpRequestHandlerResolver {
    private final UriPatternMatcher matcher;
    public HttpRequestHandlerRegistry() {
        matcher = new UriPatternMatcher();
    }
    public void register(final String pattern, final HttpRequestHandler handler) {
        matcher.register(pattern, handler);
    }
    public void unregister(final String pattern) {
        matcher.unregister(pattern);
    }
    public void setHandlers(final Map map) {
        matcher.setHandlers(map);
    }
    public HttpRequestHandler lookup(final String requestURI) {
        return (HttpRequestHandler) matcher.lookup(requestURI);
    }
    protected boolean matchUriRequestPattern(final String pattern, final String requestUri) {
        return matcher.matchUriRequestPattern(pattern, requestUri);
    }
}
