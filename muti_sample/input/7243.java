public final class IiopUrl {
    static final private int DEFAULT_IIOPNAME_PORT = 9999;
    static final private int DEFAULT_IIOP_PORT = 900;
    static final private String DEFAULT_HOST = "localhost";
    private Vector addresses;
    private String stringName;
    public static class Address {
        public int port = -1;
        public int major, minor;
        public String host;
        public Address(String hostPortVers, boolean oldFormat)
            throws MalformedURLException {
            int start;
            int at;
            if (oldFormat || (at = hostPortVers.indexOf('@')) < 0) {
                major = 1;
                minor = 0;
                start = 0;     
            } else {
                int dot = hostPortVers.indexOf('.');
                if (dot < 0) {
                    throw new MalformedURLException(
                        "invalid version: " + hostPortVers);
                }
                try {
                    major = Integer.parseInt(hostPortVers.substring(0, dot));
                    minor = Integer.parseInt(hostPortVers.substring(dot+1, at));
                } catch (NumberFormatException e) {
                    throw new MalformedURLException(
                        "Nonnumeric version: " + hostPortVers);
                }
                start = at + 1;  
            }
            int slash = hostPortVers.indexOf('/', start);
            if (slash < 0) {
                slash = hostPortVers.length();
            }
            if (hostPortVers.startsWith("[", start)) {  
                int brac = hostPortVers.indexOf(']', start + 1);
                if (brac < 0 || brac > slash) {
                    throw new IllegalArgumentException(
                        "IiopURL: name is an Invalid URL: " + hostPortVers);
                }
                host = hostPortVers.substring(start, brac + 1);
                start = brac + 1;
            } else {      
                int colon = hostPortVers.indexOf(':', start);
                int hostEnd = (colon < 0 || colon > slash)
                    ? slash
                    : colon;
                if (start < hostEnd) {
                    host = hostPortVers.substring(start, hostEnd);
                }
                start = hostEnd;   
            }
            if ((start + 1 < slash)) {
                if ( hostPortVers.startsWith(":", start)) { 
                    start++;    
                    port = Integer.parseInt(hostPortVers.
                                            substring(start, slash));
                } else {
                    throw new IllegalArgumentException(
                        "IiopURL: name is an Invalid URL: " + hostPortVers);
                }
            }
            start = slash;
            if ("".equals(host) || host == null) {
                host = DEFAULT_HOST ;
            }
            if (port == -1) {
                port = (oldFormat ? DEFAULT_IIOP_PORT :
                                DEFAULT_IIOPNAME_PORT);
            }
        }
    }
    public Vector getAddresses() {
        return addresses;
    }
    public String getStringName() {
        return stringName;
    }
    public Name getCosName() throws NamingException {
        return CNCtx.parser.parse(stringName);
    }
    public IiopUrl(String url) throws MalformedURLException {
        int addrStart;
        boolean oldFormat;
        if (url.startsWith("iiopname:
            oldFormat = false;
            addrStart = 11;
        } else if (url.startsWith("iiop:
            oldFormat = true;
            addrStart = 7;
        } else {
            throw new MalformedURLException("Invalid iiop/iiopname URL: " + url);
        }
        int addrEnd = url.indexOf('/', addrStart);
        if (addrEnd < 0) {
            addrEnd = url.length();
            stringName = "";
        } else {
            stringName = UrlUtil.decode(url.substring(addrEnd+1));
        }
        addresses = new Vector(3);
        if (oldFormat) {
            addresses.addElement(
                new Address(url.substring(addrStart, addrEnd), oldFormat));
        } else {
            StringTokenizer tokens =
                new StringTokenizer(url.substring(addrStart, addrEnd), ",");
            while (tokens.hasMoreTokens()) {
                addresses.addElement(new Address(tokens.nextToken(), oldFormat));
            }
            if (addresses.size() == 0) {
                addresses.addElement(new Address("", oldFormat));
            }
        }
    }
}
