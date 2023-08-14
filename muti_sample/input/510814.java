public class BasicHttpEntityEnclosingRequest 
            extends BasicHttpRequest implements HttpEntityEnclosingRequest {
    private HttpEntity entity;
    public BasicHttpEntityEnclosingRequest(final String method, final String uri) {
        super(method, uri);
    }
    public BasicHttpEntityEnclosingRequest(final String method, final String uri, 
            final ProtocolVersion ver) {
        this(new BasicRequestLine(method, uri, ver));
    }
    public BasicHttpEntityEnclosingRequest(final RequestLine requestline) {
        super(requestline);
    }
    public HttpEntity getEntity() {
        return this.entity;
    }
    public void setEntity(final HttpEntity entity) {
        this.entity = entity;
    }
    public boolean expectContinue() {
        Header expect = getFirstHeader(HTTP.EXPECT_DIRECTIVE);
        return expect != null && HTTP.EXPECT_CONTINUE.equalsIgnoreCase(expect.getValue());
    }
}
