class SocketOutputStream extends OutputStream {
    private PlainSocketImpl socket;
    public SocketOutputStream(SocketImpl socket) {
        super();
        this.socket = (PlainSocketImpl) socket;
    }
    @Override
    public void close() throws IOException {
        socket.close();
    }
    @Override
    public void write(byte[] buffer) throws IOException {
        socket.write(buffer, 0, buffer.length);
    }
    @Override
    public void write(byte[] buffer, int offset, int count) throws IOException {
        if (buffer != null) {
            if (0 <= offset && offset <= buffer.length && 0 <= count
                    && count <= buffer.length - offset) {
                socket.write(buffer, offset, count);
            } else {
                throw new ArrayIndexOutOfBoundsException(Msg.getString("K002f"));
            }
        } else {
            throw new NullPointerException(Msg.getString("K0047"));
        }
    }
    @Override
    public void write(int oneByte) throws IOException {
        byte[] buffer = new byte[1];
        buffer[0] = (byte) (oneByte & 0xFF);
        socket.write(buffer, 0, 1);
    }
}
