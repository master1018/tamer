final class SSLSessionImpl extends ExtendedSSLSession {
    static final SSLSessionImpl         nullSession = new SSLSessionImpl();
    private static final byte           compression_null = 0;
    private final ProtocolVersion       protocolVersion;
    private final SessionId             sessionId;
    private X509Certificate[]   peerCerts;
    private byte                compressionMethod;
    private CipherSuite         cipherSuite;
    private SecretKey           masterSecret;
    private final long          creationTime = System.currentTimeMillis();
    private long                lastUsedTime = 0;
    private final String        host;
    private final int           port;
    private SSLSessionContextImpl       context;
    private int                 sessionCount;
    private boolean             invalidated;
    private X509Certificate[]   localCerts;
    private PrivateKey          localPrivateKey;
    private String[]            localSupportedSignAlgs;
    private String[]            peerSupportedSignAlgs;
    private Principal peerPrincipal;
    private Principal localPrincipal;
    private static volatile int counter = 0;
    private static boolean      defaultRejoinable = true;
    private static final Debug debug = Debug.getInstance("ssl");
    private SSLSessionImpl() {
        this(ProtocolVersion.NONE, CipherSuite.C_NULL, null,
            new SessionId(false, null), null, -1);
    }
    SSLSessionImpl(ProtocolVersion protocolVersion, CipherSuite cipherSuite,
            Collection<SignatureAndHashAlgorithm> algorithms,
            SecureRandom generator, String host, int port) {
        this(protocolVersion, cipherSuite, algorithms,
             new SessionId(defaultRejoinable, generator), host, port);
    }
    SSLSessionImpl(ProtocolVersion protocolVersion, CipherSuite cipherSuite,
            Collection<SignatureAndHashAlgorithm> algorithms,
            SessionId id, String host, int port) {
        this.protocolVersion = protocolVersion;
        sessionId = id;
        peerCerts = null;
        compressionMethod = compression_null;
        this.cipherSuite = cipherSuite;
        masterSecret = null;
        this.host = host;
        this.port = port;
        sessionCount = ++counter;
        localSupportedSignAlgs =
            SignatureAndHashAlgorithm.getAlgorithmNames(algorithms);
        if (debug != null && Debug.isOn("session")) {
            System.out.println("%% Initialized:  " + this);
        }
    }
    void setMasterSecret(SecretKey secret) {
        if (masterSecret == null) {
            masterSecret = secret;
        } else {
            throw new RuntimeException("setMasterSecret() error");
        }
    }
    SecretKey getMasterSecret() {
        return masterSecret;
    }
    void setPeerCertificates(X509Certificate[] peer) {
        if (peerCerts == null) {
            peerCerts = peer;
        }
    }
    void setLocalCertificates(X509Certificate[] local) {
        localCerts = local;
    }
    void setLocalPrivateKey(PrivateKey privateKey) {
        localPrivateKey = privateKey;
    }
    void setPeerSupportedSignatureAlgorithms(
            Collection<SignatureAndHashAlgorithm> algorithms) {
        peerSupportedSignAlgs =
            SignatureAndHashAlgorithm.getAlgorithmNames(algorithms);
    }
    void setPeerPrincipal(Principal principal) {
        if (peerPrincipal == null) {
            peerPrincipal = principal;
        }
    }
    void setLocalPrincipal(Principal principal) {
        localPrincipal = principal;
    }
    boolean isRejoinable() {
        return sessionId != null && sessionId.length() != 0 &&
            !invalidated && isLocalAuthenticationValid();
    }
    public synchronized boolean isValid() {
        return isRejoinable();
    }
    boolean isLocalAuthenticationValid() {
        if (localPrivateKey != null) {
            try {
                localPrivateKey.getAlgorithm();
            } catch (Exception e) {
                invalidate();
                return false;
            }
        }
        return true;
    }
    public byte[] getId() {
        return sessionId.getId();
    }
    public SSLSessionContext getSessionContext() {
        SecurityManager sm;
        if ((sm = System.getSecurityManager()) != null) {
            sm.checkPermission(new SSLPermission("getSSLSessionContext"));
        }
        return context;
    }
    SessionId getSessionId() {
        return sessionId;
    }
    CipherSuite getSuite() {
        return cipherSuite;
    }
    void setSuite(CipherSuite suite) {
       cipherSuite = suite;
       if (debug != null && Debug.isOn("session")) {
           System.out.println("%% Negotiating:  " + this);
       }
    }
    public String getCipherSuite() {
        return getSuite().name;
    }
    ProtocolVersion getProtocolVersion() {
        return protocolVersion;
    }
    public String getProtocol() {
        return getProtocolVersion().name;
    }
    byte getCompression() {
        return compressionMethod;
    }
    public int hashCode() {
        return sessionId.hashCode();
    }
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof SSLSessionImpl) {
            SSLSessionImpl sess = (SSLSessionImpl) obj;
            return (sessionId != null) && (sessionId.equals(
                        sess.getSessionId()));
        }
        return false;
    }
    public java.security.cert.Certificate[] getPeerCertificates()
            throws SSLPeerUnverifiedException {
        if ((cipherSuite.keyExchange == K_KRB5) ||
            (cipherSuite.keyExchange == K_KRB5_EXPORT)) {
            throw new SSLPeerUnverifiedException("no certificates expected"
                        + " for Kerberos cipher suites");
        }
        if (peerCerts == null) {
            throw new SSLPeerUnverifiedException("peer not authenticated");
        }
        return (java.security.cert.Certificate[])peerCerts.clone();
    }
    public java.security.cert.Certificate[] getLocalCertificates() {
        return (localCerts == null ? null :
            (java.security.cert.Certificate[])localCerts.clone());
    }
    public javax.security.cert.X509Certificate[] getPeerCertificateChain()
            throws SSLPeerUnverifiedException {
        if ((cipherSuite.keyExchange == K_KRB5) ||
            (cipherSuite.keyExchange == K_KRB5_EXPORT)) {
            throw new SSLPeerUnverifiedException("no certificates expected"
                        + " for Kerberos cipher suites");
        }
        if (peerCerts == null) {
            throw new SSLPeerUnverifiedException("peer not authenticated");
        }
        javax.security.cert.X509Certificate[] certs;
        certs = new javax.security.cert.X509Certificate[peerCerts.length];
        for (int i = 0; i < peerCerts.length; i++) {
            byte[] der = null;
            try {
                der = peerCerts[i].getEncoded();
                certs[i] = javax.security.cert.X509Certificate.getInstance(der);
            } catch (CertificateEncodingException e) {
                throw new SSLPeerUnverifiedException(e.getMessage());
            } catch (javax.security.cert.CertificateException e) {
                throw new SSLPeerUnverifiedException(e.getMessage());
            }
        }
        return certs;
    }
    public X509Certificate[] getCertificateChain()
            throws SSLPeerUnverifiedException {
        if ((cipherSuite.keyExchange == K_KRB5) ||
            (cipherSuite.keyExchange == K_KRB5_EXPORT)) {
            throw new SSLPeerUnverifiedException("no certificates expected"
                        + " for Kerberos cipher suites");
        }
        if (peerCerts != null) {
            return peerCerts.clone();
        } else {
            throw new SSLPeerUnverifiedException("peer not authenticated");
        }
    }
    public Principal getPeerPrincipal()
                throws SSLPeerUnverifiedException
    {
        if ((cipherSuite.keyExchange == K_KRB5) ||
            (cipherSuite.keyExchange == K_KRB5_EXPORT)) {
            if (peerPrincipal == null) {
                throw new SSLPeerUnverifiedException("peer not authenticated");
            } else {
                return peerPrincipal;
            }
        }
        if (peerCerts == null) {
            throw new SSLPeerUnverifiedException("peer not authenticated");
        }
        return peerCerts[0].getSubjectX500Principal();
    }
    public Principal getLocalPrincipal() {
        if ((cipherSuite.keyExchange == K_KRB5) ||
            (cipherSuite.keyExchange == K_KRB5_EXPORT)) {
                return (localPrincipal == null ? null : localPrincipal);
        }
        return (localCerts == null ? null :
                localCerts[0].getSubjectX500Principal());
    }
    public long getCreationTime() {
        return creationTime;
    }
    public long getLastAccessedTime() {
        return (lastUsedTime != 0) ? lastUsedTime : creationTime;
    }
    void setLastAccessedTime(long time) {
        lastUsedTime = time;
    }
    public InetAddress getPeerAddress() {
        try {
            return InetAddress.getByName(host);
        } catch (java.net.UnknownHostException e) {
            return null;
        }
    }
    public String getPeerHost() {
        return host;
    }
    public int getPeerPort() {
        return port;
    }
    void setContext(SSLSessionContextImpl ctx) {
        if (context == null) {
            context = ctx;
        }
    }
    synchronized public void invalidate() {
        if (this == nullSession) {
            return;
        }
        invalidated = true;
        if (debug != null && Debug.isOn("session")) {
            System.out.println("%% Invalidated:  " + this);
        }
        if (context != null) {
            context.remove(sessionId);
            context = null;
        }
    }
    private Hashtable<SecureKey, Object> table = new Hashtable<>();
    public void putValue(String key, Object value) {
        if ((key == null) || (value == null)) {
            throw new IllegalArgumentException("arguments can not be null");
        }
        SecureKey secureKey = new SecureKey(key);
        Object oldValue = table.put(secureKey, value);
        if (oldValue instanceof SSLSessionBindingListener) {
            SSLSessionBindingEvent e;
            e = new SSLSessionBindingEvent(this, key);
            ((SSLSessionBindingListener)oldValue).valueUnbound(e);
        }
        if (value instanceof SSLSessionBindingListener) {
            SSLSessionBindingEvent e;
            e = new SSLSessionBindingEvent(this, key);
            ((SSLSessionBindingListener)value).valueBound(e);
        }
    }
    public Object getValue(String key) {
        if (key == null) {
            throw new IllegalArgumentException("argument can not be null");
        }
        SecureKey secureKey = new SecureKey(key);
        return table.get(secureKey);
    }
    public void removeValue(String key) {
        if (key == null) {
            throw new IllegalArgumentException("argument can not be null");
        }
        SecureKey secureKey = new SecureKey(key);
        Object value = table.remove(secureKey);
        if (value instanceof SSLSessionBindingListener) {
            SSLSessionBindingEvent e;
            e = new SSLSessionBindingEvent(this, key);
            ((SSLSessionBindingListener)value).valueUnbound(e);
        }
    }
    public String[] getValueNames() {
        Enumeration<SecureKey> e;
        Vector<Object> v = new Vector<>();
        SecureKey key;
        Object securityCtx = SecureKey.getCurrentSecurityContext();
        for (e = table.keys(); e.hasMoreElements(); ) {
            key = e.nextElement();
            if (securityCtx.equals(key.getSecurityContext())) {
                v.addElement(key.getAppKey());
            }
        }
        String[] names = new String[v.size()];
        v.copyInto(names);
        return names;
    }
    private boolean acceptLargeFragments =
        Debug.getBooleanProperty("jsse.SSLEngine.acceptLargeFragments", false);
    protected synchronized void expandBufferSizes() {
        acceptLargeFragments = true;
    }
    public synchronized int getPacketBufferSize() {
        return acceptLargeFragments ?
                Record.maxLargeRecordSize : Record.maxRecordSize;
    }
    public synchronized int getApplicationBufferSize() {
        return getPacketBufferSize() - Record.headerSize;
    }
    public String[] getLocalSupportedSignatureAlgorithms() {
        if (localSupportedSignAlgs != null) {
            return localSupportedSignAlgs.clone();
        }
        return new String[0];
    }
    public String[] getPeerSupportedSignatureAlgorithms() {
        if (peerSupportedSignAlgs != null) {
            return peerSupportedSignAlgs.clone();
        }
        return new String[0];
    }
    public String toString() {
        return "[Session-" + sessionCount
            + ", " + getCipherSuite()
            + "]";
    }
    public void finalize() {
        String[] names = getValueNames();
        for (int i = 0; i < names.length; i++) {
            removeValue(names[i]);
        }
    }
}
class SecureKey {
    private static Object       nullObject = new Object();
    private Object        appKey;
    private Object      securityCtx;
    static Object getCurrentSecurityContext() {
        SecurityManager sm = System.getSecurityManager();
        Object context = null;
        if (sm != null)
            context = sm.getSecurityContext();
        if (context == null)
            context = nullObject;
        return context;
    }
    SecureKey(Object key) {
        this.appKey = key;
        this.securityCtx = getCurrentSecurityContext();
    }
    Object getAppKey() {
        return appKey;
    }
    Object getSecurityContext() {
        return securityCtx;
    }
    public int hashCode() {
        return appKey.hashCode() ^ securityCtx.hashCode();
    }
    public boolean equals(Object o) {
        return o instanceof SecureKey && ((SecureKey)o).appKey.equals(appKey)
                        && ((SecureKey)o).securityCtx.equals(securityCtx);
    }
}
