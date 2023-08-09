public class MemoryCacheImageInputStream extends ImageInputStreamImpl {
    private InputStream is;
    private RandomAccessMemoryCache ramc = new RandomAccessMemoryCache();
    public MemoryCacheImageInputStream(InputStream stream) {
        if (stream == null) {
            throw new IllegalArgumentException("stream == null!");
        }
        is = stream;
    }
    @Override
    public int read() throws IOException {
        bitOffset = 0;
        if (streamPos >= ramc.length()) {
            int count = (int)(streamPos - ramc.length() + 1);
            int bytesAppended = ramc.appendData(is, count);
            if (bytesAppended < count) {
                return -1;
            }
        }
        int res = ramc.getData(streamPos);
        if (res >= 0) {
            streamPos++;
        }
        return res;
    }
    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        bitOffset = 0;
        if (streamPos >= ramc.length()) {
            int count = (int)(streamPos - ramc.length() + len);
            ramc.appendData(is, count);
        }
        int res = ramc.getData(b, off, len, streamPos);
        if (res > 0) {
            streamPos += res;
        }
        return res;
    }
    @Override
    public boolean isCached() {
        return true;
    }
    @Override
    public boolean isCachedFile() {
        return false;
    }
    @Override
    public boolean isCachedMemory() {
        return true;
    }
    @Override
    public void close() throws IOException {
        super.close();
        ramc.close();
    }
    @Override
    public void flushBefore(long pos) throws IOException {
        super.flushBefore(pos);
        ramc.freeBefore(getFlushedPosition());
    }
}
