public class BufferedHttpEntity extends HttpEntityWrapper {
    private final byte[] buffer;
    public BufferedHttpEntity(final HttpEntity entity) throws IOException {
        super(entity);
        if (!entity.isRepeatable() || entity.getContentLength() < 0) {
            this.buffer = EntityUtils.toByteArray(entity);
        } else {
            this.buffer = null;
        }
    }
    public long getContentLength() {
        if (this.buffer != null) {
            return this.buffer.length;
        } else {
            return wrappedEntity.getContentLength();
        }
    }
    public InputStream getContent() throws IOException {
        if (this.buffer != null) {
            return new ByteArrayInputStream(this.buffer);
        } else {
            return wrappedEntity.getContent();
        }
    }
    public boolean isChunked() {
        return (buffer == null) && wrappedEntity.isChunked();
    }
    public boolean isRepeatable() {
        return true;
    }
    public void writeTo(final OutputStream outstream) throws IOException {
        if (outstream == null) {
            throw new IllegalArgumentException("Output stream may not be null");
        }
        if (this.buffer != null) {
            outstream.write(this.buffer);
        } else {
            wrappedEntity.writeTo(outstream);
        }
    }
    public boolean isStreaming() {
        return (buffer == null) && wrappedEntity.isStreaming();
    }
} 
