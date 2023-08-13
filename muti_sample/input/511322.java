public abstract class CacheRequest {
    public CacheRequest() {
        super();
    }
    public abstract void abort();
    public abstract OutputStream getBody() throws IOException;
}
