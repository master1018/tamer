public abstract class HttpAbstractParamBean {
    protected final HttpParams params;
    public HttpAbstractParamBean (final HttpParams params) {
        if (params == null)
            throw new IllegalArgumentException("HTTP parameters may not be null");
        this.params = params;
    }
}
