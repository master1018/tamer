class JSSEClient extends CipherTest.Client {
    private final SSLContext sslContext;
    private final MyX509KeyManager keyManager;
    JSSEClient(CipherTest cipherTest) throws Exception {
        super(cipherTest);
        this.keyManager = new MyX509KeyManager(CipherTest.keyManager);
        sslContext = SSLContext.getInstance("TLS");
    }
    void runTest(CipherTest.TestParameters params) throws Exception {
        SSLSocket socket = null;
        try {
            keyManager.setAuthType(params.clientAuth);
            sslContext.init(new KeyManager[] {CipherTest.keyManager}, new TrustManager[] {cipherTest.trustManager}, cipherTest.secureRandom);
            SSLSocketFactory factory = (SSLSocketFactory)sslContext.getSocketFactory();
            socket = (SSLSocket)factory.createSocket("127.0.0.1", cipherTest.serverPort);
            socket.setSoTimeout(cipherTest.TIMEOUT);
            socket.setEnabledCipherSuites(new String[] {params.cipherSuite});
            socket.setEnabledProtocols(new String[] {params.protocol});
            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();
            sendRequest(in, out);
            socket.close();
            SSLSession session = socket.getSession();
            session.invalidate();
            String cipherSuite = session.getCipherSuite();
            if (params.cipherSuite.equals(cipherSuite) == false) {
                throw new Exception("Negotiated ciphersuite mismatch: " + cipherSuite + " != " + params.cipherSuite);
            }
            String protocol = session.getProtocol();
            if (params.protocol.equals(protocol) == false) {
                throw new Exception("Negotiated protocol mismatch: " + protocol + " != " + params.protocol);
            }
            if (cipherSuite.indexOf("DH_anon") == -1) {
                session.getPeerCertificates();
            }
            Certificate[] certificates = session.getLocalCertificates();
            if (params.clientAuth == null) {
                if (certificates != null) {
                    throw new Exception("Local certificates should be null");
                }
            } else {
                if ((certificates == null) || (certificates.length == 0)) {
                    throw new Exception("Certificates missing");
                }
                String keyAlg = certificates[0].getPublicKey().getAlgorithm();
                if (params.clientAuth != keyAlg) {
                    throw new Exception("Certificate type mismatch: " + keyAlg + " != " + params.clientAuth);
                }
            }
        } finally {
            if (socket != null) {
                socket.close();
            }
        }
    }
}
