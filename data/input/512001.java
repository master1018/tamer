public class StrictHostnameVerifier extends AbstractVerifier {
    public final void verify(
            final String host, 
            final String[] cns,
            final String[] subjectAlts) throws SSLException {
        verify(host, cns, subjectAlts, true);
    }
    @Override
    public final String toString() { 
        return "STRICT"; 
    }
}
