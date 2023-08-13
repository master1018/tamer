class LogOutputStream extends OutputStream {
    private RandomAccessFile raf;
    public LogOutputStream(RandomAccessFile raf) throws IOException {
        this.raf = raf;
    }
    public void write(int b) throws IOException {
        raf.write(b);
    }
    public void write(byte b[]) throws IOException {
        raf.write(b);
    }
    public void write(byte b[], int off, int len) throws IOException {
        raf.write(b, off, len);
    }
    public final void close() throws IOException {
    }
}
