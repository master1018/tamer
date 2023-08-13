class DefaultSSLServerSocketFactory extends SSLServerSocketFactory {
    private final String errMessage;
    DefaultSSLServerSocketFactory(String mes) {
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
    public ServerSocket createServerSocket(int port) throws IOException {
        throw new SocketException(errMessage);
    }
    @Override
    public ServerSocket createServerSocket(int port, int backlog) throws IOException {
        throw new SocketException(errMessage);
    }
    @Override
    public ServerSocket createServerSocket(int port, int backlog, InetAddress iAddress)
            throws IOException {
        throw new SocketException(errMessage);
    }
}
