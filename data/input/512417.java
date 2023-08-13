public class IISDecodingImageSource extends DecodingImageSource {
    private final InputStream is;
    public IISDecodingImageSource(ImageInputStream iis) {
        is = new IISToInputStreamWrapper(iis);
    }
    @Override
    protected boolean checkConnection() {
        return true;
    }
    @Override
    protected InputStream getInputStream() {
        return is;
    }
    static class IISToInputStreamWrapper extends InputStream {
        private ImageInputStream input;
        public IISToInputStreamWrapper(ImageInputStream input) {
            this.input=input;
        }
        @Override
        public int read() throws IOException {
            return input.read();
        }
        @Override
        public int read(byte[] b) throws IOException {
            return input.read(b);
        }
        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            return input.read(b, off, len);
        }
        @Override
        public long skip(long n) throws IOException {
            return input.skipBytes(n);
        }
        @Override
        public boolean markSupported() {
        	return true;  
        }
        @Override
        public void mark(int readlimit) {
            input.mark();
        }
        @Override
        public void reset() throws IOException {
            input.reset();
        }
        @Override
        public void close() throws IOException {
            input.close();
        }
    }
}
