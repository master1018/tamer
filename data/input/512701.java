public abstract class SSLEngine {
    private final String peerHost;
    private final int peerPort;
    protected SSLEngine() {
        super();
        peerHost = null;
        peerPort = -1;
    }
    protected SSLEngine(String host, int port) {
        super();
        this.peerHost = host;
        this.peerPort = port;
    }
    public String getPeerHost() {
        return peerHost;
    }
    public int getPeerPort() {
        return peerPort;
    }
    public abstract void beginHandshake() throws SSLException;
    public abstract void closeInbound() throws SSLException;
    public abstract void closeOutbound();
    public abstract Runnable getDelegatedTask();
    public abstract String[] getEnabledCipherSuites();
    public abstract String[] getEnabledProtocols();
    public abstract boolean getEnableSessionCreation();
    public abstract SSLEngineResult.HandshakeStatus getHandshakeStatus();
    public abstract boolean getNeedClientAuth();
    public abstract SSLSession getSession();
    public abstract String[] getSupportedCipherSuites();
    public abstract String[] getSupportedProtocols();
    public abstract boolean getUseClientMode();
    public abstract boolean getWantClientAuth();
    public abstract boolean isInboundDone();
    public abstract boolean isOutboundDone();
    public abstract void setEnabledCipherSuites(String[] suites);
    public abstract void setEnabledProtocols(String[] protocols);
    public abstract void setEnableSessionCreation(boolean flag);
    public abstract void setNeedClientAuth(boolean need);
    public abstract void setUseClientMode(boolean mode);
    public abstract void setWantClientAuth(boolean want);
    public abstract SSLEngineResult unwrap(ByteBuffer src, ByteBuffer[] dsts, int offset, int length)
            throws SSLException;
    public abstract SSLEngineResult wrap(ByteBuffer[] srcs, int offset, int length, ByteBuffer dst)
            throws SSLException;
    public SSLEngineResult unwrap(ByteBuffer src, ByteBuffer dst) throws SSLException {
        return unwrap(src, new ByteBuffer[] { dst }, 0, 1);
    }
    public SSLEngineResult unwrap(ByteBuffer src, ByteBuffer[] dsts) throws SSLException {
        if (dsts == null) {
            throw new IllegalArgumentException("Byte buffer array dsts is null");
        }
        return unwrap(src, dsts, 0, dsts.length);
    }
    public SSLEngineResult wrap(ByteBuffer[] srcs, ByteBuffer dst) throws SSLException {
        if (srcs == null) {
            throw new IllegalArgumentException("Byte buffer array srcs is null");
        }
        return wrap(srcs, 0, srcs.length, dst);
    }
    public SSLEngineResult wrap(ByteBuffer src, ByteBuffer dst) throws SSLException {
        return wrap(new ByteBuffer[] { src }, 0, 1, dst);
    }
}
