class MockSocketChannel extends SocketChannel {
    protected MockSocketChannel(SelectorProvider arg0) {
        super(arg0);
    }
    public Socket socket() {
        return null;
    }
    public boolean isConnected() {
        return false;
    }
    public boolean isConnectionPending() {
        return false;
    }
    public boolean connect(SocketAddress arg0) throws IOException {
        return false;
    }
    public boolean finishConnect() throws IOException {
        return false;
    }
    public int read(ByteBuffer arg0) throws IOException {
        return 0;
    }
    public long read(ByteBuffer[] arg0, int arg1, int arg2) throws IOException {
        return 0;
    }
    public int write(ByteBuffer arg0) throws IOException {
        return 0;
    }
    public long write(ByteBuffer[] arg0, int arg1, int arg2) throws IOException {
        return 0;
    }
    protected void implCloseSelectableChannel() throws IOException {
    }
    protected void implConfigureBlocking(boolean arg0) throws IOException {
    }
}
