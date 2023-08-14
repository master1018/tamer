public class RequestHandle {
    private String        mUrl;
    private WebAddress    mUri;
    private String        mMethod;
    private Map<String, String> mHeaders;
    private RequestQueue  mRequestQueue;
    private Request       mRequest;
    private InputStream   mBodyProvider;
    private int           mBodyLength;
    private int           mRedirectCount = 0;
    private Connection    mConnection;
    private final static String AUTHORIZATION_HEADER = "Authorization";
    private final static String PROXY_AUTHORIZATION_HEADER = "Proxy-Authorization";
    public final static int MAX_REDIRECT_COUNT = 16;
    public RequestHandle(RequestQueue requestQueue, String url, WebAddress uri,
            String method, Map<String, String> headers,
            InputStream bodyProvider, int bodyLength, Request request) {
        if (headers == null) {
            headers = new HashMap<String, String>();
        }
        mHeaders = headers;
        mBodyProvider = bodyProvider;
        mBodyLength = bodyLength;
        mMethod = method == null? "GET" : method;
        mUrl = url;
        mUri = uri;
        mRequestQueue = requestQueue;
        mRequest = request;
    }
    public RequestHandle(RequestQueue requestQueue, String url, WebAddress uri,
            String method, Map<String, String> headers,
            InputStream bodyProvider, int bodyLength, Request request,
            Connection conn) {
        this(requestQueue, url, uri, method, headers, bodyProvider, bodyLength,
                request);
        mConnection = conn;
    }
    public void cancel() {
        if (mRequest != null) {
            mRequest.cancel();
        }
    }
    public void pauseRequest(boolean pause) {
        if (mRequest != null) {
            mRequest.setLoadingPaused(pause);
        }
    }
    public void handleSslErrorResponse(boolean proceed) {
        if (mRequest != null) {
            mRequest.handleSslErrorResponse(proceed);
        }
    }
    public boolean isRedirectMax() {
        return mRedirectCount >= MAX_REDIRECT_COUNT;
    }
    public int getRedirectCount() {
        return mRedirectCount;
    }
    public void setRedirectCount(int count) {
        mRedirectCount = count;
    }
    public boolean setupRedirect(String redirectTo, int statusCode,
            Map<String, String> cacheHeaders) {
        if (HttpLog.LOGV) {
            HttpLog.v("RequestHandle.setupRedirect(): redirectCount " +
                  mRedirectCount);
        }
        mHeaders.remove(AUTHORIZATION_HEADER);
        mHeaders.remove(PROXY_AUTHORIZATION_HEADER);
        if (++mRedirectCount == MAX_REDIRECT_COUNT) {
            if (HttpLog.LOGV) HttpLog.v(
                    "RequestHandle.setupRedirect(): too many redirects " +
                    mRequest);
            mRequest.error(EventHandler.ERROR_REDIRECT_LOOP,
                           com.android.internal.R.string.httpErrorRedirectLoop);
            return false;
        }
        if (mUrl.startsWith("https:") && redirectTo.startsWith("http:")) {
            if (HttpLog.LOGV) {
                HttpLog.v("blowing away the referer on an https -> http redirect");
            }
            mHeaders.remove("Referer");
        }
        mUrl = redirectTo;
        try {
            mUri = new WebAddress(mUrl);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        mHeaders.remove("Cookie");
        String cookie = CookieManager.getInstance().getCookie(mUri);
        if (cookie != null && cookie.length() > 0) {
            mHeaders.put("Cookie", cookie);
        }
        if ((statusCode == 302 || statusCode == 303) && mMethod.equals("POST")) {
            if (HttpLog.LOGV) {
                HttpLog.v("replacing POST with GET on redirect to " + redirectTo);
            }
            mMethod = "GET";
        }
        if (statusCode == 307) {
            try {
                if (mBodyProvider != null) mBodyProvider.reset();
            } catch (java.io.IOException ex) {
                if (HttpLog.LOGV) {
                    HttpLog.v("setupRedirect() failed to reset body provider");
                }
                return false;
            }
        } else {
            mHeaders.remove("Content-Type");
            mBodyProvider = null;
        }
        mHeaders.putAll(cacheHeaders);
        createAndQueueNewRequest();
        return true;
    }
    public void setupBasicAuthResponse(boolean isProxy, String username, String password) {
        String response = computeBasicAuthResponse(username, password);
        if (HttpLog.LOGV) {
            HttpLog.v("setupBasicAuthResponse(): response: " + response);
        }
        mHeaders.put(authorizationHeader(isProxy), "Basic " + response);
        setupAuthResponse();
    }
    public void setupDigestAuthResponse(boolean isProxy,
                                        String username,
                                        String password,
                                        String realm,
                                        String nonce,
                                        String QOP,
                                        String algorithm,
                                        String opaque) {
        String response = computeDigestAuthResponse(
                username, password, realm, nonce, QOP, algorithm, opaque);
        if (HttpLog.LOGV) {
            HttpLog.v("setupDigestAuthResponse(): response: " + response);
        }
        mHeaders.put(authorizationHeader(isProxy), "Digest " + response);
        setupAuthResponse();
    }
    private void setupAuthResponse() {
        try {
            if (mBodyProvider != null) mBodyProvider.reset();
        } catch (java.io.IOException ex) {
            if (HttpLog.LOGV) {
                HttpLog.v("setupAuthResponse() failed to reset body provider");
            }
        }
        createAndQueueNewRequest();
    }
    public String getMethod() {
        return mMethod;
    }
    public static String computeBasicAuthResponse(String username, String password) {
        Assert.assertNotNull(username);
        Assert.assertNotNull(password);
        return new String(Base64.encodeBase64((username + ':' + password).getBytes()));
    }
    public void waitUntilComplete() {
        mRequest.waitUntilComplete();
    }
    public void processRequest() {
        if (mConnection != null) {
            mConnection.processRequests(mRequest);
        }
    }
    private String computeDigestAuthResponse(String username,
                                             String password,
                                             String realm,
                                             String nonce,
                                             String QOP,
                                             String algorithm,
                                             String opaque) {
        Assert.assertNotNull(username);
        Assert.assertNotNull(password);
        Assert.assertNotNull(realm);
        String A1 = username + ":" + realm + ":" + password;
        String A2 = mMethod  + ":" + mUrl;
        String nc = "000001";
        String cnonce = computeCnonce();
        String digest = computeDigest(A1, A2, nonce, QOP, nc, cnonce);
        String response = "";
        response += "username=" + doubleQuote(username) + ", ";
        response += "realm="    + doubleQuote(realm)    + ", ";
        response += "nonce="    + doubleQuote(nonce)    + ", ";
        response += "uri="      + doubleQuote(mUrl)     + ", ";
        response += "response=" + doubleQuote(digest) ;
        if (opaque     != null) {
            response += ", opaque=" + doubleQuote(opaque);
        }
         if (algorithm != null) {
            response += ", algorithm=" +  algorithm;
        }
        if (QOP        != null) {
            response += ", qop=" + QOP + ", nc=" + nc + ", cnonce=" + doubleQuote(cnonce);
        }
        return response;
    }
    public static String authorizationHeader(boolean isProxy) {
        if (!isProxy) {
            return AUTHORIZATION_HEADER;
        } else {
            return PROXY_AUTHORIZATION_HEADER;
        }
    }
    private String computeDigest(
        String A1, String A2, String nonce, String QOP, String nc, String cnonce) {
        if (HttpLog.LOGV) {
            HttpLog.v("computeDigest(): QOP: " + QOP);
        }
        if (QOP == null) {
            return KD(H(A1), nonce + ":" + H(A2));
        } else {
            if (QOP.equalsIgnoreCase("auth")) {
                return KD(H(A1), nonce + ":" + nc + ":" + cnonce + ":" + QOP + ":" + H(A2));
            }
        }
        return null;
    }
    private String KD(String secret, String data) {
        return H(secret + ":" + data);
    }
    private String H(String param) {
        if (param != null) {
            Md5MessageDigest md5 = new Md5MessageDigest();
            byte[] d = md5.digest(param.getBytes());
            if (d != null) {
                return bufferToHex(d);
            }
        }
        return null;
    }
    private String bufferToHex(byte[] buffer) {
        final char hexChars[] =
            { '0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f' };
        if (buffer != null) {
            int length = buffer.length;
            if (length > 0) {
                StringBuilder hex = new StringBuilder(2 * length);
                for (int i = 0; i < length; ++i) {
                    byte l = (byte) (buffer[i] & 0x0F);
                    byte h = (byte)((buffer[i] & 0xF0) >> 4);
                    hex.append(hexChars[h]);
                    hex.append(hexChars[l]);
                }
                return hex.toString();
            } else {
                return "";
            }
        }
        return null;
    }
    private String computeCnonce() {
        Random rand = new Random();
        int nextInt = rand.nextInt();
        nextInt = (nextInt == Integer.MIN_VALUE) ?
                Integer.MAX_VALUE : Math.abs(nextInt);
        return Integer.toString(nextInt, 16);
    }
    private String doubleQuote(String param) {
        if (param != null) {
            return "\"" + param + "\"";
        }
        return null;
    }
    private void createAndQueueNewRequest() {
        if (mConnection != null) {
            RequestHandle newHandle = mRequestQueue.queueSynchronousRequest(
                    mUrl, mUri, mMethod, mHeaders, mRequest.mEventHandler,
                    mBodyProvider, mBodyLength);
            mRequest = newHandle.mRequest;
            mConnection = newHandle.mConnection;
            newHandle.processRequest();
            return;
        }
        mRequest = mRequestQueue.queueRequest(
                mUrl, mUri, mMethod, mHeaders, mRequest.mEventHandler,
                mBodyProvider,
                mBodyLength).mRequest;
    }
}
