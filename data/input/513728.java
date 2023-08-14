public abstract class Window {
    public static final int FEATURE_OPTIONS_PANEL = 0;
    public static final int FEATURE_NO_TITLE = 1;
    public static final int FEATURE_PROGRESS = 2;
    public static final int FEATURE_LEFT_ICON = 3;
    public static final int FEATURE_RIGHT_ICON = 4;
    public static final int FEATURE_INDETERMINATE_PROGRESS = 5;
    public static final int FEATURE_CONTEXT_MENU = 6;
    public static final int FEATURE_CUSTOM_TITLE = 7;
    public static final int FEATURE_OPENGL = 8;
    public static final int PROGRESS_VISIBILITY_ON = -1;
    public static final int PROGRESS_VISIBILITY_OFF = -2;
    public static final int PROGRESS_INDETERMINATE_ON = -3;
    public static final int PROGRESS_INDETERMINATE_OFF = -4;
    public static final int PROGRESS_START = 0;
    public static final int PROGRESS_END = 10000;
    public static final int PROGRESS_SECONDARY_START = 20000;
    public static final int PROGRESS_SECONDARY_END = 30000;
    @SuppressWarnings({"PointlessBitwiseExpression"})
    protected static final int DEFAULT_FEATURES = (1 << FEATURE_OPTIONS_PANEL) |
            (1 << FEATURE_CONTEXT_MENU);
    public static final int ID_ANDROID_CONTENT = com.android.internal.R.id.content;
    private final Context mContext;
    private TypedArray mWindowStyle;
    private Callback mCallback;
    private WindowManager mWindowManager;
    private IBinder mAppToken;
    private String mAppName;
    private Window mContainer;
    private Window mActiveChild;
    private boolean mIsActive = false;
    private boolean mHasChildren = false;
    private int mForcedWindowFlags = 0;
    private int mFeatures = DEFAULT_FEATURES;
    private int mLocalFeatures = DEFAULT_FEATURES;
    private boolean mHaveWindowFormat = false;
    private int mDefaultWindowFormat = PixelFormat.OPAQUE;
    private boolean mHasSoftInputMode = false;
    private final WindowManager.LayoutParams mWindowAttributes =
        new WindowManager.LayoutParams();
    public interface Callback {
        public boolean dispatchKeyEvent(KeyEvent event);
        public boolean dispatchTouchEvent(MotionEvent event);
        public boolean dispatchTrackballEvent(MotionEvent event);
        public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event);
        public View onCreatePanelView(int featureId);
        public boolean onCreatePanelMenu(int featureId, Menu menu);
        public boolean onPreparePanel(int featureId, View view, Menu menu);
        public boolean onMenuOpened(int featureId, Menu menu);
        public boolean onMenuItemSelected(int featureId, MenuItem item);
        public void onWindowAttributesChanged(WindowManager.LayoutParams attrs);
        public void onContentChanged();
        public void onWindowFocusChanged(boolean hasFocus);
        public void onAttachedToWindow();
        public void onDetachedFromWindow();
        public void onPanelClosed(int featureId, Menu menu);
        public boolean onSearchRequested();
    }
    public Window(Context context) {
        mContext = context;
    }
    public final Context getContext() {
        return mContext;
    }
    public final TypedArray getWindowStyle() {
        synchronized (this) {
            if (mWindowStyle == null) {
                mWindowStyle = mContext.obtainStyledAttributes(
                        com.android.internal.R.styleable.Window);
            }
            return mWindowStyle;
        }
    }
    public void setContainer(Window container) {
        mContainer = container;
        if (container != null) {
            mFeatures |= 1<<FEATURE_NO_TITLE;
            mLocalFeatures |= 1<<FEATURE_NO_TITLE;
            container.mHasChildren = true;
        }
    }
    public final Window getContainer() {
        return mContainer;
    }
    public final boolean hasChildren() {
        return mHasChildren;
    }
    public void setWindowManager(WindowManager wm,
            IBinder appToken, String appName) {
        mAppToken = appToken;
        mAppName = appName;
        if (wm == null) {
            wm = WindowManagerImpl.getDefault();
        }
        mWindowManager = new LocalWindowManager(wm);
    }
    private class LocalWindowManager implements WindowManager {
        LocalWindowManager(WindowManager wm) {
            mWindowManager = wm;
            mDefaultDisplay = mContext.getResources().getDefaultDisplay(
                    mWindowManager.getDefaultDisplay());
        }
        public final void addView(View view, ViewGroup.LayoutParams params) {
            WindowManager.LayoutParams wp = (WindowManager.LayoutParams)params;
            CharSequence curTitle = wp.getTitle();
            if (wp.type >= WindowManager.LayoutParams.FIRST_SUB_WINDOW &&
                wp.type <= WindowManager.LayoutParams.LAST_SUB_WINDOW) {
                if (wp.token == null) {
                    View decor = peekDecorView();
                    if (decor != null) {
                        wp.token = decor.getWindowToken();
                    }
                }
                if (curTitle == null || curTitle.length() == 0) {
                    String title;
                    if (wp.type == WindowManager.LayoutParams.TYPE_APPLICATION_MEDIA) {
                        title="Media";
                    } else if (wp.type == WindowManager.LayoutParams.TYPE_APPLICATION_MEDIA_OVERLAY) {
                        title="MediaOvr";
                    } else if (wp.type == WindowManager.LayoutParams.TYPE_APPLICATION_PANEL) {
                        title="Panel";
                    } else if (wp.type == WindowManager.LayoutParams.TYPE_APPLICATION_SUB_PANEL) {
                        title="SubPanel";
                    } else if (wp.type == WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG) {
                        title="AtchDlg";
                    } else {
                        title=Integer.toString(wp.type);
                    }
                    if (mAppName != null) {
                        title += ":" + mAppName;
                    }
                    wp.setTitle(title);
                }
            } else {
                if (wp.token == null) {
                    wp.token = mContainer == null ? mAppToken : mContainer.mAppToken;
                }
                if ((curTitle == null || curTitle.length() == 0)
                        && mAppName != null) {
                    wp.setTitle(mAppName);
                }
           }
            if (wp.packageName == null) {
                wp.packageName = mContext.getPackageName();
            }
            mWindowManager.addView(view, params);
        }
        public void updateViewLayout(View view, ViewGroup.LayoutParams params) {
            mWindowManager.updateViewLayout(view, params);
        }
        public final void removeView(View view) {
            mWindowManager.removeView(view);
        }
        public final void removeViewImmediate(View view) {
            mWindowManager.removeViewImmediate(view);
        }
        public Display getDefaultDisplay() {
            return mDefaultDisplay;
        }
        private final WindowManager mWindowManager;
        private final Display mDefaultDisplay;
    }
    public WindowManager getWindowManager() {
        return mWindowManager;
    }
    public void setCallback(Callback callback) {
        mCallback = callback;
    }
    public final Callback getCallback() {
        return mCallback;
    }
    public abstract boolean isFloating();
    public void setLayout(int width, int height)
    {
        final WindowManager.LayoutParams attrs = getAttributes();
        attrs.width = width;
        attrs.height = height;
        if (mCallback != null) {
            mCallback.onWindowAttributesChanged(attrs);
        }
    }
    public void setGravity(int gravity)
    {
        final WindowManager.LayoutParams attrs = getAttributes();
        attrs.gravity = gravity;
        if (mCallback != null) {
            mCallback.onWindowAttributesChanged(attrs);
        }
    }
    public void setType(int type) {
        final WindowManager.LayoutParams attrs = getAttributes();
        attrs.type = type;
        if (mCallback != null) {
            mCallback.onWindowAttributesChanged(attrs);
        }
    }
    public void setFormat(int format) {
        final WindowManager.LayoutParams attrs = getAttributes();
        if (format != PixelFormat.UNKNOWN) {
            attrs.format = format;
            mHaveWindowFormat = true;
        } else {
            attrs.format = mDefaultWindowFormat;
            mHaveWindowFormat = false;
        }
        if (mCallback != null) {
            mCallback.onWindowAttributesChanged(attrs);
        }
    }
    public void setWindowAnimations(int resId) {
        final WindowManager.LayoutParams attrs = getAttributes();
        attrs.windowAnimations = resId;
        if (mCallback != null) {
            mCallback.onWindowAttributesChanged(attrs);
        }
    }
    public void setSoftInputMode(int mode) {
        final WindowManager.LayoutParams attrs = getAttributes();
        if (mode != WindowManager.LayoutParams.SOFT_INPUT_STATE_UNSPECIFIED) {
            attrs.softInputMode = mode;
            mHasSoftInputMode = true;
        } else {
            mHasSoftInputMode = false;
        }
        if (mCallback != null) {
            mCallback.onWindowAttributesChanged(attrs);
        }
    }
    public void addFlags(int flags) {
        setFlags(flags, flags);
    }
    public void clearFlags(int flags) {
        setFlags(0, flags);
    }
    public void setFlags(int flags, int mask) {
        final WindowManager.LayoutParams attrs = getAttributes();
        attrs.flags = (attrs.flags&~mask) | (flags&mask);
        mForcedWindowFlags |= mask;
        if (mCallback != null) {
            mCallback.onWindowAttributesChanged(attrs);
        }
    }
    public void setAttributes(WindowManager.LayoutParams a) {
        mWindowAttributes.copyFrom(a);
        if (mCallback != null) {
            mCallback.onWindowAttributesChanged(mWindowAttributes);
        }
    }
    public final WindowManager.LayoutParams getAttributes() {
        return mWindowAttributes;
    }
    protected final int getForcedWindowFlags() {
        return mForcedWindowFlags;
    }
    protected final boolean hasSoftInputMode() {
        return mHasSoftInputMode;
    }
    public boolean requestFeature(int featureId) {
        final int flag = 1<<featureId;
        mFeatures |= flag;
        mLocalFeatures |= mContainer != null ? (flag&~mContainer.mFeatures) : flag;
        return (mFeatures&flag) != 0;
    }
    public final void makeActive() {
        if (mContainer != null) {
            if (mContainer.mActiveChild != null) {
                mContainer.mActiveChild.mIsActive = false;
            }
            mContainer.mActiveChild = this;
        }
        mIsActive = true;
        onActive();
    }
    public final boolean isActive()
    {
        return mIsActive;
    }
    public View findViewById(int id) {
        return getDecorView().findViewById(id);
    }
    public abstract void setContentView(int layoutResID);
    public abstract void setContentView(View view);
    public abstract void setContentView(View view, ViewGroup.LayoutParams params);
    public abstract void addContentView(View view, ViewGroup.LayoutParams params);
    public abstract View getCurrentFocus();
    public abstract LayoutInflater getLayoutInflater();
    public abstract void setTitle(CharSequence title);
    public abstract void setTitleColor(int textColor);
    public abstract void openPanel(int featureId, KeyEvent event);
    public abstract void closePanel(int featureId);
    public abstract void togglePanel(int featureId, KeyEvent event);
    public abstract boolean performPanelShortcut(int featureId,
                                                 int keyCode,
                                                 KeyEvent event,
                                                 int flags);
    public abstract boolean performPanelIdentifierAction(int featureId,
                                                 int id,
                                                 int flags);
    public abstract void closeAllPanels();
    public abstract boolean performContextMenuIdentifierAction(int id, int flags);
    public abstract void onConfigurationChanged(Configuration newConfig);
    public void setBackgroundDrawableResource(int resid)
    {
        setBackgroundDrawable(mContext.getResources().getDrawable(resid));
    }
    public abstract void setBackgroundDrawable(Drawable drawable);
    public abstract void setFeatureDrawableResource(int featureId, int resId);
    public abstract void setFeatureDrawableUri(int featureId, Uri uri);
    public abstract void setFeatureDrawable(int featureId, Drawable drawable);
    public abstract void setFeatureDrawableAlpha(int featureId, int alpha);
    public abstract void setFeatureInt(int featureId, int value);
    public abstract void takeKeyEvents(boolean get);
    public abstract boolean superDispatchKeyEvent(KeyEvent event);
    public abstract boolean superDispatchTouchEvent(MotionEvent event);
    public abstract boolean superDispatchTrackballEvent(MotionEvent event);
    public abstract View getDecorView();
    public abstract View peekDecorView();
    public abstract Bundle saveHierarchyState();
    public abstract void restoreHierarchyState(Bundle savedInstanceState);
    protected abstract void onActive();
    protected final int getFeatures()
    {
        return mFeatures;
    }
    protected final int getLocalFeatures()
    {
        return mLocalFeatures;
    }
    protected void setDefaultWindowFormat(int format) {
        mDefaultWindowFormat = format;
        if (!mHaveWindowFormat) {
            final WindowManager.LayoutParams attrs = getAttributes();
            attrs.format = format;
            if (mCallback != null) {
                mCallback.onWindowAttributesChanged(attrs);
            }
        }
    }
    public abstract void setChildDrawable(int featureId, Drawable drawable);
    public abstract void setChildInt(int featureId, int value);
    public abstract boolean isShortcutKey(int keyCode, KeyEvent event);
    public abstract void setVolumeControlStream(int streamType);
    public abstract int getVolumeControlStream();
}
