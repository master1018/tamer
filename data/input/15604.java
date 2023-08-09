public class ldapURLContextFactory implements ObjectFactory {
    public Object getObjectInstance(Object urlInfo, Name name, Context nameCtx,
            Hashtable<?,?> env) throws Exception {
        if (urlInfo == null) {
            return new ldapURLContext(env);
        } else {
            return LdapCtxFactory.getLdapCtxInstance(urlInfo, env);
        }
    }
    static ResolveResult getUsingURLIgnoreRootDN(String url, Hashtable env)
            throws NamingException {
        LdapURL ldapUrl = new LdapURL(url);
        DirContext ctx = new LdapCtx("", ldapUrl.getHost(), ldapUrl.getPort(),
            env, ldapUrl.useSsl());
        String dn = (ldapUrl.getDN() != null ? ldapUrl.getDN() : "");
        CompositeName remaining = new CompositeName();
        if (!"".equals(dn)) {
            remaining.add(dn);
        }
        return new ResolveResult(ctx, remaining);
    }
}
