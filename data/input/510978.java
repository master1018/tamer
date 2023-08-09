public class DigestSchemeFactory implements AuthSchemeFactory {    
    public AuthScheme newInstance(final HttpParams params) {
        return new DigestScheme();
    }
}
