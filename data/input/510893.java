public abstract class OutputStream implements Closeable, Flushable {
    public OutputStream() {
        super();
    }
    public void close() throws IOException {
    }
    public void flush() throws IOException {
    }
    public void write(byte[] buffer) throws IOException {
        write(buffer, 0, buffer.length);
    }
    public void write(byte[] buffer, int offset, int count) throws IOException {
        if (buffer == null) {
            throw new NullPointerException(Msg.getString("K0047")); 
        }
        if ((offset | count) < 0 || count > buffer.length - offset) {
            throw new IndexOutOfBoundsException(Msg.getString("K002f")); 
        }
        for (int i = offset; i < offset + count; i++) {
            write(buffer[i]);
        }
    }
    public abstract void write(int oneByte) throws IOException;
    boolean checkError() {
        return false;
    }
}
