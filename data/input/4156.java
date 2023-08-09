public class SSLEngineDeadlock {
    private static boolean logging = false;
    private static boolean debug = false;
    private SSLContext sslc;
    private SSLEngine clientEngine;     
    private ByteBuffer clientOut;       
    private ByteBuffer clientIn;        
    private SSLEngine serverEngine;     
    private ByteBuffer serverOut;       
    private ByteBuffer serverIn;        
    private volatile boolean testDone = false;
    private ByteBuffer cTOs;            
    private ByteBuffer sTOc;            
    private static String pathToStores = "../../../../../../../etc";
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
        for (int i = 1; i <= 200; i++) {
            if ((i % 5) == 0) {
                System.out.println("Test #: " + i);
            }
            SSLEngineDeadlock test = new SSLEngineDeadlock();
            test.runTest();
        }
        System.out.println("Test Passed.");
    }
    public SSLEngineDeadlock() throws Exception {
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
    private void doTask() {
        Runnable task;
        while (!testDone) {
            if ((task = clientEngine.getDelegatedTask()) != null) {
                task.run();
            }
            if ((task = serverEngine.getDelegatedTask()) != null) {
                task.run();
            }
        }
    }
    private void runTest() throws Exception {
        boolean dataDone = false;
        createSSLEngines();
        createBuffers();
        SSLEngineResult clientResult;   
        SSLEngineResult serverResult;   
        new Thread("SSLEngine Task Dispatcher") {
            public void run() {
                try {
                    doTask();
                } catch (Exception e) {
                    System.err.println("Task thread died...test will hang");
                }
            }
        }.start();
        while (!isEngineClosed(clientEngine) ||
                !isEngineClosed(serverEngine)) {
            log("================");
            clientResult = clientEngine.wrap(clientOut, cTOs);
            log("client wrap: ", clientResult);
            serverResult = serverEngine.wrap(serverOut, sTOc);
            log("server wrap: ", serverResult);
            cTOs.flip();
            sTOc.flip();
            log("----");
            clientResult = clientEngine.unwrap(sTOc, clientIn);
            log("client unwrap: ", clientResult);
            serverResult = serverEngine.unwrap(cTOs, serverIn);
            log("server unwrap: ", serverResult);
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
        testDone = true;
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
