class DefaultSSLSocketFactory extends SSLSocketFactory {
    private final String errMessage;
    DefaultSSLSocketFactory(String mes) {
        errMessage = mes;
    }
    @Override
    public String[] getDefaultCipherSuites() {
        return new String[0];
    }
    @Override
    public String[] getSupportedCipherSuites() {
        return new String[0];
    }
    @Override
    public Socket createSocket(Socket s, String host, int port, boolean autoClose)
            throws IOException {
        throw new SocketException(errMessage);
    }
    @Override
    public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
        throw new SocketException(errMessage);
    }
    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort)
            throws IOException, UnknownHostException {
        throw new SocketException(errMessage);
    }
    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
        throw new SocketException(errMessage);
    }
    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress localAddress,
            int localPort) throws IOException {
        throw new SocketException(errMessage);
    }
}
