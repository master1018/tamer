public class PopupWindow {
    public static final int INPUT_METHOD_FROM_FOCUSABLE = 0;
    public static final int INPUT_METHOD_NEEDED = 1;
    public static final int INPUT_METHOD_NOT_NEEDED = 2;
    private Context mContext;
    private WindowManager mWindowManager;
    private boolean mIsShowing;
    private boolean mIsDropdown;
    private View mContentView;
    private View mPopupView;
    private boolean mFocusable;
    private int mInputMethodMode = INPUT_METHOD_FROM_FOCUSABLE;
    private int mSoftInputMode;
    private boolean mTouchable = true;
    private boolean mOutsideTouchable = false;
    private boolean mClippingEnabled = true;
    private OnTouchListener mTouchInterceptor;
    private int mWidthMode;
    private int mWidth;
    private int mLastWidth;
    private int mHeightMode;
    private int mHeight;
    private int mLastHeight;
    private int mPopupWidth;
    private int mPopupHeight;
    private int[] mDrawingLocation = new int[2];
    private int[] mScreenLocation = new int[2];
    private Rect mTempRect = new Rect();
    private Drawable mBackground;
    private Drawable mAboveAnchorBackgroundDrawable;
    private Drawable mBelowAnchorBackgroundDrawable;
    private boolean mAboveAnchor;
    private OnDismissListener mOnDismissListener;
    private boolean mIgnoreCheekPress = false;
    private int mAnimationStyle = -1;
    private static final int[] ABOVE_ANCHOR_STATE_SET = new int[] {
        com.android.internal.R.attr.state_above_anchor
    };
    private WeakReference<View> mAnchor;
    private OnScrollChangedListener mOnScrollChangedListener =
        new OnScrollChangedListener() {
            public void onScrollChanged() {
                View anchor = mAnchor.get();
                if (anchor != null && mPopupView != null) {
                    WindowManager.LayoutParams p = (WindowManager.LayoutParams)
                            mPopupView.getLayoutParams();
                    updateAboveAnchor(findDropDownPosition(anchor, p, mAnchorXoff, mAnchorYoff));
                    update(p.x, p.y, -1, -1, true);
                }
            }
        };
    private int mAnchorXoff, mAnchorYoff;
    public PopupWindow(Context context) {
        this(context, null);
    }
    public PopupWindow(Context context, AttributeSet attrs) {
        this(context, attrs, com.android.internal.R.attr.popupWindowStyle);
    }
    public PopupWindow(Context context, AttributeSet attrs, int defStyle) {
        mContext = context;
        mWindowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        TypedArray a =
            context.obtainStyledAttributes(
                attrs, com.android.internal.R.styleable.PopupWindow, defStyle, 0);
        mBackground = a.getDrawable(R.styleable.PopupWindow_popupBackground);
        if (mBackground instanceof StateListDrawable) {
            StateListDrawable background = (StateListDrawable) mBackground;
            int aboveAnchorStateIndex = background.getStateDrawableIndex(ABOVE_ANCHOR_STATE_SET);
            int count = background.getStateCount();
            int belowAnchorStateIndex = -1;
            for (int i = 0; i < count; i++) {
                if (i != aboveAnchorStateIndex) {
                    belowAnchorStateIndex = i;
                    break;
                }
            }
            if (aboveAnchorStateIndex != -1 && belowAnchorStateIndex != -1) {
                mAboveAnchorBackgroundDrawable = background.getStateDrawable(aboveAnchorStateIndex);
                mBelowAnchorBackgroundDrawable = background.getStateDrawable(belowAnchorStateIndex);
            } else {
                mBelowAnchorBackgroundDrawable = null;
                mAboveAnchorBackgroundDrawable = null;
            }
        }
        a.recycle();
    }
    public PopupWindow() {
        this(null, 0, 0);
    }
    public PopupWindow(View contentView) {
        this(contentView, 0, 0);
    }
    public PopupWindow(int width, int height) {
        this(null, width, height);
    }
    public PopupWindow(View contentView, int width, int height) {
        this(contentView, width, height, false);
    }
    public PopupWindow(View contentView, int width, int height, boolean focusable) {
        if (contentView != null) {
            mContext = contentView.getContext();
            mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        }
        setContentView(contentView);
        setWidth(width);
        setHeight(height);
        setFocusable(focusable);
    }
    public Drawable getBackground() {
        return mBackground;
    }
    public void setBackgroundDrawable(Drawable background) {
        mBackground = background;
    }
    public int getAnimationStyle() {
        return mAnimationStyle;
    }
    public void setIgnoreCheekPress() {
        mIgnoreCheekPress = true;
    }
    public void setAnimationStyle(int animationStyle) {
        mAnimationStyle = animationStyle;
    }
    public View getContentView() {
        return mContentView;
    }
    public void setContentView(View contentView) {
        if (isShowing()) {
            return;
        }
        mContentView = contentView;
        if (mContext == null) {
            mContext = mContentView.getContext();
        }
        if (mWindowManager == null) {
            mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        }
    }
    public void setTouchInterceptor(OnTouchListener l) {
        mTouchInterceptor = l;
    }
    public boolean isFocusable() {
        return mFocusable;
    }
    public void setFocusable(boolean focusable) {
        mFocusable = focusable;
    }
    public int getInputMethodMode() {
        return mInputMethodMode;
    }
    public void setInputMethodMode(int mode) {
        mInputMethodMode = mode;
    }
    public void setSoftInputMode(int mode) {
        mSoftInputMode = mode;
    }
    public int getSoftInputMode() {
        return mSoftInputMode;
    }
    public boolean isTouchable() {
        return mTouchable;
    }
    public void setTouchable(boolean touchable) {
        mTouchable = touchable;
    }
    public boolean isOutsideTouchable() {
        return mOutsideTouchable;
    }
    public void setOutsideTouchable(boolean touchable) {
        mOutsideTouchable = touchable;
    }
    public boolean isClippingEnabled() {
        return mClippingEnabled;
    }
    public void setClippingEnabled(boolean enabled) {
        mClippingEnabled = enabled;
    }
    public void setWindowLayoutMode(int widthSpec, int heightSpec) {
        mWidthMode = widthSpec;
        mHeightMode = heightSpec;
    }
    public int getHeight() {
        return mHeight;
    }
    public void setHeight(int height) {
        mHeight = height;
    }
    public int getWidth() {
        return mWidth;
    }
    public void setWidth(int width) {
        mWidth = width;
    }
    public boolean isShowing() {
        return mIsShowing;
    }
    public void showAtLocation(View parent, int gravity, int x, int y) {
        if (isShowing() || mContentView == null) {
            return;
        }
        unregisterForScrollChanged();
        mIsShowing = true;
        mIsDropdown = false;
        WindowManager.LayoutParams p = createPopupLayout(parent.getWindowToken());
        p.windowAnimations = computeAnimationResource();
        preparePopup(p);
        if (gravity == Gravity.NO_GRAVITY) {
            gravity = Gravity.TOP | Gravity.LEFT;
        }
        p.gravity = gravity;
        p.x = x;
        p.y = y;
        invokePopup(p);
    }
    public void showAsDropDown(View anchor) {
        showAsDropDown(anchor, 0, 0);
    }
    public void showAsDropDown(View anchor, int xoff, int yoff) {
        if (isShowing() || mContentView == null) {
            return;
        }
        registerForScrollChanged(anchor, xoff, yoff);
        mIsShowing = true;
        mIsDropdown = true;
        WindowManager.LayoutParams p = createPopupLayout(anchor.getWindowToken());
        preparePopup(p);
        updateAboveAnchor(findDropDownPosition(anchor, p, xoff, yoff));
        if (mHeightMode < 0) p.height = mLastHeight = mHeightMode;
        if (mWidthMode < 0) p.width = mLastWidth = mWidthMode;
        p.windowAnimations = computeAnimationResource();
        invokePopup(p);
    }
    private void updateAboveAnchor(boolean aboveAnchor) {
        if (aboveAnchor != mAboveAnchor) {
            mAboveAnchor = aboveAnchor;
            if (mBackground != null) {
                if (mAboveAnchorBackgroundDrawable != null) {
                    if (mAboveAnchor) {
                        mPopupView.setBackgroundDrawable(mAboveAnchorBackgroundDrawable);
                    } else {
                        mPopupView.setBackgroundDrawable(mBelowAnchorBackgroundDrawable);
                    }
                } else {
                    mPopupView.refreshDrawableState();
                }
            }
        }
    }
    public boolean isAboveAnchor() {
        return mAboveAnchor;
    }
    private void preparePopup(WindowManager.LayoutParams p) {
        if (mContentView == null || mContext == null || mWindowManager == null) {
            throw new IllegalStateException("You must specify a valid content view by "
                    + "calling setContentView() before attempting to show the popup.");
        }
        if (mBackground != null) {
            final ViewGroup.LayoutParams layoutParams = mContentView.getLayoutParams();
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            if (layoutParams != null &&
                    layoutParams.height == ViewGroup.LayoutParams.WRAP_CONTENT) {
                height = ViewGroup.LayoutParams.WRAP_CONTENT;
            }
            PopupViewContainer popupViewContainer = new PopupViewContainer(mContext);
            PopupViewContainer.LayoutParams listParams = new PopupViewContainer.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, height
            );
            popupViewContainer.setBackgroundDrawable(mBackground);
            popupViewContainer.addView(mContentView, listParams);
            mPopupView = popupViewContainer;
        } else {
            mPopupView = mContentView;
        }
        mPopupWidth = p.width;
        mPopupHeight = p.height;
    }
    private void invokePopup(WindowManager.LayoutParams p) {
        p.packageName = mContext.getPackageName();
        mWindowManager.addView(mPopupView, p);
    }
    private WindowManager.LayoutParams createPopupLayout(IBinder token) {
        WindowManager.LayoutParams p = new WindowManager.LayoutParams();
        p.gravity = Gravity.LEFT | Gravity.TOP;
        p.width = mLastWidth = mWidth;
        p.height = mLastHeight = mHeight;
        if (mBackground != null) {
            p.format = mBackground.getOpacity();
        } else {
            p.format = PixelFormat.TRANSLUCENT;
        }
        p.flags = computeFlags(p.flags);
        p.type = WindowManager.LayoutParams.TYPE_APPLICATION_PANEL;
        p.token = token;
        p.softInputMode = mSoftInputMode;
        p.setTitle("PopupWindow:" + Integer.toHexString(hashCode()));
        return p;
    }
    private int computeFlags(int curFlags) {
        curFlags &= ~(
                WindowManager.LayoutParams.FLAG_IGNORE_CHEEK_PRESSES |
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH |
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS |
                WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        if(mIgnoreCheekPress) {
            curFlags |= WindowManager.LayoutParams.FLAG_IGNORE_CHEEK_PRESSES;
        }
        if (!mFocusable) {
            curFlags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            if (mInputMethodMode == INPUT_METHOD_NEEDED) {
                curFlags |= WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;
            }
        } else if (mInputMethodMode == INPUT_METHOD_NOT_NEEDED) {
            curFlags |= WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;
        }
        if (!mTouchable) {
            curFlags |= WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        }
        if (mOutsideTouchable) {
            curFlags |= WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
        }
        if (!mClippingEnabled) {
            curFlags |= WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
        }
        return curFlags;
    }
    private int computeAnimationResource() {
        if (mAnimationStyle == -1) {
            if (mIsDropdown) {
                return mAboveAnchor
                        ? com.android.internal.R.style.Animation_DropDownUp
                        : com.android.internal.R.style.Animation_DropDownDown;
            }
            return 0;
        }
        return mAnimationStyle;
    }
    private boolean findDropDownPosition(View anchor, WindowManager.LayoutParams p,
            int xoff, int yoff) {
        anchor.getLocationInWindow(mDrawingLocation);
        p.x = mDrawingLocation[0] + xoff;
        p.y = mDrawingLocation[1] + anchor.getHeight() + yoff;
        boolean onTop = false;
        p.gravity = Gravity.LEFT | Gravity.TOP;
        anchor.getLocationOnScreen(mScreenLocation);
        final Rect displayFrame = new Rect();
        anchor.getWindowVisibleDisplayFrame(displayFrame);
        final View root = anchor.getRootView();
        if (p.y + mPopupHeight > displayFrame.bottom || p.x + mPopupWidth - root.getWidth() > 0) {
            int scrollX = anchor.getScrollX();
            int scrollY = anchor.getScrollY();
            Rect r = new Rect(scrollX, scrollY,  scrollX + mPopupWidth + xoff,
                    scrollY + mPopupHeight + anchor.getHeight() + yoff);
            anchor.requestRectangleOnScreen(r, true);
            anchor.getLocationInWindow(mDrawingLocation);
            p.x = mDrawingLocation[0] + xoff;
            p.y = mDrawingLocation[1] + anchor.getHeight() + yoff;
            anchor.getLocationOnScreen(mScreenLocation);
            onTop = (displayFrame.bottom - mScreenLocation[1] - anchor.getHeight() - yoff) <
                    (mScreenLocation[1] - yoff - displayFrame.top);
            if (onTop) {
                p.gravity = Gravity.LEFT | Gravity.BOTTOM;
                p.y = root.getHeight() - mDrawingLocation[1] + yoff;
            } else {
                p.y = mDrawingLocation[1] + anchor.getHeight() + yoff;
            }
        }
        p.gravity |= Gravity.DISPLAY_CLIP_VERTICAL;
        return onTop;
    }
    public int getMaxAvailableHeight(View anchor) {
        return getMaxAvailableHeight(anchor, 0);
    }
    public int getMaxAvailableHeight(View anchor, int yOffset) {
        return getMaxAvailableHeight(anchor, yOffset, false);
    }
    public int getMaxAvailableHeight(View anchor, int yOffset, boolean ignoreBottomDecorations) {
        final Rect displayFrame = new Rect();
        anchor.getWindowVisibleDisplayFrame(displayFrame);
        final int[] anchorPos = mDrawingLocation;
        anchor.getLocationOnScreen(anchorPos);
        int bottomEdge = displayFrame.bottom;
        if (ignoreBottomDecorations) {
            bottomEdge = anchor.getContext().getResources().getDisplayMetrics().heightPixels;
        }
        final int distanceToBottom = bottomEdge - (anchorPos[1] + anchor.getHeight()) - yOffset;
        final int distanceToTop = anchorPos[1] - displayFrame.top + yOffset;
        int returnedHeight = Math.max(distanceToBottom, distanceToTop);
        if (mBackground != null) {
            mBackground.getPadding(mTempRect);
            returnedHeight -= mTempRect.top + mTempRect.bottom; 
        }
        return returnedHeight;
    }
    public void dismiss() {
        if (isShowing() && mPopupView != null) {
            unregisterForScrollChanged();
            try {
                mWindowManager.removeView(mPopupView);                
            } finally {
                if (mPopupView != mContentView && mPopupView instanceof ViewGroup) {
                    ((ViewGroup) mPopupView).removeView(mContentView);
                }
                mPopupView = null;
                mIsShowing = false;
                if (mOnDismissListener != null) {
                    mOnDismissListener.onDismiss();
                }
            }
        }
    }
    public void setOnDismissListener(OnDismissListener onDismissListener) {
        mOnDismissListener = onDismissListener;
    }
    public void update() {
        if (!isShowing() || mContentView == null) {
            return;
        }
        WindowManager.LayoutParams p = (WindowManager.LayoutParams)
                mPopupView.getLayoutParams();
        boolean update = false;
        final int newAnim = computeAnimationResource();
        if (newAnim != p.windowAnimations) {
            p.windowAnimations = newAnim;
            update = true;
        }
        final int newFlags = computeFlags(p.flags);
        if (newFlags != p.flags) {
            p.flags = newFlags;
            update = true;
        }
        if (update) {
            mWindowManager.updateViewLayout(mPopupView, p);
        }
    }
    public void update(int width, int height) {
        WindowManager.LayoutParams p = (WindowManager.LayoutParams)
                mPopupView.getLayoutParams();
        update(p.x, p.y, width, height, false);
    }
    public void update(int x, int y, int width, int height) {
        update(x, y, width, height, false);
    }
    public void update(int x, int y, int width, int height, boolean force) {
        if (width != -1) {
            mLastWidth = width;
            setWidth(width);
        }
        if (height != -1) {
            mLastHeight = height;
            setHeight(height);
        }
        if (!isShowing() || mContentView == null) {
            return;
        }
        WindowManager.LayoutParams p = (WindowManager.LayoutParams) mPopupView.getLayoutParams();
        boolean update = force;
        final int finalWidth = mWidthMode < 0 ? mWidthMode : mLastWidth;
        if (width != -1 && p.width != finalWidth) {
            p.width = mLastWidth = finalWidth;
            update = true;
        }
        final int finalHeight = mHeightMode < 0 ? mHeightMode : mLastHeight;
        if (height != -1 && p.height != finalHeight) {
            p.height = mLastHeight = finalHeight;
            update = true;
        }
        if (p.x != x) {
            p.x = x;
            update = true;
        }
        if (p.y != y) {
            p.y = y;
            update = true;
        }
        final int newAnim = computeAnimationResource();
        if (newAnim != p.windowAnimations) {
            p.windowAnimations = newAnim;
            update = true;
        }
        final int newFlags = computeFlags(p.flags);
        if (newFlags != p.flags) {
            p.flags = newFlags;
            update = true;
        }
        if (update) {
            mWindowManager.updateViewLayout(mPopupView, p);
        }
    }
    public void update(View anchor, int width, int height) {
        update(anchor, false, 0, 0, true, width, height);
    }
    public void update(View anchor, int xoff, int yoff, int width, int height) {
        update(anchor, true, xoff, yoff, true, width, height);
    }
    private void update(View anchor, boolean updateLocation, int xoff, int yoff,
            boolean updateDimension, int width, int height) {
        if (!isShowing() || mContentView == null) {
            return;
        }
        WeakReference<View> oldAnchor = mAnchor;
        if (oldAnchor == null || oldAnchor.get() != anchor ||
                (updateLocation && (mAnchorXoff != xoff || mAnchorYoff != yoff))) {
            registerForScrollChanged(anchor, xoff, yoff);
        }
        WindowManager.LayoutParams p = (WindowManager.LayoutParams) mPopupView.getLayoutParams();
        if (updateDimension) {
            if (width == -1) {
                width = mPopupWidth;
            } else {
                mPopupWidth = width;
            }
            if (height == -1) {
                height = mPopupHeight;
            } else {
                mPopupHeight = height;
            }
        }
        int x = p.x;
        int y = p.y;
        if (updateLocation) {
            updateAboveAnchor(findDropDownPosition(anchor, p, xoff, yoff));
        } else {
            updateAboveAnchor(findDropDownPosition(anchor, p, mAnchorXoff, mAnchorYoff));            
        }
        update(p.x, p.y, width, height, x != p.x || y != p.y);
    }
    public interface OnDismissListener {
        public void onDismiss();
    }
    private void unregisterForScrollChanged() {
        WeakReference<View> anchorRef = mAnchor;
        View anchor = null;
        if (anchorRef != null) {
            anchor = anchorRef.get();
        }
        if (anchor != null) {
            ViewTreeObserver vto = anchor.getViewTreeObserver();
            vto.removeOnScrollChangedListener(mOnScrollChangedListener);
        }
        mAnchor = null;
    }
    private void registerForScrollChanged(View anchor, int xoff, int yoff) {
        unregisterForScrollChanged();
        mAnchor = new WeakReference<View>(anchor);
        ViewTreeObserver vto = anchor.getViewTreeObserver();
        if (vto != null) {
            vto.addOnScrollChangedListener(mOnScrollChangedListener);
        }
        mAnchorXoff = xoff;
        mAnchorYoff = yoff;
    }
    private class PopupViewContainer extends FrameLayout {
        public PopupViewContainer(Context context) {
            super(context);
        }
        @Override
        protected int[] onCreateDrawableState(int extraSpace) {
            if (mAboveAnchor) {
                final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
                View.mergeDrawableStates(drawableState, ABOVE_ANCHOR_STATE_SET);
                return drawableState;
            } else {
                return super.onCreateDrawableState(extraSpace);
            }
        }
        @Override
        public boolean dispatchKeyEvent(KeyEvent event) {
            if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                if (event.getAction() == KeyEvent.ACTION_DOWN
                        && event.getRepeatCount() == 0) {
                    getKeyDispatcherState().startTracking(event, this);
                    return true;
                } else if (event.getAction() == KeyEvent.ACTION_UP
                        && getKeyDispatcherState().isTracking(event) && !event.isCanceled()) {
                    dismiss();
                    return true;
                }
                return super.dispatchKeyEvent(event);
            } else {
                return super.dispatchKeyEvent(event);
            }
        }
        @Override
        public boolean dispatchTouchEvent(MotionEvent ev) {
            if (mTouchInterceptor != null && mTouchInterceptor.onTouch(this, ev)) {
                return true;
            }
            return super.dispatchTouchEvent(ev);
        }
        @Override
        public boolean onTouchEvent(MotionEvent event) {
            final int x = (int) event.getX();
            final int y = (int) event.getY();
            if ((event.getAction() == MotionEvent.ACTION_DOWN)
                    && ((x < 0) || (x >= getWidth()) || (y < 0) || (y >= getHeight()))) {
                dismiss();
                return true;
            } else if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
                dismiss();
                return true;
            } else {
                return super.onTouchEvent(event);
            }
        }
        @Override
        public void sendAccessibilityEvent(int eventType) {
            if (mContentView != null) {
                mContentView.sendAccessibilityEvent(eventType);
            } else {
                super.sendAccessibilityEvent(eventType);
            }
        }
    }
}
