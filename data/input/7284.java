public abstract class HttpContext {
    protected HttpContext () {
    }
    public abstract HttpHandler getHandler () ;
    public abstract void setHandler (HttpHandler h) ;
    public abstract String getPath() ;
    public abstract HttpServer getServer () ;
    public abstract Map<String,Object> getAttributes() ;
    public abstract List<Filter> getFilters();
    public abstract Authenticator setAuthenticator (Authenticator auth);
    public abstract Authenticator getAuthenticator ();
}
