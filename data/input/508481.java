public class SSLParameters implements Cloneable {
    private static X509KeyManager defaultKeyManager;
    private static X509TrustManager defaultTrustManager;
    private static SecureRandom defaultSecureRandom;
    private static SSLParameters defaultParameters;
    private final ClientSessionContext clientSessionContext;
    private final ServerSessionContext serverSessionContext;
    private X509KeyManager keyManager;
    private X509TrustManager trustManager;
    private SecureRandom secureRandom;
    private CipherSuite[] enabledCipherSuites;
    private String[] enabledCipherSuiteNames = null;
    private String[] enabledProtocols = ProtocolVersion.supportedProtocols;
    private boolean client_mode = true;
    private boolean need_client_auth = false;
    private boolean want_client_auth = false;
    private boolean enable_session_creation = true;
    protected CipherSuite[] getEnabledCipherSuitesMember() {
        if (enabledCipherSuites == null) this.enabledCipherSuites = CipherSuite.defaultCipherSuites;
        return enabledCipherSuites;
    }
    private int ssl_ctx = 0;
    private native int nativeinitsslctx();
    protected synchronized int getSSLCTX() {
        if (ssl_ctx == 0) ssl_ctx = nativeinitsslctx();
        return ssl_ctx;
    }
    protected SSLParameters(KeyManager[] kms, TrustManager[] tms,
            SecureRandom sr, SSLClientSessionCache clientCache,
            SSLServerSessionCache serverCache)
            throws KeyManagementException {
        this.serverSessionContext
                = new ServerSessionContext(this, serverCache);
        this.clientSessionContext
                = new ClientSessionContext(this, clientCache);
        try {
            boolean initialize_default = false;
            if ((kms == null) || (kms.length == 0)) {
                if (defaultKeyManager == null) {
                    KeyManagerFactory kmf = KeyManagerFactory.getInstance(
                            KeyManagerFactory.getDefaultAlgorithm());
                    kmf.init(null, null);                
                    kms = kmf.getKeyManagers();
                    initialize_default = true;
                } else {
                    keyManager = defaultKeyManager;
                }
            }
            if (keyManager == null) { 
                for (int i = 0; i < kms.length; i++) {
                    if (kms[i] instanceof X509KeyManager) {
                        keyManager = (X509KeyManager)kms[i];
                        break;
                    }
                }
                if (keyManager == null) {
                    throw new KeyManagementException("No X509KeyManager found");
                }
                if (initialize_default) {
                    defaultKeyManager = keyManager;
                }
            }
            initialize_default = false;
            if ((tms == null) || (tms.length == 0)) {
                if (defaultTrustManager == null) {
                    TrustManagerFactory tmf = TrustManagerFactory
                        .getInstance(TrustManagerFactory.getDefaultAlgorithm());
                    tmf.init((KeyStore)null);
                    tms = tmf.getTrustManagers();
                    initialize_default = true;
                } else {
                    trustManager = defaultTrustManager;
                }
            }
            if (trustManager == null) { 
                for (int i = 0; i < tms.length; i++) {
                    if (tms[i] instanceof X509TrustManager) {
                        trustManager = (X509TrustManager)tms[i];
                        break;
                    }
                }
                if (trustManager == null) {
                    throw new KeyManagementException("No X509TrustManager found");
                }
                if (initialize_default) {
                    defaultTrustManager = trustManager;
                    if (trustManager instanceof TrustManagerImpl) {
                        ((TrustManagerImpl) trustManager).indexTrustAnchors();
                    }
                }
            }
        } catch (NoSuchAlgorithmException e) {
            throw new KeyManagementException(e);
        } catch (KeyStoreException e) {
            throw new KeyManagementException(e);
        } catch (UnrecoverableKeyException e) {
            throw new KeyManagementException(e);            
        } catch (CertificateEncodingException e) {
            throw new KeyManagementException(e);
        } catch (InvalidAlgorithmParameterException e) {
            throw new KeyManagementException(e);
        }
            secureRandom = sr;
    }
    protected static SSLParameters getDefault() throws KeyManagementException {
        if (defaultParameters == null) {
            defaultParameters = new SSLParameters(null, null, null, null, null);
        }
        return (SSLParameters) defaultParameters.clone();
    }
    protected ServerSessionContext getServerSessionContext() {
        return serverSessionContext;
    }
    protected ClientSessionContext getClientSessionContext() {
        return clientSessionContext;
    }
    protected X509KeyManager getKeyManager() {
        return keyManager;
    }
    protected X509TrustManager getTrustManager() {
        return trustManager;
    }
    protected SecureRandom getSecureRandom() {
        if (secureRandom != null) return secureRandom;
        if (defaultSecureRandom == null)
        {
            defaultSecureRandom = new SecureRandom();
        }
        secureRandom = defaultSecureRandom;
        return secureRandom;
    }
    protected SecureRandom getSecureRandomMember() {
        return secureRandom;
    }
    protected String[] getEnabledCipherSuites() {
        if (enabledCipherSuiteNames == null) {
            CipherSuite[] enabledCipherSuites = getEnabledCipherSuitesMember();
            enabledCipherSuiteNames = new String[enabledCipherSuites.length];
            for (int i = 0; i< enabledCipherSuites.length; i++) {
                enabledCipherSuiteNames[i] = enabledCipherSuites[i].getName();
            }
        }
        return enabledCipherSuiteNames.clone();
    }
    protected void setEnabledCipherSuites(String[] suites) {
        if (suites == null) {
            throw new IllegalArgumentException("Provided parameter is null");
        }
        CipherSuite[] cipherSuites = new CipherSuite[suites.length];
        for (int i=0; i<suites.length; i++) {
            cipherSuites[i] = CipherSuite.getByName(suites[i]);
            if (cipherSuites[i] == null || !cipherSuites[i].supported) {
                throw new IllegalArgumentException(suites[i] +
                        " is not supported.");
            }
        }
        enabledCipherSuites = cipherSuites;
        enabledCipherSuiteNames = suites;
    }
    protected String[] getEnabledProtocols() {
        return enabledProtocols.clone();
    }
    protected void setEnabledProtocols(String[] protocols) {
        if (protocols == null) {
            throw new IllegalArgumentException("Provided parameter is null");
        }
        for (int i=0; i<protocols.length; i++) {
            if (!ProtocolVersion.isSupported(protocols[i])) {
                throw new IllegalArgumentException("Protocol " + protocols[i] +
                        " is not supported.");
            }
        }
        enabledProtocols = protocols;
    }
    protected void setUseClientMode(boolean mode) {
        client_mode = mode;
    }
    protected boolean getUseClientMode() {
        return client_mode;
    }
    protected void setNeedClientAuth(boolean need) {
        need_client_auth = need;
        want_client_auth = false;
    }
    protected boolean getNeedClientAuth() {
        return need_client_auth;
    }
    protected void setWantClientAuth(boolean want) {
        want_client_auth = want;
        need_client_auth = false;
    }
    protected boolean getWantClientAuth() {
        return want_client_auth;
    }
    protected void setEnableSessionCreation(boolean flag) {
        enable_session_creation = flag;
    }
    protected boolean getEnableSessionCreation() {
        return enable_session_creation;
    }
    @Override
    protected Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }
    public static X509TrustManager getDefaultTrustManager() {
        return defaultTrustManager;
    }
}
