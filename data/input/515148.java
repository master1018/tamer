class Network {
    private static final String LOGTAG = "network";
    private static Network sNetwork;
    private static boolean sPlatformNotifications;
    private static int sPlatformNotificationEnableRefCount;
    private String mProxyUsername;
    private String mProxyPassword;
    private RequestQueue mRequestQueue;
    private SslErrorHandler mSslErrorHandler;
    private HttpAuthHandler mHttpAuthHandler;
    public static synchronized Network getInstance(Context context) {
        if (sNetwork == null) {
            sNetwork = new Network(context.getApplicationContext());
            if (sPlatformNotifications) {
                --sPlatformNotificationEnableRefCount;
                enablePlatformNotifications();
            }
        }
        return sNetwork;
    }
    public static void enablePlatformNotifications() {
        if (++sPlatformNotificationEnableRefCount == 1) {
            if (sNetwork != null) {
                sNetwork.mRequestQueue.enablePlatformNotifications();
            } else {
                sPlatformNotifications = true;
            }
        }
    }
    public static void disablePlatformNotifications() {
        if (--sPlatformNotificationEnableRefCount == 0) {
            if (sNetwork != null) {
                sNetwork.mRequestQueue.disablePlatformNotifications();
            } else {
                sPlatformNotifications = false;
            }
        }
    }
    private Network(Context context) {
        if (DebugFlags.NETWORK) {
            Assert.assertTrue(Thread.currentThread().
                    getName().equals(WebViewCore.THREAD_NAME));
        }
        mSslErrorHandler = new SslErrorHandler();
        mHttpAuthHandler = new HttpAuthHandler(this);
        mRequestQueue = new RequestQueue(context);
    }
    public boolean requestURL(String method,
                              Map<String, String> headers,
                              byte [] postData,
                              LoadListener loader) {
        String url = loader.url();
        if (!URLUtil.isValidUrl(url)) {
            return false;
        }
        if (URLUtil.isAssetUrl(url) || URLUtil.isResourceUrl(url)
                || URLUtil.isFileUrl(url) || URLUtil.isDataUrl(url)) {
            return false;
        }
        InputStream bodyProvider = null;
        int bodyLength = 0;
        if (postData != null) {
            bodyLength = postData.length;
            bodyProvider = new ByteArrayInputStream(postData);
        }
        RequestQueue q = mRequestQueue;
        RequestHandle handle = null;
        if (loader.isSynchronous()) {
            handle = q.queueSynchronousRequest(url, loader.getWebAddress(),
                    method, headers, loader, bodyProvider, bodyLength);
            loader.attachRequestHandle(handle);
            handle.processRequest();
            loader.loadSynchronousMessages();
        } else {
            handle = q.queueRequest(url, loader.getWebAddress(), method,
                    headers, loader, bodyProvider, bodyLength);
            loader.attachRequestHandle(handle);
        }
        return true;
    }
    public boolean isValidProxySet() {
        synchronized (mRequestQueue) {
            return mRequestQueue.getProxyHost() != null;
        }
    }
    public String getProxyHostname() {
        return mRequestQueue.getProxyHost().getHostName();
    }
    public synchronized String getProxyUsername() {
        return mProxyUsername;
    }
    public synchronized void setProxyUsername(String proxyUsername) {
        if (DebugFlags.NETWORK) {
            Assert.assertTrue(isValidProxySet());
        }
        mProxyUsername = proxyUsername;
    }
    public synchronized String getProxyPassword() {
        return mProxyPassword;
    }
    public synchronized void setProxyPassword(String proxyPassword) {
        if (DebugFlags.NETWORK) {
            Assert.assertTrue(isValidProxySet());
        }
        mProxyPassword = proxyPassword;
    }
    public boolean saveState(Bundle outState) {
        if (DebugFlags.NETWORK) {
            Log.v(LOGTAG, "Network.saveState()");
        }
        return mSslErrorHandler.saveState(outState);
    }
    public boolean restoreState(Bundle inState) {
        if (DebugFlags.NETWORK) {
            Log.v(LOGTAG, "Network.restoreState()");
        }
        return mSslErrorHandler.restoreState(inState);
    }
    public void clearUserSslPrefTable() {
        mSslErrorHandler.clear();
    }
    public void handleSslErrorRequest(LoadListener loader) {
        if (DebugFlags.NETWORK) Assert.assertNotNull(loader);
        if (loader != null) {
            mSslErrorHandler.handleSslErrorRequest(loader);
        }
    }
     boolean checkSslPrefTable(LoadListener loader,
            SslError error) {
        if (loader != null && error != null) {
            return mSslErrorHandler.checkSslPrefTable(loader, error);
        }
        return false;
    }
    public void handleAuthRequest(LoadListener loader) {
        if (DebugFlags.NETWORK) Assert.assertNotNull(loader);
        if (loader != null) {
            mHttpAuthHandler.handleAuthRequest(loader);
        }
    }
    public void startTiming() {
        mRequestQueue.startTiming();
    }
    public void stopTiming() {
        mRequestQueue.stopTiming();
    }
}
