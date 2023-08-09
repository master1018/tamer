public class HttpProtocolParamBean extends HttpAbstractParamBean {
    public HttpProtocolParamBean (final HttpParams params) {
        super(params);
    }
    public void setHttpElementCharset (final String httpElementCharset) {
        HttpProtocolParams.setHttpElementCharset(params, httpElementCharset);
    }
    public void setContentCharset (final String contentCharset) {
        HttpProtocolParams.setContentCharset(params, contentCharset);
    }
    public void setVersion (final HttpVersion version) {
        HttpProtocolParams.setVersion(params, version);
    }
    public void setUserAgent (final String userAgent) {
        HttpProtocolParams.setUserAgent(params, userAgent);
    }
    public void setUseExpectContinue (boolean useExpectContinue) {
        HttpProtocolParams.setUseExpectContinue(params, useExpectContinue);
    }
}
