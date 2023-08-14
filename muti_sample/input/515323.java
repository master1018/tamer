class MockServerSocketChannel extends ServerSocketChannel {
    protected MockServerSocketChannel(SelectorProvider arg0) {
        super(arg0);
    }
    public ServerSocket socket() {
        return null;
    }
    public SocketChannel accept() throws IOException {
        return null;
    }
    protected void implCloseSelectableChannel() throws IOException {
    }
    protected void implConfigureBlocking(boolean arg0) throws IOException {
    }
}
