public class ThrowingReader extends FilterReader {
    private int total = 0;
    private int throwAt;
    public ThrowingReader(Reader in, int throwAt) {
        super(in);
        this.throwAt = throwAt;
    }
    @Override public int read() throws IOException {
        explodeIfNecessary();
        int result = super.read();
        total++;
        return result;
    }
    @Override public int read(char[] buf, int offset, int count)
            throws IOException {
        explodeIfNecessary();
        if (total < throwAt) {
            count = Math.min(count, (throwAt - total));
        }
        int returned = super.read(buf, offset, count);
        total += returned;
        return returned;
    }
    private void explodeIfNecessary() throws IOException {
        if (total == throwAt) {
            throwAt = Integer.MAX_VALUE;
            throw new IOException();
        }
    }
}
