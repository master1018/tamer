public class dnsURLContext extends GenericURLDirContext {
    public dnsURLContext(Hashtable env) {
        super(env);
    }
    protected ResolveResult getRootURLContext(String url, Hashtable env)
            throws NamingException {
        DnsUrl dnsUrl;
        try {
            dnsUrl = new DnsUrl(url);
        } catch (MalformedURLException e) {
            throw new InvalidNameException(e.getMessage());
        }
        DnsUrl[] urls = new DnsUrl[] { dnsUrl };
        String domain = dnsUrl.getDomain();
        return new ResolveResult(
                DnsContextFactory.getContext(".", urls, env),
                new CompositeName().add(domain));
    }
}
