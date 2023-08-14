public class RehandshakeFinished {
    private static boolean logging = true;
    private static boolean debug = false;
    static private SSLContext sslc;
    private SSLEngine clientEngine;     
    private ByteBuffer clientOut;       
    private ByteBuffer clientIn;        
    private SSLEngine serverEngine;     
    private ByteBuffer serverOut;       
    private ByteBuffer serverIn;        
    private ByteBuffer cTOs;            
    private ByteBuffer sTOc;            
    private static String pathToStores = "../../../../../../../etc";
    private static String keyStoreFile = "keystore";
    private static String trustStoreFile = "truststore";
    private static String passwd = "passphrase";
    private static String keyFilename =
            System.getProperty("test.src", "./") + "/" + pathToStores +
                "/" + keyStoreFile;
    private static String trustFilename =
            System.getProperty("test.src", "./") + "/" + pathToStores +
                "/" + trustStoreFile;
    private static Exception loadException = null;
    static {
        try {
            KeyStore ks = KeyStore.getInstance("JKS");
            KeyStore ts = KeyStore.getInstance("JKS");
            char[] passphrase = "passphrase".toCharArray();
            ks.load(new FileInputStream(keyFilename), passphrase);
            ts.load(new FileInputStream(trustFilename), passphrase);
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(ks, passphrase);
            TrustManagerFactory tmf =
                TrustManagerFactory.getInstance("SunX509");
            tmf.init(ts);
            SSLContext sslCtx = SSLContext.getInstance("TLS");
            sslCtx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
            sslc = sslCtx;
        } catch (Exception e) {
            loadException = e;
        }
    }
    public static void main(String args[]) throws Exception {
        if (debug) {
            System.setProperty("javax.net.debug", "all");
        }
        if (loadException != null) {
            throw loadException;
        }
        if ((new RehandshakeFinished().runTest()) !=
                new RehandshakeFinished().runRehandshake()) {
            throw new Exception("Sessions not equivalent");
        }
        System.out.println("Test Passed.");
    }
    private void checkResult(SSLEngine engine, SSLEngineResult result,
            HandshakeStatus rqdHsStatus,
            boolean consumed, boolean produced) throws Exception {
        HandshakeStatus hsStatus = result.getHandshakeStatus();
        if (hsStatus == HandshakeStatus.NEED_TASK) {
            Runnable runnable;
            while ((runnable = engine.getDelegatedTask()) != null) {
                runnable.run();
            }
            hsStatus = engine.getHandshakeStatus();
        }
        if (hsStatus != rqdHsStatus) {
            throw new Exception("Required " + rqdHsStatus +
                ", got " + hsStatus);
        }
        int bc = result.bytesConsumed();
        int bp = result.bytesProduced();
        if (consumed) {
            if (bc <= 0) {
                throw new Exception("Should have consumed bytes");
            }
        } else {
            if (bc > 0) {
                throw new Exception("Should not have consumed bytes");
            }
        }
        if (produced) {
            if (bp <= 0) {
                throw new Exception("Should have produced bytes");
            }
        } else {
            if (bp > 0) {
                throw new Exception("Should not have produced bytes");
            }
        }
    }
    private SSLSession runRehandshake() throws Exception {
        log("\n\n==============================================");
        log("Staring actual test.");
        createSSLEngines();
        createBuffers();
        SSLEngineResult result;
        log("Client's ClientHello");
        checkResult(clientEngine,
            clientEngine.wrap(clientOut, cTOs), HandshakeStatus.NEED_UNWRAP,
            false, true);
        cTOs.flip();
        checkResult(serverEngine,
            serverEngine.unwrap(cTOs, serverIn), HandshakeStatus.NEED_WRAP,
            true, false);
        cTOs.compact();
        log("Server's ServerHello/ServerHelloDone");
        checkResult(serverEngine,
            serverEngine.wrap(serverOut, sTOc), HandshakeStatus.NEED_WRAP,
            false, true);
        sTOc.flip();
        checkResult(clientEngine,
            clientEngine.unwrap(sTOc, clientIn), HandshakeStatus.NEED_UNWRAP,
            true, false);
        sTOc.compact();
        log("Server's CCS");
        checkResult(serverEngine,
            serverEngine.wrap(serverOut, sTOc), HandshakeStatus.NEED_WRAP,
            false, true);
        sTOc.flip();
        checkResult(clientEngine,
            clientEngine.unwrap(sTOc, clientIn), HandshakeStatus.NEED_UNWRAP,
            true, false);
        sTOc.compact();
        log("Server's FINISHED");
        checkResult(serverEngine,
            serverEngine.wrap(serverOut, sTOc), HandshakeStatus.NEED_UNWRAP,
            false, true);
        sTOc.flip();
        checkResult(clientEngine,
            clientEngine.unwrap(sTOc, clientIn), HandshakeStatus.NEED_WRAP,
            true, false);
        sTOc.compact();
        log("Client's CCS");
        checkResult(clientEngine,
            clientEngine.wrap(clientOut, cTOs), HandshakeStatus.NEED_WRAP,
            false, true);
        cTOs.flip();
        checkResult(serverEngine,
            serverEngine.unwrap(cTOs, serverIn), HandshakeStatus.NEED_UNWRAP,
            true, false);
        cTOs.compact();
        log("Client's FINISHED should trigger FINISHED messages all around.");
        checkResult(clientEngine,
            clientEngine.wrap(clientOut, cTOs), HandshakeStatus.FINISHED,
            false, true);
        cTOs.flip();
        checkResult(serverEngine,
            serverEngine.unwrap(cTOs, serverIn), HandshakeStatus.FINISHED,
            true, false);
        cTOs.compact();
        return clientEngine.getSession();
    }
    private SSLSession runTest() throws Exception {
        boolean dataDone = false;
        createSSLEngines();
        createBuffers();
        SSLEngineResult clientResult;   
        SSLEngineResult serverResult;   
        while (!isEngineClosed(clientEngine) ||
                !isEngineClosed(serverEngine)) {
            log("================");
            clientResult = clientEngine.wrap(clientOut, cTOs);
            log("client wrap: ", clientResult);
            runDelegatedTasks(clientResult, clientEngine);
            serverResult = serverEngine.wrap(serverOut, sTOc);
            log("server wrap: ", serverResult);
            runDelegatedTasks(serverResult, serverEngine);
            cTOs.flip();
            sTOc.flip();
            log("----");
            clientResult = clientEngine.unwrap(sTOc, clientIn);
            log("client unwrap: ", clientResult);
            runDelegatedTasks(clientResult, clientEngine);
            serverResult = serverEngine.unwrap(cTOs, serverIn);
            log("server unwrap: ", serverResult);
            runDelegatedTasks(serverResult, serverEngine);
            cTOs.compact();
            sTOc.compact();
            if (!dataDone && (clientOut.limit() == serverIn.position()) &&
                    (serverOut.limit() == clientIn.position())) {
                checkTransfer(serverOut, clientIn);
                checkTransfer(clientOut, serverIn);
                log("\tClosing clientEngine's *OUTBOUND*...");
                clientEngine.closeOutbound();
                dataDone = true;
            }
        }
        return clientEngine.getSession();
    }
    private void createSSLEngines() throws Exception {
        serverEngine = sslc.createSSLEngine();
        serverEngine.setUseClientMode(false);
        serverEngine.setNeedClientAuth(true);
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
