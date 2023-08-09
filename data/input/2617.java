public class CheckSockFacExport2 {
    public static void main(String argv[]) throws Exception {
        Security.setProperty("ssl.SocketFactory.provider",
            "MySSLSocketFacImpl");
        MySSLSocketFacImpl.useStandardCipherSuites();
        Security.setProperty("ssl.ServerSocketFactory.provider",
            "MySSLServerSocketFacImpl");
        MySSLServerSocketFacImpl.useStandardCipherSuites();
        boolean result = false;
        for (int i = 0; i < 2; i++) {
            switch (i) {
            case 0:
                System.out.println("Testing SSLSocketFactory:");
                SSLSocketFactory sf = (SSLSocketFactory)
                    SSLSocketFactory.getDefault();
                result = (sf instanceof MySSLSocketFacImpl);
                break;
            case 1:
                System.out.println("Testing SSLServerSocketFactory:");
                SSLServerSocketFactory ssf = (SSLServerSocketFactory)
                    SSLServerSocketFactory.getDefault();
                result = (ssf instanceof MySSLServerSocketFacImpl);
                break;
            default:
                throw new Exception("Internal Test Error");
            }
            if (result) {
                System.out.println("...accepted valid SFs");
            } else {
                throw new Exception("...wrong SF is used");
            }
        }
        System.out.println("Test Passed");
    }
}
