class RootInputStream extends InputStream {
    private InputStream is = null;
    private int lineNumber = 1;
    private int prev = -1;
    private boolean truncated = false;
    public RootInputStream(InputStream is) {
        this.is = is;
    }
    public int getLineNumber() {
        return lineNumber;
    }
    public void truncate() {
        this.truncated = true;
    }
    public int read() throws IOException {
        if (truncated) {
            return -1;
        }
        int b = is.read();
        if (prev == '\r' && b == '\n') {
            lineNumber++;
        }
        prev = b;
        return b;
    }
    public int read(byte[] b, int off, int len) throws IOException {
        if (truncated) {
            return -1;
        }
        int n = is.read(b, off, len);
        for (int i = off; i < off + n; i++) {
            if (prev == '\r' && b[i] == '\n') {
                lineNumber++;
            }
            prev = b[i];
        }
        return n;
    }
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }
}
