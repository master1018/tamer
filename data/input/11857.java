public class FtpURLConnection extends URLConnection {
    HttpURLConnection http = null;
    private Proxy instProxy;
    InputStream is = null;
    OutputStream os = null;
    FtpClient ftp = null;
    Permission permission;
    String password;
    String user;
    String host;
    String pathname;
    String filename;
    String fullpath;
    int port;
    static final int NONE = 0;
    static final int ASCII = 1;
    static final int BIN = 2;
    static final int DIR = 3;
    int type = NONE;
    private int connectTimeout = NetworkClient.DEFAULT_CONNECT_TIMEOUT;;
    private int readTimeout = NetworkClient.DEFAULT_READ_TIMEOUT;;
    protected class FtpInputStream extends FilterInputStream {
        FtpClient ftp;
        FtpInputStream(FtpClient cl, InputStream fd) {
            super(new BufferedInputStream(fd));
            ftp = cl;
        }
        @Override
        public void close() throws IOException {
            super.close();
            if (ftp != null) {
                ftp.close();
            }
        }
    }
    protected class FtpOutputStream extends FilterOutputStream {
        FtpClient ftp;
        FtpOutputStream(FtpClient cl, OutputStream fd) {
            super(fd);
            ftp = cl;
        }
        @Override
        public void close() throws IOException {
            super.close();
            if (ftp != null) {
                ftp.close();
            }
        }
    }
    public FtpURLConnection(URL url) {
        this(url, null);
    }
    FtpURLConnection(URL url, Proxy p) {
        super(url);
        instProxy = p;
        host = url.getHost();
        port = url.getPort();
        String userInfo = url.getUserInfo();
        if (userInfo != null) { 
            int delimiter = userInfo.indexOf(':');
            if (delimiter == -1) {
                user = ParseUtil.decode(userInfo);
                password = null;
            } else {
                user = ParseUtil.decode(userInfo.substring(0, delimiter++));
                password = ParseUtil.decode(userInfo.substring(delimiter));
            }
        }
    }
    private void setTimeouts() {
        if (ftp != null) {
            if (connectTimeout >= 0) {
                ftp.setConnectTimeout(connectTimeout);
            }
            if (readTimeout >= 0) {
                ftp.setReadTimeout(readTimeout);
            }
        }
    }
    public synchronized void connect() throws IOException {
        if (connected) {
            return;
        }
        Proxy p = null;
        if (instProxy == null) { 
            ProxySelector sel = java.security.AccessController.doPrivileged(
                    new java.security.PrivilegedAction<ProxySelector>() {
                        public ProxySelector run() {
                            return ProxySelector.getDefault();
                        }
                    });
            if (sel != null) {
                URI uri = sun.net.www.ParseUtil.toURI(url);
                Iterator<Proxy> it = sel.select(uri).iterator();
                while (it.hasNext()) {
                    p = it.next();
                    if (p == null || p == Proxy.NO_PROXY ||
                        p.type() == Proxy.Type.SOCKS) {
                        break;
                    }
                    if (p.type() != Proxy.Type.HTTP ||
                            !(p.address() instanceof InetSocketAddress)) {
                        sel.connectFailed(uri, p.address(), new IOException("Wrong proxy type"));
                        continue;
                    }
                    InetSocketAddress paddr = (InetSocketAddress) p.address();
                    try {
                        http = new HttpURLConnection(url, p);
                        http.setDoInput(getDoInput());
                        http.setDoOutput(getDoOutput());
                        if (connectTimeout >= 0) {
                            http.setConnectTimeout(connectTimeout);
                        }
                        if (readTimeout >= 0) {
                            http.setReadTimeout(readTimeout);
                        }
                        http.connect();
                        connected = true;
                        return;
                    } catch (IOException ioe) {
                        sel.connectFailed(uri, paddr, ioe);
                        http = null;
                    }
                }
            }
        } else { 
            p = instProxy;
            if (p.type() == Proxy.Type.HTTP) {
                http = new HttpURLConnection(url, instProxy);
                http.setDoInput(getDoInput());
                http.setDoOutput(getDoOutput());
                if (connectTimeout >= 0) {
                    http.setConnectTimeout(connectTimeout);
                }
                if (readTimeout >= 0) {
                    http.setReadTimeout(readTimeout);
                }
                http.connect();
                connected = true;
                return;
            }
        }
        if (user == null) {
            user = "anonymous";
            String vers = java.security.AccessController.doPrivileged(
                    new GetPropertyAction("java.version"));
            password = java.security.AccessController.doPrivileged(
                    new GetPropertyAction("ftp.protocol.user",
                                          "Java" + vers + "@"));
        }
        try {
            ftp = FtpClient.create();
            if (p != null) {
                ftp.setProxy(p);
            }
            setTimeouts();
            if (port != -1) {
                ftp.connect(new InetSocketAddress(host, port));
            } else {
                ftp.connect(new InetSocketAddress(host, FtpClient.defaultPort()));
            }
        } catch (UnknownHostException e) {
            throw e;
        } catch (FtpProtocolException fe) {
            throw new IOException(fe);
        }
        try {
            ftp.login(user, password.toCharArray());
        } catch (sun.net.ftp.FtpProtocolException e) {
            ftp.close();
            throw new sun.net.ftp.FtpLoginException("Invalid username/password");
        }
        connected = true;
    }
    private void decodePath(String path) {
        int i = path.indexOf(";type=");
        if (i >= 0) {
            String s1 = path.substring(i + 6, path.length());
            if ("i".equalsIgnoreCase(s1)) {
                type = BIN;
            }
            if ("a".equalsIgnoreCase(s1)) {
                type = ASCII;
            }
            if ("d".equalsIgnoreCase(s1)) {
                type = DIR;
            }
            path = path.substring(0, i);
        }
        if (path != null && path.length() > 1 &&
                path.charAt(0) == '/') {
            path = path.substring(1);
        }
        if (path == null || path.length() == 0) {
            path = "./";
        }
        if (!path.endsWith("/")) {
            i = path.lastIndexOf('/');
            if (i > 0) {
                filename = path.substring(i + 1, path.length());
                filename = ParseUtil.decode(filename);
                pathname = path.substring(0, i);
            } else {
                filename = ParseUtil.decode(path);
                pathname = null;
            }
        } else {
            pathname = path.substring(0, path.length() - 1);
            filename = null;
        }
        if (pathname != null) {
            fullpath = pathname + "/" + (filename != null ? filename : "");
        } else {
            fullpath = filename;
        }
    }
    private void cd(String path) throws FtpProtocolException, IOException {
        if (path == null || path.isEmpty()) {
            return;
        }
        if (path.indexOf('/') == -1) {
            ftp.changeDirectory(ParseUtil.decode(path));
            return;
        }
        StringTokenizer token = new StringTokenizer(path, "/");
        while (token.hasMoreTokens()) {
            ftp.changeDirectory(ParseUtil.decode(token.nextToken()));
        }
    }
    @Override
    public InputStream getInputStream() throws IOException {
        if (!connected) {
            connect();
        }
        if (http != null) {
            return http.getInputStream();
        }
        if (os != null) {
            throw new IOException("Already opened for output");
        }
        if (is != null) {
            return is;
        }
        MessageHeader msgh = new MessageHeader();
        boolean isAdir = false;
        try {
            decodePath(url.getPath());
            if (filename == null || type == DIR) {
                ftp.setAsciiType();
                cd(pathname);
                if (filename == null) {
                    is = new FtpInputStream(ftp, ftp.list(null));
                } else {
                    is = new FtpInputStream(ftp, ftp.nameList(filename));
                }
            } else {
                if (type == ASCII) {
                    ftp.setAsciiType();
                } else {
                    ftp.setBinaryType();
                }
                cd(pathname);
                is = new FtpInputStream(ftp, ftp.getFileStream(filename));
            }
            try {
                long l = ftp.getLastTransferSize();
                msgh.add("content-length", Long.toString(l));
                if (l > 0) {
                    boolean meteredInput = ProgressMonitor.getDefault().shouldMeterInput(url, "GET");
                    ProgressSource pi = null;
                    if (meteredInput) {
                        pi = new ProgressSource(url, "GET", l);
                        pi.beginTracking();
                    }
                    is = new MeteredStream(is, pi, l);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (isAdir) {
                msgh.add("content-type", "text/plain");
                msgh.add("access-type", "directory");
            } else {
                msgh.add("access-type", "file");
                String ftype = guessContentTypeFromName(fullpath);
                if (ftype == null && is.markSupported()) {
                    ftype = guessContentTypeFromStream(is);
                }
                if (ftype != null) {
                    msgh.add("content-type", ftype);
                }
            }
        } catch (FileNotFoundException e) {
            try {
                cd(fullpath);
                ftp.setAsciiType();
                is = new FtpInputStream(ftp, ftp.list(null));
                msgh.add("content-type", "text/plain");
                msgh.add("access-type", "directory");
            } catch (IOException ex) {
                throw new FileNotFoundException(fullpath);
            } catch (FtpProtocolException ex2) {
                throw new FileNotFoundException(fullpath);
            }
        } catch (FtpProtocolException ftpe) {
            throw new IOException(ftpe);
        }
        setProperties(msgh);
        return is;
    }
    @Override
    public OutputStream getOutputStream() throws IOException {
        if (!connected) {
            connect();
        }
        if (http != null) {
            OutputStream out = http.getOutputStream();
            http.getInputStream();
            return out;
        }
        if (is != null) {
            throw new IOException("Already opened for input");
        }
        if (os != null) {
            return os;
        }
        decodePath(url.getPath());
        if (filename == null || filename.length() == 0) {
            throw new IOException("illegal filename for a PUT");
        }
        try {
            if (pathname != null) {
                cd(pathname);
            }
            if (type == ASCII) {
                ftp.setAsciiType();
            } else {
                ftp.setBinaryType();
            }
            os = new FtpOutputStream(ftp, ftp.putFileStream(filename, false));
        } catch (FtpProtocolException e) {
            throw new IOException(e);
        }
        return os;
    }
    String guessContentTypeFromFilename(String fname) {
        return guessContentTypeFromName(fname);
    }
    @Override
    public Permission getPermission() {
        if (permission == null) {
            int urlport = url.getPort();
            urlport = urlport < 0 ? FtpClient.defaultPort() : urlport;
            String urlhost = this.host + ":" + urlport;
            permission = new SocketPermission(urlhost, "connect");
        }
        return permission;
    }
    @Override
    public void setRequestProperty(String key, String value) {
        super.setRequestProperty(key, value);
        if ("type".equals(key)) {
            if ("i".equalsIgnoreCase(value)) {
                type = BIN;
            } else if ("a".equalsIgnoreCase(value)) {
                type = ASCII;
            } else if ("d".equalsIgnoreCase(value)) {
                type = DIR;
            } else {
                throw new IllegalArgumentException(
                        "Value of '" + key +
                        "' request property was '" + value +
                        "' when it must be either 'i', 'a' or 'd'");
            }
        }
    }
    @Override
    public String getRequestProperty(String key) {
        String value = super.getRequestProperty(key);
        if (value == null) {
            if ("type".equals(key)) {
                value = (type == ASCII ? "a" : type == DIR ? "d" : "i");
            }
        }
        return value;
    }
    @Override
    public void setConnectTimeout(int timeout) {
        if (timeout < 0) {
            throw new IllegalArgumentException("timeouts can't be negative");
        }
        connectTimeout = timeout;
    }
    @Override
    public int getConnectTimeout() {
        return (connectTimeout < 0 ? 0 : connectTimeout);
    }
    @Override
    public void setReadTimeout(int timeout) {
        if (timeout < 0) {
            throw new IllegalArgumentException("timeouts can't be negative");
        }
        readTimeout = timeout;
    }
    @Override
    public int getReadTimeout() {
        return readTimeout < 0 ? 0 : readTimeout;
    }
}
