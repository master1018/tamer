public class ByteArrayEntity extends AbstractHttpEntity implements Cloneable {
    protected final byte[] content;
    public ByteArrayEntity(final byte[] b) {
        super();        
        if (b == null) {
            throw new IllegalArgumentException("Source byte array may not be null");
        }
        this.content = b;
    }
    public boolean isRepeatable() {
        return true;
    }
    public long getContentLength() {
        return this.content.length;
    }
    public InputStream getContent() {
        return new ByteArrayInputStream(this.content);
    }
    public void writeTo(final OutputStream outstream) throws IOException {
        if (outstream == null) {
            throw new IllegalArgumentException("Output stream may not be null");
        }
        outstream.write(this.content);
        outstream.flush();
    }
    public boolean isStreaming() {
        return false;
    }
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
} 
