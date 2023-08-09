public class HttpConfiguration {
    private Proxy proxy;
    private int hostPort;
    private String hostName;
    private URI uri;
    public HttpConfiguration(URI uri) {
        this.uri = uri;
        this.hostName = uri.getHost();
        this.hostPort = uri.getPort();
        if(hostPort == -1) {
            if(uri.getScheme().equals("https")) { 
                hostPort = 443;
            } else {
                hostPort = 80;
            }
        }
    }
    public HttpConfiguration(URI uri, Proxy proxy) {
        this.uri = uri;
        this.proxy = proxy;
        if (proxy.type() == Proxy.Type.HTTP) {
            SocketAddress proxyAddr = proxy.address();
            if (!(proxyAddr instanceof InetSocketAddress)) {
               throw new IllegalArgumentException(Msg.getString(
                   "K0316", proxyAddr.getClass())); 
            }
            InetSocketAddress iProxyAddr = (InetSocketAddress) proxyAddr;
            this.hostName = iProxyAddr.getHostName();
            this.hostPort = iProxyAddr.getPort();
        } else {
            this.hostName = uri.getHost();
            this.hostPort = uri.getPort();
            if(hostPort == -1) {
                if(uri.getScheme().equals("https")) { 
                    hostPort = 443;
                } else {
                    hostPort = 80;
                }
            }
        }
        this.uri = uri;
        SocketAddress proxyAddr = proxy.address();
        if (!(proxyAddr instanceof InetSocketAddress)) {
           throw new IllegalArgumentException(Msg.getString(
               "K0316", proxyAddr.getClass())); 
        }
        InetSocketAddress iProxyAddr = (InetSocketAddress) proxyAddr;
        this.hostName = iProxyAddr.getHostName();
        this.hostPort = iProxyAddr.getPort();
    }
    public boolean usesProxy() {
        return proxy != null;
    }
    public Proxy getProxy() {
        return proxy;
    }
    public String getHostName() {
        return hostName;
    }
    public int getHostPort() {
        return hostPort;
    }
    @Override
    public boolean equals(Object arg0) {
        if(!(arg0 instanceof HttpConfiguration)) {
            return false;
        } else {
            HttpConfiguration config = (HttpConfiguration)arg0;
            if(config.proxy != null && proxy != null) {
                return config.proxy.equals(proxy) && uri.equals(config.uri);
            }
            return uri.equals(config.uri);
        }
    }
    @Override
    public int hashCode() {
        return uri.hashCode();
    }
}