class HttpContextImpl extends HttpContext {
    private String path;
    private String protocol;
    private HttpHandler handler;
    private Map<String,Object> attributes = new HashMap<String,Object>();
    private ServerImpl server;
    private LinkedList<Filter> sfilters = new LinkedList<Filter>();
    private LinkedList<Filter> ufilters = new LinkedList<Filter>();
    private Authenticator authenticator;
    private AuthFilter authfilter;
    HttpContextImpl (String protocol, String path, HttpHandler cb, ServerImpl server) {
        if (path == null || protocol == null || path.length() < 1 || path.charAt(0) != '/') {
            throw new IllegalArgumentException ("Illegal value for path or protocol");
        }
        this.protocol = protocol.toLowerCase();
        this.path = path;
        if (!this.protocol.equals ("http") && !this.protocol.equals ("https")) {
            throw new IllegalArgumentException ("Illegal value for protocol");
        }
        this.handler = cb;
        this.server = server;
        authfilter = new AuthFilter(null);
        sfilters.add (authfilter);
    }
    public HttpHandler getHandler () {
        return handler;
    }
    public void setHandler (HttpHandler h) {
        if (h == null) {
            throw new NullPointerException ("Null handler parameter");
        }
        if (handler != null) {
            throw new IllegalArgumentException ("handler already set");
        }
        handler = h;
    }
    public String getPath() {
        return path;
    }
    public HttpServer getServer () {
        return server.getWrapper();
    }
    ServerImpl getServerImpl () {
        return server;
    }
    public String getProtocol() {
        return protocol;
    }
    public Map<String,Object> getAttributes() {
        return attributes;
    }
    public List<Filter> getFilters () {
        return ufilters;
    }
    List<Filter> getSystemFilters () {
        return sfilters;
    }
    public Authenticator setAuthenticator (Authenticator auth) {
        Authenticator old = authenticator;
        authenticator = auth;
        authfilter.setAuthenticator (auth);
        return old;
    }
    public Authenticator getAuthenticator () {
        return authenticator;
    }
    Logger getLogger () {
        return server.getLogger();
    }
}
