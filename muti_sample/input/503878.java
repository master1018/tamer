public class FileImageOutputStream extends ImageOutputStreamImpl {
    RandomAccessFile file;
    public FileImageOutputStream(File f) throws FileNotFoundException, IOException {
        this(f != null ? new RandomAccessFile(f, "rw") : null);
    }
    public FileImageOutputStream(RandomAccessFile raf) {
        if (raf == null) {
            throw new IllegalArgumentException("file should not be NULL");
        }
        file = raf;
    }
    @Override
    public void write(int b) throws IOException {
        checkClosed();
        flushBits();
        file.write(b);
        streamPos++;
    }
    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        checkClosed();
        flushBits();
        file.write(b, off, len);
        streamPos += len;
    }
    @Override
    public int read() throws IOException {
        checkClosed();
        int rt = file.read();
        if (rt != -1) {
            streamPos++;
        }
        return rt;
    }
    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        checkClosed();
        int rt = file.read(b, off, len);
        if (rt != -1) {
            streamPos += rt;
        }
        return rt;
    }
    @Override
    public long length() {
        try {
            checkClosed();
            return file.length();
        } catch (IOException e) {
            return super.length(); 
        }
    }
    @Override
    public void seek(long pos) throws IOException {
        super.seek(pos);
        file.seek(pos);
        streamPos = file.getFilePointer();
    }
    @Override
    public void close() throws IOException {
        super.close();
        file.close();
    }
}
