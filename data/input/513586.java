public class ZoomButtonsController implements View.OnTouchListener {
    private static final String TAG = "ZoomButtonsController";
    private static final int ZOOM_CONTROLS_TIMEOUT =
            (int) ViewConfiguration.getZoomControlsTimeout();
    private static final int ZOOM_CONTROLS_TOUCH_PADDING = 20;
    private int mTouchPaddingScaledSq;
    private final Context mContext;
    private final WindowManager mWindowManager;
    private boolean mAutoDismissControls = true;
    private final View mOwnerView;
    private final int[] mOwnerViewRawLocation = new int[2];
    private final FrameLayout mContainer;
    private LayoutParams mContainerLayoutParams;
    private final int[] mContainerRawLocation = new int[2];
    private ZoomControls mControls;
    private View mTouchTargetView;
    private final int[] mTouchTargetWindowLocation = new int[2];
    private boolean mReleaseTouchListenerOnUp;
    private boolean mIsVisible;
    private final Rect mTempRect = new Rect();
    private final int[] mTempIntArray = new int[2];
    private OnZoomListener mCallback;
    private Runnable mPostedVisibleInitializer;
    private final IntentFilter mConfigurationChangedFilter =
            new IntentFilter(Intent.ACTION_CONFIGURATION_CHANGED);
    private final BroadcastReceiver mConfigurationChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!mIsVisible) return;
            mHandler.removeMessages(MSG_POST_CONFIGURATION_CHANGED);
            mHandler.sendEmptyMessage(MSG_POST_CONFIGURATION_CHANGED);
        }
    };
    private static final int MSG_POST_CONFIGURATION_CHANGED = 2;
    private static final int MSG_DISMISS_ZOOM_CONTROLS = 3;
    private static final int MSG_POST_SET_VISIBLE = 4;
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_POST_CONFIGURATION_CHANGED:
                    onPostConfigurationChanged();
                    break;
                case MSG_DISMISS_ZOOM_CONTROLS:
                    setVisible(false);
                    break;
                case MSG_POST_SET_VISIBLE:
                    if (mOwnerView.getWindowToken() == null) {
                        Log.e(TAG,
                                "Cannot make the zoom controller visible if the owner view is " +
                                "not attached to a window.");
                    } else {
                        setVisible(true);
                    }
                    break;
            }
        }
    };
    public ZoomButtonsController(View ownerView) {
        mContext = ownerView.getContext();
        mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        mOwnerView = ownerView;
        mTouchPaddingScaledSq = (int)
                (ZOOM_CONTROLS_TOUCH_PADDING * mContext.getResources().getDisplayMetrics().density);
        mTouchPaddingScaledSq *= mTouchPaddingScaledSq;
        mContainer = createContainer();
    }
    public void setZoomInEnabled(boolean enabled) {
        mControls.setIsZoomInEnabled(enabled);
    }
    public void setZoomOutEnabled(boolean enabled) {
        mControls.setIsZoomOutEnabled(enabled);
    }
    public void setZoomSpeed(long speed) {
        mControls.setZoomSpeed(speed);
    }
    private FrameLayout createContainer() {
        LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.TOP | Gravity.LEFT;
        lp.flags = LayoutParams.FLAG_NOT_TOUCHABLE |
                LayoutParams.FLAG_NOT_FOCUSABLE |
                LayoutParams.FLAG_LAYOUT_NO_LIMITS |
                LayoutParams.FLAG_ALT_FOCUSABLE_IM;
        lp.height = LayoutParams.WRAP_CONTENT;
        lp.width = LayoutParams.MATCH_PARENT;
        lp.type = LayoutParams.TYPE_APPLICATION_PANEL;
        lp.format = PixelFormat.TRANSLUCENT;
        lp.windowAnimations = com.android.internal.R.style.Animation_ZoomButtons;
        mContainerLayoutParams = lp;
        FrameLayout container = new Container(mContext);
        container.setLayoutParams(lp);
        container.setMeasureAllChildren(true);
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(com.android.internal.R.layout.zoom_container, container);
        mControls = (ZoomControls) container.findViewById(com.android.internal.R.id.zoomControls);
        mControls.setOnZoomInClickListener(new OnClickListener() {
            public void onClick(View v) {
                dismissControlsDelayed(ZOOM_CONTROLS_TIMEOUT);
                if (mCallback != null) mCallback.onZoom(true);
            }
        });
        mControls.setOnZoomOutClickListener(new OnClickListener() {
            public void onClick(View v) {
                dismissControlsDelayed(ZOOM_CONTROLS_TIMEOUT);
                if (mCallback != null) mCallback.onZoom(false);
            }
        });
        return container;
    }
    public void setOnZoomListener(OnZoomListener listener) {
        mCallback = listener;
    }
    public void setFocusable(boolean focusable) {
        int oldFlags = mContainerLayoutParams.flags;
        if (focusable) {
            mContainerLayoutParams.flags &= ~LayoutParams.FLAG_NOT_FOCUSABLE;
        } else {
            mContainerLayoutParams.flags |= LayoutParams.FLAG_NOT_FOCUSABLE;
        }
        if ((mContainerLayoutParams.flags != oldFlags) && mIsVisible) {
            mWindowManager.updateViewLayout(mContainer, mContainerLayoutParams);
        }
    }
    public boolean isAutoDismissed() {
        return mAutoDismissControls;
    }
    public void setAutoDismissed(boolean autoDismiss) {
        if (mAutoDismissControls == autoDismiss) return;
        mAutoDismissControls = autoDismiss;
    }
    public boolean isVisible() {
        return mIsVisible;
    }
    public void setVisible(boolean visible) {
        if (visible) {
            if (mOwnerView.getWindowToken() == null) {
                if (!mHandler.hasMessages(MSG_POST_SET_VISIBLE)) {
                    mHandler.sendEmptyMessage(MSG_POST_SET_VISIBLE);
                }
                return;
            }
            dismissControlsDelayed(ZOOM_CONTROLS_TIMEOUT);
        }
        if (mIsVisible == visible) {
            return;
        }
        mIsVisible = visible;
        if (visible) {
            if (mContainerLayoutParams.token == null) {
                mContainerLayoutParams.token = mOwnerView.getWindowToken();
            }
            mWindowManager.addView(mContainer, mContainerLayoutParams);
            if (mPostedVisibleInitializer == null) {
                mPostedVisibleInitializer = new Runnable() {
                    public void run() {
                        refreshPositioningVariables();
                        if (mCallback != null) {
                            mCallback.onVisibilityChanged(true);
                        }
                    }
                };
            }
            mHandler.post(mPostedVisibleInitializer);
            mContext.registerReceiver(mConfigurationChangedReceiver, mConfigurationChangedFilter);
            mOwnerView.setOnTouchListener(this);
            mReleaseTouchListenerOnUp = false;
        } else {
            if (mTouchTargetView != null) {
                mReleaseTouchListenerOnUp = true;
            } else {
                mOwnerView.setOnTouchListener(null);
            }
            mContext.unregisterReceiver(mConfigurationChangedReceiver);
            mWindowManager.removeView(mContainer);
            mHandler.removeCallbacks(mPostedVisibleInitializer);
            if (mCallback != null) {
                mCallback.onVisibilityChanged(false);
            }
        }
    }
    public ViewGroup getContainer() {
        return mContainer;
    }
    public View getZoomControls() {
        return mControls;
    }
    private void dismissControlsDelayed(int delay) {
        if (mAutoDismissControls) {
            mHandler.removeMessages(MSG_DISMISS_ZOOM_CONTROLS);
            mHandler.sendEmptyMessageDelayed(MSG_DISMISS_ZOOM_CONTROLS, delay);
        }
    }
    private void refreshPositioningVariables() {
        if (mOwnerView.getWindowToken() == null) return;
        int ownerHeight = mOwnerView.getHeight();
        int ownerWidth = mOwnerView.getWidth();
        int containerOwnerYOffset = ownerHeight - mContainer.getHeight();
        mOwnerView.getLocationOnScreen(mOwnerViewRawLocation);
        mContainerRawLocation[0] = mOwnerViewRawLocation[0];
        mContainerRawLocation[1] = mOwnerViewRawLocation[1] + containerOwnerYOffset;
        int[] ownerViewWindowLoc = mTempIntArray;
        mOwnerView.getLocationInWindow(ownerViewWindowLoc);
        mContainerLayoutParams.x = ownerViewWindowLoc[0];
        mContainerLayoutParams.width = ownerWidth;
        mContainerLayoutParams.y = ownerViewWindowLoc[1] + containerOwnerYOffset;
        if (mIsVisible) {
            mWindowManager.updateViewLayout(mContainer, mContainerLayoutParams);
        }
    }
    private boolean onContainerKey(KeyEvent event) {
        int keyCode = event.getKeyCode();
        if (isInterestingKey(keyCode)) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                if (event.getAction() == KeyEvent.ACTION_DOWN
                        && event.getRepeatCount() == 0) {
                    if (mOwnerView != null) {
                        KeyEvent.DispatcherState ds = mOwnerView.getKeyDispatcherState();
                        if (ds != null) {
                            ds.startTracking(event, this);
                        }
                    }
                    return true;
                } else if (event.getAction() == KeyEvent.ACTION_UP
                        && event.isTracking() && !event.isCanceled()) {
                    setVisible(false);
                    return true;
                }
            } else {
                dismissControlsDelayed(ZOOM_CONTROLS_TIMEOUT);
            }
            return false;
        } else {
            ViewRoot viewRoot = getOwnerViewRoot();
            if (viewRoot != null) {
                viewRoot.dispatchKey(event);
            }
            return true;
        }
    }
    private boolean isInterestingKey(int keyCode) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_DPAD_UP:
            case KeyEvent.KEYCODE_DPAD_DOWN:
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_DPAD_RIGHT:
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_BACK:
                return true;
            default:
                return false;
        }
    }
    private ViewRoot getOwnerViewRoot() {
        View rootViewOfOwner = mOwnerView.getRootView();
        if (rootViewOfOwner == null) {
            return null;
        }
        ViewParent parentOfRootView = rootViewOfOwner.getParent();
        if (parentOfRootView instanceof ViewRoot) {
            return (ViewRoot) parentOfRootView;
        } else {
            return null;
        }
    }
    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getAction();
        if (event.getPointerCount() > 1) {
            return false;
        }
        if (mReleaseTouchListenerOnUp) {
            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                mOwnerView.setOnTouchListener(null);
                setTouchTargetView(null);
                mReleaseTouchListenerOnUp = false;
            }
            return true;
        }
        dismissControlsDelayed(ZOOM_CONTROLS_TIMEOUT);
        View targetView = mTouchTargetView;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                targetView = findViewForTouch((int) event.getRawX(), (int) event.getRawY());
                setTouchTargetView(targetView);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                setTouchTargetView(null);
                break;
        }
        if (targetView != null) {
            int targetViewRawX = mContainerRawLocation[0] + mTouchTargetWindowLocation[0];
            int targetViewRawY = mContainerRawLocation[1] + mTouchTargetWindowLocation[1];
            MotionEvent containerEvent = MotionEvent.obtain(event);
            containerEvent.offsetLocation(mOwnerViewRawLocation[0] - targetViewRawX,
                    mOwnerViewRawLocation[1] - targetViewRawY);
            float containerX = containerEvent.getX();
            float containerY = containerEvent.getY();
            if (containerX < 0 && containerX > -ZOOM_CONTROLS_TOUCH_PADDING) {
                containerEvent.offsetLocation(-containerX, 0);
            }
            if (containerY < 0 && containerY > -ZOOM_CONTROLS_TOUCH_PADDING) {
                containerEvent.offsetLocation(0, -containerY);
            }
            boolean retValue = targetView.dispatchTouchEvent(containerEvent);
            containerEvent.recycle();
            return retValue;
        } else {
            return false;
        }
    }
    private void setTouchTargetView(View view) {
        mTouchTargetView = view;
        if (view != null) {
            view.getLocationInWindow(mTouchTargetWindowLocation);
        }
    }
    private View findViewForTouch(int rawX, int rawY) {
        int containerCoordsX = rawX - mContainerRawLocation[0];
        int containerCoordsY = rawY - mContainerRawLocation[1];
        Rect frame = mTempRect;
        View closestChild = null;
        int closestChildDistanceSq = Integer.MAX_VALUE;
        for (int i = mContainer.getChildCount() - 1; i >= 0; i--) {
            View child = mContainer.getChildAt(i);
            if (child.getVisibility() != View.VISIBLE) {
                continue;
            }
            child.getHitRect(frame);
            if (frame.contains(containerCoordsX, containerCoordsY)) {
                return child;
            }
            int distanceX;
            if (containerCoordsX >= frame.left && containerCoordsX <= frame.right) {
                distanceX = 0;
            } else {
                distanceX = Math.min(Math.abs(frame.left - containerCoordsX),
                    Math.abs(containerCoordsX - frame.right));
            }
            int distanceY;
            if (containerCoordsY >= frame.top && containerCoordsY <= frame.bottom) {
                distanceY = 0;
            } else {
                distanceY = Math.min(Math.abs(frame.top - containerCoordsY),
                        Math.abs(containerCoordsY - frame.bottom));
            }
            int distanceSq = distanceX * distanceX + distanceY * distanceY;
            if ((distanceSq < mTouchPaddingScaledSq) &&
                    (distanceSq < closestChildDistanceSq)) {
                closestChild = child;
                closestChildDistanceSq = distanceSq;
            }
        }
        return closestChild;
    }
    private void onPostConfigurationChanged() {
        dismissControlsDelayed(ZOOM_CONTROLS_TIMEOUT);
        refreshPositioningVariables();
    }
    public interface OnZoomListener {
        void onVisibilityChanged(boolean visible);
        void onZoom(boolean zoomIn);
    }
    private class Container extends FrameLayout {
        public Container(Context context) {
            super(context);
        }
        @Override
        public boolean dispatchKeyEvent(KeyEvent event) {
            return onContainerKey(event) ? true : super.dispatchKeyEvent(event);
        }
    }
}
