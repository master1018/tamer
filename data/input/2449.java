public abstract class HttpsServer extends HttpServer {
    protected HttpsServer () {
    }
    public static HttpsServer create () throws IOException {
        return create (null, 0);
    }
    public static HttpsServer create (
        InetSocketAddress addr, int backlog
    ) throws IOException {
        HttpServerProvider provider = HttpServerProvider.provider();
        return provider.createHttpsServer (addr, backlog);
    }
    public abstract void setHttpsConfigurator (HttpsConfigurator config) ;
    public abstract HttpsConfigurator getHttpsConfigurator ();
}
