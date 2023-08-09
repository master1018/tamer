public class AuthParamBean extends HttpAbstractParamBean {
    public AuthParamBean (final HttpParams params) {
        super(params);
    }
    public void setCredentialCharset (final String charset) {
        AuthParams.setCredentialCharset(params, charset);
    }
}
