public class MySSLSocketFacImpl extends SSLSocketFactory {
    private static String[] supportedCS = CipherSuites.CUSTOM;
    public static void useStandardCipherSuites() {
        supportedCS = CipherSuites.STANDARD;
    }
    public static void useCustomCipherSuites() {
        supportedCS = CipherSuites.CUSTOM;
    }
    public MySSLSocketFacImpl() {
        super();
    }
    public String[] getDefaultCipherSuites() {
        return (String[]) supportedCS.clone();
    }
    public String[] getSupportedCipherSuites() {
        return getDefaultCipherSuites();
    }
    public Socket createSocket(Socket s, String host, int port,
        boolean autoClose) { return new MySSLSocket(this); }
    public Socket createSocket(InetAddress host, int port) {
        return new MySSLSocket(this);
    }
    public Socket createSocket(InetAddress address, int port,
        InetAddress localAddress, int localPort) {
        return new MySSLSocket(this);
    }
    public Socket createSocket(String host, int port) {
        return new MySSLSocket(this);
    }
    public Socket createSocket(String host, int port, InetAddress
        localHost, int localPort) { return new MySSLSocket(this); }
}
class MySSLSocket extends SSLSocket {
    SSLSocketFactory fac = null;
    public MySSLSocket(SSLSocketFactory fac) {
        this.fac = fac;
    }
    public String[] getSupportedCipherSuites() {
        return fac.getSupportedCipherSuites();
    }
    public String[] getEnabledCipherSuites() {
        return fac.getSupportedCipherSuites();
    }
    public void setEnabledCipherSuites(String suites[]) {}
    public String[] getSupportedProtocols() { return null; }
    public String[] getEnabledProtocols() { return null; }
    public void setEnabledProtocols(String protocols[]) {}
    public SSLSession getSession() { return null; }
    public void addHandshakeCompletedListener
        (HandshakeCompletedListener listener) {}
    public void removeHandshakeCompletedListener
        (HandshakeCompletedListener listener) {}
    public void startHandshake() throws IOException {}
    public void setUseClientMode(boolean mode) {}
    public boolean getUseClientMode() { return true; }
    public void setNeedClientAuth(boolean need) {}
    public boolean getNeedClientAuth() { return false; }
    public void setWantClientAuth(boolean want) {}
    public boolean getWantClientAuth() { return false; }
    public void setEnableSessionCreation(boolean flag) {}
    public boolean getEnableSessionCreation() { return true; }
}
