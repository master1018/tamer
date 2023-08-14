public class HttpClient extends NetworkClient {
    protected boolean cachedHttpClient = false;
    private boolean inCache;
    protected CookieHandler cookieHandler;
    MessageHeader requests;
    PosterOutputStream poster = null;
    boolean streaming;
    boolean failedOnce = false;
    private boolean ignoreContinue = true;
    private static final int    HTTP_CONTINUE = 100;
    static final int    httpPortNumber = 80;
    protected int getDefaultPort () { return httpPortNumber; }
    static private int getDefaultPort(String proto) {
        if ("http".equalsIgnoreCase(proto))
            return 80;
        if ("https".equalsIgnoreCase(proto))
            return 443;
        return -1;
    }
    protected boolean proxyDisabled;
    public boolean usingProxy = false;
    protected String host;
    protected int port;
    protected static KeepAliveCache kac = new KeepAliveCache();
    private static boolean keepAliveProp = true;
    private static boolean retryPostProp = true;
    volatile boolean keepingAlive = false;     
    int keepAliveConnections = -1;    
    int keepAliveTimeout = 0;
    private CacheRequest cacheRequest = null;
    protected URL       url;
    public boolean reuse = false;
     private HttpCapture capture = null;
    @Deprecated
    public static synchronized void resetProperties() {
    }
    int getKeepAliveTimeout() {
        return keepAliveTimeout;
    }
    static {
        String keepAlive = java.security.AccessController.doPrivileged(
            new sun.security.action.GetPropertyAction("http.keepAlive"));
        String retryPost = java.security.AccessController.doPrivileged(
            new sun.security.action.GetPropertyAction("sun.net.http.retryPost"));
        if (keepAlive != null) {
            keepAliveProp = Boolean.valueOf(keepAlive).booleanValue();
        } else {
            keepAliveProp = true;
        }
        if (retryPost != null) {
            retryPostProp = Boolean.valueOf(retryPost).booleanValue();
        } else
            retryPostProp = true;
    }
    public boolean getHttpKeepAliveSet() {
        return keepAliveProp;
    }
    protected HttpClient() {
    }
    private HttpClient(URL url)
    throws IOException {
        this(url, (String)null, -1, false);
    }
    protected HttpClient(URL url,
                         boolean proxyDisabled) throws IOException {
        this(url, null, -1, proxyDisabled);
    }
    public HttpClient(URL url, String proxyHost, int proxyPort)
    throws IOException {
        this(url, proxyHost, proxyPort, false);
    }
    protected HttpClient(URL url, Proxy p, int to) throws IOException {
        proxy = (p == null) ? Proxy.NO_PROXY : p;
        this.host = url.getHost();
        this.url = url;
        port = url.getPort();
        if (port == -1) {
            port = getDefaultPort();
        }
        setConnectTimeout(to);
        cookieHandler = java.security.AccessController.doPrivileged(
            new java.security.PrivilegedAction<CookieHandler>() {
                public CookieHandler run() {
                    return CookieHandler.getDefault();
                }
            });
        capture = HttpCapture.getCapture(url);
        openServer();
    }
    static protected Proxy newHttpProxy(String proxyHost, int proxyPort,
                                      String proto) {
        if (proxyHost == null || proto == null)
            return Proxy.NO_PROXY;
        int pport = proxyPort < 0 ? getDefaultPort(proto) : proxyPort;
        InetSocketAddress saddr = InetSocketAddress.createUnresolved(proxyHost, pport);
        return new Proxy(Proxy.Type.HTTP, saddr);
    }
    private HttpClient(URL url, String proxyHost, int proxyPort,
                       boolean proxyDisabled)
        throws IOException {
        this(url, proxyDisabled ? Proxy.NO_PROXY :
             newHttpProxy(proxyHost, proxyPort, "http"), -1);
    }
    public HttpClient(URL url, String proxyHost, int proxyPort,
                       boolean proxyDisabled, int to)
        throws IOException {
        this(url, proxyDisabled ? Proxy.NO_PROXY :
             newHttpProxy(proxyHost, proxyPort, "http"), to);
    }
    public static HttpClient New(URL url)
    throws IOException {
        return HttpClient.New(url, Proxy.NO_PROXY, -1, true);
    }
    public static HttpClient New(URL url, boolean useCache)
        throws IOException {
        return HttpClient.New(url, Proxy.NO_PROXY, -1, useCache);
    }
    public static HttpClient New(URL url, Proxy p, int to, boolean useCache)
        throws IOException {
        if (p == null) {
            p = Proxy.NO_PROXY;
        }
        HttpClient ret = null;
        if (useCache) {
            ret = kac.get(url, null);
            if (ret != null) {
                if ((ret.proxy != null && ret.proxy.equals(p)) ||
                    (ret.proxy == null && p == null)) {
                    synchronized (ret) {
                        ret.cachedHttpClient = true;
                        assert ret.inCache;
                        ret.inCache = false;
                        PlatformLogger logger = HttpURLConnection.getHttpLogger();
                        if (logger.isLoggable(PlatformLogger.FINEST)) {
                            logger.finest("KeepAlive stream retrieved from the cache, " + ret);
                        }
                    }
                } else {
                    synchronized(ret) {
                        ret.inCache = false;
                        ret.closeServer();
                    }
                    ret = null;
                }
            }
        }
        if (ret == null) {
            ret = new HttpClient(url, p, to);
        } else {
            SecurityManager security = System.getSecurityManager();
            if (security != null) {
                if (ret.proxy == Proxy.NO_PROXY || ret.proxy == null) {
                    security.checkConnect(InetAddress.getByName(url.getHost()).getHostAddress(), url.getPort());
                } else {
                    security.checkConnect(url.getHost(), url.getPort());
                }
            }
            ret.url = url;
        }
        return ret;
    }
    public static HttpClient New(URL url, Proxy p, int to) throws IOException {
        return New(url, p, to, true);
    }
    public static HttpClient New(URL url, String proxyHost, int proxyPort,
                                 boolean useCache)
        throws IOException {
        return New(url, newHttpProxy(proxyHost, proxyPort, "http"), -1, useCache);
    }
    public static HttpClient New(URL url, String proxyHost, int proxyPort,
                                 boolean useCache, int to)
        throws IOException {
        return New(url, newHttpProxy(proxyHost, proxyPort, "http"), to, useCache);
    }
    public void finished() {
        if (reuse) 
            return;
        keepAliveConnections--;
        poster = null;
        if (keepAliveConnections > 0 && isKeepingAlive() &&
               !(serverOutput.checkError())) {
            putInKeepAliveCache();
        } else {
            closeServer();
        }
    }
    protected synchronized void putInKeepAliveCache() {
        if (inCache) {
            assert false : "Duplicate put to keep alive cache";
            return;
        }
        inCache = true;
        kac.put(url, null, this);
    }
    protected synchronized boolean isInKeepAliveCache() {
        return inCache;
    }
    public void closeIdleConnection() {
        HttpClient http = kac.get(url, null);
        if (http != null) {
            http.closeServer();
        }
    }
    @Override
    public void openServer(String server, int port) throws IOException {
        serverSocket = doConnect(server, port);
        try {
            OutputStream out = serverSocket.getOutputStream();
            if (capture != null) {
                out = new HttpCaptureOutputStream(out, capture);
            }
            serverOutput = new PrintStream(
                new BufferedOutputStream(out),
                                         false, encoding);
        } catch (UnsupportedEncodingException e) {
            throw new InternalError(encoding+" encoding not found");
        }
        serverSocket.setTcpNoDelay(true);
    }
    public boolean needsTunneling() {
        return false;
    }
    public synchronized boolean isCachedConnection() {
        return cachedHttpClient;
    }
    public void afterConnect() throws IOException, UnknownHostException {
    }
    private synchronized void privilegedOpenServer(final InetSocketAddress server)
         throws IOException
    {
        try {
            java.security.AccessController.doPrivileged(
                new java.security.PrivilegedExceptionAction<Void>() {
                    public Void run() throws IOException {
                    openServer(server.getHostString(), server.getPort());
                    return null;
                }
            });
        } catch (java.security.PrivilegedActionException pae) {
            throw (IOException) pae.getException();
        }
    }
    private void superOpenServer(final String proxyHost,
                                 final int proxyPort)
        throws IOException, UnknownHostException
    {
        super.openServer(proxyHost, proxyPort);
    }
    protected synchronized void openServer() throws IOException {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkConnect(host, port);
        }
        if (keepingAlive) { 
            return;
        }
        if (url.getProtocol().equals("http") ||
            url.getProtocol().equals("https") ) {
            if ((proxy != null) && (proxy.type() == Proxy.Type.HTTP)) {
                sun.net.www.URLConnection.setProxiedHost(host);
                privilegedOpenServer((InetSocketAddress) proxy.address());
                usingProxy = true;
                return;
            } else {
                openServer(host, port);
                usingProxy = false;
                return;
            }
        } else {
            if ((proxy != null) && (proxy.type() == Proxy.Type.HTTP)) {
                sun.net.www.URLConnection.setProxiedHost(host);
                privilegedOpenServer((InetSocketAddress) proxy.address());
                usingProxy = true;
                return;
            } else {
                super.openServer(host, port);
                usingProxy = false;
                return;
            }
        }
    }
    public String getURLFile() throws IOException {
        String fileName = url.getFile();
        if ((fileName == null) || (fileName.length() == 0))
            fileName = "/";
        if (usingProxy && !proxyDisabled) {
            StringBuffer result = new StringBuffer(128);
            result.append(url.getProtocol());
            result.append(":");
            if (url.getAuthority() != null && url.getAuthority().length() > 0) {
                result.append("
                result.append(url.getAuthority());
            }
            if (url.getPath() != null) {
                result.append(url.getPath());
            }
            if (url.getQuery() != null) {
                result.append('?');
                result.append(url.getQuery());
            }
            fileName =  result.toString();
        }
        if (fileName.indexOf('\n') == -1)
            return fileName;
        else
            throw new java.net.MalformedURLException("Illegal character in URL");
    }
    @Deprecated
    public void writeRequests(MessageHeader head) {
        requests = head;
        requests.print(serverOutput);
        serverOutput.flush();
    }
    public void writeRequests(MessageHeader head,
                              PosterOutputStream pos) throws IOException {
        requests = head;
        requests.print(serverOutput);
        poster = pos;
        if (poster != null)
            poster.writeTo(serverOutput);
        serverOutput.flush();
    }
    public void writeRequests(MessageHeader head,
                              PosterOutputStream pos,
                              boolean streaming) throws IOException {
        this.streaming = streaming;
        writeRequests(head, pos);
    }
    public boolean parseHTTP(MessageHeader responses, ProgressSource pi, HttpURLConnection httpuc)
    throws IOException {
        try {
            serverInput = serverSocket.getInputStream();
            if (capture != null) {
                serverInput = new HttpCaptureInputStream(serverInput, capture);
            }
            serverInput = new BufferedInputStream(serverInput);
            return (parseHTTPHeader(responses, pi, httpuc));
        } catch (SocketTimeoutException stex) {
            if (ignoreContinue) {
                closeServer();
            }
            throw stex;
        } catch (IOException e) {
            closeServer();
            cachedHttpClient = false;
            if (!failedOnce && requests != null) {
                failedOnce = true;
                if (httpuc.getRequestMethod().equals("POST") && (!retryPostProp || streaming)) {
                }  else {
                    openServer();
                    if (needsTunneling()) {
                        httpuc.doTunneling();
                    }
                    afterConnect();
                    writeRequests(requests, poster);
                    return parseHTTP(responses, pi, httpuc);
                }
            }
            throw e;
        }
    }
    private boolean parseHTTPHeader(MessageHeader responses, ProgressSource pi, HttpURLConnection httpuc)
    throws IOException {
        keepAliveConnections = -1;
        keepAliveTimeout = 0;
        boolean ret = false;
        byte[] b = new byte[8];
        try {
            int nread = 0;
            serverInput.mark(10);
            while (nread < 8) {
                int r = serverInput.read(b, nread, 8 - nread);
                if (r < 0) {
                    break;
                }
                nread += r;
            }
            String keep=null;
            ret = b[0] == 'H' && b[1] == 'T'
                    && b[2] == 'T' && b[3] == 'P' && b[4] == '/' &&
                b[5] == '1' && b[6] == '.';
            serverInput.reset();
            if (ret) { 
                responses.parseHeader(serverInput);
                if (cookieHandler != null) {
                    URI uri = ParseUtil.toURI(url);
                    if (uri != null)
                        cookieHandler.put(uri, responses.getHeaders());
                }
                if (usingProxy) { 
                    keep = responses.findValue("Proxy-Connection");
                }
                if (keep == null) {
                    keep = responses.findValue("Connection");
                }
                if (keep != null && keep.toLowerCase(Locale.US).equals("keep-alive")) {
                    HeaderParser p = new HeaderParser(
                            responses.findValue("Keep-Alive"));
                    if (p != null) {
                        keepAliveConnections = p.findInt("max", usingProxy?50:5);
                        keepAliveTimeout = p.findInt("timeout", usingProxy?60:5);
                    }
                } else if (b[7] != '0') {
                    if (keep != null) {
                        keepAliveConnections = 1;
                    } else {
                        keepAliveConnections = 5;
                    }
                }
            } else if (nread != 8) {
                if (!failedOnce && requests != null) {
                    failedOnce = true;
                    if (httpuc.getRequestMethod().equals("POST") && (!retryPostProp || streaming)) {
                    } else {
                        closeServer();
                        cachedHttpClient = false;
                        openServer();
                        if (needsTunneling()) {
                            httpuc.doTunneling();
                        }
                        afterConnect();
                        writeRequests(requests, poster);
                        return parseHTTP(responses, pi, httpuc);
                    }
                }
                throw new SocketException("Unexpected end of file from server");
            } else {
                responses.set("Content-type", "unknown/unknown");
            }
        } catch (IOException e) {
            throw e;
        }
        int code = -1;
        try {
            String resp;
            resp = responses.getValue(0);
            int ind;
            ind = resp.indexOf(' ');
            while(resp.charAt(ind) == ' ')
                ind++;
            code = Integer.parseInt(resp.substring(ind, ind + 3));
        } catch (Exception e) {}
        if (code == HTTP_CONTINUE && ignoreContinue) {
            responses.reset();
            return parseHTTPHeader(responses, pi, httpuc);
        }
        long cl = -1;
        String te = responses.findValue("Transfer-Encoding");
        if (te != null && te.equalsIgnoreCase("chunked")) {
            serverInput = new ChunkedInputStream(serverInput, this, responses);
            if (keepAliveConnections <= 1) {
                keepAliveConnections = 1;
                keepingAlive = false;
            } else {
                keepingAlive = true;
            }
            failedOnce = false;
        } else {
            String cls = responses.findValue("content-length");
            if (cls != null) {
                try {
                    cl = Long.parseLong(cls);
                } catch (NumberFormatException e) {
                    cl = -1;
                }
            }
            String requestLine = requests.getKey(0);
            if ((requestLine != null &&
                 (requestLine.startsWith("HEAD"))) ||
                code == HttpURLConnection.HTTP_NOT_MODIFIED ||
                code == HttpURLConnection.HTTP_NO_CONTENT) {
                cl = 0;
            }
            if (keepAliveConnections > 1 &&
                (cl >= 0 ||
                 code == HttpURLConnection.HTTP_NOT_MODIFIED ||
                 code == HttpURLConnection.HTTP_NO_CONTENT)) {
                keepingAlive = true;
                failedOnce = false;
            } else if (keepingAlive) {
                keepingAlive=false;
            }
        }
        if (cl > 0) {
            if (pi != null) {
                pi.setContentType(responses.findValue("content-type"));
            }
            if (isKeepingAlive())   {
                PlatformLogger logger = HttpURLConnection.getHttpLogger();
                if (logger.isLoggable(PlatformLogger.FINEST)) {
                    logger.finest("KeepAlive stream used: " + url);
                }
                serverInput = new KeepAliveStream(serverInput, pi, cl, this);
                failedOnce = false;
            }
            else        {
                serverInput = new MeteredStream(serverInput, pi, cl);
            }
        }
        else if (cl == -1)  {
            if (pi != null) {
                pi.setContentType(responses.findValue("content-type"));
                serverInput = new MeteredStream(serverInput, pi, cl);
            }
            else    {
            }
        }
        else    {
            if (pi != null)
                pi.finishTracking();
        }
        return ret;
    }
    public synchronized InputStream getInputStream() {
        return serverInput;
    }
    public OutputStream getOutputStream() {
        return serverOutput;
    }
    @Override
    public String toString() {
        return getClass().getName()+"("+url+")";
    }
    public final boolean isKeepingAlive() {
        return getHttpKeepAliveSet() && keepingAlive;
    }
    public void setCacheRequest(CacheRequest cacheRequest) {
        this.cacheRequest = cacheRequest;
    }
    CacheRequest getCacheRequest() {
        return cacheRequest;
    }
    @Override
    protected void finalize() throws Throwable {
    }
    public void setDoNotRetry(boolean value) {
        failedOnce = value;
    }
    public void setIgnoreContinue(boolean value) {
        ignoreContinue = value;
    }
    @Override
    public void closeServer() {
        try {
            keepingAlive = false;
            serverSocket.close();
        } catch (Exception e) {}
    }
    public String getProxyHostUsed() {
        if (!usingProxy) {
            return null;
        } else {
            return ((InetSocketAddress)proxy.address()).getHostString();
        }
    }
    public int getProxyPortUsed() {
        if (usingProxy)
            return ((InetSocketAddress)proxy.address()).getPort();
        return -1;
    }
}
