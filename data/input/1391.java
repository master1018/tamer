public abstract class HttpServer {
    protected HttpServer () {
    }
    public static HttpServer create () throws IOException {
        return create (null, 0);
    }
    public static HttpServer create (
        InetSocketAddress addr, int backlog
    ) throws IOException {
        HttpServerProvider provider = HttpServerProvider.provider();
        return provider.createHttpServer (addr, backlog);
    }
    public abstract void bind (InetSocketAddress addr, int backlog) throws IOException;
    public abstract void start () ;
    public abstract void setExecutor (Executor executor);
    public abstract Executor getExecutor () ;
    public abstract void stop (int delay);
    public abstract HttpContext createContext (String path, HttpHandler handler) ;
    public abstract HttpContext createContext (String path) ;
    public abstract void removeContext (String path) throws IllegalArgumentException ;
    public abstract void removeContext (HttpContext context) ;
    public abstract InetSocketAddress getAddress() ;
}
