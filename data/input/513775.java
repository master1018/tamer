final class JWebCoreJavaBridge extends Handler {
    private static final int TIMER_MESSAGE = 1;
    private static final int FUNCPTR_MESSAGE = 2;
    private static final String LOGTAG = "webkit-timers";
    private int mNativeBridge;
    private boolean mHasInstantTimer;
    private int mPauseTimerRefCount;
    private boolean mTimerPaused;
    private boolean mHasDeferredTimers;
    private WebView mCurrentMainWebView;
    static final int REFRESH_PLUGINS = 100;
    public JWebCoreJavaBridge() {
        nativeConstructor();
    }
    @Override
    protected void finalize() {
        nativeFinalize();
    }
    synchronized void setActiveWebView(WebView webview) {
        if (mCurrentMainWebView != null) {
            return;
        }
        mCurrentMainWebView = webview;
    }
    synchronized void removeActiveWebView(WebView webview) {
        if (mCurrentMainWebView != webview) {
            return;
        }
        mCurrentMainWebView = null;
    }
    private void fireSharedTimer() { 
        PerfChecker checker = new PerfChecker();
        mHasInstantTimer = false;
        sharedTimerFired();
        checker.responseAlert("sharedTimer");
    }
    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case TIMER_MESSAGE: {
                if (mTimerPaused) {
                    mHasDeferredTimers = true;
                } else {
                    fireSharedTimer();
                }
                break;
            }
            case FUNCPTR_MESSAGE:
                nativeServiceFuncPtrQueue();
                break;
            case REFRESH_PLUGINS:
                nativeUpdatePluginDirectories(PluginManager.getInstance(null)
                        .getPluginDirectories(), ((Boolean) msg.obj)
                        .booleanValue());
                break;
        }
    }
    private void signalServiceFuncPtrQueue() {
        Message msg = obtainMessage(FUNCPTR_MESSAGE);
        sendMessage(msg);
    }
    private native void nativeServiceFuncPtrQueue();
    public void pause() {
        if (--mPauseTimerRefCount == 0) {
            mTimerPaused = true;
            mHasDeferredTimers = false;
        }
    }
    public void resume() {
        if (++mPauseTimerRefCount == 1) {
           mTimerPaused = false;
           if (mHasDeferredTimers) {
               mHasDeferredTimers = false;
               fireSharedTimer();
           }
        }
    }
    public native void setCacheSize(int bytes);
    private void setCookies(String url, String value) {
        if (value.contains("\r") || value.contains("\n")) {
            int size = value.length();
            StringBuilder buffer = new StringBuilder(size);
            int i = 0;
            while (i != -1 && i < size) {
                int ir = value.indexOf('\r', i);
                int in = value.indexOf('\n', i);
                int newi = (ir == -1) ? in : (in == -1 ? ir : (ir < in ? ir
                        : in));
                if (newi > i) {
                    buffer.append(value.subSequence(i, newi));
                } else if (newi == -1) {
                    buffer.append(value.subSequence(i, size));
                    break;
                }
                i = newi + 1;
            }
            value = buffer.toString();
        }
        CookieManager.getInstance().setCookie(url, value);
    }
    private String cookies(String url) {
        return CookieManager.getInstance().getCookie(url);
    }
    private boolean cookiesEnabled() {
        return CookieManager.getInstance().acceptCookie();
    }
    private String[] getPluginDirectories() {
        return PluginManager.getInstance(null).getPluginDirectories();
    }
    private String getPluginSharedDataDirectory() {
        return PluginManager.getInstance(null).getPluginSharedDataDirectory();
    }
    private void setSharedTimer(long timemillis) {
        if (DebugFlags.J_WEB_CORE_JAVA_BRIDGE) Log.v(LOGTAG, "setSharedTimer " + timemillis);
        if (timemillis <= 0) {
            if (mHasInstantTimer) {
                return;
            } else {
                mHasInstantTimer = true;
                Message msg = obtainMessage(TIMER_MESSAGE);
                sendMessageDelayed(msg, timemillis);
            }
        } else {
            Message msg = obtainMessage(TIMER_MESSAGE);
            sendMessageDelayed(msg, timemillis);
        }
    }
    private void stopSharedTimer() {
        if (DebugFlags.J_WEB_CORE_JAVA_BRIDGE) {
            Log.v(LOGTAG, "stopSharedTimer removing all timers");
        }
        removeMessages(TIMER_MESSAGE);
        mHasInstantTimer = false;
        mHasDeferredTimers = false;
    }
    private String[] getKeyStrengthList() {
        return CertTool.getKeyStrengthList();
    }
    synchronized private String getSignedPublicKey(int index, String challenge,
            String url) {
        if (mCurrentMainWebView != null) {
            return CertTool.getSignedPublicKey(
                    mCurrentMainWebView.getContext(), index, challenge);
        } else {
            Log.e(LOGTAG, "There is no active WebView for getSignedPublicKey");
            return "";
        }
    }
    private native void nativeConstructor();
    private native void nativeFinalize();
    private native void sharedTimerFired();
    private native void nativeUpdatePluginDirectories(String[] directories,
            boolean reload);
    public native void setNetworkOnLine(boolean online);
    public native void setNetworkType(String type, String subtype);
    public native void addPackageNames(Set<String> packageNames);
    public native void addPackageName(String packageName);
    public native void removePackageName(String packageName);
}
