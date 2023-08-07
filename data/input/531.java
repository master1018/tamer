public class XMPPSSLSocket extends SSLSocket {
    private XMPPConnection conn;
    public XMPPSSLSocket(XMPPConnection conn) {
        this.conn = conn;
    }
    @Override
    public String[] getSupportedCipherSuites() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    @Override
    public String[] getEnabledCipherSuites() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    @Override
    public void setEnabledCipherSuites(String[] arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    @Override
    public String[] getSupportedProtocols() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    @Override
    public String[] getEnabledProtocols() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    @Override
    public void setEnabledProtocols(String[] arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    @Override
    public SSLSession getSession() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    @Override
    public void addHandshakeCompletedListener(HandshakeCompletedListener arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    @Override
    public void removeHandshakeCompletedListener(HandshakeCompletedListener arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    @Override
    public void startHandshake() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    @Override
    public void setUseClientMode(boolean arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    @Override
    public boolean getUseClientMode() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    @Override
    public void setNeedClientAuth(boolean arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    @Override
    public boolean getNeedClientAuth() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    @Override
    public void setWantClientAuth(boolean arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    @Override
    public boolean getWantClientAuth() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    @Override
    public void setEnableSessionCreation(boolean arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    @Override
    public boolean getEnableSessionCreation() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
