public class BrokenInputStream extends InputStream {
    private InputStream stream;
    private int offset;
    public BrokenInputStream(InputStream stream, int offset) {
        super();
        this.stream = stream;
        this.offset = offset;
    }
    @Override
    public int read() throws IOException {
        if (offset == 0) {
            throw new IOException("Injected exception");
        }
        offset--;
        return stream.read();
    }
}