public class NoAuthClientAuth {
    private static boolean logging = true;
    private static boolean debug = false;
    private SSLContext sslc;
    private SSLEngine clientEngine;     
    private ByteBuffer clientOut;       
    private ByteBuffer clientIn;        
    private SSLEngine serverEngine;     
    private ByteBuffer serverOut;       
    private ByteBuffer serverIn;        
    private ByteBuffer cTOs;            
    private ByteBuffer sTOc;            
    private static String pathToStores = "../../../../../etc";
    private static String keyStoreFile = "keystore";
    private static String trustStoreFile = "truststore";
    private static String passwd = "passphrase";
    private static String keyFilename =
            System.getProperty("test.src", ".") + "/" + pathToStores +
                "/" + keyStoreFile;
    private static String trustFilename =
            System.getProperty("test.src", ".") + "/" + pathToStores +
                "/" + trustStoreFile;
    public static void main(String args[]) throws Exception {
        if (debug) {
            System.setProperty("javax.net.debug", "all");
        }
        NoAuthClientAuth test = new NoAuthClientAuth();
        test.runTest();
        System.out.println("Test Passed.");
    }
    public NoAuthClientAuth() throws Exception {
        KeyStore ks = KeyStore.getInstance("JKS");
        KeyStore ts = KeyStore.getInstance("JKS");
        char[] passphrase = "passphrase".toCharArray();
        ks.load(new FileInputStream(keyFilename), passphrase);
        ts.load(new FileInputStream(trustFilename), passphrase);
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, passphrase);
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(ts);
        SSLContext sslCtx = SSLContext.getInstance("TLS");
        sslCtx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
        sslc = sslCtx;
    }
    private void runTest() throws Exception {
        createSSLEngines();
        createBuffers();
        SSLEngineResult clientResult;   
        SSLEngineResult serverResult;   
        int hsCompleted = 0;
        while (!isEngineClosed(clientEngine) ||
                !isEngineClosed(serverEngine)) {
            log("================");
            clientResult = clientEngine.wrap(clientOut, cTOs);
            log("client wrap: ", clientResult);
            runDelegatedTasks(clientResult, clientEngine);
            clientOut.rewind();
            serverResult = serverEngine.wrap(serverOut, sTOc);
            log("server wrap: ", serverResult);
            runDelegatedTasks(serverResult, serverEngine);
            serverOut.rewind();
            if (serverResult.getHandshakeStatus() ==
                    HandshakeStatus.FINISHED) {
                hsCompleted++;
                log("\t" + hsCompleted + " handshake completed");
                if (hsCompleted == 1) {
                    try {
                        serverEngine.getSession().getPeerCertificates();
                        throw new Exception("Should have got exception");
                    } catch (SSLPeerUnverifiedException e) {
                        System.out.println("Caught proper exception." + e);
                    }
                    log("\tInvalidating session, setting client auth, " +
                        " starting rehandshake");
                    serverEngine.getSession().invalidate();
                    serverEngine.setNeedClientAuth(true);
                    serverEngine.beginHandshake();
                } else if (hsCompleted == 2) {
                    java.security.cert.Certificate [] certs =
                        serverEngine.getSession().getPeerCertificates();
                    System.out.println("Client Certificate(s) received");
                    for (java.security.cert.Certificate c : certs) {
                        System.out.println(c);
                    }
                    log("Closing server.");
                    serverEngine.closeOutbound();
                } 
            }
            cTOs.flip();
            sTOc.flip();
            log("----");
            clientResult = clientEngine.unwrap(sTOc, clientIn);
            log("client unwrap: ", clientResult);
            runDelegatedTasks(clientResult, clientEngine);
            clientIn.clear();
            serverResult = serverEngine.unwrap(cTOs, serverIn);
            log("server unwrap: ", serverResult);
            runDelegatedTasks(serverResult, serverEngine);
            serverIn.clear();
            cTOs.compact();
            sTOc.compact();
        }
    }
    private void createSSLEngines() throws Exception {
        serverEngine = sslc.createSSLEngine();
        serverEngine.setUseClientMode(false);
        serverEngine.setNeedClientAuth(false);
        clientEngine = sslc.createSSLEngine("client", 80);
        clientEngine.setUseClientMode(true);
    }
    private void createBuffers() {
        SSLSession session = clientEngine.getSession();
        int appBufferMax = session.getApplicationBufferSize();
        int netBufferMax = session.getPacketBufferSize();
        clientIn = ByteBuffer.allocate(appBufferMax + 50);
        serverIn = ByteBuffer.allocate(appBufferMax + 50);
        cTOs = ByteBuffer.allocateDirect(netBufferMax);
        sTOc = ByteBuffer.allocateDirect(netBufferMax);
        clientOut = ByteBuffer.wrap("Hi Server, I'm Client".getBytes());
        serverOut = ByteBuffer.wrap("Hello Client, I'm Server".getBytes());
    }
    private static void runDelegatedTasks(SSLEngineResult result,
            SSLEngine engine) throws Exception {
        if (result.getHandshakeStatus() == HandshakeStatus.NEED_TASK) {
            Runnable runnable;
            while ((runnable = engine.getDelegatedTask()) != null) {
                log("\trunning delegated task...");
                runnable.run();
            }
            HandshakeStatus hsStatus = engine.getHandshakeStatus();
            if (hsStatus == HandshakeStatus.NEED_TASK) {
                throw new Exception(
                    "handshake shouldn't need additional tasks");
            }
            log("\tnew HandshakeStatus: " + hsStatus);
        }
    }
    private static boolean isEngineClosed(SSLEngine engine) {
        return (engine.isOutboundDone() && engine.isInboundDone());
    }
    private static void checkTransfer(ByteBuffer a, ByteBuffer b)
            throws Exception {
        a.flip();
        b.flip();
        if (!a.equals(b)) {
            throw new Exception("Data didn't transfer cleanly");
        } else {
            log("\tData transferred cleanly");
        }
        a.position(a.limit());
        b.position(b.limit());
        a.limit(a.capacity());
        b.limit(b.capacity());
    }
    private static boolean resultOnce = true;
    private static void log(String str, SSLEngineResult result) {
        if (!logging) {
            return;
        }
        if (resultOnce) {
            resultOnce = false;
            System.out.println("The format of the SSLEngineResult is: \n" +
                "\t\"getStatus() / getHandshakeStatus()\" +\n" +
                "\t\"bytesConsumed() / bytesProduced()\"\n");
        }
        HandshakeStatus hsStatus = result.getHandshakeStatus();
        log(str +
            result.getStatus() + "/" + hsStatus + ", " +
            result.bytesConsumed() + "/" + result.bytesProduced() +
            " bytes");
        if (hsStatus == HandshakeStatus.FINISHED) {
            log("\t...ready for application data");
        }
    }
    private static void log(String str) {
        if (logging) {
            System.out.println(str);
        }
    }
}
