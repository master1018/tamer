public class CtsTestServer {
    private static final String TAG = "CtsTestServer";
    private static final int SERVER_PORT = 4444;
    private static final int SSL_SERVER_PORT = 4445;
    public static final String FAVICON_PATH = "/favicon.ico";
    public static final String USERAGENT_PATH = "/useragent.html";
    public static final String ASSET_PREFIX = "/assets/";
    public static final String FAVICON_ASSET_PATH = ASSET_PREFIX + "webkit/favicon.png";
    public static final String REDIRECT_PREFIX = "/redirect";
    public static final String DELAY_PREFIX = "/delayed";
    public static final String BINARY_PREFIX = "/binary";
    public static final String COOKIE_PREFIX = "/cookie";
    public static final String AUTH_PREFIX = "/auth";
    public static final String SHUTDOWN_PREFIX = "/shutdown";
    public static final int DELAY_MILLIS = 2000;
    public static final String AUTH_REALM = "Android CTS";
    public static final String AUTH_USER = "cts";
    public static final String AUTH_PASS = "secret";
    public static final String AUTH_CREDENTIALS = "Basic Y3RzOnNlY3JldA==";
    public static final String MESSAGE_401 = "401 unauthorized";
    public static final String MESSAGE_403 = "403 forbidden";
    public static final String MESSAGE_404 = "404 not found";
    private static CtsTestServer sInstance;
    private static Hashtable<Integer, String> sReasons;
    private ServerThread mServerThread;
    private String mServerUri;
    private AssetManager mAssets;
    private Context mContext;
    private boolean mSsl;
    private MimeTypeMap mMap;
    private String mLastQuery;
    private int mRequestCount;
    private long mDocValidity;
    private long mDocAge;
    public CtsTestServer(Context context) throws Exception {
        this(context, false);
    }
    public static String getReasonString(int status) {
        if (sReasons == null) {
            sReasons = new Hashtable<Integer, String>();
            sReasons.put(HttpStatus.SC_UNAUTHORIZED, "Unauthorized");
            sReasons.put(HttpStatus.SC_NOT_FOUND, "Not Found");
            sReasons.put(HttpStatus.SC_FORBIDDEN, "Forbidden");
            sReasons.put(HttpStatus.SC_MOVED_TEMPORARILY, "Moved Temporarily");
        }
        return sReasons.get(status);
    }
    public CtsTestServer(Context context, boolean ssl) throws Exception {
        if (sInstance != null) {
            sInstance.shutdown();
        }
        sInstance = this;
        mContext = context;
        mAssets = mContext.getAssets();
        mSsl = ssl;
        if (mSsl) {
            mServerUri = "https:
        } else {
            mServerUri = "http:
        }
        mMap = MimeTypeMap.getSingleton();
        mServerThread = new ServerThread(this, mSsl);
        mServerThread.start();
    }
    public void shutdown() {
        try {
            URL url = new URL(mServerUri + SHUTDOWN_PREFIX);
            URLConnection connection = openConnection(url);
            connection.connect();
            InputStream is = connection.getInputStream();
            is.close();
            mServerThread.join();
        } catch (MalformedURLException e) {
            throw new IllegalStateException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        } catch (KeyManagementException e) {
            throw new IllegalStateException(e);
        }
        sInstance = null;
    }
    private URLConnection openConnection(URL url)
            throws IOException, NoSuchAlgorithmException, KeyManagementException {
        if (mSsl) {
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setHostnameVerifier(new CtsHostnameVerifier());
            SSLContext context = SSLContext.getInstance("TLS");
            CtsTrustManager trustManager = new CtsTrustManager();
            context.init(null, new CtsTrustManager[] {trustManager}, null);
            connection.setSSLSocketFactory(context.getSocketFactory());
            return connection;
        } else {
            return url.openConnection();
        }
    }
    private static class CtsTrustManager implements X509TrustManager {
        public void checkClientTrusted(X509Certificate[] chain, String authType) {
        }
        public void checkServerTrusted(X509Certificate[] chain, String authType) {
        }
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    }
    private static class CtsHostnameVerifier implements HostnameVerifier {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }
    public String getBaseUri() {
        return mServerUri;
    }
    public String getAssetUrl(String path) {
        StringBuilder sb = new StringBuilder(getBaseUri());
        sb.append(ASSET_PREFIX);
        sb.append(path);
        return sb.toString();
    }
    public String getDelayedAssetUrl(String path) {
        StringBuilder sb = new StringBuilder(getBaseUri());
        sb.append(DELAY_PREFIX);
        sb.append(ASSET_PREFIX);
        sb.append(path);
        return sb.toString();
    }
    public String getAuthAssetUrl(String path) {
        StringBuilder sb = new StringBuilder(getBaseUri());
        sb.append(AUTH_PREFIX);
        sb.append(ASSET_PREFIX);
        sb.append(path);
        return sb.toString();
    }
    public String getRedirectingAssetUrl(String path) {
        return getRedirectingAssetUrl(path, 1);
    }
    public String getRedirectingAssetUrl(String path, int numRedirects) {
        StringBuilder sb = new StringBuilder(getBaseUri());
        for (int i = 0; i < numRedirects; i++) {
            sb.append(REDIRECT_PREFIX);
        }
        sb.append(ASSET_PREFIX);
        sb.append(path);
        return sb.toString();
    }
    public String getBinaryUrl(String mimeType, int contentLength) {
        StringBuilder sb = new StringBuilder(getBaseUri());
        sb.append(BINARY_PREFIX);
        sb.append("?type=");
        sb.append(mimeType);
        sb.append("&length=");
        sb.append(contentLength);
        return sb.toString();
    }
    public String getCookieUrl(String path) {
        StringBuilder sb = new StringBuilder(getBaseUri());
        sb.append(COOKIE_PREFIX);
        sb.append("/");
        sb.append(path);
        return sb.toString();
    }
    public String getUserAgentUrl() {
        StringBuilder sb = new StringBuilder(getBaseUri());
        sb.append(USERAGENT_PATH);
        return sb.toString();
    }
    public String getLastRequestUrl() {
        return mLastQuery;
    }
    public int getRequestCount() {
        return mRequestCount;
    }
    public void setDocumentValidity(long timeMillis) {
        mDocValidity = timeMillis;
    }
    public void setDocumentAge(long timeMillis) {
        mDocAge = timeMillis;
    }
    private HttpResponse getResponse(HttpRequest request) throws InterruptedException {
        RequestLine requestLine = request.getRequestLine();
        HttpResponse response = null;
        mRequestCount += 1;
        Log.i(TAG, requestLine.getMethod() + ": " + requestLine.getUri());
        String uriString = requestLine.getUri();
        mLastQuery = uriString;
        URI uri = URI.create(uriString);
        String path = uri.getPath();
        if (path.equals(FAVICON_PATH)) {
            path = FAVICON_ASSET_PATH;
        }
        if (path.startsWith(DELAY_PREFIX)) {
            try {
                Thread.sleep(DELAY_MILLIS);
            } catch (InterruptedException ignored) {
            }
            path = path.substring(DELAY_PREFIX.length());
        }
        if (path.startsWith(AUTH_PREFIX)) {
            Header[] auth = request.getHeaders("Authorization");
            if (auth.length > 0) {
                if (auth[0].getValue().equals(AUTH_CREDENTIALS)) {
                    path = path.substring(AUTH_PREFIX.length());
                } else {
                    response = createResponse(HttpStatus.SC_FORBIDDEN);
                }
            } else {
                response = createResponse(HttpStatus.SC_UNAUTHORIZED);
                response.addHeader("WWW-Authenticate", "Basic realm=\"" + AUTH_REALM + "\"");
            }
        }
        if (path.startsWith(BINARY_PREFIX)) {
            List <NameValuePair> args = URLEncodedUtils.parse(uri, "UTF-8");
            int length = 0;
            String mimeType = null;
            try {
                for (NameValuePair pair : args) {
                    String name = pair.getName();
                    if (name.equals("type")) {
                        mimeType = pair.getValue();
                    } else if (name.equals("length")) {
                        length = Integer.parseInt(pair.getValue());
                    }
                }
                if (length > 0 && mimeType != null) {
                    ByteArrayEntity entity = new ByteArrayEntity(new byte[length]);
                    entity.setContentType(mimeType);
                    response = createResponse(HttpStatus.SC_OK);
                    response.setEntity(entity);
                    response.addHeader("Content-Disposition", "attachment; filename=test.bin");
                } else {
                }
            } catch (Exception e) {
                Log.w(TAG, e);
            }
        } else if (path.startsWith(ASSET_PREFIX)) {
            path = path.substring(ASSET_PREFIX.length());
            try {
                InputStream in = mAssets.open(path);
                response = createResponse(HttpStatus.SC_OK);
                InputStreamEntity entity = new InputStreamEntity(in, in.available());
                String mimeType =
                    mMap.getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(path));
                if (mimeType == null) {
                    mimeType = "text/html";
                }
                entity.setContentType(mimeType);
                response.setEntity(entity);
            } catch (IOException e) {
                response = null;
            }
        } else if (path.startsWith(REDIRECT_PREFIX)) {
            response = createResponse(HttpStatus.SC_MOVED_TEMPORARILY);
            String location = getBaseUri() + path.substring(REDIRECT_PREFIX.length());
            Log.i(TAG, "Redirecting to: " + location);
            response.addHeader("Location", location);
        } else if (path.startsWith(COOKIE_PREFIX)) {
            response = createResponse(HttpStatus.SC_OK);
            Header[] cookies = request.getHeaders("Cookie");
            Pattern p = Pattern.compile("count=(\\d+)");
            StringBuilder cookieString = new StringBuilder(100);
            int count = 0;
            for (Header cookie : cookies) {
                String value = cookie.getValue();
                if (cookieString.length() > 0) {
                    cookieString.append("|");
                }
                cookieString.append(value);
                Matcher m = p.matcher(value);
                if (m.find()) {
                    count = Integer.parseInt(m.group(1)) + 1;
                }
            }
            response.addHeader("Set-Cookie", "count=" + count + "; path=" + COOKIE_PREFIX);
            response.setEntity(createEntity("<html><head><title>" + cookieString +
                    "</title></head><body>" + cookieString + "</body></html>"));
        } else if (path.equals(USERAGENT_PATH)) {
            response = createResponse(HttpStatus.SC_OK);
            Header agentHeader = request.getFirstHeader("User-Agent");
            String agent = "";
            if (agentHeader != null) {
                agent = agentHeader.getValue();
            }
            response.setEntity(createEntity("<html><head><title>" + agent + "</title></head>" +
                    "<body>" + agent + "</body></html>"));
        } else if (path.equals(SHUTDOWN_PREFIX)) {
            response = createResponse(HttpStatus.SC_OK);
        }
        if (response == null) {
            response = createResponse(HttpStatus.SC_NOT_FOUND);
        }
        StatusLine sl = response.getStatusLine();
        Log.i(TAG, sl.getStatusCode() + "(" + sl.getReasonPhrase() + ")");
        setDateHeaders(response);
        return response;
    }
    private void setDateHeaders(HttpResponse response) {
        long time = System.currentTimeMillis();
        if (mDocValidity != 0) {
            String expires =
                    DateUtils.formatDate(new Date(time + mDocValidity), DateUtils.PATTERN_RFC1123);
            response.addHeader("Expires", expires);
        }
        if (mDocAge != 0) {
            String modified =
                    DateUtils.formatDate(new Date(time - mDocAge), DateUtils.PATTERN_RFC1123);
            response.addHeader("Last-Modified", modified);
        }
        response.addHeader("Date", DateUtils.formatDate(new Date(), DateUtils.PATTERN_RFC1123));
    }
    private HttpResponse createResponse(int status) {
        HttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_0, status, null);
        String reason = getReasonString(status);
        if (reason != null) {
            StringBuffer buf = new StringBuffer("<html><head><title>");
            buf.append(reason);
            buf.append("</title></head><body>");
            buf.append(reason);
            buf.append("</body></html>");
            response.setEntity(createEntity(buf.toString()));
        }
        return response;
    }
    private StringEntity createEntity(String content) {
        try {
            StringEntity entity = new StringEntity(content);
            entity.setContentType("text/html");
            return entity;
        } catch (UnsupportedEncodingException e) {
            Log.w(TAG, e);
        }
        return null;
    }
    private static class ServerThread extends Thread {
        private CtsTestServer mServer;
        private ServerSocket mSocket;
        private boolean mIsSsl;
        private boolean mIsCancelled;
        private SSLContext mSslContext;
        private static final String SERVER_KEYS_BKS =
            "AAAAAQAAABQDkebzoP1XwqyWKRCJEpn/t8dqIQAABDkEAAVteWtleQAAARpYl20nAAAAAQAFWC41" +
            "MDkAAAJNMIICSTCCAbKgAwIBAgIESEfU1jANBgkqhkiG9w0BAQUFADBpMQswCQYDVQQGEwJVUzET" +
            "MBEGA1UECBMKQ2FsaWZvcm5pYTEMMAoGA1UEBxMDTVRWMQ8wDQYDVQQKEwZHb29nbGUxEDAOBgNV" +
            "BAsTB0FuZHJvaWQxFDASBgNVBAMTC1Rlc3QgU2VydmVyMB4XDTA4MDYwNTExNTgxNFoXDTA4MDkw" +
            "MzExNTgxNFowaTELMAkGA1UEBhMCVVMxEzARBgNVBAgTCkNhbGlmb3JuaWExDDAKBgNVBAcTA01U" +
            "VjEPMA0GA1UEChMGR29vZ2xlMRAwDgYDVQQLEwdBbmRyb2lkMRQwEgYDVQQDEwtUZXN0IFNlcnZl" +
            "cjCBnzANBgkqhkiG9w0BAQEFAAOBjQAwgYkCgYEA0LIdKaIr9/vsTq8BZlA3R+NFWRaH4lGsTAQy" +
            "DPMF9ZqEDOaL6DJuu0colSBBBQ85hQTPa9m9nyJoN3pEi1hgamqOvQIWcXBk+SOpUGRZZFXwniJV" +
            "zDKU5nE9MYgn2B9AoiH3CSuMz6HRqgVaqtppIe1jhukMc/kHVJvlKRNy9XMCAwEAATANBgkqhkiG" +
            "9w0BAQUFAAOBgQC7yBmJ9O/eWDGtSH9BH0R3dh2NdST3W9hNZ8hIa8U8klhNHbUCSSktZmZkvbPU" +
            "hse5LI3dh6RyNDuqDrbYwcqzKbFJaq/jX9kCoeb3vgbQElMRX8D2ID1vRjxwlALFISrtaN4VpWzV" +
            "yeoHPW4xldeZmoVtjn8zXNzQhLuBqX2MmAAAAqwAAAAUvkUScfw9yCSmALruURNmtBai7kQAAAZx" +
            "4Jmijxs/l8EBaleaUru6EOPioWkUAEVWCxjM/TxbGHOi2VMsQWqRr/DZ3wsDmtQgw3QTrUK666sR" +
            "MBnbqdnyCyvM1J2V1xxLXPUeRBmR2CXorYGF9Dye7NkgVdfA+9g9L/0Au6Ugn+2Cj5leoIgkgApN" +
            "vuEcZegFlNOUPVEs3SlBgUF1BY6OBM0UBHTPwGGxFBBcetcuMRbUnu65vyDG0pslT59qpaR0TMVs" +
            "P+tcheEzhyjbfM32/vwhnL9dBEgM8qMt0sqF6itNOQU/F4WGkK2Cm2v4CYEyKYw325fEhzTXosck" +
            "MhbqmcyLab8EPceWF3dweoUT76+jEZx8lV2dapR+CmczQI43tV9btsd1xiBbBHAKvymm9Ep9bPzM" +
            "J0MQi+OtURL9Lxke/70/MRueqbPeUlOaGvANTmXQD2OnW7PISwJ9lpeLfTG0LcqkoqkbtLKQLYHI" +
            "rQfV5j0j+wmvmpMxzjN3uvNajLa4zQ8l0Eok9SFaRr2RL0gN8Q2JegfOL4pUiHPsh64WWya2NB7f" +
            "V+1s65eA5ospXYsShRjo046QhGTmymwXXzdzuxu8IlnTEont6P4+J+GsWk6cldGbl20hctuUKzyx" +
            "OptjEPOKejV60iDCYGmHbCWAzQ8h5MILV82IclzNViZmzAapeeCnexhpXhWTs+xDEYSKEiG/camt" +
            "bhmZc3BcyVJrW23PktSfpBQ6D8ZxoMfF0L7V2GQMaUg+3r7ucrx82kpqotjv0xHghNIm95aBr1Qw" +
            "1gaEjsC/0wGmmBDg1dTDH+F1p9TInzr3EFuYD0YiQ7YlAHq3cPuyGoLXJ5dXYuSBfhDXJSeddUkl" +
            "k1ufZyOOcskeInQge7jzaRfmKg3U94r+spMEvb0AzDQVOKvjjo1ivxMSgFRZaDb/4qw=";
        private String PASSWORD = "android";
        private KeyManager[] getKeyManagers() throws Exception {
            byte[] bytes = Base64.decode(SERVER_KEYS_BKS.getBytes());
            InputStream inputStream = new ByteArrayInputStream(bytes);
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(inputStream, PASSWORD.toCharArray());
            inputStream.close();
            String algorithm = KeyManagerFactory.getDefaultAlgorithm();
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(algorithm);
            keyManagerFactory.init(keyStore, PASSWORD.toCharArray());
            return keyManagerFactory.getKeyManagers();
        }
        public ServerThread(CtsTestServer server, boolean ssl) throws Exception {
            super("ServerThread");
            mServer = server;
            mIsSsl = ssl;
            int retry = 3;
            while (true) {
                try {
                    if (mIsSsl) {
                        mSslContext = SSLContext.getInstance("TLS");
                        mSslContext.init(getKeyManagers(), null, null);
                        mSocket = mSslContext.getServerSocketFactory().createServerSocket(
                                SSL_SERVER_PORT);
                    } else {
                        mSocket = new ServerSocket(SERVER_PORT);
                    }
                    return;
                } catch (IOException e) {
                    Log.w(TAG, e);
                    if (--retry == 0) {
                        throw e;
                    }
                    Thread.sleep(1000);
                }
            }
        }
        public void run() {
            HttpParams params = new BasicHttpParams();
            params.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_0);
            while (!mIsCancelled) {
                try {
                    Socket socket = mSocket.accept();
                    DefaultHttpServerConnection conn = new DefaultHttpServerConnection();
                    conn.bind(socket, params);
                    HttpRequest request = conn.receiveRequestHeader();
                    if (isShutdownRequest(request)) {
                        mIsCancelled = true;
                    }
                    HttpResponse response = mServer.getResponse(request);
                    conn.sendResponseHeader(response);
                    conn.sendResponseEntity(response);
                    conn.close();
                } catch (IOException e) {
                    Log.w(TAG, e);
                } catch (HttpException e) {
                    Log.w(TAG, e);
                } catch (InterruptedException e) {
                    Log.w(TAG, e);
                } catch (UnsupportedOperationException e) {
                    Log.w(TAG, e);
                }
            }
            try {
                mSocket.close();
            } catch (IOException ignored) {
            }
        }
        private boolean isShutdownRequest(HttpRequest request) {
            RequestLine requestLine = request.getRequestLine();
            String uriString = requestLine.getUri();
            URI uri = URI.create(uriString);
            String path = uri.getPath();
            return path.equals(SHUTDOWN_PREFIX);
        }
    }
}
