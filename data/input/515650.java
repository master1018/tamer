class LoadListener extends Handler implements EventHandler {
    private static final String LOGTAG = "webkit";
    private static final int MSG_CONTENT_HEADERS = 100;
    private static final int MSG_CONTENT_DATA = 110;
    private static final int MSG_CONTENT_FINISHED = 120;
    private static final int MSG_CONTENT_ERROR = 130;
    private static final int MSG_LOCATION_CHANGED = 140;
    private static final int MSG_LOCATION_CHANGED_REQUEST = 150;
    private static final int MSG_STATUS = 160;
    private static final int MSG_SSL_CERTIFICATE = 170;
    private static final int MSG_SSL_ERROR = 180;
    private static final int HTTP_OK = 200;
    private static final int HTTP_PARTIAL_CONTENT = 206;
    private static final int HTTP_MOVED_PERMANENTLY = 301;
    private static final int HTTP_FOUND = 302;
    private static final int HTTP_SEE_OTHER = 303;
    private static final int HTTP_NOT_MODIFIED = 304;
    private static final int HTTP_TEMPORARY_REDIRECT = 307;
    private static final int HTTP_AUTH = 401;
    private static final int HTTP_NOT_FOUND = 404;
    private static final int HTTP_PROXY_AUTH = 407;
    private static HashMap<String, String> sCertificateTypeMap;
    static {
        sCertificateTypeMap = new HashMap<String, String>();
        sCertificateTypeMap.put("application/x-x509-ca-cert", CertTool.CERT);
        sCertificateTypeMap.put("application/x-x509-user-cert", CertTool.CERT);
        sCertificateTypeMap.put("application/x-pkcs12", CertTool.PKCS12);
    }
    private static int sNativeLoaderCount;
    private final ByteArrayBuilder mDataBuilder = new ByteArrayBuilder();
    private String   mUrl;
    private WebAddress mUri;
    private boolean  mPermanent;
    private String   mOriginalUrl;
    private Context  mContext;
    private BrowserFrame mBrowserFrame;
    private int      mNativeLoader;
    private String   mMimeType;
    private String   mEncoding;
    private String   mTransferEncoding;
    private int      mStatusCode;
    private String   mStatusText;
    public long mContentLength; 
    private boolean  mCancelled;  
    private boolean  mAuthFailed;  
    private CacheLoader mCacheLoader;
    private boolean  mFromCache = false;
    private HttpAuthHeader mAuthHeader;
    private int      mErrorID = OK;
    private String   mErrorDescription;
    private SslError mSslError;
    private RequestHandle mRequestHandle;
    private RequestHandle mSslErrorRequestHandle;
    private long     mPostIdentifier;
    private String mMethod;
    private Map<String, String> mRequestHeaders;
    private byte[] mPostData;
    private boolean mSynchronous;
    private Vector<Message> mMessageQueue;
    private boolean mIsMainPageLoader;
    private final boolean mIsMainResourceLoader;
    private final boolean mUserGesture;
    private Headers mHeaders;
    private final String mUsername;
    private final String mPassword;
    public static LoadListener getLoadListener(Context context,
            BrowserFrame frame, String url, int nativeLoader,
            boolean synchronous, boolean isMainPageLoader,
            boolean isMainResource, boolean userGesture, long postIdentifier,
            String username, String password) {
        sNativeLoaderCount += 1;
        return new LoadListener(context, frame, url, nativeLoader, synchronous,
                isMainPageLoader, isMainResource, userGesture, postIdentifier,
                username, password);
    }
    public static int getNativeLoaderCount() {
        return sNativeLoaderCount;
    }
    LoadListener(Context context, BrowserFrame frame, String url,
            int nativeLoader, boolean synchronous, boolean isMainPageLoader,
            boolean isMainResource, boolean userGesture, long postIdentifier,
            String username, String password) {
        if (DebugFlags.LOAD_LISTENER) {
            Log.v(LOGTAG, "LoadListener constructor url=" + url);
        }
        mContext = context;
        mBrowserFrame = frame;
        setUrl(url);
        mNativeLoader = nativeLoader;
        mSynchronous = synchronous;
        if (synchronous) {
            mMessageQueue = new Vector<Message>();
        }
        mIsMainPageLoader = isMainPageLoader;
        mIsMainResourceLoader = isMainResource;
        mUserGesture = userGesture;
        mPostIdentifier = postIdentifier;
        mUsername = username;
        mPassword = password;
    }
    private void clearNativeLoader() {
        sNativeLoaderCount -= 1;
        mNativeLoader = 0;
    }
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_CONTENT_HEADERS:
                handleHeaders((Headers) msg.obj);
                break;
            case MSG_CONTENT_DATA:
                if (mNativeLoader != 0 && !ignoreCallbacks()) {
                    commitLoad();
                }
                break;
            case MSG_CONTENT_FINISHED:
                handleEndData();
                break;
            case MSG_CONTENT_ERROR:
                handleError(msg.arg1, (String) msg.obj);
                break;
            case MSG_LOCATION_CHANGED:
                doRedirect();
                break;
            case MSG_LOCATION_CHANGED_REQUEST:
                Message contMsg = obtainMessage(MSG_LOCATION_CHANGED);
                Message stopMsg = obtainMessage(MSG_CONTENT_FINISHED);
                mBrowserFrame.getCallbackProxy().onFormResubmission(
                        stopMsg, contMsg);
                break;
            case MSG_STATUS:
                HashMap status = (HashMap) msg.obj;
                handleStatus(((Integer) status.get("major")).intValue(),
                        ((Integer) status.get("minor")).intValue(),
                        ((Integer) status.get("code")).intValue(),
                        (String) status.get("reason"));
                break;
            case MSG_SSL_CERTIFICATE:
                handleCertificate((SslCertificate) msg.obj);
                break;
            case MSG_SSL_ERROR:
                handleSslError((SslError) msg.obj);
                break;
        }
    }
    BrowserFrame getFrame() {
        return mBrowserFrame;
    }
    Context getContext() {
        return mContext;
    }
     boolean isSynchronous() {
        return mSynchronous;
    }
    public boolean cancelled() {
        return mCancelled;
    }
    public void headers(Headers headers) {
        if (DebugFlags.LOAD_LISTENER) Log.v(LOGTAG, "LoadListener.headers");
        if (mCancelled) return;
        ArrayList<String> cookies = headers.getSetCookie();
        for (int i = 0; i < cookies.size(); ++i) {
            CookieManager.getInstance().setCookie(mUri, cookies.get(i));
        }
        sendMessageInternal(obtainMessage(MSG_CONTENT_HEADERS, headers));
    }
    private static final String XML_MIME_TYPE =
            "^[\\w_\\-+~!$\\^{}|.%'`#&*]+/" +
            "[\\w_\\-+~!$\\^{}|.%'`#&*]+\\+xml$";
    private void handleHeaders(Headers headers) {
        if (mCancelled) return;
        if (mStatusCode == HTTP_PARTIAL_CONTENT) {
            WebViewWorker.getHandler().obtainMessage(
                    WebViewWorker.MSG_REMOVE_CACHE, this).sendToTarget();
            return;
        }
        mHeaders = headers;
        long contentLength = headers.getContentLength();
        if (contentLength != Headers.NO_CONTENT_LENGTH) {
            mContentLength = contentLength;
        } else {
            mContentLength = 0;
        }
        String contentType = headers.getContentType();
        if (contentType != null) {
            parseContentTypeHeader(contentType);
            if (mMimeType.equals("text/plain") ||
                    mMimeType.equals("application/octet-stream")) {
                String contentDisposition = headers.getContentDisposition();
                String url = null;
                if (contentDisposition != null) {
                    url = URLUtil.parseContentDisposition(contentDisposition);
                }
                if (url == null) {
                    url = mUrl;
                }
                String newMimeType = guessMimeTypeFromExtension(url);
                if (newMimeType != null) {
                    mMimeType = newMimeType;
                }
            } else if (mMimeType.equals("text/vnd.wap.wml")) {
                mMimeType = "text/plain";
            } else {
                if (mMimeType.equals("application/vnd.wap.xhtml+xml")) {
                    mMimeType = "application/xhtml+xml";
                }
            }
        } else {
            guessMimeType();
        }
        if (mIsMainPageLoader && mIsMainResourceLoader && mUserGesture &&
                Pattern.matches(XML_MIME_TYPE, mMimeType) &&
                !mMimeType.equalsIgnoreCase("application/xhtml+xml")) {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setDataAndType(Uri.parse(url()), mMimeType);
            ResolveInfo info = mContext.getPackageManager().resolveActivity(i,
                    PackageManager.MATCH_DEFAULT_ONLY);
            if (info != null && !mContext.getPackageName().equals(
                    info.activityInfo.packageName)) {
                try {
                    mContext.startActivity(i);
                    mBrowserFrame.stopLoading();
                    return;
                } catch (ActivityNotFoundException ex) {
                }
            }
        }
        boolean mustAuthenticate = (mStatusCode == HTTP_AUTH ||
                mStatusCode == HTTP_PROXY_AUTH);
        boolean isProxyAuthRequest = (mStatusCode == HTTP_PROXY_AUTH);
        mAuthFailed = false;
        if (mAuthHeader != null) {
            mAuthFailed = (mustAuthenticate &&
                    isProxyAuthRequest == mAuthHeader.isProxy());
            if (!mAuthFailed && mAuthHeader.isProxy()) {
                Network network = Network.getInstance(mContext);
                if (network.isValidProxySet()) {
                    synchronized (network) {
                        network.setProxyUsername(mAuthHeader.getUsername());
                        network.setProxyPassword(mAuthHeader.getPassword());
                    }
                }
            }
        }
        mAuthHeader = null;
        if (mustAuthenticate) {
            if (mStatusCode == HTTP_AUTH) {
                mAuthHeader = parseAuthHeader(
                        headers.getWwwAuthenticate());
            } else {
                mAuthHeader = parseAuthHeader(
                        headers.getProxyAuthenticate());
                if (mAuthHeader != null) {
                    mAuthHeader.setProxy();
                }
            }
        }
        if ((mStatusCode == HTTP_OK ||
                mStatusCode == HTTP_FOUND ||
                mStatusCode == HTTP_MOVED_PERMANENTLY ||
                mStatusCode == HTTP_TEMPORARY_REDIRECT) && 
                mNativeLoader != 0) {
            if (!mFromCache && mRequestHandle != null
                    && (!mRequestHandle.getMethod().equals("POST")
                            || mPostIdentifier != 0)) {
                WebViewWorker.CacheCreateData data = new WebViewWorker.CacheCreateData();
                data.mListener = this;
                data.mUrl = mUrl;
                data.mMimeType = mMimeType;
                data.mStatusCode = mStatusCode;
                data.mPostId = mPostIdentifier;
                data.mHeaders = headers;
                WebViewWorker.getHandler().obtainMessage(
                        WebViewWorker.MSG_CREATE_CACHE, data).sendToTarget();
            }
            WebViewWorker.CacheEncoding ce = new WebViewWorker.CacheEncoding();
            ce.mEncoding = mEncoding;
            ce.mListener = this;
            WebViewWorker.getHandler().obtainMessage(
                    WebViewWorker.MSG_UPDATE_CACHE_ENCODING, ce).sendToTarget();
        }
        commitHeadersCheckRedirect();
    }
    boolean proxyAuthenticate() {
        if (mAuthHeader != null) {
            return mAuthHeader.isProxy();
        }
        return false;
    }
    public void status(int majorVersion, int minorVersion,
            int code,  String reasonPhrase) {
        if (DebugFlags.LOAD_LISTENER) {
            Log.v(LOGTAG, "LoadListener: from: " + mUrl
                    + " major: " + majorVersion
                    + " minor: " + minorVersion
                    + " code: " + code
                    + " reason: " + reasonPhrase);
        }
        HashMap status = new HashMap();
        status.put("major", majorVersion);
        status.put("minor", minorVersion);
        status.put("code", code);
        status.put("reason", reasonPhrase);
        mDataBuilder.clear();
        mMimeType = "";
        mEncoding = "";
        mTransferEncoding = "";
        sendMessageInternal(obtainMessage(MSG_STATUS, status));
    }
    private void handleStatus(int major, int minor, int code, String reason) {
        if (mCancelled) return;
        mStatusCode = code;
        mStatusText = reason;
        mPermanent = false;
    }
    public void certificate(SslCertificate certificate) {
        if (DebugFlags.LOAD_LISTENER) {
            Log.v(LOGTAG, "LoadListener.certificate: " + certificate);
        }
        sendMessageInternal(obtainMessage(MSG_SSL_CERTIFICATE, certificate));
    }
    private void handleCertificate(SslCertificate certificate) {
        if (mIsMainPageLoader && mIsMainResourceLoader) {
            mBrowserFrame.certificate(certificate);
        }
    }
    public void error(int id, String description) {
        if (DebugFlags.LOAD_LISTENER) {
            Log.v(LOGTAG, "LoadListener.error url:" +
                    url() + " id:" + id + " description:" + description);
        }
        sendMessageInternal(obtainMessage(MSG_CONTENT_ERROR, id, 0, description));
    }
    private void handleError(int id, String description) {
        mErrorID = id;
        mErrorDescription = description;
        detachRequestHandle();
        notifyError();
        tearDown();
    }
    public void data(byte[] data, int length) {
        if (DebugFlags.LOAD_LISTENER) {
            Log.v(LOGTAG, "LoadListener.data(): url: " + url());
        }
        boolean sendMessage = false;
        synchronized (mDataBuilder) {
            sendMessage = mDataBuilder.isEmpty();
            mDataBuilder.append(data, 0, length);
        }
        if (sendMessage) {
            sendMessageInternal(obtainMessage(MSG_CONTENT_DATA));
        }
    }
    public void endData() {
        if (DebugFlags.LOAD_LISTENER) {
            Log.v(LOGTAG, "LoadListener.endData(): url: " + url());
        }
        sendMessageInternal(obtainMessage(MSG_CONTENT_FINISHED));
    }
    private void handleEndData() {
        if (mCancelled) return;
        switch (mStatusCode) {
            case HTTP_MOVED_PERMANENTLY:
                mPermanent = true;
            case HTTP_FOUND:
            case HTTP_SEE_OTHER:
            case HTTP_TEMPORARY_REDIRECT:
                if (mStatusCode == HTTP_TEMPORARY_REDIRECT) {
                    if (mRequestHandle != null && 
                                mRequestHandle.getMethod().equals("POST")) {
                        sendMessageInternal(obtainMessage(
                                MSG_LOCATION_CHANGED_REQUEST));  
                    } else if (mMethod != null && mMethod.equals("POST")) {
                        sendMessageInternal(obtainMessage(
                                MSG_LOCATION_CHANGED_REQUEST));
                    } else {
                        sendMessageInternal(obtainMessage(MSG_LOCATION_CHANGED));
                    }
                } else {
                    sendMessageInternal(obtainMessage(MSG_LOCATION_CHANGED));
                }
                return;
            case HTTP_AUTH:
            case HTTP_PROXY_AUTH:
                if (mAuthHeader != null &&
                        (Network.getInstance(mContext).isValidProxySet() ||
                         !mAuthHeader.isProxy())) {
                    if (!mAuthFailed && mUsername != null && mPassword != null) {
                        String host = mAuthHeader.isProxy() ?
                                Network.getInstance(mContext).getProxyHostname() :
                                mUri.mHost;
                        HttpAuthHandler.onReceivedCredentials(this, host,
                                mAuthHeader.getRealm(), mUsername, mPassword);
                        makeAuthResponse(mUsername, mPassword);
                    } else {
                        Network.getInstance(mContext).handleAuthRequest(this);
                    }
                    return;
                }
                break;  
            case HTTP_NOT_MODIFIED:
                if (mCacheLoader != null) {
                    if (isSynchronous()) {
                        mCacheLoader.load();
                    } else {
                        WebViewWorker.getHandler().obtainMessage(
                                WebViewWorker.MSG_ADD_STREAMLOADER, mCacheLoader)
                                .sendToTarget();
                    }
                    mFromCache = true;
                    if (DebugFlags.LOAD_LISTENER) {
                        Log.v(LOGTAG, "LoadListener cache load url=" + url());
                    }
                    return;
                }
                break;  
            case HTTP_NOT_FOUND:
            default:
                break;
        }
        detachRequestHandle();
        tearDown();
    }
     void setCacheLoader(CacheLoader c) {
        mCacheLoader = c;
        mFromCache = true;
    }
    boolean checkCache(Map<String, String> headers) {
        CacheResult result = CacheManager.getCacheFile(url(), mPostIdentifier,
                headers);
        mCacheLoader = null;
        mFromCache = false;
        if (result != null) {
            mCacheLoader = new CacheLoader(this, result);
            if (!headers.containsKey(
                    CacheManager.HEADER_KEY_IFNONEMATCH) &&
                    !headers.containsKey(
                            CacheManager.HEADER_KEY_IFMODIFIEDSINCE)) {
                if (DebugFlags.LOAD_LISTENER) {
                    Log.v(LOGTAG, "FrameLoader: HTTP URL in cache " +
                            "and usable: " + url());
                }
                if (isSynchronous()) {
                    mCacheLoader.load();
                } else {
                    WebViewWorker.getHandler().obtainMessage(
                            WebViewWorker.MSG_ADD_STREAMLOADER, mCacheLoader)
                            .sendToTarget();
                }
                mFromCache = true;
                return true;
            }
        }
        return false;
    }
    public boolean handleSslErrorRequest(SslError error) {
        if (DebugFlags.LOAD_LISTENER) {
            Log.v(LOGTAG,
                    "LoadListener.handleSslErrorRequest(): url:" + url() +
                    " primary error: " + error.getPrimaryError() +
                    " certificate: " + error.getCertificate());
        }
        if (Network.getInstance(mContext).checkSslPrefTable(this, error)) {
            return true;
        }
        if (isSynchronous()) {
            mRequestHandle.handleSslErrorResponse(false);
            return true;
        }
        sendMessageInternal(obtainMessage(MSG_SSL_ERROR, error));
        if (!mCancelled) {
            mSslErrorRequestHandle = mRequestHandle;
        }
        return !mCancelled;
    }
    private void handleSslError(SslError error) {
        if (!mCancelled) {
            mSslError = error;
            Network.getInstance(mContext).handleSslErrorRequest(this);
        } else if (mSslErrorRequestHandle != null) {
            mSslErrorRequestHandle.handleSslErrorResponse(true);
        }
        mSslErrorRequestHandle = null;
    }
    String realm() {
        if (mAuthHeader == null) {
            return null;
        } else {
            return mAuthHeader.getRealm();
        }
    }
    boolean authCredentialsInvalid() {
        return (mAuthFailed &&
                !(mAuthHeader.isDigest() && mAuthHeader.getStale()));
    }
    SslError sslError() {
        return mSslError;
    }
    void handleSslErrorResponse(boolean proceed) {
        if (mRequestHandle != null) {
            mRequestHandle.handleSslErrorResponse(proceed);
        }
        if (!proceed) {
            commitLoad();
            tearDown();
        }
    }
    void handleAuthResponse(String username, String password) {
        if (DebugFlags.LOAD_LISTENER) {
            Log.v(LOGTAG, "LoadListener.handleAuthResponse: url: " + mUrl
                    + " username: " + username
                    + " password: " + password);
        }
        if (username != null && password != null) {
            makeAuthResponse(username, password);
        } else {
            commitLoad();
            tearDown();
        }
    }
    void makeAuthResponse(String username, String password) {
        if (mAuthHeader == null || mRequestHandle == null) {
            return;
        }
        mAuthHeader.setUsername(username);
        mAuthHeader.setPassword(password);
        int scheme = mAuthHeader.getScheme();
        if (scheme == HttpAuthHeader.BASIC) {
            boolean isProxy = mAuthHeader.isProxy();
            mRequestHandle.setupBasicAuthResponse(isProxy, username, password);
        } else if (scheme == HttpAuthHeader.DIGEST) {
            boolean isProxy = mAuthHeader.isProxy();
            String realm     = mAuthHeader.getRealm();
            String nonce     = mAuthHeader.getNonce();
            String qop       = mAuthHeader.getQop();
            String algorithm = mAuthHeader.getAlgorithm();
            String opaque    = mAuthHeader.getOpaque();
            mRequestHandle.setupDigestAuthResponse(isProxy, username, password,
                    realm, nonce, qop, algorithm, opaque);
        }
    }
    void setRequestData(String method, Map<String, String> headers, 
            byte[] postData) {
        mMethod = method;
        mRequestHeaders = headers;
        mPostData = postData;
    }
    String url() {
        return mUrl;
    }
    WebAddress getWebAddress() {
        return mUri;
    }
    String host() {
        if (mUri != null) {
            return mUri.mHost;
        }
        return null;
    }
    String originalUrl() {
        if (mOriginalUrl != null) {
            return mOriginalUrl;
        } else {
            return mUrl;
        }
    }
    long postIdentifier() {
        return mPostIdentifier;
    }
    void attachRequestHandle(RequestHandle requestHandle) {
        if (DebugFlags.LOAD_LISTENER) {
            Log.v(LOGTAG, "LoadListener.attachRequestHandle(): " +
                    "requestHandle: " +  requestHandle);
        }
        mRequestHandle = requestHandle;
    }
    void detachRequestHandle() {
        if (DebugFlags.LOAD_LISTENER) {
            Log.v(LOGTAG, "LoadListener.detachRequestHandle(): " +
                    "requestHandle: " + mRequestHandle);
        }
        mRequestHandle = null;
    }
    void downloadFile() {
        WebViewWorker.getHandler().obtainMessage(
                WebViewWorker.MSG_REMOVE_CACHE, this).sendToTarget();
        mBrowserFrame.getCallbackProxy().onDownloadStart(url(), 
                mBrowserFrame.getUserAgentString(),
                mHeaders.getContentDisposition(), 
                mMimeType, mContentLength);
        cancel();
    }
    static boolean willLoadFromCache(String url, long identifier) {
        boolean inCache =
                CacheManager.getCacheFile(url, identifier, null) != null;
        if (DebugFlags.LOAD_LISTENER) {
            Log.v(LOGTAG, "willLoadFromCache: " + url + " in cache: " + 
                    inCache);
        }
        return inCache;
    }
    void resetCancel() {
        mCancelled = false;
    }
    String mimeType() {
        return mMimeType;
    }
    String transferEncoding() {
        return mTransferEncoding;
    }
    long contentLength() {
        return mContentLength;
    }
    private void commitHeadersCheckRedirect() {
        if (mCancelled) return;
        if ((mStatusCode >= 301 && mStatusCode <= 303) || mStatusCode == 307 ||
                (mStatusCode == 304 && mCacheLoader != null)) {
            return;
        }
        commitHeaders();
    }
    private void commitHeaders() {
        if (mIsMainPageLoader && sCertificateTypeMap.containsKey(mMimeType)) {
            return;
        }
        if (mAuthHeader != null)
            return;
        int nativeResponse = createNativeResponse();
        nativeReceivedResponse(nativeResponse);
    }
    private int createNativeResponse() {
        int statusCode = (mStatusCode == HTTP_NOT_MODIFIED &&
                mCacheLoader != null) ? HTTP_OK : mStatusCode;
        final int nativeResponse = nativeCreateResponse(
                originalUrl(), statusCode, mStatusText,
                mMimeType, mContentLength, mEncoding);
        if (mHeaders != null) {
            mHeaders.getHeaders(new Headers.HeaderCallback() {
                    public void header(String name, String value) {
                        nativeSetResponseHeader(nativeResponse, name, value);
                    }
                });
        }
        return nativeResponse;
    }
    private void commitLoad() {
        if (mCancelled) return;
        if (mIsMainPageLoader) {
            String type = sCertificateTypeMap.get(mMimeType);
            if (type != null) {
                synchronized (mDataBuilder) {
                    byte[] cert = new byte[mDataBuilder.getByteSize()];
                    int offset = 0;
                    while (true) {
                        ByteArrayBuilder.Chunk c = mDataBuilder.getFirstChunk();
                        if (c == null) break;
                        if (c.mLength != 0) {
                            System.arraycopy(c.mArray, 0, cert, offset, c.mLength);
                            offset += c.mLength;
                        }
                        c.release();
                    }
                    CertTool.addCertificate(mContext, type, cert);
                    mBrowserFrame.stopLoading();
                    return;
                }
            }
        }
        PerfChecker checker = new PerfChecker();
        ByteArrayBuilder.Chunk c;
        while (true) {
            c = mDataBuilder.getFirstChunk();
            if (c == null) break;
            if (c.mLength != 0) {
                nativeAddData(c.mArray, c.mLength);
                WebViewWorker.CacheData data = new WebViewWorker.CacheData();
                data.mListener = this;
                data.mChunk = c;
                WebViewWorker.getHandler().obtainMessage(
                        WebViewWorker.MSG_APPEND_CACHE, data).sendToTarget();
            } else {
                c.release();
            }
            checker.responseAlert("res nativeAddData");
        }
    }
    void tearDown() {
        if (getErrorID() == OK) {
            WebViewWorker.CacheSaveData data = new WebViewWorker.CacheSaveData();
            data.mListener = this;
            data.mUrl = mUrl;
            data.mPostId = mPostIdentifier;
            WebViewWorker.getHandler().obtainMessage(
                    WebViewWorker.MSG_SAVE_CACHE, data).sendToTarget();
        } else {
            WebViewWorker.getHandler().obtainMessage(
                    WebViewWorker.MSG_REMOVE_CACHE, this).sendToTarget();
        }
        if (mNativeLoader != 0) {
            PerfChecker checker = new PerfChecker();
            nativeFinished();
            checker.responseAlert("res nativeFinished");
            clearNativeLoader();
        }
    }
    private int getErrorID() {
        return mErrorID;
    }
    private String getErrorDescription() {
        return mErrorDescription;
    }
    void notifyError() {
        if (mNativeLoader != 0) {
            String description = getErrorDescription();
            if (description == null) description = "";
            nativeError(getErrorID(), description, url());
            clearNativeLoader();
        }
    }
    void pauseLoad(boolean pause) {
        if (mRequestHandle != null) {
            mRequestHandle.pauseRequest(pause);
        }
    }
    public void cancel() {
        if (DebugFlags.LOAD_LISTENER) {
            if (mRequestHandle == null) {
                Log.v(LOGTAG, "LoadListener.cancel(): no requestHandle");
            } else {
                Log.v(LOGTAG, "LoadListener.cancel()");
            }
        }
        if (mRequestHandle != null) {
            mRequestHandle.cancel();
            mRequestHandle = null;
        }
        WebViewWorker.getHandler().obtainMessage(
                WebViewWorker.MSG_REMOVE_CACHE, this).sendToTarget();
        mCancelled = true;
        clearNativeLoader();
    }
    private int mCacheRedirectCount;
    private void doRedirect() {
        if (mCancelled) {
            return;
        }
        if (mCacheRedirectCount >= RequestHandle.MAX_REDIRECT_COUNT) {
            handleError(EventHandler.ERROR_REDIRECT_LOOP, mContext.getString(
                    R.string.httpErrorRedirectLoop));
            return;
        }
        String redirectTo = mHeaders.getLocation();
        if (redirectTo != null) {
            int nativeResponse = createNativeResponse();
            redirectTo =
                    nativeRedirectedToUrl(mUrl, redirectTo, nativeResponse);
            if (mCancelled) {
                return;
            }
            if (redirectTo == null) {
                Log.d(LOGTAG, "Redirection failed for "
                        + mHeaders.getLocation());
                cancel();
                return;
            } else if (!URLUtil.isNetworkUrl(redirectTo)) {
                final String text = mContext
                        .getString(R.string.open_permission_deny)
                        + "\n" + redirectTo;
                nativeAddData(text.getBytes(), text.length());
                nativeFinished();
                clearNativeLoader();
                return;
            }
            if (getErrorID() == OK) {
                WebViewWorker.CacheSaveData data = new WebViewWorker.CacheSaveData();
                data.mListener = this;
                data.mUrl = mUrl;
                data.mPostId = mPostIdentifier;
                WebViewWorker.getHandler().obtainMessage(
                        WebViewWorker.MSG_SAVE_CACHE, data).sendToTarget();
            } else {
                WebViewWorker.getHandler().obtainMessage(
                        WebViewWorker.MSG_REMOVE_CACHE, this).sendToTarget();
            }
            mOriginalUrl = redirectTo;
            setUrl(redirectTo);
            if (mRequestHeaders == null) {
                mRequestHeaders = new HashMap<String, String>();
            }
            boolean fromCache = false;
            if (mCacheLoader != null) {
                mCacheRedirectCount++;
                fromCache = true;
            }
            if (!checkCache(mRequestHeaders)) {
                if (mRequestHandle != null) {
                    try {
                        mRequestHandle.setupRedirect(mUrl, mStatusCode,
                                mRequestHeaders);
                    } catch(RuntimeException e) {
                        Log.e(LOGTAG, e.getMessage());
                        handleError(EventHandler.ERROR_BAD_URL,
                                mContext.getString(R.string.httpErrorBadUrl));
                        return;
                    }
                } else {
                    Network network = Network.getInstance(getContext());
                    if (!network.requestURL(mMethod, mRequestHeaders,
                            mPostData, this)) {
                        handleError(EventHandler.ERROR_BAD_URL,
                                mContext.getString(R.string.httpErrorBadUrl));
                        return;
                    }
                }
                if (fromCache) {
                    mRequestHandle.setRedirectCount(mCacheRedirectCount);
                }
            } else if (!fromCache) {
                mCacheRedirectCount = mRequestHandle.getRedirectCount() + 1;
            }
        } else {
            commitHeaders();
            commitLoad();
            tearDown();
        }
        if (DebugFlags.LOAD_LISTENER) {
            Log.v(LOGTAG, "LoadListener.onRedirect(): redirect to: " +
                    redirectTo);
        }
    }
    private static final Pattern CONTENT_TYPE_PATTERN =
            Pattern.compile("^((?:[xX]-)?[a-zA-Z\\*]+/[\\w\\+\\*-]+[\\.[\\w\\+-]+]*)$");
     void parseContentTypeHeader(String contentType) {
        if (DebugFlags.LOAD_LISTENER) {
            Log.v(LOGTAG, "LoadListener.parseContentTypeHeader: " +
                    "contentType: " + contentType);
        }
        if (contentType != null) {
            int i = contentType.indexOf(';');
            if (i >= 0) {
                mMimeType = contentType.substring(0, i);
                int j = contentType.indexOf('=', i);
                if (j > 0) {
                    i = contentType.indexOf(';', j);
                    if (i < j) {
                        i = contentType.length();
                    }
                    mEncoding = contentType.substring(j + 1, i);
                } else {
                    mEncoding = contentType.substring(i + 1);
                }
                mEncoding = mEncoding.trim().toLowerCase();
                if (i < contentType.length() - 1) {
                    mTransferEncoding =
                            contentType.substring(i + 1).trim().toLowerCase();
                }
            } else {
                mMimeType = contentType;
            }
            mMimeType = mMimeType.trim();
            try {
                Matcher m = CONTENT_TYPE_PATTERN.matcher(mMimeType);
                if (m.find()) {
                    mMimeType = m.group(1);
                } else {
                    guessMimeType();
                }
            } catch (IllegalStateException ex) {
                guessMimeType();
            }
        }
        mMimeType = mMimeType.toLowerCase();
    }
    private HttpAuthHeader parseAuthHeader(String header) {
        if (header != null) {
            int posMax = 256;
            int posLen = 0;
            int[] pos = new int [posMax];
            int headerLen = header.length();
            if (headerLen > 0) {
                boolean quoted = false;
                for (int i = 0; i < headerLen && posLen < posMax; ++i) {
                    if (header.charAt(i) == '\"') {
                        quoted = !quoted;
                    } else {
                        if (!quoted) {
                            if (header.regionMatches(true, i,
                                    HttpAuthHeader.BASIC_TOKEN, 0,
                                    HttpAuthHeader.BASIC_TOKEN.length())) {
                                pos[posLen++] = i;
                                continue;
                            }
                            if (header.regionMatches(true, i,
                                    HttpAuthHeader.DIGEST_TOKEN, 0,
                                    HttpAuthHeader.DIGEST_TOKEN.length())) {
                                pos[posLen++] = i;
                                continue;
                            }
                        }
                    }
                }
            }
            if (posLen > 0) {
                for (int i = 0; i < posLen; i++) {
                    if (header.regionMatches(true, pos[i],
                                HttpAuthHeader.DIGEST_TOKEN, 0,
                                HttpAuthHeader.DIGEST_TOKEN.length())) {
                        String sub = header.substring(pos[i],
                                (i + 1 < posLen ? pos[i + 1] : headerLen));
                        HttpAuthHeader rval = new HttpAuthHeader(sub);
                        if (rval.isSupportedScheme()) {
                            return rval;
                        }
                    }
                }
                for (int i = 0; i < posLen; i++) {
                    if (header.regionMatches(true, pos[i],
                                HttpAuthHeader.BASIC_TOKEN, 0,
                                HttpAuthHeader.BASIC_TOKEN.length())) {
                        String sub = header.substring(pos[i],
                                (i + 1 < posLen ? pos[i + 1] : headerLen));
                        HttpAuthHeader rval = new HttpAuthHeader(sub);
                        if (rval.isSupportedScheme()) {
                            return rval;
                        }
                    }
                }
            }
        }
        return null;
    }
    private boolean ignoreCallbacks() {
        return (mCancelled || mAuthHeader != null ||
                (mStatusCode > 300 && mStatusCode < 400 && mStatusCode != 305));
    }
    void setUrl(String url) {
        if (url != null) {
            mUri = null;
            if (URLUtil.isNetworkUrl(url)) {
                mUrl = URLUtil.stripAnchor(url);
                try {
                    mUri = new WebAddress(mUrl);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            } else {
                mUrl = url;
            }
        }
    }
    private void guessMimeType() {
        if (URLUtil.isDataUrl(mUrl) && mMimeType.length() != 0) {
            cancel();
            final String text = mContext.getString(R.string.httpErrorBadUrl);
            handleError(EventHandler.ERROR_BAD_URL, text);
        } else {
            mMimeType = "text/html";
            String newMimeType = guessMimeTypeFromExtension(mUrl);
            if (newMimeType != null) {
                mMimeType = newMimeType;
            }
        }
    }
    private String guessMimeTypeFromExtension(String url) {
        if (DebugFlags.LOAD_LISTENER) {
            Log.v(LOGTAG, "guessMimeTypeFromExtension: url = " + url);
        }
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                MimeTypeMap.getFileExtensionFromUrl(url));
    }
    private void sendMessageInternal(Message msg) {
        if (mSynchronous) {
            mMessageQueue.add(msg);
        } else {
            sendMessage(msg);
        }
    }
     void loadSynchronousMessages() {
        if (DebugFlags.LOAD_LISTENER && !mSynchronous) {
            throw new AssertionError();
        }
        while (!mMessageQueue.isEmpty()) {
            handleMessage(mMessageQueue.remove(0));
        }
    }
    private native int nativeCreateResponse(String url, int statusCode,
            String statusText, String mimeType, long expectedLength,
            String encoding);
    private native void nativeSetResponseHeader(int nativeResponse, String key,
            String val);
    private native void nativeReceivedResponse(int nativeResponse);
    private native void nativeAddData(byte[] data, int length);
    private native void nativeFinished();
    private native String nativeRedirectedToUrl(String baseUrl,
            String redirectTo, int nativeResponse);
    private native void nativeError(int id, String desc, String failingUrl);
}
