class BrowserFrame extends Handler {
    private static final String LOGTAG = "webkit";
    private final static int MAX_OUTSTANDING_REQUESTS = 300;
    private final CallbackProxy mCallbackProxy;
    private final WebSettings mSettings;
    private final Context mContext;
    private final WebViewDatabase mDatabase;
    private final WebViewCore mWebViewCore;
     boolean mLoadInitFromJava;
    private int mLoadType;
    private boolean mFirstLayoutDone = true;
    private boolean mCommitted = true;
    private boolean mBlockMessages = false;
    private boolean mIsMainFrame;
    private Map<String, Object> mJSInterfaceMap;
    static final int FRAME_COMPLETED = 1001;
    static final int ORIENTATION_CHANGED = 1002;
    static final int POLICY_FUNCTION = 1003;
    static final int FRAME_LOADTYPE_STANDARD = 0;
    static final int FRAME_LOADTYPE_BACK = 1;
    static final int FRAME_LOADTYPE_FORWARD = 2;
    static final int FRAME_LOADTYPE_INDEXEDBACKFORWARD = 3;
    static final int FRAME_LOADTYPE_RELOAD = 4;
    static final int FRAME_LOADTYPE_RELOADALLOWINGSTALEDATA = 5;
    static final int FRAME_LOADTYPE_SAME = 6;
    static final int FRAME_LOADTYPE_REDIRECT = 7;
    static final int FRAME_LOADTYPE_REPLACE = 8;
    private static final int TRANSITION_SWITCH_THRESHOLD = 75;
     int mNativeFrame;
    static JWebCoreJavaBridge sJavaBridge;
    private static class ConfigCallback implements ComponentCallbacks {
        private final ArrayList<WeakReference<Handler>> mHandlers =
                new ArrayList<WeakReference<Handler>>();
        private final WindowManager mWindowManager;
        ConfigCallback(WindowManager wm) {
            mWindowManager = wm;
        }
        public synchronized void addHandler(Handler h) {
            mHandlers.add(new WeakReference<Handler>(h));
        }
        public void onConfigurationChanged(Configuration newConfig) {
            if (mHandlers.size() == 0) {
                return;
            }
            int orientation =
                    mWindowManager.getDefaultDisplay().getOrientation();
            switch (orientation) {
                case Surface.ROTATION_90:
                    orientation = 90;
                    break;
                case Surface.ROTATION_180:
                    orientation = 180;
                    break;
                case Surface.ROTATION_270:
                    orientation = -90;
                    break;
                case Surface.ROTATION_0:
                    orientation = 0;
                    break;
                default:
                    break;
            }
            synchronized (this) {
                ArrayList<WeakReference> handlersToRemove =
                        new ArrayList<WeakReference>(mHandlers.size());
                for (WeakReference<Handler> wh : mHandlers) {
                    Handler h = wh.get();
                    if (h != null) {
                        h.sendMessage(h.obtainMessage(ORIENTATION_CHANGED,
                                    orientation, 0));
                    } else {
                        handlersToRemove.add(wh);
                    }
                }
                for (WeakReference weak : handlersToRemove) {
                    mHandlers.remove(weak);
                }
            }
        }
        public void onLowMemory() {}
    }
    static ConfigCallback sConfigCallback;
    public BrowserFrame(Context context, WebViewCore w, CallbackProxy proxy,
            WebSettings settings, Map<String, Object> javascriptInterfaces) {
        Context appContext = context.getApplicationContext();
        if (sJavaBridge == null) {
            sJavaBridge = new JWebCoreJavaBridge();
            ActivityManager am = (ActivityManager) context
                    .getSystemService(Context.ACTIVITY_SERVICE);
            if (am.getMemoryClass() > 16) {
                sJavaBridge.setCacheSize(8 * 1024 * 1024);
            } else {
                sJavaBridge.setCacheSize(4 * 1024 * 1024);
            }
            CacheManager.init(appContext);
            CookieSyncManager.createInstance(appContext);
            PluginManager.getInstance(appContext);
        }
        if (sConfigCallback == null) {
            sConfigCallback = new ConfigCallback(
                    (WindowManager) context.getSystemService(
                            Context.WINDOW_SERVICE));
            ViewRoot.addConfigCallback(sConfigCallback);
        }
        sConfigCallback.addHandler(this);
        mJSInterfaceMap = javascriptInterfaces;
        mSettings = settings;
        mContext = context;
        mCallbackProxy = proxy;
        mDatabase = WebViewDatabase.getInstance(appContext);
        mWebViewCore = w;
        AssetManager am = context.getAssets();
        nativeCreateFrame(w, am, proxy.getBackForwardList());
        if (DebugFlags.BROWSER_FRAME) {
            Log.v(LOGTAG, "BrowserFrame constructor: this=" + this);
        }
    }
    public void loadUrl(String url, Map<String, String> extraHeaders) {
        mLoadInitFromJava = true;
        if (URLUtil.isJavaScriptUrl(url)) {
            stringByEvaluatingJavaScriptFromString(
                    url.substring("javascript:".length()));
        } else {
            nativeLoadUrl(url, extraHeaders);
        }
        mLoadInitFromJava = false;
    }
    public void postUrl(String url, byte[] data) {
        mLoadInitFromJava = true;
        nativePostUrl(url, data);
        mLoadInitFromJava = false;
    }
    public void loadData(String baseUrl, String data, String mimeType,
            String encoding, String historyUrl) {
        mLoadInitFromJava = true;
        if (historyUrl == null || historyUrl.length() == 0) {
            historyUrl = "about:blank";
        }
        if (data == null) {
            data = "";
        }
        if (baseUrl == null || baseUrl.length() == 0) {
            baseUrl = "about:blank";
        }
        if (mimeType == null || mimeType.length() == 0) {
            mimeType = "text/html";
        }
        nativeLoadData(baseUrl, data, mimeType, encoding, historyUrl);
        mLoadInitFromJava = false;
    }
    public void goBackOrForward(int steps) {
        mLoadInitFromJava = true;
        nativeGoBackOrForward(steps);
        mLoadInitFromJava = false;
    }
    private void reportError(final int errorCode, final String description,
            final String failingUrl) {
        resetLoadingStates();
        mCallbackProxy.onReceivedError(errorCode, description, failingUrl);
    }
    private void resetLoadingStates() {
        mCommitted = true;
        mFirstLayoutDone = true;
    }
    boolean committed() {
        return mCommitted;
    }
    boolean firstLayoutDone() {
        return mFirstLayoutDone;
    }
    int loadType() {
        return mLoadType;
    }
    void didFirstLayout() {
        if (!mFirstLayoutDone) {
            mFirstLayoutDone = true;
            mWebViewCore.contentDraw();
        }
    }
    private void loadStarted(String url, Bitmap favicon, int loadType,
            boolean isMainFrame) {
        mIsMainFrame = isMainFrame;
        if (isMainFrame || loadType == FRAME_LOADTYPE_STANDARD) {
            mLoadType = loadType;
            if (isMainFrame) {
                mCallbackProxy.onPageStarted(url, favicon);
                mFirstLayoutDone = false;
                mCommitted = false;
                mWebViewCore.removeMessages(WebViewCore.EventHub.WEBKIT_DRAW);
            }
            if (loadType == FRAME_LOADTYPE_STANDARD
                    && mSettings.getSaveFormData()) {
                final WebHistoryItem h = mCallbackProxy.getBackForwardList()
                        .getCurrentItem();
                if (h != null) {
                    String currentUrl = h.getUrl();
                    if (currentUrl != null) {
                        mDatabase.setFormData(currentUrl, getFormTextData());
                    }
                }
            }
        }
    }
    private void transitionToCommitted(int loadType, boolean isMainFrame) {
        if (isMainFrame) {
            mCommitted = true;
            mWebViewCore.getWebView().mViewManager.postResetStateAll();
        }
    }
    private void loadFinished(String url, int loadType, boolean isMainFrame) {
        if (isMainFrame || loadType == FRAME_LOADTYPE_STANDARD) {
            if (isMainFrame) {
                resetLoadingStates();
                mCallbackProxy.switchOutDrawHistory();
                mCallbackProxy.onPageFinished(url);
            }
        }
    }
    void certificate(SslCertificate certificate) {
        if (mIsMainFrame) {
            mCallbackProxy.onReceivedCertificate(certificate);
        }
    }
    public void destroy() {
        nativeDestroyFrame();
        mBlockMessages = true;
        removeCallbacksAndMessages(null);
    }
    @Override
    public void handleMessage(Message msg) {
        if (mBlockMessages) {
            return;
        }
        switch (msg.what) {
            case FRAME_COMPLETED: {
                if (mSettings.getSavePassword() && hasPasswordField()) {
                    WebHistoryItem item = mCallbackProxy.getBackForwardList()
                            .getCurrentItem();
                    if (item != null) {
                        WebAddress uri = new WebAddress(item.getUrl());
                        String schemePlusHost = uri.mScheme + uri.mHost;
                        String[] up =
                                mDatabase.getUsernamePassword(schemePlusHost);
                        if (up != null && up[0] != null) {
                            setUsernamePassword(up[0], up[1]);
                        }
                    }
                }
                WebViewWorker.getHandler().sendEmptyMessage(
                        WebViewWorker.MSG_TRIM_CACHE);
                break;
            }
            case POLICY_FUNCTION: {
                nativeCallPolicyFunction(msg.arg1, msg.arg2);
                break;
            }
            case ORIENTATION_CHANGED: {
                nativeOrientationChanged(msg.arg1);
                break;
            }
            default:
                break;
        }
    }
    private void setTitle(String title) {
        mCallbackProxy.onReceivedTitle(title);
    }
    public void externalRepresentation(Message callback) {
        callback.obj = externalRepresentation();;
        callback.sendToTarget();
    }
    private native String externalRepresentation();
    public void documentAsText(Message callback) {
        callback.obj = documentAsText();;
        callback.sendToTarget();
    }
    private native String documentAsText();
    private void windowObjectCleared(int nativeFramePointer) {
        if (mJSInterfaceMap != null) {
            Iterator iter = mJSInterfaceMap.keySet().iterator();
            while (iter.hasNext())  {
                String interfaceName = (String) iter.next();
                nativeAddJavascriptInterface(nativeFramePointer,
                        mJSInterfaceMap.get(interfaceName), interfaceName);
            }
        }
    }
    public boolean handleUrl(String url) {
        if (mLoadInitFromJava == true) {
            return false;
        }
        if (mCallbackProxy.shouldOverrideUrlLoading(url)) {
            didFirstLayout();
            return true;
        } else {
            return false;
        }
    }
    public void addJavascriptInterface(Object obj, String interfaceName) {
        if (mJSInterfaceMap == null) {
            mJSInterfaceMap = new HashMap<String, Object>();
        }
        if (mJSInterfaceMap.containsKey(interfaceName)) {
            mJSInterfaceMap.remove(interfaceName);
        }
        mJSInterfaceMap.put(interfaceName, obj);
    }
    private int getFileSize(String uri) {
        int size = 0;
        try {
            InputStream stream = mContext.getContentResolver()
                            .openInputStream(Uri.parse(uri));
            size = stream.available();
            stream.close();
        } catch (Exception e) {}
        return size;
    }
    private int getFile(String uri, byte[] buffer, int offset,
            int expectedSize) {
        int size = 0;
        try {
            InputStream stream = mContext.getContentResolver()
                            .openInputStream(Uri.parse(uri));
            size = stream.available();
            if (size <= expectedSize && buffer != null
                    && buffer.length - offset >= size) {
                stream.read(buffer, offset, size);
            } else {
                size = 0;
            }
            stream.close();
        } catch (java.io.FileNotFoundException e) {
            Log.e(LOGTAG, "FileNotFoundException:" + e);
            size = 0;
        } catch (java.io.IOException e2) {
            Log.e(LOGTAG, "IOException: " + e2);
            size = 0;
        }
        return size;
    }
    private LoadListener startLoadingResource(int loaderHandle,
                                              String url,
                                              String method,
                                              HashMap headers,
                                              byte[] postData,
                                              long postDataIdentifier,
                                              int cacheMode,
                                              boolean mainResource,
                                              boolean userGesture,
                                              boolean synchronous,
                                              String username,
                                              String password) {
        PerfChecker checker = new PerfChecker();
        if (mSettings.getCacheMode() != WebSettings.LOAD_DEFAULT) {
            cacheMode = mSettings.getCacheMode();
        }
        if (method.equals("POST")) {
            if (cacheMode == WebSettings.LOAD_NORMAL) {
                cacheMode = WebSettings.LOAD_NO_CACHE;
            }
            if (mSettings.getSavePassword() && hasPasswordField()) {
                try {
                    if (DebugFlags.BROWSER_FRAME) {
                        Assert.assertNotNull(mCallbackProxy.getBackForwardList()
                                .getCurrentItem());
                    }
                    WebAddress uri = new WebAddress(mCallbackProxy
                            .getBackForwardList().getCurrentItem().getUrl());
                    String schemePlusHost = uri.mScheme + uri.mHost;
                    String[] ret = getUsernamePassword();
                    if (ret != null && postData != null && 
                            ret[0].length() > 0 && ret[1].length() > 0) {
                        String postString = new String(postData);
                        if (postString.contains(URLEncoder.encode(ret[0])) &&
                                postString.contains(URLEncoder.encode(ret[1]))) {
                            String[] saved = mDatabase.getUsernamePassword(
                                    schemePlusHost);
                            if (saved != null) {
                                if (saved[0] != null) {
                                    mDatabase.setUsernamePassword(
                                            schemePlusHost, ret[0], ret[1]);
                                }
                            } else {
                                mCallbackProxy.onSavePassword(schemePlusHost, ret[0], 
                                        ret[1], null);
                            }
                        }
                    }
                } catch (ParseException ex) {
                }
            }
        }
        boolean isMainFramePage = mIsMainFrame;
        if (DebugFlags.BROWSER_FRAME) {
            Log.v(LOGTAG, "startLoadingResource: url=" + url + ", method="
                    + method + ", postData=" + postData + ", isMainFramePage="
                    + isMainFramePage + ", mainResource=" + mainResource
                    + ", userGesture=" + userGesture);
        }
        LoadListener loadListener = LoadListener.getLoadListener(mContext,
                this, url, loaderHandle, synchronous, isMainFramePage,
                mainResource, userGesture, postDataIdentifier, username, password);
        mCallbackProxy.onLoadResource(url);
        if (LoadListener.getNativeLoaderCount() > MAX_OUTSTANDING_REQUESTS) {
            loadListener.error(
                    android.net.http.EventHandler.ERROR, mContext.getString(
                            com.android.internal.R.string.httpErrorTooManyRequests));
            return loadListener;
        }
        FrameLoader loader = new FrameLoader(loadListener, mSettings, method);
        loader.setHeaders(headers);
        loader.setPostData(postData);
        loader.setCacheMode(headers.containsKey("If-Modified-Since")
                || headers.containsKey("If-None-Match") ? 
                        WebSettings.LOAD_NO_CACHE : cacheMode);
        if (!loader.executeLoad()) {
            checker.responseAlert("startLoadingResource fail");
        }
        checker.responseAlert("startLoadingResource succeed");
        return !synchronous ? loadListener : null;
    }
    private void setProgress(int newProgress) {
        mCallbackProxy.onProgressChanged(newProgress);
        if (newProgress == 100) {
            sendMessageDelayed(obtainMessage(FRAME_COMPLETED), 100);
        }
        if (mFirstLayoutDone && newProgress > TRANSITION_SWITCH_THRESHOLD) {
            mCallbackProxy.switchOutDrawHistory();
        }
    }
    private void didReceiveIcon(Bitmap icon) {
        mCallbackProxy.onReceivedIcon(icon);
    }
    private void didReceiveTouchIconUrl(String url, boolean precomposed) {
        mCallbackProxy.onReceivedTouchIconUrl(url, precomposed);
    }
    private BrowserFrame createWindow(boolean dialog, boolean userGesture) {
        WebView w = mCallbackProxy.createWindow(dialog, userGesture);
        if (w != null) {
            return w.getWebViewCore().getBrowserFrame();
        }
        return null;
    }
    private void requestFocus() {
        mCallbackProxy.onRequestFocus();
    }
    private void closeWindow(WebViewCore w) {
        mCallbackProxy.onCloseWindow(w.getWebView());
    }
    static final int POLICY_USE = 0;
    static final int POLICY_IGNORE = 2;
    private void decidePolicyForFormResubmission(int policyFunction) {
        Message dontResend = obtainMessage(POLICY_FUNCTION, policyFunction,
                POLICY_IGNORE);
        Message resend = obtainMessage(POLICY_FUNCTION, policyFunction,
                POLICY_USE);
        mCallbackProxy.onFormResubmission(dontResend, resend);
    }
    private void updateVisitedHistory(String url, boolean isReload) {
        mCallbackProxy.doUpdateVisitedHistory(url, isReload);
    }
     CallbackProxy getCallbackProxy() {
        return mCallbackProxy;
    }
    String getUserAgentString() {
        return mSettings.getUserAgentString();
    }
    private static final int NODOMAIN = 1;
    private static final int LOADERROR = 2;
    private static final int DRAWABLEDIR = 3;
    private static final int FILE_UPLOAD_LABEL = 4;
    private static final int RESET_LABEL = 5;
    private static final int SUBMIT_LABEL = 6;
    String getRawResFilename(int id) {
        int resid;
        switch (id) {
            case NODOMAIN:
                resid = com.android.internal.R.raw.nodomain;
                break;
            case LOADERROR:
                resid = com.android.internal.R.raw.loaderror;
                break;
            case DRAWABLEDIR:
                resid = com.android.internal.R.drawable.btn_check_off;
                break;
            case FILE_UPLOAD_LABEL:
                return mContext.getResources().getString(
                        com.android.internal.R.string.upload_file);
            case RESET_LABEL:
                return mContext.getResources().getString(
                        com.android.internal.R.string.reset);
            case SUBMIT_LABEL:
                return mContext.getResources().getString(
                        com.android.internal.R.string.submit);
            default:
                Log.e(LOGTAG, "getRawResFilename got incompatible resource ID");
                return "";
        }
        TypedValue value = new TypedValue();
        mContext.getResources().getValue(resid, value, true);
        if (id == DRAWABLEDIR) {
            String path = value.string.toString();
            int index = path.lastIndexOf('/');
            if (index < 0) {
                Log.e(LOGTAG, "Can't find drawable directory.");
                return "";
            }
            return path.substring(0, index + 1);
        }
        return value.string.toString();
    }
    private float density() {
        return mContext.getResources().getDisplayMetrics().density;
    }
    private native void nativeCreateFrame(WebViewCore w, AssetManager am,
            WebBackForwardList list);
    public native void nativeDestroyFrame();
    private native void nativeCallPolicyFunction(int policyFunction,
            int decision);
    public native void reload(boolean allowStale);
    private native void nativeGoBackOrForward(int steps);
    public native String stringByEvaluatingJavaScriptFromString(String script);
    private native void nativeAddJavascriptInterface(int nativeFramePointer,
            Object obj, String interfaceName);
    private native void setCacheDisabled(boolean disabled);
    public native boolean cacheDisabled();
    public native void clearCache();
    private native void nativeLoadUrl(String url, Map<String, String> headers);
    private native void nativePostUrl(String url, byte[] postData);
    private native void nativeLoadData(String baseUrl, String data,
            String mimeType, String encoding, String historyUrl);
    public void stopLoading() {
        if (mIsMainFrame) {
            resetLoadingStates();
        }
        nativeStopLoading();
    }
    private native void nativeStopLoading();
    public native boolean documentHasImages();
    private native boolean hasPasswordField();
    private native String[] getUsernamePassword();
    private native void setUsernamePassword(String username, String password);
    private native HashMap getFormTextData();
    private native void nativeOrientationChanged(int orientation);
}
