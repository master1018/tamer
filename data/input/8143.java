class ChannelIO {
    protected SocketChannel sc;
    protected ByteBuffer requestBB;
    static private int requestBBSize = 4096;
    protected ChannelIO(SocketChannel sc, boolean blocking)
            throws IOException {
        this.sc = sc;
        sc.configureBlocking(blocking);
    }
    static ChannelIO getInstance(SocketChannel sc, boolean blocking)
            throws IOException {
        ChannelIO cio = new ChannelIO(sc, blocking);
        cio.requestBB = ByteBuffer.allocate(requestBBSize);
        return cio;
    }
    SocketChannel getSocketChannel() {
        return sc;
    }
    protected void resizeRequestBB(int remaining) {
        if (requestBB.remaining() < remaining) {
            ByteBuffer bb = ByteBuffer.allocate(requestBB.capacity() * 2);
            requestBB.flip();
            bb.put(requestBB);
            requestBB = bb;
        }
    }
    boolean doHandshake() throws IOException {
        return true;
    }
    boolean doHandshake(SelectionKey sk) throws IOException {
        return true;
    }
    int read() throws IOException {
        resizeRequestBB(requestBBSize/20);
        return sc.read(requestBB);
    }
    ByteBuffer getReadBuf() {
        return requestBB;
    }
    int write(ByteBuffer src) throws IOException {
        return sc.write(src);
    }
    long transferTo(FileChannel fc, long pos, long len) throws IOException {
        return fc.transferTo(pos, len, sc);
    }
    boolean dataFlush() throws IOException {
        return true;
    }
    boolean shutdown() throws IOException {
        return true;
    }
    void close() throws IOException {
        sc.close();
    }
}
