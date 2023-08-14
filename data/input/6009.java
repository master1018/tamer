final public class DefaultHostnameVerifier implements HostnameVerifier {
    public boolean verify(String hostname, SSLSession session) {
        return false;
    }
}
