final class DefaultServerSocketFactory extends ServerSocketFactory {
    DefaultServerSocketFactory() {
        super();
    }
    @Override
    public ServerSocket createServerSocket(int port) throws IOException {
        return new ServerSocket(port);
    }
    @Override
    public ServerSocket createServerSocket(int port, int backlog) throws IOException {
        return new ServerSocket(port, backlog);
    }
    @Override
    public ServerSocket createServerSocket(int port, int backlog, InetAddress iAddress)
            throws IOException {
        return new ServerSocket(port, backlog, iAddress);
    }
}
