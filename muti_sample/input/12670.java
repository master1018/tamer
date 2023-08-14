public class DnsContextFactory implements InitialContextFactory {
    private static final String DEFAULT_URL = "dns:";
    private static final int DEFAULT_PORT = 53;
    public Context getInitialContext(Hashtable<?,?> env) throws NamingException {
        if (env == null) {
            env = new Hashtable(5);
        }
        return urlToContext(getInitCtxUrl(env), env);
    }
    public static DnsContext getContext(String domain,
                                        String[] servers, Hashtable<?,?> env)
            throws NamingException {
        return new DnsContext(domain, servers, env);
    }
    public static DnsContext getContext(String domain,
                                        DnsUrl[] urls, Hashtable env)
            throws NamingException {
        String[] servers = serversForUrls(urls);
        DnsContext ctx = getContext(domain, servers, env);
        if (platformServersUsed(urls)) {
            ctx.setProviderUrl(constructProviderUrl(domain, servers));
        }
        return ctx;
    }
    public static boolean platformServersAvailable() {
        return !filterNameServers(
                    ResolverConfiguration.open().nameservers(), true
                ).isEmpty();
    }
    private static Context urlToContext(String url, Hashtable env)
            throws NamingException {
        DnsUrl[] urls;
        try {
            urls = DnsUrl.fromList(url);
        } catch (MalformedURLException e) {
            throw new ConfigurationException(e.getMessage());
        }
        if (urls.length == 0) {
            throw new ConfigurationException(
                    "Invalid DNS pseudo-URL(s): " + url);
        }
        String domain = urls[0].getDomain();
        for (int i = 1; i < urls.length; i++) {
            if (!domain.equalsIgnoreCase(urls[i].getDomain())) {
                throw new ConfigurationException(
                        "Conflicting domains: " + url);
            }
        }
        return getContext(domain, urls, env);
    }
    private static String[] serversForUrls(DnsUrl[] urls)
            throws NamingException {
        if (urls.length == 0) {
            throw new ConfigurationException("DNS pseudo-URL required");
        }
        List<String> servers = new ArrayList<>();
        for (int i = 0; i < urls.length; i++) {
            String server = urls[i].getHost();
            int port = urls[i].getPort();
            if (server == null && port < 0) {
                List<String> platformServers = filterNameServers(
                    ResolverConfiguration.open().nameservers(), false);
                if (!platformServers.isEmpty()) {
                    servers.addAll(platformServers);
                    continue;  
                }
            }
            if (server == null) {
                server = "localhost";
            }
            servers.add((port < 0)
                        ? server
                        : server + ":" + port);
        }
        return servers.toArray(new String[servers.size()]);
    }
    private static boolean platformServersUsed(DnsUrl[] urls) {
        if (!platformServersAvailable()) {
            return false;
        }
        for (int i = 0; i < urls.length; i++) {
            if (urls[i].getHost() == null &&
                urls[i].getPort() < 0) {
                return true;
            }
        }
        return false;
    }
    private static String constructProviderUrl(String domain,
                                               String[] servers) {
        String path = "";
        if (!domain.equals(".")) {
            try {
                path = "/" + UrlUtil.encode(domain, "ISO-8859-1");
            } catch (java.io.UnsupportedEncodingException e) {
            }
        }
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < servers.length; i++) {
            if (i > 0) {
                buf.append(' ');
            }
            buf.append("dns:
        }
        return buf.toString();
    }
    private static String getInitCtxUrl(Hashtable env) {
        String url = (String) env.get(Context.PROVIDER_URL);
        return ((url != null) ? url : DEFAULT_URL);
    }
    private static List filterNameServers(List input, boolean oneIsEnough) {
        SecurityManager security = System.getSecurityManager();
        if (security == null || input == null || input.isEmpty()) {
            return input;
        } else {
            List output = new ArrayList();
            for (Object o: input) {
                if (o instanceof String) {
                    String platformServer = (String)o;
                    int colon = platformServer.indexOf(':',
                            platformServer.indexOf(']') + 1);
                    int p = (colon < 0)
                        ? DEFAULT_PORT
                        : Integer.parseInt(
                            platformServer.substring(colon + 1));
                    String s = (colon < 0)
                        ? platformServer
                        : platformServer.substring(0, colon);
                    try {
                        security.checkConnect(s, p);
                        output.add(platformServer);
                        if (oneIsEnough) {
                            return output;
                        }
                    } catch (SecurityException se) {
                        continue;
                    }
                }
            }
            return output;
        }
    }
}
