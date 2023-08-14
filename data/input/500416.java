public class NetscapeDraftSpecFactory implements CookieSpecFactory {    
    public CookieSpec newInstance(final HttpParams params) {
        if (params != null) {
            return new NetscapeDraftSpec(
                    (String []) params.getParameter(CookieSpecPNames.DATE_PATTERNS));
        } else {
            return new NetscapeDraftSpec();
        }
    }
}
