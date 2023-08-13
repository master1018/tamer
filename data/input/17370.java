public class DummyVerifier implements HostnameVerifier {
    public boolean verify (String s, SSLSession s1) {
        return true;
    }
}
