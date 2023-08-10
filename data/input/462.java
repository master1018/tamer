public class HttpClientURLStreamHandlerFactory implements URLStreamHandlerFactory, InitializingBean {
    private HttpClient httpClient;
    private HttpClientURLStreamHandler streamHandler;
    public void setHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }
    @Override
    public void afterPropertiesSet() {
        Assert.notNull(this.httpClient, "property 'httpClient' is required");
        this.streamHandler = new HttpClientURLStreamHandler(this.httpClient);
        URL.setURLStreamHandlerFactory(this);
    }
    @Override
    public URLStreamHandler createURLStreamHandler(String protocol) {
        if ("http".equals(protocol) || "https".equals(protocol)) {
            return this.streamHandler;
        }
        return null;
    }
}
