public class JMXServiceURL implements Serializable {
    private static final long serialVersionUID = 8173364409860779292L;
    public JMXServiceURL(String serviceURL) throws MalformedURLException {
        final int serviceURLLength = serviceURL.length();
        for (int i = 0; i < serviceURLLength; i++) {
            char c = serviceURL.charAt(i);
            if (c < 32 || c >= 127) {
                throw new MalformedURLException("Service URL contains " +
                                                "non-ASCII character 0x" +
                                                Integer.toHexString(c));
            }
        }
        final String requiredPrefix = "service:jmx:";
        final int requiredPrefixLength = requiredPrefix.length();
        if (!serviceURL.regionMatches(true, 
                                      0,    
                                      requiredPrefix,
                                      0,    
                                      requiredPrefixLength)) {
            throw new MalformedURLException("Service URL must start with " +
                                            requiredPrefix);
        }
        final int protoStart = requiredPrefixLength;
        final int protoEnd = indexOf(serviceURL, ':', protoStart);
        this.protocol =
            serviceURL.substring(protoStart, protoEnd).toLowerCase();
        if (!serviceURL.regionMatches(protoEnd, ":
            throw new MalformedURLException("Missing \":
                                            "protocol name");
        }
        final int hostStart = protoEnd + 3;
        final int hostEnd;
        if (hostStart < serviceURLLength
            && serviceURL.charAt(hostStart) == '[') {
            hostEnd = serviceURL.indexOf(']', hostStart) + 1;
            if (hostEnd == 0)
                throw new MalformedURLException("Bad host name: [ without ]");
            this.host = serviceURL.substring(hostStart + 1, hostEnd - 1);
            if (!isNumericIPv6Address(this.host)) {
                throw new MalformedURLException("Address inside [...] must " +
                                                "be numeric IPv6 address");
            }
        } else {
            hostEnd =
                indexOfFirstNotInSet(serviceURL, hostNameBitSet, hostStart);
            this.host = serviceURL.substring(hostStart, hostEnd);
        }
        final int portEnd;
        if (hostEnd < serviceURLLength && serviceURL.charAt(hostEnd) == ':') {
            if (this.host.length() == 0) {
                throw new MalformedURLException("Cannot give port number " +
                                                "without host name");
            }
            final int portStart = hostEnd + 1;
            portEnd =
                indexOfFirstNotInSet(serviceURL, numericBitSet, portStart);
            final String portString = serviceURL.substring(portStart, portEnd);
            try {
                this.port = Integer.parseInt(portString);
            } catch (NumberFormatException e) {
                throw new MalformedURLException("Bad port number: \"" +
                                                portString + "\": " + e);
            }
        } else {
            portEnd = hostEnd;
            this.port = 0;
        }
        final int urlPathStart = portEnd;
        if (urlPathStart < serviceURLLength)
            this.urlPath = serviceURL.substring(urlPathStart);
        else
            this.urlPath = "";
        validate();
    }
    public JMXServiceURL(String protocol, String host, int port)
            throws MalformedURLException {
        this(protocol, host, port, null);
    }
    public JMXServiceURL(String protocol, String host, int port,
                         String urlPath)
            throws MalformedURLException {
        if (protocol == null)
            protocol = "jmxmp";
        if (host == null) {
            InetAddress local;
            try {
                local = InetAddress.getLocalHost();
            } catch (UnknownHostException e) {
                throw new MalformedURLException("Local host name unknown: " +
                                                e);
            }
            host = local.getHostName();
            try {
                validateHost(host);
            } catch (MalformedURLException e) {
                if (logger.fineOn()) {
                    logger.fine("JMXServiceURL",
                                "Replacing illegal local host name " +
                                host + " with numeric IP address " +
                                "(see RFC 1034)", e);
                }
                host = local.getHostAddress();
            }
        }
        if (host.startsWith("[")) {
            if (!host.endsWith("]")) {
                throw new MalformedURLException("Host starts with [ but " +
                                                "does not end with ]");
            }
            host = host.substring(1, host.length() - 1);
            if (!isNumericIPv6Address(host)) {
                throw new MalformedURLException("Address inside [...] must " +
                                                "be numeric IPv6 address");
            }
            if (host.startsWith("["))
                throw new MalformedURLException("More than one [[...]]");
        }
        this.protocol = protocol.toLowerCase();
        this.host = host;
        this.port = port;
        if (urlPath == null)
            urlPath = "";
        this.urlPath = urlPath;
        validate();
    }
    private void validate() throws MalformedURLException {
        final int protoEnd = indexOfFirstNotInSet(protocol, protocolBitSet, 0);
        if (protoEnd == 0 || protoEnd < protocol.length()
            || !alphaBitSet.get(protocol.charAt(0))) {
            throw new MalformedURLException("Missing or invalid protocol " +
                                            "name: \"" + protocol + "\"");
        }
        validateHost();
        if (port < 0)
            throw new MalformedURLException("Bad port: " + port);
        if (urlPath.length() > 0) {
            if (!urlPath.startsWith("/") && !urlPath.startsWith(";"))
                throw new MalformedURLException("Bad URL path: " + urlPath);
        }
    }
    private void validateHost() throws MalformedURLException {
        if (host.length() == 0) {
            if (port != 0) {
                throw new MalformedURLException("Cannot give port number " +
                                                "without host name");
            }
            return;
        }
        validateHost(host);
    }
    private static void validateHost(String h)
            throws MalformedURLException {
        if (isNumericIPv6Address(h)) {
            try {
                InetAddress.getByName(h);
            } catch (Exception e) {
                MalformedURLException bad =
                    new MalformedURLException("Bad IPv6 address: " + h);
                EnvHelp.initCause(bad, e);
                throw bad;
            }
        } else {
            final int hostLen = h.length();
            char lastc = '.';
            boolean sawDot = false;
            char componentStart = 0;
            loop:
            for (int i = 0; i < hostLen; i++) {
                char c = h.charAt(i);
                boolean isAlphaNumeric = alphaNumericBitSet.get(c);
                if (lastc == '.')
                    componentStart = c;
                if (isAlphaNumeric)
                    lastc = 'a';
                else if (c == '-') {
                    if (lastc == '.')
                        break; 
                    lastc = '-';
                } else if (c == '.') {
                    sawDot = true;
                    if (lastc != 'a')
                        break; 
                    lastc = '.';
                } else {
                    lastc = '.'; 
                    break;
                }
            }
            try {
                if (lastc != 'a')
                    throw randomException;
                if (sawDot && !alphaBitSet.get(componentStart)) {
                    StringTokenizer tok = new StringTokenizer(h, ".", true);
                    for (int i = 0; i < 4; i++) {
                        String ns = tok.nextToken();
                        int n = Integer.parseInt(ns);
                        if (n < 0 || n > 255)
                            throw randomException;
                        if (i < 3 && !tok.nextToken().equals("."))
                            throw randomException;
                    }
                    if (tok.hasMoreTokens())
                        throw randomException;
                }
            } catch (Exception e) {
                throw new MalformedURLException("Bad host: \"" + h + "\"");
            }
        }
    }
    private static final Exception randomException = new Exception();
    public String getProtocol() {
        return protocol;
    }
    public String getHost() {
        return host;
    }
    public int getPort() {
        return port;
    }
    public String getURLPath() {
        return urlPath;
    }
    public String toString() {
        if (toString != null)
            return toString;
        StringBuilder buf = new StringBuilder("service:jmx:");
        buf.append(getProtocol()).append(":
        final String getHost = getHost();
        if (isNumericIPv6Address(getHost))
            buf.append('[').append(getHost).append(']');
        else
            buf.append(getHost);
        final int getPort = getPort();
        if (getPort != 0)
            buf.append(':').append(getPort);
        buf.append(getURLPath());
        toString = buf.toString();
        return toString;
    }
    public boolean equals(Object obj) {
        if (!(obj instanceof JMXServiceURL))
            return false;
        JMXServiceURL u = (JMXServiceURL) obj;
        return
            (u.getProtocol().equalsIgnoreCase(getProtocol()) &&
             u.getHost().equalsIgnoreCase(getHost()) &&
             u.getPort() == getPort() &&
             u.getURLPath().equals(getURLPath()));
    }
    public int hashCode() {
        return toString().hashCode();
    }
    private static boolean isNumericIPv6Address(String s) {
        return (s.indexOf(':') >= 0);
    }
    private static int indexOf(String s, char c, int fromIndex) {
        int index = s.indexOf(c, fromIndex);
        if (index < 0)
            return s.length();
        else
            return index;
    }
    private static int indexOfFirstNotInSet(String s, BitSet set,
                                            int fromIndex) {
        final int slen = s.length();
        int i = fromIndex;
        while (true) {
            if (i >= slen)
                break;
            char c = s.charAt(i);
            if (c >= 128)
                break; 
            if (!set.get(c))
                break;
            i++;
        }
        return i;
    }
    private final static BitSet alphaBitSet = new BitSet(128);
    private final static BitSet numericBitSet = new BitSet(128);
    private final static BitSet alphaNumericBitSet = new BitSet(128);
    private final static BitSet protocolBitSet = new BitSet(128);
    private final static BitSet hostNameBitSet = new BitSet(128);
    static {
        for (char c = '0'; c <= '9'; c++)
            numericBitSet.set(c);
        for (char c = 'A'; c <= 'Z'; c++)
            alphaBitSet.set(c);
        for (char c = 'a'; c <= 'z'; c++)
            alphaBitSet.set(c);
        alphaNumericBitSet.or(alphaBitSet);
        alphaNumericBitSet.or(numericBitSet);
        protocolBitSet.or(alphaNumericBitSet);
        protocolBitSet.set('+');
        protocolBitSet.set('-');
        hostNameBitSet.or(alphaNumericBitSet);
        hostNameBitSet.set('-');
        hostNameBitSet.set('.');
    }
    private final String protocol;
    private final String host;
    private final int port;
    private final String urlPath;
    private transient String toString;
    private static final ClassLogger logger =
        new ClassLogger("javax.management.remote.misc", "JMXServiceURL");
}
