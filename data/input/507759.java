public class PositionedInputStream extends FilterInputStream {
    private int currentPosition; 
    public PositionedInputStream(InputStream in) {
        super(in);
    }
    public int currentPosition() {
        return currentPosition;
    }
    @Override
    public int read() throws IOException {
        int read = in.read();
        if (read >= 0) {
            currentPosition++;
        }
        return read;
    }
    @Override
    public int read(byte b[], int off, int len) throws IOException {
        int read = in.read(b, off, len);
        if (read >= 0) {
            currentPosition += read;
        }
        return read;
    }
    public void resetCurrentPosition() {
        currentPosition = 0;
    }
    @Override
    public long skip(long n) throws IOException {
        long skip = in.skip(n);
        currentPosition += skip; 
        return skip;
    }
}
