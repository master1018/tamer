public class dnsURLContextFactory implements ObjectFactory {
    public Object getObjectInstance(Object urlInfo, Name name,
                                    Context nameCtx, Hashtable<?,?> env)
            throws NamingException {
        if (urlInfo == null) {
            return (new dnsURLContext(env));
        } else if (urlInfo instanceof String) {
            return getUsingURL((String) urlInfo, env);
        } else if (urlInfo instanceof String[]) {
            return getUsingURLs((String[]) urlInfo, env);
        } else {
            throw (new ConfigurationException(
                    "dnsURLContextFactory.getObjectInstance: " +
                    "argument must be a DNS URL String or an array of them"));
        }
    }
    private static Object getUsingURL(String url, Hashtable env)
            throws NamingException {
        dnsURLContext urlCtx = new dnsURLContext(env);
        try {
            return urlCtx.lookup(url);
        } finally {
            urlCtx.close();
        }
    }
    private static Object getUsingURLs(String[] urls, Hashtable env)
            throws NamingException {
        if (urls.length == 0) {
            throw (new ConfigurationException(
                    "dnsURLContextFactory: empty URL array"));
        }
        dnsURLContext urlCtx = new dnsURLContext(env);
        try {
            NamingException ne = null;
            for (int i = 0; i < urls.length; i++) {
                try {
                    return urlCtx.lookup(urls[i]);
                } catch (NamingException e) {
                    ne = e;
                }
            }
            throw ne;   
        } finally {
            urlCtx.close();
        }
    }
}
