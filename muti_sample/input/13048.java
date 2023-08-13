public class AcceptLargeFragments {
    public static void main (String[] args) throws Exception {
        SSLContext context = SSLContext.getDefault();
        System.setProperty("jsse.SSLEngine.acceptLargeFragments", "true");
        SSLEngine cliEngine = context.createSSLEngine();
        cliEngine.setUseClientMode(true);
        SSLEngine srvEngine = context.createSSLEngine();
        srvEngine.setUseClientMode(false);
        SSLSession cliSession = cliEngine.getSession();
        SSLSession srvSession = srvEngine.getSession();
        if (cliSession.getPacketBufferSize() < 33049 ||
            srvSession.getPacketBufferSize() < 33049) {
                throw new Exception("Don't accept large SSL/TLS fragments");
        }
        if (cliSession.getApplicationBufferSize() < 32768 ||
            srvSession.getApplicationBufferSize() < 32768) {
                throw new Exception(
                        "Don't accept large SSL/TLS application data ");
        }
    }
}
