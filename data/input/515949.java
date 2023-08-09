public class OpenSSLSessionImpl implements SSLSession {
    long lastAccessedTime = 0;
    X509Certificate[] localCertificates;
    X509Certificate[] peerCertificates;
    private boolean isValid = true;
    private TwoKeyHashMap values = new TwoKeyHashMap();
    private javax.security.cert.X509Certificate[] peerCertificateChain;
    protected int session;
    private SSLParameters sslParameters;
    private String peerHost;
    private int peerPort;
    private final SSLSessionContext sessionContext;
    protected OpenSSLSessionImpl(int session, SSLParameters sslParameters,
            String peerHost, int peerPort, SSLSessionContext sessionContext) {
        this.session = session;
        this.sslParameters = sslParameters;
        this.peerHost = peerHost;
        this.peerPort = peerPort;
        this.sessionContext = sessionContext;
    }
    OpenSSLSessionImpl(byte[] derData, SSLParameters sslParameters,
            String peerHost, int peerPort,
            javax.security.cert.X509Certificate[] peerCertificateChain,
            SSLSessionContext sessionContext)
            throws IOException {
        this.sslParameters = sslParameters;
        this.peerHost = peerHost;
        this.peerPort = peerPort;
        this.peerCertificateChain = peerCertificateChain;
        this.sessionContext = sessionContext;
        initializeNative(derData);
    }
    public native byte[] getId();
    native byte[] getEncoded();
    private void initializeNative(byte[] derData) throws IOException {
        this.session = initializeNativeImpl(derData, derData.length);
        if (this.session == 0) {
            throw new IOException("Invalid session data");
        }
    }
    private native int initializeNativeImpl(byte[] data, int size);
    public native long getCreationTime();
    public long getLastAccessedTime() {
        return (lastAccessedTime == 0) ? getCreationTime() : lastAccessedTime;
    }
    public int getApplicationBufferSize() {
        return SSLRecordProtocol.MAX_DATA_LENGTH;
    }
    public int getPacketBufferSize() {
        return SSLRecordProtocol.MAX_SSL_PACKET_SIZE;
    }
    public Principal getLocalPrincipal() {
        if (localCertificates != null && localCertificates.length > 0) {
            return localCertificates[0].getSubjectX500Principal();
        } else {
            return null;
        }
    }
    public Certificate[] getLocalCertificates() {
        X509Certificate[] localCertificates = null;
        String alias = sslParameters.getKeyManager().chooseClientAlias(new String[] { "RSA" }, null, null);
        if (alias != null) {
            localCertificates = sslParameters.getKeyManager().getCertificateChain(alias);
        }
        return localCertificates;
    }
    private native byte[][] getPeerCertificatesImpl();
    public javax.security.cert.X509Certificate[] getPeerCertificateChain() throws SSLPeerUnverifiedException {
        if (peerCertificateChain == null) {
            try {
                byte[][] bytes = getPeerCertificatesImpl();
                if (bytes == null) throw new SSLPeerUnverifiedException("No certificate available");
                peerCertificateChain = new javax.security.cert.X509Certificate[bytes.length];
                for(int i = 0; i < bytes.length; i++) {
                    peerCertificateChain[i] = javax.security.cert.X509Certificate.getInstance(bytes[i]);
                }
                return peerCertificateChain;
            } catch (javax.security.cert.CertificateException e) {
                throw new SSLPeerUnverifiedException(e.getMessage());
            }
        } else {
            return peerCertificateChain;
        }
    }
    public Certificate[] getPeerCertificates() throws SSLPeerUnverifiedException {
        if (peerCertificates == null) {
            if (peerCertificateChain == null) getPeerCertificateChain();
            try {
                if (peerCertificateChain.length == 0) return new X509Certificate[]{};
                peerCertificates = new X509CertImpl[peerCertificateChain.length];
                for(int i = 0; i < peerCertificates.length; i++) {
                    peerCertificates[i] = new X509CertImpl(peerCertificateChain[i].getEncoded());
                }
                return peerCertificates;
            } catch (SSLPeerUnverifiedException e) {
                return new X509Certificate[]{};
            } catch (IOException e) {
                return new X509Certificate[]{};
            } catch (CertificateEncodingException e) {
                return new X509Certificate[]{};
            }
        } else {
            return peerCertificates;
        }
    }
    public Principal getPeerPrincipal() throws SSLPeerUnverifiedException {
        if (peerCertificates == null) {
            throw new SSLPeerUnverifiedException("No peer certificate");
        }
        return peerCertificates[0].getSubjectX500Principal();
    }
    public String getPeerHost() {
        return peerHost;
    }
    public int getPeerPort() {
        return peerPort;
    }
    public native String getCipherSuite();
    public native String getProtocol();
    public SSLSessionContext getSessionContext() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new SSLPermission("getSSLSessionContext"));
        }
        return sessionContext;
    }
    public boolean isValid() {
        SSLSessionContext context = sessionContext;
        if (isValid
                && context != null
                && context.getSessionTimeout() != 0
                && lastAccessedTime + context.getSessionTimeout() > System.currentTimeMillis()) {
            isValid = false;
        }
        return isValid;
    }
    public void invalidate() {
        isValid = false;
    }
    public Object getValue(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Parameter is null");
        }
        return values.get(name, AccessController.getContext());
    }
    public String[] getValueNames() {
        Vector v = new Vector();
        AccessControlContext current = AccessController.getContext();
        AccessControlContext cont;
        for (Iterator it = values.entrySet().iterator(); it.hasNext();) {
            TwoKeyHashMap.Entry entry = (TwoKeyHashMap.Entry) it.next();
            cont = (AccessControlContext) entry.getKey2();
            if ((current == null && cont == null)
                    || (current != null && current.equals(cont))) {
                v.add(entry.getKey1());
            }
        }
        return (String[]) v.toArray(new String[0]);
    }
    public void putValue(String name, Object value) {
        if (name == null || value == null) {
            throw new IllegalArgumentException("Parameter is null");
        }
        Object old = values.put(name, AccessController.getContext(), value);
        if (value instanceof SSLSessionBindingListener) {
            ((SSLSessionBindingListener) value)
                    .valueBound(new SSLSessionBindingEvent(this, name));
        }
        if (old instanceof SSLSessionBindingListener) {
            ((SSLSessionBindingListener) old)
                    .valueUnbound(new SSLSessionBindingEvent(this, name));
        }
    }
    public void removeValue(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Parameter is null");
        }
        Object old = values.remove(name, AccessController.getContext());
        if (old instanceof SSLSessionBindingListener) {
            SSLSessionBindingListener listener = (SSLSessionBindingListener) old;
            listener.valueUnbound(new SSLSessionBindingEvent(this, name));
        }
    }
    protected void finalize() {
        synchronized (OpenSSLSocketImpl.class) {
            freeImpl(session);
        }
    }
    private native void freeImpl(int session);
}
