public class WebSettings {
    public enum LayoutAlgorithm {
        NORMAL,
        SINGLE_COLUMN,
        NARROW_COLUMNS
    }
    public enum TextSize {
        SMALLEST(50),
        SMALLER(75),
        NORMAL(100),
        LARGER(150),
        LARGEST(200);
        TextSize(int size) {
            value = size;
        }
        int value;
    }
    public enum ZoomDensity {
        FAR(150),      
        MEDIUM(100),    
        CLOSE(75);     
        ZoomDensity(int size) {
            value = size;
        }
        int value;
    }
    public static final int LOAD_DEFAULT = -1;
    public static final int LOAD_NORMAL = 0;
    public static final int LOAD_CACHE_ELSE_NETWORK = 1;
    public static final int LOAD_NO_CACHE = 2;
    public static final int LOAD_CACHE_ONLY = 3;
    public enum RenderPriority {
        NORMAL,
        HIGH,
        LOW
    }
    public enum PluginState {
        ON,
        ON_DEMAND,
        OFF
    }
    private WebView mWebView;
    private BrowserFrame mBrowserFrame;
    private boolean mSyncPending = false;
    private final EventHandler mEventHandler;
    private LayoutAlgorithm mLayoutAlgorithm = LayoutAlgorithm.NARROW_COLUMNS;
    private Context         mContext;
    private TextSize        mTextSize = TextSize.NORMAL;
    private String          mStandardFontFamily = "sans-serif";
    private String          mFixedFontFamily = "monospace";
    private String          mSansSerifFontFamily = "sans-serif";
    private String          mSerifFontFamily = "serif";
    private String          mCursiveFontFamily = "cursive";
    private String          mFantasyFontFamily = "fantasy";
    private String          mDefaultTextEncoding;
    private String          mUserAgent;
    private boolean         mUseDefaultUserAgent;
    private String          mAcceptLanguage;
    private int             mMinimumFontSize = 8;
    private int             mMinimumLogicalFontSize = 8;
    private int             mDefaultFontSize = 16;
    private int             mDefaultFixedFontSize = 13;
    private int             mPageCacheCapacity = 0;
    private boolean         mLoadsImagesAutomatically = true;
    private boolean         mBlockNetworkImage = false;
    private boolean         mBlockNetworkLoads;
    private boolean         mJavaScriptEnabled = false;
    private PluginState     mPluginState = PluginState.OFF;
    private boolean         mJavaScriptCanOpenWindowsAutomatically = false;
    private boolean         mUseDoubleTree = false;
    private boolean         mUseWideViewport = false;
    private boolean         mSupportMultipleWindows = false;
    private boolean         mShrinksStandaloneImagesToFit = false;
    private boolean         mAppCacheEnabled = false;
    private boolean         mDatabaseEnabled = false;
    private boolean         mDomStorageEnabled = false;
    private boolean         mWorkersEnabled = false;  
    private boolean         mGeolocationEnabled = true;
    private long            mAppCacheMaxSize = Long.MAX_VALUE;
    private String          mAppCachePath = "";
    private String          mDatabasePath = "";
    private boolean         mDatabasePathHasBeenSet = false;
    private String          mGeolocationDatabasePath = "";
    private ZoomDensity     mDefaultZoom = ZoomDensity.MEDIUM;
    private RenderPriority  mRenderPriority = RenderPriority.NORMAL;
    private int             mOverrideCacheMode = LOAD_DEFAULT;
    private boolean         mSaveFormData = true;
    private boolean         mSavePassword = true;
    private boolean         mLightTouchEnabled = false;
    private boolean         mNeedInitialFocus = true;
    private boolean         mNavDump = false;
    private boolean         mSupportZoom = true;
    private boolean         mBuiltInZoomControls = false;
    private boolean         mAllowFileAccess = true;
    private boolean         mLoadWithOverviewMode = false;
    static private int      mDoubleTapToastCount = 3;
    private static final String PREF_FILE = "WebViewSettings";
    private static final String DOUBLE_TAP_TOAST_COUNT = "double_tap_toast_count";
    private class EventHandler {
        static final int SYNC = 0;
        static final int PRIORITY = 1;
        static final int SET_DOUBLE_TAP_TOAST_COUNT = 2;
        private Handler mHandler;
        private synchronized void createHandler() {
            setRenderPriority();
            mHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    switch (msg.what) {
                        case SYNC:
                            synchronized (WebSettings.this) {
                                if (mBrowserFrame.mNativeFrame != 0) {
                                    nativeSync(mBrowserFrame.mNativeFrame);
                                }
                                mSyncPending = false;
                            }
                            break;
                        case PRIORITY: {
                            setRenderPriority();
                            break;
                        }
                        case SET_DOUBLE_TAP_TOAST_COUNT: {
                            SharedPreferences.Editor editor = mContext
                                    .getSharedPreferences(PREF_FILE,
                                            Context.MODE_PRIVATE).edit();
                            editor.putInt(DOUBLE_TAP_TOAST_COUNT,
                                    mDoubleTapToastCount);
                            editor.commit();
                            break;
                        }
                    }
                }
            };
        }
        private void setRenderPriority() {
            synchronized (WebSettings.this) {
                if (mRenderPriority == RenderPriority.NORMAL) {
                    android.os.Process.setThreadPriority(
                            android.os.Process.THREAD_PRIORITY_DEFAULT);
                } else if (mRenderPriority == RenderPriority.HIGH) {
                    android.os.Process.setThreadPriority(
                            android.os.Process.THREAD_PRIORITY_FOREGROUND +
                            android.os.Process.THREAD_PRIORITY_LESS_FAVORABLE);
                } else if (mRenderPriority == RenderPriority.LOW) {
                    android.os.Process.setThreadPriority(
                            android.os.Process.THREAD_PRIORITY_BACKGROUND);
                }
            }
        }
        private synchronized boolean sendMessage(Message msg) {
            if (mHandler != null) {
                mHandler.sendMessage(msg);
                return true;
            } else {
                return false;
            }
        }
    }
    private static final String DESKTOP_USERAGENT =
            "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_5_7; en-us)"
            + " AppleWebKit/530.17 (KHTML, like Gecko) Version/4.0"
            + " Safari/530.17";
    private static final String IPHONE_USERAGENT = 
            "Mozilla/5.0 (iPhone; U; CPU iPhone OS 3_0 like Mac OS X; en-us)"
            + " AppleWebKit/528.18 (KHTML, like Gecko) Version/4.0"
            + " Mobile/7A341 Safari/528.16";
    private static Locale sLocale;
    private static Object sLockForLocaleSettings;
    WebSettings(Context context, WebView webview) {
        mEventHandler = new EventHandler();
        mContext = context;
        mWebView = webview;
        mDefaultTextEncoding = context.getString(com.android.internal.
                                                 R.string.default_text_encoding);
        if (sLockForLocaleSettings == null) {
            sLockForLocaleSettings = new Object();
            sLocale = Locale.getDefault();
        }
        mAcceptLanguage = getCurrentAcceptLanguage();
        mUserAgent = getCurrentUserAgent();
        mUseDefaultUserAgent = true;
        mBlockNetworkLoads = mContext.checkPermission(
                "android.permission.INTERNET", android.os.Process.myPid(),
                android.os.Process.myUid()) != PackageManager.PERMISSION_GRANTED;
    }
    private String getCurrentAcceptLanguage() {
        Locale locale;
        synchronized(sLockForLocaleSettings) {
            locale = sLocale;
        }
        StringBuffer buffer = new StringBuffer();
        final String language = locale.getLanguage();
        if (language != null) {
            buffer.append(language);
            final String country = locale.getCountry();
            if (country != null) {
                buffer.append("-");
                buffer.append(country);
            }
        }
        if (!locale.equals(Locale.US)) {
            buffer.append(", ");
            java.util.Locale us = Locale.US;
            if (us.getLanguage() != null) {
                buffer.append(us.getLanguage());
                final String country = us.getCountry();
                if (country != null) {
                    buffer.append("-");
                    buffer.append(country);
                }
            }
        }
        return buffer.toString();
    }
    private synchronized String getCurrentUserAgent() {
        Locale locale;
        synchronized(sLockForLocaleSettings) {
            locale = sLocale;
        }
        StringBuffer buffer = new StringBuffer();
        final String version = Build.VERSION.RELEASE;
        if (version.length() > 0) {
            buffer.append(version);
        } else {
            buffer.append("1.0");
        }  
        buffer.append("; ");
        final String language = locale.getLanguage();
        if (language != null) {
            buffer.append(language.toLowerCase());
            final String country = locale.getCountry();
            if (country != null) {
                buffer.append("-");
                buffer.append(country.toLowerCase());
            }
        } else {
            buffer.append("en");
        }
        if ("REL".equals(Build.VERSION.CODENAME)) {
            final String model = Build.MODEL;
            if (model.length() > 0) {
                buffer.append("; ");
                buffer.append(model);
            }
        }
        final String id = Build.ID;
        if (id.length() > 0) {
            buffer.append(" Build/");
            buffer.append(id);
        }
        final String base = mContext.getResources().getText(
                com.android.internal.R.string.web_user_agent).toString();
        return String.format(base, buffer);
    }
    public void setNavDump(boolean enabled) {
        mNavDump = enabled;
    }
    public boolean getNavDump() {
        return mNavDump;
    }
    public void setSupportZoom(boolean support) {
        mSupportZoom = support;
        mWebView.updateMultiTouchSupport(mContext);
    }
    public boolean supportZoom() {
        return mSupportZoom;
    }
    public void setBuiltInZoomControls(boolean enabled) {
        mBuiltInZoomControls = enabled;
        mWebView.updateMultiTouchSupport(mContext);
    }
    public boolean getBuiltInZoomControls() {
        return mBuiltInZoomControls;
    }
    public void setAllowFileAccess(boolean allow) {
        mAllowFileAccess = allow;
    }
    public boolean getAllowFileAccess() {
        return mAllowFileAccess;
    }
    public void setLoadWithOverviewMode(boolean overview) {
        mLoadWithOverviewMode = overview;
    }
    public boolean getLoadWithOverviewMode() {
        return mLoadWithOverviewMode;
    }
    public void setSaveFormData(boolean save) {
        mSaveFormData = save;
    }
    public boolean getSaveFormData() {
        return mSaveFormData;
    }
    public void setSavePassword(boolean save) {
        mSavePassword = save;
    }
    public boolean getSavePassword() {
        return mSavePassword;
    }
    public synchronized void setTextSize(TextSize t) {
        if (WebView.mLogEvent && mTextSize != t ) {
            EventLog.writeEvent(EventLogTags.BROWSER_TEXT_SIZE_CHANGE,
                    mTextSize.value, t.value);
        }
        mTextSize = t;
        postSync();
    }
    public synchronized TextSize getTextSize() {
        return mTextSize;
    }
    public void setDefaultZoom(ZoomDensity zoom) {
        if (mDefaultZoom != zoom) {
            mDefaultZoom = zoom;
            mWebView.updateDefaultZoomDensity(zoom.value);
        }
    }
    public ZoomDensity getDefaultZoom() {
        return mDefaultZoom;
    }
    public void setLightTouchEnabled(boolean enabled) {
        mLightTouchEnabled = enabled;
    }
    public boolean getLightTouchEnabled() {
        return mLightTouchEnabled;
    }
    @Deprecated
    public synchronized void setUseDoubleTree(boolean use) {
        return;
    }
    @Deprecated
    public synchronized boolean getUseDoubleTree() {
        return false;
    }
    @Deprecated
    public synchronized void setUserAgent(int ua) {
        String uaString = null;
        if (ua == 1) {
            if (DESKTOP_USERAGENT.equals(mUserAgent)) {
                return; 
            } else {
                uaString = DESKTOP_USERAGENT;
            }
        } else if (ua == 2) {
            if (IPHONE_USERAGENT.equals(mUserAgent)) {
                return; 
            } else {
                uaString = IPHONE_USERAGENT;
            }
        } else if (ua != 0) {
            return; 
        }
        setUserAgentString(uaString);
    }
    @Deprecated
    public synchronized int getUserAgent() {
        if (DESKTOP_USERAGENT.equals(mUserAgent)) {
            return 1;
        } else if (IPHONE_USERAGENT.equals(mUserAgent)) {
            return 2;
        } else if (mUseDefaultUserAgent) {
            return 0;
        }
        return -1;
    }
    public synchronized void setUseWideViewPort(boolean use) {
        if (mUseWideViewport != use) {
            mUseWideViewport = use;
            postSync();
        }
    }
    public synchronized boolean getUseWideViewPort() {
        return mUseWideViewport;
    }
    public synchronized void setSupportMultipleWindows(boolean support) {
        if (mSupportMultipleWindows != support) {
            mSupportMultipleWindows = support;
            postSync();
        }
    }
    public synchronized boolean supportMultipleWindows() {
        return mSupportMultipleWindows;
    }
    public synchronized void setLayoutAlgorithm(LayoutAlgorithm l) {
        if (mLayoutAlgorithm != l) {
            mLayoutAlgorithm = l;
            postSync();
        }
    }
    public synchronized LayoutAlgorithm getLayoutAlgorithm() {
        return mLayoutAlgorithm;
    }
    public synchronized void setStandardFontFamily(String font) {
        if (font != null && !font.equals(mStandardFontFamily)) {
            mStandardFontFamily = font;
            postSync();
        }
    }
    public synchronized String getStandardFontFamily() {
        return mStandardFontFamily;
    }
    public synchronized void setFixedFontFamily(String font) {
        if (font != null && !font.equals(mFixedFontFamily)) {
            mFixedFontFamily = font;
            postSync();
        }
    }
    public synchronized String getFixedFontFamily() {
        return mFixedFontFamily;
    }
    public synchronized void setSansSerifFontFamily(String font) {
        if (font != null && !font.equals(mSansSerifFontFamily)) {
            mSansSerifFontFamily = font;
            postSync();
        }
    }
    public synchronized String getSansSerifFontFamily() {
        return mSansSerifFontFamily;
    }
    public synchronized void setSerifFontFamily(String font) {
        if (font != null && !font.equals(mSerifFontFamily)) {
            mSerifFontFamily = font;
            postSync();
        }
    }
    public synchronized String getSerifFontFamily() {
        return mSerifFontFamily;
    }
    public synchronized void setCursiveFontFamily(String font) {
        if (font != null && !font.equals(mCursiveFontFamily)) {
            mCursiveFontFamily = font;
            postSync();
        }
    }
    public synchronized String getCursiveFontFamily() {
        return mCursiveFontFamily;
    }
    public synchronized void setFantasyFontFamily(String font) {
        if (font != null && !font.equals(mFantasyFontFamily)) {
            mFantasyFontFamily = font;
            postSync();
        }
    }
    public synchronized String getFantasyFontFamily() {
        return mFantasyFontFamily;
    }
    public synchronized void setMinimumFontSize(int size) {
        size = pin(size);
        if (mMinimumFontSize != size) {
            mMinimumFontSize = size;
            postSync();
        }
    }
    public synchronized int getMinimumFontSize() {
        return mMinimumFontSize;
    }
    public synchronized void setMinimumLogicalFontSize(int size) {
        size = pin(size);
        if (mMinimumLogicalFontSize != size) {
            mMinimumLogicalFontSize = size;
            postSync();
        }
    }
    public synchronized int getMinimumLogicalFontSize() {
        return mMinimumLogicalFontSize;
    }
    public synchronized void setDefaultFontSize(int size) {
        size = pin(size);
        if (mDefaultFontSize != size) {
            mDefaultFontSize = size;
            postSync();
        }
    }
    public synchronized int getDefaultFontSize() {
        return mDefaultFontSize;
    }
    public synchronized void setDefaultFixedFontSize(int size) {
        size = pin(size);
        if (mDefaultFixedFontSize != size) {
            mDefaultFixedFontSize = size;
            postSync();
        }
    }
    public synchronized int getDefaultFixedFontSize() {
        return mDefaultFixedFontSize;
    }
    public synchronized void setPageCacheCapacity(int size) {
        if (size < 0) size = 0;
        if (size > 20) size = 20;
        if (mPageCacheCapacity != size) {
            mPageCacheCapacity = size;
            postSync();
        }
    }
    public synchronized void setLoadsImagesAutomatically(boolean flag) {
        if (mLoadsImagesAutomatically != flag) {
            mLoadsImagesAutomatically = flag;
            postSync();
        }
    }
    public synchronized boolean getLoadsImagesAutomatically() {
        return mLoadsImagesAutomatically;
    }
    public synchronized void setBlockNetworkImage(boolean flag) {
        if (mBlockNetworkImage != flag) {
            mBlockNetworkImage = flag;
            postSync();
        }
    }
    public synchronized boolean getBlockNetworkImage() {
        return mBlockNetworkImage;
    }
    public synchronized void setBlockNetworkLoads(boolean flag) {
        if (mBlockNetworkLoads != flag) {
            mBlockNetworkLoads = flag;
            verifyNetworkAccess();
        }
    }
    public synchronized boolean getBlockNetworkLoads() {
        return mBlockNetworkLoads;
    }
    private void verifyNetworkAccess() {
        if (!mBlockNetworkLoads) {
            if (mContext.checkPermission("android.permission.INTERNET", 
                    android.os.Process.myPid(), android.os.Process.myUid()) != 
                        PackageManager.PERMISSION_GRANTED) {
                throw new SecurityException
                        ("Permission denied - " +
                                "application missing INTERNET permission");
            }
        }
    }
    public synchronized void setJavaScriptEnabled(boolean flag) {
        if (mJavaScriptEnabled != flag) {
            mJavaScriptEnabled = flag;
            postSync();
        }
    }
    public synchronized void setPluginsEnabled(boolean flag) {
        setPluginState(PluginState.ON);
    }
    public synchronized void setPluginState(PluginState state) {
        if (mPluginState != state) {
            mPluginState = state;
            postSync();
        }
    }
    public synchronized void setPluginsPath(String pluginsPath) {
    }
    public synchronized void setDatabasePath(String databasePath) {
        if (databasePath != null && !mDatabasePathHasBeenSet) {
            mDatabasePath = databasePath;
            mDatabasePathHasBeenSet = true;
            postSync();
        }
    }
    public synchronized void setGeolocationDatabasePath(String databasePath) {
        if (databasePath != null
                && !databasePath.equals(mGeolocationDatabasePath)) {
            mGeolocationDatabasePath = databasePath;
            postSync();
        }
    }
    public synchronized void setAppCacheEnabled(boolean flag) {
        if (mAppCacheEnabled != flag) {
            mAppCacheEnabled = flag;
            postSync();
        }
    }
    public synchronized void setAppCachePath(String appCachePath) {
        if (appCachePath != null && !appCachePath.equals(mAppCachePath)) {
            mAppCachePath = appCachePath;
            postSync();
        }
    }
    public synchronized void setAppCacheMaxSize(long appCacheMaxSize) {
        if (appCacheMaxSize != mAppCacheMaxSize) {
            mAppCacheMaxSize = appCacheMaxSize;
            postSync();
        }
    }
    public synchronized void setDatabaseEnabled(boolean flag) {
       if (mDatabaseEnabled != flag) {
           mDatabaseEnabled = flag;
           postSync();
       }
    }
    public synchronized void setDomStorageEnabled(boolean flag) {
       if (mDomStorageEnabled != flag) {
           mDomStorageEnabled = flag;
           postSync();
       }
    }
    public synchronized boolean getDomStorageEnabled() {
       return mDomStorageEnabled;
    }
    public synchronized String getDatabasePath() {
        return mDatabasePath;
    }
    public synchronized boolean getDatabaseEnabled() {
        return mDatabaseEnabled;
    }
    public synchronized void setWorkersEnabled(boolean flag) {
        if (mWorkersEnabled != flag) {
            mWorkersEnabled = flag;
            postSync();
        }
    }
    public synchronized void setGeolocationEnabled(boolean flag) {
        if (mGeolocationEnabled != flag) {
            mGeolocationEnabled = flag;
            postSync();
        }
    }
    public synchronized boolean getJavaScriptEnabled() {
        return mJavaScriptEnabled;
    }
    public synchronized boolean getPluginsEnabled() {
        return mPluginState == PluginState.ON;
    }
    public synchronized PluginState getPluginState() {
        return mPluginState;
    }
    public synchronized String getPluginsPath() {
        return "";
    }
    public synchronized void setJavaScriptCanOpenWindowsAutomatically(
            boolean flag) {
        if (mJavaScriptCanOpenWindowsAutomatically != flag) {
            mJavaScriptCanOpenWindowsAutomatically = flag;
            postSync();
        }
    }
    public synchronized boolean getJavaScriptCanOpenWindowsAutomatically() {
        return mJavaScriptCanOpenWindowsAutomatically;
    }
    public synchronized void setDefaultTextEncodingName(String encoding) {
        if (encoding != null && !encoding.equals(mDefaultTextEncoding)) {
            mDefaultTextEncoding = encoding;
            postSync();
        }
    }
    public synchronized String getDefaultTextEncodingName() {
        return mDefaultTextEncoding;
    }
    public synchronized void setUserAgentString(String ua) {
        if (ua == null || ua.length() == 0) {
            synchronized(sLockForLocaleSettings) {
                Locale currentLocale = Locale.getDefault(); 
                if (!sLocale.equals(currentLocale)) {
                    sLocale = currentLocale;
                    mAcceptLanguage = getCurrentAcceptLanguage();
                }
            }
            ua = getCurrentUserAgent();
            mUseDefaultUserAgent = true;
        } else  {
            mUseDefaultUserAgent = false;
        }
        if (!ua.equals(mUserAgent)) {
            mUserAgent = ua;
            postSync();
        }
    }
    public synchronized String getUserAgentString() {
        if (DESKTOP_USERAGENT.equals(mUserAgent) ||
                IPHONE_USERAGENT.equals(mUserAgent) ||
                !mUseDefaultUserAgent) {
            return mUserAgent;
        }
        boolean doPostSync = false;
        synchronized(sLockForLocaleSettings) {
            Locale currentLocale = Locale.getDefault();
            if (!sLocale.equals(currentLocale)) {
                sLocale = currentLocale;
                mUserAgent = getCurrentUserAgent();
                mAcceptLanguage = getCurrentAcceptLanguage();
                doPostSync = true;
            }
        }
        if (doPostSync) {
            postSync();
        }
        return mUserAgent;
    }
     synchronized String getAcceptLanguage() {
        synchronized(sLockForLocaleSettings) {
            Locale currentLocale = Locale.getDefault();
            if (!sLocale.equals(currentLocale)) {
                sLocale = currentLocale;
                mAcceptLanguage = getCurrentAcceptLanguage();
            }
        }
        return mAcceptLanguage;
    }
    public void setNeedInitialFocus(boolean flag) {
        if (mNeedInitialFocus != flag) {
            mNeedInitialFocus = flag;
        }
    }
     boolean getNeedInitialFocus() {
        return mNeedInitialFocus;
    }
    public synchronized void setRenderPriority(RenderPriority priority) {
        if (mRenderPriority != priority) {
            mRenderPriority = priority;
            mEventHandler.sendMessage(Message.obtain(null,
                    EventHandler.PRIORITY));
        }
    }
    public void setCacheMode(int mode) {
        if (mode != mOverrideCacheMode) {
            mOverrideCacheMode = mode;
        }
    }
    public int getCacheMode() {
        return mOverrideCacheMode;
    }
    public void setShrinksStandaloneImagesToFit(boolean shrink) {
        if (mShrinksStandaloneImagesToFit != shrink) {
            mShrinksStandaloneImagesToFit = shrink;
            postSync();
        }
     }
    int getDoubleTapToastCount() {
        return mDoubleTapToastCount;
    }
    void setDoubleTapToastCount(int count) {
        if (mDoubleTapToastCount != count) {
            mDoubleTapToastCount = count;
            mEventHandler.sendMessage(Message.obtain(null,
                    EventHandler.SET_DOUBLE_TAP_TOAST_COUNT));
        }
    }
    synchronized void syncSettingsAndCreateHandler(BrowserFrame frame) {
        mBrowserFrame = frame;
        if (DebugFlags.WEB_SETTINGS) {
            junit.framework.Assert.assertTrue(frame.mNativeFrame != 0);
        }
        SharedPreferences sp = mContext.getSharedPreferences(PREF_FILE,
                Context.MODE_PRIVATE);
        if (mDoubleTapToastCount > 0) {
            mDoubleTapToastCount = sp.getInt(DOUBLE_TAP_TOAST_COUNT,
                    mDoubleTapToastCount);
        }
        nativeSync(frame.mNativeFrame);
        mSyncPending = false;
        mEventHandler.createHandler();
    }
    synchronized void onDestroyed() {
    }
    private int pin(int size) {
        if (size < 1) {
            return 1;
        } else if (size > 72) {
            return 72;
        }
        return size;
    }
    private synchronized void postSync() {
        if (!mSyncPending) {
            mSyncPending = mEventHandler.sendMessage(
                    Message.obtain(null, EventHandler.SYNC));
        }
    }
    private native void nativeSync(int nativeFrame);
}
