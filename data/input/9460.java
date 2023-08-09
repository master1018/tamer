final public class LdapURL extends Uri {
    private boolean useSsl = false;
    private String DN = null;
    private String attributes = null;
    private String scope = null;
    private String filter = null;
    private String extensions = null;
    public LdapURL(String url) throws NamingException {
        super();
        try {
            init(url); 
            useSsl = scheme.equalsIgnoreCase("ldaps");
            if (! (scheme.equalsIgnoreCase("ldap") || useSsl)) {
                throw new MalformedURLException("Not an LDAP URL: " + url);
            }
            parsePathAndQuery(); 
        } catch (MalformedURLException e) {
            NamingException ne = new NamingException("Cannot parse url: " + url);
            ne.setRootCause(e);
            throw ne;
        } catch (UnsupportedEncodingException e) {
            NamingException ne = new NamingException("Cannot parse url: " + url);
            ne.setRootCause(e);
            throw ne;
        }
    }
    public boolean useSsl() {
        return useSsl;
    }
    public String getDN() {
        return DN;
    }
    public String getAttributes() {
        return attributes;
    }
    public String getScope() {
        return scope;
    }
    public String getFilter() {
        return filter;
    }
    public String getExtensions() {
        return extensions;
    }
    public static String[] fromList(String urlList) throws NamingException {
        String[] urls = new String[(urlList.length() + 1) / 2];
        int i = 0;              
        StringTokenizer st = new StringTokenizer(urlList, " ");
        while (st.hasMoreTokens()) {
            urls[i++] = st.nextToken();
        }
        String[] trimmed = new String[i];
        System.arraycopy(urls, 0, trimmed, 0, i);
        return trimmed;
    }
    public static boolean hasQueryComponents(String url) {
        return (url.lastIndexOf('?') != -1);
    }
    static String toUrlString(String host, int port, String dn, boolean useSsl)
        {
        try {
            String h = (host != null) ? host : "";
            if ((h.indexOf(':') != -1) && (h.charAt(0) != '[')) {
                h = "[" + h + "]";          
            }
            String p = (port != -1) ? (":" + port) : "";
            String d = (dn != null) ? ("/" + UrlUtil.encode(dn, "UTF8")) : "";
            return useSsl ? "ldaps:
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("UTF-8 encoding unavailable");
        }
    }
    private void parsePathAndQuery() throws MalformedURLException,
        UnsupportedEncodingException {
        if (path.equals("")) {
            return;
        }
        DN = path.startsWith("/") ? path.substring(1) : path;
        if (DN.length() > 0) {
            DN = UrlUtil.decode(DN, "UTF8");
        }
        if (query == null) {
            return;
        }
        int qmark2 = query.indexOf('?', 1);
        if (qmark2 < 0) {
            attributes = query.substring(1);
            return;
        } else if (qmark2 != 1) {
            attributes = query.substring(1, qmark2);
        }
        int qmark3 = query.indexOf('?', qmark2 + 1);
        if (qmark3 < 0) {
            scope = query.substring(qmark2 + 1);
            return;
        } else if (qmark3 != qmark2 + 1) {
            scope = query.substring(qmark2 + 1, qmark3);
        }
        int qmark4 = query.indexOf('?', qmark3 + 1);
        if (qmark4 < 0) {
            filter = query.substring(qmark3 + 1);
        } else {
            if (qmark4 != qmark3 + 1) {
                filter = query.substring(qmark3 + 1, qmark4);
            }
            extensions = query.substring(qmark4 + 1);
            if (extensions.length() > 0) {
                extensions = UrlUtil.decode(extensions, "UTF8");
            }
        }
        if (filter != null && filter.length() > 0) {
            filter = UrlUtil.decode(filter, "UTF8");
        }
    }
}
