public class CheckSockFacExport1 {
    public static void main(String argv[]) throws Exception {
        Security.setProperty("ssl.SocketFactory.provider",
                             "MySSLSocketFacImpl");
        MySSLSocketFacImpl.useCustomCipherSuites();
        Security.setProperty("ssl.ServerSocketFactory.provider",
            "MySSLServerSocketFacImpl");
        MySSLServerSocketFacImpl.useCustomCipherSuites();
        String[] supportedCS = null;
        for (int i = 0; i < 2; i++) {
            switch (i) {
            case 0:
                System.out.println("Testing SSLSocketFactory:");
                SSLSocketFactory sf = (SSLSocketFactory)
                    SSLSocketFactory.getDefault();
                supportedCS = sf.getSupportedCipherSuites();
                break;
            case 1:
                System.out.println("Testing SSLServerSocketFactory:");
                SSLServerSocketFactory ssf = (SSLServerSocketFactory)
                    SSLServerSocketFactory.getDefault();
                supportedCS = ssf.getSupportedCipherSuites();
                break;
            default:
                throw new Exception("Internal Test Error");
            }
            System.out.println(Arrays.asList(supportedCS));
            if (supportedCS.length == 0) {
                throw new Exception("supported ciphersuites are empty");
            }
        }
        System.out.println("Test Passed");
    }
}
