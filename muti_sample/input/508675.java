public class BestMatchSpecFactory implements CookieSpecFactory {    
    public CookieSpec newInstance(final HttpParams params) {
        if (params != null) {
            return new BestMatchSpec(
                    (String []) params.getParameter(CookieSpecPNames.DATE_PATTERNS), 
                    params.getBooleanParameter(CookieSpecPNames.SINGLE_COOKIE_HEADER, false));
        } else {
            return new BestMatchSpec();
        }
    }
}
