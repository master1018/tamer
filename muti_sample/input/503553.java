public class WebView extends AbsoluteLayout
        implements ViewTreeObserver.OnGlobalFocusChangeListener,
        ViewGroup.OnHierarchyChangeListener {
    private static final boolean DEBUG_DRAG_TRACKER = false;
    static private final boolean AUTO_REDRAW_HACK = false;
    private boolean mAutoRedraw;
    static final String LOGTAG = "webview";
    private static class ExtendedZoomControls extends FrameLayout {
        public ExtendedZoomControls(Context context, AttributeSet attrs) {
            super(context, attrs);
            LayoutInflater inflater = (LayoutInflater)
                    context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            inflater.inflate(com.android.internal.R.layout.zoom_magnify, this, true);
            mPlusMinusZoomControls = (ZoomControls) findViewById(
                    com.android.internal.R.id.zoomControls);
            findViewById(com.android.internal.R.id.zoomMagnify).setVisibility(
                    View.GONE);
        }
        public void show(boolean showZoom, boolean canZoomOut) {
            mPlusMinusZoomControls.setVisibility(
                    showZoom ? View.VISIBLE : View.GONE);
            fade(View.VISIBLE, 0.0f, 1.0f);
        }
        public void hide() {
            fade(View.GONE, 1.0f, 0.0f);
        }
        private void fade(int visibility, float startAlpha, float endAlpha) {
            AlphaAnimation anim = new AlphaAnimation(startAlpha, endAlpha);
            anim.setDuration(500);
            startAnimation(anim);
            setVisibility(visibility);
        }
        public boolean hasFocus() {
            return mPlusMinusZoomControls.hasFocus();
        }
        public void setOnZoomInClickListener(OnClickListener listener) {
            mPlusMinusZoomControls.setOnZoomInClickListener(listener);
        }
        public void setOnZoomOutClickListener(OnClickListener listener) {
            mPlusMinusZoomControls.setOnZoomOutClickListener(listener);
        }
        ZoomControls    mPlusMinusZoomControls;
    }
    public class WebViewTransport {
        private WebView mWebview;
        public synchronized void setWebView(WebView webview) {
            mWebview = webview;
        }
        public synchronized WebView getWebView() {
            return mWebview;
        }
    }
    private final CallbackProxy mCallbackProxy;
    private final WebViewDatabase mDatabase;
    private SslCertificate mCertificate;
    private int mNativeClass;
    private WebViewCore mWebViewCore;
     final Handler mPrivateHandler = new PrivateHandler();
    private WebTextView mWebTextView;
    private int mTextGeneration;
     final ViewManager mViewManager;
    PluginFullScreenHolder mFullScreenHolder;
    private float mLastTouchX;
    private float mLastTouchY;
    private long mLastTouchTime;
    private long mLastSentTouchTime;
    private static final int TOUCH_SENT_INTERVAL = 50;
    private int mCurrentTouchInterval = TOUCH_SENT_INTERVAL;
    VelocityTracker mVelocityTracker;
    private int mMaximumFling;
    private float mLastVelocity;
    private float mLastVelX;
    private float mLastVelY;
    private int mTouchMode = TOUCH_DONE_MODE;
    private static final int TOUCH_INIT_MODE = 1;
    private static final int TOUCH_DRAG_START_MODE = 2;
    private static final int TOUCH_DRAG_MODE = 3;
    private static final int TOUCH_SHORTPRESS_START_MODE = 4;
    private static final int TOUCH_SHORTPRESS_MODE = 5;
    private static final int TOUCH_DOUBLE_TAP_MODE = 6;
    private static final int TOUCH_DONE_MODE = 7;
    private static final int TOUCH_SELECT_MODE = 8;
    private static final int TOUCH_PINCH_DRAG = 9;
    private boolean mForwardTouchEvents = false;
    private static final int PREVENT_DEFAULT_NO = 0;
    private static final int PREVENT_DEFAULT_MAYBE_YES = 1;
    private static final int PREVENT_DEFAULT_NO_FROM_TOUCH_DOWN = 2;
    private static final int PREVENT_DEFAULT_YES = 3;
    private static final int PREVENT_DEFAULT_IGNORE = 4;
    private int mPreventDefault = PREVENT_DEFAULT_IGNORE;
    private boolean mConfirmMove;
    private boolean mDeferTouchProcess;
    private int mDeferTouchMode = TOUCH_DONE_MODE;
    private float mLastDeferTouchX;
    private float mLastDeferTouchY;
    boolean mDragFromTextInput;
    private boolean mDrawCursorRing = true;
    private boolean mIsPaused;
    private boolean mDelayedDeleteRootLayer;
    private int mTouchSlopSquare;
    private int mDoubleTapSlopSquare;
    private int mNavSlop;
    private static final int TAP_TIMEOUT = 200;
    private static final int LONG_PRESS_TIMEOUT = 1000;
    private static final int MIN_FLING_TIME = 250;
    private static final int MOTIONLESS_TIME = 100;
    private static final long ZOOM_CONTROLS_TIMEOUT =
            ViewConfiguration.getZoomControlsTimeout();
    private static final int PAGE_SCROLL_OVERLAP = 24;
    boolean mWidthCanMeasure;
    boolean mHeightCanMeasure;
    int mLastWidthSent;
    int mLastHeightSent;
    private int mContentWidth;   
    private int mContentHeight;  
    private boolean mOverlayHorizontalScrollbar = true;
    private boolean mOverlayVerticalScrollbar = false;
    private static final int STD_SPEED = 480;  
    private static final int MAX_DURATION = 750;   
    private static final int SLIDE_TITLE_DURATION = 500;   
    private Scroller mScroller;
    private boolean mWrapContent;
    private static final int MOTIONLESS_FALSE           = 0;
    private static final int MOTIONLESS_PENDING         = 1;
    private static final int MOTIONLESS_TRUE            = 2;
    private static final int MOTIONLESS_IGNORE          = 3;
    private int mHeldMotionless;
    private boolean mSupportMultiTouch;
    private ScaleGestureDetector mScaleDetector;
    private int mAnchorX;
    private int mAnchorY;
    private static final int REMEMBER_PASSWORD          = 1;
    private static final int NEVER_REMEMBER_PASSWORD    = 2;
    private static final int SWITCH_TO_SHORTPRESS       = 3;
    private static final int SWITCH_TO_LONGPRESS        = 4;
    private static final int RELEASE_SINGLE_TAP         = 5;
    private static final int REQUEST_FORM_DATA          = 6;
    private static final int RESUME_WEBCORE_PRIORITY    = 7;
    private static final int DRAG_HELD_MOTIONLESS       = 8;
    private static final int AWAKEN_SCROLL_BARS         = 9;
    private static final int PREVENT_DEFAULT_TIMEOUT    = 10;
    private static final int FIRST_PRIVATE_MSG_ID = REMEMBER_PASSWORD;
    private static final int LAST_PRIVATE_MSG_ID = PREVENT_DEFAULT_TIMEOUT;
    static final int SCROLL_TO_MSG_ID                   = 101;
    static final int SCROLL_BY_MSG_ID                   = 102;
    static final int SPAWN_SCROLL_TO_MSG_ID             = 103;
    static final int SYNC_SCROLL_TO_MSG_ID              = 104;
    static final int NEW_PICTURE_MSG_ID                 = 105;
    static final int UPDATE_TEXT_ENTRY_MSG_ID           = 106;
    static final int WEBCORE_INITIALIZED_MSG_ID         = 107;
    static final int UPDATE_TEXTFIELD_TEXT_MSG_ID       = 108;
    static final int UPDATE_ZOOM_RANGE                  = 109;
    static final int MOVE_OUT_OF_PLUGIN                 = 110;
    static final int CLEAR_TEXT_ENTRY                   = 111;
    static final int UPDATE_TEXT_SELECTION_MSG_ID       = 112;
    static final int SHOW_RECT_MSG_ID                   = 113;
    static final int LONG_PRESS_CENTER                  = 114;
    static final int PREVENT_TOUCH_ID                   = 115;
    static final int WEBCORE_NEED_TOUCH_EVENTS          = 116;
    static final int INVAL_RECT_MSG_ID                  = 117;
    static final int REQUEST_KEYBOARD                   = 118;
    static final int DO_MOTION_UP                       = 119;
    static final int SHOW_FULLSCREEN                    = 120;
    static final int HIDE_FULLSCREEN                    = 121;
    static final int DOM_FOCUS_CHANGED                  = 122;
    static final int IMMEDIATE_REPAINT_MSG_ID           = 123;
    static final int SET_ROOT_LAYER_MSG_ID              = 124;
    static final int RETURN_LABEL                       = 125;
    static final int FIND_AGAIN                         = 126;
    static final int CENTER_FIT_RECT                    = 127;
    static final int REQUEST_KEYBOARD_WITH_SELECTION_MSG_ID = 128;
    static final int SET_SCROLLBAR_MODES                = 129;
    private static final int FIRST_PACKAGE_MSG_ID = SCROLL_TO_MSG_ID;
    private static final int LAST_PACKAGE_MSG_ID = SET_SCROLLBAR_MODES;
    static final String[] HandlerPrivateDebugString = {
        "REMEMBER_PASSWORD", 
        "NEVER_REMEMBER_PASSWORD", 
        "SWITCH_TO_SHORTPRESS", 
        "SWITCH_TO_LONGPRESS", 
        "RELEASE_SINGLE_TAP", 
        "REQUEST_FORM_DATA", 
        "RESUME_WEBCORE_PRIORITY", 
        "DRAG_HELD_MOTIONLESS", 
        "AWAKEN_SCROLL_BARS", 
        "PREVENT_DEFAULT_TIMEOUT" 
    };
    static final String[] HandlerPackageDebugString = {
        "SCROLL_TO_MSG_ID", 
        "SCROLL_BY_MSG_ID", 
        "SPAWN_SCROLL_TO_MSG_ID", 
        "SYNC_SCROLL_TO_MSG_ID", 
        "NEW_PICTURE_MSG_ID", 
        "UPDATE_TEXT_ENTRY_MSG_ID", 
        "WEBCORE_INITIALIZED_MSG_ID", 
        "UPDATE_TEXTFIELD_TEXT_MSG_ID", 
        "UPDATE_ZOOM_RANGE", 
        "MOVE_OUT_OF_PLUGIN", 
        "CLEAR_TEXT_ENTRY", 
        "UPDATE_TEXT_SELECTION_MSG_ID", 
        "SHOW_RECT_MSG_ID", 
        "LONG_PRESS_CENTER", 
        "PREVENT_TOUCH_ID", 
        "WEBCORE_NEED_TOUCH_EVENTS", 
        "INVAL_RECT_MSG_ID", 
        "REQUEST_KEYBOARD", 
        "DO_MOTION_UP", 
        "SHOW_FULLSCREEN", 
        "HIDE_FULLSCREEN", 
        "DOM_FOCUS_CHANGED", 
        "IMMEDIATE_REPAINT_MSG_ID", 
        "SET_ROOT_LAYER_MSG_ID", 
        "RETURN_LABEL", 
        "FIND_AGAIN", 
        "CENTER_FIT_RECT", 
        "REQUEST_KEYBOARD_WITH_SELECTION_MSG_ID", 
        "SET_SCROLLBAR_MODES" 
    };
    static final int DEFAULT_VIEWPORT_WIDTH = 800;
    static int sMaxViewportWidth = DEFAULT_VIEWPORT_WIDTH;
    private static float DEFAULT_MAX_ZOOM_SCALE;
    private static float DEFAULT_MIN_ZOOM_SCALE;
    private float mMaxZoomScale;
    private float mMinZoomScale;
    private boolean mMinZoomScaleFixed = true;
    private int mInitialScaleInPercent = 0;
    boolean mInZoomOverview = false;
    int mZoomOverviewWidth = DEFAULT_VIEWPORT_WIDTH;
    float mTextWrapScale;
    static int DEFAULT_SCALE_PERCENT;
    private float mDefaultScale;
    private static float MINIMUM_SCALE_INCREMENT = 0.01f;
    private boolean mPreviewZoomOnly = false;
    private float mActualScale;
    private float mInvActualScale;
    private float mZoomScale;
    private float mInvInitialZoomScale;
    private float mInvFinalZoomScale;
    private int mInitialScrollX;
    private int mInitialScrollY;
    private long mZoomStart;
    private static final int ZOOM_ANIMATION_LENGTH = 500;
    private boolean mUserScroll = false;
    private int mSnapScrollMode = SNAP_NONE;
    private static final int SNAP_NONE = 0;
    private static final int SNAP_LOCK = 1; 
    private static final int SNAP_X = 2; 
    private static final int SNAP_Y = 4; 
    private boolean mSnapPositive;
    private static final int DRAW_EXTRAS_NONE = 0;
    private static final int DRAW_EXTRAS_FIND = 1;
    private static final int DRAW_EXTRAS_SELECTION = 2;
    private static final int DRAW_EXTRAS_CURSOR_RING = 3;
    private static final int SCROLLBAR_AUTO = 0;
    private static final int SCROLLBAR_ALWAYSOFF = 1;
    private static final int SCROLLBAR_ALWAYSON = 2;
    private int mHorizontalScrollBarMode = SCROLLBAR_AUTO;
    private int mVerticalScrollBarMode = SCROLLBAR_AUTO;
    private boolean mGotKeyDown;
     static boolean mLogEvent = true;
    private long mLastTouchUpTime = 0;
    public static final String SCHEME_TEL = "tel:";
    public static final String SCHEME_MAILTO = "mailto:";
    public static final String SCHEME_GEO = "geo:0,0?q=";
    private int mBackgroundColor = Color.WHITE;
    private PictureListener mPictureListener;
    public interface PictureListener {
        public void onNewPicture(WebView view, Picture picture);
    }
    public  class HitTestResult {
        public static final int UNKNOWN_TYPE = 0;
        public static final int ANCHOR_TYPE = 1;
        public static final int PHONE_TYPE = 2;
        public static final int GEO_TYPE = 3;
        public static final int EMAIL_TYPE = 4;
        public static final int IMAGE_TYPE = 5;
        public static final int IMAGE_ANCHOR_TYPE = 6;
        public static final int SRC_ANCHOR_TYPE = 7;
        public static final int SRC_IMAGE_ANCHOR_TYPE = 8;
        public static final int EDIT_TEXT_TYPE = 9;
        private int mType;
        private String mExtra;
        HitTestResult() {
            mType = UNKNOWN_TYPE;
        }
        private void setType(int type) {
            mType = type;
        }
        private void setExtra(String extra) {
            mExtra = extra;
        }
        public int getType() {
            return mType;
        }
        public String getExtra() {
            return mExtra;
        }
    }
    private ExtendedZoomControls mZoomControls;
    private Runnable mZoomControlRunnable;
    private ZoomButtonsController mZoomButtonsController;
    private float mZoomCenterX;
    private float mZoomCenterY;
    private ZoomButtonsController.OnZoomListener mZoomListener =
            new ZoomButtonsController.OnZoomListener() {
        public void onVisibilityChanged(boolean visible) {
            if (visible) {
                switchOutDrawHistory();
                mZoomButtonsController.getZoomControls().setVisibility(
                        View.VISIBLE);
                updateZoomButtonsEnabled();
            }
        }
        public void onZoom(boolean zoomIn) {
            if (zoomIn) {
                zoomIn();
            } else {
                zoomOut();
            }
            updateZoomButtonsEnabled();
        }
    };
    public WebView(Context context) {
        this(context, null);
    }
    public WebView(Context context, AttributeSet attrs) {
        this(context, attrs, com.android.internal.R.attr.webViewStyle);
    }
    public WebView(Context context, AttributeSet attrs, int defStyle) {
        this(context, attrs, defStyle, null);
    }
    protected WebView(Context context, AttributeSet attrs, int defStyle,
            Map<String, Object> javascriptInterfaces) {
        super(context, attrs, defStyle);
        init();
        mCallbackProxy = new CallbackProxy(context, this);
        mViewManager = new ViewManager(this);
        mWebViewCore = new WebViewCore(context, this, mCallbackProxy, javascriptInterfaces);
        mDatabase = WebViewDatabase.getInstance(context);
        mScroller = new Scroller(context);
        updateMultiTouchSupport(context);
    }
    void updateMultiTouchSupport(Context context) {
        WebSettings settings = getSettings();
        mSupportMultiTouch = context.getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_TOUCHSCREEN_MULTITOUCH)
                && settings.supportZoom() && settings.getBuiltInZoomControls();
        if (mSupportMultiTouch && (mScaleDetector == null)) {
            mScaleDetector = new ScaleGestureDetector(context,
                    new ScaleDetectorListener());
        } else if (!mSupportMultiTouch && (mScaleDetector != null)) {
            mScaleDetector = null;
        }
    }
    private void updateZoomButtonsEnabled() {
        if (mZoomButtonsController == null) return;
        boolean canZoomIn = mActualScale < mMaxZoomScale;
        boolean canZoomOut = mActualScale > mMinZoomScale && !mInZoomOverview;
        if (!canZoomIn && !canZoomOut) {
            mZoomButtonsController.getZoomControls().setVisibility(View.GONE);
        } else {
            mZoomButtonsController.setZoomInEnabled(canZoomIn);
            mZoomButtonsController.setZoomOutEnabled(canZoomOut);
        }
    }
    private void init() {
        setWillNotDraw(false);
        setFocusable(true);
        setFocusableInTouchMode(true);
        setClickable(true);
        setLongClickable(true);
        final ViewConfiguration configuration = ViewConfiguration.get(getContext());
        int slop = configuration.getScaledTouchSlop();
        mTouchSlopSquare = slop * slop;
        mMinLockSnapReverseDistance = slop;
        slop = configuration.getScaledDoubleTapSlop();
        mDoubleTapSlopSquare = slop * slop;
        final float density = getContext().getResources().getDisplayMetrics().density;
        mNavSlop = (int) (16 * density);
        DEFAULT_SCALE_PERCENT = (int) (100 * density);
        mDefaultScale = density;
        mActualScale = density;
        mInvActualScale = 1 / density;
        mTextWrapScale = density;
        DEFAULT_MAX_ZOOM_SCALE = 4.0f * density;
        DEFAULT_MIN_ZOOM_SCALE = 0.25f * density;
        mMaxZoomScale = DEFAULT_MAX_ZOOM_SCALE;
        mMinZoomScale = DEFAULT_MIN_ZOOM_SCALE;
        mMaximumFling = configuration.getScaledMaximumFlingVelocity();
    }
    void updateDefaultZoomDensity(int zoomDensity) {
        final float density = getContext().getResources().getDisplayMetrics().density
                * 100 / zoomDensity;
        if (Math.abs(density - mDefaultScale) > 0.01) {
            float scaleFactor = density / mDefaultScale;
            mNavSlop = (int) (16 * density);
            DEFAULT_SCALE_PERCENT = (int) (100 * density);
            DEFAULT_MAX_ZOOM_SCALE = 4.0f * density;
            DEFAULT_MIN_ZOOM_SCALE = 0.25f * density;
            mDefaultScale = density;
            mMaxZoomScale *= scaleFactor;
            mMinZoomScale *= scaleFactor;
            setNewZoomScale(mActualScale * scaleFactor, true, false);
        }
    }
     boolean onSavePassword(String schemePlusHost, String username,
            String password, final Message resumeMsg) {
       boolean rVal = false;
       if (resumeMsg == null) {
           mDatabase.setUsernamePassword(schemePlusHost, username, password);
       } else {
            final Message remember = mPrivateHandler.obtainMessage(
                    REMEMBER_PASSWORD);
            remember.getData().putString("host", schemePlusHost);
            remember.getData().putString("username", username);
            remember.getData().putString("password", password);
            remember.obj = resumeMsg;
            final Message neverRemember = mPrivateHandler.obtainMessage(
                    NEVER_REMEMBER_PASSWORD);
            neverRemember.getData().putString("host", schemePlusHost);
            neverRemember.getData().putString("username", username);
            neverRemember.getData().putString("password", password);
            neverRemember.obj = resumeMsg;
            new AlertDialog.Builder(getContext())
                    .setTitle(com.android.internal.R.string.save_password_label)
                    .setMessage(com.android.internal.R.string.save_password_message)
                    .setPositiveButton(com.android.internal.R.string.save_password_notnow,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            resumeMsg.sendToTarget();
                        }
                    })
                    .setNeutralButton(com.android.internal.R.string.save_password_remember,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            remember.sendToTarget();
                        }
                    })
                    .setNegativeButton(com.android.internal.R.string.save_password_never,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            neverRemember.sendToTarget();
                        }
                    })
                    .setOnCancelListener(new OnCancelListener() {
                        public void onCancel(DialogInterface dialog) {
                            resumeMsg.sendToTarget();
                        }
                    }).show();
            rVal = true;
        }
       return rVal;
    }
    @Override
    public void setScrollBarStyle(int style) {
        if (style == View.SCROLLBARS_INSIDE_INSET
                || style == View.SCROLLBARS_OUTSIDE_INSET) {
            mOverlayHorizontalScrollbar = mOverlayVerticalScrollbar = false;
        } else {
            mOverlayHorizontalScrollbar = mOverlayVerticalScrollbar = true;
        }
        super.setScrollBarStyle(style);
    }
    public void setHorizontalScrollbarOverlay(boolean overlay) {
        mOverlayHorizontalScrollbar = overlay;
    }
    public void setVerticalScrollbarOverlay(boolean overlay) {
        mOverlayVerticalScrollbar = overlay;
    }
    public boolean overlayHorizontalScrollbar() {
        return mOverlayHorizontalScrollbar;
    }
    public boolean overlayVerticalScrollbar() {
        return mOverlayVerticalScrollbar;
    }
     int getViewWidth() {
        if (!isVerticalScrollBarEnabled() || mOverlayVerticalScrollbar) {
            return getWidth();
        } else {
            return getWidth() - getVerticalScrollbarWidth();
        }
    }
    private int getTitleHeight() {
        return mTitleBar != null ? mTitleBar.getHeight() : 0;
    }
    private int getVisibleTitleHeight() {
        return Math.max(getTitleHeight() - mScrollY, 0);
    }
     int getViewHeight() {
        return getViewHeightWithTitle() - getVisibleTitleHeight();
    }
    private int getViewHeightWithTitle() {
        int height = getHeight();
        if (isHorizontalScrollBarEnabled() && !mOverlayHorizontalScrollbar) {
            height -= getHorizontalScrollbarHeight();
        }
        return height;
    }
    public SslCertificate getCertificate() {
        return mCertificate;
    }
    public void setCertificate(SslCertificate certificate) {
        if (DebugFlags.WEB_VIEW) {
            Log.v(LOGTAG, "setCertificate=" + certificate);
        }
        mCertificate = certificate;
    }
    public void savePassword(String host, String username, String password) {
        mDatabase.setUsernamePassword(host, username, password);
    }
    public void setHttpAuthUsernamePassword(String host, String realm,
            String username, String password) {
        mDatabase.setHttpAuthUsernamePassword(host, realm, username, password);
    }
    public String[] getHttpAuthUsernamePassword(String host, String realm) {
        return mDatabase.getHttpAuthUsernamePassword(host, realm);
    }
    public void destroy() {
        clearTextEntry(false);
        if (mWebViewCore != null) {
            mCallbackProxy.setWebViewClient(null);
            mCallbackProxy.setWebChromeClient(null);
            synchronized (this) {
                WebViewCore webViewCore = mWebViewCore;
                mWebViewCore = null; 
                webViewCore.destroy();
            }
            mPrivateHandler.removeCallbacksAndMessages(null);
            mCallbackProxy.removeCallbacksAndMessages(null);
            synchronized (mCallbackProxy) {
                mCallbackProxy.notify();
            }
        }
        if (mNativeClass != 0) {
            nativeDestroy();
            mNativeClass = 0;
        }
    }
    public static void enablePlatformNotifications() {
        Network.enablePlatformNotifications();
    }
    public static void disablePlatformNotifications() {
        Network.disablePlatformNotifications();
    }
    public void setJsFlags(String flags) {
        mWebViewCore.sendMessage(EventHub.SET_JS_FLAGS, flags);
    }
    public void setNetworkAvailable(boolean networkUp) {
        mWebViewCore.sendMessage(EventHub.SET_NETWORK_STATE,
                networkUp ? 1 : 0, 0);
    }
    public void setNetworkType(String type, String subtype) {
        Map<String, String> map = new HashMap<String, String>();
        map.put("type", type);
        map.put("subtype", subtype);
        mWebViewCore.sendMessage(EventHub.SET_NETWORK_TYPE, map);
    }
    public WebBackForwardList saveState(Bundle outState) {
        if (outState == null) {
            return null;
        }
        WebBackForwardList list = copyBackForwardList();
        final int currentIndex = list.getCurrentIndex();
        final int size = list.getSize();
        if (currentIndex < 0 || currentIndex >= size || size == 0) {
            return null;
        }
        outState.putInt("index", currentIndex);
        ArrayList<byte[]> history = new ArrayList<byte[]>(size);
        for (int i = 0; i < size; i++) {
            WebHistoryItem item = list.getItemAtIndex(i);
            if (null == item) {
                Log.w(LOGTAG, "saveState: Unexpected null history item.");
                return null;
            }
            byte[] data = item.getFlattenedData();
            if (data == null) {
                return null;
            }
            history.add(data);
        }
        outState.putSerializable("history", history);
        if (mCertificate != null) {
            outState.putBundle("certificate",
                               SslCertificate.saveState(mCertificate));
        }
        return list;
    }
    public boolean savePicture(Bundle b, final File dest) {
        if (dest == null || b == null) {
            return false;
        }
        final Picture p = capturePicture();
        final File temp = new File(dest.getPath() + ".writing");
        new Thread(new Runnable() {
            public void run() {
                try {
                    FileOutputStream out = new FileOutputStream(temp);
                    p.writeToStream(out);
                    out.close();
                    temp.renameTo(dest);
                } catch (Exception e) {
                } finally {
                    temp.delete();
                }
            }
        }).start();
        b.putInt("scrollX", mScrollX);
        b.putInt("scrollY", mScrollY);
        b.putFloat("scale", mActualScale);
        b.putFloat("textwrapScale", mTextWrapScale);
        b.putBoolean("overview", mInZoomOverview);
        return true;
    }
    private void restoreHistoryPictureFields(Picture p, Bundle b) {
        int sx = b.getInt("scrollX", 0);
        int sy = b.getInt("scrollY", 0);
        float scale = b.getFloat("scale", 1.0f);
        mDrawHistory = true;
        mHistoryPicture = p;
        mScrollX = sx;
        mScrollY = sy;
        mHistoryWidth = Math.round(p.getWidth() * scale);
        mHistoryHeight = Math.round(p.getHeight() * scale);
        mActualScale = scale;
        mInvActualScale = 1 / scale;
        mTextWrapScale = b.getFloat("textwrapScale", scale);
        mInZoomOverview = b.getBoolean("overview");
        invalidate();
    }
    public boolean restorePicture(Bundle b, File src) {
        if (src == null || b == null) {
            return false;
        }
        if (!src.exists()) {
            return false;
        }
        try {
            final FileInputStream in = new FileInputStream(src);
            final Bundle copy = new Bundle(b);
            new Thread(new Runnable() {
                public void run() {
                    final Picture p = Picture.createFromStream(in);
                    if (p != null) {
                        mPrivateHandler.post(new Runnable() {
                            public void run() {
                                restoreHistoryPictureFields(p, copy);
                            }
                        });
                    }
                    try {
                        in.close();
                    } catch (Exception e) {
                    }
                }
            }).start();
        } catch (FileNotFoundException e){
            e.printStackTrace();
        }
        return true;
    }
    public WebBackForwardList restoreState(Bundle inState) {
        WebBackForwardList returnList = null;
        if (inState == null) {
            return returnList;
        }
        if (inState.containsKey("index") && inState.containsKey("history")) {
            mCertificate = SslCertificate.restoreState(
                inState.getBundle("certificate"));
            final WebBackForwardList list = mCallbackProxy.getBackForwardList();
            final int index = inState.getInt("index");
            synchronized (list) {
                final List<byte[]> history =
                        (List<byte[]>) inState.getSerializable("history");
                final int size = history.size();
                if (index < 0 || index >= size) {
                    return null;
                }
                for (int i = 0; i < size; i++) {
                    byte[] data = history.remove(0);
                    if (data == null) {
                        return null;
                    }
                    WebHistoryItem item = new WebHistoryItem(data);
                    list.addHistoryItem(item);
                }
                returnList = copyBackForwardList();
                returnList.setCurrentIndex(index);
            }
            mWebViewCore.removeMessages();
            mWebViewCore.sendMessage(EventHub.RESTORE_STATE, index);
        }
        return returnList;
    }
    public void loadUrl(String url, Map<String, String> extraHeaders) {
        switchOutDrawHistory();
        WebViewCore.GetUrlData arg = new WebViewCore.GetUrlData();
        arg.mUrl = url;
        arg.mExtraHeaders = extraHeaders;
        mWebViewCore.sendMessage(EventHub.LOAD_URL, arg);
        clearTextEntry(false);
    }
    public void loadUrl(String url) {
        if (url == null) {
            return;
        }
        loadUrl(url, null);
    }
    public void postUrl(String url, byte[] postData) {
        if (URLUtil.isNetworkUrl(url)) {
            switchOutDrawHistory();
            WebViewCore.PostUrlData arg = new WebViewCore.PostUrlData();
            arg.mUrl = url;
            arg.mPostData = postData;
            mWebViewCore.sendMessage(EventHub.POST_URL, arg);
            clearTextEntry(false);
        } else {
            loadUrl(url);
        }
    }
    public void loadData(String data, String mimeType, String encoding) {
        loadUrl("data:" + mimeType + ";" + encoding + "," + data);
    }
    public void loadDataWithBaseURL(String baseUrl, String data,
            String mimeType, String encoding, String historyUrl) {
        if (baseUrl != null && baseUrl.toLowerCase().startsWith("data:")) {
            loadData(data, mimeType, encoding);
            return;
        }
        switchOutDrawHistory();
        WebViewCore.BaseUrlData arg = new WebViewCore.BaseUrlData();
        arg.mBaseUrl = baseUrl;
        arg.mData = data;
        arg.mMimeType = mimeType;
        arg.mEncoding = encoding;
        arg.mHistoryUrl = historyUrl;
        mWebViewCore.sendMessage(EventHub.LOAD_DATA, arg);
        clearTextEntry(false);
    }
    public void stopLoading() {
        switchOutDrawHistory();
        mWebViewCore.sendMessage(EventHub.STOP_LOADING);
    }
    public void reload() {
        clearTextEntry(false);
        switchOutDrawHistory();
        mWebViewCore.sendMessage(EventHub.RELOAD);
    }
    public boolean canGoBack() {
        WebBackForwardList l = mCallbackProxy.getBackForwardList();
        synchronized (l) {
            if (l.getClearPending()) {
                return false;
            } else {
                return l.getCurrentIndex() > 0;
            }
        }
    }
    public void goBack() {
        goBackOrForward(-1);
    }
    public boolean canGoForward() {
        WebBackForwardList l = mCallbackProxy.getBackForwardList();
        synchronized (l) {
            if (l.getClearPending()) {
                return false;
            } else {
                return l.getCurrentIndex() < l.getSize() - 1;
            }
        }
    }
    public void goForward() {
        goBackOrForward(1);
    }
    public boolean canGoBackOrForward(int steps) {
        WebBackForwardList l = mCallbackProxy.getBackForwardList();
        synchronized (l) {
            if (l.getClearPending()) {
                return false;
            } else {
                int newIndex = l.getCurrentIndex() + steps;
                return newIndex >= 0 && newIndex < l.getSize();
            }
        }
    }
    public void goBackOrForward(int steps) {
        goBackOrForward(steps, false);
    }
    private void goBackOrForward(int steps, boolean ignoreSnapshot) {
        if (steps != 0) {
            clearTextEntry(false);
            mWebViewCore.sendMessage(EventHub.GO_BACK_FORWARD, steps,
                    ignoreSnapshot ? 1 : 0);
        }
    }
    private boolean extendScroll(int y) {
        int finalY = mScroller.getFinalY();
        int newY = pinLocY(finalY + y);
        if (newY == finalY) return false;
        mScroller.setFinalY(newY);
        mScroller.extendDuration(computeDuration(0, y));
        return true;
    }
    public boolean pageUp(boolean top) {
        if (mNativeClass == 0) {
            return false;
        }
        nativeClearCursor(); 
        if (top) {
            return pinScrollTo(mScrollX, 0, true, 0);
        }
        int h = getHeight();
        int y;
        if (h > 2 * PAGE_SCROLL_OVERLAP) {
            y = -h + PAGE_SCROLL_OVERLAP;
        } else {
            y = -h / 2;
        }
        mUserScroll = true;
        return mScroller.isFinished() ? pinScrollBy(0, y, true, 0)
                : extendScroll(y);
    }
    public boolean pageDown(boolean bottom) {
        if (mNativeClass == 0) {
            return false;
        }
        nativeClearCursor(); 
        if (bottom) {
            return pinScrollTo(mScrollX, computeVerticalScrollRange(), true, 0);
        }
        int h = getHeight();
        int y;
        if (h > 2 * PAGE_SCROLL_OVERLAP) {
            y = h - PAGE_SCROLL_OVERLAP;
        } else {
            y = h / 2;
        }
        mUserScroll = true;
        return mScroller.isFinished() ? pinScrollBy(0, y, true, 0)
                : extendScroll(y);
    }
    public void clearView() {
        mContentWidth = 0;
        mContentHeight = 0;
        mWebViewCore.sendMessage(EventHub.CLEAR_CONTENT);
    }
    public Picture capturePicture() {
        if (null == mWebViewCore) return null; 
        return mWebViewCore.copyContentPicture();
    }
    private boolean inEditingMode() {
        return mWebTextView != null && mWebTextView.getParent() != null;
    }
    private void clearTextEntry(boolean disableFocusController) {
        if (inEditingMode()) {
            mWebTextView.remove();
            if (disableFocusController) {
                setFocusControllerInactive();
            }
        }
    }
    public float getScale() {
        return mActualScale;
    }
    public void setInitialScale(int scaleInPercent) {
        mInitialScaleInPercent = scaleInPercent;
    }
    public void invokeZoomPicker() {
        if (!getSettings().supportZoom()) {
            Log.w(LOGTAG, "This WebView doesn't support zoom.");
            return;
        }
        clearTextEntry(false);
        if (getSettings().getBuiltInZoomControls()) {
            getZoomButtonsController().setVisible(true);
        } else {
            mPrivateHandler.removeCallbacks(mZoomControlRunnable);
            mPrivateHandler.postDelayed(mZoomControlRunnable,
                    ZOOM_CONTROLS_TIMEOUT);
        }
    }
    public HitTestResult getHitTestResult() {
        if (mNativeClass == 0) {
            return null;
        }
        HitTestResult result = new HitTestResult();
        if (nativeHasCursorNode()) {
            if (nativeCursorIsTextInput()) {
                result.setType(HitTestResult.EDIT_TEXT_TYPE);
            } else {
                String text = nativeCursorText();
                if (text != null) {
                    if (text.startsWith(SCHEME_TEL)) {
                        result.setType(HitTestResult.PHONE_TYPE);
                        result.setExtra(text.substring(SCHEME_TEL.length()));
                    } else if (text.startsWith(SCHEME_MAILTO)) {
                        result.setType(HitTestResult.EMAIL_TYPE);
                        result.setExtra(text.substring(SCHEME_MAILTO.length()));
                    } else if (text.startsWith(SCHEME_GEO)) {
                        result.setType(HitTestResult.GEO_TYPE);
                        result.setExtra(URLDecoder.decode(text
                                .substring(SCHEME_GEO.length())));
                    } else if (nativeCursorIsAnchor()) {
                        result.setType(HitTestResult.SRC_ANCHOR_TYPE);
                        result.setExtra(text);
                    }
                }
            }
        }
        int type = result.getType();
        if (type == HitTestResult.UNKNOWN_TYPE
                || type == HitTestResult.SRC_ANCHOR_TYPE) {
            int contentX = viewToContentX((int) mLastTouchX + mScrollX);
            int contentY = viewToContentY((int) mLastTouchY + mScrollY);
            String text = nativeImageURI(contentX, contentY);
            if (text != null) {
                result.setType(type == HitTestResult.UNKNOWN_TYPE ?
                        HitTestResult.IMAGE_TYPE :
                        HitTestResult.SRC_IMAGE_ANCHOR_TYPE);
                result.setExtra(text);
            }
        }
        return result;
    }
    private void domChangedFocus() {
        if (inEditingMode()) {
            mPrivateHandler.obtainMessage(DOM_FOCUS_CHANGED).sendToTarget();
        }
    }
    public void requestFocusNodeHref(Message hrefMsg) {
        if (hrefMsg == null || mNativeClass == 0) {
            return;
        }
        if (nativeCursorIsAnchor()) {
            mWebViewCore.sendMessage(EventHub.REQUEST_CURSOR_HREF,
                    nativeCursorFramePointer(), nativeCursorNodePointer(),
                    hrefMsg);
        }
    }
    public void requestImageRef(Message msg) {
        if (0 == mNativeClass) return; 
        int contentX = viewToContentX((int) mLastTouchX + mScrollX);
        int contentY = viewToContentY((int) mLastTouchY + mScrollY);
        String ref = nativeImageURI(contentX, contentY);
        Bundle data = msg.getData();
        data.putString("url", ref);
        msg.setData(data);
        msg.sendToTarget();
    }
    private static int pinLoc(int x, int viewMax, int docMax) {
        if (docMax < viewMax) {   
            x = 0;
        } else if (x < 0) {
            x = 0;
        } else if (x + viewMax > docMax) {
            x = docMax - viewMax;
        }
        return x;
    }
    private int pinLocX(int x) {
        return pinLoc(x, getViewWidth(), computeHorizontalScrollRange());
    }
    private int pinLocY(int y) {
        return pinLoc(y, getViewHeightWithTitle(),
                      computeVerticalScrollRange() + getTitleHeight());
    }
    private View mTitleBar;
    private Drawable mTitleShadow;
    public void setEmbeddedTitleBar(View v) {
        if (mTitleBar == v) return;
        if (mTitleBar != null) {
            removeView(mTitleBar);
        }
        if (null != v) {
            addView(v, new AbsoluteLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT, 0, 0));
            if (mTitleShadow == null) {
                mTitleShadow = (Drawable) mContext.getResources().getDrawable(
                        com.android.internal.R.drawable.title_bar_shadow);
            }
        }
        mTitleBar = v;
    }
    private int viewToContentDimension(int d) {
        return Math.round(d * mInvActualScale);
    }
     int viewToContentX(int x) {
        return viewToContentDimension(x);
    }
     int viewToContentY(int y) {
        return viewToContentDimension(y - getTitleHeight());
    }
    private float viewToContentXf(int x) {
        return x * mInvActualScale;
    }
    private float viewToContentYf(int y) {
        return (y - getTitleHeight()) * mInvActualScale;
    }
     int contentToViewDimension(int d) {
        return Math.round(d * mActualScale);
    }
     int contentToViewX(int x) {
        return contentToViewDimension(x);
    }
     int contentToViewY(int y) {
        return contentToViewDimension(y) + getTitleHeight();
    }
    private Rect contentToViewRect(Rect x) {
        return new Rect(contentToViewX(x.left), contentToViewY(x.top),
                        contentToViewX(x.right), contentToViewY(x.bottom));
    }
    private void viewInvalidate(int l, int t, int r, int b) {
        final float scale = mActualScale;
        final int dy = getTitleHeight();
        invalidate((int)Math.floor(l * scale),
                   (int)Math.floor(t * scale) + dy,
                   (int)Math.ceil(r * scale),
                   (int)Math.ceil(b * scale) + dy);
    }
    private void viewInvalidateDelayed(long delay, int l, int t, int r, int b) {
        final float scale = mActualScale;
        final int dy = getTitleHeight();
        postInvalidateDelayed(delay,
                              (int)Math.floor(l * scale),
                              (int)Math.floor(t * scale) + dy,
                              (int)Math.ceil(r * scale),
                              (int)Math.ceil(b * scale) + dy);
    }
    private void invalidateContentRect(Rect r) {
        viewInvalidate(r.left, r.top, r.right, r.bottom);
    }
    private void abortAnimation() {
        mScroller.abortAnimation();
        mLastVelocity = 0;
    }
    private void recordNewContentSize(int w, int h, boolean updateLayout) {
        if ((w | h) == 0) {
            return;
        }
        if (mContentWidth != w || mContentHeight != h) {
            mContentWidth = w;
            mContentHeight = h;
            if (!mDrawHistory) {
                int oldX = mScrollX;
                int oldY = mScrollY;
                mScrollX = pinLocX(mScrollX);
                mScrollY = pinLocY(mScrollY);
                if (oldX != mScrollX || oldY != mScrollY) {
                    onScrollChanged(mScrollX, mScrollY, oldX, oldY);
                }
                if (!mScroller.isFinished()) {
                    mScroller.setFinalX(pinLocX(mScroller.getFinalX()));
                    mScroller.setFinalY(pinLocY(mScroller.getFinalY()));
                }
            }
        }
        contentSizeChanged(updateLayout);
    }
    private void setNewZoomScale(float scale, boolean updateTextWrapScale,
            boolean force) {
        if (scale < mMinZoomScale) {
            scale = mMinZoomScale;
            if (scale < mDefaultScale) mInZoomOverview = true;
        } else if (scale > mMaxZoomScale) {
            scale = mMaxZoomScale;
        }
        if (updateTextWrapScale) {
            mTextWrapScale = scale;
            mLastHeightSent = 0;
        }
        if (scale != mActualScale || force) {
            if (mDrawHistory) {
                if (scale != mActualScale && !mPreviewZoomOnly) {
                    mCallbackProxy.onScaleChanged(mActualScale, scale);
                }
                mActualScale = scale;
                mInvActualScale = 1 / scale;
                sendViewSizeZoom();
            } else {
                int oldX = mScrollX;
                int oldY = mScrollY;
                float ratio = scale * mInvActualScale;   
                float sx = ratio * oldX + (ratio - 1) * mZoomCenterX;
                float sy = ratio * oldY + (ratio - 1)
                        * (mZoomCenterY - getTitleHeight());
                if (scale != mActualScale && !mPreviewZoomOnly) {
                    mCallbackProxy.onScaleChanged(mActualScale, scale);
                }
                mActualScale = scale;
                mInvActualScale = 1 / scale;
                mViewManager.scaleAll();
                mScrollX = pinLocX(Math.round(sx));
                mScrollY = pinLocY(Math.round(sy));
                if (oldX != mScrollX || oldY != mScrollY) {
                    onScrollChanged(mScrollX, mScrollY, oldX, oldY);
                } else {
                    sendOurVisibleRect();
                }
                sendViewSizeZoom();
            }
        }
    }
    private Rect mLastVisibleRectSent;
    private Rect mLastGlobalRect;
    private Rect sendOurVisibleRect() {
        if (mPreviewZoomOnly) return mLastVisibleRectSent;
        Rect rect = new Rect();
        calcOurContentVisibleRect(rect);
        if (!rect.equals(mLastVisibleRectSent)) {
            Point pos = new Point(rect.left, rect.top);
            mWebViewCore.sendMessage(EventHub.SET_SCROLL_OFFSET,
                    nativeMoveGeneration(), 0, pos);
            mLastVisibleRectSent = rect;
        }
        Rect globalRect = new Rect();
        if (getGlobalVisibleRect(globalRect)
                && !globalRect.equals(mLastGlobalRect)) {
            if (DebugFlags.WEB_VIEW) {
                Log.v(LOGTAG, "sendOurVisibleRect=(" + globalRect.left + ","
                        + globalRect.top + ",r=" + globalRect.right + ",b="
                        + globalRect.bottom);
            }
            mWebViewCore.sendMessage(EventHub.SET_GLOBAL_BOUNDS, globalRect);
            mLastGlobalRect = globalRect;
        }
        return rect;
    }
    private void calcOurVisibleRect(Rect r) {
        Point p = new Point();
        getGlobalVisibleRect(r, p);
        r.offset(-p.x, -p.y);
        if (mFindIsUp) {
            r.bottom -= mFindHeight;
        }
    }
    private void calcOurContentVisibleRect(Rect r) {
        calcOurVisibleRect(r);
        r.left = Math.max(viewToContentX(r.left), 0);
        r.top = Math.max(viewToContentY(r.top + getVisibleTitleHeight()), 0);
        r.right = Math.min(viewToContentX(r.right), mContentWidth);
        r.bottom = Math.min(viewToContentY(r.bottom), mContentHeight);
    }
    private void calcOurContentVisibleRectF(RectF r) {
        Rect ri = new Rect(0,0,0,0);
        calcOurVisibleRect(ri);
        r.left = Math.max(viewToContentXf(ri.left), 0.0f);
        r.top = Math.max(viewToContentYf(ri.top + getVisibleTitleHeight()), 0.0f);
        r.right = Math.min(viewToContentXf(ri.right), (float)mContentWidth);
        r.bottom = Math.min(viewToContentYf(ri.bottom), (float)mContentHeight);
    }
    static class ViewSizeData {
        int mWidth;
        int mHeight;
        int mTextWrapWidth;
        int mAnchorX;
        int mAnchorY;
        float mScale;
        boolean mIgnoreHeight;
    }
    private boolean sendViewSizeZoom() {
        if (mPreviewZoomOnly) return false;
        int viewWidth = getViewWidth();
        int newWidth = Math.round(viewWidth * mInvActualScale);
        int newHeight = Math.round(getViewHeight() * mInvActualScale);
        if (newWidth > mLastWidthSent && mWrapContent) {
            newHeight = 0;
        }
        if (newWidth != mLastWidthSent || newHeight != mLastHeightSent) {
            ViewSizeData data = new ViewSizeData();
            data.mWidth = newWidth;
            data.mHeight = newHeight;
            data.mTextWrapWidth = Math.round(viewWidth / mTextWrapScale);;
            data.mScale = mActualScale;
            data.mIgnoreHeight = mZoomScale != 0 && !mHeightCanMeasure;
            data.mAnchorX = mAnchorX;
            data.mAnchorY = mAnchorY;
            mWebViewCore.sendMessage(EventHub.VIEW_SIZE_CHANGED, data);
            mLastWidthSent = newWidth;
            mLastHeightSent = newHeight;
            mAnchorX = mAnchorY = 0;
            return true;
        }
        return false;
    }
    @Override
    protected int computeHorizontalScrollRange() {
        if (mDrawHistory) {
            return mHistoryWidth;
        } else if (mHorizontalScrollBarMode == SCROLLBAR_ALWAYSOFF
                && (mActualScale - mMinZoomScale <= MINIMUM_SCALE_INCREMENT)) {
            return computeHorizontalScrollExtent();
        } else {
            return (int) Math.floor(mContentWidth * mActualScale);
        }
    }
    @Override
    protected int computeVerticalScrollRange() {
        if (mDrawHistory) {
            return mHistoryHeight;
        } else if (mVerticalScrollBarMode == SCROLLBAR_ALWAYSOFF
                && (mActualScale - mMinZoomScale <= MINIMUM_SCALE_INCREMENT)) {
            return computeVerticalScrollExtent();
        } else {
            return (int) Math.floor(mContentHeight * mActualScale);
        }
    }
    @Override
    protected int computeVerticalScrollOffset() {
        return Math.max(mScrollY - getTitleHeight(), 0);
    }
    @Override
    protected int computeVerticalScrollExtent() {
        return getViewHeight();
    }
    @Override
    protected void onDrawVerticalScrollBar(Canvas canvas,
                                           Drawable scrollBar,
                                           int l, int t, int r, int b) {
        scrollBar.setBounds(l, t + getVisibleTitleHeight(), r, b);
        scrollBar.draw(canvas);
    }
    public String getUrl() {
        WebHistoryItem h = mCallbackProxy.getBackForwardList().getCurrentItem();
        return h != null ? h.getUrl() : null;
    }
    public String getOriginalUrl() {
        WebHistoryItem h = mCallbackProxy.getBackForwardList().getCurrentItem();
        return h != null ? h.getOriginalUrl() : null;
    }
    public String getTitle() {
        WebHistoryItem h = mCallbackProxy.getBackForwardList().getCurrentItem();
        return h != null ? h.getTitle() : null;
    }
    public Bitmap getFavicon() {
        WebHistoryItem h = mCallbackProxy.getBackForwardList().getCurrentItem();
        return h != null ? h.getFavicon() : null;
    }
    public String getTouchIconUrl() {
        WebHistoryItem h = mCallbackProxy.getBackForwardList().getCurrentItem();
        return h != null ? h.getTouchIconUrl() : null;
    }
    public int getProgress() {
        return mCallbackProxy.getProgress();
    }
    public int getContentHeight() {
        return mContentHeight;
    }
    public int getContentWidth() {
        return mContentWidth;
    }
    public void pauseTimers() {
        mWebViewCore.sendMessage(EventHub.PAUSE_TIMERS);
    }
    public void resumeTimers() {
        mWebViewCore.sendMessage(EventHub.RESUME_TIMERS);
    }
    public void onPause() {
        if (!mIsPaused) {
            mIsPaused = true;
            mWebViewCore.sendMessage(EventHub.ON_PAUSE);
        }
    }
    public void onResume() {
        if (mIsPaused) {
            mIsPaused = false;
            mWebViewCore.sendMessage(EventHub.ON_RESUME);
        }
    }
    public boolean isPaused() {
        return mIsPaused;
    }
    public void freeMemory() {
        mWebViewCore.sendMessage(EventHub.FREE_MEMORY);
    }
    public void clearCache(boolean includeDiskFiles) {
        mWebViewCore.sendMessage(EventHub.CLEAR_CACHE,
                includeDiskFiles ? 1 : 0, 0);
    }
    public void clearFormData() {
        if (inEditingMode()) {
            AutoCompleteAdapter adapter = null;
            mWebTextView.setAdapterCustom(adapter);
        }
    }
    public void clearHistory() {
        mCallbackProxy.getBackForwardList().setClearPending();
        mWebViewCore.sendMessage(EventHub.CLEAR_HISTORY);
    }
    public void clearSslPreferences() {
        mWebViewCore.sendMessage(EventHub.CLEAR_SSL_PREF_TABLE);
    }
    public WebBackForwardList copyBackForwardList() {
        return mCallbackProxy.getBackForwardList().clone();
    }
    public void findNext(boolean forward) {
        if (0 == mNativeClass) return; 
        nativeFindNext(forward);
    }
    public int findAll(String find) {
        if (0 == mNativeClass) return 0; 
        int result = find != null ? nativeFindAll(find.toLowerCase(),
                find.toUpperCase()) : 0;
        invalidate();
        mLastFind = find;
        return result;
    }
    public void setFindIsUp(boolean isUp) {
        mFindIsUp = isUp;
        if (isUp) {
            recordNewContentSize(mContentWidth, mContentHeight + mFindHeight,
                    false);
        }
        if (0 == mNativeClass) return; 
        nativeSetFindIsUp(isUp);
    }
    private boolean mFindIsUp;
    private int mFindHeight;
    private String mLastFind;
    public static String findAddress(String addr) {
        return findAddress(addr, false);
    }
    public static String findAddress(String addr, boolean caseInsensitive) {
        return WebViewCore.nativeFindAddress(addr, caseInsensitive);
    }
    public void clearMatches() {
        mLastFind = "";
        if (mNativeClass == 0)
            return;
        nativeSetFindIsEmpty();
        invalidate();
    }
    public void notifyFindDialogDismissed() {
        if (mWebViewCore == null) {
            return;
        }
        clearMatches();
        setFindIsUp(false);
        recordNewContentSize(mContentWidth, mContentHeight - mFindHeight,
                false);
        pinScrollTo(mScrollX, mScrollY, false, 0);
        invalidate();
    }
    public void setFindDialogHeight(int height) {
        if (DebugFlags.WEB_VIEW) {
            Log.v(LOGTAG, "setFindDialogHeight height=" + height);
        }
        mFindHeight = height;
    }
    public void documentHasImages(Message response) {
        if (response == null) {
            return;
        }
        mWebViewCore.sendMessage(EventHub.DOC_HAS_IMAGES, response);
    }
    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            int oldX = mScrollX;
            int oldY = mScrollY;
            mScrollX = mScroller.getCurrX();
            mScrollY = mScroller.getCurrY();
            postInvalidate();  
            if (oldX != mScrollX || oldY != mScrollY) {
                onScrollChanged(mScrollX, mScrollY, oldX, oldY);
            }
        } else {
            super.computeScroll();
        }
    }
    private static int computeDuration(int dx, int dy) {
        int distance = Math.max(Math.abs(dx), Math.abs(dy));
        int duration = distance * 1000 / STD_SPEED;
        return Math.min(duration, MAX_DURATION);
    }
    private boolean pinScrollBy(int dx, int dy, boolean animate, int animationDuration) {
        return pinScrollTo(mScrollX + dx, mScrollY + dy, animate, animationDuration);
    }
    private boolean pinScrollTo(int x, int y, boolean animate, int animationDuration) {
        x = pinLocX(x);
        y = pinLocY(y);
        int dx = x - mScrollX;
        int dy = y - mScrollY;
        if ((dx | dy) == 0) {
            return false;
        }
        if (animate) {
            mScroller.startScroll(mScrollX, mScrollY, dx, dy,
                    animationDuration > 0 ? animationDuration : computeDuration(dx, dy));
            awakenScrollBars(mScroller.getDuration());
            invalidate();
        } else {
            abortAnimation(); 
            scrollTo(x, y);
        }
        return true;
    }
    private boolean setContentScrollBy(int cx, int cy, boolean animate) {
        if (mDrawHistory) {
            return false;
        }
        cx = contentToViewDimension(cx);
        cy = contentToViewDimension(cy);
        if (mHeightCanMeasure) {
            if (cy != 0) {
                Rect tempRect = new Rect();
                calcOurVisibleRect(tempRect);
                tempRect.offset(cx, cy);
                requestRectangleOnScreen(tempRect);
            }
            return cy == 0 && cx != 0 && pinScrollBy(cx, 0, animate, 0);
        } else {
            return pinScrollBy(cx, cy, animate, 0);
        }
    }
     void onPageFinished(String url) {
        if (mPageThatNeedsToSlideTitleBarOffScreen != null) {
            if (mPageThatNeedsToSlideTitleBarOffScreen.equals(url)
                    && mScrollX == 0 && mScrollY == 0) {
                pinScrollTo(0, mYDistanceToSlideTitleOffScreen, true,
                        SLIDE_TITLE_DURATION);
            }
            mPageThatNeedsToSlideTitleBarOffScreen = null;
        }
    }
    private String mPageThatNeedsToSlideTitleBarOffScreen;
    private int mYDistanceToSlideTitleOffScreen;
    private boolean setContentScrollTo(int cx, int cy) {
        if (mDrawHistory) {
            return false;
        }
        int vx;
        int vy;
        if ((cx | cy) == 0) {
            vx = 0;
            vy = 0;
        } else {
            vx = contentToViewX(cx);
            vy = contentToViewY(cy);
        }
        if (cx == 0 && cy == 1 && mScrollX == 0 && mScrollY == 0
                && mTitleBar != null) {
            if (getProgress() < 100) {
                mPageThatNeedsToSlideTitleBarOffScreen = getUrl();
                mYDistanceToSlideTitleOffScreen = vy;
            } else {
                pinScrollTo(vx, vy, true, SLIDE_TITLE_DURATION);
            }
            return false;
        }
        pinScrollTo(vx, vy, false, 0);
        if ((mScrollX != vx && cx >= 0) || (mScrollY != vy && cy >= 0)) {
            return true;
        } else {
            return false;
        }
    }
    private void spawnContentScrollTo(int cx, int cy) {
        if (mDrawHistory) {
            return;
        }
        int vx = contentToViewX(cx);
        int vy = contentToViewY(cy);
        pinScrollTo(vx, vy, true, 0);
    }
    private void contentSizeChanged(boolean updateLayout) {
        if ((mContentWidth | mContentHeight) == 0) {
            return;
        }
        if (mHeightCanMeasure) {
            if (getMeasuredHeight() != contentToViewDimension(mContentHeight)
                    || updateLayout) {
                requestLayout();
            }
        } else if (mWidthCanMeasure) {
            if (getMeasuredWidth() != contentToViewDimension(mContentWidth)
                    || updateLayout) {
                requestLayout();
            }
        } else {
            sendViewSizeZoom();
        }
    }
    public void setWebViewClient(WebViewClient client) {
        mCallbackProxy.setWebViewClient(client);
    }
    public WebViewClient getWebViewClient() {
        return mCallbackProxy.getWebViewClient();
    }
    public void setDownloadListener(DownloadListener listener) {
        mCallbackProxy.setDownloadListener(listener);
    }
    public void setWebChromeClient(WebChromeClient client) {
        mCallbackProxy.setWebChromeClient(client);
    }
    public WebChromeClient getWebChromeClient() {
        return mCallbackProxy.getWebChromeClient();
    }
    public void setWebBackForwardListClient(WebBackForwardListClient client) {
        mCallbackProxy.setWebBackForwardListClient(client);
    }
    public WebBackForwardListClient getWebBackForwardListClient() {
        return mCallbackProxy.getWebBackForwardListClient();
    }
    public void setPictureListener(PictureListener listener) {
        mPictureListener = listener;
    }
    public void externalRepresentation(Message callback) {
        mWebViewCore.sendMessage(EventHub.REQUEST_EXT_REPRESENTATION, callback);
    }
    public void documentAsText(Message callback) {
        mWebViewCore.sendMessage(EventHub.REQUEST_DOC_AS_TEXT, callback);
    }
    public void addJavascriptInterface(Object obj, String interfaceName) {
        WebViewCore.JSInterfaceData arg = new WebViewCore.JSInterfaceData();
        arg.mObject = obj;
        arg.mInterfaceName = interfaceName;
        mWebViewCore.sendMessage(EventHub.ADD_JS_INTERFACE, arg);
    }
    public WebSettings getSettings() {
        return mWebViewCore.getSettings();
    }
    public void addPackageNames(Set<String> packageNames) {
        mWebViewCore.sendMessage(EventHub.ADD_PACKAGE_NAMES, packageNames);
    }
    public void addPackageName(String packageName) {
        mWebViewCore.sendMessage(EventHub.ADD_PACKAGE_NAME, packageName);
    }
    public void removePackageName(String packageName) {
        mWebViewCore.sendMessage(EventHub.REMOVE_PACKAGE_NAME, packageName);
    }
    @Deprecated
    public static synchronized PluginList getPluginList() {
        return new PluginList();
    }
    @Deprecated
    public void refreshPlugins(boolean reloadOpenPages) { }
    @Override
    protected void finalize() throws Throwable {
        try {
            destroy();
        } finally {
            super.finalize();
        }
    }
    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        if (child == mTitleBar) {
            mTitleBar.offsetLeftAndRight(mScrollX - mTitleBar.getLeft());
        }
        return super.drawChild(canvas, child, drawingTime);
    }
    private void drawContent(Canvas canvas) {
        nativeRecordButtons(hasFocus() && hasWindowFocus(),
                            mTouchMode == TOUCH_SHORTPRESS_START_MODE
                            || mTrackballDown || mGotCenterDown, false);
        drawCoreAndCursorRing(canvas, mBackgroundColor, mDrawCursorRing);
    }
    @Override
    protected void onDraw(Canvas canvas) {
        if (mNativeClass == 0) {
            return;
        }
        if ((mContentWidth | mContentHeight) == 0 && mHistoryPicture == null) {
            canvas.drawColor(mBackgroundColor);
            return;
        }
        int saveCount = canvas.save();
        if (mTitleBar != null) {
            canvas.translate(0, (int) mTitleBar.getHeight());
        }
        if (mDragTrackerHandler == null) {
            drawContent(canvas);
        } else {
            if (!mDragTrackerHandler.draw(canvas)) {
                drawContent(canvas);
            }
            if (mDragTrackerHandler.isFinished()) {
                mDragTrackerHandler = null;
            }
        }
        canvas.restoreToCount(saveCount);
        int titleH = getVisibleTitleHeight();
        if (mTitleBar != null && titleH == 0) {
            int height = (int) (5f * getContext().getResources()
                    .getDisplayMetrics().density);
            mTitleShadow.setBounds(mScrollX, mScrollY, mScrollX + getWidth(),
                    mScrollY + height);
            mTitleShadow.draw(canvas);
        }
        if (AUTO_REDRAW_HACK && mAutoRedraw) {
            invalidate();
        }
        mWebViewCore.signalRepaintDone();
    }
    @Override
    public void setLayoutParams(ViewGroup.LayoutParams params) {
        if (params.height == LayoutParams.WRAP_CONTENT) {
            mWrapContent = true;
        }
        super.setLayoutParams(params);
    }
    @Override
    public boolean performLongClick() {
        if (getParent() == null) return false;
        if (mNativeClass != 0 && nativeCursorIsTextInput()) {
            centerKeyPressOnTextField();
            rebuildWebTextView();
        }
        if (inEditingMode()) {
            return mWebTextView.performLongClick();
        } else {
            return super.performLongClick();
        }
    }
    boolean inAnimateZoom() {
        return mZoomScale != 0;
    }
    private boolean mNeedToAdjustWebTextView;
    private boolean didUpdateTextViewBounds(boolean allowIntersect) {
        Rect contentBounds = nativeFocusCandidateNodeBounds();
        Rect vBox = contentToViewRect(contentBounds);
        Rect visibleRect = new Rect();
        calcOurVisibleRect(visibleRect);
        if (allowIntersect ? Rect.intersects(visibleRect, vBox)
                : visibleRect.contains(vBox)) {
            mWebTextView.setRect(vBox.left, vBox.top, vBox.width(),
                    vBox.height());
            mWebTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                    contentToViewDimension(
                    nativeFocusCandidateTextSize()));
            return true;
        } else {
            mWebTextView.remove();
            return false;
        }
    }
    private void drawExtras(Canvas canvas, int extras, boolean animationsRunning) {
        if (animationsRunning) {
            canvas.setDrawFilter(mWebViewCore.mZoomFilter);
        }
        nativeDrawExtras(canvas, extras);
        canvas.setDrawFilter(null);
    }
    private void drawCoreAndCursorRing(Canvas canvas, int color,
        boolean drawCursorRing) {
        if (mDrawHistory) {
            canvas.scale(mActualScale, mActualScale);
            canvas.drawPicture(mHistoryPicture);
            return;
        }
        boolean animateZoom = mZoomScale != 0;
        boolean animateScroll = ((!mScroller.isFinished()
                || mVelocityTracker != null)
                && (mTouchMode != TOUCH_DRAG_MODE ||
                mHeldMotionless != MOTIONLESS_TRUE))
                || mDeferTouchMode == TOUCH_DRAG_MODE;
        if (mTouchMode == TOUCH_DRAG_MODE) {
            if (mHeldMotionless == MOTIONLESS_PENDING) {
                mPrivateHandler.removeMessages(DRAG_HELD_MOTIONLESS);
                mPrivateHandler.removeMessages(AWAKEN_SCROLL_BARS);
                mHeldMotionless = MOTIONLESS_FALSE;
            }
            if (mHeldMotionless == MOTIONLESS_FALSE) {
                mPrivateHandler.sendMessageDelayed(mPrivateHandler
                        .obtainMessage(DRAG_HELD_MOTIONLESS), MOTIONLESS_TIME);
                mHeldMotionless = MOTIONLESS_PENDING;
            }
        }
        if (animateZoom) {
            float zoomScale;
            int interval = (int) (SystemClock.uptimeMillis() - mZoomStart);
            if (interval < ZOOM_ANIMATION_LENGTH) {
                float ratio = (float) interval / ZOOM_ANIMATION_LENGTH;
                zoomScale = 1.0f / (mInvInitialZoomScale
                        + (mInvFinalZoomScale - mInvInitialZoomScale) * ratio);
                invalidate();
            } else {
                zoomScale = mZoomScale;
                mZoomScale = 0;
                WebViewCore.resumeUpdatePicture(mWebViewCore);
                invalidate();
                if (mNeedToAdjustWebTextView) {
                    mNeedToAdjustWebTextView = false;
                    if (didUpdateTextViewBounds(false)
                            && nativeFocusCandidateIsPassword()) {
                        mWebTextView.setInPassword(true);
                    }
                }
            }
            float scale = zoomScale * mInvInitialZoomScale;
            int tx = Math.round(scale * (mInitialScrollX + mZoomCenterX)
                    - mZoomCenterX);
            tx = -pinLoc(tx, getViewWidth(), Math.round(mContentWidth
                    * zoomScale)) + mScrollX;
            int titleHeight = getTitleHeight();
            int ty = Math.round(scale
                    * (mInitialScrollY + mZoomCenterY - titleHeight)
                    - (mZoomCenterY - titleHeight));
            ty = -(ty <= titleHeight ? Math.max(ty, 0) : pinLoc(ty
                    - titleHeight, getViewHeight(), Math.round(mContentHeight
                    * zoomScale)) + titleHeight) + mScrollY;
            canvas.translate(tx, ty);
            canvas.scale(zoomScale, zoomScale);
            if (inEditingMode() && !mNeedToAdjustWebTextView
                    && mZoomScale != 0) {
                mNeedToAdjustWebTextView = true;
                if (nativeFocusCandidateIsPassword()) {
                    mWebTextView.setInPassword(false);
                }
            }
        } else {
            canvas.scale(mActualScale, mActualScale);
        }
        boolean UIAnimationsRunning = false;
        if (mNativeClass != 0 && nativeEvaluateLayersAnimations()) {
            UIAnimationsRunning = true;
            invalidate();
        }
        mWebViewCore.drawContentPicture(canvas, color,
                (animateZoom || mPreviewZoomOnly || UIAnimationsRunning),
                animateScroll);
        if (mNativeClass == 0) return;
        int extras = DRAW_EXTRAS_NONE;
        if (mFindIsUp) {
            if (!animateScroll) {
                extras = DRAW_EXTRAS_FIND;
            }
        } else if (mShiftIsPressed && !nativeFocusIsPlugin()) {
            if (!animateZoom && !mPreviewZoomOnly) {
                extras = DRAW_EXTRAS_SELECTION;
                nativeSetSelectionRegion(mTouchSelection || mExtendSelection);
                nativeSetSelectionPointer(!mTouchSelection, mInvActualScale,
                        mSelectX, mSelectY - getTitleHeight(),
                        mExtendSelection);
            }
        } else if (drawCursorRing) {
            extras = DRAW_EXTRAS_CURSOR_RING;
        }
        drawExtras(canvas, extras, UIAnimationsRunning);
        if (extras == DRAW_EXTRAS_CURSOR_RING) {
            if (mTouchMode == TOUCH_SHORTPRESS_START_MODE) {
                mTouchMode = TOUCH_SHORTPRESS_MODE;
                HitTestResult hitTest = getHitTestResult();
                if (hitTest == null
                        || hitTest.mType == HitTestResult.UNKNOWN_TYPE) {
                    mPrivateHandler.removeMessages(SWITCH_TO_LONGPRESS);
                }
            }
        }
        if (mFocusSizeChanged) {
            mFocusSizeChanged = false;
            if (!animateZoom && inEditingMode()) {
                didUpdateTextViewBounds(true);
            }
        }
    }
    private boolean mDrawHistory = false;
    private Picture mHistoryPicture = null;
    private int mHistoryWidth = 0;
    private int mHistoryHeight = 0;
    boolean drawHistory() {
        return mDrawHistory;
    }
    void switchOutDrawHistory() {
        if (null == mWebViewCore) return; 
        if (mDrawHistory && mWebViewCore.pictureReady()) {
            mDrawHistory = false;
            mHistoryPicture = null;
            invalidate();
            int oldScrollX = mScrollX;
            int oldScrollY = mScrollY;
            mScrollX = pinLocX(mScrollX);
            mScrollY = pinLocY(mScrollY);
            if (oldScrollX != mScrollX || oldScrollY != mScrollY) {
                mUserScroll = false;
                mWebViewCore.sendMessage(EventHub.SYNC_SCROLL, oldScrollX,
                        oldScrollY);
                onScrollChanged(mScrollX, mScrollY, oldScrollX, oldScrollY);
            } else {
                sendOurVisibleRect();
            }
        }
    }
    WebViewCore.CursorData cursorData() {
        WebViewCore.CursorData result = new WebViewCore.CursorData();
        result.mMoveGeneration = nativeMoveGeneration();
        result.mFrame = nativeCursorFramePointer();
        Point position = nativeCursorPosition();
        result.mX = position.x;
        result.mY = position.y;
        return result;
    }
     void deleteSelection(int start, int end) {
        mTextGeneration++;
        WebViewCore.TextSelectionData data
                = new WebViewCore.TextSelectionData(start, end);
        mWebViewCore.sendMessage(EventHub.DELETE_SELECTION, mTextGeneration, 0,
                data);
    }
     void setSelection(int start, int end) {
        mWebViewCore.sendMessage(EventHub.SET_SELECTION, start, end);
    }
    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
      InputConnection connection = super.onCreateInputConnection(outAttrs);
      outAttrs.imeOptions |= EditorInfo.IME_FLAG_NO_FULLSCREEN;
      return connection;
    }
    private void displaySoftKeyboard(boolean isTextView) {
        InputMethodManager imm = (InputMethodManager)
                getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        boolean zoom = mActualScale < mDefaultScale;
        if (zoom) {
            mInZoomOverview = false;
            mZoomCenterX = mLastTouchX;
            mZoomCenterY = mLastTouchY;
            setNewZoomScale(mDefaultScale, false, false);
        }
        if (isTextView) {
            rebuildWebTextView();
            if (inEditingMode()) {
                imm.showSoftInput(mWebTextView, 0);
                if (zoom) {
                    didUpdateTextViewBounds(true);
                }
                return;
            }
        }
        imm.showSoftInput(this, 0);
    }
    private void hideSoftKeyboard() {
        InputMethodManager imm = (InputMethodManager)
                getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(this.getWindowToken(), 0);
    }
     void rebuildWebTextView() {
        if (!hasFocus() && (null == mWebTextView || !mWebTextView.hasFocus())) {
            return;
        }
        boolean alreadyThere = inEditingMode();
        if (0 == mNativeClass || !nativeFocusCandidateIsTextInput()) {
            if (alreadyThere) {
                mWebTextView.remove();
            }
            return;
        }
        if (mWebTextView == null) {
            mWebTextView = new WebTextView(mContext, WebView.this);
            mTextGeneration = 0;
        }
        mWebTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                contentToViewDimension(nativeFocusCandidateTextSize()));
        Rect visibleRect = new Rect();
        calcOurContentVisibleRect(visibleRect);
        Rect bounds = nativeFocusCandidateNodeBounds();
        Rect vBox = contentToViewRect(bounds);
        mWebTextView.setRect(vBox.left, vBox.top, vBox.width(), vBox.height());
        if (!Rect.intersects(bounds, visibleRect)) {
            mWebTextView.bringIntoView();
        }
        String text = nativeFocusCandidateText();
        int nodePointer = nativeFocusCandidatePointer();
        if (alreadyThere && mWebTextView.isSameTextField(nodePointer)) {
            if (text != null && !text.equals(mWebTextView.getText().toString())
                    && nativeTextGeneration() == mTextGeneration) {
                mWebTextView.setTextAndKeepSelection(text);
            }
        } else {
            mWebTextView.setGravity(nativeFocusCandidateIsRtlText() ?
                    Gravity.RIGHT : Gravity.NO_GRAVITY);
            mWebTextView.setNodePointer(nodePointer);
            mWebTextView.setType(nativeFocusCandidateType());
            if (null == text) {
                if (DebugFlags.WEB_VIEW) {
                    Log.v(LOGTAG, "rebuildWebTextView null == text");
                }
                text = "";
            }
            mWebTextView.setTextAndKeepSelection(text);
            InputMethodManager imm = InputMethodManager.peekInstance();
            if (imm != null && imm.isActive(mWebTextView)) {
                imm.restartInput(mWebTextView);
            }
        }
        mWebTextView.requestFocus();
    }
     void requestFormData(String name, int nodePointer) {
        if (mWebViewCore.getSettings().getSaveFormData()) {
            Message update = mPrivateHandler.obtainMessage(REQUEST_FORM_DATA);
            update.arg1 = nodePointer;
            RequestFormData updater = new RequestFormData(name, getUrl(),
                    update);
            Thread t = new Thread(updater);
            t.start();
        }
    }
     void requestLabel(int framePointer, int nodePointer) {
        mWebViewCore.sendMessage(EventHub.REQUEST_LABEL, framePointer,
                nodePointer);
    }
    private class RequestFormData implements Runnable {
        private String mName;
        private String mUrl;
        private Message mUpdateMessage;
        public RequestFormData(String name, String url, Message msg) {
            mName = name;
            mUrl = url;
            mUpdateMessage = msg;
        }
        public void run() {
            ArrayList<String> pastEntries = mDatabase.getFormData(mUrl, mName);
            if (pastEntries.size() > 0) {
                AutoCompleteAdapter adapter = new
                        AutoCompleteAdapter(mContext, pastEntries);
                mUpdateMessage.obj = adapter;
                mUpdateMessage.sendToTarget();
            }
        }
    }
    public void dumpDisplayTree() {
        nativeDumpDisplayTree(getUrl());
    }
    public void dumpDomTree(boolean toFile) {
        mWebViewCore.sendMessage(EventHub.DUMP_DOMTREE, toFile ? 1 : 0, 0);
    }
    public void dumpRenderTree(boolean toFile) {
        mWebViewCore.sendMessage(EventHub.DUMP_RENDERTREE, toFile ? 1 : 0, 0);
    }
    public void dumpV8Counters() {
        mWebViewCore.sendMessage(EventHub.DUMP_V8COUNTERS);
    }
    private boolean mGotCenterDown = false;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (DebugFlags.WEB_VIEW) {
            Log.v(LOGTAG, "keyDown at " + System.currentTimeMillis()
                    + ", " + event + ", unicode=" + event.getUnicodeChar());
        }
        if (mNativeClass == 0) {
            return false;
        }
        if (AUTO_REDRAW_HACK && (keyCode == KeyEvent.KEYCODE_CALL)) {
            mAutoRedraw = !mAutoRedraw;
            if (mAutoRedraw) {
                invalidate();
            }
            return true;
        }
        if (event.isSystem()
                || mCallbackProxy.uiOverrideKeyEvent(event)) {
            return false;
        }
        if (keyCode == KeyEvent.KEYCODE_SHIFT_LEFT
                || keyCode == KeyEvent.KEYCODE_SHIFT_RIGHT) {
            if (nativeFocusIsPlugin()) {
                mShiftIsPressed = true;
            } else if (!nativeCursorWantsKeyEvents() && !mShiftIsPressed) {
                setUpSelectXY();
            }
        }
        if (keyCode >= KeyEvent.KEYCODE_DPAD_UP
                && keyCode <= KeyEvent.KEYCODE_DPAD_RIGHT) {
            switchOutDrawHistory();
            if (nativeFocusIsPlugin()) {
                letPluginHandleNavKey(keyCode, event.getEventTime(), true);
                return true;
            }
            if (mShiftIsPressed) {
                int xRate = keyCode == KeyEvent.KEYCODE_DPAD_LEFT
                    ? -1 : keyCode == KeyEvent.KEYCODE_DPAD_RIGHT ? 1 : 0;
                int yRate = keyCode == KeyEvent.KEYCODE_DPAD_UP ?
                    -1 : keyCode == KeyEvent.KEYCODE_DPAD_DOWN ? 1 : 0;
                int multiplier = event.getRepeatCount() + 1;
                moveSelection(xRate * multiplier, yRate * multiplier);
                return true;
            }
            if (navHandledKey(keyCode, 1, false, event.getEventTime())) {
                playSoundEffect(keyCodeToSoundsEffect(keyCode));
                return true;
            }
            return false;
        }
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            switchOutDrawHistory();
            if (event.getRepeatCount() == 0) {
                if (mShiftIsPressed && !nativeFocusIsPlugin()) {
                    return true; 
                }
                mGotCenterDown = true;
                mPrivateHandler.sendMessageDelayed(mPrivateHandler
                        .obtainMessage(LONG_PRESS_CENTER), LONG_PRESS_TIMEOUT);
                nativeRecordButtons(hasFocus() && hasWindowFocus(), true, true);
                return true;
            }
            return false;
        }
        if (keyCode != KeyEvent.KEYCODE_SHIFT_LEFT
                && keyCode != KeyEvent.KEYCODE_SHIFT_RIGHT) {
            mExtendSelection = mShiftIsPressed = false;
            if (mTouchMode == TOUCH_SELECT_MODE) {
                mTouchMode = TOUCH_INIT_MODE;
            }
        }
        if (getSettings().getNavDump()) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_4:
                    dumpDisplayTree();
                    break;
                case KeyEvent.KEYCODE_5:
                case KeyEvent.KEYCODE_6:
                    dumpDomTree(keyCode == KeyEvent.KEYCODE_5);
                    break;
                case KeyEvent.KEYCODE_7:
                case KeyEvent.KEYCODE_8:
                    dumpRenderTree(keyCode == KeyEvent.KEYCODE_7);
                    break;
                case KeyEvent.KEYCODE_9:
                    nativeInstrumentReport();
                    return true;
            }
        }
        if (nativeCursorIsTextInput()) {
            mWebViewCore.sendMessage(EventHub.CLICK, nativeCursorFramePointer(),
                    nativeCursorNodePointer());
            rebuildWebTextView();
            if (inEditingMode()) {
                mWebTextView.setDefaultSelection();
                return mWebTextView.dispatchKeyEvent(event);
            }
        } else if (nativeHasFocusNode()) {
            rebuildWebTextView();
            if (inEditingMode()) {
                mWebTextView.setDefaultSelection();
                return mWebTextView.dispatchKeyEvent(event);
            }
        }
        if (nativeCursorWantsKeyEvents() || true) {
            mWebViewCore.sendMessage(EventHub.KEY_DOWN, event);
            return true;
        }
        return false;
    }
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (DebugFlags.WEB_VIEW) {
            Log.v(LOGTAG, "keyUp at " + System.currentTimeMillis()
                    + ", " + event + ", unicode=" + event.getUnicodeChar());
        }
        if (mNativeClass == 0) {
            return false;
        }
        if (keyCode == KeyEvent.KEYCODE_CALL && nativeHasCursorNode()) {
            String text = nativeCursorText();
            if (!nativeCursorIsTextInput() && text != null
                    && text.startsWith(SCHEME_TEL)) {
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(text));
                getContext().startActivity(intent);
                return true;
            }
        }
        if (event.isSystem() || mCallbackProxy.uiOverrideKeyEvent(event)) {
            return false;
        }
        if (keyCode == KeyEvent.KEYCODE_SHIFT_LEFT
                || keyCode == KeyEvent.KEYCODE_SHIFT_RIGHT) {
            if (nativeFocusIsPlugin()) {
                mShiftIsPressed = false;
            } else if (commitCopy()) {
                return true;
            }
        }
        if (keyCode >= KeyEvent.KEYCODE_DPAD_UP
                && keyCode <= KeyEvent.KEYCODE_DPAD_RIGHT) {
            if (nativeFocusIsPlugin()) {
                letPluginHandleNavKey(keyCode, event.getEventTime(), false);
                return true;
            }
            return false;
        }
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            mPrivateHandler.removeMessages(LONG_PRESS_CENTER);
            mGotCenterDown = false;
            if (mShiftIsPressed && !nativeFocusIsPlugin()) {
                if (mExtendSelection) {
                    commitCopy();
                } else {
                    mExtendSelection = true;
                    invalidate(); 
                }
                return true; 
            }
            Rect visibleRect = sendOurVisibleRect();
            if (!nativeCursorIntersects(visibleRect)) {
                return false;
            }
            WebViewCore.CursorData data = cursorData();
            mWebViewCore.sendMessage(EventHub.SET_MOVE_MOUSE, data);
            playSoundEffect(SoundEffectConstants.CLICK);
            if (nativeCursorIsTextInput()) {
                rebuildWebTextView();
                centerKeyPressOnTextField();
                if (inEditingMode()) {
                    mWebTextView.setDefaultSelection();
                }
                return true;
            }
            clearTextEntry(true);
            nativeSetFollowedLink(true);
            if (!mCallbackProxy.uiOverrideUrlLoading(nativeCursorText())) {
                mWebViewCore.sendMessage(EventHub.CLICK, data.mFrame,
                        nativeCursorNodePointer());
            }
            return true;
        }
        if (nativeCursorWantsKeyEvents() || true) {
            mWebViewCore.sendMessage(EventHub.KEY_UP, event);
            return true;
        }
        return false;
    }
    private void setUpSelectXY() {
        mExtendSelection = false;
        mShiftIsPressed = true;
        if (nativeHasCursorNode()) {
            Rect rect = nativeCursorNodeBounds();
            mSelectX = contentToViewX(rect.left);
            mSelectY = contentToViewY(rect.top);
        } else if (mLastTouchY > getVisibleTitleHeight()) {
            mSelectX = mScrollX + (int) mLastTouchX;
            mSelectY = mScrollY + (int) mLastTouchY;
        } else {
            mSelectX = mScrollX + getViewWidth() / 2;
            mSelectY = mScrollY + getViewHeightWithTitle() / 2;
        }
        nativeHideCursor();
    }
    public void emulateShiftHeld() {
        if (0 == mNativeClass) return; 
        setUpSelectXY();
    }
    private boolean commitCopy() {
        boolean copiedSomething = false;
        if (mExtendSelection) {
            String selection = nativeGetSelection();
            if (selection != "") {
                if (DebugFlags.WEB_VIEW) {
                    Log.v(LOGTAG, "commitCopy \"" + selection + "\"");
                }
                Toast.makeText(mContext
                        , com.android.internal.R.string.text_copied
                        , Toast.LENGTH_SHORT).show();
                copiedSomething = true;
                try {
                    IClipboard clip = IClipboard.Stub.asInterface(
                            ServiceManager.getService("clipboard"));
                            clip.setClipboardText(selection);
                } catch (android.os.RemoteException e) {
                    Log.e(LOGTAG, "Clipboard failed", e);
                }
            }
            mExtendSelection = false;
        }
        mShiftIsPressed = false;
        invalidate(); 
        if (mTouchMode == TOUCH_SELECT_MODE) {
            mTouchMode = TOUCH_INIT_MODE;
        }
        return copiedSomething;
    }
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (hasWindowFocus()) setActive(true);
    }
    @Override
    protected void onDetachedFromWindow() {
        clearTextEntry(false);
        dismissZoomControl();
        if (hasWindowFocus()) setActive(false);
        super.onDetachedFromWindow();
    }
    @Deprecated
    public void onChildViewAdded(View parent, View child) {}
    @Deprecated
    public void onChildViewRemoved(View p, View child) {}
    @Deprecated
    public void onGlobalFocusChanged(View oldFocus, View newFocus) {
    }
    private void setActive(boolean active) {
        if (active) {
            if (hasFocus()) {
                mDrawCursorRing = true;
                if (mNativeClass != 0) {
                    nativeRecordButtons(true, false, true);
                    if (inEditingMode()) {
                        mWebViewCore.sendMessage(EventHub.SET_ACTIVE, 1, 0);
                    }
                }
            } else {
                mDrawCursorRing = false;
            }
        } else {
            if (mWebViewCore != null && getSettings().getBuiltInZoomControls()
                    && (mZoomButtonsController == null ||
                            !mZoomButtonsController.isVisible())) {
                mDrawCursorRing = false;
            }
            mGotKeyDown = false;
            mShiftIsPressed = false;
            mPrivateHandler.removeMessages(SWITCH_TO_LONGPRESS);
            mTouchMode = TOUCH_DONE_MODE;
            if (mNativeClass != 0) {
                nativeRecordButtons(false, false, true);
            }
            setFocusControllerInactive();
        }
        invalidate();
    }
    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        setActive(hasWindowFocus);
        if (hasWindowFocus) {
            BrowserFrame.sJavaBridge.setActiveWebView(this);
        } else {
            BrowserFrame.sJavaBridge.removeActiveWebView(this);
        }
        super.onWindowFocusChanged(hasWindowFocus);
    }
     void setFocusControllerInactive() {
        if (mNativeClass == 0) return;
        mWebViewCore.sendMessage(EventHub.SET_ACTIVE, 0, 0);
    }
    @Override
    protected void onFocusChanged(boolean focused, int direction,
            Rect previouslyFocusedRect) {
        if (DebugFlags.WEB_VIEW) {
            Log.v(LOGTAG, "MT focusChanged " + focused + ", " + direction);
        }
        if (focused) {
            if (hasWindowFocus()) {
                mDrawCursorRing = true;
                if (mNativeClass != 0) {
                    nativeRecordButtons(true, false, true);
                }
            }
        } else {
            if (!inEditingMode()) {
                mDrawCursorRing = false;
                if (mNativeClass != 0) {
                    nativeRecordButtons(false, false, true);
                }
                setFocusControllerInactive();
            }
            mGotKeyDown = false;
        }
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
    }
    @Override
    protected boolean setFrame(int left, int top, int right, int bottom) {
        boolean changed = super.setFrame(left, top, right, bottom);
        if (!changed && mHeightCanMeasure) {
            sendViewSizeZoom();
        }
        return changed;
    }
    private static class PostScale implements Runnable {
        final WebView mWebView;
        final boolean mUpdateTextWrap;
        public PostScale(WebView webView, boolean updateTextWrap) {
            mWebView = webView;
            mUpdateTextWrap = updateTextWrap;
        }
        public void run() {
            if (mWebView.mWebViewCore != null) {
                mWebView.setNewZoomScale(mWebView.mActualScale,
                        mUpdateTextWrap, true);
                if (mWebView.getSettings().getBuiltInZoomControls()) {
                    mWebView.updateZoomButtonsEnabled();
                }
            }
        }
    }
    @Override
    protected void onSizeChanged(int w, int h, int ow, int oh) {
        super.onSizeChanged(w, h, ow, oh);
        if (mZoomScale == 0) { 
            mZoomCenterX = 0;
            mZoomCenterY = getVisibleTitleHeight();
            mAnchorX = viewToContentX((int) mZoomCenterX + mScrollX);
            mAnchorY = viewToContentY((int) mZoomCenterY + mScrollY);
        }
        int newMaxViewportWidth = (int) (Math.max(w, h) / DEFAULT_MIN_ZOOM_SCALE);
        if (newMaxViewportWidth > sMaxViewportWidth) {
            sMaxViewportWidth = newMaxViewportWidth;
        }
        if (!mMinZoomScaleFixed) {
            mMinZoomScale = Math.min(1.0f, (float) getViewWidth()
                    / (mDrawHistory ? mHistoryPicture.getWidth()
                            : mZoomOverviewWidth));
            if (mInitialScaleInPercent > 0) {
                float initialScale = mInitialScaleInPercent / 100.0f;
                if (mMinZoomScale > initialScale) {
                    mMinZoomScale = initialScale;
                }
            }
        }
        dismissZoomControl();
        post(new PostScale(this, w != ow));
    }
    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        sendOurVisibleRect();
        int titleHeight = getTitleHeight();
        if (Math.max(titleHeight - t, 0) != Math.max(titleHeight - oldt, 0)) {
            sendViewSizeZoom();
        }
    }
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        boolean dispatch = true;
        if (!inEditingMode() && (mNativeClass == 0 || !nativeFocusIsPlugin())) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                mGotKeyDown = true;
            } else {
                if (!mGotKeyDown) {
                    dispatch = false;
                }
                mGotKeyDown = false;
            }
        }
        if (dispatch) {
            return super.dispatchKeyEvent(event);
        } else {
            return false;
        }
    }
    private int mMinLockSnapReverseDistance;
    private static final float MAX_SLOPE_FOR_DIAG = 1.5f;
    private static final int MIN_BREAK_SNAP_CROSS_DISTANCE = 80;
    private static int sign(float x) {
        return x > 0 ? 1 : (x < 0 ? -1 : 0);
    }
    private static final int MIN_SCROLL_AMOUNT_TO_DISABLE_DRAG_TRACKER = 4;
    private class DragTrackerHandler {
        private final DragTracker mProxy;
        private final float mStartY, mStartX;
        private final float mMinDY, mMinDX;
        private final float mMaxDY, mMaxDX;
        private float mCurrStretchY, mCurrStretchX;
        private int mSX, mSY;
        private Interpolator mInterp;
        private float[] mXY = new float[2];
        private static final int DRAGGING_STATE = 0;
        private static final int ANIMATING_STATE = 1;
        private static final int FINISHED_STATE = 2;
        private int mState;
        public DragTrackerHandler(float x, float y, DragTracker proxy) {
            mProxy = proxy;
            int docBottom = computeVerticalScrollRange() + getTitleHeight();
            int viewTop = getScrollY();
            int viewBottom = viewTop + getHeight();
            mStartY = y;
            mMinDY = -viewTop;
            mMaxDY = docBottom - viewBottom;
            if (DebugFlags.DRAG_TRACKER || DEBUG_DRAG_TRACKER) {
                Log.d(DebugFlags.DRAG_TRACKER_LOGTAG, " dragtracker y= " + y +
                      " up/down= " + mMinDY + " " + mMaxDY);
            }
            int docRight = computeHorizontalScrollRange();
            int viewLeft = getScrollX();
            int viewRight = viewLeft + getWidth();
            mStartX = x;
            mMinDX = -viewLeft;
            mMaxDX = docRight - viewRight;
            mState = DRAGGING_STATE;
            mProxy.onStartDrag(x, y);
            mSX = -99999;
        }
        private float computeStretch(float delta, float min, float max) {
            float stretch = 0;
            if (max - min > MIN_SCROLL_AMOUNT_TO_DISABLE_DRAG_TRACKER) {
                if (delta < min) {
                    stretch = delta - min;
                } else if (delta > max) {
                    stretch = delta - max;
                }
            }
            return stretch;
        }
        public void dragTo(float x, float y) {
            float sy = computeStretch(mStartY - y, mMinDY, mMaxDY);
            float sx = computeStretch(mStartX - x, mMinDX, mMaxDX);
            if ((mSnapScrollMode & SNAP_X) != 0) {
                sy = 0;
            } else if ((mSnapScrollMode & SNAP_Y) != 0) {
                sx = 0;
            }
            if (mCurrStretchX != sx || mCurrStretchY != sy) {
                mCurrStretchX = sx;
                mCurrStretchY = sy;
                if (DebugFlags.DRAG_TRACKER || DEBUG_DRAG_TRACKER) {
                    Log.d(DebugFlags.DRAG_TRACKER_LOGTAG, "---- stretch " + sx +
                          " " + sy);
                }
                if (mProxy.onStretchChange(sx, sy)) {
                    invalidate();
                }
            }
        }
        public void stopDrag() {
            final int DURATION = 200;
            int now = (int)SystemClock.uptimeMillis();
            mInterp = new Interpolator(2);
            mXY[0] = mCurrStretchX;
            mXY[1] = mCurrStretchY;
            float[] blend = new float[] { 0, 0.5f, 0.75f, 1 };
            mInterp.setKeyFrame(0, now, mXY, blend);
            float[] zerozero = new float[] { 0, 0 };
            mInterp.setKeyFrame(1, now + DURATION, zerozero, null);
            mState = ANIMATING_STATE;
            if (DebugFlags.DRAG_TRACKER || DEBUG_DRAG_TRACKER) {
                Log.d(DebugFlags.DRAG_TRACKER_LOGTAG, "----- stopDrag, starting animation");
            }
        }
        public boolean isFinished() {
            return mState == FINISHED_STATE;
        }
        private int hiddenHeightOfTitleBar() {
            return getTitleHeight() - getVisibleTitleHeight();
        }
        private Bitmap.Config offscreenBitmapConfig() {
            return Bitmap.Config.RGB_565;
        }
        public boolean draw(Canvas canvas) {
            if (mCurrStretchX != 0 || mCurrStretchY != 0) {
                int sx = getScrollX();
                int sy = getScrollY() - hiddenHeightOfTitleBar();
                if (mSX != sx || mSY != sy) {
                    buildBitmap(sx, sy);
                    mSX = sx;
                    mSY = sy;
                }
                if (mState == ANIMATING_STATE) {
                    Interpolator.Result result = mInterp.timeToValues(mXY);
                    if (result == Interpolator.Result.FREEZE_END) {
                        mState = FINISHED_STATE;
                        return false;
                    } else {
                        mProxy.onStretchChange(mXY[0], mXY[1]);
                        invalidate();
                    }
                }
                int count = canvas.save(Canvas.MATRIX_SAVE_FLAG);
                canvas.translate(sx, sy);
                mProxy.onDraw(canvas);
                canvas.restoreToCount(count);
                return true;
            }
            if (DebugFlags.DRAG_TRACKER || DEBUG_DRAG_TRACKER) {
                Log.d(DebugFlags.DRAG_TRACKER_LOGTAG, " -- draw false " +
                      mCurrStretchX + " " + mCurrStretchY);
            }
            return false;
        }
        private void buildBitmap(int sx, int sy) {
            int w = getWidth();
            int h = getViewHeight();
            Bitmap bm = Bitmap.createBitmap(w, h, offscreenBitmapConfig());
            Canvas canvas = new Canvas(bm);
            canvas.translate(-sx, -sy);
            drawContent(canvas);
            if (DebugFlags.DRAG_TRACKER || DEBUG_DRAG_TRACKER) {
                Log.d(DebugFlags.DRAG_TRACKER_LOGTAG, "--- buildBitmap " + sx +
                      " " + sy + " " + w + " " + h);
            }
            mProxy.onBitmapChange(bm);
        }
    }
    public static class DragTracker {
        public void onStartDrag(float x, float y) {}
        public boolean onStretchChange(float sx, float sy) {
            return false;
        }
        public void onStopDrag() {}
        public void onBitmapChange(Bitmap bm) {}
        public void onDraw(Canvas canvas) {}
    }
    public DragTracker getDragTracker() {
        return mDragTracker;
    }
    public void setDragTracker(DragTracker tracker) {
        mDragTracker = tracker;
    }
    private DragTracker mDragTracker;
    private DragTrackerHandler mDragTrackerHandler;
    private class ScaleDetectorListener implements
            ScaleGestureDetector.OnScaleGestureListener {
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            cancelTouch();
            dismissZoomControl();
            mInZoomOverview = false;
            if (inEditingMode() && nativeFocusCandidateIsPassword()) {
                mWebTextView.setInPassword(false);
            }
            mViewManager.startZoom();
            return true;
        }
        public void onScaleEnd(ScaleGestureDetector detector) {
            if (mPreviewZoomOnly) {
                mPreviewZoomOnly = false;
                mAnchorX = viewToContentX((int) mZoomCenterX + mScrollX);
                mAnchorY = viewToContentY((int) mZoomCenterY + mScrollY);
                boolean reflowNow = (mActualScale - mMinZoomScale
                        <= MINIMUM_SCALE_INCREMENT)
                        || ((mActualScale <= 0.8 * mTextWrapScale));
                setNewZoomScale(mActualScale, reflowNow, true);
                invalidate();
            }
            if (inEditingMode() && didUpdateTextViewBounds(false)
                    && nativeFocusCandidateIsPassword()) {
                mWebTextView.setInPassword(true);
            }
            mTouchMode = TOUCH_PINCH_DRAG;
            mConfirmMove = true;
            startTouch(detector.getFocusX(), detector.getFocusY(),
                    mLastTouchTime);
            mViewManager.endZoom();
        }
        public boolean onScale(ScaleGestureDetector detector) {
            float scale = (float) (Math.round(detector.getScaleFactor()
                    * mActualScale * 100) / 100.0);
            if (Math.abs(scale - mActualScale) >= MINIMUM_SCALE_INCREMENT) {
                mPreviewZoomOnly = true;
                if (scale > mActualScale) {
                    scale = Math.min(scale, mActualScale * 1.25f);
                } else {
                    scale = Math.max(scale, mActualScale * 0.8f);
                }
                mZoomCenterX = detector.getFocusX();
                mZoomCenterY = detector.getFocusY();
                setNewZoomScale(scale, false, false);
                invalidate();
                return true;
            }
            return false;
        }
    }
    private boolean hitFocusedPlugin(int contentX, int contentY) {
        if (DebugFlags.WEB_VIEW) {
            Log.v(LOGTAG, "nativeFocusIsPlugin()=" + nativeFocusIsPlugin());
            Rect r = nativeFocusNodeBounds();
            Log.v(LOGTAG, "nativeFocusNodeBounds()=(" + r.left + ", " + r.top
                    + ", " + r.right + ", " + r.bottom + ")");
        }
        return nativeFocusIsPlugin()
                && nativeFocusNodeBounds().contains(contentX, contentY);
    }
    private boolean shouldForwardTouchEvent() {
        return mFullScreenHolder != null || (mForwardTouchEvents
                && mTouchMode != TOUCH_SELECT_MODE
                && mPreventDefault != PREVENT_DEFAULT_IGNORE);
    }
    private boolean inFullScreenMode() {
        return mFullScreenHolder != null;
    }
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mNativeClass == 0 || !isClickable() || !isLongClickable()) {
            return false;
        }
        if (DebugFlags.WEB_VIEW) {
            Log.v(LOGTAG, ev + " at " + ev.getEventTime() + " mTouchMode="
                    + mTouchMode);
        }
        int action;
        float x, y;
        long eventTime = ev.getEventTime();
        if (mSupportMultiTouch && ev.getPointerCount() > 1) {
            if (mMinZoomScale < mMaxZoomScale) {
                mScaleDetector.onTouchEvent(ev);
                if (mScaleDetector.isInProgress()) {
                    mLastTouchTime = eventTime;
                    return true;
                }
                x = mScaleDetector.getFocusX();
                y = mScaleDetector.getFocusY();
                action = ev.getAction() & MotionEvent.ACTION_MASK;
                if (action == MotionEvent.ACTION_POINTER_DOWN) {
                    cancelTouch();
                    action = MotionEvent.ACTION_DOWN;
                } else if (action == MotionEvent.ACTION_POINTER_UP) {
                    mLastTouchX = x;
                    mLastTouchY = y;
                } else if (action == MotionEvent.ACTION_MOVE) {
                    if (x < 0 || y < 0) {
                        return true;
                    }
                }
            } else {
                return true;
            }
        } else {
            action = ev.getAction();
            x = ev.getX();
            y = ev.getY();
        }
        if (x > getViewWidth() - 1) {
            x = getViewWidth() - 1;
        }
        if (y > getViewHeightWithTitle() - 1) {
            y = getViewHeightWithTitle() - 1;
        }
        float fDeltaX = mLastTouchX - x;
        float fDeltaY = mLastTouchY - y;
        int deltaX = (int) fDeltaX;
        int deltaY = (int) fDeltaY;
        int contentX = viewToContentX((int) x + mScrollX);
        int contentY = viewToContentY((int) y + mScrollY);
        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                mPreventDefault = PREVENT_DEFAULT_NO;
                mConfirmMove = false;
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                    mTouchMode = TOUCH_DRAG_START_MODE;
                    mConfirmMove = true;
                    mPrivateHandler.removeMessages(RESUME_WEBCORE_PRIORITY);
                } else if (!inFullScreenMode() && mShiftIsPressed) {
                    mSelectX = mScrollX + (int) x;
                    mSelectY = mScrollY + (int) y;
                    mTouchMode = TOUCH_SELECT_MODE;
                    if (DebugFlags.WEB_VIEW) {
                        Log.v(LOGTAG, "select=" + mSelectX + "," + mSelectY);
                    }
                    nativeMoveSelection(contentX, contentY, false);
                    mTouchSelection = mExtendSelection = true;
                    invalidate(); 
                } else if (mPrivateHandler.hasMessages(RELEASE_SINGLE_TAP)) {
                    mPrivateHandler.removeMessages(RELEASE_SINGLE_TAP);
                    if (deltaX * deltaX + deltaY * deltaY < mDoubleTapSlopSquare) {
                        mTouchMode = TOUCH_DOUBLE_TAP_MODE;
                    } else {
                        doShortPress();
                        mTouchMode = TOUCH_INIT_MODE;
                        mDeferTouchProcess = (!inFullScreenMode()
                                && mForwardTouchEvents) ? hitFocusedPlugin(
                                contentX, contentY) : false;
                    }
                } else { 
                    mPreviewZoomOnly = false;
                    mTouchMode = TOUCH_INIT_MODE;
                    mDeferTouchProcess = (!inFullScreenMode()
                            && mForwardTouchEvents) ? hitFocusedPlugin(
                            contentX, contentY) : false;
                    mWebViewCore.sendMessage(
                            EventHub.UPDATE_FRAME_CACHE_IF_LOADING);
                    if (mLogEvent && eventTime - mLastTouchUpTime < 1000) {
                        EventLog.writeEvent(EventLogTags.BROWSER_DOUBLE_TAP_DURATION,
                                (eventTime - mLastTouchUpTime), eventTime);
                    }
                }
                if (mTouchMode == TOUCH_INIT_MODE
                        || mTouchMode == TOUCH_DOUBLE_TAP_MODE) {
                    mPrivateHandler.sendEmptyMessageDelayed(
                            SWITCH_TO_SHORTPRESS, TAP_TIMEOUT);
                    mPrivateHandler.sendEmptyMessageDelayed(
                            SWITCH_TO_LONGPRESS, LONG_PRESS_TIMEOUT);
                    if (inFullScreenMode() || mDeferTouchProcess) {
                        mPreventDefault = PREVENT_DEFAULT_YES;
                    } else if (mForwardTouchEvents) {
                        mPreventDefault = PREVENT_DEFAULT_MAYBE_YES;
                    } else {
                        mPreventDefault = PREVENT_DEFAULT_NO;
                    }
                    if (shouldForwardTouchEvent()) {
                        TouchEventData ted = new TouchEventData();
                        ted.mAction = action;
                        ted.mX = contentX;
                        ted.mY = contentY;
                        ted.mMetaState = ev.getMetaState();
                        ted.mReprocess = mDeferTouchProcess;
                        if (mDeferTouchProcess) {
                            mLastTouchX = x;
                            mLastTouchY = y;
                            ted.mViewX = x;
                            ted.mViewY = y;
                            mWebViewCore.sendMessage(EventHub.TOUCH_EVENT, ted);
                            break;
                        }
                        mWebViewCore.sendMessage(EventHub.TOUCH_EVENT, ted);
                        if (!inFullScreenMode()) {
                            mPrivateHandler.sendMessageDelayed(mPrivateHandler
                                    .obtainMessage(PREVENT_DEFAULT_TIMEOUT,
                                            action, 0), TAP_TIMEOUT);
                        }
                    }
                }
                startTouch(x, y, eventTime);
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                boolean firstMove = false;
                if (!mConfirmMove && (deltaX * deltaX + deltaY * deltaY)
                        >= mTouchSlopSquare) {
                    mPrivateHandler.removeMessages(SWITCH_TO_SHORTPRESS);
                    mPrivateHandler.removeMessages(SWITCH_TO_LONGPRESS);
                    mConfirmMove = true;
                    firstMove = true;
                    if (mTouchMode == TOUCH_DOUBLE_TAP_MODE) {
                        mTouchMode = TOUCH_INIT_MODE;
                    }
                }
                if (shouldForwardTouchEvent() && mConfirmMove && (firstMove
                        || eventTime - mLastSentTouchTime > mCurrentTouchInterval)) {
                    mLastSentTouchTime = eventTime;
                    TouchEventData ted = new TouchEventData();
                    ted.mAction = action;
                    ted.mX = contentX;
                    ted.mY = contentY;
                    ted.mMetaState = ev.getMetaState();
                    ted.mReprocess = mDeferTouchProcess;
                    if (mDeferTouchProcess) {
                        ted.mViewX = x;
                        ted.mViewY = y;
                        mWebViewCore.sendMessage(EventHub.TOUCH_EVENT, ted);
                        break;
                    }
                    mWebViewCore.sendMessage(EventHub.TOUCH_EVENT, ted);
                    if (firstMove && !inFullScreenMode()) {
                        mPrivateHandler.sendMessageDelayed(mPrivateHandler
                                .obtainMessage(PREVENT_DEFAULT_TIMEOUT,
                                        action, 0), TAP_TIMEOUT);
                    }
                }
                if (mTouchMode == TOUCH_DONE_MODE
                        || mPreventDefault == PREVENT_DEFAULT_YES) {
                    break;
                }
                if (mVelocityTracker == null) {
                    Log.e(LOGTAG, "Got null mVelocityTracker when "
                            + "mPreventDefault = " + mPreventDefault
                            + " mDeferTouchProcess = " + mDeferTouchProcess
                            + " mTouchMode = " + mTouchMode);
                }
                mVelocityTracker.addMovement(ev);
                if (mTouchMode != TOUCH_DRAG_MODE) {
                    if (mTouchMode == TOUCH_SELECT_MODE) {
                        mSelectX = mScrollX + (int) x;
                        mSelectY = mScrollY + (int) y;
                        if (DebugFlags.WEB_VIEW) {
                            Log.v(LOGTAG, "xtend=" + mSelectX + "," + mSelectY);
                        }
                        nativeMoveSelection(contentX, contentY, true);
                        invalidate();
                        break;
                    }
                    if (!mConfirmMove) {
                        break;
                    }
                    if (mPreventDefault == PREVENT_DEFAULT_MAYBE_YES
                            || mPreventDefault == PREVENT_DEFAULT_NO_FROM_TOUCH_DOWN) {
                        mLastTouchTime = eventTime;
                        break;
                    }
                    int ax = Math.abs(deltaX);
                    int ay = Math.abs(deltaY);
                    if (ax > MAX_SLOPE_FOR_DIAG * ay) {
                        mSnapScrollMode = SNAP_X;
                        mSnapPositive = deltaX > 0;
                    } else if (ay > MAX_SLOPE_FOR_DIAG * ax) {
                        mSnapScrollMode = SNAP_Y;
                        mSnapPositive = deltaY > 0;
                    }
                    mTouchMode = TOUCH_DRAG_MODE;
                    mLastTouchX = x;
                    mLastTouchY = y;
                    fDeltaX = 0.0f;
                    fDeltaY = 0.0f;
                    deltaX = 0;
                    deltaY = 0;
                    startDrag();
                }
                if (mDragTrackerHandler != null) {
                    mDragTrackerHandler.dragTo(x, y);
                }
                int newScrollX = pinLocX(mScrollX + deltaX);
                int newDeltaX = newScrollX - mScrollX;
                if (deltaX != newDeltaX) {
                    deltaX = newDeltaX;
                    fDeltaX = (float) newDeltaX;
                }
                int newScrollY = pinLocY(mScrollY + deltaY);
                int newDeltaY = newScrollY - mScrollY;
                if (deltaY != newDeltaY) {
                    deltaY = newDeltaY;
                    fDeltaY = (float) newDeltaY;
                }
                boolean done = false;
                boolean keepScrollBarsVisible = false;
                if (Math.abs(fDeltaX) < 1.0f && Math.abs(fDeltaY) < 1.0f) {
                    mLastTouchX = x;
                    mLastTouchY = y;
                    keepScrollBarsVisible = done = true;
                } else {
                    if (mSnapScrollMode == SNAP_X || mSnapScrollMode == SNAP_Y) {
                        int ax = Math.abs(deltaX);
                        int ay = Math.abs(deltaY);
                        if (mSnapScrollMode == SNAP_X) {
                            if (ay > MAX_SLOPE_FOR_DIAG * ax
                                    && ay > MIN_BREAK_SNAP_CROSS_DISTANCE) {
                                mSnapScrollMode = SNAP_NONE;
                            }
                            if (ax > MAX_SLOPE_FOR_DIAG * ay &&
                                    (mSnapPositive
                                    ? deltaX < -mMinLockSnapReverseDistance
                                    : deltaX > mMinLockSnapReverseDistance)) {
                                mSnapScrollMode |= SNAP_LOCK;
                            }
                        } else {
                            if (ax > MAX_SLOPE_FOR_DIAG * ay
                                    && ax > MIN_BREAK_SNAP_CROSS_DISTANCE) {
                                mSnapScrollMode = SNAP_NONE;
                            }
                            if (ay > MAX_SLOPE_FOR_DIAG * ax &&
                                    (mSnapPositive
                                    ? deltaY < -mMinLockSnapReverseDistance
                                    : deltaY > mMinLockSnapReverseDistance)) {
                                mSnapScrollMode |= SNAP_LOCK;
                            }
                        }
                    }
                    if (mSnapScrollMode != SNAP_NONE) {
                        if ((mSnapScrollMode & SNAP_X) == SNAP_X) {
                            deltaY = 0;
                        } else {
                            deltaX = 0;
                        }
                    }
                    if ((deltaX | deltaY) != 0) {
                        if (deltaX != 0) {
                            mLastTouchX = x;
                        }
                        if (deltaY != 0) {
                            mLastTouchY = y;
                        }
                        mHeldMotionless = MOTIONLESS_FALSE;
                    } else {
                        mLastTouchX = x;
                        mLastTouchY = y;
                        keepScrollBarsVisible = true;
                    }
                    mLastTouchTime = eventTime;
                    mUserScroll = true;
                }
                doDrag(deltaX, deltaY);
                if (keepScrollBarsVisible) {
                    if (mHeldMotionless != MOTIONLESS_TRUE) {
                        mHeldMotionless = MOTIONLESS_TRUE;
                        invalidate();
                    }
                    awakenScrollBars(ViewConfiguration.getScrollDefaultDelay(),
                            false);
                    return !done;
                }
                break;
            }
            case MotionEvent.ACTION_UP: {
                if (shouldForwardTouchEvent()) {
                    TouchEventData ted = new TouchEventData();
                    ted.mAction = action;
                    ted.mX = contentX;
                    ted.mY = contentY;
                    ted.mMetaState = ev.getMetaState();
                    ted.mReprocess = mDeferTouchProcess;
                    if (mDeferTouchProcess) {
                        ted.mViewX = x;
                        ted.mViewY = y;
                    }
                    mWebViewCore.sendMessage(EventHub.TOUCH_EVENT, ted);
                }
                mLastTouchUpTime = eventTime;
                switch (mTouchMode) {
                    case TOUCH_DOUBLE_TAP_MODE: 
                        mPrivateHandler.removeMessages(SWITCH_TO_SHORTPRESS);
                        mPrivateHandler.removeMessages(SWITCH_TO_LONGPRESS);
                        if (inFullScreenMode() || mDeferTouchProcess) {
                            TouchEventData ted = new TouchEventData();
                            ted.mAction = WebViewCore.ACTION_DOUBLETAP;
                            ted.mX = contentX;
                            ted.mY = contentY;
                            ted.mMetaState = ev.getMetaState();
                            ted.mReprocess = mDeferTouchProcess;
                            if (mDeferTouchProcess) {
                                ted.mViewX = x;
                                ted.mViewY = y;
                            }
                            mWebViewCore.sendMessage(EventHub.TOUCH_EVENT, ted);
                        } else if (mPreventDefault != PREVENT_DEFAULT_YES){
                            doDoubleTap();
                            mTouchMode = TOUCH_DONE_MODE;
                        }
                        break;
                    case TOUCH_SELECT_MODE:
                        commitCopy();
                        mTouchSelection = false;
                        break;
                    case TOUCH_INIT_MODE: 
                    case TOUCH_SHORTPRESS_START_MODE:
                    case TOUCH_SHORTPRESS_MODE:
                        mPrivateHandler.removeMessages(SWITCH_TO_SHORTPRESS);
                        mPrivateHandler.removeMessages(SWITCH_TO_LONGPRESS);
                        if (mConfirmMove) {
                            Log.w(LOGTAG, "Miss a drag as we are waiting for" +
                                    " WebCore's response for touch down.");
                            if (mPreventDefault != PREVENT_DEFAULT_YES
                                    && (computeMaxScrollX() > 0
                                            || computeMaxScrollY() > 0)) {
                                cancelWebCoreTouchEvent(contentX, contentY,
                                        true);
                                WebViewCore.reducePriority();
                                WebViewCore.pauseUpdatePicture(mWebViewCore);
                            } else {
                                invalidate();
                                break;
                            }
                        } else {
                            if (mTouchMode == TOUCH_INIT_MODE) {
                                mPrivateHandler.sendEmptyMessageDelayed(
                                        RELEASE_SINGLE_TAP, ViewConfiguration
                                                .getDoubleTapTimeout());
                            } else {
                                doShortPress();
                            }
                            break;
                        }
                    case TOUCH_DRAG_MODE:
                        mPrivateHandler.removeMessages(DRAG_HELD_MOTIONLESS);
                        mPrivateHandler.removeMessages(AWAKEN_SCROLL_BARS);
                        if (eventTime - mLastTouchTime <= MIN_FLING_TIME) {
                            if (mVelocityTracker == null) {
                                Log.e(LOGTAG, "Got null mVelocityTracker when "
                                        + "mPreventDefault = "
                                        + mPreventDefault
                                        + " mDeferTouchProcess = "
                                        + mDeferTouchProcess);
                            }
                            mVelocityTracker.addMovement(ev);
                            mHeldMotionless = MOTIONLESS_IGNORE;
                            doFling();
                            break;
                        }
                        mHeldMotionless = MOTIONLESS_TRUE;
                        invalidate();
                    case TOUCH_DRAG_START_MODE:
                        mLastVelocity = 0;
                        WebViewCore.resumePriority();
                        WebViewCore.resumeUpdatePicture(mWebViewCore);
                        break;
                }
                stopTouch();
                break;
            }
            case MotionEvent.ACTION_CANCEL: {
                if (mTouchMode == TOUCH_DRAG_MODE) {
                    invalidate();
                }
                cancelWebCoreTouchEvent(contentX, contentY, false);
                cancelTouch();
                break;
            }
        }
        return true;
    }
    private void cancelWebCoreTouchEvent(int x, int y, boolean removeEvents) {
        if (shouldForwardTouchEvent()) {
            if (removeEvents) {
                mWebViewCore.removeMessages(EventHub.TOUCH_EVENT);
            }
            TouchEventData ted = new TouchEventData();
            ted.mX = x;
            ted.mY = y;
            ted.mAction = MotionEvent.ACTION_CANCEL;
            mWebViewCore.sendMessage(EventHub.TOUCH_EVENT, ted);
            mPreventDefault = PREVENT_DEFAULT_IGNORE;
        }
    }
    private void startTouch(float x, float y, long eventTime) {
        mLastTouchX = x;
        mLastTouchY = y;
        mLastTouchTime = eventTime;
        mVelocityTracker = VelocityTracker.obtain();
        mSnapScrollMode = SNAP_NONE;
        if (mDragTracker != null) {
            mDragTrackerHandler = new DragTrackerHandler(x, y, mDragTracker);
        }
    }
    private void startDrag() {
        WebViewCore.reducePriority();
        WebViewCore.pauseUpdatePicture(mWebViewCore);
        if (!mDragFromTextInput) {
            nativeHideCursor();
        }
        WebSettings settings = getSettings();
        if (settings.supportZoom()
                && settings.getBuiltInZoomControls()
                && !getZoomButtonsController().isVisible()
                && mMinZoomScale < mMaxZoomScale
                && (mHorizontalScrollBarMode != SCROLLBAR_ALWAYSOFF
                        || mVerticalScrollBarMode != SCROLLBAR_ALWAYSOFF)) {
            mZoomButtonsController.setVisible(true);
            int count = settings.getDoubleTapToastCount();
            if (mInZoomOverview && count > 0) {
                settings.setDoubleTapToastCount(--count);
                Toast.makeText(mContext,
                        com.android.internal.R.string.double_tap_toast,
                        Toast.LENGTH_LONG).show();
            }
        }
    }
    private void doDrag(int deltaX, int deltaY) {
        if ((deltaX | deltaY) != 0) {
            scrollBy(deltaX, deltaY);
        }
        if (!getSettings().getBuiltInZoomControls()) {
            boolean showPlusMinus = mMinZoomScale < mMaxZoomScale;
            if (mZoomControls != null && showPlusMinus) {
                if (mZoomControls.getVisibility() == View.VISIBLE) {
                    mPrivateHandler.removeCallbacks(mZoomControlRunnable);
                } else {
                    mZoomControls.show(showPlusMinus, false);
                }
                mPrivateHandler.postDelayed(mZoomControlRunnable,
                        ZOOM_CONTROLS_TIMEOUT);
            }
        }
    }
    private void stopTouch() {
        if (mDragTrackerHandler != null) {
            mDragTrackerHandler.stopDrag();
        }
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }
    private void cancelTouch() {
        if (mDragTrackerHandler != null) {
            mDragTrackerHandler.stopDrag();
        }
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
        if (mTouchMode == TOUCH_DRAG_MODE) {
            WebViewCore.resumePriority();
            WebViewCore.resumeUpdatePicture(mWebViewCore);
        }
        mPrivateHandler.removeMessages(SWITCH_TO_SHORTPRESS);
        mPrivateHandler.removeMessages(SWITCH_TO_LONGPRESS);
        mPrivateHandler.removeMessages(DRAG_HELD_MOTIONLESS);
        mPrivateHandler.removeMessages(AWAKEN_SCROLL_BARS);
        mHeldMotionless = MOTIONLESS_TRUE;
        mTouchMode = TOUCH_DONE_MODE;
        nativeHideCursor();
    }
    private long mTrackballFirstTime = 0;
    private long mTrackballLastTime = 0;
    private float mTrackballRemainsX = 0.0f;
    private float mTrackballRemainsY = 0.0f;
    private int mTrackballXMove = 0;
    private int mTrackballYMove = 0;
    private boolean mExtendSelection = false;
    private boolean mTouchSelection = false;
    private static final int TRACKBALL_KEY_TIMEOUT = 1000;
    private static final int TRACKBALL_TIMEOUT = 200;
    private static final int TRACKBALL_WAIT = 100;
    private static final int TRACKBALL_SCALE = 400;
    private static final int TRACKBALL_SCROLL_COUNT = 5;
    private static final int TRACKBALL_MOVE_COUNT = 10;
    private static final int TRACKBALL_MULTIPLIER = 3;
    private static final int SELECT_CURSOR_OFFSET = 16;
    private int mSelectX = 0;
    private int mSelectY = 0;
    private boolean mFocusSizeChanged = false;
    private boolean mShiftIsPressed = false;
    private boolean mTrackballDown = false;
    private long mTrackballUpTime = 0;
    private long mLastCursorTime = 0;
    private Rect mLastCursorBounds;
    private boolean mMapTrackballToArrowKeys = true;
    public void setMapTrackballToArrowKeys(boolean setMap) {
        mMapTrackballToArrowKeys = setMap;
    }
    void resetTrackballTime() {
        mTrackballLastTime = 0;
    }
    @Override
    public boolean onTrackballEvent(MotionEvent ev) {
        long time = ev.getEventTime();
        if ((ev.getMetaState() & KeyEvent.META_ALT_ON) != 0) {
            if (ev.getY() > 0) pageDown(true);
            if (ev.getY() < 0) pageUp(true);
            return true;
        }
        boolean shiftPressed = mShiftIsPressed && (mNativeClass == 0
                || !nativeFocusIsPlugin());
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            if (shiftPressed) {
                return true; 
            }
            mTrackballDown = true;
            if (mNativeClass == 0) {
                return false;
            }
            nativeRecordButtons(hasFocus() && hasWindowFocus(), true, true);
            if (time - mLastCursorTime <= TRACKBALL_TIMEOUT
                    && !mLastCursorBounds.equals(nativeGetCursorRingBounds())) {
                nativeSelectBestAt(mLastCursorBounds);
            }
            if (DebugFlags.WEB_VIEW) {
                Log.v(LOGTAG, "onTrackballEvent down ev=" + ev
                        + " time=" + time
                        + " mLastCursorTime=" + mLastCursorTime);
            }
            if (isInTouchMode()) requestFocusFromTouch();
            return false; 
        }
        if (ev.getAction() == MotionEvent.ACTION_UP) {
            mPrivateHandler.removeMessages(LONG_PRESS_CENTER);
            mTrackballDown = false;
            mTrackballUpTime = time;
            if (shiftPressed) {
                if (mExtendSelection) {
                    commitCopy();
                } else {
                    mExtendSelection = true;
                    invalidate(); 
                }
                return true; 
            }
            if (DebugFlags.WEB_VIEW) {
                Log.v(LOGTAG, "onTrackballEvent up ev=" + ev
                        + " time=" + time
                );
            }
            return false; 
        }
        if (mMapTrackballToArrowKeys && mShiftIsPressed == false) {
            if (DebugFlags.WEB_VIEW) Log.v(LOGTAG, "onTrackballEvent gmail quit");
            return false;
        }
        if (mTrackballDown) {
            if (DebugFlags.WEB_VIEW) Log.v(LOGTAG, "onTrackballEvent down quit");
            return true; 
        }
        if (time - mTrackballUpTime < TRACKBALL_TIMEOUT) {
            if (DebugFlags.WEB_VIEW) Log.v(LOGTAG, "onTrackballEvent up timeout quit");
            return true;
        }
        switchOutDrawHistory();
        if (time - mTrackballLastTime > TRACKBALL_TIMEOUT) {
            if (DebugFlags.WEB_VIEW) {
                Log.v(LOGTAG, "onTrackballEvent time="
                        + time + " last=" + mTrackballLastTime);
            }
            mTrackballFirstTime = time;
            mTrackballXMove = mTrackballYMove = 0;
        }
        mTrackballLastTime = time;
        if (DebugFlags.WEB_VIEW) {
            Log.v(LOGTAG, "onTrackballEvent ev=" + ev + " time=" + time);
        }
        mTrackballRemainsX += ev.getX();
        mTrackballRemainsY += ev.getY();
        doTrackball(time);
        return true;
    }
    void moveSelection(float xRate, float yRate) {
        if (mNativeClass == 0)
            return;
        int width = getViewWidth();
        int height = getViewHeight();
        mSelectX += xRate;
        mSelectY += yRate;
        int maxX = width + mScrollX;
        int maxY = height + mScrollY;
        mSelectX = Math.min(maxX, Math.max(mScrollX - SELECT_CURSOR_OFFSET
                , mSelectX));
        mSelectY = Math.min(maxY, Math.max(mScrollY - SELECT_CURSOR_OFFSET
                , mSelectY));
        if (DebugFlags.WEB_VIEW) {
            Log.v(LOGTAG, "moveSelection"
                    + " mSelectX=" + mSelectX
                    + " mSelectY=" + mSelectY
                    + " mScrollX=" + mScrollX
                    + " mScrollY=" + mScrollY
                    + " xRate=" + xRate
                    + " yRate=" + yRate
                    );
        }
        nativeMoveSelection(viewToContentX(mSelectX),
                viewToContentY(mSelectY), mExtendSelection);
        int scrollX = mSelectX < mScrollX ? -SELECT_CURSOR_OFFSET
                : mSelectX > maxX - SELECT_CURSOR_OFFSET ? SELECT_CURSOR_OFFSET
                : 0;
        int scrollY = mSelectY < mScrollY ? -SELECT_CURSOR_OFFSET
                : mSelectY > maxY - SELECT_CURSOR_OFFSET ? SELECT_CURSOR_OFFSET
                : 0;
        pinScrollBy(scrollX, scrollY, true, 0);
        Rect select = new Rect(mSelectX, mSelectY, mSelectX + 1, mSelectY + 1);
        requestRectangleOnScreen(select);
        invalidate();
   }
    private int scaleTrackballX(float xRate, int width) {
        int xMove = (int) (xRate / TRACKBALL_SCALE * width);
        int nextXMove = xMove;
        if (xMove > 0) {
            if (xMove > mTrackballXMove) {
                xMove -= mTrackballXMove;
            }
        } else if (xMove < mTrackballXMove) {
            xMove -= mTrackballXMove;
        }
        mTrackballXMove = nextXMove;
        return xMove;
    }
    private int scaleTrackballY(float yRate, int height) {
        int yMove = (int) (yRate / TRACKBALL_SCALE * height);
        int nextYMove = yMove;
        if (yMove > 0) {
            if (yMove > mTrackballYMove) {
                yMove -= mTrackballYMove;
            }
        } else if (yMove < mTrackballYMove) {
            yMove -= mTrackballYMove;
        }
        mTrackballYMove = nextYMove;
        return yMove;
    }
    private int keyCodeToSoundsEffect(int keyCode) {
        switch(keyCode) {
            case KeyEvent.KEYCODE_DPAD_UP:
                return SoundEffectConstants.NAVIGATION_UP;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                return SoundEffectConstants.NAVIGATION_RIGHT;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                return SoundEffectConstants.NAVIGATION_DOWN;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                return SoundEffectConstants.NAVIGATION_LEFT;
        }
        throw new IllegalArgumentException("keyCode must be one of " +
                "{KEYCODE_DPAD_UP, KEYCODE_DPAD_RIGHT, KEYCODE_DPAD_DOWN, " +
                "KEYCODE_DPAD_LEFT}.");
    }
    private void doTrackball(long time) {
        int elapsed = (int) (mTrackballLastTime - mTrackballFirstTime);
        if (elapsed == 0) {
            elapsed = TRACKBALL_TIMEOUT;
        }
        float xRate = mTrackballRemainsX * 1000 / elapsed;
        float yRate = mTrackballRemainsY * 1000 / elapsed;
        int viewWidth = getViewWidth();
        int viewHeight = getViewHeight();
        if (mShiftIsPressed && (mNativeClass == 0 || !nativeFocusIsPlugin())) {
            moveSelection(scaleTrackballX(xRate, viewWidth),
                    scaleTrackballY(yRate, viewHeight));
            mTrackballRemainsX = mTrackballRemainsY = 0;
            return;
        }
        float ax = Math.abs(xRate);
        float ay = Math.abs(yRate);
        float maxA = Math.max(ax, ay);
        if (DebugFlags.WEB_VIEW) {
            Log.v(LOGTAG, "doTrackball elapsed=" + elapsed
                    + " xRate=" + xRate
                    + " yRate=" + yRate
                    + " mTrackballRemainsX=" + mTrackballRemainsX
                    + " mTrackballRemainsY=" + mTrackballRemainsY);
        }
        int width = mContentWidth - viewWidth;
        int height = mContentHeight - viewHeight;
        if (width < 0) width = 0;
        if (height < 0) height = 0;
        ax = Math.abs(mTrackballRemainsX * TRACKBALL_MULTIPLIER);
        ay = Math.abs(mTrackballRemainsY * TRACKBALL_MULTIPLIER);
        maxA = Math.max(ax, ay);
        int count = Math.max(0, (int) maxA);
        int oldScrollX = mScrollX;
        int oldScrollY = mScrollY;
        if (count > 0) {
            int selectKeyCode = ax < ay ? mTrackballRemainsY < 0 ?
                    KeyEvent.KEYCODE_DPAD_UP : KeyEvent.KEYCODE_DPAD_DOWN :
                    mTrackballRemainsX < 0 ? KeyEvent.KEYCODE_DPAD_LEFT :
                    KeyEvent.KEYCODE_DPAD_RIGHT;
            count = Math.min(count, TRACKBALL_MOVE_COUNT);
            if (DebugFlags.WEB_VIEW) {
                Log.v(LOGTAG, "doTrackball keyCode=" + selectKeyCode
                        + " count=" + count
                        + " mTrackballRemainsX=" + mTrackballRemainsX
                        + " mTrackballRemainsY=" + mTrackballRemainsY);
            }
            if (mNativeClass != 0 && nativeFocusIsPlugin()) {
                for (int i = 0; i < count; i++) {
                    letPluginHandleNavKey(selectKeyCode, time, true);
                }
                letPluginHandleNavKey(selectKeyCode, time, false);
            } else if (navHandledKey(selectKeyCode, count, false, time)) {
                playSoundEffect(keyCodeToSoundsEffect(selectKeyCode));
            }
            mTrackballRemainsX = mTrackballRemainsY = 0;
        }
        if (count >= TRACKBALL_SCROLL_COUNT) {
            int xMove = scaleTrackballX(xRate, width);
            int yMove = scaleTrackballY(yRate, height);
            if (DebugFlags.WEB_VIEW) {
                Log.v(LOGTAG, "doTrackball pinScrollBy"
                        + " count=" + count
                        + " xMove=" + xMove + " yMove=" + yMove
                        + " mScrollX-oldScrollX=" + (mScrollX-oldScrollX)
                        + " mScrollY-oldScrollY=" + (mScrollY-oldScrollY)
                        );
            }
            if (Math.abs(mScrollX - oldScrollX) > Math.abs(xMove)) {
                xMove = 0;
            }
            if (Math.abs(mScrollY - oldScrollY) > Math.abs(yMove)) {
                yMove = 0;
            }
            if (xMove != 0 || yMove != 0) {
                pinScrollBy(xMove, yMove, true, 0);
            }
            mUserScroll = true;
        }
    }
    private int computeMaxScrollX() {
        return Math.max(computeHorizontalScrollRange() - getViewWidth(), 0);
    }
    private int computeMaxScrollY() {
        return Math.max(computeVerticalScrollRange() + getTitleHeight()
                - getViewHeightWithTitle(), 0);
    }
    public void flingScroll(int vx, int vy) {
        mScroller.fling(mScrollX, mScrollY, vx, vy, 0, computeMaxScrollX(), 0,
                computeMaxScrollY());
        invalidate();
    }
    private void doFling() {
        if (mVelocityTracker == null) {
            return;
        }
        int maxX = computeMaxScrollX();
        int maxY = computeMaxScrollY();
        mVelocityTracker.computeCurrentVelocity(1000, mMaximumFling);
        int vx = (int) mVelocityTracker.getXVelocity();
        int vy = (int) mVelocityTracker.getYVelocity();
        if (mSnapScrollMode != SNAP_NONE) {
            if ((mSnapScrollMode & SNAP_X) == SNAP_X) {
                vy = 0;
            } else {
                vx = 0;
            }
        }
        if (true ) {
            vx = vx * 3 / 4;
            vy = vy * 3 / 4;
        }
        if ((maxX == 0 && vy == 0) || (maxY == 0 && vx == 0)) {
            WebViewCore.resumePriority();
            WebViewCore.resumeUpdatePicture(mWebViewCore);
            return;
        }
        float currentVelocity = mScroller.getCurrVelocity();
        if (mLastVelocity > 0 && currentVelocity > 0) {
            float deltaR = (float) (Math.abs(Math.atan2(mLastVelY, mLastVelX)
                    - Math.atan2(vy, vx)));
            final float circle = (float) (Math.PI) * 2.0f;
            if (deltaR > circle * 0.9f || deltaR < circle * 0.1f) {
                vx += currentVelocity * mLastVelX / mLastVelocity;
                vy += currentVelocity * mLastVelY / mLastVelocity;
                if (DebugFlags.WEB_VIEW) {
                    Log.v(LOGTAG, "doFling vx= " + vx + " vy=" + vy);
                }
            } else if (DebugFlags.WEB_VIEW) {
                Log.v(LOGTAG, "doFling missed " + deltaR / circle);
            }
        } else if (DebugFlags.WEB_VIEW) {
            Log.v(LOGTAG, "doFling start last=" + mLastVelocity
                    + " current=" + currentVelocity
                    + " vx=" + vx + " vy=" + vy
                    + " maxX=" + maxX + " maxY=" + maxY
                    + " mScrollX=" + mScrollX + " mScrollY=" + mScrollY);
        }
        mLastVelX = vx;
        mLastVelY = vy;
        mLastVelocity = (float) Math.hypot(vx, vy);
        mScroller.fling(mScrollX, mScrollY, -vx, -vy, 0, maxX, 0, maxY);
        final int time = mScroller.getDuration();
        mPrivateHandler.sendEmptyMessageDelayed(RESUME_WEBCORE_PRIORITY, time);
        awakenScrollBars(time);
        invalidate();
    }
    private boolean zoomWithPreview(float scale, boolean updateTextWrapScale) {
        float oldScale = mActualScale;
        mInitialScrollX = mScrollX;
        mInitialScrollY = mScrollY;
        if (Math.abs(scale - mDefaultScale) < MINIMUM_SCALE_INCREMENT) {
            scale = mDefaultScale;
        }
        setNewZoomScale(scale, updateTextWrapScale, false);
        if (oldScale != mActualScale) {
            mZoomStart = SystemClock.uptimeMillis();
            mInvInitialZoomScale = 1.0f / oldScale;
            mInvFinalZoomScale = 1.0f / mActualScale;
            mZoomScale = mActualScale;
            WebViewCore.pauseUpdatePicture(mWebViewCore);
            invalidate();
            return true;
        } else {
            return false;
        }
    }
    @Deprecated
    public View getZoomControls() {
        if (!getSettings().supportZoom()) {
            Log.w(LOGTAG, "This WebView doesn't support zoom.");
            return null;
        }
        if (mZoomControls == null) {
            mZoomControls = createZoomControls();
            mZoomControls.setVisibility(View.VISIBLE);
            mZoomControlRunnable = new Runnable() {
                public void run() {
                    if (!mZoomControls.hasFocus()) {
                        mZoomControls.hide();
                    } else {
                        mPrivateHandler.removeCallbacks(mZoomControlRunnable);
                        mPrivateHandler.postDelayed(mZoomControlRunnable,
                                ZOOM_CONTROLS_TIMEOUT);
                    }
                }
            };
        }
        return mZoomControls;
    }
    private ExtendedZoomControls createZoomControls() {
        ExtendedZoomControls zoomControls = new ExtendedZoomControls(mContext
            , null);
        zoomControls.setOnZoomInClickListener(new OnClickListener() {
            public void onClick(View v) {
                mPrivateHandler.removeCallbacks(mZoomControlRunnable);
                mPrivateHandler.postDelayed(mZoomControlRunnable,
                        ZOOM_CONTROLS_TIMEOUT);
                zoomIn();
            }
        });
        zoomControls.setOnZoomOutClickListener(new OnClickListener() {
            public void onClick(View v) {
                mPrivateHandler.removeCallbacks(mZoomControlRunnable);
                mPrivateHandler.postDelayed(mZoomControlRunnable,
                        ZOOM_CONTROLS_TIMEOUT);
                zoomOut();
            }
        });
        return zoomControls;
    }
    public ZoomButtonsController getZoomButtonsController() {
        if (mZoomButtonsController == null) {
            mZoomButtonsController = new ZoomButtonsController(this);
            mZoomButtonsController.setOnZoomListener(mZoomListener);
            View controls = mZoomButtonsController.getZoomControls();
            ViewGroup.LayoutParams params = controls.getLayoutParams();
            if (params instanceof FrameLayout.LayoutParams) {
                FrameLayout.LayoutParams frameParams = (FrameLayout.LayoutParams) params;
                frameParams.gravity = Gravity.RIGHT;
            }
        }
        return mZoomButtonsController;
    }
    public boolean zoomIn() {
        switchOutDrawHistory();
        mInZoomOverview = false;
        mZoomCenterX = getViewWidth() * .5f;
        mZoomCenterY = getViewHeight() * .5f;
        mAnchorX = viewToContentX((int) mZoomCenterX + mScrollX);
        mAnchorY = viewToContentY((int) mZoomCenterY + mScrollY);
        return zoomWithPreview(mActualScale * 1.25f, true);
    }
    public boolean zoomOut() {
        switchOutDrawHistory();
        mZoomCenterX = getViewWidth() * .5f;
        mZoomCenterY = getViewHeight() * .5f;
        mAnchorX = viewToContentX((int) mZoomCenterX + mScrollX);
        mAnchorY = viewToContentY((int) mZoomCenterY + mScrollY);
        return zoomWithPreview(mActualScale * 0.8f, true);
    }
    private void updateSelection() {
        if (mNativeClass == 0) {
            return;
        }
        int contentX = viewToContentX((int) mLastTouchX + mScrollX);
        int contentY = viewToContentY((int) mLastTouchY + mScrollY);
        Rect rect = new Rect(contentX - mNavSlop, contentY - mNavSlop,
                contentX + mNavSlop, contentY + mNavSlop);
        nativeSelectBestAt(rect);
    }
     void scrollFocusedTextInput(float xPercent, int y) {
        if (!inEditingMode() || mWebViewCore == null) {
            return;
        }
        mWebViewCore.sendMessage(EventHub.SCROLL_TEXT_INPUT,
                viewToContentDimension(y),
                new Float(xPercent));
    }
     void initiateTextFieldDrag(float x, float y, long eventTime) {
        if (!inEditingMode()) {
            return;
        }
        mLastTouchX = x + (float) (mWebTextView.getLeft() - mScrollX);
        mLastTouchY = y + (float) (mWebTextView.getTop() - mScrollY);
        mLastTouchTime = eventTime;
        if (!mScroller.isFinished()) {
            abortAnimation();
            mPrivateHandler.removeMessages(RESUME_WEBCORE_PRIORITY);
        }
        mSnapScrollMode = SNAP_NONE;
        mVelocityTracker = VelocityTracker.obtain();
        mTouchMode = TOUCH_DRAG_START_MODE;
    }
     boolean textFieldDrag(MotionEvent event) {
        if (!inEditingMode()) {
            return false;
        }
        mDragFromTextInput = true;
        event.offsetLocation((float) (mWebTextView.getLeft() - mScrollX),
                (float) (mWebTextView.getTop() - mScrollY));
        boolean result = onTouchEvent(event);
        mDragFromTextInput = false;
        return result;
    }
     void touchUpOnTextField(MotionEvent event) {
        if (!inEditingMode()) {
            return;
        }
        int x = viewToContentX((int) event.getX() + mWebTextView.getLeft());
        int y = viewToContentY((int) event.getY() + mWebTextView.getTop());
        nativeMotionUp(x, y, mNavSlop);
    }
     void centerKeyPressOnTextField() {
        mWebViewCore.sendMessage(EventHub.CLICK, nativeCursorFramePointer(),
                    nativeCursorNodePointer());
    }
    private void doShortPress() {
        if (mNativeClass == 0) {
            return;
        }
        if (mPreventDefault == PREVENT_DEFAULT_YES) {
            return;
        }
        mTouchMode = TOUCH_DONE_MODE;
        switchOutDrawHistory();
        int contentX = viewToContentX((int) mLastTouchX + mScrollX);
        int contentY = viewToContentY((int) mLastTouchY + mScrollY);
        if (nativePointInNavCache(contentX, contentY, mNavSlop)) {
            WebViewCore.MotionUpData motionUpData = new WebViewCore
                    .MotionUpData();
            motionUpData.mFrame = nativeCacheHitFramePointer();
            motionUpData.mNode = nativeCacheHitNodePointer();
            motionUpData.mBounds = nativeCacheHitNodeBounds();
            motionUpData.mX = contentX;
            motionUpData.mY = contentY;
            mWebViewCore.sendMessageAtFrontOfQueue(EventHub.VALID_NODE_BOUNDS,
                    motionUpData);
        } else {
            doMotionUp(contentX, contentY);
        }
    }
    private void doMotionUp(int contentX, int contentY) {
        if (mLogEvent && nativeMotionUp(contentX, contentY, mNavSlop)) {
            EventLog.writeEvent(EventLogTags.BROWSER_SNAP_CENTER);
        }
        if (nativeHasCursorNode() && !nativeCursorIsTextInput()) {
            playSoundEffect(SoundEffectConstants.CLICK);
        }
    }
    private boolean isPluginFitOnScreen(ViewManager.ChildView view) {
        int viewWidth = getViewWidth();
        int viewHeight = getViewHeightWithTitle();
        float scale = Math.min((float) viewWidth / view.width,
                (float) viewHeight / view.height);
        if (scale < mMinZoomScale) {
            scale = mMinZoomScale;
        } else if (scale > mMaxZoomScale) {
            scale = mMaxZoomScale;
        }
        if (Math.abs(scale - mActualScale) < MINIMUM_SCALE_INCREMENT) {
            if (contentToViewX(view.x) >= mScrollX
                    && contentToViewX(view.x + view.width) <= mScrollX
                            + viewWidth
                    && contentToViewY(view.y) >= mScrollY
                    && contentToViewY(view.y + view.height) <= mScrollY
                            + viewHeight) {
                return true;
            }
        }
        return false;
    }
    private void centerFitRect(int docX, int docY, int docWidth, int docHeight) {
        int viewWidth = getViewWidth();
        int viewHeight = getViewHeightWithTitle();
        float scale = Math.min((float) viewWidth / docWidth, (float) viewHeight
                / docHeight);
        if (scale < mMinZoomScale) {
            scale = mMinZoomScale;
        } else if (scale > mMaxZoomScale) {
            scale = mMaxZoomScale;
        }
        if (Math.abs(scale - mActualScale) < MINIMUM_SCALE_INCREMENT) {
            pinScrollTo(contentToViewX(docX + docWidth / 2) - viewWidth / 2,
                    contentToViewY(docY + docHeight / 2) - viewHeight / 2,
                    true, 0);
        } else {
            float oldScreenX = docX * mActualScale - mScrollX;
            float rectViewX = docX * scale;
            float rectViewWidth = docWidth * scale;
            float newMaxWidth = mContentWidth * scale;
            float newScreenX = (viewWidth - rectViewWidth) / 2;
            if (newScreenX > rectViewX) {
                newScreenX = rectViewX;
            } else if (newScreenX > (newMaxWidth - rectViewX - rectViewWidth)) {
                newScreenX = viewWidth - (newMaxWidth - rectViewX);
            }
            mZoomCenterX = (oldScreenX * scale - newScreenX * mActualScale)
                    / (scale - mActualScale);
            float oldScreenY = docY * mActualScale + getTitleHeight()
                    - mScrollY;
            float rectViewY = docY * scale + getTitleHeight();
            float rectViewHeight = docHeight * scale;
            float newMaxHeight = mContentHeight * scale + getTitleHeight();
            float newScreenY = (viewHeight - rectViewHeight) / 2;
            if (newScreenY > rectViewY) {
                newScreenY = rectViewY;
            } else if (newScreenY > (newMaxHeight - rectViewY - rectViewHeight)) {
                newScreenY = viewHeight - (newMaxHeight - rectViewY);
            }
            mZoomCenterY = (oldScreenY * scale - newScreenY * mActualScale)
                    / (scale - mActualScale);
            zoomWithPreview(scale, false);
        }
    }
    void dismissZoomControl() {
        if (mWebViewCore == null) {
            if (mZoomButtonsController != null) {
                mZoomButtonsController.setVisible(false);
            }
            if (mZoomControls != null) {
                mZoomControls.hide();
            }
            return;
        }
        WebSettings settings = getSettings();
        if (settings.getBuiltInZoomControls()) {
            if (mZoomButtonsController != null) {
                mZoomButtonsController.setVisible(false);
            }
        } else {
            if (mZoomControlRunnable != null) {
                mPrivateHandler.removeCallbacks(mZoomControlRunnable);
            }
            if (mZoomControls != null) {
                mZoomControls.hide();
            }
        }
    }
    private void doDoubleTap() {
        if (mWebViewCore.getSettings().getUseWideViewPort() == false) {
            return;
        }
        mZoomCenterX = mLastTouchX;
        mZoomCenterY = mLastTouchY;
        mAnchorX = viewToContentX((int) mZoomCenterX + mScrollX);
        mAnchorY = viewToContentY((int) mZoomCenterY + mScrollY);
        WebSettings settings = getSettings();
        settings.setDoubleTapToastCount(0);
        dismissZoomControl();
        ViewManager.ChildView plugin = mViewManager.hitTest(mAnchorX, mAnchorY);
        if (plugin != null) {
            if (isPluginFitOnScreen(plugin)) {
                mInZoomOverview = true;
                if (mScrollY < getTitleHeight()) mScrollY = 0;
                zoomWithPreview((float) getViewWidth() / mZoomOverviewWidth,
                        true);
            } else {
                mInZoomOverview = false;
                centerFitRect(plugin.x, plugin.y, plugin.width, plugin.height);
            }
            return;
        }
        boolean zoomToDefault = false;
        if ((settings.getLayoutAlgorithm() == WebSettings.LayoutAlgorithm.NARROW_COLUMNS)
                && (Math.abs(mActualScale - mTextWrapScale) >= MINIMUM_SCALE_INCREMENT)) {
            setNewZoomScale(mActualScale, true, true);
            float overviewScale = (float) getViewWidth() / mZoomOverviewWidth;
            if (Math.abs(mActualScale - overviewScale) < MINIMUM_SCALE_INCREMENT) {
                mInZoomOverview = true;
            }
        } else if (!mInZoomOverview) {
            float newScale = (float) getViewWidth() / mZoomOverviewWidth;
            if (Math.abs(mActualScale - newScale) >= MINIMUM_SCALE_INCREMENT) {
                mInZoomOverview = true;
                if (mScrollY < getTitleHeight()) mScrollY = 0;
                zoomWithPreview(newScale, true);
            } else if (Math.abs(mActualScale - mDefaultScale) >= MINIMUM_SCALE_INCREMENT) {
                zoomToDefault = true;
            }
        } else {
            zoomToDefault = true;
        }
        if (zoomToDefault) {
            mInZoomOverview = false;
            int left = nativeGetBlockLeftEdge(mAnchorX, mAnchorY, mActualScale);
            if (left != NO_LEFTEDGE) {
                int viewLeft = contentToViewX(left < 5 ? 0 : (left - 5))
                        - mScrollX;
                if (viewLeft > 0) {
                    mZoomCenterX = viewLeft * mDefaultScale
                            / (mDefaultScale - mActualScale);
                } else {
                    scrollBy(viewLeft, 0);
                    mZoomCenterX = 0;
                }
            }
            zoomWithPreview(mDefaultScale, true);
        }
    }
    private void overrideLoading(String url) {
        mCallbackProxy.uiOverrideUrlLoading(url);
    }
    @Override
    public boolean requestFocus(int direction, Rect previouslyFocusedRect) {
        boolean result = false;
        if (inEditingMode()) {
            result = mWebTextView.requestFocus(direction,
                    previouslyFocusedRect);
        } else {
            result = super.requestFocus(direction, previouslyFocusedRect);
            if (mWebViewCore.getSettings().getNeedInitialFocus()) {
                int fakeKeyDirection = 0;
                switch(direction) {
                    case View.FOCUS_UP:
                        fakeKeyDirection = KeyEvent.KEYCODE_DPAD_UP;
                        break;
                    case View.FOCUS_DOWN:
                        fakeKeyDirection = KeyEvent.KEYCODE_DPAD_DOWN;
                        break;
                    case View.FOCUS_LEFT:
                        fakeKeyDirection = KeyEvent.KEYCODE_DPAD_LEFT;
                        break;
                    case View.FOCUS_RIGHT:
                        fakeKeyDirection = KeyEvent.KEYCODE_DPAD_RIGHT;
                        break;
                    default:
                        return result;
                }
                if (mNativeClass != 0 && !nativeHasCursorNode()) {
                    navHandledKey(fakeKeyDirection, 1, true, 0);
                }
            }
        }
        return result;
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int measuredHeight = heightSize;
        int measuredWidth = widthSize;
        int contentHeight = contentToViewDimension(mContentHeight);
        int contentWidth = contentToViewDimension(mContentWidth);
        if (heightMode != MeasureSpec.EXACTLY) {
            mHeightCanMeasure = true;
            measuredHeight = contentHeight;
            if (heightMode == MeasureSpec.AT_MOST) {
                if (measuredHeight > heightSize) {
                    measuredHeight = heightSize;
                    mHeightCanMeasure = false;
                }
            }
        } else {
            mHeightCanMeasure = false;
        }
        if (mNativeClass != 0) {
            nativeSetHeightCanMeasure(mHeightCanMeasure);
        }
        if (widthMode == MeasureSpec.UNSPECIFIED) {
            mWidthCanMeasure = true;
            measuredWidth = contentWidth;
        } else {
            mWidthCanMeasure = false;
        }
        synchronized (this) {
            setMeasuredDimension(measuredWidth, measuredHeight);
        }
    }
    @Override
    public boolean requestChildRectangleOnScreen(View child,
                                                 Rect rect,
                                                 boolean immediate) {
        rect.offset(child.getLeft() - child.getScrollX(),
                child.getTop() - child.getScrollY());
        Rect content = new Rect(viewToContentX(mScrollX),
                viewToContentY(mScrollY),
                viewToContentX(mScrollX + getWidth()
                - getVerticalScrollbarWidth()),
                viewToContentY(mScrollY + getViewHeightWithTitle()));
        content = nativeSubtractLayers(content);
        int screenTop = contentToViewY(content.top);
        int screenBottom = contentToViewY(content.bottom);
        int height = screenBottom - screenTop;
        int scrollYDelta = 0;
        if (rect.bottom > screenBottom) {
            int oneThirdOfScreenHeight = height / 3;
            if (rect.height() > 2 * oneThirdOfScreenHeight) {
                scrollYDelta = rect.top - screenTop;
            } else {
                scrollYDelta = rect.top - (screenTop + oneThirdOfScreenHeight);
            }
        } else if (rect.top < screenTop) {
            scrollYDelta = rect.top - screenTop;
        }
        int screenLeft = contentToViewX(content.left);
        int screenRight = contentToViewX(content.right);
        int width = screenRight - screenLeft;
        int scrollXDelta = 0;
        if (rect.right > screenRight && rect.left > screenLeft) {
            if (rect.width() > width) {
                scrollXDelta += (rect.left - screenLeft);
            } else {
                scrollXDelta += (rect.right - screenRight);
            }
        } else if (rect.left < screenLeft) {
            scrollXDelta -= (screenLeft - rect.left);
        }
        if ((scrollYDelta | scrollXDelta) != 0) {
            return pinScrollBy(scrollXDelta, scrollYDelta, !immediate, 0);
        }
        return false;
    }
     void replaceTextfieldText(int oldStart, int oldEnd,
            String replace, int newStart, int newEnd) {
        WebViewCore.ReplaceTextData arg = new WebViewCore.ReplaceTextData();
        arg.mReplace = replace;
        arg.mNewStart = newStart;
        arg.mNewEnd = newEnd;
        mTextGeneration++;
        arg.mTextGeneration = mTextGeneration;
        mWebViewCore.sendMessage(EventHub.REPLACE_TEXT, oldStart, oldEnd, arg);
    }
     void passToJavaScript(String currentText, KeyEvent event) {
        WebViewCore.JSKeyData arg = new WebViewCore.JSKeyData();
        arg.mEvent = event;
        arg.mCurrentText = currentText;
        mTextGeneration++;
        mWebViewCore.sendMessage(EventHub.PASS_TO_JS, mTextGeneration, 0, arg);
        mWebViewCore.removeMessages(EventHub.SAVE_DOCUMENT_STATE);
        mWebViewCore.sendMessageDelayed(EventHub.SAVE_DOCUMENT_STATE,
                cursorData(), 1000);
    }
     synchronized WebViewCore getWebViewCore() {
        return mWebViewCore;
    }
    class PrivateHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if (DebugFlags.WEB_VIEW && msg.what != INVAL_RECT_MSG_ID) {
                if (msg.what >= FIRST_PRIVATE_MSG_ID
                        && msg.what <= LAST_PRIVATE_MSG_ID) {
                    Log.v(LOGTAG, HandlerPrivateDebugString[msg.what
                            - FIRST_PRIVATE_MSG_ID]);
                } else if (msg.what >= FIRST_PACKAGE_MSG_ID
                        && msg.what <= LAST_PACKAGE_MSG_ID) {
                    Log.v(LOGTAG, HandlerPackageDebugString[msg.what
                            - FIRST_PACKAGE_MSG_ID]);
                } else {
                    Log.v(LOGTAG, Integer.toString(msg.what));
                }
            }
            if (mWebViewCore == null) {
                return;
            }
            switch (msg.what) {
                case REMEMBER_PASSWORD: {
                    mDatabase.setUsernamePassword(
                            msg.getData().getString("host"),
                            msg.getData().getString("username"),
                            msg.getData().getString("password"));
                    ((Message) msg.obj).sendToTarget();
                    break;
                }
                case NEVER_REMEMBER_PASSWORD: {
                    mDatabase.setUsernamePassword(
                            msg.getData().getString("host"), null, null);
                    ((Message) msg.obj).sendToTarget();
                    break;
                }
                case PREVENT_DEFAULT_TIMEOUT: {
                    if ((msg.arg1 == MotionEvent.ACTION_DOWN
                            && mPreventDefault == PREVENT_DEFAULT_MAYBE_YES)
                            || (msg.arg1 == MotionEvent.ACTION_MOVE
                            && mPreventDefault == PREVENT_DEFAULT_NO_FROM_TOUCH_DOWN)) {
                        cancelWebCoreTouchEvent(
                                viewToContentX((int) mLastTouchX + mScrollX),
                                viewToContentY((int) mLastTouchY + mScrollY),
                                true);
                    }
                    break;
                }
                case SWITCH_TO_SHORTPRESS: {
                    if (mTouchMode == TOUCH_INIT_MODE) {
                        if (mPreventDefault != PREVENT_DEFAULT_YES) {
                            mTouchMode = TOUCH_SHORTPRESS_START_MODE;
                            updateSelection();
                        } else {
                            mTouchMode = TOUCH_SHORTPRESS_MODE;
                        }
                    } else if (mTouchMode == TOUCH_DOUBLE_TAP_MODE) {
                        mTouchMode = TOUCH_DONE_MODE;
                    }
                    break;
                }
                case SWITCH_TO_LONGPRESS: {
                    if (inFullScreenMode() || mDeferTouchProcess) {
                        TouchEventData ted = new TouchEventData();
                        ted.mAction = WebViewCore.ACTION_LONGPRESS;
                        ted.mX = viewToContentX((int) mLastTouchX + mScrollX);
                        ted.mY = viewToContentY((int) mLastTouchY + mScrollY);
                        ted.mMetaState = 0;
                        ted.mReprocess = mDeferTouchProcess;
                        if (mDeferTouchProcess) {
                            ted.mViewX = mLastTouchX;
                            ted.mViewY = mLastTouchY;
                        }
                        mWebViewCore.sendMessage(EventHub.TOUCH_EVENT, ted);
                    } else if (mPreventDefault != PREVENT_DEFAULT_YES) {
                        mTouchMode = TOUCH_DONE_MODE;
                        performLongClick();
                        rebuildWebTextView();
                    }
                    break;
                }
                case RELEASE_SINGLE_TAP: {
                    doShortPress();
                    break;
                }
                case SCROLL_BY_MSG_ID:
                    setContentScrollBy(msg.arg1, msg.arg2, (Boolean) msg.obj);
                    break;
                case SYNC_SCROLL_TO_MSG_ID:
                    if (mUserScroll) {
                        mUserScroll = false;
                        break;
                    }
                case SCROLL_TO_MSG_ID:
                    if (setContentScrollTo(msg.arg1, msg.arg2)) {
                        mUserScroll = false;
                        mWebViewCore.sendMessage(EventHub.SYNC_SCROLL,
                                msg.arg1, msg.arg2);
                    }
                    break;
                case SPAWN_SCROLL_TO_MSG_ID:
                    spawnContentScrollTo(msg.arg1, msg.arg2);
                    break;
                case UPDATE_ZOOM_RANGE: {
                    WebViewCore.RestoreState restoreState
                            = (WebViewCore.RestoreState) msg.obj;
                    updateZoomRange(restoreState, getViewWidth(),
                            restoreState.mScrollX, false);
                    break;
                }
                case NEW_PICTURE_MSG_ID: {
                    if (mDelayedDeleteRootLayer) {
                        mDelayedDeleteRootLayer = false;
                        nativeSetRootLayer(0);
                    }
                    WebSettings settings = mWebViewCore.getSettings();
                    final int viewWidth = getViewWidth();
                    final WebViewCore.DrawData draw =
                            (WebViewCore.DrawData) msg.obj;
                    final Point viewSize = draw.mViewPoint;
                    boolean useWideViewport = settings.getUseWideViewPort();
                    WebViewCore.RestoreState restoreState = draw.mRestoreState;
                    boolean hasRestoreState = restoreState != null;
                    if (hasRestoreState) {
                        updateZoomRange(restoreState, viewSize.x,
                                draw.mMinPrefWidth, true);
                        if (!mDrawHistory) {
                            mInZoomOverview = false;
                            if (mInitialScaleInPercent > 0) {
                                setNewZoomScale(mInitialScaleInPercent / 100.0f,
                                    mInitialScaleInPercent != mTextWrapScale * 100,
                                    false);
                            } else if (restoreState.mViewScale > 0) {
                                mTextWrapScale = restoreState.mTextWrapScale;
                                setNewZoomScale(restoreState.mViewScale, false,
                                    false);
                            } else {
                                mInZoomOverview = useWideViewport
                                    && settings.getLoadWithOverviewMode();
                                float scale;
                                if (mInZoomOverview) {
                                    scale = (float) viewWidth
                                        / DEFAULT_VIEWPORT_WIDTH;
                                } else {
                                    scale = restoreState.mTextWrapScale;
                                }
                                setNewZoomScale(scale, Math.abs(scale
                                    - mTextWrapScale) >= MINIMUM_SCALE_INCREMENT,
                                    false);
                            }
                            setContentScrollTo(restoreState.mScrollX,
                                restoreState.mScrollY);
                            clearTextEntry(false);
                            if (getSettings().getBuiltInZoomControls()) {
                                updateZoomButtonsEnabled();
                            }
                        }
                    }
                    final boolean updateLayout = viewSize.x == mLastWidthSent
                            && viewSize.y == mLastHeightSent;
                    recordNewContentSize(draw.mWidthHeight.x,
                            draw.mWidthHeight.y
                            + (mFindIsUp ? mFindHeight : 0), updateLayout);
                    if (DebugFlags.WEB_VIEW) {
                        Rect b = draw.mInvalRegion.getBounds();
                        Log.v(LOGTAG, "NEW_PICTURE_MSG_ID {" +
                                b.left+","+b.top+","+b.right+","+b.bottom+"}");
                    }
                    invalidateContentRect(draw.mInvalRegion.getBounds());
                    if (mPictureListener != null) {
                        mPictureListener.onNewPicture(WebView.this, capturePicture());
                    }
                    if (useWideViewport) {
                        mZoomOverviewWidth = Math.min(sMaxViewportWidth, Math
                                .max((int) (viewWidth / mDefaultScale), Math
                                        .max(draw.mMinPrefWidth,
                                                draw.mViewPoint.x)));
                    }
                    if (!mMinZoomScaleFixed) {
                        mMinZoomScale = (float) viewWidth / mZoomOverviewWidth;
                    }
                    if (!mDrawHistory && mInZoomOverview) {
                        if (Math.abs((viewWidth * mInvActualScale)
                                - mZoomOverviewWidth) > 1) {
                            setNewZoomScale((float) viewWidth
                                    / mZoomOverviewWidth, Math.abs(mActualScale
                                    - mTextWrapScale) < MINIMUM_SCALE_INCREMENT,
                                    false);
                        }
                    }
                    if (draw.mFocusSizeChanged && inEditingMode()) {
                        mFocusSizeChanged = true;
                    }
                    if (hasRestoreState) {
                        mViewManager.postReadyToDrawAll();
                    }
                    break;
                }
                case WEBCORE_INITIALIZED_MSG_ID:
                    nativeCreate(msg.arg1);
                    break;
                case UPDATE_TEXTFIELD_TEXT_MSG_ID:
                    if (inEditingMode() &&
                            mWebTextView.isSameTextField(msg.arg1)) {
                        if (msg.getData().getBoolean("password")) {
                            Spannable text = (Spannable) mWebTextView.getText();
                            int start = Selection.getSelectionStart(text);
                            int end = Selection.getSelectionEnd(text);
                            mWebTextView.setInPassword(true);
                            Spannable pword =
                                    (Spannable) mWebTextView.getText();
                            Selection.setSelection(pword, start, end);
                        } else if (msg.arg2 == mTextGeneration) {
                            mWebTextView.setTextAndKeepSelection(
                                    (String) msg.obj);
                        }
                    }
                    break;
                case REQUEST_KEYBOARD_WITH_SELECTION_MSG_ID:
                    displaySoftKeyboard(true);
                    updateTextSelectionFromMessage(msg.arg1, msg.arg2,
                            (WebViewCore.TextSelectionData) msg.obj);
                    break;
                case UPDATE_TEXT_SELECTION_MSG_ID:
                    rebuildWebTextView();
                    updateTextSelectionFromMessage(msg.arg1, msg.arg2,
                            (WebViewCore.TextSelectionData) msg.obj);
                    break;
                case RETURN_LABEL:
                    if (inEditingMode()
                            && mWebTextView.isSameTextField(msg.arg1)) {
                        mWebTextView.setHint((String) msg.obj);
                        InputMethodManager imm
                                = InputMethodManager.peekInstance();
                        if (imm != null && imm.isActive(mWebTextView)) {
                            imm.restartInput(mWebTextView);
                        }
                    }
                    break;
                case MOVE_OUT_OF_PLUGIN:
                    navHandledKey(msg.arg1, 1, false, 0);
                    break;
                case UPDATE_TEXT_ENTRY_MSG_ID:
                    if (inEditingMode() && nativeCursorIsTextInput()) {
                        mWebTextView.bringIntoView();
                        rebuildWebTextView();
                    }
                    break;
                case CLEAR_TEXT_ENTRY:
                    clearTextEntry(false);
                    break;
                case INVAL_RECT_MSG_ID: {
                    Rect r = (Rect)msg.obj;
                    if (r == null) {
                        invalidate();
                    } else {
                        viewInvalidate(r.left, r.top, r.right, r.bottom);
                    }
                    break;
                }
                case IMMEDIATE_REPAINT_MSG_ID: {
                    invalidate();
                    break;
                }
                case SET_ROOT_LAYER_MSG_ID: {
                    if (0 == msg.arg1) {
                        mDelayedDeleteRootLayer = true;
                    } else {
                        mDelayedDeleteRootLayer = false;
                        nativeSetRootLayer(msg.arg1);
                        invalidate();
                    }
                    break;
                }
                case REQUEST_FORM_DATA:
                    AutoCompleteAdapter adapter = (AutoCompleteAdapter) msg.obj;
                    if (mWebTextView.isSameTextField(msg.arg1)) {
                        mWebTextView.setAdapterCustom(adapter);
                    }
                    break;
                case RESUME_WEBCORE_PRIORITY:
                    WebViewCore.resumePriority();
                    WebViewCore.resumeUpdatePicture(mWebViewCore);
                    break;
                case LONG_PRESS_CENTER:
                    mGotCenterDown = false;
                    mTrackballDown = false;
                    performLongClick();
                    break;
                case WEBCORE_NEED_TOUCH_EVENTS:
                    mForwardTouchEvents = (msg.arg1 != 0);
                    break;
                case PREVENT_TOUCH_ID:
                    if (inFullScreenMode()) {
                        break;
                    }
                    if (msg.obj == null) {
                        if (msg.arg1 == MotionEvent.ACTION_DOWN
                                && mPreventDefault == PREVENT_DEFAULT_MAYBE_YES) {
                            mPreventDefault = msg.arg2 == 1 ? PREVENT_DEFAULT_YES
                                    : PREVENT_DEFAULT_NO_FROM_TOUCH_DOWN;
                        } else if (msg.arg1 == MotionEvent.ACTION_MOVE
                                && mPreventDefault == PREVENT_DEFAULT_NO_FROM_TOUCH_DOWN) {
                            mPreventDefault = msg.arg2 == 1 ? PREVENT_DEFAULT_YES
                                    : PREVENT_DEFAULT_NO;
                        }
                    } else if (msg.arg2 == 0) {
                        TouchEventData ted = (TouchEventData) msg.obj;
                        switch (ted.mAction) {
                            case MotionEvent.ACTION_DOWN:
                                mLastDeferTouchX = ted.mViewX;
                                mLastDeferTouchY = ted.mViewY;
                                mDeferTouchMode = TOUCH_INIT_MODE;
                                break;
                            case MotionEvent.ACTION_MOVE: {
                                if (mDeferTouchMode != TOUCH_DRAG_MODE) {
                                    mDeferTouchMode = TOUCH_DRAG_MODE;
                                    mLastDeferTouchX = ted.mViewX;
                                    mLastDeferTouchY = ted.mViewY;
                                    startDrag();
                                }
                                int deltaX = pinLocX((int) (mScrollX
                                        + mLastDeferTouchX - ted.mViewX))
                                        - mScrollX;
                                int deltaY = pinLocY((int) (mScrollY
                                        + mLastDeferTouchY - ted.mViewY))
                                        - mScrollY;
                                doDrag(deltaX, deltaY);
                                if (deltaX != 0) mLastDeferTouchX = ted.mViewX;
                                if (deltaY != 0) mLastDeferTouchY = ted.mViewY;
                                break;
                            }
                            case MotionEvent.ACTION_UP:
                            case MotionEvent.ACTION_CANCEL:
                                if (mDeferTouchMode == TOUCH_DRAG_MODE) {
                                    WebViewCore.resumePriority();
                                    WebViewCore.resumeUpdatePicture(mWebViewCore);
                                }
                                mDeferTouchMode = TOUCH_DONE_MODE;
                                break;
                            case WebViewCore.ACTION_DOUBLETAP:
                                mLastTouchX = ted.mViewX;
                                mLastTouchY = ted.mViewY;
                                doDoubleTap();
                                mDeferTouchMode = TOUCH_DONE_MODE;
                                break;
                            case WebViewCore.ACTION_LONGPRESS:
                                HitTestResult hitTest = getHitTestResult();
                                if (hitTest != null && hitTest.mType
                                        != HitTestResult.UNKNOWN_TYPE) {
                                    performLongClick();
                                    rebuildWebTextView();
                                }
                                mDeferTouchMode = TOUCH_DONE_MODE;
                                break;
                        }
                    }
                    break;
                case REQUEST_KEYBOARD:
                    if (msg.arg1 == 0) {
                        hideSoftKeyboard();
                    } else {
                        displaySoftKeyboard(false);
                    }
                    break;
                case FIND_AGAIN:
                    if (mFindIsUp) {
                        findAll(mLastFind);
                    }
                    break;
                case DRAG_HELD_MOTIONLESS:
                    mHeldMotionless = MOTIONLESS_TRUE;
                    invalidate();
                case AWAKEN_SCROLL_BARS:
                    if (mTouchMode == TOUCH_DRAG_MODE
                            && mHeldMotionless == MOTIONLESS_TRUE) {
                        awakenScrollBars(ViewConfiguration
                                .getScrollDefaultDelay(), false);
                        mPrivateHandler.sendMessageDelayed(mPrivateHandler
                                .obtainMessage(AWAKEN_SCROLL_BARS),
                                ViewConfiguration.getScrollDefaultDelay());
                    }
                    break;
                case DO_MOTION_UP:
                    doMotionUp(msg.arg1, msg.arg2);
                    break;
                case SHOW_FULLSCREEN: {
                    View view = (View) msg.obj;
                    int npp = msg.arg1;
                    if (mFullScreenHolder != null) {
                        Log.w(LOGTAG, "Should not have another full screen.");
                        mFullScreenHolder.dismiss();
                    }
                    mFullScreenHolder = new PluginFullScreenHolder(WebView.this, npp);
                    mFullScreenHolder.setContentView(view);
                    mFullScreenHolder.setCancelable(false);
                    mFullScreenHolder.setCanceledOnTouchOutside(false);
                    mFullScreenHolder.show();
                    break;
                }
                case HIDE_FULLSCREEN:
                    if (inFullScreenMode()) {
                        mFullScreenHolder.dismiss();
                        mFullScreenHolder = null;
                    }
                    break;
                case DOM_FOCUS_CHANGED:
                    if (inEditingMode()) {
                        nativeClearCursor();
                        rebuildWebTextView();
                    }
                    break;
                case SHOW_RECT_MSG_ID: {
                    WebViewCore.ShowRectData data = (WebViewCore.ShowRectData) msg.obj;
                    int x = mScrollX;
                    int left = contentToViewX(data.mLeft);
                    int width = contentToViewDimension(data.mWidth);
                    int maxWidth = contentToViewDimension(data.mContentWidth);
                    int viewWidth = getViewWidth();
                    if (width < viewWidth) {
                        x += left + width / 2 - mScrollX - viewWidth / 2;
                    } else {
                        x += (int) (left + data.mXPercentInDoc * width
                                - mScrollX - data.mXPercentInView * viewWidth);
                    }
                    if (DebugFlags.WEB_VIEW) {
                        Log.v(LOGTAG, "showRectMsg=(left=" + left + ",width=" +
                              width + ",maxWidth=" + maxWidth +
                              ",viewWidth=" + viewWidth + ",x="
                              + x + ",xPercentInDoc=" + data.mXPercentInDoc +
                              ",xPercentInView=" + data.mXPercentInView+ ")");
                    }
                    x = Math.max(0,
                            (Math.min(maxWidth, x + viewWidth)) - viewWidth);
                    int top = contentToViewY(data.mTop);
                    int height = contentToViewDimension(data.mHeight);
                    int maxHeight = contentToViewDimension(data.mContentHeight);
                    int viewHeight = getViewHeight();
                    int y = (int) (top + data.mYPercentInDoc * height -
                                   data.mYPercentInView * viewHeight);
                    if (DebugFlags.WEB_VIEW) {
                        Log.v(LOGTAG, "showRectMsg=(top=" + top + ",height=" +
                              height + ",maxHeight=" + maxHeight +
                              ",viewHeight=" + viewHeight + ",y="
                              + y + ",yPercentInDoc=" + data.mYPercentInDoc +
                              ",yPercentInView=" + data.mYPercentInView+ ")");
                    }
                    y = Math.max(0,
                            (Math.min(maxHeight, y + viewHeight) - viewHeight));
                    y = Math.max(0, y - getVisibleTitleHeight());
                    scrollTo(x, y);
                    }
                    break;
                case CENTER_FIT_RECT:
                    Rect r = (Rect)msg.obj;
                    mInZoomOverview = false;
                    centerFitRect(r.left, r.top, r.width(), r.height());
                    break;
                case SET_SCROLLBAR_MODES:
                    mHorizontalScrollBarMode = msg.arg1;
                    mVerticalScrollBarMode = msg.arg2;
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    }
    private void updateTextSelectionFromMessage(int nodePointer,
            int textGeneration, WebViewCore.TextSelectionData data) {
        if (inEditingMode()
                && mWebTextView.isSameTextField(nodePointer)
                && textGeneration == mTextGeneration) {
            mWebTextView.setSelectionFromWebKit(data.mStart, data.mEnd);
        }
    }
    private class InvokeListBox implements Runnable {
        private boolean     mMultiple;
        private int[]       mSelectedArray;
        private int         mSelection;
        private Container[] mContainers;
        private class Container extends Object {
            final static int OPTGROUP = -1;
            final static int OPTION_DISABLED = 0;
            final static int OPTION_ENABLED = 1;
            String  mString;
            int     mEnabled;
            int     mId;
            public String toString() {
                return mString;
            }
        }
        private class MyArrayListAdapter extends ArrayAdapter<Container> {
            public MyArrayListAdapter(Context context, Container[] objects, boolean multiple) {
                super(context,
                            multiple ? com.android.internal.R.layout.select_dialog_multichoice :
                            com.android.internal.R.layout.select_dialog_singlechoice,
                            objects);
            }
            @Override
            public View getView(int position, View convertView,
                    ViewGroup parent) {
                convertView = super.getView(position, null, parent);
                Container c = item(position);
                if (c != null && Container.OPTION_ENABLED != c.mEnabled) {
                    LinearLayout layout = new LinearLayout(mContext);
                    layout.setOrientation(LinearLayout.VERTICAL);
                    if (position > 0) {
                        View dividerTop = new View(mContext);
                        dividerTop.setBackgroundResource(
                                android.R.drawable.divider_horizontal_bright);
                        layout.addView(dividerTop);
                    }
                    if (Container.OPTGROUP == c.mEnabled) {
                        Assert.assertTrue(
                                convertView instanceof CheckedTextView);
                        ((CheckedTextView) convertView).setCheckMarkDrawable(
                                null);
                    } else {
                        convertView.setEnabled(false);
                    }
                    layout.addView(convertView);
                    if (position < getCount() - 1) {
                        View dividerBottom = new View(mContext);
                        dividerBottom.setBackgroundResource(
                                android.R.drawable.divider_horizontal_bright);
                        layout.addView(dividerBottom);
                    }
                    return layout;
                }
                return convertView;
            }
            @Override
            public boolean hasStableIds() {
                return false;
            }
            private Container item(int position) {
                if (position < 0 || position >= getCount()) {
                    return null;
                }
                return (Container) getItem(position);
            }
            @Override
            public long getItemId(int position) {
                Container item = item(position);
                if (item == null) {
                    return -1;
                }
                return item.mId;
            }
            @Override
            public boolean areAllItemsEnabled() {
                return false;
            }
            @Override
            public boolean isEnabled(int position) {
                Container item = item(position);
                if (item == null) {
                    return false;
                }
                return Container.OPTION_ENABLED == item.mEnabled;
            }
        }
        private InvokeListBox(String[] array, int[] enabled, int[] selected) {
            mMultiple = true;
            mSelectedArray = selected;
            int length = array.length;
            mContainers = new Container[length];
            for (int i = 0; i < length; i++) {
                mContainers[i] = new Container();
                mContainers[i].mString = array[i];
                mContainers[i].mEnabled = enabled[i];
                mContainers[i].mId = i;
            }
        }
        private InvokeListBox(String[] array, int[] enabled, int selection) {
            mSelection = selection;
            mMultiple = false;
            int length = array.length;
            mContainers = new Container[length];
            for (int i = 0; i < length; i++) {
                mContainers[i] = new Container();
                mContainers[i].mString = array[i];
                mContainers[i].mEnabled = enabled[i];
                mContainers[i].mId = i;
            }
        }
        private class SingleDataSetObserver extends DataSetObserver {
            private long        mCheckedId;
            private ListView    mListView;
            private Adapter     mAdapter;
            public SingleDataSetObserver(long id, ListView l, Adapter a) {
                mCheckedId = id;
                mListView = l;
                mAdapter = a;
            }
            public void onChanged() {
                int position = mListView.getCheckedItemPosition();
                long id = mAdapter.getItemId(position);
                if (mCheckedId != id) {
                    mListView.clearChoices();
                    int count = mAdapter.getCount();
                    for (int i = 0; i < count; i++) {
                        if (mAdapter.getItemId(i) == mCheckedId) {
                            mListView.setItemChecked(i, true);
                            break;
                        }
                    }
                }
            }
            public void onInvalidate() {}
        }
        public void run() {
            final ListView listView = (ListView) LayoutInflater.from(mContext)
                    .inflate(com.android.internal.R.layout.select_dialog, null);
            final MyArrayListAdapter adapter = new
                    MyArrayListAdapter(mContext, mContainers, mMultiple);
            AlertDialog.Builder b = new AlertDialog.Builder(mContext)
                    .setView(listView).setCancelable(true)
                    .setInverseBackgroundForced(true);
            if (mMultiple) {
                b.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mWebViewCore.sendMessage(
                                EventHub.LISTBOX_CHOICES,
                                adapter.getCount(), 0,
                                listView.getCheckedItemPositions());
                    }});
                b.setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mWebViewCore.sendMessage(
                                EventHub.SINGLE_LISTBOX_CHOICE, -2, 0);
                }});
            }
            final AlertDialog dialog = b.create();
            listView.setAdapter(adapter);
            listView.setFocusableInTouchMode(true);
            listView.setTextFilterEnabled(!mMultiple);
            if (mMultiple) {
                listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                int length = mSelectedArray.length;
                for (int i = 0; i < length; i++) {
                    listView.setItemChecked(mSelectedArray[i], true);
                }
            } else {
                listView.setOnItemClickListener(new OnItemClickListener() {
                    public void onItemClick(AdapterView parent, View v,
                            int position, long id) {
                        mWebViewCore.sendMessage(
                                EventHub.SINGLE_LISTBOX_CHOICE, (int)id, 0);
                        dialog.dismiss();
                    }
                });
                if (mSelection != -1) {
                    listView.setSelection(mSelection);
                    listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
                    listView.setItemChecked(mSelection, true);
                    DataSetObserver observer = new SingleDataSetObserver(
                            adapter.getItemId(mSelection), listView, adapter);
                    adapter.registerDataSetObserver(observer);
                }
            }
            dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                public void onCancel(DialogInterface dialog) {
                    mWebViewCore.sendMessage(
                                EventHub.SINGLE_LISTBOX_CHOICE, -2, 0);
                }
            });
            dialog.show();
        }
    }
    void requestListBox(String[] array, int[] enabledArray, int[]
            selectedArray) {
        mPrivateHandler.post(
                new InvokeListBox(array, enabledArray, selectedArray));
    }
    private void updateZoomRange(WebViewCore.RestoreState restoreState,
            int viewWidth, int minPrefWidth, boolean updateZoomOverview) {
        if (restoreState.mMinScale == 0) {
            if (restoreState.mMobileSite) {
                if (minPrefWidth > Math.max(0, viewWidth)) {
                    mMinZoomScale = (float) viewWidth / minPrefWidth;
                    mMinZoomScaleFixed = false;
                    if (updateZoomOverview) {
                        WebSettings settings = getSettings();
                        mInZoomOverview = settings.getUseWideViewPort() &&
                                settings.getLoadWithOverviewMode();
                    }
                } else {
                    mMinZoomScale = restoreState.mDefaultScale;
                    mMinZoomScaleFixed = true;
                }
            } else {
                mMinZoomScale = DEFAULT_MIN_ZOOM_SCALE;
                mMinZoomScaleFixed = false;
            }
        } else {
            mMinZoomScale = restoreState.mMinScale;
            mMinZoomScaleFixed = true;
        }
        if (restoreState.mMaxScale == 0) {
            mMaxZoomScale = DEFAULT_MAX_ZOOM_SCALE;
        } else {
            mMaxZoomScale = restoreState.mMaxScale;
        }
    }
    void requestListBox(String[] array, int[] enabledArray, int selection) {
        mPrivateHandler.post(
                new InvokeListBox(array, enabledArray, selection));
    }
    private void sendMoveFocus(int frame, int node) {
        mWebViewCore.sendMessage(EventHub.SET_MOVE_FOCUS,
                new WebViewCore.CursorData(frame, node, 0, 0));
    }
    private void sendMoveMouse(int frame, int node, int x, int y) {
        mWebViewCore.sendMessage(EventHub.SET_MOVE_MOUSE,
                new WebViewCore.CursorData(frame, node, x, y));
    }
    private void sendMoveMouseIfLatest(boolean removeFocus) {
        if (removeFocus) {
            clearTextEntry(true);
        }
        mWebViewCore.sendMessage(EventHub.SET_MOVE_MOUSE_IF_LATEST,
                cursorData());
    }
    private void sendMotionUp(int touchGeneration,
            int frame, int node, int x, int y) {
        WebViewCore.TouchUpData touchUpData = new WebViewCore.TouchUpData();
        touchUpData.mMoveGeneration = touchGeneration;
        touchUpData.mFrame = frame;
        touchUpData.mNode = node;
        touchUpData.mX = x;
        touchUpData.mY = y;
        mWebViewCore.sendMessage(EventHub.TOUCH_UP, touchUpData);
    }
    private int getScaledMaxXScroll() {
        int width;
        if (mHeightCanMeasure == false) {
            width = getViewWidth() / 4;
        } else {
            Rect visRect = new Rect();
            calcOurVisibleRect(visRect);
            width = visRect.width() / 2;
        }
        return viewToContentX(width);
    }
    private int getScaledMaxYScroll() {
        int height;
        if (mHeightCanMeasure == false) {
            height = getViewHeight() / 4;
        } else {
            Rect visRect = new Rect();
            calcOurVisibleRect(visRect);
            height = visRect.height() / 2;
        }
        return Math.round(height * mInvActualScale);
    }
    private void viewInvalidate() {
        invalidate();
    }
    private void letPluginHandleNavKey(int keyCode, long time, boolean down) {
        int keyEventAction;
        int eventHubAction;
        if (down) {
            keyEventAction = KeyEvent.ACTION_DOWN;
            eventHubAction = EventHub.KEY_DOWN;
            playSoundEffect(keyCodeToSoundsEffect(keyCode));
        } else {
            keyEventAction = KeyEvent.ACTION_UP;
            eventHubAction = EventHub.KEY_UP;
        }
        KeyEvent event = new KeyEvent(time, time, keyEventAction, keyCode,
                1, (mShiftIsPressed ? KeyEvent.META_SHIFT_ON : 0)
                | (false ? KeyEvent.META_ALT_ON : 0) 
                | (false ? KeyEvent.META_SYM_ON : 0) 
                , 0, 0, 0);
        mWebViewCore.sendMessage(eventHubAction, event);
    }
    private boolean navHandledKey(int keyCode, int count, boolean noScroll,
            long time) {
        if (mNativeClass == 0) {
            return false;
        }
        mLastCursorTime = time;
        mLastCursorBounds = nativeGetCursorRingBounds();
        boolean keyHandled
                = nativeMoveCursor(keyCode, count, noScroll) == false;
        if (DebugFlags.WEB_VIEW) {
            Log.v(LOGTAG, "navHandledKey mLastCursorBounds=" + mLastCursorBounds
                    + " mLastCursorTime=" + mLastCursorTime
                    + " handled=" + keyHandled);
        }
        if (keyHandled == false || mHeightCanMeasure == false) {
            return keyHandled;
        }
        Rect contentCursorRingBounds = nativeGetCursorRingBounds();
        if (contentCursorRingBounds.isEmpty()) return keyHandled;
        Rect viewCursorRingBounds = contentToViewRect(contentCursorRingBounds);
        Rect visRect = new Rect();
        calcOurVisibleRect(visRect);
        Rect outset = new Rect(visRect);
        int maxXScroll = visRect.width() / 2;
        int maxYScroll = visRect.height() / 2;
        outset.inset(-maxXScroll, -maxYScroll);
        if (Rect.intersects(outset, viewCursorRingBounds) == false) {
            return keyHandled;
        }
        int maxH = Math.min(viewCursorRingBounds.right - visRect.right,
                maxXScroll);
        if (maxH > 0) {
            pinScrollBy(maxH, 0, true, 0);
        } else {
            maxH = Math.max(viewCursorRingBounds.left - visRect.left,
                    -maxXScroll);
            if (maxH < 0) {
                pinScrollBy(maxH, 0, true, 0);
            }
        }
        if (mLastCursorBounds.isEmpty()) return keyHandled;
        if (mLastCursorBounds.equals(contentCursorRingBounds)) {
            return keyHandled;
        }
        if (DebugFlags.WEB_VIEW) {
            Log.v(LOGTAG, "navHandledKey contentCursorRingBounds="
                    + contentCursorRingBounds);
        }
        requestRectangleOnScreen(viewCursorRingBounds);
        mUserScroll = true;
        return keyHandled;
    }
    public void setBackgroundColor(int color) {
        mBackgroundColor = color;
        mWebViewCore.sendMessage(EventHub.SET_BACKGROUND_COLOR, color);
    }
    public void debugDump() {
        nativeDebugDump();
        mWebViewCore.sendMessage(EventHub.DUMP_NAVTREE);
    }
    public void drawPage(Canvas canvas) {
        mWebViewCore.drawContentPicture(canvas, 0, false, false);
    }
    public void setTouchInterval(int interval) {
        mCurrentTouchInterval = interval;
    }
     void updateCachedTextfield(String updatedText) {
        nativeUpdateCachedTextfield(updatedText, mTextGeneration);
    }
    private native int nativeCacheHitFramePointer();
    private native Rect nativeCacheHitNodeBounds();
    private native int nativeCacheHitNodePointer();
     native void nativeClearCursor();
    private native void     nativeCreate(int ptr);
    private native int      nativeCursorFramePointer();
    private native Rect     nativeCursorNodeBounds();
    private native int nativeCursorNodePointer();
     native boolean nativeCursorMatchesFocus();
    private native boolean  nativeCursorIntersects(Rect visibleRect);
    private native boolean  nativeCursorIsAnchor();
    private native boolean  nativeCursorIsTextInput();
    private native Point    nativeCursorPosition();
    private native String   nativeCursorText();
    private native boolean  nativeCursorWantsKeyEvents();
    private native void     nativeDebugDump();
    private native void     nativeDestroy();
    private native boolean  nativeEvaluateLayersAnimations();
    private native void     nativeDrawExtras(Canvas canvas, int extra);
    private native void     nativeDumpDisplayTree(String urlOrNull);
    private native int      nativeFindAll(String findLower, String findUpper);
    private native void     nativeFindNext(boolean forward);
     native int      nativeFocusCandidateFramePointer();
     native boolean  nativeFocusCandidateHasNextTextfield();
     native boolean  nativeFocusCandidateIsPassword();
    private native boolean  nativeFocusCandidateIsRtlText();
    private native boolean  nativeFocusCandidateIsTextInput();
     native int      nativeFocusCandidateMaxLength();
     native String   nativeFocusCandidateName();
    private native Rect     nativeFocusCandidateNodeBounds();
     native int      nativeFocusCandidatePointer();
    private native String   nativeFocusCandidateText();
    private native int      nativeFocusCandidateTextSize();
    private native int      nativeFocusCandidateType();
    private native boolean  nativeFocusIsPlugin();
    private native Rect     nativeFocusNodeBounds();
     native int nativeFocusNodePointer();
    private native Rect     nativeGetCursorRingBounds();
    private native String   nativeGetSelection();
    private native boolean  nativeHasCursorNode();
    private native boolean  nativeHasFocusNode();
    private native void     nativeHideCursor();
    private native String   nativeImageURI(int x, int y);
    private native void     nativeInstrumentReport();
     native boolean nativeMoveCursorToNextTextInput();
    private native boolean  nativeMotionUp(int x, int y, int slop);
    private native boolean  nativeMoveCursor(int keyCode, int count,
            boolean noScroll);
    private native int      nativeMoveGeneration();
    private native void     nativeMoveSelection(int x, int y,
            boolean extendSelection);
    private native boolean  nativePointInNavCache(int x, int y, int slop);
    private native void     nativeRecordButtons(boolean focused,
            boolean pressed, boolean invalidate);
    private native void     nativeSelectBestAt(Rect rect);
    private native void     nativeSetFindIsEmpty();
    private native void     nativeSetFindIsUp(boolean isUp);
    private native void     nativeSetFollowedLink(boolean followed);
    private native void     nativeSetHeightCanMeasure(boolean measure);
    private native void     nativeSetRootLayer(int layer);
    private native void     nativeSetSelectionPointer(boolean set,
            float scale, int x, int y, boolean extendSelection);
    private native void     nativeSetSelectionRegion(boolean set);
    private native Rect     nativeSubtractLayers(Rect content);
    private native int      nativeTextGeneration();
    private native void     nativeUpdateCachedTextfield(String updatedText,
            int generation);
    private static final int NO_LEFTEDGE = -1;
    private native int      nativeGetBlockLeftEdge(int x, int y, float scale);
}
