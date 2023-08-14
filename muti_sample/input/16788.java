public abstract class SSLEngine {
    private String peerHost = null;
    private int peerPort = -1;
    protected SSLEngine() {
    }
    protected SSLEngine(String peerHost, int peerPort) {
        this.peerHost = peerHost;
        this.peerPort = peerPort;
    }
    public String getPeerHost() {
        return peerHost;
    }
    public int getPeerPort() {
        return peerPort;
    }
    public SSLEngineResult wrap(ByteBuffer src,
            ByteBuffer dst) throws SSLException {
        return wrap(new ByteBuffer [] { src }, 0, 1, dst);
    }
    public SSLEngineResult wrap(ByteBuffer [] srcs,
            ByteBuffer dst) throws SSLException {
        if (srcs == null) {
            throw new IllegalArgumentException("src == null");
        }
        return wrap(srcs, 0, srcs.length, dst);
    }
    public abstract SSLEngineResult wrap(ByteBuffer [] srcs, int offset,
            int length, ByteBuffer dst) throws SSLException;
    public SSLEngineResult unwrap(ByteBuffer src,
            ByteBuffer dst) throws SSLException {
        return unwrap(src, new ByteBuffer [] { dst }, 0, 1);
    }
    public SSLEngineResult unwrap(ByteBuffer src,
            ByteBuffer [] dsts) throws SSLException {
        if (dsts == null) {
            throw new IllegalArgumentException("dsts == null");
        }
        return unwrap(src, dsts, 0, dsts.length);
    }
    public abstract SSLEngineResult unwrap(ByteBuffer src,
            ByteBuffer [] dsts, int offset, int length) throws SSLException;
    public abstract Runnable getDelegatedTask();
    public abstract void closeInbound() throws SSLException;
    public abstract boolean isInboundDone();
    public abstract void closeOutbound();
    public abstract boolean isOutboundDone();
    public abstract String [] getSupportedCipherSuites();
    public abstract String [] getEnabledCipherSuites();
    public abstract void setEnabledCipherSuites(String suites []);
    public abstract String [] getSupportedProtocols();
    public abstract String [] getEnabledProtocols();
    public abstract void setEnabledProtocols(String protocols[]);
    public abstract SSLSession getSession();
    public SSLSession getHandshakeSession() {
        throw new UnsupportedOperationException();
    }
    public abstract void beginHandshake() throws SSLException;
    public abstract SSLEngineResult.HandshakeStatus getHandshakeStatus();
    public abstract void setUseClientMode(boolean mode);
    public abstract boolean getUseClientMode();
    public abstract void setNeedClientAuth(boolean need);
    public abstract boolean getNeedClientAuth();
    public abstract void setWantClientAuth(boolean want);
    public abstract boolean getWantClientAuth();
    public abstract void setEnableSessionCreation(boolean flag);
    public abstract boolean getEnableSessionCreation();
    public SSLParameters getSSLParameters() {
        SSLParameters params = new SSLParameters();
        params.setCipherSuites(getEnabledCipherSuites());
        params.setProtocols(getEnabledProtocols());
        if (getNeedClientAuth()) {
            params.setNeedClientAuth(true);
        } else if (getWantClientAuth()) {
            params.setWantClientAuth(true);
        }
        return params;
    }
    public void setSSLParameters(SSLParameters params) {
        String[] s;
        s = params.getCipherSuites();
        if (s != null) {
            setEnabledCipherSuites(s);
        }
        s = params.getProtocols();
        if (s != null) {
            setEnabledProtocols(s);
        }
        if (params.getNeedClientAuth()) {
            setNeedClientAuth(true);
        } else if (params.getWantClientAuth()) {
            setWantClientAuth(true);
        } else {
            setWantClientAuth(false);
        }
    }
}
