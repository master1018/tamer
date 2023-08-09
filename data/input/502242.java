public class BasicSchemeFactory implements AuthSchemeFactory {    
    public AuthScheme newInstance(final HttpParams params) {
        return new BasicScheme();
    }
}
