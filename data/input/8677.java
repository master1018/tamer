public class rmiURLContext extends GenericURLContext {
    public rmiURLContext(Hashtable env) {
        super(env);
    }
    protected ResolveResult getRootURLContext(String url, Hashtable env)
            throws NamingException
    {
        if (!url.startsWith("rmi:")) {
            throw (new IllegalArgumentException(
                    "rmiURLContext: name is not an RMI URL: " + url));
        }
        String host = null;
        int port = -1;
        String objName = null;
        int i = 4;              
        if (url.startsWith("
            i += 2;                             
            int slash = url.indexOf('/', i);
            if (slash < 0) {
                slash = url.length();
            }
            if (url.startsWith("[", i)) {               
                int brac = url.indexOf(']', i + 1);
                if (brac < 0 || brac > slash) {
                    throw new IllegalArgumentException(
                        "rmiURLContext: name is an Invalid URL: " + url);
                }
                host = url.substring(i, brac + 1);      
                i = brac + 1;                           
            } else {                                    
                int colon = url.indexOf(':', i);
                int hostEnd = (colon < 0 || colon > slash)
                    ? slash
                    : colon;
                if (i < hostEnd) {
                    host = url.substring(i, hostEnd);
                }
                i = hostEnd;                            
            }
            if ((i + 1 < slash)) {
                if ( url.startsWith(":", i)) {       
                    i++;                             
                    port = Integer.parseInt(url.substring(i, slash));
                } else {
                    throw new IllegalArgumentException(
                        "rmiURLContext: name is an Invalid URL: " + url);
                }
            }
            i = slash;
        }
        if ("".equals(host)) {
            host = null;
        }
        if (url.startsWith("/", i)) {           
            i++;
        }
        if (i < url.length()) {
            objName = url.substring(i);
        }
        CompositeName remaining = new CompositeName();
        if (objName != null) {
            remaining.add(objName);
        }
        Context regCtx = new RegistryContext(host, port, env);
        return (new ResolveResult(regCtx, remaining));
    }
}
