public class SSL_NULL {
    private static volatile Boolean result;
    public static void main(String[] args) throws Exception {
        final SSLServerSocket serverSocket = (SSLServerSocket)
            SSLServerSocketFactory.getDefault().createServerSocket(0);
        serverSocket.setEnabledCipherSuites(
            serverSocket.getSupportedCipherSuites());
        new Thread() {
            public void run() {
                try {
                    SSLSocket socket = (SSLSocket) serverSocket.accept();
                    String suite = socket.getSession().getCipherSuite();
                    if (!"SSL_NULL_WITH_NULL_NULL".equals(suite)) {
                        System.err.println(
                            "Wrong suite for failed handshake: " +
                            "got " + suite +
                            ", expected SSL_NULL_WITH_NULL_NULL");
                    } else {
                        result = Boolean.TRUE;
                        return;
                    }
                } catch (IOException e) {
                    System.err.println("Unexpected exception");
                    e.printStackTrace();
                } finally {
                    if (result == null) {
                        result = Boolean.FALSE;
                    }
                }
            }
        }.start();
        SSLSocket socket = (SSLSocket)
            SSLSocketFactory.getDefault().createSocket(
                "localhost", serverSocket.getLocalPort());
        socket.setEnabledCipherSuites(
            new String[] { "SSL_RSA_WITH_RC4_128_MD5" });
        try {
            OutputStream out = socket.getOutputStream();
            out.write(0);
            out.flush();
            throw new RuntimeException("No exception received");
        } catch (SSLHandshakeException e) {
        }
        System.out.println("client: " + socket.getSession().getCipherSuite());
        while (result == null) {
            Thread.sleep(50);
        }
        if (result.booleanValue()) {
            System.out.println("Test passed");
        } else {
            throw new Exception("Test failed");
        }
    }
}
