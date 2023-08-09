final class WebViewCore {
    private static final String LOGTAG = "webcore";
    static {
        System.loadLibrary("webcore");
    }
    private WebView mWebView;
    private final CallbackProxy mCallbackProxy;
    private final WebSettings mSettings;
    private final Context mContext;
    private int mNativeClass;
    private BrowserFrame mBrowserFrame;
    private Map<String, Object> mJavascriptInterfaces;
    private int mViewportWidth = -1;
    private int mViewportHeight = -1;
    private int mViewportInitialScale = 0;
    private int mViewportMinimumScale = 0;
    private int mViewportMaximumScale = 0;
    private boolean mViewportUserScalable = true;
    private int mViewportDensityDpi = -1;
    private int mRestoredScale = 0;
    private int mRestoredScreenWidthScale = 0;
    private int mRestoredX = 0;
    private int mRestoredY = 0;
    private int mWebkitScrollX = 0;
    private int mWebkitScrollY = 0;
     static final String THREAD_NAME = "WebViewCoreThread";
    public WebViewCore(Context context, WebView w, CallbackProxy proxy,
            Map<String, Object> javascriptInterfaces) {
        mCallbackProxy = proxy;
        mWebView = w;
        mJavascriptInterfaces = javascriptInterfaces;
        mContext = context;
        synchronized (WebViewCore.class) {
            if (sWebCoreHandler == null) {
                Thread t = new Thread(new WebCoreThread());
                t.setName(THREAD_NAME);
                t.start();
                try {
                    WebViewCore.class.wait();
                } catch (InterruptedException e) {
                    Log.e(LOGTAG, "Caught exception while waiting for thread " +
                           "creation.");
                    Log.e(LOGTAG, Log.getStackTraceString(e));
                }
            }
        }
        mEventHub = new EventHub();
        mSettings = new WebSettings(mContext, mWebView);
        WebIconDatabase.getInstance();
        WebStorage.getInstance().createUIHandler();
        GeolocationPermissions.getInstance().createUIHandler();
        Message init = sWebCoreHandler.obtainMessage(
                WebCoreThread.INITIALIZE, this);
        sWebCoreHandler.sendMessage(init);
    }
    private void initialize() {
        mBrowserFrame = new BrowserFrame(mContext, this, mCallbackProxy,
                mSettings, mJavascriptInterfaces);
        mJavascriptInterfaces = null;
        mSettings.syncSettingsAndCreateHandler(mBrowserFrame);
        WebIconDatabase.getInstance().createHandler();
        WebStorage.getInstance().createHandler();
        GeolocationPermissions.getInstance().createHandler();
        mEventHub.transferMessages();
        if (mWebView != null) {
            Message.obtain(mWebView.mPrivateHandler,
                    WebView.WEBCORE_INITIALIZED_MSG_ID,
                    mNativeClass, 0).sendToTarget();
        }
    }
     void initializeSubwindow() {
        initialize();
        sWebCoreHandler.removeMessages(WebCoreThread.INITIALIZE, this);
    }
     BrowserFrame getBrowserFrame() {
        return mBrowserFrame;
    }
    public static void pauseTimers() {
        if (BrowserFrame.sJavaBridge == null) {
            throw new IllegalStateException(
                    "No WebView has been created in this process!");
        }
        BrowserFrame.sJavaBridge.pause();
    }
    public static void resumeTimers() {
        if (BrowserFrame.sJavaBridge == null) {
            throw new IllegalStateException(
                    "No WebView has been created in this process!");
        }
        BrowserFrame.sJavaBridge.resume();
    }
    public WebSettings getSettings() {
        return mSettings;
    }
    protected void addMessageToConsole(String message, int lineNumber, String sourceID,
            int msgLevel) {
        mCallbackProxy.addMessageToConsole(message, lineNumber, sourceID, msgLevel);
    }
    protected void jsAlert(String url, String message) {
        mCallbackProxy.onJsAlert(url, message);
    }
    private String openFileChooser() {
        Uri uri = mCallbackProxy.openFileChooser();
        if (uri == null) return "";
        Cursor cursor = mContext.getContentResolver().query(
                uri,
                new String[] { OpenableColumns.DISPLAY_NAME },
                null,
                null,
                null);
        String name = "";
        if (cursor != null) {
            try {
                if (cursor.moveToNext()) {
                    name = cursor.getString(0);
                }
            } finally {
                cursor.close();
            }
        }
        return uri.toString() + "/" + name;
    }
    protected void exceededDatabaseQuota(String url,
                                         String databaseIdentifier,
                                         long currentQuota,
                                         long estimatedSize) {
        mCallbackProxy.onExceededDatabaseQuota(url, databaseIdentifier,
                currentQuota, estimatedSize, getUsedQuota(),
                new WebStorage.QuotaUpdater() {
                        public void updateQuota(long quota) {
                            nativeSetNewStorageLimit(quota);
                        }
                });
    }
    protected void reachedMaxAppCacheSize(long spaceNeeded) {
        mCallbackProxy.onReachedMaxAppCacheSize(spaceNeeded, getUsedQuota(),
                new WebStorage.QuotaUpdater() {
                    public void updateQuota(long quota) {
                        nativeSetNewStorageLimit(quota);
                    }
                });
    }
    protected void populateVisitedLinks() {
        ValueCallback callback = new ValueCallback<String[]>() {
            public void onReceiveValue(String[] value) {
                sendMessage(EventHub.POPULATE_VISITED_LINKS, (Object)value);
            }
        };
        mCallbackProxy.getVisitedHistory(callback);
    }
    protected void geolocationPermissionsShowPrompt(String origin) {
        mCallbackProxy.onGeolocationPermissionsShowPrompt(origin,
                new GeolocationPermissions.Callback() {
          public void invoke(String origin, boolean allow, boolean remember) {
            GeolocationPermissionsData data = new GeolocationPermissionsData();
            data.mOrigin = origin;
            data.mAllow = allow;
            data.mRemember = remember;
            sendMessage(EventHub.GEOLOCATION_PERMISSIONS_PROVIDE, data);
          }
        });
    }
    protected void geolocationPermissionsHidePrompt() {
        mCallbackProxy.onGeolocationPermissionsHidePrompt();
    }
    protected boolean jsConfirm(String url, String message) {
        return mCallbackProxy.onJsConfirm(url, message);
    }
    protected String jsPrompt(String url, String message, String defaultValue) {
        return mCallbackProxy.onJsPrompt(url, message, defaultValue);
    }
    protected boolean jsUnload(String url, String message) {
        return mCallbackProxy.onJsBeforeUnload(url, message);
    }
    protected boolean jsInterrupt() {
        return mCallbackProxy.onJsTimeout();
    }
    static native String nativeFindAddress(String addr, boolean caseInsensitive);
    private native void nativeClearContent();
    private native void nativeCopyContentToPicture(Picture picture);
    private native boolean nativeDrawContent(Canvas canvas, int color);
    private native boolean nativePictureReady();
    private native boolean nativeRecordContent(Region invalRegion, Point wh);
    private native boolean nativeFocusBoundsChanged();
    private native void nativeSplitContent();
    private native boolean nativeKey(int keyCode, int unichar,
            int repeatCount, boolean isShift, boolean isAlt, boolean isSym,
            boolean isDown);
    private native void nativeClick(int framePtr, int nodePtr);
    private native void nativeSendListBoxChoices(boolean[] choices, int size);
    private native void nativeSendListBoxChoice(int choice);
    private native void nativeSetSize(int width, int height, int screenWidth,
            float scale, int realScreenWidth, int screenHeight, int anchorX,
            int anchorY, boolean ignoreHeight);
    private native int nativeGetContentMinPrefWidth();
    private native void nativeReplaceTextfieldText(
            int oldStart, int oldEnd, String replace, int newStart, int newEnd,
            int textGeneration);
    private native void passToJs(int gen,
            String currentText, int keyCode, int keyValue, boolean down,
            boolean cap, boolean fn, boolean sym);
    private native void nativeSetFocusControllerActive(boolean active);
    private native void nativeSaveDocumentState(int frame);
    private native void nativeMoveFocus(int framePtr, int nodePointer);
    private native void nativeMoveMouse(int framePtr, int x, int y);
    private native void nativeMoveMouseIfLatest(int moveGeneration,
            int framePtr, int x, int y);
    private native String nativeRetrieveHref(int framePtr, int nodePtr);
    private native String nativeRetrieveAnchorText(int framePtr, int nodePtr);
    private native void nativeTouchUp(int touchGeneration,
            int framePtr, int nodePtr, int x, int y);
    private native boolean nativeHandleTouchEvent(int action, int x, int y,
            int metaState);
    private native void nativeUpdateFrameCache();
    private native void nativeSetBackgroundColor(int color);
    private native void nativeDumpDomTree(boolean useFile);
    private native void nativeDumpRenderTree(boolean useFile);
    private native void nativeDumpNavTree();
    private native void nativeDumpV8Counters();
    private native void nativeSetJsFlags(String flags);
    private native void nativeDeleteSelection(int start, int end,
            int textGeneration);
    private native void nativeSetSelection(int start, int end);
    private native void nativeRegisterURLSchemeAsLocal(String scheme);
    private native void nativeSetNewStorageLimit(long limit);
    private native void nativeGeolocationPermissionsProvide(String origin, boolean allow, boolean remember);
    private native void  nativeProvideVisitedHistory(String[] history);
    private final EventHub mEventHub;
    private static Handler sWebCoreHandler;
    private static class WebCoreThread implements Runnable {
        private static final int INITIALIZE = 0;
        private static final int REDUCE_PRIORITY = 1;
        private static final int RESUME_PRIORITY = 2;
        public void run() {
            Looper.prepare();
            Assert.assertNull(sWebCoreHandler);
            synchronized (WebViewCore.class) {
                sWebCoreHandler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        switch (msg.what) {
                            case INITIALIZE:
                                WebViewCore core = (WebViewCore) msg.obj;
                                core.initialize();
                                break;
                            case REDUCE_PRIORITY:
                                Process.setThreadPriority(
                                        Process.THREAD_PRIORITY_DEFAULT + 3 *
                                        Process.THREAD_PRIORITY_LESS_FAVORABLE);
                                break;
                            case RESUME_PRIORITY:
                                Process.setThreadPriority(
                                        Process.THREAD_PRIORITY_DEFAULT);
                                break;
                        }
                    }
                };
                WebViewCore.class.notify();
            }
            Looper.loop();
        }
    }
    static class BaseUrlData {
        String mBaseUrl;
        String mData;
        String mMimeType;
        String mEncoding;
        String mHistoryUrl;
    }
    static class CursorData {
        CursorData() {}
        CursorData(int frame, int node, int x, int y) {
            mFrame = frame;
            mNode = node;
            mX = x;
            mY = y;
        }
        int mMoveGeneration;
        int mFrame;
        int mNode;
        int mX;
        int mY;
    }
    static class JSInterfaceData {
        Object mObject;
        String mInterfaceName;
    }
    static class JSKeyData {
        String mCurrentText;
        KeyEvent mEvent;
    }
    static class MotionUpData {
        int mFrame;
        int mNode;
        Rect mBounds;
        int mX;
        int mY;
    }
    static class GetUrlData {
        String mUrl;
        Map<String, String> mExtraHeaders;
    }
    static class PostUrlData {
        String mUrl;
        byte[] mPostData;
    }
    static class ReplaceTextData {
        String mReplace;
        int mNewStart;
        int mNewEnd;
        int mTextGeneration;
    }
    static class TextSelectionData {
        public TextSelectionData(int start, int end) {
            mStart = start;
            mEnd = end;
        }
        int mStart;
        int mEnd;
    }
    static class TouchUpData {
        int mMoveGeneration;
        int mFrame;
        int mNode;
        int mX;
        int mY;
    }
    static final int ACTION_LONGPRESS = 0x100;
    static final int ACTION_DOUBLETAP = 0x200;
    static class TouchEventData {
        int mAction;
        int mX;
        int mY;
        int mMetaState;
        boolean mReprocess;
        float mViewX;
        float mViewY;
    }
    static class GeolocationPermissionsData {
        String mOrigin;
        boolean mAllow;
        boolean mRemember;
    }
        static final String[] HandlerDebugString = {
            "REQUEST_LABEL", 
            "UPDATE_FRAME_CACHE_IF_LOADING", 
            "SCROLL_TEXT_INPUT", 
            "LOAD_URL", 
            "STOP_LOADING", 
            "RELOAD", 
            "KEY_DOWN", 
            "KEY_UP", 
            "VIEW_SIZE_CHANGED", 
            "GO_BACK_FORWARD", 
            "SET_SCROLL_OFFSET", 
            "RESTORE_STATE", 
            "PAUSE_TIMERS", 
            "RESUME_TIMERS", 
            "CLEAR_CACHE", 
            "CLEAR_HISTORY", 
            "SET_SELECTION", 
            "REPLACE_TEXT", 
            "PASS_TO_JS", 
            "SET_GLOBAL_BOUNDS", 
            "UPDATE_CACHE_AND_TEXT_ENTRY", 
            "CLICK", 
            "SET_NETWORK_STATE", 
            "DOC_HAS_IMAGES", 
            "121", 
            "DELETE_SELECTION", 
            "LISTBOX_CHOICES", 
            "SINGLE_LISTBOX_CHOICE", 
            "MESSAGE_RELAY", 
            "SET_BACKGROUND_COLOR", 
            "SET_MOVE_FOCUS", 
            "SAVE_DOCUMENT_STATE", 
            "129", 
            "WEBKIT_DRAW", 
            "SYNC_SCROLL", 
            "POST_URL", 
            "SPLIT_PICTURE_SET", 
            "CLEAR_CONTENT", 
            "SET_MOVE_MOUSE", 
            "SET_MOVE_MOUSE_IF_LATEST", 
            "REQUEST_CURSOR_HREF", 
            "ADD_JS_INTERFACE", 
            "LOAD_DATA", 
            "TOUCH_UP", 
            "TOUCH_EVENT", 
            "SET_ACTIVE", 
            "ON_PAUSE",     
            "ON_RESUME",    
            "FREE_MEMORY",  
            "VALID_NODE_BOUNDS", 
        };
    class EventHub {
        static final int REQUEST_LABEL = 97;
        static final int UPDATE_FRAME_CACHE_IF_LOADING = 98;
        static final int SCROLL_TEXT_INPUT = 99;
        static final int LOAD_URL = 100;
        static final int STOP_LOADING = 101;
        static final int RELOAD = 102;
        static final int KEY_DOWN = 103;
        static final int KEY_UP = 104;
        static final int VIEW_SIZE_CHANGED = 105;
        static final int GO_BACK_FORWARD = 106;
        static final int SET_SCROLL_OFFSET = 107;
        static final int RESTORE_STATE = 108;
        static final int PAUSE_TIMERS = 109;
        static final int RESUME_TIMERS = 110;
        static final int CLEAR_CACHE = 111;
        static final int CLEAR_HISTORY = 112;
        static final int SET_SELECTION = 113;
        static final int REPLACE_TEXT = 114;
        static final int PASS_TO_JS = 115;
        static final int SET_GLOBAL_BOUNDS = 116;
        static final int UPDATE_CACHE_AND_TEXT_ENTRY = 117;
        static final int CLICK = 118;
        static final int SET_NETWORK_STATE = 119;
        static final int DOC_HAS_IMAGES = 120;
        static final int DELETE_SELECTION = 122;
        static final int LISTBOX_CHOICES = 123;
        static final int SINGLE_LISTBOX_CHOICE = 124;
        static final int MESSAGE_RELAY = 125;
        static final int SET_BACKGROUND_COLOR = 126;
        static final int SET_MOVE_FOCUS = 127;
        static final int SAVE_DOCUMENT_STATE = 128;
        static final int WEBKIT_DRAW = 130;
        static final int SYNC_SCROLL = 131;
        static final int POST_URL = 132;
        static final int SPLIT_PICTURE_SET = 133;
        static final int CLEAR_CONTENT = 134;
        static final int SET_MOVE_MOUSE = 135;
        static final int SET_MOVE_MOUSE_IF_LATEST = 136;
        static final int REQUEST_CURSOR_HREF = 137;
        static final int ADD_JS_INTERFACE = 138;
        static final int LOAD_DATA = 139;
        static final int TOUCH_UP = 140;
        static final int TOUCH_EVENT = 141;
        static final int SET_ACTIVE = 142;
        static final int ON_PAUSE = 143;
        static final int ON_RESUME = 144;
        static final int FREE_MEMORY = 145;
        static final int VALID_NODE_BOUNDS = 146;
        static final int CLEAR_SSL_PREF_TABLE = 150;
        static final int REQUEST_EXT_REPRESENTATION = 160;
        static final int REQUEST_DOC_AS_TEXT = 161;
        static final int DUMP_DOMTREE = 170;
        static final int DUMP_RENDERTREE = 171;
        static final int DUMP_NAVTREE = 172;
        static final int DUMP_V8COUNTERS = 173;
        static final int SET_JS_FLAGS = 174;
        static final int GEOLOCATION_PERMISSIONS_PROVIDE = 180;
        static final int POPULATE_VISITED_LINKS = 181;
        static final int HIDE_FULLSCREEN = 182;
        static final int SET_NETWORK_TYPE = 183;
        static final int ADD_PACKAGE_NAMES = 184;
        static final int ADD_PACKAGE_NAME = 185;
        static final int REMOVE_PACKAGE_NAME = 186;
        private static final int DESTROY =     200;
        private Handler mHandler;
        private ArrayList<Message> mMessages = new ArrayList<Message>();
        private boolean mBlockMessages;
        private int mTid;
        private int mSavedPriority;
        private EventHub() {}
        private void transferMessages() {
            mTid = Process.myTid();
            mSavedPriority = Process.getThreadPriority(mTid);
            mHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    if (DebugFlags.WEB_VIEW_CORE) {
                        Log.v(LOGTAG, (msg.what < REQUEST_LABEL
                                || msg.what
                                > VALID_NODE_BOUNDS ? Integer.toString(msg.what)
                                : HandlerDebugString[msg.what
                                        - REQUEST_LABEL])
                                + " arg1=" + msg.arg1 + " arg2=" + msg.arg2
                                + " obj=" + msg.obj);
                    }
                    switch (msg.what) {
                        case WEBKIT_DRAW:
                            webkitDraw();
                            break;
                        case DESTROY:
                            synchronized (WebViewCore.this) {
                                mBrowserFrame.destroy();
                                mBrowserFrame = null;
                                mSettings.onDestroyed();
                                mNativeClass = 0;
                                mWebView = null;
                            }
                            break;
                        case REQUEST_LABEL:
                            if (mWebView != null) {
                                int nodePointer = msg.arg2;
                                String label = nativeRequestLabel(msg.arg1,
                                        nodePointer);
                                if (label != null && label.length() > 0) {
                                    Message.obtain(mWebView.mPrivateHandler,
                                            WebView.RETURN_LABEL, nodePointer,
                                            0, label).sendToTarget();
                                }
                            }
                            break;
                        case UPDATE_FRAME_CACHE_IF_LOADING:
                            nativeUpdateFrameCacheIfLoading();
                            break;
                        case SCROLL_TEXT_INPUT:
                            nativeScrollFocusedTextInput(
                                    ((Float) msg.obj).floatValue(), msg.arg1);
                            break;
                        case LOAD_URL: {
                            GetUrlData param = (GetUrlData) msg.obj;
                            loadUrl(param.mUrl, param.mExtraHeaders);
                            break;
                        }
                        case POST_URL: {
                            PostUrlData param = (PostUrlData) msg.obj;
                            mBrowserFrame.postUrl(param.mUrl, param.mPostData);
                            break;
                        }
                        case LOAD_DATA:
                            BaseUrlData loadParams = (BaseUrlData) msg.obj;
                            String baseUrl = loadParams.mBaseUrl;
                            if (baseUrl != null) {
                                int i = baseUrl.indexOf(':');
                                if (i > 0) {
                                    String scheme = baseUrl.substring(0, i);
                                    if (!scheme.startsWith("http") &&
                                            !scheme.startsWith("ftp") &&
                                            !scheme.startsWith("about") &&
                                            !scheme.startsWith("javascript")) {
                                        nativeRegisterURLSchemeAsLocal(scheme);
                                    }
                                }
                            }
                            mBrowserFrame.loadData(baseUrl,
                                    loadParams.mData,
                                    loadParams.mMimeType,
                                    loadParams.mEncoding,
                                    loadParams.mHistoryUrl);
                            break;
                        case STOP_LOADING:
                            if (mBrowserFrame.committed()
                                    && !mBrowserFrame.firstLayoutDone()) {
                                mBrowserFrame.didFirstLayout();
                            }
                            stopLoading();
                            break;
                        case RELOAD:
                            mBrowserFrame.reload(false);
                            break;
                        case KEY_DOWN:
                            key((KeyEvent) msg.obj, true);
                            break;
                        case KEY_UP:
                            key((KeyEvent) msg.obj, false);
                            break;
                        case CLICK:
                            nativeClick(msg.arg1, msg.arg2);
                            break;
                        case VIEW_SIZE_CHANGED: {
                            WebView.ViewSizeData data =
                                    (WebView.ViewSizeData) msg.obj;
                            viewSizeChanged(data.mWidth, data.mHeight,
                                    data.mTextWrapWidth, data.mScale,
                                    data.mAnchorX, data.mAnchorY,
                                    data.mIgnoreHeight);
                            break;
                        }
                        case SET_SCROLL_OFFSET:
                            Point pt = (Point) msg.obj;
                            nativeSetScrollOffset(msg.arg1, pt.x, pt.y);
                            break;
                        case SET_GLOBAL_BOUNDS:
                            Rect r = (Rect) msg.obj;
                            nativeSetGlobalBounds(r.left, r.top, r.width(),
                                r.height());
                            break;
                        case GO_BACK_FORWARD:
                            if (!mBrowserFrame.committed() && msg.arg1 == -1 &&
                                    (mBrowserFrame.loadType() ==
                                    BrowserFrame.FRAME_LOADTYPE_STANDARD)) {
                                mBrowserFrame.reload(true);
                            } else {
                                mBrowserFrame.goBackOrForward(msg.arg1);
                            }
                            break;
                        case RESTORE_STATE:
                            stopLoading();
                            restoreState(msg.arg1);
                            break;
                        case PAUSE_TIMERS:
                            mSavedPriority = Process.getThreadPriority(mTid);
                            Process.setThreadPriority(mTid,
                                    Process.THREAD_PRIORITY_BACKGROUND);
                            pauseTimers();
                            WebViewWorker.getHandler().sendEmptyMessage(
                                    WebViewWorker.MSG_PAUSE_CACHE_TRANSACTION);
                            break;
                        case RESUME_TIMERS:
                            Process.setThreadPriority(mTid, mSavedPriority);
                            resumeTimers();
                            WebViewWorker.getHandler().sendEmptyMessage(
                                    WebViewWorker.MSG_RESUME_CACHE_TRANSACTION);
                            break;
                        case ON_PAUSE:
                            nativePause();
                            break;
                        case ON_RESUME:
                            nativeResume();
                            break;
                        case FREE_MEMORY:
                            clearCache(false);
                            nativeFreeMemory();
                            break;
                        case SET_NETWORK_STATE:
                            if (BrowserFrame.sJavaBridge == null) {
                                throw new IllegalStateException("No WebView " +
                                        "has been created in this process!");
                            }
                            BrowserFrame.sJavaBridge
                                    .setNetworkOnLine(msg.arg1 == 1);
                            break;
                        case SET_NETWORK_TYPE:
                            if (BrowserFrame.sJavaBridge == null) {
                                throw new IllegalStateException("No WebView " +
                                        "has been created in this process!");
                            }
                            Map<String, String> map = (Map<String, String>) msg.obj;
                            BrowserFrame.sJavaBridge
                                    .setNetworkType(map.get("type"), map.get("subtype"));
                            break;
                        case CLEAR_CACHE:
                            clearCache(msg.arg1 == 1);
                            break;
                        case CLEAR_HISTORY:
                            mCallbackProxy.getBackForwardList().
                                    close(mBrowserFrame.mNativeFrame);
                            break;
                        case REPLACE_TEXT:
                            ReplaceTextData rep = (ReplaceTextData) msg.obj;
                            nativeReplaceTextfieldText(msg.arg1, msg.arg2,
                                    rep.mReplace, rep.mNewStart, rep.mNewEnd,
                                    rep.mTextGeneration);
                            break;
                        case PASS_TO_JS: {
                            JSKeyData jsData = (JSKeyData) msg.obj;
                            KeyEvent evt = jsData.mEvent;
                            int keyCode = evt.getKeyCode();
                            int keyValue = evt.getUnicodeChar();
                            int generation = msg.arg1;
                            passToJs(generation,
                                    jsData.mCurrentText,
                                    keyCode,
                                    keyValue,
                                    evt.isDown(),
                                    evt.isShiftPressed(), evt.isAltPressed(),
                                    evt.isSymPressed());
                            break;
                        }
                        case SAVE_DOCUMENT_STATE: {
                            CursorData cDat = (CursorData) msg.obj;
                            nativeSaveDocumentState(cDat.mFrame);
                            break;
                        }
                        case CLEAR_SSL_PREF_TABLE:
                            Network.getInstance(mContext)
                                    .clearUserSslPrefTable();
                            break;
                        case TOUCH_UP:
                            TouchUpData touchUpData = (TouchUpData) msg.obj;
                            nativeTouchUp(touchUpData.mMoveGeneration,
                                    touchUpData.mFrame, touchUpData.mNode,
                                    touchUpData.mX, touchUpData.mY);
                            break;
                        case TOUCH_EVENT: {
                            TouchEventData ted = (TouchEventData) msg.obj;
                            Message.obtain(
                                    mWebView.mPrivateHandler,
                                    WebView.PREVENT_TOUCH_ID,
                                    ted.mAction,
                                    nativeHandleTouchEvent(ted.mAction, ted.mX,
                                            ted.mY, ted.mMetaState) ? 1 : 0,
                                    ted.mReprocess ? ted : null).sendToTarget();
                            break;
                        }
                        case SET_ACTIVE:
                            nativeSetFocusControllerActive(msg.arg1 == 1);
                            break;
                        case ADD_JS_INTERFACE:
                            JSInterfaceData jsData = (JSInterfaceData) msg.obj;
                            mBrowserFrame.addJavascriptInterface(jsData.mObject,
                                    jsData.mInterfaceName);
                            break;
                        case REQUEST_EXT_REPRESENTATION:
                            mBrowserFrame.externalRepresentation(
                                    (Message) msg.obj);
                            break;
                        case REQUEST_DOC_AS_TEXT:
                            mBrowserFrame.documentAsText((Message) msg.obj);
                            break;
                        case SET_MOVE_FOCUS:
                            CursorData focusData = (CursorData) msg.obj;
                            nativeMoveFocus(focusData.mFrame, focusData.mNode);
                            break;
                        case SET_MOVE_MOUSE:
                            CursorData cursorData = (CursorData) msg.obj;
                            nativeMoveMouse(cursorData.mFrame,
                                     cursorData.mX, cursorData.mY);
                            break;
                        case SET_MOVE_MOUSE_IF_LATEST:
                            CursorData cData = (CursorData) msg.obj;
                            nativeMoveMouseIfLatest(cData.mMoveGeneration,
                                    cData.mFrame,
                                    cData.mX, cData.mY);
                            break;
                        case REQUEST_CURSOR_HREF: {
                            Message hrefMsg = (Message) msg.obj;
                            hrefMsg.getData().putString("url",
                                    nativeRetrieveHref(msg.arg1, msg.arg2));
                            hrefMsg.getData().putString("title",
                                    nativeRetrieveAnchorText(msg.arg1, msg.arg2));
                            hrefMsg.sendToTarget();
                            break;
                        }
                        case UPDATE_CACHE_AND_TEXT_ENTRY:
                            nativeUpdateFrameCache();
                            if (mWebView != null) {
                                mWebView.postInvalidate();
                            }
                            sendUpdateTextEntry();
                            break;
                        case DOC_HAS_IMAGES:
                            Message imageResult = (Message) msg.obj;
                            imageResult.arg1 =
                                    mBrowserFrame.documentHasImages() ? 1 : 0;
                            imageResult.sendToTarget();
                            break;
                        case DELETE_SELECTION:
                            TextSelectionData deleteSelectionData
                                    = (TextSelectionData) msg.obj;
                            nativeDeleteSelection(deleteSelectionData.mStart,
                                    deleteSelectionData.mEnd, msg.arg1);
                            break;
                        case SET_SELECTION:
                            nativeSetSelection(msg.arg1, msg.arg2);
                            break;
                        case LISTBOX_CHOICES:
                            SparseBooleanArray choices = (SparseBooleanArray)
                                    msg.obj;
                            int choicesSize = msg.arg1;
                            boolean[] choicesArray = new boolean[choicesSize];
                            for (int c = 0; c < choicesSize; c++) {
                                choicesArray[c] = choices.get(c);
                            }
                            nativeSendListBoxChoices(choicesArray,
                                    choicesSize);
                            break;
                        case SINGLE_LISTBOX_CHOICE:
                            nativeSendListBoxChoice(msg.arg1);
                            break;
                        case SET_BACKGROUND_COLOR:
                            nativeSetBackgroundColor(msg.arg1);
                            break;
                        case DUMP_DOMTREE:
                            nativeDumpDomTree(msg.arg1 == 1);
                            break;
                        case DUMP_RENDERTREE:
                            nativeDumpRenderTree(msg.arg1 == 1);
                            break;
                        case DUMP_NAVTREE:
                            nativeDumpNavTree();
                            break;
                        case DUMP_V8COUNTERS:
                            nativeDumpV8Counters();
                            break;
                        case SET_JS_FLAGS:
                            nativeSetJsFlags((String)msg.obj);
                            break;
                        case GEOLOCATION_PERMISSIONS_PROVIDE:
                            GeolocationPermissionsData data =
                                    (GeolocationPermissionsData) msg.obj;
                            nativeGeolocationPermissionsProvide(data.mOrigin,
                                    data.mAllow, data.mRemember);
                            break;
                        case SYNC_SCROLL:
                            mWebkitScrollX = msg.arg1;
                            mWebkitScrollY = msg.arg2;
                            break;
                        case SPLIT_PICTURE_SET:
                            nativeSplitContent();
                            mSplitPictureIsScheduled = false;
                            break;
                        case CLEAR_CONTENT:
                            nativeClearContent();
                            break;
                        case MESSAGE_RELAY:
                            if (msg.obj instanceof Message) {
                                ((Message) msg.obj).sendToTarget();
                            }
                            break;
                        case POPULATE_VISITED_LINKS:
                            nativeProvideVisitedHistory((String[])msg.obj);
                            break;
                        case VALID_NODE_BOUNDS: {
                            MotionUpData motionUpData = (MotionUpData) msg.obj;
                            if (!nativeValidNodeAndBounds(
                                    motionUpData.mFrame, motionUpData.mNode,
                                    motionUpData.mBounds)) {
                                nativeUpdateFrameCache();
                            }
                            Message message = mWebView.mPrivateHandler
                                    .obtainMessage(WebView.DO_MOTION_UP,
                                    motionUpData.mX, motionUpData.mY);
                            mWebView.mPrivateHandler.sendMessageAtFrontOfQueue(
                                    message);
                            break;
                        }
                        case HIDE_FULLSCREEN:
                            nativeFullScreenPluginHidden(msg.arg1);
                            break;
                        case ADD_PACKAGE_NAMES:
                            if (BrowserFrame.sJavaBridge == null) {
                                throw new IllegalStateException("No WebView " +
                                        "has been created in this process!");
                            }
                            BrowserFrame.sJavaBridge.addPackageNames(
                                    (Set<String>) msg.obj);
                            break;
                        case ADD_PACKAGE_NAME:
                            if (BrowserFrame.sJavaBridge == null) {
                                throw new IllegalStateException("No WebView " +
                                        "has been created in this process!");
                            }
                            BrowserFrame.sJavaBridge.addPackageName(
                                    (String) msg.obj);
                            break;
                        case REMOVE_PACKAGE_NAME:
                            if (BrowserFrame.sJavaBridge == null) {
                                throw new IllegalStateException("No WebView " +
                                        "has been created in this process!");
                            }
                            BrowserFrame.sJavaBridge.removePackageName(
                                    (String) msg.obj);
                            break;
                    }
                }
            };
            synchronized (this) {
                int size = mMessages.size();
                for (int i = 0; i < size; i++) {
                    mHandler.sendMessage(mMessages.get(i));
                }
                mMessages = null;
            }
        }
        private synchronized void sendMessage(Message msg) {
            if (mBlockMessages) {
                return;
            }
            if (mMessages != null) {
                mMessages.add(msg);
            } else {
                mHandler.sendMessage(msg);
            }
        }
        private synchronized void removeMessages(int what) {
            if (mBlockMessages) {
                return;
            }
            if (what == EventHub.WEBKIT_DRAW) {
                mDrawIsScheduled = false;
            }
            if (mMessages != null) {
                Log.w(LOGTAG, "Not supported in this case.");
            } else {
                mHandler.removeMessages(what);
            }
        }
        private synchronized boolean hasMessages(int what) {
            if (mBlockMessages) {
                return false;
            }
            if (mMessages != null) {
                Log.w(LOGTAG, "hasMessages() is not supported in this case.");
                return false;
            } else {
                return mHandler.hasMessages(what);
            }
        }
        private synchronized void sendMessageDelayed(Message msg, long delay) {
            if (mBlockMessages) {
                return;
            }
            mHandler.sendMessageDelayed(msg, delay);
        }
        private synchronized void sendMessageAtFrontOfQueue(Message msg) {
            if (mBlockMessages) {
                return;
            }
            if (mMessages != null) {
                mMessages.add(0, msg);
            } else {
                mHandler.sendMessageAtFrontOfQueue(msg);
            }
        }
        private synchronized void removeMessages() {
            mDrawIsScheduled = false;
            mSplitPictureIsScheduled = false;
            if (mMessages != null) {
                mMessages.clear();
            } else {
                mHandler.removeCallbacksAndMessages(null);
            }
        }
        private synchronized void blockMessages() {
            mBlockMessages = true;
        }
    }
    void stopLoading() {
        if (DebugFlags.WEB_VIEW_CORE) Log.v(LOGTAG, "CORE stopLoading");
        if (mBrowserFrame != null) {
            mBrowserFrame.stopLoading();
        }
    }
    void sendMessage(Message msg) {
        mEventHub.sendMessage(msg);
    }
    void sendMessage(int what) {
        mEventHub.sendMessage(Message.obtain(null, what));
    }
    void sendMessage(int what, Object obj) {
        mEventHub.sendMessage(Message.obtain(null, what, obj));
    }
    void sendMessage(int what, int arg1) {
        mEventHub.sendMessage(Message.obtain(null, what, arg1, 0));
    }
    void sendMessage(int what, int arg1, int arg2) {
        mEventHub.sendMessage(Message.obtain(null, what, arg1, arg2));
    }
    void sendMessage(int what, int arg1, Object obj) {
        mEventHub.sendMessage(Message.obtain(null, what, arg1, 0, obj));
    }
    void sendMessage(int what, int arg1, int arg2, Object obj) {
        mEventHub.sendMessage(Message.obtain(null, what, arg1, arg2, obj));
    }
    void sendMessageAtFrontOfQueue(int what, Object obj) {
        mEventHub.sendMessageAtFrontOfQueue(Message.obtain(
                null, what, obj));
    }
    void sendMessageDelayed(int what, Object obj, long delay) {
        mEventHub.sendMessageDelayed(Message.obtain(null, what, obj), delay);
    }
    void removeMessages(int what) {
        mEventHub.removeMessages(what);
    }
    void removeMessages() {
        mEventHub.removeMessages();
    }
    void destroy() {
        synchronized (mEventHub) {
            boolean hasResume = mEventHub.hasMessages(EventHub.RESUME_TIMERS);
            boolean hasPause = mEventHub.hasMessages(EventHub.PAUSE_TIMERS);
            mEventHub.removeMessages();
            mEventHub.sendMessageAtFrontOfQueue(
                    Message.obtain(null, EventHub.DESTROY));
            if (hasPause) {
                mEventHub.sendMessageAtFrontOfQueue(
                        Message.obtain(null, EventHub.PAUSE_TIMERS));
            }
            if (hasResume) {
                mEventHub.sendMessageAtFrontOfQueue(
                        Message.obtain(null, EventHub.RESUME_TIMERS));
            }
            mEventHub.blockMessages();
        }
    }
    private void clearCache(boolean includeDiskFiles) {
        mBrowserFrame.clearCache();
        if (includeDiskFiles) {
            CacheManager.removeAllCacheFiles();
        }
    }
    private void loadUrl(String url, Map<String, String> extraHeaders) {
        if (DebugFlags.WEB_VIEW_CORE) Log.v(LOGTAG, " CORE loadUrl " + url);
        mBrowserFrame.loadUrl(url, extraHeaders);
    }
    private void key(KeyEvent evt, boolean isDown) {
        if (DebugFlags.WEB_VIEW_CORE) {
            Log.v(LOGTAG, "CORE key at " + System.currentTimeMillis() + ", "
                    + evt);
        }
        int keyCode = evt.getKeyCode();
        if (!nativeKey(keyCode, evt.getUnicodeChar(),
                evt.getRepeatCount(), evt.isShiftPressed(), evt.isAltPressed(),
                evt.isSymPressed(),
                isDown) && keyCode != KeyEvent.KEYCODE_ENTER) {
            if (keyCode >= KeyEvent.KEYCODE_DPAD_UP
                    && keyCode <= KeyEvent.KEYCODE_DPAD_RIGHT) {
                if (DebugFlags.WEB_VIEW_CORE) {
                    Log.v(LOGTAG, "key: arrow unused by plugin: " + keyCode);
                }
                if (mWebView != null && evt.isDown()) {
                    Message.obtain(mWebView.mPrivateHandler,
                            WebView.MOVE_OUT_OF_PLUGIN, keyCode).sendToTarget();
                }
                return;
            }
            mCallbackProxy.onUnhandledKeyEvent(evt);
        }
    }
    private int mCurrentViewWidth = 0;
    private int mCurrentViewHeight = 0;
    private float mCurrentViewScale = 1.0f;
    private void viewSizeChanged(int w, int h, int textwrapWidth, float scale,
            int anchorX, int anchorY, boolean ignoreHeight) {
        if (DebugFlags.WEB_VIEW_CORE) {
            Log.v(LOGTAG, "viewSizeChanged w=" + w + "; h=" + h
                    + "; textwrapWidth=" + textwrapWidth + "; scale=" + scale);
        }
        if (w == 0) {
            Log.w(LOGTAG, "skip viewSizeChanged as w is 0");
            return;
        }
        int width = w;
        if (mSettings.getUseWideViewPort()) {
            if (mViewportWidth == -1) {
                if (mSettings.getLayoutAlgorithm() ==
                        WebSettings.LayoutAlgorithm.NORMAL) {
                    width = WebView.DEFAULT_VIEWPORT_WIDTH;
                } else {
                    width = Math.min(WebView.sMaxViewportWidth, Math.max(w,
                            Math.max(WebView.DEFAULT_VIEWPORT_WIDTH,
                                    nativeGetContentMinPrefWidth())));
                }
            } else if (mViewportWidth > 0) {
                width = Math.max(w, mViewportWidth);
            } else {
                width = textwrapWidth;
            }
        }
        nativeSetSize(width, width == w ? h : Math.round((float) width * h / w),
                textwrapWidth, scale, w, h, anchorX, anchorY, ignoreHeight);
        boolean needInvalidate = (mCurrentViewWidth == 0);
        mCurrentViewWidth = w;
        mCurrentViewHeight = h;
        mCurrentViewScale = scale;
        if (needInvalidate) {
            if (DebugFlags.WEB_VIEW_CORE) Log.v(LOGTAG, "viewSizeChanged");
            contentDraw();
        }
        mEventHub.sendMessage(Message.obtain(null,
                EventHub.UPDATE_CACHE_AND_TEXT_ENTRY));
    }
    private void sendUpdateTextEntry() {
        if (mWebView != null) {
            Message.obtain(mWebView.mPrivateHandler,
                    WebView.UPDATE_TEXT_ENTRY_MSG_ID).sendToTarget();
        }
    }
    private long getUsedQuota() {
        WebStorage webStorage = WebStorage.getInstance();
        Collection<WebStorage.Origin> origins = webStorage.getOriginsSync();
        if (origins == null) {
            return 0;
        }
        long usedQuota = 0;
        for (WebStorage.Origin website : origins) {
            usedQuota += website.getQuota();
        }
        return usedQuota;
    }
    private boolean mDrawIsScheduled;
    private boolean mSplitPictureIsScheduled;
    private boolean mDrawIsPaused;
    private RestoreState mRestoreState = null;
    static class RestoreState {
        float mMinScale;
        float mMaxScale;
        float mViewScale;
        float mTextWrapScale;
        float mDefaultScale;
        int mScrollX;
        int mScrollY;
        boolean mMobileSite;
    }
    static class DrawData {
        DrawData() {
            mInvalRegion = new Region();
            mWidthHeight = new Point();
        }
        Region mInvalRegion;
        Point mViewPoint;
        Point mWidthHeight;
        int mMinPrefWidth;
        RestoreState mRestoreState; 
        boolean mFocusSizeChanged;
    }
    private void webkitDraw() {
        mDrawIsScheduled = false;
        DrawData draw = new DrawData();
        if (DebugFlags.WEB_VIEW_CORE) Log.v(LOGTAG, "webkitDraw start");
        if (nativeRecordContent(draw.mInvalRegion, draw.mWidthHeight)
                == false) {
            if (DebugFlags.WEB_VIEW_CORE) Log.v(LOGTAG, "webkitDraw abort");
            return;
        }
        if (mWebView != null) {
            draw.mFocusSizeChanged = nativeFocusBoundsChanged();
            draw.mViewPoint = new Point(mCurrentViewWidth, mCurrentViewHeight);
            if (mSettings.getUseWideViewPort()) {
                draw.mMinPrefWidth = Math.max(
                        mViewportWidth == -1 ? WebView.DEFAULT_VIEWPORT_WIDTH
                                : (mViewportWidth == 0 ? mCurrentViewWidth
                                        : mViewportWidth),
                        nativeGetContentMinPrefWidth());
            }
            if (mRestoreState != null) {
                draw.mRestoreState = mRestoreState;
                mRestoreState = null;
            }
            if (DebugFlags.WEB_VIEW_CORE) Log.v(LOGTAG, "webkitDraw NEW_PICTURE_MSG_ID");
            Message.obtain(mWebView.mPrivateHandler,
                    WebView.NEW_PICTURE_MSG_ID, draw).sendToTarget();
            if (mWebkitScrollX != 0 || mWebkitScrollY != 0) {
                Message.obtain(mWebView.mPrivateHandler,
                        WebView.SYNC_SCROLL_TO_MSG_ID, mWebkitScrollX,
                        mWebkitScrollY).sendToTarget();
                mWebkitScrollX = mWebkitScrollY = 0;
            }
        }
    }
    static final int ZOOM_BITS = Paint.FILTER_BITMAP_FLAG |
                                         Paint.DITHER_FLAG |
                                         Paint.SUBPIXEL_TEXT_FLAG;
    static final int SCROLL_BITS = Paint.FILTER_BITMAP_FLAG |
                                           Paint.DITHER_FLAG;
    final DrawFilter mZoomFilter =
                    new PaintFlagsDrawFilter(ZOOM_BITS, Paint.LINEAR_TEXT_FLAG);
    final DrawFilter mScrollFilter =
                new PaintFlagsDrawFilter(SCROLL_BITS, 0);
     void drawContentPicture(Canvas canvas, int color,
                                          boolean animatingZoom,
                                          boolean animatingScroll) {
        DrawFilter df = null;
        if (animatingZoom) {
            df = mZoomFilter;
        } else if (animatingScroll) {
            df = mScrollFilter;
        }
        canvas.setDrawFilter(df);
        boolean tookTooLong = nativeDrawContent(canvas, color);
        canvas.setDrawFilter(null);
        if (tookTooLong && mSplitPictureIsScheduled == false) {
            mSplitPictureIsScheduled = true;
            sendMessage(EventHub.SPLIT_PICTURE_SET);
        }
    }
     synchronized boolean pictureReady() {
        return 0 != mNativeClass ? nativePictureReady() : false;
    }
     synchronized Picture copyContentPicture() {
        Picture result = new Picture();
        if (0 != mNativeClass) {
            nativeCopyContentToPicture(result);
        }
        return result;
    }
    static void reducePriority() {
        sWebCoreHandler.removeMessages(WebCoreThread.REDUCE_PRIORITY);
        sWebCoreHandler.removeMessages(WebCoreThread.RESUME_PRIORITY);
        sWebCoreHandler.sendMessageAtFrontOfQueue(sWebCoreHandler
                .obtainMessage(WebCoreThread.REDUCE_PRIORITY));
    }
    static void resumePriority() {
        sWebCoreHandler.removeMessages(WebCoreThread.REDUCE_PRIORITY);
        sWebCoreHandler.removeMessages(WebCoreThread.RESUME_PRIORITY);
        sWebCoreHandler.sendMessageAtFrontOfQueue(sWebCoreHandler
                .obtainMessage(WebCoreThread.RESUME_PRIORITY));
    }
    static void pauseUpdatePicture(WebViewCore core) {
        if (core != null) {
            synchronized (core) {
                core.mDrawIsPaused = true;
                if (core.mDrawIsScheduled) {
                    core.mEventHub.removeMessages(EventHub.WEBKIT_DRAW);
                }
            }
        }
    }
    static void resumeUpdatePicture(WebViewCore core) {
        if (core != null) {
            synchronized (core) {
                core.mDrawIsPaused = false;
                if (core.mDrawIsScheduled) {
                    core.mDrawIsScheduled = false;
                    core.contentDraw();
                }
            }
        }
    }
    private void restoreState(int index) {
        WebBackForwardList list = mCallbackProxy.getBackForwardList();
        int size = list.getSize();
        for (int i = 0; i < size; i++) {
            list.getItemAtIndex(i).inflate(mBrowserFrame.mNativeFrame);
        }
        mBrowserFrame.mLoadInitFromJava = true;
        list.restoreIndex(mBrowserFrame.mNativeFrame, index);
        mBrowserFrame.mLoadInitFromJava = false;
    }
     void contentDraw() {
        if (mCurrentViewWidth == 0 || !mBrowserFrame.firstLayoutDone()) {
            return;
        }
        synchronized (this) {
            if (mDrawIsScheduled) return;
            mDrawIsScheduled = true;
            if (mDrawIsPaused) return;
            mEventHub.sendMessage(Message.obtain(null, EventHub.WEBKIT_DRAW));
        }
    }
    private void contentScrollBy(int dx, int dy, boolean animate) {
        if (!mBrowserFrame.firstLayoutDone()) {
            return;
        }
        if (mWebView != null) {
            Message msg = Message.obtain(mWebView.mPrivateHandler,
                    WebView.SCROLL_BY_MSG_ID, dx, dy, new Boolean(animate));
            if (mDrawIsScheduled) {
                mEventHub.sendMessage(Message.obtain(null,
                        EventHub.MESSAGE_RELAY, msg));
            } else {
                msg.sendToTarget();
            }
        }
    }
    private void contentScrollTo(int x, int y) {
        if (!mBrowserFrame.firstLayoutDone()) {
            mRestoredX = x;
            mRestoredY = y;
            return;
        }
        if (mWebView != null) {
            Message msg = Message.obtain(mWebView.mPrivateHandler,
                    WebView.SCROLL_TO_MSG_ID, x, y);
            if (mDrawIsScheduled) {
                mEventHub.sendMessage(Message.obtain(null,
                        EventHub.MESSAGE_RELAY, msg));
            } else {
                msg.sendToTarget();
            }
        }
    }
    private void contentSpawnScrollTo(int x, int y) {
        if (!mBrowserFrame.firstLayoutDone()) {
            mRestoredX = x;
            mRestoredY = y;
            return;
        }
        if (mWebView != null) {
            Message msg = Message.obtain(mWebView.mPrivateHandler,
                    WebView.SPAWN_SCROLL_TO_MSG_ID, x, y);
            if (mDrawIsScheduled) {
                mEventHub.sendMessage(Message.obtain(null,
                        EventHub.MESSAGE_RELAY, msg));
            } else {
                msg.sendToTarget();
            }
        }
    }
    private void sendNotifyProgressFinished() {
        sendUpdateTextEntry();
        WebViewWorker.getHandler().removeMessages(
                WebViewWorker.MSG_CACHE_TRANSACTION_TICKER);
        WebViewWorker.getHandler().sendEmptyMessage(
                WebViewWorker.MSG_CACHE_TRANSACTION_TICKER);
        contentDraw();
    }
    private void sendViewInvalidate(int left, int top, int right, int bottom) {
        if (mWebView != null) {
            Message.obtain(mWebView.mPrivateHandler,
                           WebView.INVAL_RECT_MSG_ID,
                           new Rect(left, top, right, bottom)).sendToTarget();
        }
    }
    private static boolean mRepaintScheduled = false;
     void signalRepaintDone() {
        mRepaintScheduled = false;
    }
    private void sendImmediateRepaint() {
        if (mWebView != null && !mRepaintScheduled) {
            mRepaintScheduled = true;
            Message.obtain(mWebView.mPrivateHandler,
                           WebView.IMMEDIATE_REPAINT_MSG_ID).sendToTarget();
        }
    }
    private void setRootLayer(int layer) {
        if (mWebView != null) {
            Message.obtain(mWebView.mPrivateHandler,
                           WebView.SET_ROOT_LAYER_MSG_ID,
                           layer, 0).sendToTarget();
        }
    }
     WebView getWebView() {
        return mWebView;
    }
    private native void setViewportSettingsFromNative();
    private void didFirstLayout(boolean standardLoad) {
        if (DebugFlags.WEB_VIEW_CORE) {
            Log.v(LOGTAG, "didFirstLayout standardLoad =" + standardLoad);
        }
        mBrowserFrame.didFirstLayout();
        if (mWebView == null) return;
        boolean updateRestoreState = standardLoad || mRestoredScale > 0;
        setupViewport(updateRestoreState);
        if (!updateRestoreState) {
            mWebView.mViewManager.postReadyToDrawAll();
        }
        mWebkitScrollX = mWebkitScrollY = mRestoredX = mRestoredY
                = mRestoredScale = mRestoredScreenWidthScale = 0;
    }
    private void updateViewport() {
        if (mBrowserFrame.firstLayoutDone()) {
            setupViewport(true);
        }
    }
    private void setupViewport(boolean updateRestoreState) {
        setViewportSettingsFromNative();
        float adjust = 1.0f;
        if (mViewportDensityDpi == -1) {
            if (WebView.DEFAULT_SCALE_PERCENT != 100) {
                adjust = WebView.DEFAULT_SCALE_PERCENT / 100.0f;
            }
        } else if (mViewportDensityDpi > 0) {
            adjust = (float) mContext.getResources().getDisplayMetrics().densityDpi
                    / mViewportDensityDpi;
        }
        int defaultScale = (int) (adjust * 100);
        if (mViewportInitialScale > 0) {
            mViewportInitialScale *= adjust;
        }
        if (mViewportMinimumScale > 0) {
            mViewportMinimumScale *= adjust;
        }
        if (mViewportMaximumScale > 0) {
            mViewportMaximumScale *= adjust;
        }
        if (mViewportWidth == 0) {
            if (mViewportInitialScale == 0) {
                mViewportInitialScale = defaultScale;
            }
        }
        if (mViewportUserScalable == false) {
            mViewportInitialScale = defaultScale;
            mViewportMinimumScale = defaultScale;
            mViewportMaximumScale = defaultScale;
        }
        if (mViewportMinimumScale > mViewportInitialScale
                && mViewportInitialScale != 0) {
            mViewportMinimumScale = mViewportInitialScale;
        }
        if (mViewportMaximumScale > 0
                && mViewportMaximumScale < mViewportInitialScale) {
            mViewportMaximumScale = mViewportInitialScale;
        }
        if (mViewportWidth < 0 && mViewportInitialScale == defaultScale) {
            mViewportWidth = 0;
        }
        if (mViewportWidth != 0 && !updateRestoreState) {
            RestoreState restoreState = new RestoreState();
            restoreState.mMinScale = mViewportMinimumScale / 100.0f;
            restoreState.mMaxScale = mViewportMaximumScale / 100.0f;
            restoreState.mDefaultScale = adjust;
            restoreState.mMobileSite = false;
            restoreState.mScrollX = 0;
            Message.obtain(mWebView.mPrivateHandler,
                    WebView.UPDATE_ZOOM_RANGE, restoreState).sendToTarget();
            return;
        }
        int webViewWidth;
        int viewportWidth = mCurrentViewWidth;
        if (viewportWidth == 0) {
            webViewWidth = mWebView.getViewWidth();
            viewportWidth = (int) (webViewWidth / adjust);
            if (viewportWidth == 0) {
                Log.w(LOGTAG, "Can't get the viewWidth after the first layout");
            }
        } else {
            webViewWidth = Math.round(viewportWidth * mCurrentViewScale);
        }
        mRestoreState = new RestoreState();
        mRestoreState.mMinScale = mViewportMinimumScale / 100.0f;
        mRestoreState.mMaxScale = mViewportMaximumScale / 100.0f;
        mRestoreState.mDefaultScale = adjust;
        mRestoreState.mScrollX = mRestoredX;
        mRestoreState.mScrollY = mRestoredY;
        mRestoreState.mMobileSite = (0 == mViewportWidth);
        if (mRestoredScale > 0) {
            mRestoreState.mViewScale = mRestoredScale / 100.0f;
            if (mRestoredScreenWidthScale > 0) {
                mRestoreState.mTextWrapScale =
                        mRestoredScreenWidthScale / 100.0f;
            } else {
                mRestoreState.mTextWrapScale = mRestoreState.mViewScale;
            }
        } else {
            if (mViewportInitialScale > 0) {
                mRestoreState.mViewScale = mRestoreState.mTextWrapScale =
                        mViewportInitialScale / 100.0f;
            } else if (mViewportWidth > 0 && mViewportWidth < webViewWidth) {
                mRestoreState.mViewScale = mRestoreState.mTextWrapScale =
                        (float) webViewWidth / mViewportWidth;
            } else {
                mRestoreState.mTextWrapScale = adjust;
                mRestoreState.mViewScale = 0;
            }
        }
        if (mWebView.mHeightCanMeasure) {
            mWebView.mLastHeightSent = 0;
            WebView.ViewSizeData data = new WebView.ViewSizeData();
            data.mWidth = mWebView.mLastWidthSent;
            data.mHeight = 0;
            data.mTextWrapWidth = data.mWidth;
            data.mScale = -1.0f;
            data.mIgnoreHeight = false;
            data.mAnchorX = data.mAnchorY = 0;
            mEventHub.removeMessages(EventHub.VIEW_SIZE_CHANGED);
            mEventHub.sendMessageAtFrontOfQueue(Message.obtain(null,
                    EventHub.VIEW_SIZE_CHANGED, data));
        } else if (mSettings.getUseWideViewPort()) {
            if (viewportWidth == 0) {
                mWebView.mLastWidthSent = 0;
            } else {
                WebView.ViewSizeData data = new WebView.ViewSizeData();
                data.mScale = mRestoreState.mViewScale == 0
                        ? (mRestoredScale > 0 ? mRestoredScale / 100.0f
                                : mRestoreState.mTextWrapScale)
                        : mRestoreState.mViewScale;
                if (DebugFlags.WEB_VIEW_CORE) {
                    Log.v(LOGTAG, "setupViewport"
                             + " mRestoredScale=" + mRestoredScale
                             + " mViewScale=" + mRestoreState.mViewScale
                             + " mTextWrapScale=" + mRestoreState.mTextWrapScale
                             );
                }
                data.mWidth = Math.round(webViewWidth / data.mScale);
                data.mHeight = mCurrentViewHeight == 0 ?
                        Math.round(mWebView.getViewHeight() / data.mScale)
                        : mCurrentViewHeight * data.mWidth / viewportWidth;
                data.mTextWrapWidth = Math.round(webViewWidth
                        / mRestoreState.mTextWrapScale);
                data.mIgnoreHeight = false;
                data.mAnchorX = data.mAnchorY = 0;
                mEventHub.removeMessages(EventHub.VIEW_SIZE_CHANGED);
                mEventHub.sendMessageAtFrontOfQueue(Message.obtain(null,
                        EventHub.VIEW_SIZE_CHANGED, data));
            }
        }
    }
    private void restoreScale(int scale) {
        if (mBrowserFrame.firstLayoutDone() == false) {
            mRestoredScale = scale;
        }
    }
    private void restoreScreenWidthScale(int scale) {
        if (!mSettings.getUseWideViewPort()) {
            return;
        }
        if (mBrowserFrame.firstLayoutDone() == false) {
            mRestoredScreenWidthScale = scale;
        }
    }
    private void needTouchEvents(boolean need) {
        if (mWebView != null) {
            Message.obtain(mWebView.mPrivateHandler,
                    WebView.WEBCORE_NEED_TOUCH_EVENTS, need ? 1 : 0, 0)
                    .sendToTarget();
        }
    }
    private void updateTextfield(int ptr, boolean changeToPassword,
            String text, int textGeneration) {
        if (mWebView != null) {
            Message msg = Message.obtain(mWebView.mPrivateHandler,
                    WebView.UPDATE_TEXTFIELD_TEXT_MSG_ID, ptr,
                    textGeneration, text);
            msg.getData().putBoolean("password", changeToPassword);
            msg.sendToTarget();
        }
    }
    private void updateTextSelection(int pointer, int start, int end,
            int textGeneration) {
        if (mWebView != null) {
            Message.obtain(mWebView.mPrivateHandler,
                WebView.UPDATE_TEXT_SELECTION_MSG_ID, pointer, textGeneration,
                new TextSelectionData(start, end)).sendToTarget();
        }
    }
    private void clearTextEntry() {
        if (mWebView == null) return;
        Message.obtain(mWebView.mPrivateHandler,
                WebView.CLEAR_TEXT_ENTRY).sendToTarget();
    }
    private void sendFindAgain() {
        if (mWebView == null) return;
        Message.obtain(mWebView.mPrivateHandler,
                WebView.FIND_AGAIN).sendToTarget();
    }
    private native void nativeUpdateFrameCacheIfLoading();
    private native String nativeRequestLabel(int framePtr, int nodePtr);
    private native void nativeScrollFocusedTextInput(float xPercent, int y);
    private native void nativeSetScrollOffset(int gen, int dx, int dy);
    private native void nativeSetGlobalBounds(int x, int y, int w, int h);
    private void requestListBox(String[] array, int[] enabledArray,
            int[] selectedArray) {
        if (mWebView != null) {
            mWebView.requestListBox(array, enabledArray, selectedArray);
        }
    }
    private void requestListBox(String[] array, int[] enabledArray,
            int selection) {
        if (mWebView != null) {
            mWebView.requestListBox(array, enabledArray, selection);
        }
    }
    private void requestKeyboardWithSelection(int pointer, int selStart,
            int selEnd, int textGeneration) {
        if (mWebView != null) {
            Message.obtain(mWebView.mPrivateHandler,
                    WebView.REQUEST_KEYBOARD_WITH_SELECTION_MSG_ID, pointer,
                    textGeneration, new TextSelectionData(selStart, selEnd))
                    .sendToTarget();
        }
    }
    private void requestKeyboard(boolean showKeyboard) {
        if (mWebView != null) {
            Message.obtain(mWebView.mPrivateHandler,
                    WebView.REQUEST_KEYBOARD, showKeyboard ? 1 : 0, 0)
                    .sendToTarget();
        }
    }
    private Context getContext() {
        return mContext;
    }
    private Class<?> getPluginClass(String libName, String clsName) {
        if (mWebView == null) {
            return null;
        }
        PluginManager pluginManager = PluginManager.getInstance(null);
        String pkgName = pluginManager.getPluginsAPKName(libName);
        if (pkgName == null) {
            Log.w(LOGTAG, "Unable to resolve " + libName + " to a plugin APK");
            return null;
        }
        try {
            return pluginManager.getPluginClass(pkgName, clsName);
        } catch (NameNotFoundException e) {
            Log.e(LOGTAG, "Unable to find plugin classloader for the apk (" + pkgName + ")");
        } catch (ClassNotFoundException e) {
            Log.e(LOGTAG, "Unable to find plugin class (" + clsName +
                    ") in the apk (" + pkgName + ")");
        }
        return null;
    }
    private void showFullScreenPlugin(ViewManager.ChildView childView, int npp) {
        if (mWebView == null) {
            return;
        }
        Message message = mWebView.mPrivateHandler.obtainMessage(WebView.SHOW_FULLSCREEN);
        message.obj = childView.mView;
        message.arg1 = npp;
        message.sendToTarget();
    }
    private void hideFullScreenPlugin() {
        if (mWebView == null) {
            return;
        }
        mWebView.mPrivateHandler.obtainMessage(WebView.HIDE_FULLSCREEN)
                .sendToTarget();
    }
    private ViewManager.ChildView addSurface(View pluginView, int x, int y,
                                             int width, int height) {
        if (mWebView == null) {
            return null;
        }
        if (pluginView == null) {
            Log.e(LOGTAG, "Attempted to add an empty plugin view to the view hierarchy");
            return null;
        }
        pluginView.setWillNotDraw(false);
        if(pluginView instanceof SurfaceView)
            ((SurfaceView)pluginView).setZOrderOnTop(true);
        ViewManager.ChildView view = mWebView.mViewManager.createView();
        view.mView = pluginView;
        view.attachView(x, y, width, height);
        return view;
    }
    private void updateSurface(ViewManager.ChildView childView, int x, int y,
            int width, int height) {
        childView.attachView(x, y, width, height);
    }
    private void destroySurface(ViewManager.ChildView childView) {
        childView.removeView();
    }
    static class ShowRectData {
        int mLeft;
        int mTop;
        int mWidth;
        int mHeight;
        int mContentWidth;
        int mContentHeight;
        float mXPercentInDoc;
        float mXPercentInView;
        float mYPercentInDoc;
        float mYPercentInView;
    }
    private void showRect(int left, int top, int width, int height,
            int contentWidth, int contentHeight, float xPercentInDoc,
            float xPercentInView, float yPercentInDoc, float yPercentInView) {
        if (mWebView != null) {
            ShowRectData data = new ShowRectData();
            data.mLeft = left;
            data.mTop = top;
            data.mWidth = width;
            data.mHeight = height;
            data.mContentWidth = contentWidth;
            data.mContentHeight = contentHeight;
            data.mXPercentInDoc = xPercentInDoc;
            data.mXPercentInView = xPercentInView;
            data.mYPercentInDoc = yPercentInDoc;
            data.mYPercentInView = yPercentInView;
            Message.obtain(mWebView.mPrivateHandler, WebView.SHOW_RECT_MSG_ID,
                    data).sendToTarget();
        }
    }
    private void centerFitRect(int x, int y, int width, int height) {
        if (mWebView == null) {
            return;
        }
        mWebView.mPrivateHandler.obtainMessage(WebView.CENTER_FIT_RECT,
                new Rect(x, y, x + width, y + height)).sendToTarget();
    }
    private void setScrollbarModes(int hMode, int vMode) {
        if (mWebView == null) {
            return;
        }
        mWebView.mPrivateHandler.obtainMessage(WebView.SET_SCROLLBAR_MODES,
                hMode, vMode).sendToTarget();
    }
    private native void nativePause();
    private native void nativeResume();
    private native void nativeFreeMemory();
    private native void nativeFullScreenPluginHidden(int npp);
    private native boolean nativeValidNodeAndBounds(int frame, int node,
            Rect bounds);
}
