public class ProxySelectorRoutePlanner implements HttpRoutePlanner {
    protected SchemeRegistry schemeRegistry;
    protected ProxySelector proxySelector;
    public ProxySelectorRoutePlanner(SchemeRegistry schreg,
                                     ProxySelector prosel) {
        if (schreg == null) {
            throw new IllegalArgumentException
                ("SchemeRegistry must not be null.");
        }
        schemeRegistry = schreg;
        proxySelector  = prosel;
    }
    public ProxySelector getProxySelector() {
        return this.proxySelector;
    }
    public void setProxySelector(ProxySelector prosel) {
        this.proxySelector = prosel;
    }
    public HttpRoute determineRoute(HttpHost target,
                                    HttpRequest request,
                                    HttpContext context)
        throws HttpException {
        if (request == null) {
            throw new IllegalStateException
                ("Request must not be null.");
        }
        HttpRoute route =
            ConnRouteParams.getForcedRoute(request.getParams());
        if (route != null)
            return route;
        if (target == null) {
            throw new IllegalStateException
                ("Target host must not be null.");
        }
        final InetAddress local =
            ConnRouteParams.getLocalAddress(request.getParams());
        final HttpHost proxy = determineProxy(target, request, context);
        final Scheme schm =
            this.schemeRegistry.getScheme(target.getSchemeName());
        final boolean secure = schm.isLayered();
        if (proxy == null) {
            route = new HttpRoute(target, local, secure);
        } else {
            route = new HttpRoute(target, local, proxy, secure);
        }
        return route;
    }
    protected HttpHost determineProxy(HttpHost    target,
                                      HttpRequest request,
                                      HttpContext context)
        throws HttpException {
        ProxySelector psel = this.proxySelector;
        if (psel == null)
            psel = ProxySelector.getDefault();
        if (psel == null)
            return null;
        URI targetURI = null;
        try {
            targetURI = new URI(target.toURI());
        } catch (URISyntaxException usx) {
            throw new HttpException
                ("Cannot convert host to URI: " + target, usx);
        }
        List<Proxy> proxies = psel.select(targetURI);
        Proxy p = chooseProxy(proxies, target, request, context);
        HttpHost result = null;
        if (p.type() == Proxy.Type.HTTP) {
            if (!(p.address() instanceof InetSocketAddress)) {
                throw new HttpException
                    ("Unable to handle non-Inet proxy address: "+p.address());
            }
            final InetSocketAddress isa = (InetSocketAddress) p.address();
            result = new HttpHost(getHost(isa), isa.getPort());
        }
        return result;
    }
    protected String getHost(InetSocketAddress isa) {
       return isa.isUnresolved() ?
            isa.getHostName() : isa.getAddress().getHostAddress();
    }
    protected Proxy chooseProxy(List<Proxy> proxies,
                                HttpHost    target,
                                HttpRequest request,
                                HttpContext context) {
        if ((proxies == null) || proxies.isEmpty()) {
            throw new IllegalArgumentException
                ("Proxy list must not be empty.");
        }
        Proxy result = null;
        for (int i=0; (result == null) && (i < proxies.size()); i++) {
            Proxy p = proxies.get(i);
            switch (p.type()) {
            case DIRECT:
            case HTTP:
                result = p;
                break;
            case SOCKS:
                break;
            }
        }
        if (result == null) {
            result = Proxy.NO_PROXY;
        }
        return result;
    }
} 
