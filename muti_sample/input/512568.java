public abstract class FilterWriter extends Writer {
    protected Writer out;
    protected FilterWriter(Writer out) {
        super(out);
        this.out = out;
    }
    @Override
    public void close() throws IOException {
        synchronized (lock) {
            out.close();
        }
    }
    @Override
    public void flush() throws IOException {
        synchronized (lock) {
            out.flush();
        }
    }
    @Override
    public void write(char[] buffer, int offset, int count) throws IOException {
        synchronized (lock) {
            out.write(buffer, offset, count);
        }
    }
    @Override
    public void write(int oneChar) throws IOException {
        synchronized (lock) {
            out.write(oneChar);
        }
    }
    @Override
    public void write(String str, int offset, int count) throws IOException {
        synchronized (lock) {
            out.write(str, offset, count);
        }
    }
}
