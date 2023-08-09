public class SSLSessionImpl implements SSLSession, Cloneable  {
    public static final SSLSessionImpl NULL_SESSION = new SSLSessionImpl(null);
    private static final class ValueKey {
        final String name;
        final AccessControlContext acc;
        ValueKey(String name) {
            super();
            this.name = name;
            this.acc = AccessController.getContext();
        }
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((acc == null) ? 0 : acc.hashCode());
            result = prime * result + ((name == null) ? 0 : name.hashCode());
            return result;
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (!(obj instanceof ValueKey))
                return false;
            ValueKey other = (ValueKey) obj;
            if (acc == null) {
                if (other.acc != null)
                    return false;
            } else if (!acc.equals(other.acc))
                return false;
            if (name == null) {
                if (other.name != null)
                    return false;
            } else if (!name.equals(other.name))
                return false;
            return true;
        }
    }
    private long creationTime;
    private boolean isValid = true;
    private Map<ValueKey, Object> values = new HashMap<ValueKey, Object>();
    byte[] id;
    long lastAccessedTime;
    ProtocolVersion protocol;
    CipherSuite cipherSuite;
    SSLSessionContext context;
    X509Certificate[] localCertificates;
    X509Certificate[] peerCertificates;
    private String peerHost;
    private int peerPort = -1;
    byte[] master_secret;
    byte[] clientRandom;
    byte[] serverRandom;
    final boolean isServer;
    public SSLSessionImpl(CipherSuite cipher_suite, SecureRandom sr) {
        creationTime = System.currentTimeMillis();
        lastAccessedTime = creationTime;
        if (cipher_suite == null) {
            this.cipherSuite = CipherSuite.TLS_NULL_WITH_NULL_NULL;
            id = new byte[0];
            isServer = false;
        } else {
            this.cipherSuite = cipher_suite;
            id = new byte[32];
            sr.nextBytes(id);
            long time = creationTime / 1000;
            id[28] = (byte) ((time & 0xFF000000) >>> 24);
            id[29] = (byte) ((time & 0x00FF0000) >>> 16);
            id[30] = (byte) ((time & 0x0000FF00) >>> 8);
            id[31] = (byte) ((time & 0x000000FF));
            isServer = true;
        }
    }
    public SSLSessionImpl(SecureRandom sr) {
        this(null, sr);
    }
    public int getApplicationBufferSize() {
        return SSLRecordProtocol.MAX_DATA_LENGTH;
    }
    public String getCipherSuite() {
        return cipherSuite.getName();
    }
    public long getCreationTime() {
        return creationTime;
    }
    public byte[] getId() {
        return id;
    }
    public long getLastAccessedTime() {
        return lastAccessedTime;
    }
    public Certificate[] getLocalCertificates() {
        return localCertificates;
    }
    public Principal getLocalPrincipal() {
        if (localCertificates != null && localCertificates.length > 0) {
            return localCertificates[0].getSubjectX500Principal();
        }
        return null;
    }
    public int getPacketBufferSize() {
        return SSLRecordProtocol.MAX_SSL_PACKET_SIZE;
    }
    public javax.security.cert.X509Certificate[] getPeerCertificateChain()
            throws SSLPeerUnverifiedException {
        if (peerCertificates == null) {
            throw new SSLPeerUnverifiedException("No peer certificate");
        }
        javax.security.cert.X509Certificate[] certs = new javax.security.cert.X509Certificate[peerCertificates.length];
        for (int i = 0; i < certs.length; i++) {
            try {
                certs[i] = javax.security.cert.X509Certificate.getInstance(peerCertificates[i]
                        .getEncoded());
            } catch (javax.security.cert.CertificateException e) {
            } catch (CertificateEncodingException e) {
            }
        }
        return certs;
    }
    public Certificate[] getPeerCertificates() throws SSLPeerUnverifiedException {
        if (peerCertificates == null) {
            throw new SSLPeerUnverifiedException("No peer certificate");
        }
        return peerCertificates;
    }
    public String getPeerHost() {
        return peerHost;
    }
    public int getPeerPort() {
        return peerPort;
    }
    public Principal getPeerPrincipal() throws SSLPeerUnverifiedException {
        if (peerCertificates == null) {
            throw new SSLPeerUnverifiedException("No peer certificate");
        }
        return peerCertificates[0].getSubjectX500Principal();
    }
    public String getProtocol() {
        return protocol.name;
    }
    public SSLSessionContext getSessionContext() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new SSLPermission("getSSLSessionContext"));
        }
        return context;
    }
    public Object getValue(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Parameter is null");
        }
        return values.get(new ValueKey(name));
    }
    public String[] getValueNames() {
        final Vector<String> v = new Vector<String>();
        final AccessControlContext currAcc = AccessController.getContext();
        for (ValueKey key : values.keySet()) {
            if ((currAcc == null && key.acc == null)
                    || (currAcc != null && currAcc.equals(key.acc))) {
                v.add(key.name);
            }
        }
        return v.toArray(new String[v.size()]);
    }
    public void invalidate() {
        isValid = false;
    }
    public boolean isValid() {
        if (isValid && context != null && context.getSessionTimeout() != 0
                && lastAccessedTime + context.getSessionTimeout() > System.currentTimeMillis()) {
            isValid = false;
        }
        return isValid;
    }
    public void putValue(String name, Object value) {
        if (name == null || value == null) {
            throw new IllegalArgumentException("Parameter is null");
        }
        Object old = values.put(new ValueKey(name), value);
        if (value instanceof SSLSessionBindingListener) {
            ((SSLSessionBindingListener) value).valueBound(new SSLSessionBindingEvent(this, name));
        }
        if (old instanceof SSLSessionBindingListener) {
            ((SSLSessionBindingListener) old).valueUnbound(new SSLSessionBindingEvent(this, name));
        }
    }
    public void removeValue(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Parameter is null");
        }
        Object old = values.remove(new ValueKey(name));
        if (old instanceof SSLSessionBindingListener) {
            SSLSessionBindingListener listener = (SSLSessionBindingListener) old;
            listener.valueUnbound(new SSLSessionBindingEvent(this, name));
        }
    }
    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }
    void setPeer(String peerHost, int peerPort) {
        this.peerHost = peerHost;
        this.peerPort = peerPort;
    }
}
