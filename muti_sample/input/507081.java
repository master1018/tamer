public abstract class CacheResponse {
    public CacheResponse() {
        super();
    }
    public abstract InputStream getBody() throws IOException;
    public abstract Map<String, List<String>> getHeaders() throws IOException;
}
