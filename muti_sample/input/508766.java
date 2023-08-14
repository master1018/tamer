public class FileCacheImageInputStream extends ImageInputStreamImpl {
    private InputStream is;
    private File file;
    private RandomAccessFile raf;
    public FileCacheImageInputStream(InputStream stream, File cacheDir) throws IOException {
        if (stream == null) {
            throw new IllegalArgumentException("stream == null!");
        }
        is = stream;
        if (cacheDir == null || cacheDir.isDirectory()) {
            file = File.createTempFile(FileCacheImageOutputStream.IIO_TEMP_FILE_PREFIX, null,
                    cacheDir);
            file.deleteOnExit();
        } else {
            throw new IllegalArgumentException("Not a directory!");
        }
        raf = new RandomAccessFile(file, "rw");
    }
    @Override
    public int read() throws IOException {
        bitOffset = 0;
        if (streamPos >= raf.length()) {
            int b = is.read();
            if (b < 0) {
                return -1;
            }
            raf.seek(streamPos++);
            raf.write(b);
            return b;
        }
        raf.seek(streamPos++);
        return raf.read();
    }
    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        bitOffset = 0;
        if (streamPos >= raf.length()) {
            int nBytes = is.read(b, off, len);
            if (nBytes < 0) {
                return -1;
            }
            raf.seek(streamPos);
            raf.write(b, off, nBytes);
            streamPos += nBytes;
            return nBytes;
        }
        raf.seek(streamPos);
        int nBytes = raf.read(b, off, len);
        streamPos += nBytes;
        return nBytes;
    }
    @Override
    public boolean isCached() {
        return true;
    }
    @Override
    public boolean isCachedFile() {
        return true;
    }
    @Override
    public boolean isCachedMemory() {
        return false;
    }
    @Override
    public void close() throws IOException {
        super.close();
        raf.close();
        file.delete();
    }
}
