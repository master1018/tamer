public class DefaultSSLServSocketFac {
    public static void main(String[] args) throws Exception {
        try {
            Security.setProperty("ssl.ServerSocketFactory.provider", "oops");
            ServerSocketFactory ssocketFactory =
                        SSLServerSocketFactory.getDefault();
            SSLServerSocket sslServerSocket =
                        (SSLServerSocket)ssocketFactory.createServerSocket();
        } catch (Exception e) {
            if (!(e.getCause() instanceof ClassNotFoundException)) {
                throw e;
            }
        }
    }
}
