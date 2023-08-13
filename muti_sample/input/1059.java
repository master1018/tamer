class WindowsUriSupport {
    private WindowsUriSupport() {
    }
    private static final String IPV6_LITERAL_SUFFIX = ".ipv6-literal.net";
    private static URI toUri(String path, boolean isUnc, boolean addSlash) {
        String uriHost;
        String uriPath;
        if (isUnc) {
            int slash = path.indexOf('\\', 2);
            uriHost = path.substring(2, slash);
            uriPath = path.substring(slash).replace('\\', '/');
            if (uriHost.endsWith(IPV6_LITERAL_SUFFIX)) {
                uriHost = uriHost
                    .substring(0, uriHost.length() - IPV6_LITERAL_SUFFIX.length())
                    .replace('-', ':')
                    .replace('s', '%');
            }
        } else {
            uriHost = "";
            uriPath = "/" + path.replace('\\', '/');
        }
        if (addSlash)
            uriPath += "/";
        try {
            return new URI("file", uriHost, uriPath, null);
        } catch (URISyntaxException x) {
            if (!isUnc)
                throw new AssertionError(x);
        }
        uriPath = "
        if (addSlash)
            uriPath += "/";
        try {
            return new URI("file", null, uriPath, null);
        } catch (URISyntaxException x) {
            throw new AssertionError(x);
        }
    }
    static URI toUri(WindowsPath path) {
        path = path.toAbsolutePath();
        String s = path.toString();
        boolean addSlash = false;
        if (!s.endsWith("\\")) {
            try {
                 addSlash = WindowsFileAttributes.get(path, true).isDirectory();
            } catch (WindowsException x) {
            }
        }
        return toUri(s, path.isUnc(), addSlash);
    }
    static WindowsPath fromUri(WindowsFileSystem fs, URI uri) {
        if (!uri.isAbsolute())
            throw new IllegalArgumentException("URI is not absolute");
        if (uri.isOpaque())
            throw new IllegalArgumentException("URI is not hierarchical");
        String scheme = uri.getScheme();
        if ((scheme == null) || !scheme.equalsIgnoreCase("file"))
            throw new IllegalArgumentException("URI scheme is not \"file\"");
        if (uri.getFragment() != null)
            throw new IllegalArgumentException("URI has a fragment component");
        if (uri.getQuery() != null)
            throw new IllegalArgumentException("URI has a query component");
        String path = uri.getPath();
        if (path.equals(""))
            throw new IllegalArgumentException("URI path component is empty");
        String auth = uri.getAuthority();
        if (auth != null && !auth.equals("")) {
            String host = uri.getHost();
            if (host == null)
                throw new IllegalArgumentException("URI authority component has undefined host");
            if (uri.getUserInfo() != null)
                throw new IllegalArgumentException("URI authority component has user-info");
            if (uri.getPort() != -1)
                throw new IllegalArgumentException("URI authority component has port number");
            if (host.startsWith("[")) {
                host = host.substring(1, host.length()-1)
                           .replace(':', '-')
                           .replace('%', 's');
                host += IPV6_LITERAL_SUFFIX;
            }
            path = "\\\\" + host + path;
        } else {
            if ((path.length() > 2) && (path.charAt(2) == ':')) {
                path = path.substring(1);
            }
        }
        return WindowsPath.parse(fs, path);
    }
}
