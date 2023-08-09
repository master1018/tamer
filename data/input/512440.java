public abstract class HttpURLConnection extends URLConnection {
    @SuppressWarnings("nls")
    private String methodTokens[] = { "GET", "DELETE", "HEAD", "OPTIONS",
            "POST", "PUT", "TRACE" };
    protected String method = "GET"; 
    protected int responseCode = -1;
    protected String responseMessage;
    protected boolean instanceFollowRedirects = followRedirects;
    private static boolean followRedirects = true;
    protected int chunkLength = -1;
    protected int fixedContentLength = -1;
    private final static int DEFAULT_CHUNK_LENGTH = 1024;
    public final static int HTTP_ACCEPTED = 202;
    public final static int HTTP_BAD_GATEWAY = 502;
    public final static int HTTP_BAD_METHOD = 405;
    public final static int HTTP_BAD_REQUEST = 400;
    public final static int HTTP_CLIENT_TIMEOUT = 408;
    public final static int HTTP_CONFLICT = 409;
    public final static int HTTP_CREATED = 201;
    public final static int HTTP_ENTITY_TOO_LARGE = 413;
    public final static int HTTP_FORBIDDEN = 403;
    public final static int HTTP_GATEWAY_TIMEOUT = 504;
    public final static int HTTP_GONE = 410;
    public final static int HTTP_INTERNAL_ERROR = 500;
    public final static int HTTP_LENGTH_REQUIRED = 411;
    public final static int HTTP_MOVED_PERM = 301;
    public final static int HTTP_MOVED_TEMP = 302;
    public final static int HTTP_MULT_CHOICE = 300;
    public final static int HTTP_NO_CONTENT = 204;
    public final static int HTTP_NOT_ACCEPTABLE = 406;
    public final static int HTTP_NOT_AUTHORITATIVE = 203;
    public final static int HTTP_NOT_FOUND = 404;
    public final static int HTTP_NOT_IMPLEMENTED = 501;
    public final static int HTTP_NOT_MODIFIED = 304;
    public final static int HTTP_OK = 200;
    public final static int HTTP_PARTIAL = 206;
    public final static int HTTP_PAYMENT_REQUIRED = 402;
    public final static int HTTP_PRECON_FAILED = 412;
    public final static int HTTP_PROXY_AUTH = 407;
    public final static int HTTP_REQ_TOO_LONG = 414;
    public final static int HTTP_RESET = 205;
    public final static int HTTP_SEE_OTHER = 303;
    @Deprecated
    public final static int HTTP_SERVER_ERROR = 500;
    public final static int HTTP_USE_PROXY = 305;
    public final static int HTTP_UNAUTHORIZED = 401;
    public final static int HTTP_UNSUPPORTED_TYPE = 415;
    public final static int HTTP_UNAVAILABLE = 503;
    public final static int HTTP_VERSION = 505;
    protected HttpURLConnection(URL url) {
        super(url);
    }
    public abstract void disconnect();
    public java.io.InputStream getErrorStream() {
        return null;
    }
    public static boolean getFollowRedirects() {
        return followRedirects;
    }
    @Override
    public java.security.Permission getPermission() throws IOException {
        int port = url.getPort();
        if (port < 0) {
            port = 80;
        }
        return new SocketPermission(url.getHost() + ":" + port, 
                "connect, resolve"); 
    }
    public String getRequestMethod() {
        return method;
    }
    public int getResponseCode() throws IOException {
        getInputStream();
        String response = getHeaderField(0);
        if (response == null) {
            return -1;
        }
        response = response.trim();
        int mark = response.indexOf(" ") + 1; 
        if (mark == 0) {
            return -1;
        }
        int last = mark + 3;
        if (last > response.length()) {
            last = response.length();
        }
        responseCode = Integer.parseInt(response.substring(mark, last));
        if (last + 1 <= response.length()) {
            responseMessage = response.substring(last + 1);
        }
        return responseCode;
    }
    public String getResponseMessage() throws IOException {
        if (responseMessage != null) {
            return responseMessage;
        }
        getResponseCode();
        return responseMessage;
    }
    public static void setFollowRedirects(boolean auto) {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkSetFactory();
        }
        followRedirects = auto;
    }
    public void setRequestMethod(String method) throws ProtocolException {
        if (connected) {
            throw new ProtocolException(Msg.getString("K0037")); 
        }
        for (int i = 0; i < methodTokens.length; i++) {
            if (methodTokens[i].equals(method)) {
                this.method = methodTokens[i];
                return;
            }
        }
        throw new ProtocolException();
    }
    public abstract boolean usingProxy();
    public boolean getInstanceFollowRedirects() {
        return instanceFollowRedirects;
    }
    public void setInstanceFollowRedirects(boolean followRedirects) {
        instanceFollowRedirects = followRedirects;
    }
    @Override
    public long getHeaderFieldDate(String field, long defaultValue) {
        return super.getHeaderFieldDate(field, defaultValue);
    }
    public void setFixedLengthStreamingMode(int contentLength) {
        if (super.connected) {
            throw new IllegalStateException(Msg.getString("K0079")); 
        }
        if (0 < chunkLength) {
            throw new IllegalStateException(Msg.getString("KA003")); 
        }
        if (0 > contentLength) {
            throw new IllegalArgumentException(Msg.getString("K0051")); 
        }
        this.fixedContentLength = contentLength;
    }
    public void setChunkedStreamingMode(int chunklen) {
        if (super.connected) {
            throw new IllegalStateException(Msg.getString("K0079")); 
        }
        if (0 <= fixedContentLength) {
            throw new IllegalStateException(Msg.getString("KA003")); 
        }
        if (0 >= chunklen) {
            chunkLength = DEFAULT_CHUNK_LENGTH;
        } else {
            chunkLength = chunklen;
        }
    }
}
