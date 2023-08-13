final public class LdapCtxFactory implements ObjectFactory, InitialContextFactory {
    public final static String ADDRESS_TYPE = "URL";
    public Object getObjectInstance(Object ref, Name name, Context nameCtx,
        Hashtable<?,?> env) throws Exception {
        if (!isLdapRef(ref)) {
            return null;
        }
        ObjectFactory factory = new ldapURLContextFactory();
        String[] urls = getURLs((Reference)ref);
        return factory.getObjectInstance(urls, name, nameCtx, env);
    }
    public Context getInitialContext(Hashtable<?,?> envprops)
        throws NamingException {
        try {
            String providerUrl = (envprops != null) ?
                (String)envprops.get(Context.PROVIDER_URL) : null;
            if (providerUrl == null) {
                return new LdapCtx("", LdapCtx.DEFAULT_HOST,
                    LdapCtx.DEFAULT_PORT, envprops, false);
            }
            String[] urls = LdapURL.fromList(providerUrl);
            if (urls.length == 0) {
                throw new ConfigurationException(Context.PROVIDER_URL +
                    " property does not contain a URL");
            }
            return getLdapCtxInstance(urls, envprops);
        } catch (LdapReferralException e) {
            if (envprops != null &&
                "throw".equals(envprops.get(Context.REFERRAL))) {
                throw e;
            }
            Control[] bindCtls = (envprops != null)?
                (Control[])envprops.get(LdapCtx.BIND_CONTROLS) : null;
            return (LdapCtx)e.getReferralContext(envprops, bindCtls);
        }
    }
    private static boolean isLdapRef(Object obj) {
        if (!(obj instanceof Reference)) {
            return false;
        }
        String thisClassName = LdapCtxFactory.class.getName();
        Reference ref = (Reference)obj;
        return thisClassName.equals(ref.getFactoryClassName());
    }
    private static String[] getURLs(Reference ref) throws NamingException {
        int size = 0;   
        String[] urls = new String[ref.size()];
        Enumeration addrs = ref.getAll();
        while (addrs.hasMoreElements()) {
            RefAddr addr = (RefAddr)addrs.nextElement();
            if ((addr instanceof StringRefAddr) &&
                addr.getType().equals(ADDRESS_TYPE)) {
                urls[size++] = (String)addr.getContent();
            }
        }
        if (size == 0) {
            throw (new ConfigurationException(
                    "Reference contains no valid addresses"));
        }
        if (size == ref.size()) {
            return urls;
        }
        String[] urls2 = new String[size];
        System.arraycopy(urls, 0, urls2, 0, size);
        return urls2;
    }
    public static DirContext getLdapCtxInstance(Object urlInfo, Hashtable env)
            throws NamingException {
        if (urlInfo instanceof String) {
            return getUsingURL((String)urlInfo, env);
        } else if (urlInfo instanceof String[]) {
            return getUsingURLs((String[])urlInfo, env);
        } else {
            throw new IllegalArgumentException(
                "argument must be an LDAP URL String or array of them");
        }
    }
    private static DirContext getUsingURL(String url, Hashtable env)
            throws NamingException {
        DirContext ctx = null;
        LdapURL ldapUrl = new LdapURL(url);
        String dn = ldapUrl.getDN();
        String host = ldapUrl.getHost();
        int port = ldapUrl.getPort();
        String[] hostports;
        String domainName = null;
        if (host == null &&
            port == -1 &&
            dn != null &&
            (domainName = ServiceLocator.mapDnToDomainName(dn)) != null &&
            (hostports = ServiceLocator.getLdapService(domainName, env))
                != null) {
            String scheme = ldapUrl.getScheme() + ":
            String[] newUrls = new String[hostports.length];
            String query = ldapUrl.getQuery();
            String urlSuffix = ldapUrl.getPath() + (query != null ? query : "");
            for (int i = 0; i < hostports.length; i++) {
                newUrls[i] = scheme + hostports[i] + urlSuffix;
            }
            ctx = getUsingURLs(newUrls, env);
            ((LdapCtx)ctx).setDomainName(domainName);
        } else {
            ctx = new LdapCtx(dn, host, port, env, ldapUrl.useSsl());
            ((LdapCtx)ctx).setProviderUrl(url);
        }
        return ctx;
    }
    private static DirContext getUsingURLs(String[] urls, Hashtable env)
            throws NamingException {
        NamingException ne = null;
        DirContext ctx = null;
        for (int i = 0; i < urls.length; i++) {
            try {
                return getUsingURL(urls[i], env);
            } catch (AuthenticationException e) {
                throw e;
            } catch (NamingException e) {
                ne = e;
            }
        }
        throw ne;
    }
    public static Attribute createTypeNameAttr(Class cl) {
        Vector v = new Vector(10);
        String[] types = getTypeNames(cl, v);
        if (types.length > 0) {
            BasicAttribute tAttr =
                new BasicAttribute(Obj.JAVA_ATTRIBUTES[Obj.TYPENAME]);
            for (int i = 0; i < types.length; i++) {
                tAttr.add(types[i]);
            }
            return tAttr;
        }
        return null;
    }
    private static String[] getTypeNames(Class currentClass, Vector v) {
        getClassesAux(currentClass, v);
        Class[] members = currentClass.getInterfaces();
        for (int i = 0; i < members.length; i++) {
            getClassesAux(members[i], v);
        }
        String[] ret = new String[v.size()];
        int i = 0;
        for (java.util.Enumeration e = v.elements(); e.hasMoreElements();) {
            ret[i++] = (String)e.nextElement();
        }
        return ret;
    }
    private static void getClassesAux(Class currentClass, Vector v) {
        if (!v.contains(currentClass.getName())) {
            v.addElement(currentClass.getName());
        }
        currentClass = currentClass.getSuperclass();
        while (currentClass != null) {
            getTypeNames(currentClass, v);
            currentClass = currentClass.getSuperclass();
        }
    }
}
