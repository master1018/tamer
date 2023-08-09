public class CloseInboundException {
    private SSLEngine ssle1;    
    private SSLEngine ssle2;    
    SSLEngineResult result1;    
    SSLEngineResult result2;    
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
    private void runTest(boolean inboundClose) throws Exception {
        boolean done = false;
        while (!isEngineClosed(ssle1) || !isEngineClosed(ssle2)) {
            System.out.println("================");
            result1 = ssle1.wrap(appOut1, oneToTwo);
            result2 = ssle2.wrap(appOut2, twoToOne);
            System.out.println("wrap1 = " + result1);
            System.out.println("oneToTwo  = " + oneToTwo);
            System.out.println("wrap2 = " + result2);
            System.out.println("twoToOne  = " + twoToOne);
            runDelegatedTasks(result1, ssle1);
            runDelegatedTasks(result2, ssle2);
            oneToTwo.flip();
            twoToOne.flip();
            System.out.println("----");
            result1 = ssle1.unwrap(twoToOne, appIn1);
            if (done && inboundClose) {
                try {
                    result2 = ssle2.unwrap(oneToTwo, appIn2);
                    throw new Exception("Didn't throw Exception");
                } catch (SSLException e) {
                    System.out.println("Caught proper exception\n" + e);
                    return;
                }
            } else {
                result2 = ssle2.unwrap(oneToTwo, appIn2);
            }
            System.out.println("unwrap1 = " + result1);
            System.out.println("twoToOne  = " + twoToOne);
            System.out.println("unwrap2 = " + result2);
            System.out.println("oneToTwo  = " + oneToTwo);
            runDelegatedTasks(result1, ssle1);
            runDelegatedTasks(result2, ssle2);
            oneToTwo.compact();
            twoToOne.compact();
            if (!done && (appOut1.limit() == appIn2.position()) &&
                (appOut2.limit() == appIn1.position())) {
                if (inboundClose) {
                    try {
                        System.out.println("Closing ssle1's *INBOUND*...");
                        ssle1.closeInbound();
                        throw new Exception("closeInbound didn't throw");
                    } catch (SSLException e) {
                        System.out.println("Caught closeInbound exc properly");
                        checkStatus();
                    }
                    done = true;
                } else {
                    done = true;
                    System.out.println("Closing ssle1's *OUTBOUND*...");
                    ssle1.closeOutbound();
                }
            }
        }
    }
    private void checkStatus() throws Exception {
        System.out.println("\nCalling last wrap");
        int pos = oneToTwo.position();
        result1 = ssle1.wrap(appOut1, oneToTwo);
        System.out.println("result1 = " + result1);
        if ((pos >= oneToTwo.position()) ||
                !result1.getStatus().equals(Status.CLOSED) ||
                !result1.getHandshakeStatus().equals(
                    HandshakeStatus.NOT_HANDSHAKING) ||
                !ssle1.isOutboundDone() ||
                !ssle1.isInboundDone()) {
            throw new Exception(result1.toString());
        }
        System.out.println("Make sure we don't throw a second SSLException.");
        ssle1.closeInbound();
    }
    public static void main(String args[]) throws Exception {
        CloseInboundException test;
        test = new CloseInboundException();
        test.runTest(false);
        test = new CloseInboundException();
        test.runTest(true);
        System.out.println("Test PASSED!!!");
    }
    public CloseInboundException() throws Exception {
        SSLContext sslc = getSSLContext(keyFilename, trustFilename);
        ssle1 = sslc.createSSLEngine("host1", 1);
        ssle1.setUseClientMode(true);
        ssle2 = sslc.createSSLEngine("host2", 2);
        ssle2.setUseClientMode(false);
        createBuffers();
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
        System.out.println("AppOut1 = " + appOut1);
        System.out.println("AppOut2 = " + appOut2);
        System.out.println();
    }
    private static void runDelegatedTasks(SSLEngineResult result,
            SSLEngine engine) throws Exception {
        if (result.getHandshakeStatus().equals(HandshakeStatus.NEED_TASK)) {
            Runnable runnable;
            while ((runnable = engine.getDelegatedTask()) != null) {
                System.out.println("running delegated task...");
                runnable.run();
            }
        }
    }
    private static boolean isEngineClosed(SSLEngine engine) {
        return (engine.isOutboundDone() && engine.isInboundDone());
    }
}
