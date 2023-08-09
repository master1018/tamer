public class BrowserCompatSpecFactory implements CookieSpecFactory {    
    public CookieSpec newInstance(final HttpParams params) {
        if (params != null) {
            return new BrowserCompatSpec(
                    (String []) params.getParameter(CookieSpecPNames.DATE_PATTERNS));
        } else {
            return new BrowserCompatSpec();
        }
    }
}
