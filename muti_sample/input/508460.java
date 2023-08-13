public class Dialog implements DialogInterface, Window.Callback,
        KeyEvent.Callback, OnCreateContextMenuListener {
    private Activity mOwnerActivity;
    final Context mContext;
    final WindowManager mWindowManager;
    Window mWindow;
    View mDecor;
    protected boolean mCancelable = true;
    private Message mCancelMessage;
    private Message mDismissMessage;
    private Message mShowMessage;
    private boolean mCanceledOnTouchOutside = false;
    private OnKeyListener mOnKeyListener;
    private boolean mCreated = false;
    private boolean mShowing = false;
    private final Thread mUiThread;
    private final Handler mHandler = new Handler();
    private static final int DISMISS = 0x43;
    private static final int CANCEL = 0x44;
    private static final int SHOW = 0x45;
    private Handler mListenersHandler;
    private final Runnable mDismissAction = new Runnable() {
        public void run() {
            dismissDialog();
        }
    };
    public Dialog(Context context) {
        this(context, 0);
    }
    public Dialog(Context context, int theme) {
        mContext = new ContextThemeWrapper(
            context, theme == 0 ? com.android.internal.R.style.Theme_Dialog : theme);
        mWindowManager = (WindowManager)context.getSystemService("window");
        Window w = PolicyManager.makeNewWindow(mContext);
        mWindow = w;
        w.setCallback(this);
        w.setWindowManager(mWindowManager, null, null);
        w.setGravity(Gravity.CENTER);
        mUiThread = Thread.currentThread();
        mListenersHandler = new ListenersHandler(this);
    }
    @Deprecated
    protected Dialog(Context context, boolean cancelable,
            Message cancelCallback) {
        this(context);
        mCancelable = cancelable;
        mCancelMessage = cancelCallback;
    }
    protected Dialog(Context context, boolean cancelable,
            OnCancelListener cancelListener) {
        this(context);
        mCancelable = cancelable;
        setOnCancelListener(cancelListener);
    }
    public final Context getContext() {
        return mContext;
    }
    public final void setOwnerActivity(Activity activity) {
        mOwnerActivity = activity;
        getWindow().setVolumeControlStream(mOwnerActivity.getVolumeControlStream());
    }
    public final Activity getOwnerActivity() {
        return mOwnerActivity;
    }
    public boolean isShowing() {
        return mShowing;
    }
    public void show() {
        if (mShowing) {
            if (mDecor != null) {
                mDecor.setVisibility(View.VISIBLE);
            }
            return;
        }
        if (!mCreated) {
            dispatchOnCreate(null);
        }
        onStart();
        mDecor = mWindow.getDecorView();
        WindowManager.LayoutParams l = mWindow.getAttributes();
        if ((l.softInputMode
                & WindowManager.LayoutParams.SOFT_INPUT_IS_FORWARD_NAVIGATION) == 0) {
            WindowManager.LayoutParams nl = new WindowManager.LayoutParams();
            nl.copyFrom(l);
            nl.softInputMode |=
                    WindowManager.LayoutParams.SOFT_INPUT_IS_FORWARD_NAVIGATION;
            l = nl;
        }
        try {
            mWindowManager.addView(mDecor, l);
            mShowing = true;
            sendShowMessage();
        } finally {
        }
    }
    public void hide() {
        if (mDecor != null) {
            mDecor.setVisibility(View.GONE);
        }
    }
    public void dismiss() {
        if (Thread.currentThread() != mUiThread) {
            mHandler.post(mDismissAction);
        } else {
            mDismissAction.run();
        }
    }
    private void dismissDialog() {
        if (mDecor == null || !mShowing) {
            return;
        }
        try {
            mWindowManager.removeView(mDecor);
        } finally {
            mDecor = null;
            mWindow.closeAllPanels();
            onStop();
            mShowing = false;
            sendDismissMessage();
        }
    }
    private void sendDismissMessage() {
        if (mDismissMessage != null) {
            Message.obtain(mDismissMessage).sendToTarget();
        }
    }
    private void sendShowMessage() {
        if (mShowMessage != null) {
            Message.obtain(mShowMessage).sendToTarget();
        }
    }
    void dispatchOnCreate(Bundle savedInstanceState) {
        if (!mCreated) {
            onCreate(savedInstanceState);
            mCreated = true;
        }
    }
    protected void onCreate(Bundle savedInstanceState) {
    }
    protected void onStart() {
    }
    protected void onStop() {
    }
    private static final String DIALOG_SHOWING_TAG = "android:dialogShowing";
    private static final String DIALOG_HIERARCHY_TAG = "android:dialogHierarchy";
    public Bundle onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putBoolean(DIALOG_SHOWING_TAG, mShowing);
        if (mCreated) {
            bundle.putBundle(DIALOG_HIERARCHY_TAG, mWindow.saveHierarchyState());
        }
        return bundle;
    }
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        final Bundle dialogHierarchyState = savedInstanceState.getBundle(DIALOG_HIERARCHY_TAG);
        if (dialogHierarchyState == null) {
            return;
        }
        dispatchOnCreate(savedInstanceState);
        mWindow.restoreHierarchyState(dialogHierarchyState);
        if (savedInstanceState.getBoolean(DIALOG_SHOWING_TAG)) {
            show();
        }
    }
    public Window getWindow() {
        return mWindow;
    }
    public View getCurrentFocus() {
        return mWindow != null ? mWindow.getCurrentFocus() : null;
    }
    public View findViewById(int id) {
        return mWindow.findViewById(id);
    }
    public void setContentView(int layoutResID) {
        mWindow.setContentView(layoutResID);
    }
    public void setContentView(View view) {
        mWindow.setContentView(view);
    }
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        mWindow.setContentView(view, params);
    }
    public void addContentView(View view, ViewGroup.LayoutParams params) {
        mWindow.addContentView(view, params);
    }
    public void setTitle(CharSequence title) {
        mWindow.setTitle(title);
        mWindow.getAttributes().setTitle(title);
    }
    public void setTitle(int titleId) {
        setTitle(mContext.getText(titleId));
    }
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            event.startTracking();
            return true;
        }
        return false;
    }
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        return false;
    }
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.isTracking()
                && !event.isCanceled()) {
            onBackPressed();
            return true;
        }
        return false;
    }
    public boolean onKeyMultiple(int keyCode, int repeatCount, KeyEvent event) {
        return false;
    }
    public void onBackPressed() {
        if (mCancelable) {
            cancel();
        }
    }
    public boolean onTouchEvent(MotionEvent event) {
        if (mCancelable && mCanceledOnTouchOutside && event.getAction() == MotionEvent.ACTION_DOWN
                && isOutOfBounds(event)) {
            cancel();
            return true;
        }
        return false;
    }
    private boolean isOutOfBounds(MotionEvent event) {
        final int x = (int) event.getX();
        final int y = (int) event.getY();
        final int slop = ViewConfiguration.get(mContext).getScaledWindowTouchSlop();
        final View decorView = getWindow().getDecorView();
        return (x < -slop) || (y < -slop)
                || (x > (decorView.getWidth()+slop))
                || (y > (decorView.getHeight()+slop));
    }
    public boolean onTrackballEvent(MotionEvent event) {
        return false;
    }
    public void onWindowAttributesChanged(WindowManager.LayoutParams params) {
        if (mDecor != null) {
            mWindowManager.updateViewLayout(mDecor, params);
        }
    }
    public void onContentChanged() {
    }
    public void onWindowFocusChanged(boolean hasFocus) {
    }
    public void onAttachedToWindow() {
    }
    public void onDetachedFromWindow() {
    }
    public boolean dispatchKeyEvent(KeyEvent event) {
        if ((mOnKeyListener != null) && (mOnKeyListener.onKey(this, event.getKeyCode(), event))) {
            return true;
        }
        if (mWindow.superDispatchKeyEvent(event)) {
            return true;
        }
        return event.dispatch(this, mDecor != null
                ? mDecor.getKeyDispatcherState() : null, this);
    }
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (mWindow.superDispatchTouchEvent(ev)) {
            return true;
        }
        return onTouchEvent(ev);
    }
    public boolean dispatchTrackballEvent(MotionEvent ev) {
        if (mWindow.superDispatchTrackballEvent(ev)) {
            return true;
        }
        return onTrackballEvent(ev);
    }
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        event.setClassName(getClass().getName());
        event.setPackageName(mContext.getPackageName());
        LayoutParams params = getWindow().getAttributes();
        boolean isFullScreen = (params.width == LayoutParams.MATCH_PARENT) &&
            (params.height == LayoutParams.MATCH_PARENT);
        event.setFullScreen(isFullScreen);
        return false;
    }
    public View onCreatePanelView(int featureId) {
        return null;
    }
    public boolean onCreatePanelMenu(int featureId, Menu menu) {
        if (featureId == Window.FEATURE_OPTIONS_PANEL) {
            return onCreateOptionsMenu(menu);
        }
        return false;
    }
    public boolean onPreparePanel(int featureId, View view, Menu menu) {
        if (featureId == Window.FEATURE_OPTIONS_PANEL && menu != null) {
            boolean goforit = onPrepareOptionsMenu(menu);
            return goforit && menu.hasVisibleItems();
        }
        return true;
    }
    public boolean onMenuOpened(int featureId, Menu menu) {
        return true;
    }
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        return false;
    }
    public void onPanelClosed(int featureId, Menu menu) {
    }
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }
    public boolean onPrepareOptionsMenu(Menu menu) {
        return true;
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        return false;
    }
    public void onOptionsMenuClosed(Menu menu) {
    }
    public void openOptionsMenu() {
        mWindow.openPanel(Window.FEATURE_OPTIONS_PANEL, null);
    }
    public void closeOptionsMenu() {
        mWindow.closePanel(Window.FEATURE_OPTIONS_PANEL);
    }
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
    }
    public void registerForContextMenu(View view) {
        view.setOnCreateContextMenuListener(this);
    }
    public void unregisterForContextMenu(View view) {
        view.setOnCreateContextMenuListener(null);
    }
    public void openContextMenu(View view) {
        view.showContextMenu();
    }
    public boolean onContextItemSelected(MenuItem item) {
        return false;
    }
    public void onContextMenuClosed(Menu menu) {
    }
    public boolean onSearchRequested() {
        final SearchManager searchManager = (SearchManager) mContext
                .getSystemService(Context.SEARCH_SERVICE);
        final ComponentName appName = getAssociatedActivity();
        if (appName != null) {
            searchManager.startSearch(null, false, appName, null, false);
            dismiss();
            return true;
        } else {
            return false;
        }
    }
    private ComponentName getAssociatedActivity() {
        Activity activity = mOwnerActivity;
        Context context = getContext();
        while (activity == null && context != null) {
            if (context instanceof Activity) {
                activity = (Activity) context;  
            } else {
                context = (context instanceof ContextWrapper) ?
                        ((ContextWrapper) context).getBaseContext() : 
                        null;                                         
            }
        }
        return activity == null ? null : activity.getComponentName();
    }
    public void takeKeyEvents(boolean get) {
        mWindow.takeKeyEvents(get);
    }
    public final boolean requestWindowFeature(int featureId) {
        return getWindow().requestFeature(featureId);
    }
    public final void setFeatureDrawableResource(int featureId, int resId) {
        getWindow().setFeatureDrawableResource(featureId, resId);
    }
    public final void setFeatureDrawableUri(int featureId, Uri uri) {
        getWindow().setFeatureDrawableUri(featureId, uri);
    }
    public final void setFeatureDrawable(int featureId, Drawable drawable) {
        getWindow().setFeatureDrawable(featureId, drawable);
    }
    public final void setFeatureDrawableAlpha(int featureId, int alpha) {
        getWindow().setFeatureDrawableAlpha(featureId, alpha);
    }
    public LayoutInflater getLayoutInflater() {
        return getWindow().getLayoutInflater();
    }
    public void setCancelable(boolean flag) {
        mCancelable = flag;
    }
    public void setCanceledOnTouchOutside(boolean cancel) {
        if (cancel && !mCancelable) {
            mCancelable = true;
        }
        mCanceledOnTouchOutside = cancel;
    }
    public void cancel() {
        if (mCancelMessage != null) {
            Message.obtain(mCancelMessage).sendToTarget();
        }
        dismiss();
    }
    public void setOnCancelListener(final OnCancelListener listener) {
        if (listener != null) {
            mCancelMessage = mListenersHandler.obtainMessage(CANCEL, listener);
        } else {
            mCancelMessage = null;
        }
    }
    public void setCancelMessage(final Message msg) {
        mCancelMessage = msg;
    }
    public void setOnDismissListener(final OnDismissListener listener) {
        if (listener != null) {
            mDismissMessage = mListenersHandler.obtainMessage(DISMISS, listener);
        } else {
            mDismissMessage = null;
        }
    }
    public void setOnShowListener(OnShowListener listener) {
        if (listener != null) {
            mShowMessage = mListenersHandler.obtainMessage(SHOW, listener);
        } else {
            mShowMessage = null;
        }
    }
    public void setDismissMessage(final Message msg) {
        mDismissMessage = msg;
    }
    public final void setVolumeControlStream(int streamType) {
        getWindow().setVolumeControlStream(streamType);
    }
    public final int getVolumeControlStream() {
        return getWindow().getVolumeControlStream();
    }
    public void setOnKeyListener(final OnKeyListener onKeyListener) {
        mOnKeyListener = onKeyListener;
    }
    private static final class ListenersHandler extends Handler {
        private WeakReference<DialogInterface> mDialog;
        public ListenersHandler(Dialog dialog) {
            mDialog = new WeakReference<DialogInterface>(dialog);
        }
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DISMISS:
                    ((OnDismissListener) msg.obj).onDismiss(mDialog.get());
                    break;
                case CANCEL:
                    ((OnCancelListener) msg.obj).onCancel(mDialog.get());
                    break;
                case SHOW:
                    ((OnShowListener) msg.obj).onShow(mDialog.get());
                    break;
            }
        }
    }
}
