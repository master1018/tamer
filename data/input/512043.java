public class AllowAllHostnameVerifier extends AbstractVerifier {
    public final void verify(
            final String host, 
            final String[] cns,
            final String[] subjectAlts) {
    }
    @Override
    public final String toString() { 
        return "ALLOW_ALL"; 
    }
}
