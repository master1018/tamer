public class iiopURLContextFactory implements ObjectFactory {
    public Object getObjectInstance(Object urlInfo, Name name, Context nameCtx,
                                    Hashtable<?,?> env) throws Exception {
        if (urlInfo == null) {
            return new iiopURLContext(env);
        }
        if (urlInfo instanceof String) {
            return getUsingURL((String)urlInfo, env);
        } else if (urlInfo instanceof String[]) {
            return getUsingURLs((String[])urlInfo, env);
        } else {
            throw (new IllegalArgumentException(
                    "iiopURLContextFactory.getObjectInstance: " +
                    "argument must be a URL String or array of URLs"));
        }
    }
    static ResolveResult getUsingURLIgnoreRest(String url, Hashtable env)
        throws NamingException {
        return CNCtx.createUsingURL(url, env);
    }
    private static Object getUsingURL(String url, Hashtable env)
        throws NamingException {
        ResolveResult res = getUsingURLIgnoreRest(url, env);
        Context ctx = (Context)res.getResolvedObj();
        try {
            return ctx.lookup(res.getRemainingName());
        } finally {
            ctx.close();
        }
    }
    private static Object getUsingURLs(String[] urls, Hashtable env) {
        for (int i = 0; i < urls.length; i++) {
            String url = urls[i];
            try {
                Object obj = getUsingURL(url, env);
                if (obj != null) {
                    return obj;
                }
            } catch (NamingException e) {
            }
        }
        return null;    
    }
}
