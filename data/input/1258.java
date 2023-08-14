public class iiopURLContext
        extends com.sun.jndi.toolkit.url.GenericURLContext {
    iiopURLContext(Hashtable env) {
        super(env);
    }
    protected ResolveResult getRootURLContext(String name, Hashtable env)
    throws NamingException {
        return iiopURLContextFactory.getUsingURLIgnoreRest(name, env);
    }
    protected Name getURLSuffix(String prefix, String url)
        throws NamingException {
        try {
            if (url.startsWith("iiop:
                IiopUrl parsedUrl = new IiopUrl(url);
                return parsedUrl.getCosName();
            } else if (url.startsWith("corbaname:")) {
                CorbanameUrl parsedUrl = new CorbanameUrl(url);
                return parsedUrl.getCosName();
            } else {
                throw new MalformedURLException("Not a valid URL: " + url);
            }
        } catch (MalformedURLException e) {
            throw new InvalidNameException(e.getMessage());
        }
    }
}
