final class DefaultSocketFactory extends SocketFactory {
    DefaultSocketFactory() {
        super();
    }
    @Override
    public Socket createSocket() throws IOException {
        return new Socket();
    }
    @Override
    public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
        return new Socket(host, port);
    }
    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort)
            throws IOException, UnknownHostException {
        return new Socket(host, port, localHost, localPort);
    }
    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
        return new Socket(host, port);
    }
    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress localAddress,
            int localPort) throws IOException {
        return new Socket(address, port, localAddress, localPort);
    }
}
