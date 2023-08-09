public class OpenSSLServerSocketImpl extends javax.net.ssl.SSLServerSocket {
    private int ssl_ctx;
    private boolean client_mode = true;
    private long ssl_op_no = 0x00000000L;
    private SSLParameters sslParameters;
    private static final String[] supportedProtocols = new String[] {
        "SSLv3",
        "TLSv1"
        };
    private native static void nativeinitstatic();
    static {
        nativeinitstatic();
    }
    private native void nativeinit(String privatekey, String certificate, byte[] seed);
    private void init() throws IOException {
        String alias = sslParameters.getKeyManager().chooseServerAlias("RSA", null, null);
        if (alias == null) {
            throw new IOException("No suitable certificates found");
        }
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
    }
    protected OpenSSLServerSocketImpl(SSLParameters sslParameters)
        throws IOException {
        super();
        this.sslParameters = sslParameters;
        init();
    }
    protected OpenSSLServerSocketImpl(int port, SSLParameters sslParameters)
        throws IOException {
        super(port);
        this.sslParameters = sslParameters;
        init();
    }
    protected OpenSSLServerSocketImpl(int port, int backlog, SSLParameters sslParameters)
        throws IOException {
        super(port, backlog);
        this.sslParameters = sslParameters;
        init();
    }
    protected OpenSSLServerSocketImpl(int port, int backlog, InetAddress iAddress, SSLParameters sslParameters)
        throws IOException {
        super(port, backlog, iAddress);
        this.sslParameters = sslParameters;
        init();
    }
    @Override
    public boolean getEnableSessionCreation() {
        return sslParameters.getEnableSessionCreation();
    }
    @Override
    public void setEnableSessionCreation(boolean flag) {
        sslParameters.setEnableSessionCreation(flag);
    }
    @Override
    public String[] getSupportedProtocols() {
        return supportedProtocols.clone();
    }
    static private long SSL_OP_NO_SSLv3 = 0x02000000L;
    static private long SSL_OP_NO_TLSv1 = 0x04000000L;
    @Override
    public String[] getEnabledProtocols() {
        ArrayList<String> array = new ArrayList<String>();
        if ((ssl_op_no & SSL_OP_NO_SSLv3) == 0x00000000L) {
            array.add(supportedProtocols[0]);
        }
        if ((ssl_op_no & SSL_OP_NO_TLSv1) == 0x00000000L) {
            array.add(supportedProtocols[1]);
        }
        return array.toArray(new String[array.size()]);
    }
    private native void nativesetenabledprotocols(long l);
    @Override
    public void setEnabledProtocols(String[] protocols) {
        if (protocols == null) {
            throw new IllegalArgumentException("Provided parameter is null");
        }
        ssl_op_no  = SSL_OP_NO_SSLv3 | SSL_OP_NO_TLSv1;
        for (int i = 0; i < protocols.length; i++) {
            if (protocols[i].equals("SSLv3"))
                ssl_op_no ^= SSL_OP_NO_SSLv3;
            else if (protocols[i].equals("TLSv1"))
                ssl_op_no ^= SSL_OP_NO_TLSv1;
            else throw new IllegalArgumentException("Protocol " + protocols[i] +
            " is not supported.");
        }
        nativesetenabledprotocols(ssl_op_no);
    }
    static native String[] nativegetsupportedciphersuites();
    @Override
    public String[] getSupportedCipherSuites() {
        return nativegetsupportedciphersuites();
    }
    @Override
    public String[] getEnabledCipherSuites() {
        return OpenSSLSocketImpl.nativeGetEnabledCipherSuites(ssl_ctx);
    }
    @Override
    public void setEnabledCipherSuites(String[] suites) {
        OpenSSLSocketImpl.setEnabledCipherSuites(ssl_ctx, suites);
    }
    static private int SSL_VERIFY_NONE =                 0x00;
    static private int SSL_VERIFY_PEER =                 0x01;
    static private int SSL_VERIFY_FAIL_IF_NO_PEER_CERT = 0x02;
    static private int SSL_VERIFY_CLIENT_ONCE =          0x04;
    private native void nativesetclientauth(int value);
    private void setClientAuth() {
        int value = SSL_VERIFY_NONE;
        if (sslParameters.getNeedClientAuth()) {
            value |= SSL_VERIFY_PEER|SSL_VERIFY_FAIL_IF_NO_PEER_CERT|SSL_VERIFY_CLIENT_ONCE;
        } else if (sslParameters.getWantClientAuth()) {
            value |= SSL_VERIFY_PEER|SSL_VERIFY_CLIENT_ONCE;
        }
        nativesetclientauth(value);
    }
    @Override
    public boolean getWantClientAuth() {
        return sslParameters.getWantClientAuth();
    }
    @Override
    public void setWantClientAuth(boolean want) {
        sslParameters.setWantClientAuth(want);
        setClientAuth();
    }
    @Override
    public boolean getNeedClientAuth() {
        return sslParameters.getNeedClientAuth();
    }
    @Override
    public void setNeedClientAuth(boolean need) {
        sslParameters.setNeedClientAuth(need);
        setClientAuth();
    }
    @Override
    public void setUseClientMode(boolean mode) {
        sslParameters.setUseClientMode(mode);
    }
    @Override
    public boolean getUseClientMode() {
        return sslParameters.getUseClientMode();
    }
    @Override
    public Socket accept() throws IOException {
        OpenSSLSocketImpl socket
                = new OpenSSLSocketImpl(sslParameters, ssl_op_no);
        implAccept(socket);
        socket.accept(ssl_ctx, client_mode);
        return socket;
    }
    private native void nativefree();
    @Override
    protected void finalize() throws Throwable {
        if (!isClosed()) close();
    }
    @Override
    public synchronized void close() throws IOException {
        nativefree();
        super.close();
    }
}
