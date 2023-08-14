abstract class AbstractSessionContext implements SSLSessionContext {
    volatile int maximumSize;
    volatile int timeout;
    final SSLParameters parameters;
    static final int OPEN_SSL = 1;
    AbstractSessionContext(SSLParameters parameters, int maximumSize,
            int timeout) {
        this.parameters = parameters;
        this.maximumSize = maximumSize;
        this.timeout = timeout;
    }
    abstract Iterator<SSLSession> sessionIterator();
    public final Enumeration getIds() {
        final Iterator<SSLSession> iterator = sessionIterator();
        return new Enumeration<byte[]>() {
            public boolean hasMoreElements() {
                return iterator.hasNext();
            }
            public byte[] nextElement() {
                return iterator.next().getId();
            }
        };
    }
    public final int getSessionCacheSize() {
        return maximumSize;
    }
    public final int getSessionTimeout() {
        return timeout;
    }
    abstract void trimToSize();
    public final void setSessionCacheSize(int size)
            throws IllegalArgumentException {
        if (size < 0) {
            throw new IllegalArgumentException("size < 0");
        }
        int oldMaximum = maximumSize;
        maximumSize = size;
        if (size < oldMaximum) {
            trimToSize();
        }
    }
    byte[] toBytes(SSLSession session) {
        if (!(session instanceof OpenSSLSessionImpl)) {
            return null;
        }
        OpenSSLSessionImpl sslSession = (OpenSSLSessionImpl) session;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream daos = new DataOutputStream(baos);
            daos.writeInt(OPEN_SSL); 
            byte[] data = sslSession.getEncoded();
            daos.writeInt(data.length);
            daos.write(data);
            X509Certificate[] certs = session.getPeerCertificateChain();
            daos.writeInt(certs.length);
            for (X509Certificate cert : certs) {
                data = cert.getEncoded();
                daos.writeInt(data.length);
                daos.write(data);
            }
            return baos.toByteArray();
        } catch (IOException e) {
            log(e);
            return null;
        } catch (CertificateEncodingException e) {
            log(e);
            return null;
        }
    }
    SSLSession toSession(byte[] data, String host, int port) {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        DataInputStream dais = new DataInputStream(bais);
        try {
            int type = dais.readInt();
            if (type != OPEN_SSL) {
                log(new AssertionError("Unexpected type ID: " + type));
                return null;
            }
            int length = dais.readInt();
            byte[] sessionData = new byte[length];
            dais.readFully(sessionData);
            int count = dais.readInt();
            X509Certificate[] certs = new X509Certificate[count];
            for (int i = 0; i < count; i++) {
                length = dais.readInt();
                byte[] certData = new byte[length];
                dais.readFully(certData);
                certs[i] = X509Certificate.getInstance(certData);
            }
            return new OpenSSLSessionImpl(sessionData, parameters, host, port,
                    certs, this);
        } catch (IOException e) {
            log(e);
            return null;
        } catch (CertificateException e) {
            log(e);
            return null;
        }
    }
    static void log(Throwable t) {
        java.util.logging.Logger.global.log(Level.WARNING,
                "Error converting session.", t);
    }
    static class ByteArray {
        private final byte[] bytes;
        ByteArray(byte[] bytes) {
            this.bytes = bytes;
        }
        @Override
        public int hashCode() {
            return Arrays.hashCode(bytes);
        }
        @Override
        @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
        public boolean equals(Object o) {
            ByteArray other = (ByteArray) o;
            return Arrays.equals(bytes, other.bytes);
        }
    }
}