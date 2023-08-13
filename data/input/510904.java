public class CookieSpecParamBean extends HttpAbstractParamBean {
    public CookieSpecParamBean (final HttpParams params) {
        super(params);
    }
    public void setDatePatterns (final Collection <String> patterns) {
        params.setParameter(CookieSpecPNames.DATE_PATTERNS, patterns);
    }
    public void setSingleHeader (final boolean singleHeader) {
        params.setBooleanParameter(CookieSpecPNames.SINGLE_COOKIE_HEADER, singleHeader);
    }
}
