class FrameLoader {
    private final LoadListener mListener;
    private final String mMethod;
    private final WebSettings mSettings;
    private Map<String, String> mHeaders;
    private byte[] mPostData;
    private Network mNetwork;
    private int mCacheMode;
    private String mReferrer;
    private String mContentType;
    private static final int URI_PROTOCOL = 0x100;
    private static final String CONTENT_TYPE = "content-type";
    private static final String mAboutBlank =
            "<!DOCTYPE html PUBLIC \"-
            "<html><head><title>about:blank</title></head><body></body></html>";
    static final String HEADER_STR = "text/xml, text/html, " +
            "application/xhtml+xml, image/png, text/plain, *
    public boolean executeLoad() {
        String url = mListener.url();
        if (URLUtil.isNetworkUrl(url)){
            if (mSettings.getBlockNetworkLoads()) {
                mListener.error(EventHandler.ERROR_BAD_URL,
                        mListener.getContext().getString(
                                com.android.internal.R.string.httpErrorBadUrl));
                return false;
            }
            if (!URLUtil.verifyURLEncoding(mListener.host())) {
                mListener.error(EventHandler.ERROR_BAD_URL,
                        mListener.getContext().getString(
                        com.android.internal.R.string.httpErrorBadUrl));
                return false;
            }
            mNetwork = Network.getInstance(mListener.getContext());
            if (mListener.isSynchronous()) {
                return handleHTTPLoad();
            }
            WebViewWorker.getHandler().obtainMessage(
                    WebViewWorker.MSG_ADD_HTTPLOADER, this).sendToTarget();
            return true;
        } else if (handleLocalFile(url, mListener, mSettings)) {
            return true;
        }
        if (DebugFlags.FRAME_LOADER) {
            Log.v(LOGTAG, "FrameLoader.executeLoad: url protocol not supported:"
                    + mListener.url());
        }
        mListener.error(EventHandler.ERROR_UNSUPPORTED_SCHEME,
                mListener.getContext().getText(
                        com.android.internal.R.string.httpErrorUnsupportedScheme).toString());
        return false;
    }
    static boolean handleLocalFile(String url, LoadListener loadListener,
            WebSettings settings) {
        try {
            url = new String(URLUtil.decode(url.getBytes()));
        } catch (IllegalArgumentException e) {
            loadListener.error(EventHandler.ERROR_BAD_URL,
                    loadListener.getContext().getString(
                            com.android.internal.R.string.httpErrorBadUrl));
            return true;
        }
        if (URLUtil.isAssetUrl(url)) {
            if (loadListener.isSynchronous()) {
                new FileLoader(url, loadListener, FileLoader.TYPE_ASSET,
                        true).load();
            } else {
                WebViewWorker.getHandler().obtainMessage(
                        WebViewWorker.MSG_ADD_STREAMLOADER,
                        new FileLoader(url, loadListener, FileLoader.TYPE_ASSET,
                                true)).sendToTarget();
            }
            return true;
        } else if (URLUtil.isResourceUrl(url)) {
            if (loadListener.isSynchronous()) {
                new FileLoader(url, loadListener, FileLoader.TYPE_RES,
                        true).load();
            } else {
                WebViewWorker.getHandler().obtainMessage(
                        WebViewWorker.MSG_ADD_STREAMLOADER,
                        new FileLoader(url, loadListener, FileLoader.TYPE_RES,
                                true)).sendToTarget();
            }
            return true;
        } else if (URLUtil.isFileUrl(url)) {
            if (loadListener.isSynchronous()) {
                new FileLoader(url, loadListener, FileLoader.TYPE_FILE,
                        settings.getAllowFileAccess()).load();
            } else {
                WebViewWorker.getHandler().obtainMessage(
                        WebViewWorker.MSG_ADD_STREAMLOADER,
                        new FileLoader(url, loadListener, FileLoader.TYPE_FILE,
                                settings.getAllowFileAccess())).sendToTarget();
            }
            return true;
        } else if (URLUtil.isContentUrl(url)) {
            if (loadListener.isSynchronous()) {
                new ContentLoader(loadListener.url(), loadListener).load();
            } else {
                WebViewWorker.getHandler().obtainMessage(
                        WebViewWorker.MSG_ADD_STREAMLOADER,
                        new ContentLoader(loadListener.url(), loadListener))
                        .sendToTarget();
            }
            return true;
        } else if (URLUtil.isDataUrl(url)) {
            new DataLoader(url, loadListener).load();
            return true;
        } else if (URLUtil.isAboutUrl(url)) {
            loadListener.data(mAboutBlank.getBytes(), mAboutBlank.length());
            loadListener.endData();
            return true;
        }
        return false;
    }
    boolean handleHTTPLoad() {
        if (mHeaders == null) {
            mHeaders = new HashMap<String, String>();
        }
        populateStaticHeaders();
        populateHeaders();
        if (handleCache()) {
            mListener.setRequestData(mMethod, mHeaders, mPostData);
            return true;
        }
        if (DebugFlags.FRAME_LOADER) {
            Log.v(LOGTAG, "FrameLoader: http " + mMethod + " load for: "
                    + mListener.url());
        }
        boolean ret = false;
        int error = EventHandler.ERROR_UNSUPPORTED_SCHEME;
        try {
            ret = mNetwork.requestURL(mMethod, mHeaders,
                    mPostData, mListener);
        } catch (android.net.ParseException ex) {
            error = EventHandler.ERROR_BAD_URL;
        } catch (java.lang.RuntimeException ex) {
            error = EventHandler.ERROR_BAD_URL;
        }
        if (!ret) {
            mListener.error(error, mListener.getContext().getText(
                    EventHandler.errorStringResources[Math.abs(error)]).toString());
            return false;
        }
        return true;
    }
    private void startCacheLoad(CacheResult result) {
        if (DebugFlags.FRAME_LOADER) {
            Log.v(LOGTAG, "FrameLoader: loading from cache: "
                  + mListener.url());
        }
        CacheLoader cacheLoader =
                new CacheLoader(mListener, result);
        mListener.setCacheLoader(cacheLoader);
        if (mListener.isSynchronous()) {
            cacheLoader.load();
        } else {
            WebViewWorker.getHandler().obtainMessage(
                    WebViewWorker.MSG_ADD_STREAMLOADER, cacheLoader).sendToTarget();
        }
    }
    private boolean handleCache() {
        switch (mCacheMode) {
            case WebSettings.LOAD_NO_CACHE:
                break;
            case WebSettings.LOAD_CACHE_ONLY: {
                CacheResult result = CacheManager.getCacheFile(mListener.url(),
                        mListener.postIdentifier(), null);
                if (result != null) {
                    startCacheLoad(result);
                } else {
                    int err = EventHandler.FILE_NOT_FOUND_ERROR;
                    mListener.error(err, mListener.getContext().getText(
                            EventHandler.errorStringResources[Math.abs(err)])
                            .toString());
                }
                return true;
            }
            case WebSettings.LOAD_CACHE_ELSE_NETWORK: {
                if (DebugFlags.FRAME_LOADER) {
                    Log.v(LOGTAG, "FrameLoader: checking cache: "
                            + mListener.url());
                }
                CacheResult result = CacheManager.getCacheFile(mListener.url(),
                        mListener.postIdentifier(), null);
                if (result != null) {
                    startCacheLoad(result);
                    return true;
                }
                break;
            }
            default:
            case WebSettings.LOAD_NORMAL:
                return mListener.checkCache(mHeaders);
        }
        return false;
    }
    private void populateStaticHeaders() {
        String accept = mHeaders.get("Accept");
        if (accept == null || accept.length() == 0) {
            mHeaders.put("Accept", HEADER_STR);
        }
        mHeaders.put("Accept-Charset", "utf-8, iso-8859-1, utf-16, *;q=0.7");
        String acceptLanguage = mSettings.getAcceptLanguage();
        if (acceptLanguage.length() > 0) {
            mHeaders.put("Accept-Language", acceptLanguage);
        }
        mHeaders.put("User-Agent", mSettings.getUserAgentString());
    }
    private void populateHeaders() {
        if (mReferrer != null) mHeaders.put("Referer", mReferrer);
        if (mContentType != null) mHeaders.put(CONTENT_TYPE, mContentType);
        if (mNetwork.isValidProxySet()) {
            String username;
            String password;
            synchronized (mNetwork) {
                username = mNetwork.getProxyUsername();
                password = mNetwork.getProxyPassword();
            }
            if (username != null && password != null) {
                String proxyHeader = RequestHandle.authorizationHeader(true);
                mHeaders.put(proxyHeader,
                        "Basic " + RequestHandle.computeBasicAuthResponse(
                                username, password));
            }
        }
        String cookie = CookieManager.getInstance().getCookie(
                mListener.getWebAddress());
        if (cookie != null && cookie.length() > 0) {
            mHeaders.put("Cookie", cookie);
        }
    }
}
