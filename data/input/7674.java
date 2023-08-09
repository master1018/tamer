public class URIName implements GeneralNameInterface {
    private URI uri;
    private String host;
    private DNSName hostDNS;
    private IPAddressName hostIP;
    public URIName(DerValue derValue) throws IOException {
        this(derValue.getIA5String());
    }
    public URIName(String name) throws IOException {
        try {
            uri = new URI(name);
        } catch (URISyntaxException use) {
            throw (IOException) new IOException
                ("invalid URI name:" + name).initCause(use);
        }
        if (uri.getScheme() == null) {
            throw new IOException("URI name must include scheme:" + name);
        }
        host = uri.getHost();
        if (host != null) {
            if (host.charAt(0) == '[') {
                String ipV6Host = host.substring(1, host.length()-1);
                try {
                    hostIP = new IPAddressName(ipV6Host);
                } catch (IOException ioe) {
                    throw new IOException("invalid URI name (host " +
                        "portion is not a valid IPv6 address):" + name);
                }
            } else {
                try {
                    hostDNS = new DNSName(host);
                } catch (IOException ioe) {
                    try {
                        hostIP = new IPAddressName(host);
                    } catch (Exception ioe2) {
                        throw new IOException("invalid URI name (host " +
                            "portion is not a valid DNS name, IPv4 address," +
                            " or IPv6 address):" + name);
                    }
                }
            }
        }
    }
    public static URIName nameConstraint(DerValue value) throws IOException {
        URI uri;
        String name = value.getIA5String();
        try {
            uri = new URI(name);
        } catch (URISyntaxException use) {
            throw (IOException) new IOException
                ("invalid URI name constraint:" + name).initCause(use);
        }
        if (uri.getScheme() == null) {
            String host = uri.getSchemeSpecificPart();
            try {
                DNSName hostDNS;
                if (host.charAt(0) == '.') {
                    hostDNS = new DNSName(host.substring(1));
                } else {
                    hostDNS = new DNSName(host);
                }
                return new URIName(uri, host, hostDNS);
            } catch (IOException ioe) {
                throw (IOException) new IOException
                    ("invalid URI name constraint:" + name).initCause(ioe);
            }
        } else {
            throw new IOException("invalid URI name constraint (should not " +
                "include scheme):" + name);
        }
    }
    URIName(URI uri, String host, DNSName hostDNS) {
        this.uri = uri;
        this.host = host;
        this.hostDNS = hostDNS;
    }
    public int getType() {
        return GeneralNameInterface.NAME_URI;
    }
    public void encode(DerOutputStream out) throws IOException {
        out.putIA5String(uri.toASCIIString());
    }
    public String toString() {
        return "URIName: " + uri.toString();
    }
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof URIName)) {
            return false;
        }
        URIName other = (URIName) obj;
        return uri.equals(other.getURI());
    }
    public URI getURI() {
        return uri;
    }
    public String getName() {
        return uri.toString();
    }
    public String getScheme() {
        return uri.getScheme();
    }
    public String getHost() {
        return host;
    }
    public Object getHostObject() {
        if (hostIP != null) {
            return hostIP;
        } else {
            return hostDNS;
        }
    }
    public int hashCode() {
        return uri.hashCode();
    }
    public int constrains(GeneralNameInterface inputName)
        throws UnsupportedOperationException {
        int constraintType;
        if (inputName == null) {
            constraintType = NAME_DIFF_TYPE;
        } else if (inputName.getType() != NAME_URI) {
            constraintType = NAME_DIFF_TYPE;
        } else {
            String otherHost = ((URIName)inputName).getHost();
            if (otherHost.equalsIgnoreCase(host)) {
                constraintType = NAME_MATCH;
            } else {
                Object otherHostObject = ((URIName)inputName).getHostObject();
                if ((hostDNS == null) ||
                    !(otherHostObject instanceof DNSName)) {
                    constraintType = NAME_SAME_TYPE;
                } else {
                    boolean thisDomain = (host.charAt(0) == '.');
                    boolean otherDomain = (otherHost.charAt(0) == '.');
                    DNSName otherDNS = (DNSName) otherHostObject;
                    constraintType = hostDNS.constrains(otherDNS);
                    if ((!thisDomain && !otherDomain) &&
                        ((constraintType == NAME_WIDENS) ||
                         (constraintType == NAME_NARROWS))) {
                        constraintType = NAME_SAME_TYPE;
                    }
                    if ((thisDomain != otherDomain) &&
                        (constraintType == NAME_MATCH)) {
                        if (thisDomain) {
                            constraintType = NAME_WIDENS;
                        } else {
                            constraintType = NAME_NARROWS;
                        }
                    }
                }
            }
        }
        return constraintType;
    }
    public int subtreeDepth() throws UnsupportedOperationException {
        DNSName dnsName = null;
        try {
            dnsName = new DNSName(host);
        } catch (IOException ioe) {
            throw new UnsupportedOperationException(ioe.getMessage());
        }
        return dnsName.subtreeDepth();
    }
}
