public class DelegatedTaskWrongException {
    private static boolean debug = false;
    private SSLContext sslc;
    private SSLEngine ssle1;    
    private SSLEngine ssle2;    
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
        ssle1.setEnabledProtocols(new String [] { "SSLv3" });
        ssle2.setEnabledProtocols(new String [] { "TLSv1" });
    }
    private void runTest() throws Exception {
        boolean dataDone = false;
        createSSLEngines();
        createBuffers();
        SSLEngineResult result1;        
        SSLEngineResult result2;        
        result1 = ssle1.wrap(appOut1, oneToTwo);
        oneToTwo.flip();
        result2 = ssle2.unwrap(oneToTwo, appIn2);
        runDelegatedTasks(result2, ssle2);
        try {
            result2 = ssle2.unwrap(oneToTwo, appIn2);
            throw new Exception(
                "TEST FAILED:  Didn't generate any exception");
        } catch (SSLHandshakeException e) {
            System.out.println("TEST PASSED:  Caught right exception");
        } catch (SSLException e) {
            System.out.println("TEST FAILED:  Generated wrong exception");
            throw e;
        }
    }
    public static void main(String args[]) throws Exception {
        DelegatedTaskWrongException test;
        test = new DelegatedTaskWrongException();
        test.createSSLEngines();
        test.runTest();
        System.out.println("Test Passed.");
    }
    public DelegatedTaskWrongException() throws Exception {
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
        a.position(a.limit());
        b.position(b.limit());
        a.limit(a.capacity());
        b.limit(b.capacity());
    }
    private static void log(String str) {
        if (debug) {
            System.out.println(str);
        }
    }
}
