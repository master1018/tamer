class RandomBlock {
    private static final String TAG = "RandomBlock";
    private static final boolean DEBUG = false;
    private static final int BLOCK_SIZE = 4096;
    private byte[] block = new byte[BLOCK_SIZE];
    private RandomBlock() { }
    static RandomBlock fromFile(String filename) throws IOException {
        if (DEBUG) Slog.v(TAG, "reading from file " + filename);
        InputStream stream = null;
        try {
            stream = new FileInputStream(filename);
            return fromStream(stream);
        } finally {
            close(stream);
        }
    }
    private static RandomBlock fromStream(InputStream in) throws IOException {
        RandomBlock retval = new RandomBlock();
        int total = 0;
        while(total < BLOCK_SIZE) {
            int result = in.read(retval.block, total, BLOCK_SIZE - total);
            if (result == -1) {
                throw new EOFException();
            }
            total += result;
        }
        return retval;
    }
    void toFile(String filename) throws IOException {
        if (DEBUG) Slog.v(TAG, "writing to file " + filename);
        RandomAccessFile out = null;
        try {
            out = new RandomAccessFile(filename, "rws");
            toDataOut(out);
            truncateIfPossible(out);
        } finally {
            close(out);
        }
    }
    private static void truncateIfPossible(RandomAccessFile f) {
        try {
            f.setLength(BLOCK_SIZE);
        } catch (IOException e) {
        }
    }
    private void toDataOut(DataOutput out) throws IOException {
        out.write(block);
    }
    private static void close(Closeable c) {
        try {
            if (c == null) {
                return;
            }
            c.close();
        } catch (IOException e) {
            Slog.w(TAG, "IOException thrown while closing Closeable", e);
        }
    }
}
