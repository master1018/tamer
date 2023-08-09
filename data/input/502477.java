public class RFC2109SpecFactory implements CookieSpecFactory {    
    public CookieSpec newInstance(final HttpParams params) {
        if (params != null) {
            return new RFC2109Spec(
                    (String []) params.getParameter(CookieSpecPNames.DATE_PATTERNS), 
                    params.getBooleanParameter(CookieSpecPNames.SINGLE_COOKIE_HEADER, false));
        } else {
            return new RFC2109Spec();
        }
    }
}
