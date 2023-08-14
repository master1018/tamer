public abstract class AuthenticationInfo extends AuthCacheValue implements Cloneable {
    public static final char SERVER_AUTHENTICATION = 's';
    public static final char PROXY_AUTHENTICATION = 'p';
    static boolean serializeAuth;
    static {
        serializeAuth = java.security.AccessController.doPrivileged(
            new sun.security.action.GetBooleanAction(
                "http.auth.serializeRequests")).booleanValue();
    }
    transient protected PasswordAuthentication pw;
    public PasswordAuthentication credentials() {
        return pw;
    }
    public AuthCacheValue.Type getAuthType() {
        return type == SERVER_AUTHENTICATION ?
            AuthCacheValue.Type.Server:
            AuthCacheValue.Type.Proxy;
    }
    AuthScheme getAuthScheme() {
        return authScheme;
    }
    public String getHost() {
        return host;
    }
    public int getPort() {
        return port;
    }
    public String getRealm() {
        return realm;
    }
    public String getPath() {
        return path;
    }
    public String getProtocolScheme() {
        return protocol;
    }
    static private HashMap<String,Thread> requests = new HashMap<>();
    static private boolean requestIsInProgress (String key) {
        if (!serializeAuth) {
            return false;
        }
        synchronized (requests) {
            Thread t, c;
            c = Thread.currentThread();
            if ((t = requests.get(key)) == null) {
                requests.put (key, c);
                return false;
            }
            if (t == c) {
                return false;
            }
            while (requests.containsKey(key)) {
                try {
                    requests.wait ();
                } catch (InterruptedException e) {}
            }
        }
        return true;
    }
    static private void requestCompleted (String key) {
        synchronized (requests) {
            Thread thread = requests.get(key);
            if (thread != null && thread == Thread.currentThread()) {
                boolean waspresent = requests.remove(key) != null;
                assert waspresent;
            }
            requests.notifyAll();
        }
    }
    char type;
    AuthScheme authScheme;
    String protocol;
    String host;
    int port;
    String realm;
    String path;
    public AuthenticationInfo(char type, AuthScheme authScheme, String host, int port, String realm) {
        this.type = type;
        this.authScheme = authScheme;
        this.protocol = "";
        this.host = host.toLowerCase();
        this.port = port;
        this.realm = realm;
        this.path = null;
    }
    public Object clone() {
        try {
            return super.clone ();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }
    public AuthenticationInfo(char type, AuthScheme authScheme, URL url, String realm) {
        this.type = type;
        this.authScheme = authScheme;
        this.protocol = url.getProtocol().toLowerCase();
        this.host = url.getHost().toLowerCase();
        this.port = url.getPort();
        if (this.port == -1) {
            this.port = url.getDefaultPort();
        }
        this.realm = realm;
        String urlPath = url.getPath();
        if (urlPath.length() == 0)
            this.path = urlPath;
        else {
            this.path = reducePath (urlPath);
        }
    }
    static String reducePath (String urlPath) {
        int sepIndex = urlPath.lastIndexOf('/');
        int targetSuffixIndex = urlPath.lastIndexOf('.');
        if (sepIndex != -1)
            if (sepIndex < targetSuffixIndex)
                return urlPath.substring(0, sepIndex+1);
            else
                return urlPath;
        else
            return urlPath;
    }
    static AuthenticationInfo getServerAuth(URL url) {
        int port = url.getPort();
        if (port == -1) {
            port = url.getDefaultPort();
        }
        String key = SERVER_AUTHENTICATION + ":" + url.getProtocol().toLowerCase()
                + ":" + url.getHost().toLowerCase() + ":" + port;
        return getAuth(key, url);
    }
    static String getServerAuthKey(URL url, String realm, AuthScheme scheme) {
        int port = url.getPort();
        if (port == -1) {
            port = url.getDefaultPort();
        }
        String key = SERVER_AUTHENTICATION + ":" + scheme + ":" + url.getProtocol().toLowerCase()
                     + ":" + url.getHost().toLowerCase() + ":" + port + ":" + realm;
        return key;
    }
    static AuthenticationInfo getServerAuth(String key) {
        AuthenticationInfo cached = getAuth(key, null);
        if ((cached == null) && requestIsInProgress (key)) {
            cached = getAuth(key, null);
        }
        return cached;
    }
    static AuthenticationInfo getAuth(String key, URL url) {
        if (url == null) {
            return (AuthenticationInfo)cache.get (key, null);
        } else {
            return (AuthenticationInfo)cache.get (key, url.getPath());
        }
    }
    static AuthenticationInfo getProxyAuth(String host, int port) {
        String key = PROXY_AUTHENTICATION + "::" + host.toLowerCase() + ":" + port;
        AuthenticationInfo result = (AuthenticationInfo) cache.get(key, null);
        return result;
    }
    static String getProxyAuthKey(String host, int port, String realm, AuthScheme scheme) {
        String key = PROXY_AUTHENTICATION + ":" + scheme + "::" + host.toLowerCase()
                        + ":" + port + ":" + realm;
        return key;
    }
    static AuthenticationInfo getProxyAuth(String key) {
        AuthenticationInfo cached = (AuthenticationInfo) cache.get(key, null);
        if ((cached == null) && requestIsInProgress (key)) {
            cached = (AuthenticationInfo) cache.get(key, null);
        }
        return cached;
    }
    void addToCache() {
        String key = cacheKey(true);
        cache.put(key, this);
        if (supportsPreemptiveAuthorization()) {
            cache.put(cacheKey(false), this);
        }
        endAuthRequest(key);
    }
    static void endAuthRequest (String key) {
        if (!serializeAuth) {
            return;
        }
        synchronized (requests) {
            requestCompleted(key);
        }
    }
    void removeFromCache() {
        cache.remove(cacheKey(true), this);
        if (supportsPreemptiveAuthorization()) {
            cache.remove(cacheKey(false), this);
        }
    }
    public abstract boolean supportsPreemptiveAuthorization();
    public String getHeaderName() {
        if (type == SERVER_AUTHENTICATION) {
            return "Authorization";
        } else {
            return "Proxy-authorization";
        }
    }
    public abstract String getHeaderValue(URL url, String method);
    public abstract boolean setHeaders(HttpURLConnection conn, HeaderParser p, String raw);
    public abstract boolean isAuthorizationStale (String header);
    String cacheKey(boolean includeRealm) {
        if (includeRealm) {
            return type + ":" + authScheme + ":" + protocol + ":"
                        + host + ":" + port + ":" + realm;
        } else {
            return type + ":" + protocol + ":" + host + ":" + port;
        }
    }
    String s1, s2;  
    private void readObject(ObjectInputStream s)
        throws IOException, ClassNotFoundException
    {
        s.defaultReadObject ();
        pw = new PasswordAuthentication (s1, s2.toCharArray());
        s1 = null; s2= null;
    }
    private synchronized void writeObject(java.io.ObjectOutputStream s)
        throws IOException
    {
        s1 = pw.getUserName();
        s2 = new String (pw.getPassword());
        s.defaultWriteObject ();
    }
}
