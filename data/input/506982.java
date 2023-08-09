public class HttpEntityWrapper implements HttpEntity {
    protected HttpEntity wrappedEntity;
    public HttpEntityWrapper(HttpEntity wrapped) {
        super();
        if (wrapped == null) {
            throw new IllegalArgumentException
                ("wrapped entity must not be null");
        }
        wrappedEntity = wrapped;
    } 
    public boolean isRepeatable() {
        return wrappedEntity.isRepeatable();
    }
    public boolean isChunked() {
        return wrappedEntity.isChunked();
    }
    public long getContentLength() {
        return wrappedEntity.getContentLength();
    }
    public Header getContentType() {
        return wrappedEntity.getContentType();
    }
    public Header getContentEncoding() {
        return wrappedEntity.getContentEncoding();
    }
    public InputStream getContent()
        throws IOException {
        return wrappedEntity.getContent();
    }
    public void writeTo(OutputStream outstream)
        throws IOException {
        wrappedEntity.writeTo(outstream);
    }
    public boolean isStreaming() {
        return wrappedEntity.isStreaming();
    }
    public void consumeContent()
        throws IOException {
        wrappedEntity.consumeContent();
    }
} 
