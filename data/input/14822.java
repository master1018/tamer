public class ProxyService extends Object {
    private static ProxyServiceProvider provider = null;
    public static void setProvider(ProxyServiceProvider p)
    throws IOException {
        if(null == provider)
            provider = p;
        else
            throw new IOException("Proxy service provider has already been set.");
    }
    public static ProxyInfo[] getProxyInfo(URL url)
    throws IOException {
        if(null == provider)
            throw new IOException("Proxy service provider is not yet set");
        return provider.getProxyInfo(url);
    }
}
