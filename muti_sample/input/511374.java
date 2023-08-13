public class ViewConfiguration {
    private static final int SCROLL_BAR_SIZE = 10;
    private static final int SCROLL_BAR_FADE_DURATION = 250;
    private static final int SCROLL_BAR_DEFAULT_DELAY = 300;
    private static final int FADING_EDGE_LENGTH = 12;
    private static final int PRESSED_STATE_DURATION = 125;
    private static final int LONG_PRESS_TIMEOUT = 500;
    private static final int GLOBAL_ACTIONS_KEY_TIMEOUT = 500;
    private static final int TAP_TIMEOUT = 115;
    private static final int JUMP_TAP_TIMEOUT = 500;
    private static final int DOUBLE_TAP_TIMEOUT = 300;
    private static final int ZOOM_CONTROLS_TIMEOUT = 3000;
    private static final int EDGE_SLOP = 12;
    private static final int TOUCH_SLOP = 16;
    private static final int PAGING_TOUCH_SLOP = TOUCH_SLOP * 2;
    private static final int DOUBLE_TAP_SLOP = 100;
    private static final int WINDOW_TOUCH_SLOP = 16;
    private static final int MINIMUM_FLING_VELOCITY = 50;
    private static final int MAXIMUM_FLING_VELOCITY = 4000;
    @Deprecated
    private static final int MAXIMUM_DRAWING_CACHE_SIZE = 320 * 480 * 4; 
    private static float SCROLL_FRICTION = 0.015f;
    private final int mEdgeSlop;
    private final int mFadingEdgeLength;
    private final int mMinimumFlingVelocity;
    private final int mMaximumFlingVelocity;
    private final int mScrollbarSize;
    private final int mTouchSlop;
    private final int mPagingTouchSlop;
    private final int mDoubleTapSlop;
    private final int mWindowTouchSlop;
    private final int mMaximumDrawingCacheSize;
    private static final SparseArray<ViewConfiguration> sConfigurations =
            new SparseArray<ViewConfiguration>(2);
    @Deprecated
    public ViewConfiguration() {
        mEdgeSlop = EDGE_SLOP;
        mFadingEdgeLength = FADING_EDGE_LENGTH;
        mMinimumFlingVelocity = MINIMUM_FLING_VELOCITY;
        mMaximumFlingVelocity = MAXIMUM_FLING_VELOCITY;
        mScrollbarSize = SCROLL_BAR_SIZE;
        mTouchSlop = TOUCH_SLOP;
        mPagingTouchSlop = PAGING_TOUCH_SLOP;
        mDoubleTapSlop = DOUBLE_TAP_SLOP;
        mWindowTouchSlop = WINDOW_TOUCH_SLOP;
        mMaximumDrawingCacheSize = MAXIMUM_DRAWING_CACHE_SIZE;
    }
    private ViewConfiguration(Context context) {
        final DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        final float density = metrics.density;
        mEdgeSlop = (int) (density * EDGE_SLOP + 0.5f);
        mFadingEdgeLength = (int) (density * FADING_EDGE_LENGTH + 0.5f);
        mMinimumFlingVelocity = (int) (density * MINIMUM_FLING_VELOCITY + 0.5f);
        mMaximumFlingVelocity = (int) (density * MAXIMUM_FLING_VELOCITY + 0.5f);
        mScrollbarSize = (int) (density * SCROLL_BAR_SIZE + 0.5f);
        mTouchSlop = (int) (density * TOUCH_SLOP + 0.5f);
        mPagingTouchSlop = (int) (density * PAGING_TOUCH_SLOP + 0.5f);
        mDoubleTapSlop = (int) (density * DOUBLE_TAP_SLOP + 0.5f);
        mWindowTouchSlop = (int) (density * WINDOW_TOUCH_SLOP + 0.5f);
        mMaximumDrawingCacheSize = 4 * metrics.widthPixels * metrics.heightPixels;
    }
    public static ViewConfiguration get(Context context) {
        final DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        final int density = (int) (100.0f * metrics.density);
        ViewConfiguration configuration = sConfigurations.get(density);
        if (configuration == null) {
            configuration = new ViewConfiguration(context);
            sConfigurations.put(density, configuration);
        }
        return configuration;
    }
    @Deprecated
    public static int getScrollBarSize() {
        return SCROLL_BAR_SIZE;
    }
    public int getScaledScrollBarSize() {
        return mScrollbarSize;
    }
    public static int getScrollBarFadeDuration() {
        return SCROLL_BAR_FADE_DURATION;
    }
    public static int getScrollDefaultDelay() {
        return SCROLL_BAR_DEFAULT_DELAY;
    }
    @Deprecated
    public static int getFadingEdgeLength() {
        return FADING_EDGE_LENGTH;
    }
    public int getScaledFadingEdgeLength() {
        return mFadingEdgeLength;
    }
    public static int getPressedStateDuration() {
        return PRESSED_STATE_DURATION;
    }
    public static int getLongPressTimeout() {
        return LONG_PRESS_TIMEOUT;
    }
    public static int getTapTimeout() {
        return TAP_TIMEOUT;
    }
    public static int getJumpTapTimeout() {
        return JUMP_TAP_TIMEOUT;
    }
    public static int getDoubleTapTimeout() {
        return DOUBLE_TAP_TIMEOUT;
    }
    @Deprecated
    public static int getEdgeSlop() {
        return EDGE_SLOP;
    }
    public int getScaledEdgeSlop() {
        return mEdgeSlop;
    }
    @Deprecated
    public static int getTouchSlop() {
        return TOUCH_SLOP;
    }
    public int getScaledTouchSlop() {
        return mTouchSlop;
    }
    public int getScaledPagingTouchSlop() {
        return mPagingTouchSlop;
    }
    @Deprecated
    public static int getDoubleTapSlop() {
        return DOUBLE_TAP_SLOP;
    }
    public int getScaledDoubleTapSlop() {
        return mDoubleTapSlop;
    }
    @Deprecated
    public static int getWindowTouchSlop() {
        return WINDOW_TOUCH_SLOP;
    }
    public int getScaledWindowTouchSlop() {
        return mWindowTouchSlop;
    }
    @Deprecated
    public static int getMinimumFlingVelocity() {
        return MINIMUM_FLING_VELOCITY;
    }
    public int getScaledMinimumFlingVelocity() {
        return mMinimumFlingVelocity;
    }
    @Deprecated
    public static int getMaximumFlingVelocity() {
        return MAXIMUM_FLING_VELOCITY;
    }
    public int getScaledMaximumFlingVelocity() {
        return mMaximumFlingVelocity;
    }
    @Deprecated
    public static int getMaximumDrawingCacheSize() {
        return MAXIMUM_DRAWING_CACHE_SIZE;
    }
    public int getScaledMaximumDrawingCacheSize() {
        return mMaximumDrawingCacheSize;
    }
    public static long getZoomControlsTimeout() {
        return ZOOM_CONTROLS_TIMEOUT;
    }
    public static long getGlobalActionKeyTimeout() {
        return GLOBAL_ACTIONS_KEY_TIMEOUT;
    }
    public static float getScrollFriction() {
        return SCROLL_FRICTION;
    }
}
