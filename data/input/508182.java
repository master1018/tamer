public class FilterOutputStream extends OutputStream {
    protected OutputStream out;
    public FilterOutputStream(OutputStream out) {
        this.out = out;
    }
    @Override
    public void close() throws IOException {
        Throwable thrown = null;
        try {
            flush();
        } catch (Throwable e) {
            thrown = e;
        }
        try {
            out.close();
        } catch (Throwable e) {
            if (thrown == null) {
                thrown = e;
            }
        }
        if (thrown != null) {
            SneakyThrow.sneakyThrow(thrown);
        }
    }
    @Override
    public void flush() throws IOException {
        out.flush();
    }
    @Override
    public void write(byte[] buffer) throws IOException {
        write(buffer, 0, buffer.length);
    }
    @Override
    public void write(byte[] buffer, int offset, int length) throws IOException {
        if (offset > buffer.length || offset < 0) {
            throw new ArrayIndexOutOfBoundsException(Msg.getString("K002e", offset)); 
        }
        if (length < 0 || length > buffer.length - offset) {
            throw new ArrayIndexOutOfBoundsException(Msg.getString("K0031", length)); 
        }
        for (int i = 0; i < length; i++) {
            write(buffer[offset + i]);
        }
    }
    @Override
    public void write(int oneByte) throws IOException {
        out.write(oneByte);
    }
}
