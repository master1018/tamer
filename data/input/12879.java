public class MySSLServerSocketFacImpl extends SSLServerSocketFactory {
    private static String[] supportedCS = CipherSuites.CUSTOM;
    public static void useStandardCipherSuites() {
        supportedCS = CipherSuites.STANDARD;
    }
    public static void useCustomCipherSuites() {
        supportedCS = CipherSuites.CUSTOM;
    }
    public MySSLServerSocketFacImpl() {
        super();
    }
    public String[] getDefaultCipherSuites() {
        return (String[]) supportedCS.clone();
    }
    public String[] getSupportedCipherSuites() {
        return getDefaultCipherSuites();
    }
    public ServerSocket createServerSocket(int port) { return null; }
    public ServerSocket createServerSocket(int port, int backlog) {
        return null;
    }
    public ServerSocket createServerSocket(int port, int backlog,
                                           InetAddress ifAddress) {
        return null;
    }
}
