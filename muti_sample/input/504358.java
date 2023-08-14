public class BasicHttpEntity extends AbstractHttpEntity {
    private InputStream content;
    private boolean contentObtained;
    private long length;
    public BasicHttpEntity() {
        super();
        this.length = -1;
    }
    public long getContentLength() {
        return this.length;
    }
    public InputStream getContent()
        throws IllegalStateException {
        if (this.content == null) {
            throw new IllegalStateException("Content has not been provided");
        }
        if (this.contentObtained) {
            throw new IllegalStateException("Content has been consumed");
        }
        this.contentObtained = true;
        return this.content;
    } 
    public boolean isRepeatable() {
        return false;
    }
    public void setContentLength(long len) {
        this.length = len;
    }
    public void setContent(final InputStream instream) {
        this.content = instream;
        this.contentObtained = false; 
    }
    public void writeTo(final OutputStream outstream) throws IOException {
        if (outstream == null) {
            throw new IllegalArgumentException("Output stream may not be null");
        }
        InputStream instream = getContent();
        int l;
        byte[] tmp = new byte[2048];
        while ((l = instream.read(tmp)) != -1) {
            outstream.write(tmp, 0, l);
        }
    }
    public boolean isStreaming() {
        return !this.contentObtained && this.content != null;
    }
    public void consumeContent() throws IOException {
        if (content != null) {
            content.close(); 
        }
    }
} 
