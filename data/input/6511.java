public class EngineEnforceUseClientMode {
    private static boolean debug = false;
    private SSLContext sslc;
    private SSLEngine ssle1;    
    private SSLEngine ssle2;    
    private SSLEngine ssle3;    
    private SSLEngine ssle4;    
    private SSLEngine ssle5;    
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
    private ByteBuffer appOut1;         
    private ByteBuffer appIn1;          
    private ByteBuffer appOut2;         
    private ByteBuffer appIn2;          
    private ByteBuffer oneToTwo;        
    private ByteBuffer twoToOne;        
    private void createSSLEngines() throws Exception {
        ssle1 = sslc.createSSLEngine("client", 1);
        ssle1.setUseClientMode(true);
        ssle2 = sslc.createSSLEngine();
        ssle2.setUseClientMode(false);
        ssle2.setNeedClientAuth(true);
        ssle3 = sslc.createSSLEngine();
        ssle4 = sslc.createSSLEngine();
        ssle5 = sslc.createSSLEngine();
    }
    private void runTest() throws Exception {
        createSSLEngines();
        createBuffers();
        try {
            System.out.println("Testing wrap()");
            ssle3.wrap(appOut1, oneToTwo);
            throw new RuntimeException(
                "wrap():  Didn't catch the exception properly");
        } catch (IllegalStateException e) {
            System.out.println("Caught the correct exception.");
            ssle3.wrap(appOut1, oneToTwo);
            oneToTwo.flip();
            if (oneToTwo.hasRemaining()) {
                throw new Exception("wrap1 generated data");
            }
            oneToTwo.clear();
        }
        try {
            System.out.println("Testing unwrap()");
            ssle4.unwrap(oneToTwo, appIn1);
            throw new RuntimeException(
                "unwrap():  Didn't catch the exception properly");
        } catch (IllegalStateException e) {
            System.out.println("Caught the correct exception.");
            ssle4.wrap(appOut1, oneToTwo);
            oneToTwo.flip();
            if (oneToTwo.hasRemaining()) {
                throw new Exception("wrap2 generated data");
            }
            oneToTwo.clear();
        }
        try {
            System.out.println("Testing beginHandshake()");
            ssle5.beginHandshake();
            throw new RuntimeException(
                "unwrap():  Didn't catch the exception properly");
        } catch (IllegalStateException e) {
            System.out.println("Caught the correct exception.");
            ssle5.wrap(appOut1, oneToTwo);
            oneToTwo.flip();
            if (oneToTwo.hasRemaining()) {
                throw new Exception("wrap3 generated data");
            }
            oneToTwo.clear();
        }
        boolean dataDone = false;
        SSLEngineResult result1;        
        SSLEngineResult result2;        
        while (!isEngineClosed(ssle1) || !isEngineClosed(ssle2)) {
            log("================");
            result1 = ssle1.wrap(appOut1, oneToTwo);
            result2 = ssle2.wrap(appOut2, twoToOne);
            log("wrap1:  " + result1);
            log("oneToTwo  = " + oneToTwo);
            log("");
            log("wrap2:  " + result2);
            log("twoToOne  = " + twoToOne);
            runDelegatedTasks(result1, ssle1);
            runDelegatedTasks(result2, ssle2);
            oneToTwo.flip();
            twoToOne.flip();
            log("----");
            result1 = ssle1.unwrap(twoToOne, appIn1);
            result2 = ssle2.unwrap(oneToTwo, appIn2);
            log("unwrap1: " + result1);
            log("twoToOne  = " + twoToOne);
            log("");
            log("unwrap2: " + result2);
            log("oneToTwo  = " + oneToTwo);
            runDelegatedTasks(result1, ssle1);
            runDelegatedTasks(result2, ssle2);
            oneToTwo.compact();
            twoToOne.compact();
            if (!dataDone && (appOut1.limit() == appIn2.position()) &&
                    (appOut2.limit() == appIn1.position())) {
                checkTransfer(appOut1, appIn2);
                checkTransfer(appOut2, appIn1);
                System.out.println("Try changing modes...");
                try {
                    ssle2.setUseClientMode(false);
                    throw new RuntimeException(
                        "setUseClientMode():  " +
                        "Didn't catch the exception properly");
                } catch (IllegalArgumentException e) {
                    System.out.println("Caught the correct exception.");
                }
                return;
            }
        }
    }
    public static void main(String args[]) throws Exception {
        EngineEnforceUseClientMode test;
        test = new EngineEnforceUseClientMode();
        test.createSSLEngines();
        test.runTest();
        System.out.println("Test Passed.");
    }
    public EngineEnforceUseClientMode() throws Exception {
        sslc = getSSLContext(keyFilename, trustFilename);
    }
    private SSLContext getSSLContext(String keyFile, String trustFile)
            throws Exception {
        KeyStore ks = KeyStore.getInstance("JKS");
        KeyStore ts = KeyStore.getInstance("JKS");
        char[] passphrase = "passphrase".toCharArray();
        ks.load(new FileInputStream(keyFile), passphrase);
        ts.load(new FileInputStream(trustFile), passphrase);
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, passphrase);
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(ts);
        SSLContext sslCtx = SSLContext.getInstance("TLS");
        sslCtx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
        return sslCtx;
    }
    private void createBuffers() {
        SSLSession session = ssle1.getSession();
        int appBufferMax = session.getApplicationBufferSize();
        int netBufferMax = session.getPacketBufferSize();
        appIn1 = ByteBuffer.allocateDirect(appBufferMax + 50);
        appIn2 = ByteBuffer.allocateDirect(appBufferMax + 50);
        oneToTwo = ByteBuffer.allocateDirect(netBufferMax);
        twoToOne = ByteBuffer.allocateDirect(netBufferMax);
        appOut1 = ByteBuffer.wrap("Hi Engine2, I'm SSLEngine1".getBytes());
        appOut2 = ByteBuffer.wrap("Hello Engine1, I'm SSLEngine2".getBytes());
        log("AppOut1 = " + appOut1);
        log("AppOut2 = " + appOut2);
        log("");
    }
    private static void runDelegatedTasks(SSLEngineResult result,
            SSLEngine engine) throws Exception {
        if (result.getHandshakeStatus() == HandshakeStatus.NEED_TASK) {
            Runnable runnable;
            while ((runnable = engine.getDelegatedTask()) != null) {
                log("running delegated task...");
                runnable.run();
            }
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
            log("Data transferred cleanly");
        }
        a.clear();
        b.clear();
    }
    private static void log(String str) {
        if (debug) {
            System.out.println(str);
        }
    }
}
