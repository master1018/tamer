public class OpenSSLSocketImpl extends javax.net.ssl.SSLSocket {
    private int ssl_ctx;
    private int ssl;
    private InputStream is;
    private OutputStream os;
    private final Object handshakeLock = new Object();
    private Object readLock = new Object();
    private Object writeLock = new Object();
    private SSLParameters sslParameters;
    private OpenSSLSessionImpl sslSession;
    private Socket socket;
    private boolean autoClose;
    private boolean handshakeStarted = false;
    private ArrayList<HandshakeCompletedListener> listeners;
    private long ssl_op_no = 0x00000000L;
    private int timeout = 0;
    private int handshakeTimeout = -1;  
    private InetSocketAddress address;
    private static final String[] supportedProtocols = new String[] {
        "SSLv3",
        "TLSv1"
        };
    private static final AtomicInteger instanceCount = new AtomicInteger(0);
    public static int getInstanceCount() {
        return instanceCount.get();
    }
    private static void updateInstanceCount(int amount) {
        instanceCount.addAndGet(amount);
    }
    private native static void nativeinitstatic();
    static {
        nativeinitstatic();
    }
    private native void nativeinit(String privatekey, String certificate, byte[] seed);
    private void init() throws IOException {
        String alias = sslParameters.getKeyManager().chooseClientAlias(new String[] { "RSA" }, null, null);
        if (alias != null) {
            PrivateKey privateKey = sslParameters.getKeyManager().getPrivateKey(alias);
            X509Certificate[] certificates = sslParameters.getKeyManager().getCertificateChain(alias);
            ByteArrayOutputStream privateKeyOS = new ByteArrayOutputStream();
            PEMWriter privateKeyPEMWriter = new PEMWriter(new OutputStreamWriter(privateKeyOS));
            privateKeyPEMWriter.writeObject(privateKey);
            privateKeyPEMWriter.close();
            ByteArrayOutputStream certificateOS = new ByteArrayOutputStream();
            PEMWriter certificateWriter = new PEMWriter(new OutputStreamWriter(certificateOS));
            for (int i = 0; i < certificates.length; i++) {
                certificateWriter.writeObject(certificates[i]);
            }
            certificateWriter.close();
            nativeinit(privateKeyOS.toString(), certificateOS.toString(),
                    sslParameters.getSecureRandomMember() != null ?
                    sslParameters.getSecureRandomMember().generateSeed(1024) : null);
        } else {
            nativeinit(null, null,
                    sslParameters.getSecureRandomMember() != null ?
                    sslParameters.getSecureRandomMember().generateSeed(1024) : null);
        }
    }
    protected OpenSSLSocketImpl(SSLParameters sslParameters, long ssl_op_no) throws IOException {
        super();
        this.sslParameters = sslParameters;
        this.ssl_op_no = ssl_op_no;
        updateInstanceCount(1);
    }
    protected OpenSSLSocketImpl(SSLParameters sslParameters) throws IOException {
        super();
        this.sslParameters = sslParameters;
        init();
        updateInstanceCount(1);
    }
    protected OpenSSLSocketImpl(String host, int port,
            SSLParameters sslParameters)
        throws IOException {
        super(host, port);
        this.sslParameters = sslParameters;
        init();
        updateInstanceCount(1);
    }
    protected OpenSSLSocketImpl(InetAddress address, int port,
            SSLParameters sslParameters)
        throws IOException {
        super(address, port);
        this.sslParameters = sslParameters;
        init();
        updateInstanceCount(1);
    }
    protected OpenSSLSocketImpl(String host, int port, InetAddress clientAddress,
            int clientPort, SSLParameters sslParameters)
        throws IOException {
        super(host, port, clientAddress, clientPort);
        this.sslParameters = sslParameters;
        init();
        updateInstanceCount(1);
    }
    protected OpenSSLSocketImpl(InetAddress address, int port,
            InetAddress clientAddress, int clientPort, SSLParameters sslParameters)
        throws IOException {
        super(address, port, clientAddress, clientPort);
        this.sslParameters = sslParameters;
        init();
        updateInstanceCount(1);
    }
    protected OpenSSLSocketImpl(Socket socket, String host, int port,
            boolean autoClose, SSLParameters sslParameters) throws IOException {
        super();
        this.socket = socket;
        this.timeout = socket.getSoTimeout();
        this.address = new InetSocketAddress(host, port);
        this.autoClose = autoClose;
        this.sslParameters = sslParameters;
        init();
        updateInstanceCount(1);
    }
    private native boolean nativeconnect(int ctx, Socket sock, boolean client_mode, int sslsession) throws IOException;
    private native int nativegetsslsession(int ssl);
    private native String nativecipherauthenticationmethod();
    private OpenSSLSessionImpl getCachedClientSession() {
        if (super.getInetAddress() == null ||
                super.getInetAddress().getHostAddress() == null ||
                super.getInetAddress().getHostName() == null) {
            return null;
        }
        ClientSessionContext sessionContext
                = sslParameters.getClientSessionContext();
        return (OpenSSLSessionImpl) sessionContext.getSession(
                super.getInetAddress().getHostName(),
                super.getPort());
    }
    static class LoggerHolder {
        static final Logger logger = Logger.getLogger(
                OpenSSLSocketImpl.class.getName());
    }
    public synchronized void startHandshake() throws IOException {
        synchronized (handshakeLock) {
            if (!handshakeStarted) {
                handshakeStarted = true;
            } else {
                return;
            }
        }
        OpenSSLSessionImpl session = getCachedClientSession();
        if (session == null && !sslParameters.getEnableSessionCreation()) {
            throw new SSLHandshakeException("SSL Session may not be created");
        } else {
            int savedTimeout = timeout;
            if (handshakeTimeout >= 0) {
                setSoTimeout(handshakeTimeout);
            }
            Socket socket = this.socket != null ? this.socket : this;
            int sessionId = session != null ? session.session : 0;
            boolean reusedSession;
            synchronized (OpenSSLSocketImpl.class) {
                reusedSession = nativeconnect(ssl_ctx, socket,
                        sslParameters.getUseClientMode(), sessionId);
            }
            if (reusedSession) {
                session.lastAccessedTime = System.currentTimeMillis();
                sslSession = session;
                LoggerHolder.logger.fine("Reused cached session for "
                        + getInetAddress().getHostName() + ".");
            } else {
                if (session != null) {
                    LoggerHolder.logger.fine("Reuse of cached session for "
                            + getInetAddress().getHostName() + " failed.");
                } else {
                    LoggerHolder.logger.fine("Created new session for "
                            + getInetAddress().getHostName() + ".");
                }
                ClientSessionContext sessionContext
                        = sslParameters.getClientSessionContext();
                synchronized (OpenSSLSocketImpl.class) {
                    sessionId = nativegetsslsession(ssl);
                }
                if (address == null) {
                    sslSession = new OpenSSLSessionImpl(
                            sessionId, sslParameters,
                            super.getInetAddress().getHostName(),
                            super.getPort(), sessionContext);
                } else  {
                    sslSession = new OpenSSLSessionImpl(
                            sessionId, sslParameters,
                            address.getHostName(), address.getPort(),
                            sessionContext);
                }
                try {
                    X509Certificate[] peerCertificates = (X509Certificate[])
                            sslSession.getPeerCertificates();
                    if (peerCertificates == null
                            || peerCertificates.length == 0) {
                        throw new SSLException("Server sends no certificate");
                    }
                    String authMethod;
                    synchronized (OpenSSLSocketImpl.class) {
                        authMethod = nativecipherauthenticationmethod();
                    }
                    sslParameters.getTrustManager().checkServerTrusted(
                            peerCertificates,
                            authMethod);
                    sessionContext.putSession(sslSession);
                } catch (CertificateException e) {
                    throw new SSLException("Not trusted server certificate", e);
                }
            }
            if (handshakeTimeout >= 0) {
                setSoTimeout(savedTimeout);
            }
        }
        if (listeners != null) {
            HandshakeCompletedEvent event =
                new HandshakeCompletedEvent(this, sslSession);
            int size = listeners.size();
            for (int i = 0; i < size; i++) {
                listeners.get(i).handshakeCompleted(event);
            }
        }
    }
    native synchronized void nativeaccept(Socket socketObject, int m_ctx, boolean client_mode);
    protected void accept(int m_ctx, boolean client_mode) throws IOException {
        handshakeStarted = true;
        nativeaccept(this, m_ctx, client_mode);
        ServerSessionContext sessionContext
                = sslParameters.getServerSessionContext();
        sslSession = new OpenSSLSessionImpl(nativegetsslsession(ssl),
                sslParameters, super.getInetAddress().getHostName(),
                super.getPort(), sessionContext);
        sslSession.lastAccessedTime = System.currentTimeMillis();
        sessionContext.putSession(sslSession);
    }
    @SuppressWarnings("unused")
    private int verify_callback(byte[][] bytes) {
        try {
            X509Certificate[] peerCertificateChain
                    = new X509Certificate[bytes.length];
            for(int i = 0; i < bytes.length; i++) {
                peerCertificateChain[i] =
                    new X509CertImpl(javax.security.cert.X509Certificate.getInstance(bytes[i]).getEncoded());
            }
            try {
                sslParameters.getTrustManager().checkClientTrusted(peerCertificateChain, "null");
            } catch (CertificateException e) {
                throw new AlertException(AlertProtocol.BAD_CERTIFICATE,
                        new SSLException("Not trusted server certificate", e));
            }
        } catch (javax.security.cert.CertificateException e) {
            return 0;
        } catch (IOException e) {
            return 0;
        }
        return 1;
    }
    public InputStream getInputStream() throws IOException {
        synchronized(this) {
            if (is == null) {
                is = new SSLInputStream();
            }
            return is;
        }
    }
    public OutputStream getOutputStream() throws IOException {
        synchronized(this) {
            if (os == null) {
                os = new SSLOutputStream();
            }
            return os;
        }
    }
    public void shutdownInput() throws IOException {
        throw new UnsupportedOperationException(
        "Method shutdownInput() is not supported.");
    }
    public void shutdownOutput() throws IOException {
        throw new UnsupportedOperationException(
        "Method shutdownOutput() is not supported.");
    }
    private native int nativeread(int timeout) throws IOException;
    private native int nativeread(byte[] b, int off, int len, int timeout) throws IOException;
    private class SSLInputStream extends InputStream {
        SSLInputStream() throws IOException {
            OpenSSLSocketImpl.this.startHandshake();
        }
        public int read() throws IOException {
            synchronized(readLock) {
                return OpenSSLSocketImpl.this.nativeread(timeout);
            }
        }
        public int read(byte[] b, int off, int len) throws IOException {
            synchronized(readLock) {
                return OpenSSLSocketImpl.this.nativeread(b, off, len, timeout);
            }
        }
    }
    private native void nativewrite(int b) throws IOException;
    private native void nativewrite(byte[] b, int off, int len) throws IOException;
    private class SSLOutputStream extends OutputStream {
        SSLOutputStream() throws IOException {
            OpenSSLSocketImpl.this.startHandshake();
        }
        public void write(int b) throws IOException {
            synchronized(writeLock) {
                OpenSSLSocketImpl.this.nativewrite(b);
            }
        }
        public void write(byte[] b, int start, int len) throws IOException {
            synchronized(writeLock) {
                OpenSSLSocketImpl.this.nativewrite(b, start, len);
            }
        }
    }
    public SSLSession getSession() {
        try {
            startHandshake();
        } catch (IOException e) {
            Logger.getLogger(getClass().getName()).log(Level.WARNING,
                    "Error negotiating SSL connection.", e);
            return SSLSessionImpl.NULL_SESSION;
        }
        return sslSession;
    }
    public void addHandshakeCompletedListener(
            HandshakeCompletedListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Provided listener is null");
        }
        if (listeners == null) {
            listeners = new ArrayList();
        }
        listeners.add(listener);
    }
    public void removeHandshakeCompletedListener(
            HandshakeCompletedListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Provided listener is null");
        }
        if (listeners == null) {
            throw new IllegalArgumentException(
                    "Provided listener is not registered");
        }
        if (!listeners.remove(listener)) {
            throw new IllegalArgumentException(
                    "Provided listener is not registered");
        }
    }
    public boolean getEnableSessionCreation() {
        return sslParameters.getEnableSessionCreation();
    }
    public void setEnableSessionCreation(boolean flag) {
        sslParameters.setEnableSessionCreation(flag);
    }
    static native String[] nativegetsupportedciphersuites();
    public String[] getSupportedCipherSuites() {
        return nativegetsupportedciphersuites();
    }
    static native String[] nativeGetEnabledCipherSuites(int ssl_ctx);
    public String[] getEnabledCipherSuites() {
        return nativeGetEnabledCipherSuites(ssl_ctx);
    }
    static native void nativeSetEnabledCipherSuites(int ssl_ctx, String controlString);
    private static boolean findSuite(String suite) {
        String[] supportedCipherSuites = nativegetsupportedciphersuites();
        for(int i = 0; i < supportedCipherSuites.length; i++) {
            if (supportedCipherSuites[i].equals(suite)) {
                return true;
            }
        }
        throw new IllegalArgumentException("Protocol " + suite + " is not supported.");
    }
    public void setEnabledCipherSuites(String[] suites) {
        setEnabledCipherSuites(ssl_ctx, suites);
    }
    static void setEnabledCipherSuites(int ssl_ctx, String[] suites) {
        if (suites == null) {
            throw new IllegalArgumentException("Provided parameter is null");
        }
        String controlString = "";
        for (int i = 0; i < suites.length; i++) {
            findSuite(suites[i]);
            if (i == 0) controlString = suites[i];
            else controlString += ":" + suites[i];
        }
        nativeSetEnabledCipherSuites(ssl_ctx, controlString);
    }
    public String[] getSupportedProtocols() {
        return supportedProtocols.clone();
    }
    static private long SSL_OP_NO_SSLv3 = 0x02000000L;
    static private long SSL_OP_NO_TLSv1 = 0x04000000L;
    @Override
    public String[] getEnabledProtocols() {
        ArrayList<String> array = new ArrayList<String>();
        if ((ssl_op_no & SSL_OP_NO_SSLv3) == 0x00000000L) {
            array.add(supportedProtocols[1]);
        }
        if ((ssl_op_no & SSL_OP_NO_TLSv1) == 0x00000000L) {
            array.add(supportedProtocols[2]);
        }
        return array.toArray(new String[array.size()]);
    }
    private native void nativesetenabledprotocols(long l);
    @Override
    public synchronized void setEnabledProtocols(String[] protocols) {
        if (protocols == null) {
            throw new IllegalArgumentException("Provided parameter is null");
        }
        ssl_op_no  = SSL_OP_NO_SSLv3 | SSL_OP_NO_TLSv1;
        for(int i = 0; i < protocols.length; i++) {
            if (protocols[i].equals("SSLv3"))
                ssl_op_no ^= SSL_OP_NO_SSLv3;
            else if (protocols[i].equals("TLSv1"))
                ssl_op_no ^= SSL_OP_NO_TLSv1;
            else throw new IllegalArgumentException("Protocol " + protocols[i] +
            " is not supported.");
        }
        nativesetenabledprotocols(ssl_op_no);
    }
    public boolean getUseClientMode() {
        return sslParameters.getUseClientMode();
    }
    public synchronized void setUseClientMode(boolean mode) {
        if (handshakeStarted) {
            throw new IllegalArgumentException(
            "Could not change the mode after the initial handshake has begun.");
        }
        sslParameters.setUseClientMode(mode);
    }
    public boolean getWantClientAuth() {
        return sslParameters.getWantClientAuth();
    }
    public boolean getNeedClientAuth() {
        return sslParameters.getNeedClientAuth();
    }
    public void setNeedClientAuth(boolean need) {
        sslParameters.setNeedClientAuth(need);
    }
    public void setWantClientAuth(boolean want) {
        sslParameters.setWantClientAuth(want);
    }
    public void sendUrgentData(int data) throws IOException {
        throw new SocketException(
                "Method sendUrgentData() is not supported.");
    }
    public void setOOBInline(boolean on) throws SocketException {
        throw new SocketException(
                "Methods sendUrgentData, setOOBInline are not supported.");
    }
    public synchronized void setSoTimeout(int timeout) throws SocketException {
        super.setSoTimeout(timeout);
        this.timeout = timeout;
    }
    public synchronized void setHandshakeTimeout(int timeout) throws SocketException {
        this.handshakeTimeout = timeout;
    }
    private native void nativeinterrupt() throws IOException;
    private native void nativeclose() throws IOException;
    public void close() throws IOException {
        synchronized (handshakeLock) {
            if (!handshakeStarted) {
                handshakeStarted = true;
                synchronized (this) {
                    nativefree();
                    if (socket != null) {
                        if (autoClose && !socket.isClosed()) socket.close();
                    } else {
                        if (!super.isClosed()) super.close();
                    }
                }
                return;
            }
        }
        nativeinterrupt();
        synchronized (this) {
            synchronized (writeLock) {
                synchronized (readLock) {
                    IOException pendingException = null;
                    try {
                        if (handshakeStarted) {
                            nativeclose();
                        }
                    } catch (IOException ex) {
                        pendingException = ex;
                    }
                    nativefree();
                    if (socket != null) {
                        if (autoClose && !socket.isClosed())
                            socket.close();
                    } else {
                        if (!super.isClosed())
                            super.close();
                    }
                    if (pendingException != null) {
                        throw pendingException;
                    }
                }
            }
        }
    }
    private native void nativefree();
    protected void finalize() throws IOException {
        updateInstanceCount(-1);
        if (ssl == 0) {
            return;
        }
        nativefree();
    }
    public static boolean verifySignature(byte[] message, byte[] signature, String algorithm, RSAPublicKey key) {
        byte[] modulus = key.getModulus().toByteArray();
        byte[] exponent = key.getPublicExponent().toByteArray();
        return nativeverifysignature(message, signature, algorithm, modulus, exponent) == 1;
    }
    private static native int nativeverifysignature(byte[] message, byte[] signature,
            String algorithm, byte[] modulus, byte[] exponent);
}
