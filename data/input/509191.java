public class OpenSSLServerSocketFactoryImpl extends javax.net.ssl.SSLServerSocketFactory {
    private SSLParameters sslParameters;
    private IOException instantiationException;
    public OpenSSLServerSocketFactoryImpl() {
        super();
        try {
            this.sslParameters = SSLParameters.getDefault();
            this.sslParameters.setUseClientMode(false);
        } catch (KeyManagementException e) {
            instantiationException =
                new IOException("Delayed instantiation exception:");
            instantiationException.initCause(e);
        }
    }
    public OpenSSLServerSocketFactoryImpl(SSLParameters sslParameters) {
        this.sslParameters = sslParameters;
    }
    public String[] getDefaultCipherSuites() {
        return OpenSSLServerSocketImpl.nativegetsupportedciphersuites();
    }
    public String[] getSupportedCipherSuites() {
        return OpenSSLServerSocketImpl.nativegetsupportedciphersuites();
    }
    public ServerSocket createServerSocket() throws IOException {
        return new OpenSSLServerSocketImpl((SSLParameters) sslParameters.clone());
    }
    public ServerSocket createServerSocket(int port) throws IOException {
        return new OpenSSLServerSocketImpl(port, (SSLParameters) sslParameters.clone());
    }
    public ServerSocket createServerSocket(int port, int backlog)
            throws IOException {
        return new OpenSSLServerSocketImpl(port, backlog, (SSLParameters) sslParameters.clone());
    }
    public ServerSocket createServerSocket(int port, int backlog,
            InetAddress iAddress) throws IOException {
        return new OpenSSLServerSocketImpl(port, backlog, iAddress, (SSLParameters) sslParameters.clone());
    }
}
