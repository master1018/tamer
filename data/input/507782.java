public abstract class HttpEntityEnclosingRequestBase 
    extends HttpRequestBase implements HttpEntityEnclosingRequest {
    private HttpEntity entity;
    public HttpEntityEnclosingRequestBase() {
        super();
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
    public Object clone() throws CloneNotSupportedException {
        HttpEntityEnclosingRequestBase clone = 
            (HttpEntityEnclosingRequestBase) super.clone();
        if (this.entity != null) {
            clone.entity = (HttpEntity) CloneUtils.clone(this.entity);
        }
        return clone;
    }
}
