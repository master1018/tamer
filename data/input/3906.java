public abstract class HttpExchange {
    protected HttpExchange () {
    }
    public abstract Headers getRequestHeaders () ;
    public abstract Headers getResponseHeaders () ;
    public abstract URI getRequestURI () ;
    public abstract String getRequestMethod ();
    public abstract HttpContext getHttpContext ();
    public abstract void close () ;
    public abstract InputStream getRequestBody () ;
    public abstract OutputStream getResponseBody () ;
    public abstract void sendResponseHeaders (int rCode, long responseLength) throws IOException ;
    public abstract InetSocketAddress getRemoteAddress ();
    public abstract int getResponseCode ();
    public abstract InetSocketAddress getLocalAddress ();
    public abstract String getProtocol ();
    public abstract Object getAttribute (String name) ;
    public abstract void setAttribute (String name, Object value) ;
    public abstract void setStreams (InputStream i, OutputStream o);
    public abstract HttpPrincipal getPrincipal ();
}
