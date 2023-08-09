class SocketInputStream extends InputStream {
    private final PlainSocketImpl socket;
    public SocketInputStream(SocketImpl socket) {
        super();
        this.socket = (PlainSocketImpl) socket;
    }
    @Override
    public int available() throws IOException {
        return socket.available();
    }
    @Override
    public void close() throws IOException {
        socket.close();
    }
    @Override
    public int read() throws IOException {
        byte[] buffer = new byte[1];
        int result = socket.read(buffer, 0, 1);
        return (-1 == result) ? result : buffer[0] & 0xFF;
    }
    @Override
    public int read(byte[] buffer) throws IOException {
        return read(buffer, 0, buffer.length);
    }
    @Override
    public int read(byte[] buffer, int offset, int count) throws IOException {
        if (null == buffer) {
            throw new IOException(Msg.getString("K0047"));
        }
        if (0 == count) {
            return 0;
        }
        if (0 > offset || offset >= buffer.length) {
            throw new ArrayIndexOutOfBoundsException(Msg.getString("K002e", offset));
        }
        if (0 > count || offset + count > buffer.length) {
            throw new ArrayIndexOutOfBoundsException(Msg.getString("K002f"));
        }
        return socket.read(buffer, offset, count);
    }
    @Override
    public long skip(long n) throws IOException {
        return (0 == n) ? 0 : super.skip(n);
    }
}
