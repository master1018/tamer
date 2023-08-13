public class OpenSSLSocketFactoryImpl extends javax.net.ssl.SSLSocketFactory {
    private SSLParameters sslParameters;
    private IOException instantiationException;
    public OpenSSLSocketFactoryImpl() {
        super();
        try {
            sslParameters = SSLParameters.getDefault();
        } catch (KeyManagementException e) {
            instantiationException =
                new IOException("Delayed instantiation exception:");
            instantiationException.initCause(e);
        }
    }
    public OpenSSLSocketFactoryImpl(SSLParameters sslParameters) {
        super();
        this.sslParameters = sslParameters;
    }
    public String[] getDefaultCipherSuites() {
        return OpenSSLSocketImpl.nativegetsupportedciphersuites();
    }
    public String[] getSupportedCipherSuites() {
        return OpenSSLSocketImpl.nativegetsupportedciphersuites();
    }
    public Socket createSocket() throws IOException {
        if (instantiationException != null) {
            throw instantiationException;
        }
        return new OpenSSLSocketImpl((SSLParameters) sslParameters.clone());
    }
    public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
        return new OpenSSLSocketImpl(host, port, (SSLParameters) sslParameters.clone());
    }
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort)
            throws IOException, UnknownHostException {
        return new OpenSSLSocketImpl(host, port, localHost, localPort, (SSLParameters) sslParameters.clone());
    }
    public Socket createSocket(InetAddress host, int port) throws IOException {
        return new OpenSSLSocketImpl(host, port, (SSLParameters) sslParameters.clone());
    }
    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort)
            throws IOException {
        return new OpenSSLSocketImpl(address, port, localAddress, localPort, (SSLParameters) sslParameters.clone());
    }
    public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
        return new OpenSSLSocketImplWrapper(s, host, port, autoClose, (SSLParameters) sslParameters.clone());
    }
}
