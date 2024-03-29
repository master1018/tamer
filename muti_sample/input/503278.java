public class MySSLContextSpi extends SSLContextSpi {
    private boolean init = false;
    protected void engineInit(KeyManager[] km, TrustManager[] tm,
            SecureRandom sr) throws KeyManagementException {
        if (sr == null) {
            throw new KeyManagementException(
                    "secureRandom is null");
        }
        init = true;
    }
    protected SSLSocketFactory engineGetSocketFactory() {
        if (!init) {
            throw new RuntimeException("Not initialiazed");
        }   
        return null;
    }
    protected SSLServerSocketFactory engineGetServerSocketFactory() {
        if (!init) {
            throw new RuntimeException("Not initialiazed");
        }
        return null;
    }
    protected SSLSessionContext engineGetServerSessionContext() {
        if (!init) {
            throw new RuntimeException("Not initialiazed");
        }
        return null;
    }
    protected SSLSessionContext engineGetClientSessionContext() {
        if (!init) {
            throw new RuntimeException("Not initialiazed");
        }
        return null;
    }
    protected SSLEngine engineCreateSSLEngine(String host, int port) {
        if (!init) {
            throw new RuntimeException("Not initialiazed");
        }
        return new tmpSSLEngine(host, port);
    }
    protected SSLEngine engineCreateSSLEngine() {
        if (!init) {
            throw new RuntimeException("Not initialiazed");
        }
        return new tmpSSLEngine();
    }
    public class tmpSSLEngine extends SSLEngine {
        String tmpHost;
        int tmpPort;
        public tmpSSLEngine() {
            tmpHost = null;
            tmpPort = 0;        
        }
        public tmpSSLEngine(String host, int port) {
            tmpHost = host;
            tmpPort = port;        
        }
        public String getPeerHost() {
            return tmpHost;        
        }
        public int getPeerPort() {
            return tmpPort;
        }
        public void beginHandshake() throws SSLException { }
        public void closeInbound() throws SSLException { }
        public void closeOutbound() {}
        public Runnable getDelegatedTask() { return null; }
        public String[] getEnabledCipherSuites() { return null; }
        public String[] getEnabledProtocols() {return null; }
        public boolean getEnableSessionCreation() { return true; }
        public SSLEngineResult.HandshakeStatus getHandshakeStatus() { return null; }
        public boolean getNeedClientAuth() { return true; }
        public SSLSession getSession() { return null; }
        public String[] getSupportedCipherSuites()  { return null; }
        public String[] getSupportedProtocols()  { return null; }
        public boolean getUseClientMode()  { return true; }
        public boolean getWantClientAuth()  { return true; }
        public boolean isInboundDone()  { return true; }
        public boolean isOutboundDone()  { return true; }
        public void setEnabledCipherSuites(String[] suites) { }
        public void setEnabledProtocols(String[] protocols) { }
        public void setEnableSessionCreation(boolean flag) { }
        public void setNeedClientAuth(boolean need) { }
        public void setUseClientMode(boolean mode) { }
        public void setWantClientAuth(boolean want) { }        
        public SSLEngineResult unwrap(ByteBuffer src, ByteBuffer[] dsts,
                int offset, int length) throws SSLException {
            return null;
        }        
        public SSLEngineResult wrap(ByteBuffer[] srcs, int offset,
                int length, ByteBuffer dst) throws SSLException { 
            return null;
        }
    }
}
