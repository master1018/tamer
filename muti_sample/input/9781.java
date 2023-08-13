class SSLServerSocketImpl extends SSLServerSocket
{
    private SSLContextImpl      sslContext;
    private byte                doClientAuth = SSLEngineImpl.clauth_none;
    private boolean             useServerMode = true;
    private boolean             enableSessionCreation = true;
    private CipherSuiteList     enabledCipherSuites = null;
    private ProtocolList        enabledProtocols = null;
    private boolean             checkedEnabled = false;
    private String              identificationProtocol = null;
    private AlgorithmConstraints    algorithmConstraints = null;
    SSLServerSocketImpl(int port, int backlog, SSLContextImpl context)
    throws IOException, SSLException
    {
        super(port, backlog);
        initServer(context);
    }
    SSLServerSocketImpl(
        int             port,
        int             backlog,
        InetAddress     address,
        SSLContextImpl  context)
        throws IOException
    {
        super(port, backlog, address);
        initServer(context);
    }
    SSLServerSocketImpl(SSLContextImpl context) throws IOException {
        super();
        initServer(context);
    }
    private void initServer(SSLContextImpl context) throws SSLException {
        if (context == null) {
            throw new SSLException("No Authentication context given");
        }
        sslContext = context;
        enabledCipherSuites = sslContext.getDefaultCipherSuiteList(true);
        enabledProtocols = sslContext.getDefaultProtocolList(true);
    }
    public String[] getSupportedCipherSuites() {
        return sslContext.getSuportedCipherSuiteList().toStringArray();
    }
    synchronized public String[] getEnabledCipherSuites() {
        return enabledCipherSuites.toStringArray();
    }
    synchronized public void setEnabledCipherSuites(String[] suites) {
        enabledCipherSuites = new CipherSuiteList(suites);
        checkedEnabled = false;
    }
    public String[] getSupportedProtocols() {
        return sslContext.getSuportedProtocolList().toStringArray();
    }
    synchronized public void setEnabledProtocols(String[] protocols) {
        enabledProtocols = new ProtocolList(protocols);
    }
    synchronized public String[] getEnabledProtocols() {
        return enabledProtocols.toStringArray();
    }
    public void setNeedClientAuth(boolean flag) {
        doClientAuth = (flag ?
            SSLEngineImpl.clauth_required : SSLEngineImpl.clauth_none);
    }
    public boolean getNeedClientAuth() {
        return (doClientAuth == SSLEngineImpl.clauth_required);
    }
    public void setWantClientAuth(boolean flag) {
        doClientAuth = (flag ?
            SSLEngineImpl.clauth_requested : SSLEngineImpl.clauth_none);
    }
    public boolean getWantClientAuth() {
        return (doClientAuth == SSLEngineImpl.clauth_requested);
    }
    public void setUseClientMode(boolean flag) {
        if (useServerMode != (!flag) &&
                sslContext.isDefaultProtocolList(enabledProtocols)) {
            enabledProtocols = sslContext.getDefaultProtocolList(!flag);
        }
        useServerMode = !flag;
    }
    public boolean getUseClientMode() {
        return !useServerMode;
    }
    public void setEnableSessionCreation(boolean flag) {
        enableSessionCreation = flag;
    }
    public boolean getEnableSessionCreation() {
        return enableSessionCreation;
    }
    synchronized public SSLParameters getSSLParameters() {
        SSLParameters params = super.getSSLParameters();
        params.setEndpointIdentificationAlgorithm(identificationProtocol);
        params.setAlgorithmConstraints(algorithmConstraints);
        return params;
    }
    synchronized public void setSSLParameters(SSLParameters params) {
        super.setSSLParameters(params);
        identificationProtocol = params.getEndpointIdentificationAlgorithm();
        algorithmConstraints = params.getAlgorithmConstraints();
    }
    public Socket accept() throws IOException {
        SSLSocketImpl s = new SSLSocketImpl(sslContext, useServerMode,
            enabledCipherSuites, doClientAuth, enableSessionCreation,
            enabledProtocols, identificationProtocol, algorithmConstraints);
        implAccept(s);
        s.doneConnect();
        return s;
    }
    public String toString() {
        return "[SSL: "+ super.toString() + "]";
    }
}
