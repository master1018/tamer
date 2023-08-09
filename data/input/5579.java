final public class HttpCallerInfo {
    final public URL url;
    final public String host, protocol, prompt, scheme;
    final public int port;
    final public InetAddress addr;
    final public RequestorType authType;
    public HttpCallerInfo(HttpCallerInfo old, String scheme) {
        this.url = old.url;
        this.host = old.host;
        this.protocol = old.protocol;
        this.prompt = old.prompt;
        this.port = old.port;
        this.addr = old.addr;
        this.authType = old.authType;
        this.scheme = scheme;
    }
    public HttpCallerInfo(URL url) {
        this.url= url;
        prompt = "";
        host = url.getHost();
        int p = url.getPort();
        if (p == -1) {
            port = url.getDefaultPort();
        } else {
            port = p;
        }
        InetAddress ia;
        try {
            ia = InetAddress.getByName(url.getHost());
        } catch (Exception e) {
            ia = null;
        }
        addr = ia;
        protocol = url.getProtocol();
        authType = RequestorType.SERVER;
        scheme = "";
    }
    public HttpCallerInfo(URL url, String host, int port) {
        this.url= url;
        this.host = host;
        this.port = port;
        prompt = "";
        addr = null;
        protocol = url.getProtocol();
        authType = RequestorType.PROXY;
        scheme = "";
    }
}
