public class InputStreamEntity extends AbstractHttpEntity {
    private final static int BUFFER_SIZE = 2048;
    private final InputStream content;
    private final long length;
    private boolean consumed = false;
    public InputStreamEntity(final InputStream instream, long length) {
        super();        
        if (instream == null) {
            throw new IllegalArgumentException("Source input stream may not be null");
        }
        this.content = instream;
        this.length = length;
    }
    public boolean isRepeatable() {
        return false;
    }
    public long getContentLength() {
        return this.length;
    }
    public InputStream getContent() throws IOException {
        return this.content;
    }
    public void writeTo(final OutputStream outstream) throws IOException {
        if (outstream == null) {
            throw new IllegalArgumentException("Output stream may not be null");
        }
        InputStream instream = this.content;
        byte[] buffer = new byte[BUFFER_SIZE];
        int l;
        if (this.length < 0) {
            while ((l = instream.read(buffer)) != -1) {
                outstream.write(buffer, 0, l);
            }
        } else {
            long remaining = this.length;
            while (remaining > 0) {
                l = instream.read(buffer, 0, (int)Math.min(BUFFER_SIZE, remaining));
                if (l == -1) {
                    break;
                }
                outstream.write(buffer, 0, l);
                remaining -= l;
            }
        }
        this.consumed = true;
    }
    public boolean isStreaming() {
        return !this.consumed;
    }
    public void consumeContent() throws IOException {
        this.consumed = true;
        this.content.close();
    }
} 
