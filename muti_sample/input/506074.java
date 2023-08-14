public class KeyguardViewManager implements KeyguardWindowController {
    private final static boolean DEBUG = false;
    private static String TAG = "KeyguardViewManager";
    private final Context mContext;
    private final ViewManager mViewManager;
    private final KeyguardViewCallback mCallback;
    private final KeyguardViewProperties mKeyguardViewProperties;
    private final KeyguardUpdateMonitor mUpdateMonitor;
    private WindowManager.LayoutParams mWindowLayoutParams;
    private boolean mNeedsInput = false;
    private FrameLayout mKeyguardHost;
    private KeyguardViewBase mKeyguardView;
    private boolean mScreenOn = false;
    public KeyguardViewManager(Context context, ViewManager viewManager,
            KeyguardViewCallback callback, KeyguardViewProperties keyguardViewProperties, KeyguardUpdateMonitor updateMonitor) {
        mContext = context;
        mViewManager = viewManager;
        mCallback = callback;
        mKeyguardViewProperties = keyguardViewProperties;
        mUpdateMonitor = updateMonitor;
    }
    private static class KeyguardViewHost extends FrameLayout {
        private final KeyguardViewCallback mCallback;
        private KeyguardViewHost(Context context, KeyguardViewCallback callback) {
            super(context);
            mCallback = callback;
        }
        @Override
        protected void dispatchDraw(Canvas canvas) {
            super.dispatchDraw(canvas);
            mCallback.keyguardDoneDrawing();
        }
    }
    public synchronized void show() {
        if (DEBUG) Log.d(TAG, "show(); mKeyguardView==" + mKeyguardView);
        if (mKeyguardHost == null) {
            if (DEBUG) Log.d(TAG, "keyguard host is null, creating it...");
            mKeyguardHost = new KeyguardViewHost(mContext, mCallback);
            final int stretch = ViewGroup.LayoutParams.MATCH_PARENT;
            int flags = WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN
                    | WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER
                    | WindowManager.LayoutParams.FLAG_KEEP_SURFACE_WHILE_ANIMATING
                     ;
            if (!mNeedsInput) {
                flags |= WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;
            }
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                    stretch, stretch, WindowManager.LayoutParams.TYPE_KEYGUARD,
                    flags, PixelFormat.TRANSLUCENT);
            lp.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN;
            lp.windowAnimations = com.android.internal.R.style.Animation_LockScreen;
            lp.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_NOSENSOR;
            lp.setTitle("Keyguard");
            mWindowLayoutParams = lp;
            mViewManager.addView(mKeyguardHost, lp);
        }
        if (mKeyguardView == null) {
            if (DEBUG) Log.d(TAG, "keyguard view is null, creating it...");
            mKeyguardView = mKeyguardViewProperties.createKeyguardView(mContext, mUpdateMonitor, this);
            mKeyguardView.setId(R.id.lock_screen);
            mKeyguardView.setCallback(mCallback);
            final ViewGroup.LayoutParams lp = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            mKeyguardHost.addView(mKeyguardView, lp);
            if (mScreenOn) {
                mKeyguardView.onScreenTurnedOn();
            }
        }
        mKeyguardHost.setVisibility(View.VISIBLE);
        mKeyguardView.requestFocus();
    }
    public void setNeedsInput(boolean needsInput) {
        mNeedsInput = needsInput;
        if (mWindowLayoutParams != null) {
            if (needsInput) {
                mWindowLayoutParams.flags &=
                    ~WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;
            } else {
                mWindowLayoutParams.flags |=
                    WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;
            }
            mViewManager.updateViewLayout(mKeyguardHost, mWindowLayoutParams);
        }
    }
    public synchronized void reset() {
        if (DEBUG) Log.d(TAG, "reset()");
        if (mKeyguardView != null) {
            mKeyguardView.reset();
        }
    }
    public synchronized void onScreenTurnedOff() {
        if (DEBUG) Log.d(TAG, "onScreenTurnedOff()");
        mScreenOn = false;
        if (mKeyguardView != null) {
            mKeyguardView.onScreenTurnedOff();
        }
    }
    public synchronized void onScreenTurnedOn() {
        if (DEBUG) Log.d(TAG, "onScreenTurnedOn()");
        mScreenOn = true;
        if (mKeyguardView != null) {
            mKeyguardView.onScreenTurnedOn();
        }
    }
    public synchronized void verifyUnlock() {
        if (DEBUG) Log.d(TAG, "verifyUnlock()");
        show();
        mKeyguardView.verifyUnlock();
    }
    public boolean wakeWhenReadyTq(int keyCode) {
        if (DEBUG) Log.d(TAG, "wakeWhenReady(" + keyCode + ")");
        if (mKeyguardView != null) {
            mKeyguardView.wakeWhenReadyTq(keyCode);
            return true;
        } else {
            Log.w(TAG, "mKeyguardView is null in wakeWhenReadyTq");
            return false;
        }
    }
    public synchronized void hide() {
        if (DEBUG) Log.d(TAG, "hide()");
        if (mKeyguardHost != null) {
            mKeyguardHost.setVisibility(View.GONE);
            if (mKeyguardView != null) {
                final KeyguardViewBase lastView = mKeyguardView;
                mKeyguardView = null;
                mKeyguardHost.postDelayed(new Runnable() {
                    public void run() {
                        synchronized (KeyguardViewManager.this) {
                            mKeyguardHost.removeView(lastView);
                            lastView.cleanUp();
                        }
                    }
                }, 500);
            }
        }
    }
    public synchronized boolean isShowing() {
        return (mKeyguardHost != null && mKeyguardHost.getVisibility() == View.VISIBLE);
    }
}
