public class Uri {
    protected String uri;
    protected String scheme;
    protected String host = null;
    protected int port = -1;
    protected boolean hasAuthority;
    protected String path;
    protected String query = null;
    public Uri(String uri) throws MalformedURLException {
        init(uri);
    }
    protected Uri() {
    }
    protected void init(String uri) throws MalformedURLException {
        this.uri = uri;
        parse(uri);
    }
    public String getScheme() {
        return scheme;
    }
    public String getHost() {
        return host;
    }
    public int getPort() {
        return port;
    }
    public String getPath() {
        return path;
    }
    public String getQuery() {
        return query;
    }
    public String toString() {
        return uri;
    }
    private void parse(String uri) throws MalformedURLException {
        int i;  
        i = uri.indexOf(':');                           
        if (i < 0) {
            throw new MalformedURLException("Invalid URI: " + uri);
        }
        scheme = uri.substring(0, i);
        i++;                                            
        hasAuthority = uri.startsWith("
        if (hasAuthority) {                             
            i += 2;                                     
            int slash = uri.indexOf('/', i);
            if (slash < 0) {
                slash = uri.length();
            }
            if (uri.startsWith("[", i)) {               
                int brac = uri.indexOf(']', i + 1);
                if (brac < 0 || brac > slash) {
                    throw new MalformedURLException("Invalid URI: " + uri);
                }
                host = uri.substring(i, brac + 1);      
                i = brac + 1;                           
            } else {                                    
                int colon = uri.indexOf(':', i);
                int hostEnd = (colon < 0 || colon > slash)
                    ? slash
                    : colon;
                if (i < hostEnd) {
                    host = uri.substring(i, hostEnd);
                }
                i = hostEnd;                            
            }
            if ((i + 1 < slash) &&
                        uri.startsWith(":", i)) {       
                i++;                                    
                port = Integer.parseInt(uri.substring(i, slash));
            }
            i = slash;                                  
        }
        int qmark = uri.indexOf('?', i);                
        if (qmark < 0) {
            path = uri.substring(i);
        } else {
            path = uri.substring(i, qmark);
            query = uri.substring(qmark);
        }
    }
}
