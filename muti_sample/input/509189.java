public class EntityEnclosingRequestWrapper extends RequestWrapper 
    implements HttpEntityEnclosingRequest {
    private HttpEntity entity;
    public EntityEnclosingRequestWrapper(final HttpEntityEnclosingRequest request) 
        throws ProtocolException {
        super(request);
        this.entity = request.getEntity();
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
    @Override
    public boolean isRepeatable() {
        return this.entity == null || this.entity.isRepeatable();
    }
}
