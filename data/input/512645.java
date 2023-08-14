public class MemoryCacheImageOutputStream extends ImageOutputStreamImpl {
    OutputStream os;
    RandomAccessMemoryCache ramc = new RandomAccessMemoryCache();
    public MemoryCacheImageOutputStream(OutputStream stream) {
        if (stream == null) {
            throw new IllegalArgumentException("stream == null!");
        }
        os = stream;
    }
    @Override
    public void write(int b) throws IOException {
        flushBits(); 
        ramc.putData(b, streamPos);
        streamPos++;
    }
    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        flushBits(); 
        ramc.putData(b, off, len, streamPos);
        streamPos += len;
    }
    @Override
    public int read() throws IOException {
        bitOffset = 0;
        int res = ramc.getData(streamPos);
        if (res >= 0) {
            streamPos++;
        }
        return res;
    }
    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        bitOffset = 0;
        int res = ramc.getData(b, off, len, streamPos);
        if (res > 0) {
            streamPos += res;
        }
        return res;
    }
    @Override
    public long length() {
        return ramc.length();
    }
    @Override
    public boolean isCached() {
        return true;
    }
    @Override
    public boolean isCachedMemory() {
        return true;
    }
    @Override
    public boolean isCachedFile() {
        return false;
    }
    @Override
    public void close() throws IOException {
        flushBefore(length());
        super.close();
        ramc.close();
    }
    @Override
    public void flushBefore(long pos) throws IOException {
        long flushedPosition = getFlushedPosition();
        super.flushBefore(pos);
        long newFlushedPosition = getFlushedPosition();
        int nBytes = (int)(newFlushedPosition - flushedPosition);
        ramc.getData(os, nBytes, flushedPosition);
        ramc.freeBefore(newFlushedPosition);
        os.flush();
    }
}
