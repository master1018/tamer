public class HostIdentifier {
    private URI uri;
    private URI canonicalize(String uriString) throws URISyntaxException {
        if ((uriString == null) || (uriString.compareTo("localhost") == 0)) {
            uriString = "
            return new URI(uriString);
        }
        URI u = new URI(uriString);
        if (u.isAbsolute()) {
            if (u.isOpaque()) {
                String scheme = u.getScheme();
                String ssp = u.getSchemeSpecificPart();
                String frag = u.getFragment();
                URI u2 = null;
                int c1index = uriString.indexOf(":");
                int c2index = uriString.lastIndexOf(":");
                if (c2index != c1index) {
                    if (frag == null) {
                        u2 = new URI(scheme + ":
                    } else {
                        u2 = new URI(scheme + ":
                    }
                    return u2;
                }
                u2 = new URI("
                return u2;
            } else {
                return u;
            }
        } else {
            String ssp = u.getSchemeSpecificPart();
            if (ssp.startsWith("
                return u;
            } else {
                return new URI("
            }
        }
    }
    public HostIdentifier(String uriString) throws URISyntaxException {
        uri = canonicalize(uriString);
    }
    public HostIdentifier(String scheme, String authority, String path,
                          String query, String fragment)
           throws URISyntaxException {
        uri = new URI(scheme, authority, path, query, fragment);
    }
    public HostIdentifier(VmIdentifier vmid) {
        StringBuilder sb = new StringBuilder();
        String scheme = vmid.getScheme();
        String host = vmid.getHost();
        String authority = vmid.getAuthority();
        if ((scheme != null) && (scheme.compareTo("file") == 0)) {
            try {
                uri = new URI("file:
            } catch (URISyntaxException e) { };
            return;
        }
        if ((host != null) && (host.compareTo(authority) == 0)) {
            host = null;
        }
        if (scheme == null) {
            if (host == null) {
                scheme = "local";            
            } else {
                scheme = "rmi";
            }
        }
        sb.append(scheme).append(":
        if (host == null) {
            sb.append("localhost");          
        } else {
            sb.append(host);
        }
        int port = vmid.getPort();
        if (port != -1) {
            sb.append(":").append(port);
        }
        String path = vmid.getPath();
        if ((path != null) && (path.length() != 0)) {
            sb.append(path);
        }
        String query = vmid.getQuery();
        if (query != null) {
            sb.append("?").append(query);
        }
        String frag = vmid.getFragment();
        if (frag != null) {
            sb.append("#").append(frag);
        }
        try {
           uri = new URI(sb.toString());
        } catch (URISyntaxException e) {
           throw new RuntimeException("Internal Error", e);
        }
    }
    public VmIdentifier resolve(VmIdentifier vmid)
           throws URISyntaxException, MonitorException {
        String scheme = vmid.getScheme();
        String host = vmid.getHost();
        String authority = vmid.getAuthority();
        if ((scheme != null) && (scheme.compareTo("file") == 0)) {
            return vmid;
        }
        if ((host != null) && (host.compareTo(authority) == 0)) {
            host = null;
        }
        if (scheme == null) {
            scheme = getScheme();
        }
        URI nuri = null;
        StringBuffer sb = new StringBuffer();
        sb.append(scheme).append(":
        String userInfo = vmid.getUserInfo();
        if (userInfo != null) {
            sb.append(userInfo);
        } else {
            sb.append(vmid.getAuthority());
        }
        if (host == null) {
            host = getHost();
        }
        sb.append("@").append(host);
        int port = vmid.getPort();
        if (port == -1) {
            port = getPort();
        }
        if (port != -1) {
            sb.append(":").append(port);
        }
        String path = vmid.getPath();
        if ((path == null) || (path.length() == 0)) {
            path = getPath();
        }
        if ((path != null) && (path.length() > 0)) {
            sb.append(path);
        }
        String query = vmid.getQuery();
        if (query == null) {
            query = getQuery();
        }
        if (query != null) {
            sb.append("?").append(query);
        }
        String fragment = vmid.getFragment();
        if (fragment == null) {
            fragment = getFragment();
        }
        if (fragment != null) {
            sb.append("#").append(fragment);
        }
        String s = sb.toString();
        return new VmIdentifier(s);
    }
    public String getScheme() {
        return uri.isAbsolute() ? uri.getScheme() : null;
    }
    public String getSchemeSpecificPart() {
        return  uri.getSchemeSpecificPart();
    }
    public String getUserInfo() {
        return uri.getUserInfo();
    }
    public String getHost() {
        return (uri.getHost() == null) ? "localhost" : uri.getHost();
    }
    public int getPort() {
        return uri.getPort();
    }
    public String getPath() {
        return uri.getPath();
    }
    public String getQuery() {
        return uri.getQuery();
    }
    public String getFragment() {
        return uri.getFragment();
    }
    public String getMode() {
        String query = getQuery();
        if (query != null) {
            String[] queryArgs = query.split("\\+");
            for (int i = 0; i < queryArgs.length; i++) {
                if (queryArgs[i].startsWith("mode=")) {
                    int index = queryArgs[i].indexOf('=');
                    return queryArgs[i].substring(index+1);
                }
            }
        }
        return "r";
    }
    public URI getURI() {
        return uri;
    }
    public int hashCode() {
        return uri.hashCode();
    }
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof HostIdentifier)) {
            return false;
        }
        return uri.equals(((HostIdentifier)object).uri);
    }
    public String toString() {
        return uri.toString();
    }
}
