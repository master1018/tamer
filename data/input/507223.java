public class FileImageInputStream extends ImageInputStreamImpl {
    RandomAccessFile raf;
    @SuppressWarnings( {
        "DuplicateThrows"
    })
    public FileImageInputStream(File f) throws FileNotFoundException, IOException {
        if (f == null) {
            throw new IllegalArgumentException("f == null!");
        }
        raf = new RandomAccessFile(f, "r");
    }
    public FileImageInputStream(RandomAccessFile raf) {
        if (raf == null) {
            throw new IllegalArgumentException("raf == null!");
        }
        this.raf = raf;
    }
    @Override
    public int read() throws IOException {
        bitOffset = 0;
        int res = raf.read();
        if (res != -1) {
            streamPos++;
        }
        return res;
    }
    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        bitOffset = 0;
        int numRead = raf.read(b, off, len);
        if (numRead >= 0) {
            streamPos += numRead;
        }
        return numRead;
    }
    @Override
    public long length() {
        try {
            return raf.length();
        } catch (IOException e) {
            return -1L;
        }
    }
    @Override
    public void seek(long pos) throws IOException {
        if (pos < getFlushedPosition()) {
            throw new IndexOutOfBoundsException();
        }
        raf.seek(pos);
        streamPos = raf.getFilePointer();
        bitOffset = 0;
    }
    @Override
    public void close() throws IOException {
        super.close();
        raf.close();
    }
}
