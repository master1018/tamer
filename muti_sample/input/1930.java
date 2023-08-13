public class FileImageOutputStream extends ImageOutputStreamImpl {
    private RandomAccessFile raf;
    private final Object disposerReferent;
    private final CloseableDisposerRecord disposerRecord;
    public FileImageOutputStream(File f)
        throws FileNotFoundException, IOException {
        this(f == null ? null : new RandomAccessFile(f, "rw"));
    }
    public FileImageOutputStream(RandomAccessFile raf) {
        if (raf == null) {
            throw new IllegalArgumentException("raf == null!");
        }
        this.raf = raf;
        disposerRecord = new CloseableDisposerRecord(raf);
        if (getClass() == FileImageOutputStream.class) {
            disposerReferent = new Object();
            Disposer.addRecord(disposerReferent, disposerRecord);
        } else {
            disposerReferent = new StreamFinalizer(this);
        }
    }
    public int read() throws IOException {
        checkClosed();
        bitOffset = 0;
        int val = raf.read();
        if (val != -1) {
            ++streamPos;
        }
        return val;
    }
    public int read(byte[] b, int off, int len) throws IOException {
        checkClosed();
        bitOffset = 0;
        int nbytes = raf.read(b, off, len);
        if (nbytes != -1) {
            streamPos += nbytes;
        }
        return nbytes;
    }
    public void write(int b) throws IOException {
        flushBits(); 
        raf.write(b);
        ++streamPos;
    }
    public void write(byte[] b, int off, int len) throws IOException {
        flushBits(); 
        raf.write(b, off, len);
        streamPos += len;
    }
    public long length() {
        try {
            checkClosed();
            return raf.length();
        } catch (IOException e) {
            return -1L;
        }
    }
    public void seek(long pos) throws IOException {
        checkClosed();
        if (pos < flushedPos) {
            throw new IndexOutOfBoundsException("pos < flushedPos!");
        }
        bitOffset = 0;
        raf.seek(pos);
        streamPos = raf.getFilePointer();
    }
    public void close() throws IOException {
        super.close();
        disposerRecord.dispose(); 
        raf = null;
    }
    protected void finalize() throws Throwable {
    }
}
