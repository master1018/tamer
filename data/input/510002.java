class FtpURLInputStream extends InputStream {
    private InputStream is; 
    private Socket controlSocket;
    public FtpURLInputStream(InputStream is, Socket controlSocket) {
        this.is = is;
        this.controlSocket = controlSocket;
    }
    @Override
    public int read() throws IOException {
        return is.read();
    }
    @Override
    public int read(byte[] buf, int off, int nbytes) throws IOException {
        return is.read(buf, off, nbytes);
    }
    @Override
    public synchronized void reset() throws IOException {
        is.reset();
    }
    @Override
    public synchronized void mark(int limit) {
        is.mark(limit);
    }
    @Override
    public boolean markSupported() {
        return is.markSupported();
    }
    @Override
    public void close() {
        try {
            is.close();
        } catch (Exception e) {
        }
        try {
            controlSocket.close();
        } catch (Exception e) {
        }
    }
    @Override
    public int available() throws IOException {
        return is.available();
    }
    @Override
    public long skip(long sbytes) throws IOException {
        return is.skip(sbytes);
    }
}
