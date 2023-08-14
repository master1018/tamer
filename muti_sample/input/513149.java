public final class URL implements java.io.Serializable {
    private static final long serialVersionUID = -7627629688361524110L;
    private static final NetPermission specifyStreamHandlerPermission = new NetPermission(
            "specifyStreamHandler"); 
    private int hashCode;
    private String file;
    private String protocol = null;
    private String host;
    private int port = -1;
    private String authority = null;
    private transient String userInfo = null;
    private transient String path = null;
    private transient String query = null;
    private String ref = null;
    private static Hashtable<String, URLStreamHandler> streamHandlers = new Hashtable<String, URLStreamHandler>();
    transient URLStreamHandler strmHandler;
    private static URLStreamHandlerFactory streamHandlerFactory;
    public static synchronized void setURLStreamHandlerFactory(
            URLStreamHandlerFactory streamFactory) {
        if (streamHandlerFactory != null) {
            throw new Error(Msg.getString("K004b")); 
        }
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkSetFactory();
        }
        streamHandlers.clear();
        streamHandlerFactory = streamFactory;
    }
    public URL(String spec) throws MalformedURLException {
        this((URL) null, spec, (URLStreamHandler) null);
    }
    public URL(URL context, String spec) throws MalformedURLException {
        this(context, spec, (URLStreamHandler) null);
    }
    public URL(URL context, String spec, URLStreamHandler handler)
            throws MalformedURLException {
        if (handler != null) {
            SecurityManager sm = System.getSecurityManager();
            if (sm != null) {
                sm.checkPermission(specifyStreamHandlerPermission);
            }
            strmHandler = handler;
        }
        if (spec == null) {
            throw new MalformedURLException();
        }
        spec = spec.trim();
        int index;
        try {
            index = spec.indexOf(':');
        } catch (NullPointerException e) {
            throw new MalformedURLException(e.toString());
        }
        int startIPv6Addr = spec.indexOf('[');
        if (index >= 0) {
            if ((startIPv6Addr == -1) || (index < startIPv6Addr)) {
                protocol = spec.substring(0, index);
                char c = protocol.charAt(0);
                boolean valid = ('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z');
                for (int i = 1; valid && (i < protocol.length()); i++) {
                    c = protocol.charAt(i);
                    valid = ('a' <= c && c <= 'z') ||
                            ('A' <= c && c <= 'Z') ||
                            ('0' <= c && c <= '9') ||
                            (c == '+') ||
                            (c == '-') ||
                            (c == '.');
                }
                if (!valid) {
                    protocol = null;
                    index = -1;
                } else {
                    protocol = Util.toASCIILowerCase(protocol);
                }
            }
        }
        if (protocol != null) {
            if (context != null && protocol.equals(context.getProtocol())) {
                String cPath = context.getPath();
                if (cPath != null && cPath.startsWith("/")) { 
                    set(protocol, context.getHost(), context.getPort(), context
                            .getAuthority(), context.getUserInfo(), cPath,
                            context.getQuery(), null);
                }
                if (strmHandler == null) {
                    strmHandler = context.strmHandler;
                }
            }
        } else {
            if (context == null) {
                throw new MalformedURLException(
                        org.apache.harmony.luni.util.Msg.getString(
                                "K00d8", spec)); 
            }
            set(context.getProtocol(), context.getHost(), context.getPort(),
                    context.getAuthority(), context.getUserInfo(), context
                            .getPath(), context.getQuery(), null);
            if (strmHandler == null) {
                strmHandler = context.strmHandler;
            }
        }
        if (strmHandler == null) {
            setupStreamHandler();
            if (strmHandler == null) {
                throw new MalformedURLException(
                        org.apache.harmony.luni.util.Msg.getString(
                                "K00b3", protocol)); 
            }
        }
        try {
            strmHandler.parseURL(this, spec, ++index, spec.length());
        } catch (Exception e) {
            throw new MalformedURLException(e.toString());
        }
        if (port < -1) {
            throw new MalformedURLException(org.apache.harmony.luni.util.Msg
                    .getString("K0325", port)); 
        }
    }
    public URL(String protocol, String host, String file)
            throws MalformedURLException {
        this(protocol, host, -1, file, (URLStreamHandler) null);
    }
    public URL(String protocol, String host, int port, String file)
            throws MalformedURLException {
        this(protocol, host, port, file, (URLStreamHandler) null);
    }
    public URL(String protocol, String host, int port, String file,
            URLStreamHandler handler) throws MalformedURLException {
        if (port < -1) {
            throw new MalformedURLException(Msg.getString("K0325", port)); 
        }
        if (host != null && host.indexOf(":") != -1 && host.charAt(0) != '[') { 
            host = "[" + host + "]"; 
        }
        if (protocol == null) {
            throw new NullPointerException(Msg.getString("K00b3", "null")); 
        }
        this.protocol = protocol;
        this.host = host;
        this.port = port;
        int index = -1;
        index = file.indexOf("#", file.lastIndexOf("/")); 
        if (index >= 0) {
            this.file = file.substring(0, index);
            ref = file.substring(index + 1);
        } else {
            this.file = file;
        }
        fixURL(false);
        if (handler == null) {
            setupStreamHandler();
            if (strmHandler == null) {
                throw new MalformedURLException(
                        Msg.getString("K00b3", protocol)); 
            }
        } else {
            SecurityManager sm = System.getSecurityManager();
            if (sm != null) {
                sm.checkPermission(specifyStreamHandlerPermission);
            }
            strmHandler = handler;
        }
    }
    void fixURL(boolean fixHost) {
        int index;
        if (host != null && host.length() > 0) {
            authority = host;
            if (port != -1) {
                authority = authority + ":" + port; 
            }
        }
        if (fixHost) {
            if (host != null && (index = host.lastIndexOf('@')) > -1) {
                userInfo = host.substring(0, index);
                host = host.substring(index + 1);
            } else {
                userInfo = null;
            }
        }
        if (file != null && (index = file.indexOf('?')) > -1) {
            query = file.substring(index + 1);
            path = file.substring(0, index);
        } else {
            query = null;
            path = file;
        }
    }
    protected void set(String protocol, String host, int port, String file,
            String ref) {
        if (this.protocol == null) {
            this.protocol = protocol;
        }
        this.host = host;
        this.file = file;
        this.port = port;
        this.ref = ref;
        hashCode = 0;
        fixURL(true);
    }
    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (this == o) {
            return true;
        }
        if (this.getClass() != o.getClass()) {
            return false;
        }
        return strmHandler.equals(this, (URL) o);
    }
    public boolean sameFile(URL otherURL) {
        return strmHandler.sameFile(this, otherURL);
    }
    @Override
    public int hashCode() {
        if (hashCode == 0) {
            hashCode = strmHandler.hashCode(this);
        }
        return hashCode;
    }
    void setupStreamHandler() {
        strmHandler = streamHandlers.get(protocol);
        if (strmHandler != null) {
            return;
        }
        if (streamHandlerFactory != null) {
            strmHandler = streamHandlerFactory.createURLStreamHandler(protocol);
            if (strmHandler != null) {
                streamHandlers.put(protocol, strmHandler);
                return;
            }
        }
        String packageList = AccessController
                .doPrivileged(new PriviAction<String>(
                        "java.protocol.handler.pkgs")); 
        if (packageList != null) {
            StringTokenizer st = new StringTokenizer(packageList, "|"); 
            while (st.hasMoreTokens()) {
                String className = st.nextToken() + "." + protocol + ".Handler"; 
                try {
                    strmHandler = (URLStreamHandler) Class.forName(className,
                            true, ClassLoader.getSystemClassLoader())
                            .newInstance();
                    if (strmHandler != null) {
                        streamHandlers.put(protocol, strmHandler);
                    }
                    return;
                } catch (IllegalAccessException e) {
                } catch (InstantiationException e) {
                } catch (ClassNotFoundException e) {
                }
            }
        }
        String className = "org.apache.harmony.luni.internal.net.www.protocol." + protocol 
                + ".Handler"; 
        try {
            strmHandler = (URLStreamHandler) Class.forName(className)
                    .newInstance();
        } catch (IllegalAccessException e) {
        } catch (InstantiationException e) {
        } catch (ClassNotFoundException e) {
        }
        if (strmHandler != null) {
            streamHandlers.put(protocol, strmHandler);
        }
    }
    public final Object getContent() throws IOException {
        return openConnection().getContent();
    }
    @SuppressWarnings("unchecked")
    public final Object getContent(Class[] types) throws IOException {
        return openConnection().getContent(types);
    }
    public final InputStream openStream() throws java.io.IOException {
        return openConnection().getInputStream();
    }
    public URLConnection openConnection() throws IOException {
        return strmHandler.openConnection(this);
    }
    public URI toURI() throws URISyntaxException {
        return new URI(toExternalForm());
    }
    public URLConnection openConnection(Proxy proxy) throws IOException {
        if (proxy == null) {
            throw new IllegalArgumentException(Msg.getString("K034c")); 
        }
        SecurityManager sm = System.getSecurityManager();
        if (sm != null && proxy.type() != Proxy.Type.DIRECT) {
            InetSocketAddress pAddress = (InetSocketAddress) proxy.address();
            String pHostName = pAddress.isUnresolved() ? pAddress.getHostName()
                    : pAddress.getAddress().getHostAddress();
            sm.checkConnect(pHostName, pAddress.getPort());
        }
        return strmHandler.openConnection(this, proxy);
    }
    @Override
    public String toString() {
        return toExternalForm();
    }
    public String toExternalForm() {
        if (strmHandler == null) {
            return "unknown protocol(" + protocol + "):
        }
        return strmHandler.toExternalForm(this);
    }
    private void readObject(java.io.ObjectInputStream stream)
            throws java.io.IOException {
        try {
            stream.defaultReadObject();
            if (host != null && authority == null) {
                fixURL(true);
            } else if (authority != null) {
                int index;
                if ((index = authority.lastIndexOf('@')) > -1) {
                    userInfo = authority.substring(0, index);
                }
                if (file != null && (index = file.indexOf('?')) > -1) {
                    query = file.substring(index + 1);
                    path = file.substring(0, index);
                } else {
                    path = file;
                }
            }
            setupStreamHandler();
            if (strmHandler == null) {
                throw new IOException(Msg.getString("K00b3", protocol)); 
            }
        } catch (ClassNotFoundException e) {
            throw new IOException(e.toString());
        }
    }
    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
    }
    public String getFile() {
        return file;
    }
    public String getHost() {
        return host;
    }
    public int getPort() {
        return port;
    }
    public String getProtocol() {
        return protocol;
    }
    public String getRef() {
        return ref;
    }
    public String getQuery() {
        return query;
    }
    public String getPath() {
        return path;
    }
    public String getUserInfo() {
        return userInfo;
    }
    public String getAuthority() {
        return authority;
    }
    protected void set(String protocol, String host, int port,
            String authority, String userInfo, String path, String query,
            String ref) {
        String filePart = path;
        if (query != null && !query.equals("")) { 
            if (filePart != null) {
                filePart = filePart + "?" + query; 
            } else {
                filePart = "?" + query; 
            }
        }
        set(protocol, host, port, filePart, ref);
        this.authority = authority;
        this.userInfo = userInfo;
        this.path = path;
        this.query = query;
    }
    public int getDefaultPort() {
        return strmHandler.getDefaultPort();
    }
}
