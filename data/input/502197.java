public class BrowserCompatHostnameVerifier extends AbstractVerifier {
    public final void verify(
            final String host, 
            final String[] cns,
            final String[] subjectAlts) throws SSLException {
        verify(host, cns, subjectAlts, false);
    }
    @Override
    public final String toString() { 
        return "BROWSER_COMPATIBLE"; 
    }
}
