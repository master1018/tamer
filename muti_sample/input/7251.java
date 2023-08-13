public class SslRMIClientSocketFactory
    implements RMIClientSocketFactory, Serializable {
    public SslRMIClientSocketFactory() {
    }
    public Socket createSocket(String host, int port) throws IOException {
        final SocketFactory sslSocketFactory = getDefaultClientSocketFactory();
        final SSLSocket sslSocket = (SSLSocket)
            sslSocketFactory.createSocket(host, port);
        final String enabledCipherSuites =
            System.getProperty("javax.rmi.ssl.client.enabledCipherSuites");
        if (enabledCipherSuites != null) {
            StringTokenizer st = new StringTokenizer(enabledCipherSuites, ",");
            int tokens = st.countTokens();
            String enabledCipherSuitesList[] = new String[tokens];
            for (int i = 0 ; i < tokens; i++) {
                enabledCipherSuitesList[i] = st.nextToken();
            }
            try {
                sslSocket.setEnabledCipherSuites(enabledCipherSuitesList);
            } catch (IllegalArgumentException e) {
                throw (IOException)
                    new IOException(e.getMessage()).initCause(e);
            }
        }
        final String enabledProtocols =
            System.getProperty("javax.rmi.ssl.client.enabledProtocols");
        if (enabledProtocols != null) {
            StringTokenizer st = new StringTokenizer(enabledProtocols, ",");
            int tokens = st.countTokens();
            String enabledProtocolsList[] = new String[tokens];
            for (int i = 0 ; i < tokens; i++) {
                enabledProtocolsList[i] = st.nextToken();
            }
            try {
                sslSocket.setEnabledProtocols(enabledProtocolsList);
            } catch (IllegalArgumentException e) {
                throw (IOException)
                    new IOException(e.getMessage()).initCause(e);
            }
        }
        return sslSocket;
    }
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        return this.getClass().equals(obj.getClass());
    }
    public int hashCode() {
        return this.getClass().hashCode();
    }
    private static SocketFactory defaultSocketFactory = null;
    private static synchronized SocketFactory getDefaultClientSocketFactory() {
        if (defaultSocketFactory == null)
            defaultSocketFactory = SSLSocketFactory.getDefault();
        return defaultSocketFactory;
    }
    private static final long serialVersionUID = -8310631444933958385L;
}
