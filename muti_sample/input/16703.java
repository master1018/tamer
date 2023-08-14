public class VmIdentifier {
    private URI uri;
    private URI canonicalize(String uriString) throws URISyntaxException {
        if (uriString == null) {
            uriString = "local:
            return new URI(uriString);
        }
        URI u = new URI(uriString);
        if (u.isAbsolute()) {
            if (u.isOpaque()) {
                u = new URI(u.getScheme(), "
                            u.getFragment());
            }
        } else {
            if (!uriString.startsWith("
                if (u.getFragment() == null) {
                    u = new URI("
                } else {
                    u = new URI("
                                + u.getFragment());
                }
            }
        }
        return u;
    }
    private void validate() throws URISyntaxException {
        String s = getScheme();
        if ((s != null) && (s.compareTo("file") == 0)) {
            return;
        }
        if (getLocalVmId() == -1) {
            throw new URISyntaxException(uri.toString(), "Local vmid required");
        }
    }
    public VmIdentifier(String uriString) throws URISyntaxException {
        URI u;
        try {
            u = canonicalize(uriString);
        } catch (URISyntaxException e) {
            if (uriString.startsWith("
                throw e;
            }
            u = canonicalize("
        }
        uri = u;
        validate();
    }
    public VmIdentifier(URI uri) throws URISyntaxException {
        this.uri = uri;
        validate();
    }
    public HostIdentifier getHostIdentifier() throws URISyntaxException {
        StringBuffer sb = new StringBuffer();
        if (getScheme() != null) {
            sb.append(getScheme()).append(":");
        }
        sb.append("
        if (getPort() != -1) {
            sb.append(":").append(getPort());
        }
        if (getPath() != null) {
            sb.append(getPath());
        }
        return new HostIdentifier(sb.toString());
    }
    public String getScheme() {
        return uri.getScheme();
    }
    public String getSchemeSpecificPart() {
        return uri.getSchemeSpecificPart();
    }
    public String getUserInfo() {
        return uri.getUserInfo();
    }
    public String getHost() {
        return uri.getHost();
    }
    public int getPort() {
        return uri.getPort();
    }
    public String getAuthority() {
        return uri.getAuthority();
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
    public int getLocalVmId() {
        int result = -1;
        try {
            if (uri.getUserInfo() == null) {
                result = Integer.parseInt(uri.getAuthority());
            } else {
                result = Integer.parseInt(uri.getUserInfo());
            }
        } catch (NumberFormatException e) { }
        return result;
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
        if (!(object instanceof VmIdentifier)) {
            return false;
        }
        return uri.equals(((VmIdentifier)object).uri);
    }
    public String toString() {
        return uri.toString();
    }
}
