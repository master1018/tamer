public class MySSLEngineImpl extends SSLEngine {
    private static String[] supportedCS = CipherSuites.CUSTOM;
    public static void useStandardCipherSuites() {
        supportedCS = CipherSuites.STANDARD;
    }
    public static void useCustomCipherSuites() {
        supportedCS = CipherSuites.CUSTOM;
    }
    public MySSLEngineImpl() {
        super();
    }
    public MySSLEngineImpl(String host, int port) {
        super(host, port);
    }
    public SSLEngineResult wrap(ByteBuffer [] src, int off, int len,
        ByteBuffer dst) throws SSLException { return null; }
    public SSLEngineResult unwrap(ByteBuffer src,
        ByteBuffer [] dst, int off, int len)
        throws SSLException { return null; }
    public Runnable getDelegatedTask() { return null; }
    public void closeInbound() {}
    public boolean isInboundDone() { return false; }
    public void closeOutbound() {}
    public boolean isOutboundDone() { return false; }
    public String[] getEnabledCipherSuites() {
        return getSupportedCipherSuites();
    }
    public String[] getSupportedCipherSuites() {
        return (String[]) supportedCS.clone();
    }
    public void setEnabledCipherSuites(String[] suites) {}
    public String[] getSupportedProtocols() { return null; }
    public String[] getEnabledProtocols() { return null; }
    public void setEnabledProtocols(String[] protocols) {}
    public SSLSession getSession() { return null; }
    public void beginHandshake() throws SSLException {}
    public SSLEngineResult.HandshakeStatus getHandshakeStatus() {
        return SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING;
    }
    public void setUseClientMode(boolean mode) {};
    public boolean getUseClientMode() { return false; }
    public void setNeedClientAuth(boolean need) {}
    public boolean getNeedClientAuth() { return false; }
    public void setWantClientAuth(boolean need) {}
    public boolean getWantClientAuth() { return false; }
    public void setEnableSessionCreation(boolean flag) {}
    public boolean getEnableSessionCreation() { return false; }
}
