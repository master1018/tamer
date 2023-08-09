public abstract class SSLServerSocket extends ServerSocket {
    protected SSLServerSocket()
    throws IOException
        { super(); }
    protected SSLServerSocket(int port)
    throws IOException
        { super(port); }
    protected SSLServerSocket(int port, int backlog)
    throws IOException
        { super(port, backlog); }
    protected SSLServerSocket(int port, int backlog, InetAddress address)
    throws IOException
        { super(port, backlog, address); }
    public abstract String [] getEnabledCipherSuites();
    public abstract void setEnabledCipherSuites(String suites []);
    public abstract String [] getSupportedCipherSuites();
    public abstract String [] getSupportedProtocols();
    public abstract String [] getEnabledProtocols();
    public abstract void setEnabledProtocols(String protocols[]);
    public abstract void setNeedClientAuth(boolean need);
    public abstract boolean getNeedClientAuth();
    public abstract void setWantClientAuth(boolean want);
    public abstract boolean getWantClientAuth();
    public abstract void setUseClientMode(boolean mode);
    public abstract boolean getUseClientMode();
    public abstract void setEnableSessionCreation(boolean flag);
    public abstract boolean getEnableSessionCreation();
    public SSLParameters getSSLParameters() {
        SSLParameters parameters = new SSLParameters();
        parameters.setCipherSuites(getEnabledCipherSuites());
        parameters.setProtocols(getEnabledProtocols());
        if (getNeedClientAuth()) {
            parameters.setNeedClientAuth(true);
        } else if (getWantClientAuth()) {
            parameters.setWantClientAuth(true);
        }
        return parameters;
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
