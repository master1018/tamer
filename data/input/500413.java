public class View implements Drawable.Callback, KeyEvent.Callback, AccessibilityEventSource {
    private static final boolean DBG = false;
    protected static final String VIEW_LOG_TAG = "View";
    public static final int NO_ID = -1;
    private static final int NOT_FOCUSABLE = 0x00000000;
    private static final int FOCUSABLE = 0x00000001;
    private static final int FOCUSABLE_MASK = 0x00000001;
    private static final int FITS_SYSTEM_WINDOWS = 0x00000002;
    public static final int VISIBLE = 0x00000000;
    public static final int INVISIBLE = 0x00000004;
    public static final int GONE = 0x00000008;
    static final int VISIBILITY_MASK = 0x0000000C;
    private static final int[] VISIBILITY_FLAGS = {VISIBLE, INVISIBLE, GONE};
    static final int ENABLED = 0x00000000;
    static final int DISABLED = 0x00000020;
    static final int ENABLED_MASK = 0x00000020;
    static final int WILL_NOT_DRAW = 0x00000080;
    static final int DRAW_MASK = 0x00000080;
    static final int SCROLLBARS_NONE = 0x00000000;
    static final int SCROLLBARS_HORIZONTAL = 0x00000100;
    static final int SCROLLBARS_VERTICAL = 0x00000200;
    static final int SCROLLBARS_MASK = 0x00000300;
    static final int FADING_EDGE_NONE = 0x00000000;
    static final int FADING_EDGE_HORIZONTAL = 0x00001000;
    static final int FADING_EDGE_VERTICAL = 0x00002000;
    static final int FADING_EDGE_MASK = 0x00003000;
    static final int CLICKABLE = 0x00004000;
    static final int DRAWING_CACHE_ENABLED = 0x00008000;
    static final int SAVE_DISABLED = 0x000010000;
    static final int SAVE_DISABLED_MASK = 0x000010000;
    static final int WILL_NOT_CACHE_DRAWING = 0x000020000;
    static final int FOCUSABLE_IN_TOUCH_MODE = 0x00040000;
    public static final int DRAWING_CACHE_QUALITY_LOW = 0x00080000;
    public static final int DRAWING_CACHE_QUALITY_HIGH = 0x00100000;
    public static final int DRAWING_CACHE_QUALITY_AUTO = 0x00000000;
    private static final int[] DRAWING_CACHE_QUALITY_FLAGS = {
            DRAWING_CACHE_QUALITY_AUTO, DRAWING_CACHE_QUALITY_LOW, DRAWING_CACHE_QUALITY_HIGH
    };
    static final int DRAWING_CACHE_QUALITY_MASK = 0x00180000;
    static final int LONG_CLICKABLE = 0x00200000;
    static final int DUPLICATE_PARENT_STATE = 0x00400000;
    public static final int SCROLLBARS_INSIDE_OVERLAY = 0;
    public static final int SCROLLBARS_INSIDE_INSET = 0x01000000;
    public static final int SCROLLBARS_OUTSIDE_OVERLAY = 0x02000000;
    public static final int SCROLLBARS_OUTSIDE_INSET = 0x03000000;
    static final int SCROLLBARS_INSET_MASK = 0x01000000;
    static final int SCROLLBARS_OUTSIDE_MASK = 0x02000000;
    static final int SCROLLBARS_STYLE_MASK = 0x03000000;
    public static final int KEEP_SCREEN_ON = 0x04000000;
    public static final int SOUND_EFFECTS_ENABLED = 0x08000000;
    public static final int HAPTIC_FEEDBACK_ENABLED = 0x10000000;
    public static final int FOCUSABLES_ALL = 0x00000000;
    public static final int FOCUSABLES_TOUCH_MODE = 0x00000001;
    public static final int FOCUS_BACKWARD = 0x00000001;
    public static final int FOCUS_FORWARD = 0x00000002;
    public static final int FOCUS_LEFT = 0x00000011;
    public static final int FOCUS_UP = 0x00000021;
    public static final int FOCUS_RIGHT = 0x00000042;
    public static final int FOCUS_DOWN = 0x00000082;
    protected static final int[] EMPTY_STATE_SET = {};
    protected static final int[] ENABLED_STATE_SET = {R.attr.state_enabled};
    protected static final int[] FOCUSED_STATE_SET = {R.attr.state_focused};
    protected static final int[] SELECTED_STATE_SET = {R.attr.state_selected};
    protected static final int[] PRESSED_STATE_SET = {R.attr.state_pressed};
    protected static final int[] WINDOW_FOCUSED_STATE_SET =
            {R.attr.state_window_focused};
    protected static final int[] ENABLED_FOCUSED_STATE_SET =
            stateSetUnion(ENABLED_STATE_SET, FOCUSED_STATE_SET);
    protected static final int[] ENABLED_SELECTED_STATE_SET =
            stateSetUnion(ENABLED_STATE_SET, SELECTED_STATE_SET);
    protected static final int[] ENABLED_WINDOW_FOCUSED_STATE_SET =
            stateSetUnion(ENABLED_STATE_SET, WINDOW_FOCUSED_STATE_SET);
    protected static final int[] FOCUSED_SELECTED_STATE_SET =
            stateSetUnion(FOCUSED_STATE_SET, SELECTED_STATE_SET);
    protected static final int[] FOCUSED_WINDOW_FOCUSED_STATE_SET =
            stateSetUnion(FOCUSED_STATE_SET, WINDOW_FOCUSED_STATE_SET);
    protected static final int[] SELECTED_WINDOW_FOCUSED_STATE_SET =
            stateSetUnion(SELECTED_STATE_SET, WINDOW_FOCUSED_STATE_SET);
    protected static final int[] ENABLED_FOCUSED_SELECTED_STATE_SET =
            stateSetUnion(ENABLED_FOCUSED_STATE_SET, SELECTED_STATE_SET);
    protected static final int[] ENABLED_FOCUSED_WINDOW_FOCUSED_STATE_SET =
            stateSetUnion(ENABLED_FOCUSED_STATE_SET, WINDOW_FOCUSED_STATE_SET);
    protected static final int[] ENABLED_SELECTED_WINDOW_FOCUSED_STATE_SET =
            stateSetUnion(ENABLED_SELECTED_STATE_SET, WINDOW_FOCUSED_STATE_SET);
    protected static final int[] FOCUSED_SELECTED_WINDOW_FOCUSED_STATE_SET =
            stateSetUnion(FOCUSED_SELECTED_STATE_SET, WINDOW_FOCUSED_STATE_SET);
    protected static final int[] ENABLED_FOCUSED_SELECTED_WINDOW_FOCUSED_STATE_SET =
            stateSetUnion(ENABLED_FOCUSED_SELECTED_STATE_SET,
                          WINDOW_FOCUSED_STATE_SET);
    protected static final int[] PRESSED_WINDOW_FOCUSED_STATE_SET =
            stateSetUnion(PRESSED_STATE_SET, WINDOW_FOCUSED_STATE_SET);
    protected static final int[] PRESSED_SELECTED_STATE_SET =
            stateSetUnion(PRESSED_STATE_SET, SELECTED_STATE_SET);
    protected static final int[] PRESSED_SELECTED_WINDOW_FOCUSED_STATE_SET =
            stateSetUnion(PRESSED_SELECTED_STATE_SET, WINDOW_FOCUSED_STATE_SET);
    protected static final int[] PRESSED_FOCUSED_STATE_SET =
            stateSetUnion(PRESSED_STATE_SET, FOCUSED_STATE_SET);
    protected static final int[] PRESSED_FOCUSED_WINDOW_FOCUSED_STATE_SET =
            stateSetUnion(PRESSED_FOCUSED_STATE_SET, WINDOW_FOCUSED_STATE_SET);
    protected static final int[] PRESSED_FOCUSED_SELECTED_STATE_SET =
            stateSetUnion(PRESSED_FOCUSED_STATE_SET, SELECTED_STATE_SET);
    protected static final int[] PRESSED_FOCUSED_SELECTED_WINDOW_FOCUSED_STATE_SET =
            stateSetUnion(PRESSED_FOCUSED_SELECTED_STATE_SET, WINDOW_FOCUSED_STATE_SET);
    protected static final int[] PRESSED_ENABLED_STATE_SET =
            stateSetUnion(PRESSED_STATE_SET, ENABLED_STATE_SET);
    protected static final int[] PRESSED_ENABLED_WINDOW_FOCUSED_STATE_SET =
            stateSetUnion(PRESSED_ENABLED_STATE_SET, WINDOW_FOCUSED_STATE_SET);
    protected static final int[] PRESSED_ENABLED_SELECTED_STATE_SET =
            stateSetUnion(PRESSED_ENABLED_STATE_SET, SELECTED_STATE_SET);
    protected static final int[] PRESSED_ENABLED_SELECTED_WINDOW_FOCUSED_STATE_SET =
            stateSetUnion(PRESSED_ENABLED_SELECTED_STATE_SET, WINDOW_FOCUSED_STATE_SET);
    protected static final int[] PRESSED_ENABLED_FOCUSED_STATE_SET =
            stateSetUnion(PRESSED_ENABLED_STATE_SET, FOCUSED_STATE_SET);
    protected static final int[] PRESSED_ENABLED_FOCUSED_WINDOW_FOCUSED_STATE_SET =
            stateSetUnion(PRESSED_ENABLED_FOCUSED_STATE_SET, WINDOW_FOCUSED_STATE_SET);
    protected static final int[] PRESSED_ENABLED_FOCUSED_SELECTED_STATE_SET =
            stateSetUnion(PRESSED_ENABLED_FOCUSED_STATE_SET, SELECTED_STATE_SET);
    protected static final int[] PRESSED_ENABLED_FOCUSED_SELECTED_WINDOW_FOCUSED_STATE_SET =
            stateSetUnion(PRESSED_ENABLED_FOCUSED_SELECTED_STATE_SET, WINDOW_FOCUSED_STATE_SET);
    private static final int[][] VIEW_STATE_SETS = {
        EMPTY_STATE_SET,                                           
        WINDOW_FOCUSED_STATE_SET,                                  
        SELECTED_STATE_SET,                                        
        SELECTED_WINDOW_FOCUSED_STATE_SET,                         
        FOCUSED_STATE_SET,                                         
        FOCUSED_WINDOW_FOCUSED_STATE_SET,                          
        FOCUSED_SELECTED_STATE_SET,                                
        FOCUSED_SELECTED_WINDOW_FOCUSED_STATE_SET,                 
        ENABLED_STATE_SET,                                         
        ENABLED_WINDOW_FOCUSED_STATE_SET,                          
        ENABLED_SELECTED_STATE_SET,                                
        ENABLED_SELECTED_WINDOW_FOCUSED_STATE_SET,                 
        ENABLED_FOCUSED_STATE_SET,                                 
        ENABLED_FOCUSED_WINDOW_FOCUSED_STATE_SET,                  
        ENABLED_FOCUSED_SELECTED_STATE_SET,                        
        ENABLED_FOCUSED_SELECTED_WINDOW_FOCUSED_STATE_SET,         
        PRESSED_STATE_SET,                                         
        PRESSED_WINDOW_FOCUSED_STATE_SET,                          
        PRESSED_SELECTED_STATE_SET,                                
        PRESSED_SELECTED_WINDOW_FOCUSED_STATE_SET,                 
        PRESSED_FOCUSED_STATE_SET,                                 
        PRESSED_FOCUSED_WINDOW_FOCUSED_STATE_SET,                  
        PRESSED_FOCUSED_SELECTED_STATE_SET,                        
        PRESSED_FOCUSED_SELECTED_WINDOW_FOCUSED_STATE_SET,         
        PRESSED_ENABLED_STATE_SET,                                 
        PRESSED_ENABLED_WINDOW_FOCUSED_STATE_SET,                  
        PRESSED_ENABLED_SELECTED_STATE_SET,                        
        PRESSED_ENABLED_SELECTED_WINDOW_FOCUSED_STATE_SET,         
        PRESSED_ENABLED_FOCUSED_STATE_SET,                         
        PRESSED_ENABLED_FOCUSED_WINDOW_FOCUSED_STATE_SET,          
        PRESSED_ENABLED_FOCUSED_SELECTED_STATE_SET,                
        PRESSED_ENABLED_FOCUSED_SELECTED_WINDOW_FOCUSED_STATE_SET, 
    };
    protected static final int[] LAST_STATE_SET = {R.attr.state_last};
    protected static final int[] FIRST_STATE_SET = {R.attr.state_first};
    protected static final int[] MIDDLE_STATE_SET = {R.attr.state_middle};
    protected static final int[] SINGLE_STATE_SET = {R.attr.state_single};
    protected static final int[] PRESSED_LAST_STATE_SET = {R.attr.state_last, R.attr.state_pressed};
    protected static final int[] PRESSED_FIRST_STATE_SET = {R.attr.state_first, R.attr.state_pressed};
    protected static final int[] PRESSED_MIDDLE_STATE_SET = {R.attr.state_middle, R.attr.state_pressed};
    protected static final int[] PRESSED_SINGLE_STATE_SET = {R.attr.state_single, R.attr.state_pressed};
    static final ThreadLocal<Rect> sThreadLocal = new ThreadLocal<Rect>();
    private static WeakHashMap<View, SparseArray<Object>> sTags;
    private static final Object sTagsLock = new Object();
    protected Animation mCurrentAnimation = null;
    @ViewDebug.ExportedProperty
    protected int mMeasuredWidth;
    @ViewDebug.ExportedProperty
    protected int mMeasuredHeight;
    @ViewDebug.ExportedProperty(resolveId = true)
    int mID = NO_ID;
    protected Object mTag;
    static final int WANTS_FOCUS                    = 0x00000001;
    static final int FOCUSED                        = 0x00000002;
    static final int SELECTED                       = 0x00000004;
    static final int IS_ROOT_NAMESPACE              = 0x00000008;
    static final int HAS_BOUNDS                     = 0x00000010;
    static final int DRAWN                          = 0x00000020;
    static final int DRAW_ANIMATION                 = 0x00000040;
    static final int SKIP_DRAW                      = 0x00000080;
    static final int ONLY_DRAWS_BACKGROUND          = 0x00000100;
    static final int REQUEST_TRANSPARENT_REGIONS    = 0x00000200;
    static final int DRAWABLE_STATE_DIRTY           = 0x00000400;
    static final int MEASURED_DIMENSION_SET         = 0x00000800;
    static final int FORCE_LAYOUT                   = 0x00001000;
    private static final int LAYOUT_REQUIRED        = 0x00002000;
    private static final int PRESSED                = 0x00004000;
    static final int DRAWING_CACHE_VALID            = 0x00008000;
    static final int ANIMATION_STARTED              = 0x00010000;
    private static final int SAVE_STATE_CALLED      = 0x00020000;
    static final int ALPHA_SET                      = 0x00040000;
    static final int SCROLL_CONTAINER               = 0x00080000;
    static final int SCROLL_CONTAINER_ADDED         = 0x00100000;
    static final int DIRTY                          = 0x00200000;
    static final int DIRTY_OPAQUE                   = 0x00400000;
    static final int DIRTY_MASK                     = 0x00600000;
    static final int OPAQUE_BACKGROUND              = 0x00800000;
    static final int OPAQUE_SCROLLBARS              = 0x01000000;
    static final int OPAQUE_MASK                    = 0x01800000;
    private static final int PREPRESSED             = 0x02000000;
    static final int CANCEL_NEXT_UP_EVENT = 0x04000000;
    private static final int AWAKEN_SCROLL_BARS_ON_ATTACH = 0x08000000;
    protected ViewParent mParent;
    AttachInfo mAttachInfo;
    @ViewDebug.ExportedProperty(flagMapping = {
        @ViewDebug.FlagToString(mask = FORCE_LAYOUT, equals = FORCE_LAYOUT,
                name = "FORCE_LAYOUT"),
        @ViewDebug.FlagToString(mask = LAYOUT_REQUIRED, equals = LAYOUT_REQUIRED,
                name = "LAYOUT_REQUIRED"),
        @ViewDebug.FlagToString(mask = DRAWING_CACHE_VALID, equals = DRAWING_CACHE_VALID,
            name = "DRAWING_CACHE_INVALID", outputIf = false),
        @ViewDebug.FlagToString(mask = DRAWN, equals = DRAWN, name = "DRAWN", outputIf = true),
        @ViewDebug.FlagToString(mask = DRAWN, equals = DRAWN, name = "NOT_DRAWN", outputIf = false),
        @ViewDebug.FlagToString(mask = DIRTY_MASK, equals = DIRTY_OPAQUE, name = "DIRTY_OPAQUE"),
        @ViewDebug.FlagToString(mask = DIRTY_MASK, equals = DIRTY, name = "DIRTY")
    })
    int mPrivateFlags;
    int mWindowAttachCount;
    protected ViewGroup.LayoutParams mLayoutParams;
    @ViewDebug.ExportedProperty
    int mViewFlags;
    @ViewDebug.ExportedProperty
    protected int mLeft;
    @ViewDebug.ExportedProperty
    protected int mRight;
    @ViewDebug.ExportedProperty
    protected int mTop;
    @ViewDebug.ExportedProperty
    protected int mBottom;
    @ViewDebug.ExportedProperty
    protected int mScrollX;
    @ViewDebug.ExportedProperty
    protected int mScrollY;
    @ViewDebug.ExportedProperty
    protected int mPaddingLeft;
    @ViewDebug.ExportedProperty
    protected int mPaddingRight;
    @ViewDebug.ExportedProperty
    protected int mPaddingTop;
    @ViewDebug.ExportedProperty
    protected int mPaddingBottom;
    private CharSequence mContentDescription;
    @ViewDebug.ExportedProperty
    int mUserPaddingRight;
    @ViewDebug.ExportedProperty
    int mUserPaddingBottom;
    int mOldWidthMeasureSpec = Integer.MIN_VALUE;
    int mOldHeightMeasureSpec = Integer.MIN_VALUE;
    private Resources mResources = null;
    private Drawable mBGDrawable;
    private int mBackgroundResource;
    private boolean mBackgroundSizeChanged;
    protected OnFocusChangeListener mOnFocusChangeListener;
    protected OnClickListener mOnClickListener;
    protected OnLongClickListener mOnLongClickListener;
    protected OnCreateContextMenuListener mOnCreateContextMenuListener;
    private OnKeyListener mOnKeyListener;
    private OnTouchListener mOnTouchListener;
    protected Context mContext;
    private ScrollabilityCache mScrollCache;
    private int[] mDrawableState = null;
    private SoftReference<Bitmap> mDrawingCache;
    private SoftReference<Bitmap> mUnscaledDrawingCache;
    private int mNextFocusLeftId = View.NO_ID;
    private int mNextFocusRightId = View.NO_ID;
    private int mNextFocusUpId = View.NO_ID;
    private int mNextFocusDownId = View.NO_ID;
    private CheckForLongPress mPendingCheckForLongPress;
    private CheckForTap mPendingCheckForTap = null;
    private PerformClick mPerformClick;
    private UnsetPressedState mUnsetPressedState;
    private boolean mHasPerformedLongPress;
    @ViewDebug.ExportedProperty
    private int mMinHeight;
    @ViewDebug.ExportedProperty
    private int mMinWidth;
    private TouchDelegate mTouchDelegate = null;
    private int mDrawingCacheBackgroundColor = 0;
    private ViewTreeObserver mFloatingTreeObserver;
    private int mTouchSlop;
    static long sInstanceCount = 0;
    public View(Context context) {
        mContext = context;
        mResources = context != null ? context.getResources() : null;
        mViewFlags = SOUND_EFFECTS_ENABLED | HAPTIC_FEEDBACK_ENABLED;
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }
    public View(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public View(Context context, AttributeSet attrs, int defStyle) {
        this(context);
        TypedArray a = context.obtainStyledAttributes(attrs, com.android.internal.R.styleable.View,
                defStyle, 0);
        Drawable background = null;
        int leftPadding = -1;
        int topPadding = -1;
        int rightPadding = -1;
        int bottomPadding = -1;
        int padding = -1;
        int viewFlagValues = 0;
        int viewFlagMasks = 0;
        boolean setScrollContainer = false;
        int x = 0;
        int y = 0;
        int scrollbarStyle = SCROLLBARS_INSIDE_OVERLAY;
        final int N = a.getIndexCount();
        for (int i = 0; i < N; i++) {
            int attr = a.getIndex(i);
            switch (attr) {
                case com.android.internal.R.styleable.View_background:
                    background = a.getDrawable(attr);
                    break;
                case com.android.internal.R.styleable.View_padding:
                    padding = a.getDimensionPixelSize(attr, -1);
                    break;
                 case com.android.internal.R.styleable.View_paddingLeft:
                    leftPadding = a.getDimensionPixelSize(attr, -1);
                    break;
                case com.android.internal.R.styleable.View_paddingTop:
                    topPadding = a.getDimensionPixelSize(attr, -1);
                    break;
                case com.android.internal.R.styleable.View_paddingRight:
                    rightPadding = a.getDimensionPixelSize(attr, -1);
                    break;
                case com.android.internal.R.styleable.View_paddingBottom:
                    bottomPadding = a.getDimensionPixelSize(attr, -1);
                    break;
                case com.android.internal.R.styleable.View_scrollX:
                    x = a.getDimensionPixelOffset(attr, 0);
                    break;
                case com.android.internal.R.styleable.View_scrollY:
                    y = a.getDimensionPixelOffset(attr, 0);
                    break;
                case com.android.internal.R.styleable.View_id:
                    mID = a.getResourceId(attr, NO_ID);
                    break;
                case com.android.internal.R.styleable.View_tag:
                    mTag = a.getText(attr);
                    break;
                case com.android.internal.R.styleable.View_fitsSystemWindows:
                    if (a.getBoolean(attr, false)) {
                        viewFlagValues |= FITS_SYSTEM_WINDOWS;
                        viewFlagMasks |= FITS_SYSTEM_WINDOWS;
                    }
                    break;
                case com.android.internal.R.styleable.View_focusable:
                    if (a.getBoolean(attr, false)) {
                        viewFlagValues |= FOCUSABLE;
                        viewFlagMasks |= FOCUSABLE_MASK;
                    }
                    break;
                case com.android.internal.R.styleable.View_focusableInTouchMode:
                    if (a.getBoolean(attr, false)) {
                        viewFlagValues |= FOCUSABLE_IN_TOUCH_MODE | FOCUSABLE;
                        viewFlagMasks |= FOCUSABLE_IN_TOUCH_MODE | FOCUSABLE_MASK;
                    }
                    break;
                case com.android.internal.R.styleable.View_clickable:
                    if (a.getBoolean(attr, false)) {
                        viewFlagValues |= CLICKABLE;
                        viewFlagMasks |= CLICKABLE;
                    }
                    break;
                case com.android.internal.R.styleable.View_longClickable:
                    if (a.getBoolean(attr, false)) {
                        viewFlagValues |= LONG_CLICKABLE;
                        viewFlagMasks |= LONG_CLICKABLE;
                    }
                    break;
                case com.android.internal.R.styleable.View_saveEnabled:
                    if (!a.getBoolean(attr, true)) {
                        viewFlagValues |= SAVE_DISABLED;
                        viewFlagMasks |= SAVE_DISABLED_MASK;
                    }
                    break;
                case com.android.internal.R.styleable.View_duplicateParentState:
                    if (a.getBoolean(attr, false)) {
                        viewFlagValues |= DUPLICATE_PARENT_STATE;
                        viewFlagMasks |= DUPLICATE_PARENT_STATE;
                    }
                    break;
                case com.android.internal.R.styleable.View_visibility:
                    final int visibility = a.getInt(attr, 0);
                    if (visibility != 0) {
                        viewFlagValues |= VISIBILITY_FLAGS[visibility];
                        viewFlagMasks |= VISIBILITY_MASK;
                    }
                    break;
                case com.android.internal.R.styleable.View_drawingCacheQuality:
                    final int cacheQuality = a.getInt(attr, 0);
                    if (cacheQuality != 0) {
                        viewFlagValues |= DRAWING_CACHE_QUALITY_FLAGS[cacheQuality];
                        viewFlagMasks |= DRAWING_CACHE_QUALITY_MASK;
                    }
                    break;
                case com.android.internal.R.styleable.View_contentDescription:
                    mContentDescription = a.getString(attr);
                    break;
                case com.android.internal.R.styleable.View_soundEffectsEnabled:
                    if (!a.getBoolean(attr, true)) {
                        viewFlagValues &= ~SOUND_EFFECTS_ENABLED;
                        viewFlagMasks |= SOUND_EFFECTS_ENABLED;
                    }
                    break;
                case com.android.internal.R.styleable.View_hapticFeedbackEnabled:
                    if (!a.getBoolean(attr, true)) {
                        viewFlagValues &= ~HAPTIC_FEEDBACK_ENABLED;
                        viewFlagMasks |= HAPTIC_FEEDBACK_ENABLED;
                    }
                    break;
                case R.styleable.View_scrollbars:
                    final int scrollbars = a.getInt(attr, SCROLLBARS_NONE);
                    if (scrollbars != SCROLLBARS_NONE) {
                        viewFlagValues |= scrollbars;
                        viewFlagMasks |= SCROLLBARS_MASK;
                        initializeScrollbars(a);
                    }
                    break;
                case R.styleable.View_fadingEdge:
                    final int fadingEdge = a.getInt(attr, FADING_EDGE_NONE);
                    if (fadingEdge != FADING_EDGE_NONE) {
                        viewFlagValues |= fadingEdge;
                        viewFlagMasks |= FADING_EDGE_MASK;
                        initializeFadingEdge(a);
                    }
                    break;
                case R.styleable.View_scrollbarStyle:
                    scrollbarStyle = a.getInt(attr, SCROLLBARS_INSIDE_OVERLAY);
                    if (scrollbarStyle != SCROLLBARS_INSIDE_OVERLAY) {
                        viewFlagValues |= scrollbarStyle & SCROLLBARS_STYLE_MASK;
                        viewFlagMasks |= SCROLLBARS_STYLE_MASK;
                    }
                    break;
                case R.styleable.View_isScrollContainer:
                    setScrollContainer = true;
                    if (a.getBoolean(attr, false)) {
                        setScrollContainer(true);
                    }
                    break;
                case com.android.internal.R.styleable.View_keepScreenOn:
                    if (a.getBoolean(attr, false)) {
                        viewFlagValues |= KEEP_SCREEN_ON;
                        viewFlagMasks |= KEEP_SCREEN_ON;
                    }
                    break;
                case R.styleable.View_nextFocusLeft:
                    mNextFocusLeftId = a.getResourceId(attr, View.NO_ID);
                    break;
                case R.styleable.View_nextFocusRight:
                    mNextFocusRightId = a.getResourceId(attr, View.NO_ID);
                    break;
                case R.styleable.View_nextFocusUp:
                    mNextFocusUpId = a.getResourceId(attr, View.NO_ID);
                    break;
                case R.styleable.View_nextFocusDown:
                    mNextFocusDownId = a.getResourceId(attr, View.NO_ID);
                    break;
                case R.styleable.View_minWidth:
                    mMinWidth = a.getDimensionPixelSize(attr, 0);
                    break;
                case R.styleable.View_minHeight:
                    mMinHeight = a.getDimensionPixelSize(attr, 0);
                    break;
                case R.styleable.View_onClick:
                    if (context.isRestricted()) {
                        throw new IllegalStateException("The android:onClick attribute cannot " 
                                + "be used within a restricted context");
                    }
                    final String handlerName = a.getString(attr);
                    if (handlerName != null) {
                        setOnClickListener(new OnClickListener() {
                            private Method mHandler;
                            public void onClick(View v) {
                                if (mHandler == null) {
                                    try {
                                        mHandler = getContext().getClass().getMethod(handlerName,
                                                View.class);
                                    } catch (NoSuchMethodException e) {
                                        int id = getId();
                                        String idText = id == NO_ID ? "" : " with id '"
                                                + getContext().getResources().getResourceEntryName(
                                                    id) + "'";
                                        throw new IllegalStateException("Could not find a method " +
                                                handlerName + "(View) in the activity "
                                                + getContext().getClass() + " for onClick handler"
                                                + " on view " + View.this.getClass() + idText, e);
                                    }
                                }
                                try {
                                    mHandler.invoke(getContext(), View.this);
                                } catch (IllegalAccessException e) {
                                    throw new IllegalStateException("Could not execute non "
                                            + "public method of the activity", e);
                                } catch (InvocationTargetException e) {
                                    throw new IllegalStateException("Could not execute "
                                            + "method of the activity", e);
                                }
                            }
                        });
                    }
                    break;
            }
        }
        if (background != null) {
            setBackgroundDrawable(background);
        }
        if (padding >= 0) {
            leftPadding = padding;
            topPadding = padding;
            rightPadding = padding;
            bottomPadding = padding;
        }
        setPadding(leftPadding >= 0 ? leftPadding : mPaddingLeft,
                topPadding >= 0 ? topPadding : mPaddingTop,
                rightPadding >= 0 ? rightPadding : mPaddingRight,
                bottomPadding >= 0 ? bottomPadding : mPaddingBottom);
        if (viewFlagMasks != 0) {
            setFlags(viewFlagValues, viewFlagMasks);
        }
        if (scrollbarStyle != SCROLLBARS_INSIDE_OVERLAY) {
            recomputePadding();
        }
        if (x != 0 || y != 0) {
            scrollTo(x, y);
        }
        if (!setScrollContainer && (viewFlagValues&SCROLLBARS_VERTICAL) != 0) {
            setScrollContainer(true);
        }
        computeOpaqueFlags();
        a.recycle();
    }
    View() {
    }
    protected void initializeFadingEdge(TypedArray a) {
        initScrollCache();
        mScrollCache.fadingEdgeLength = a.getDimensionPixelSize(
                R.styleable.View_fadingEdgeLength,
                ViewConfiguration.get(mContext).getScaledFadingEdgeLength());
    }
    public int getVerticalFadingEdgeLength() {
        if (isVerticalFadingEdgeEnabled()) {
            ScrollabilityCache cache = mScrollCache;
            if (cache != null) {
                return cache.fadingEdgeLength;
            }
        }
        return 0;
    }
    public void setFadingEdgeLength(int length) {
        initScrollCache();
        mScrollCache.fadingEdgeLength = length;
    }
    public int getHorizontalFadingEdgeLength() {
        if (isHorizontalFadingEdgeEnabled()) {
            ScrollabilityCache cache = mScrollCache;
            if (cache != null) {
                return cache.fadingEdgeLength;
            }
        }
        return 0;
    }
    public int getVerticalScrollbarWidth() {
        ScrollabilityCache cache = mScrollCache;
        if (cache != null) {
            ScrollBarDrawable scrollBar = cache.scrollBar;
            if (scrollBar != null) {
                int size = scrollBar.getSize(true);
                if (size <= 0) {
                    size = cache.scrollBarSize;
                }
                return size;
            }
            return 0;
        }
        return 0;
    }
    protected int getHorizontalScrollbarHeight() {
        ScrollabilityCache cache = mScrollCache;
        if (cache != null) {
            ScrollBarDrawable scrollBar = cache.scrollBar;
            if (scrollBar != null) {
                int size = scrollBar.getSize(false);
                if (size <= 0) {
                    size = cache.scrollBarSize;
                }
                return size;
            }
            return 0;
        }
        return 0;
    }
    protected void initializeScrollbars(TypedArray a) {
        initScrollCache();
        final ScrollabilityCache scrollabilityCache = mScrollCache;
        if (scrollabilityCache.scrollBar == null) {
            scrollabilityCache.scrollBar = new ScrollBarDrawable();
        }
        final boolean fadeScrollbars = a.getBoolean(R.styleable.View_fadeScrollbars, true);
        if (!fadeScrollbars) {
            scrollabilityCache.state = ScrollabilityCache.ON;
        }
        scrollabilityCache.fadeScrollBars = fadeScrollbars;
        scrollabilityCache.scrollBarFadeDuration = a.getInt(
                R.styleable.View_scrollbarFadeDuration, ViewConfiguration
                        .getScrollBarFadeDuration());
        scrollabilityCache.scrollBarDefaultDelayBeforeFade = a.getInt(
                R.styleable.View_scrollbarDefaultDelayBeforeFade,
                ViewConfiguration.getScrollDefaultDelay());
        scrollabilityCache.scrollBarSize = a.getDimensionPixelSize(
                com.android.internal.R.styleable.View_scrollbarSize,
                ViewConfiguration.get(mContext).getScaledScrollBarSize());
        Drawable track = a.getDrawable(R.styleable.View_scrollbarTrackHorizontal);
        scrollabilityCache.scrollBar.setHorizontalTrackDrawable(track);
        Drawable thumb = a.getDrawable(R.styleable.View_scrollbarThumbHorizontal);
        if (thumb != null) {
            scrollabilityCache.scrollBar.setHorizontalThumbDrawable(thumb);
        }
        boolean alwaysDraw = a.getBoolean(R.styleable.View_scrollbarAlwaysDrawHorizontalTrack,
                false);
        if (alwaysDraw) {
            scrollabilityCache.scrollBar.setAlwaysDrawHorizontalTrack(true);
        }
        track = a.getDrawable(R.styleable.View_scrollbarTrackVertical);
        scrollabilityCache.scrollBar.setVerticalTrackDrawable(track);
        thumb = a.getDrawable(R.styleable.View_scrollbarThumbVertical);
        if (thumb != null) {
            scrollabilityCache.scrollBar.setVerticalThumbDrawable(thumb);
        }
        alwaysDraw = a.getBoolean(R.styleable.View_scrollbarAlwaysDrawVerticalTrack,
                false);
        if (alwaysDraw) {
            scrollabilityCache.scrollBar.setAlwaysDrawVerticalTrack(true);
        }
        recomputePadding();
    }
    private void initScrollCache() {
        if (mScrollCache == null) {
            mScrollCache = new ScrollabilityCache(ViewConfiguration.get(mContext), this);
        }
    }
    public void setOnFocusChangeListener(OnFocusChangeListener l) {
        mOnFocusChangeListener = l;
    }
    public OnFocusChangeListener getOnFocusChangeListener() {
        return mOnFocusChangeListener;
    }
    public void setOnClickListener(OnClickListener l) {
        if (!isClickable()) {
            setClickable(true);
        }
        mOnClickListener = l;
    }
    public void setOnLongClickListener(OnLongClickListener l) {
        if (!isLongClickable()) {
            setLongClickable(true);
        }
        mOnLongClickListener = l;
    }
    public void setOnCreateContextMenuListener(OnCreateContextMenuListener l) {
        if (!isLongClickable()) {
            setLongClickable(true);
        }
        mOnCreateContextMenuListener = l;
    }
    public boolean performClick() {
        sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_CLICKED);
        if (mOnClickListener != null) {
            playSoundEffect(SoundEffectConstants.CLICK);
            mOnClickListener.onClick(this);
            return true;
        }
        return false;
    }
    public boolean performLongClick() {
        sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_LONG_CLICKED);
        boolean handled = false;
        if (mOnLongClickListener != null) {
            handled = mOnLongClickListener.onLongClick(View.this);
        }
        if (!handled) {
            handled = showContextMenu();
        }
        if (handled) {
            performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
        }
        return handled;
    }
    public boolean showContextMenu() {
        return getParent().showContextMenuForChild(this);
    }
    public void setOnKeyListener(OnKeyListener l) {
        mOnKeyListener = l;
    }
    public void setOnTouchListener(OnTouchListener l) {
        mOnTouchListener = l;
    }
    void handleFocusGainInternal(int direction, Rect previouslyFocusedRect) {
        if (DBG) {
            System.out.println(this + " requestFocus()");
        }
        if ((mPrivateFlags & FOCUSED) == 0) {
            mPrivateFlags |= FOCUSED;
            if (mParent != null) {
                mParent.requestChildFocus(this, this);
            }
            onFocusChanged(true, direction, previouslyFocusedRect);
            refreshDrawableState();
        }
    }
    public boolean requestRectangleOnScreen(Rect rectangle) {
        return requestRectangleOnScreen(rectangle, false);
    }
    public boolean requestRectangleOnScreen(Rect rectangle, boolean immediate) {
        View child = this;
        ViewParent parent = mParent;
        boolean scrolled = false;
        while (parent != null) {
            scrolled |= parent.requestChildRectangleOnScreen(child,
                    rectangle, immediate);
            rectangle.offset(child.getLeft(), child.getTop());
            rectangle.offset(-child.getScrollX(), -child.getScrollY());
            if (!(parent instanceof View)) {
                break;
            }
            child = (View) parent;
            parent = child.getParent();
        }
        return scrolled;
    }
    public void clearFocus() {
        if (DBG) {
            System.out.println(this + " clearFocus()");
        }
        if ((mPrivateFlags & FOCUSED) != 0) {
            mPrivateFlags &= ~FOCUSED;
            if (mParent != null) {
                mParent.clearChildFocus(this);
            }
            onFocusChanged(false, 0, null);
            refreshDrawableState();
        }
    }
    void clearFocusForRemoval() {
        if ((mPrivateFlags & FOCUSED) != 0) {
            mPrivateFlags &= ~FOCUSED;
            onFocusChanged(false, 0, null);
            refreshDrawableState();
        }
    }
    void unFocus() {
        if (DBG) {
            System.out.println(this + " unFocus()");
        }
        if ((mPrivateFlags & FOCUSED) != 0) {
            mPrivateFlags &= ~FOCUSED;
            onFocusChanged(false, 0, null);
            refreshDrawableState();
        }
    }
    @ViewDebug.ExportedProperty
    public boolean hasFocus() {
        return (mPrivateFlags & FOCUSED) != 0;
    }
    public boolean hasFocusable() {
        return (mViewFlags & VISIBILITY_MASK) == VISIBLE && isFocusable();
    }
    protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
        if (gainFocus) {
            sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED);
        }
        InputMethodManager imm = InputMethodManager.peekInstance();
        if (!gainFocus) {
            if (isPressed()) {
                setPressed(false);
            }
            if (imm != null && mAttachInfo != null
                    && mAttachInfo.mHasWindowFocus) {
                imm.focusOut(this);
            }
            onFocusLost();
        } else if (imm != null && mAttachInfo != null
                && mAttachInfo.mHasWindowFocus) {
            imm.focusIn(this);
        }
        invalidate();
        if (mOnFocusChangeListener != null) {
            mOnFocusChangeListener.onFocusChange(this, gainFocus);
        }
        if (mAttachInfo != null) {
            mAttachInfo.mKeyDispatchState.reset(this);
        }
    }
    public void sendAccessibilityEvent(int eventType) {
        if (AccessibilityManager.getInstance(mContext).isEnabled()) {
            sendAccessibilityEventUnchecked(AccessibilityEvent.obtain(eventType));
        }
    }
    public void sendAccessibilityEventUnchecked(AccessibilityEvent event) {
        event.setClassName(getClass().getName());
        event.setPackageName(getContext().getPackageName());
        event.setEnabled(isEnabled());
        event.setContentDescription(mContentDescription);
        if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_FOCUSED && mAttachInfo != null) {
            ArrayList<View> focusablesTempList = mAttachInfo.mFocusablesTempList;
            getRootView().addFocusables(focusablesTempList, View.FOCUS_FORWARD, FOCUSABLES_ALL);
            event.setItemCount(focusablesTempList.size());
            event.setCurrentItemIndex(focusablesTempList.indexOf(this));
            focusablesTempList.clear();
        }
        dispatchPopulateAccessibilityEvent(event);
        AccessibilityManager.getInstance(mContext).sendAccessibilityEvent(event);
    }
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        return false;
    }
    public CharSequence getContentDescription() {
        return mContentDescription;
    }
    public void setContentDescription(CharSequence contentDescription) {
        mContentDescription = contentDescription;
    }
    protected void onFocusLost() {
        resetPressedState();
    }
    private void resetPressedState() {
        if ((mViewFlags & ENABLED_MASK) == DISABLED) {
            return;
        }
        if (isPressed()) {
            setPressed(false);
            if (!mHasPerformedLongPress) {
                removeLongPressCallback();
            }
        }
    }
    @ViewDebug.ExportedProperty
    public boolean isFocused() {
        return (mPrivateFlags & FOCUSED) != 0;
    }
    public View findFocus() {
        return (mPrivateFlags & FOCUSED) != 0 ? this : null;
    }
    public void setScrollContainer(boolean isScrollContainer) {
        if (isScrollContainer) {
            if (mAttachInfo != null && (mPrivateFlags&SCROLL_CONTAINER_ADDED) == 0) {
                mAttachInfo.mScrollContainers.add(this);
                mPrivateFlags |= SCROLL_CONTAINER_ADDED;
            }
            mPrivateFlags |= SCROLL_CONTAINER;
        } else {
            if ((mPrivateFlags&SCROLL_CONTAINER_ADDED) != 0) {
                mAttachInfo.mScrollContainers.remove(this);
            }
            mPrivateFlags &= ~(SCROLL_CONTAINER|SCROLL_CONTAINER_ADDED);
        }
    }
    public int getDrawingCacheQuality() {
        return mViewFlags & DRAWING_CACHE_QUALITY_MASK;
    }
    public void setDrawingCacheQuality(int quality) {
        setFlags(quality, DRAWING_CACHE_QUALITY_MASK);
    }
    public boolean getKeepScreenOn() {
        return (mViewFlags & KEEP_SCREEN_ON) != 0;
    }
    public void setKeepScreenOn(boolean keepScreenOn) {
        setFlags(keepScreenOn ? KEEP_SCREEN_ON : 0, KEEP_SCREEN_ON);
    }
    public int getNextFocusLeftId() {
        return mNextFocusLeftId;
    }
    public void setNextFocusLeftId(int nextFocusLeftId) {
        mNextFocusLeftId = nextFocusLeftId;
    }
    public int getNextFocusRightId() {
        return mNextFocusRightId;
    }
    public void setNextFocusRightId(int nextFocusRightId) {
        mNextFocusRightId = nextFocusRightId;
    }
    public int getNextFocusUpId() {
        return mNextFocusUpId;
    }
    public void setNextFocusUpId(int nextFocusUpId) {
        mNextFocusUpId = nextFocusUpId;
    }
    public int getNextFocusDownId() {
        return mNextFocusDownId;
    }
    public void setNextFocusDownId(int nextFocusDownId) {
        mNextFocusDownId = nextFocusDownId;
    }
    public boolean isShown() {
        View current = this;
        do {
            if ((current.mViewFlags & VISIBILITY_MASK) != VISIBLE) {
                return false;
            }
            ViewParent parent = current.mParent;
            if (parent == null) {
                return false; 
            }
            if (!(parent instanceof View)) {
                return true;
            }
            current = (View) parent;
        } while (current != null);
        return false;
    }
    protected boolean fitSystemWindows(Rect insets) {
        if ((mViewFlags & FITS_SYSTEM_WINDOWS) == FITS_SYSTEM_WINDOWS) {
            mPaddingLeft = insets.left;
            mPaddingTop = insets.top;
            mPaddingRight = insets.right;
            mPaddingBottom = insets.bottom;
            requestLayout();
            return true;
        }
        return false;
    }
    @ViewDebug.ExportedProperty(mapping = {
        @ViewDebug.IntToString(from = VISIBLE,   to = "VISIBLE"),
        @ViewDebug.IntToString(from = INVISIBLE, to = "INVISIBLE"),
        @ViewDebug.IntToString(from = GONE,      to = "GONE")
    })
    public int getVisibility() {
        return mViewFlags & VISIBILITY_MASK;
    }
    @RemotableViewMethod
    public void setVisibility(int visibility) {
        setFlags(visibility, VISIBILITY_MASK);
        if (mBGDrawable != null) mBGDrawable.setVisible(visibility == VISIBLE, false);
    }
    @ViewDebug.ExportedProperty
    public boolean isEnabled() {
        return (mViewFlags & ENABLED_MASK) == ENABLED;
    }
    @RemotableViewMethod
    public void setEnabled(boolean enabled) {
        if (enabled == isEnabled()) return;
        setFlags(enabled ? ENABLED : DISABLED, ENABLED_MASK);
        refreshDrawableState();
        invalidate();
    }
    public void setFocusable(boolean focusable) {
        if (!focusable) {
            setFlags(0, FOCUSABLE_IN_TOUCH_MODE);
        }
        setFlags(focusable ? FOCUSABLE : NOT_FOCUSABLE, FOCUSABLE_MASK);
    }
    public void setFocusableInTouchMode(boolean focusableInTouchMode) {
        setFlags(focusableInTouchMode ? FOCUSABLE_IN_TOUCH_MODE : 0, FOCUSABLE_IN_TOUCH_MODE);
        if (focusableInTouchMode) {
            setFlags(FOCUSABLE, FOCUSABLE_MASK);
        }
    }
    public void setSoundEffectsEnabled(boolean soundEffectsEnabled) {
        setFlags(soundEffectsEnabled ? SOUND_EFFECTS_ENABLED: 0, SOUND_EFFECTS_ENABLED);
    }
    @ViewDebug.ExportedProperty
    public boolean isSoundEffectsEnabled() {
        return SOUND_EFFECTS_ENABLED == (mViewFlags & SOUND_EFFECTS_ENABLED);
    }
    public void setHapticFeedbackEnabled(boolean hapticFeedbackEnabled) {
        setFlags(hapticFeedbackEnabled ? HAPTIC_FEEDBACK_ENABLED: 0, HAPTIC_FEEDBACK_ENABLED);
    }
    @ViewDebug.ExportedProperty
    public boolean isHapticFeedbackEnabled() {
        return HAPTIC_FEEDBACK_ENABLED == (mViewFlags & HAPTIC_FEEDBACK_ENABLED);
    }
    public void setWillNotDraw(boolean willNotDraw) {
        setFlags(willNotDraw ? WILL_NOT_DRAW : 0, DRAW_MASK);
    }
    @ViewDebug.ExportedProperty
    public boolean willNotDraw() {
        return (mViewFlags & DRAW_MASK) == WILL_NOT_DRAW;
    }
    public void setWillNotCacheDrawing(boolean willNotCacheDrawing) {
        setFlags(willNotCacheDrawing ? WILL_NOT_CACHE_DRAWING : 0, WILL_NOT_CACHE_DRAWING);
    }
    @ViewDebug.ExportedProperty
    public boolean willNotCacheDrawing() {
        return (mViewFlags & WILL_NOT_CACHE_DRAWING) == WILL_NOT_CACHE_DRAWING;
    }
    @ViewDebug.ExportedProperty
    public boolean isClickable() {
        return (mViewFlags & CLICKABLE) == CLICKABLE;
    }
    public void setClickable(boolean clickable) {
        setFlags(clickable ? CLICKABLE : 0, CLICKABLE);
    }
    public boolean isLongClickable() {
        return (mViewFlags & LONG_CLICKABLE) == LONG_CLICKABLE;
    }
    public void setLongClickable(boolean longClickable) {
        setFlags(longClickable ? LONG_CLICKABLE : 0, LONG_CLICKABLE);
    }
    public void setPressed(boolean pressed) {
        if (pressed) {
            mPrivateFlags |= PRESSED;
        } else {
            mPrivateFlags &= ~PRESSED;
        }
        refreshDrawableState();
        dispatchSetPressed(pressed);
    }
    protected void dispatchSetPressed(boolean pressed) {
    }
    public boolean isPressed() {
        return (mPrivateFlags & PRESSED) == PRESSED;
    }
    public boolean isSaveEnabled() {
        return (mViewFlags & SAVE_DISABLED_MASK) != SAVE_DISABLED;
    }
    public void setSaveEnabled(boolean enabled) {
        setFlags(enabled ? 0 : SAVE_DISABLED, SAVE_DISABLED_MASK);
    }
    @ViewDebug.ExportedProperty
    public final boolean isFocusable() {
        return FOCUSABLE == (mViewFlags & FOCUSABLE_MASK);
    }
    @ViewDebug.ExportedProperty
    public final boolean isFocusableInTouchMode() {
        return FOCUSABLE_IN_TOUCH_MODE == (mViewFlags & FOCUSABLE_IN_TOUCH_MODE);
    }
    public View focusSearch(int direction) {
        if (mParent != null) {
            return mParent.focusSearch(this, direction);
        } else {
            return null;
        }
    }
    public boolean dispatchUnhandledMove(View focused, int direction) {
        return false;
    }
    View findUserSetNextFocus(View root, int direction) {
        switch (direction) {
            case FOCUS_LEFT:
                if (mNextFocusLeftId == View.NO_ID) return null;
                return findViewShouldExist(root, mNextFocusLeftId);
            case FOCUS_RIGHT:
                if (mNextFocusRightId == View.NO_ID) return null;
                return findViewShouldExist(root, mNextFocusRightId);
            case FOCUS_UP:
                if (mNextFocusUpId == View.NO_ID) return null;
                return findViewShouldExist(root, mNextFocusUpId);
            case FOCUS_DOWN:
                if (mNextFocusDownId == View.NO_ID) return null;
                return findViewShouldExist(root, mNextFocusDownId);
        }
        return null;
    }
    private static View findViewShouldExist(View root, int childViewId) {
        View result = root.findViewById(childViewId);
        if (result == null) {
            Log.w(VIEW_LOG_TAG, "couldn't find next focus view specified "
                    + "by user for id " + childViewId);
        }
        return result;
    }
    public ArrayList<View> getFocusables(int direction) {
        ArrayList<View> result = new ArrayList<View>(24);
        addFocusables(result, direction);
        return result;
    }
    public void addFocusables(ArrayList<View> views, int direction) {
        addFocusables(views, direction, FOCUSABLES_TOUCH_MODE);
    }
    public void addFocusables(ArrayList<View> views, int direction, int focusableMode) {
        if (!isFocusable()) {
            return;
        }
        if ((focusableMode & FOCUSABLES_TOUCH_MODE) == FOCUSABLES_TOUCH_MODE &&
                isInTouchMode() && !isFocusableInTouchMode()) {
            return;
        }
        if (views != null) {
            views.add(this);
        }
    }
    public ArrayList<View> getTouchables() {
        ArrayList<View> result = new ArrayList<View>();
        addTouchables(result);
        return result;
    }
    public void addTouchables(ArrayList<View> views) {
        final int viewFlags = mViewFlags;
        if (((viewFlags & CLICKABLE) == CLICKABLE || (viewFlags & LONG_CLICKABLE) == LONG_CLICKABLE)
                && (viewFlags & ENABLED_MASK) == ENABLED) {
            views.add(this);
        }
    }
    public final boolean requestFocus() {
        return requestFocus(View.FOCUS_DOWN);
    }
    public final boolean requestFocus(int direction) {
        return requestFocus(direction, null);
    }
    public boolean requestFocus(int direction, Rect previouslyFocusedRect) {
        if ((mViewFlags & FOCUSABLE_MASK) != FOCUSABLE ||
                (mViewFlags & VISIBILITY_MASK) != VISIBLE) {
            return false;
        }
        if (isInTouchMode() &&
                (FOCUSABLE_IN_TOUCH_MODE != (mViewFlags & FOCUSABLE_IN_TOUCH_MODE))) {
            return false;
        }
        if (hasAncestorThatBlocksDescendantFocus()) {
            return false;
        }
        handleFocusGainInternal(direction, previouslyFocusedRect);
        return true;
    }
    public final boolean requestFocusFromTouch() {
        if (isInTouchMode()) {
            View root = getRootView();
            if (root != null) {
               ViewRoot viewRoot = (ViewRoot)root.getParent();
               if (viewRoot != null) {
                   viewRoot.ensureTouchMode(false);
               }
            }
        }
        return requestFocus(View.FOCUS_DOWN);
    }
    private boolean hasAncestorThatBlocksDescendantFocus() {
        ViewParent ancestor = mParent;
        while (ancestor instanceof ViewGroup) {
            final ViewGroup vgAncestor = (ViewGroup) ancestor;
            if (vgAncestor.getDescendantFocusability() == ViewGroup.FOCUS_BLOCK_DESCENDANTS) {
                return true;
            } else {
                ancestor = vgAncestor.getParent();
            }
        }
        return false;
    }
    public void dispatchStartTemporaryDetach() {
        onStartTemporaryDetach();
    }
    public void onStartTemporaryDetach() {
        removeUnsetPressCallback();
        mPrivateFlags |= CANCEL_NEXT_UP_EVENT;
    }
    public void dispatchFinishTemporaryDetach() {
        onFinishTemporaryDetach();
    }
    public void onFinishTemporaryDetach() {
    }
    private static void captureViewInfo(String subTag, View v) {
        if (v == null || SystemProperties.getInt(ViewDebug.SYSTEM_PROPERTY_CAPTURE_VIEW, 0) == 0) {
            return;
        }
        ViewDebug.dumpCapturedView(subTag, v);
    }
    public KeyEvent.DispatcherState getKeyDispatcherState() {
        return mAttachInfo != null ? mAttachInfo.mKeyDispatchState : null;
    }
    public boolean dispatchKeyEventPreIme(KeyEvent event) {
        return onKeyPreIme(event.getKeyCode(), event);
    }
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (android.util.Config.LOGV) {
            captureViewInfo("captureViewKeyEvent", this);
        }
        if (mOnKeyListener != null && (mViewFlags & ENABLED_MASK) == ENABLED
                && mOnKeyListener.onKey(this, event.getKeyCode(), event)) {
            return true;
        }
        return event.dispatch(this, mAttachInfo != null
                ? mAttachInfo.mKeyDispatchState : null, this);
    }
    public boolean dispatchKeyShortcutEvent(KeyEvent event) {
        return onKeyShortcut(event.getKeyCode(), event);
    }
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (mOnTouchListener != null && (mViewFlags & ENABLED_MASK) == ENABLED &&
                mOnTouchListener.onTouch(this, event)) {
            return true;
        }
        return onTouchEvent(event);
    }
    public boolean dispatchTrackballEvent(MotionEvent event) {
        return onTrackballEvent(event);
    }
    public void dispatchWindowFocusChanged(boolean hasFocus) {
        onWindowFocusChanged(hasFocus);
    }
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        InputMethodManager imm = InputMethodManager.peekInstance();
        if (!hasWindowFocus) {
            if (isPressed()) {
                setPressed(false);
            }
            if (imm != null && (mPrivateFlags & FOCUSED) != 0) {
                imm.focusOut(this);
            }
            removeLongPressCallback();
            onFocusLost();
        } else if (imm != null && (mPrivateFlags & FOCUSED) != 0) {
            imm.focusIn(this);
        }
        refreshDrawableState();
    }
    public boolean hasWindowFocus() {
        return mAttachInfo != null && mAttachInfo.mHasWindowFocus;
    }
    protected void dispatchVisibilityChanged(View changedView, int visibility) {
        onVisibilityChanged(changedView, visibility);
    }
    protected void onVisibilityChanged(View changedView, int visibility) {
        if (visibility == VISIBLE) {
            if (mAttachInfo != null) {
                initialAwakenScrollBars();
            } else {
                mPrivateFlags |= AWAKEN_SCROLL_BARS_ON_ATTACH;
            }
        }
    }
    public void dispatchDisplayHint(int hint) {
        onDisplayHint(hint);
    }
    protected void onDisplayHint(int hint) {
    }
    public void dispatchWindowVisibilityChanged(int visibility) {
        onWindowVisibilityChanged(visibility);
    }
    protected void onWindowVisibilityChanged(int visibility) {
        if (visibility == VISIBLE) {
            initialAwakenScrollBars();
        }
    }
    public int getWindowVisibility() {
        return mAttachInfo != null ? mAttachInfo.mWindowVisibility : GONE;
    }
    public void getWindowVisibleDisplayFrame(Rect outRect) {
        if (mAttachInfo != null) {
            try {
                mAttachInfo.mSession.getDisplayFrame(mAttachInfo.mWindow, outRect);
            } catch (RemoteException e) {
                return;
            }
            final Rect insets = mAttachInfo.mVisibleInsets;
            outRect.left += insets.left;
            outRect.top += insets.top;
            outRect.right -= insets.right;
            outRect.bottom -= insets.bottom;
            return;
        }
        Display d = WindowManagerImpl.getDefault().getDefaultDisplay();
        outRect.set(0, 0, d.getWidth(), d.getHeight());
    }
    public void dispatchConfigurationChanged(Configuration newConfig) {
        onConfigurationChanged(newConfig);
    }
    protected void onConfigurationChanged(Configuration newConfig) {
    }
    void dispatchCollectViewAttributes(int visibility) {
        performCollectViewAttributes(visibility);
    }
    void performCollectViewAttributes(int visibility) {
        if (((visibility | mViewFlags) & (VISIBILITY_MASK | KEEP_SCREEN_ON))
                == (VISIBLE | KEEP_SCREEN_ON)) {
            mAttachInfo.mKeepScreenOn = true;
        }
    }
    void needGlobalAttributesUpdate(boolean force) {
        AttachInfo ai = mAttachInfo;
        if (ai != null) {
            if (ai.mKeepScreenOn || force) {
                ai.mRecomputeGlobalAttributes = true;
            }
        }
    }
    @ViewDebug.ExportedProperty
    public boolean isInTouchMode() {
        if (mAttachInfo != null) {
            return mAttachInfo.mInTouchMode;
        } else {
            return ViewRoot.isInTouchMode();
        }
    }
    @ViewDebug.CapturedViewProperty
    public final Context getContext() {
        return mContext;
    }
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        return false;
    }
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean result = false;
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER: {
                if ((mViewFlags & ENABLED_MASK) == DISABLED) {
                    return true;
                }
                if (((mViewFlags & CLICKABLE) == CLICKABLE ||
                        (mViewFlags & LONG_CLICKABLE) == LONG_CLICKABLE) &&
                        (event.getRepeatCount() == 0)) {
                    setPressed(true);
                    if ((mViewFlags & LONG_CLICKABLE) == LONG_CLICKABLE) {
                        postCheckForLongClick(0);
                    }
                    return true;
                }
                break;
            }
        }
        return result;
    }
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        return false;
    }
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        boolean result = false;
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER: {
                if ((mViewFlags & ENABLED_MASK) == DISABLED) {
                    return true;
                }
                if ((mViewFlags & CLICKABLE) == CLICKABLE && isPressed()) {
                    setPressed(false);
                    if (!mHasPerformedLongPress) {
                        removeLongPressCallback();
                        result = performClick();
                    }
                }
                break;
            }
        }
        return result;
    }
    public boolean onKeyMultiple(int keyCode, int repeatCount, KeyEvent event) {
        return false;
    }
    public boolean onKeyShortcut(int keyCode, KeyEvent event) {
        return false;
    }
    public boolean onCheckIsTextEditor() {
        return false;
    }
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        return null;
    }
    public boolean checkInputConnectionProxy(View view) {
        return false;
    }
    public void createContextMenu(ContextMenu menu) {
        ContextMenuInfo menuInfo = getContextMenuInfo();
        ((MenuBuilder)menu).setCurrentMenuInfo(menuInfo);
        onCreateContextMenu(menu);
        if (mOnCreateContextMenuListener != null) {
            mOnCreateContextMenuListener.onCreateContextMenu(menu, this, menuInfo);
        }
        ((MenuBuilder)menu).setCurrentMenuInfo(null);
        if (mParent != null) {
            mParent.createContextMenu(menu);
        }
    }
    protected ContextMenuInfo getContextMenuInfo() {
        return null;
    }
    protected void onCreateContextMenu(ContextMenu menu) {
    }
    public boolean onTrackballEvent(MotionEvent event) {
        return false;
    }
    public boolean onTouchEvent(MotionEvent event) {
        final int viewFlags = mViewFlags;
        if ((viewFlags & ENABLED_MASK) == DISABLED) {
            return (((viewFlags & CLICKABLE) == CLICKABLE ||
                    (viewFlags & LONG_CLICKABLE) == LONG_CLICKABLE));
        }
        if (mTouchDelegate != null) {
            if (mTouchDelegate.onTouchEvent(event)) {
                return true;
            }
        }
        if (((viewFlags & CLICKABLE) == CLICKABLE ||
                (viewFlags & LONG_CLICKABLE) == LONG_CLICKABLE)) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_UP:
                    boolean prepressed = (mPrivateFlags & PREPRESSED) != 0;
                    if ((mPrivateFlags & PRESSED) != 0 || prepressed) {
                        boolean focusTaken = false;
                        if (isFocusable() && isFocusableInTouchMode() && !isFocused()) {
                            focusTaken = requestFocus();
                        }
                        if (!mHasPerformedLongPress) {
                            removeLongPressCallback();
                            if (!focusTaken) {
                                if (mPerformClick == null) {
                                    mPerformClick = new PerformClick();
                                }
                                if (!post(mPerformClick)) {
                                    performClick();
                                }
                            }
                        }
                        if (mUnsetPressedState == null) {
                            mUnsetPressedState = new UnsetPressedState();
                        }
                        if (prepressed) {
                            mPrivateFlags |= PRESSED;
                            refreshDrawableState();
                            postDelayed(mUnsetPressedState,
                                    ViewConfiguration.getPressedStateDuration());
                        } else if (!post(mUnsetPressedState)) {
                            mUnsetPressedState.run();
                        }
                        removeTapCallback();
                    }
                    break;
                case MotionEvent.ACTION_DOWN:
                    if (mPendingCheckForTap == null) {
                        mPendingCheckForTap = new CheckForTap();
                    }
                    mPrivateFlags |= PREPRESSED;
                    mHasPerformedLongPress = false;
                    postDelayed(mPendingCheckForTap, ViewConfiguration.getTapTimeout());
                    break;
                case MotionEvent.ACTION_CANCEL:
                    mPrivateFlags &= ~PRESSED;
                    refreshDrawableState();
                    removeTapCallback();
                    break;
                case MotionEvent.ACTION_MOVE:
                    final int x = (int) event.getX();
                    final int y = (int) event.getY();
                    int slop = mTouchSlop;
                    if ((x < 0 - slop) || (x >= getWidth() + slop) ||
                            (y < 0 - slop) || (y >= getHeight() + slop)) {
                        removeTapCallback();
                        if ((mPrivateFlags & PRESSED) != 0) {
                            removeLongPressCallback();
                            mPrivateFlags &= ~PRESSED;
                            refreshDrawableState();
                        }
                    }
                    break;
            }
            return true;
        }
        return false;
    }
    private void removeLongPressCallback() {
        if (mPendingCheckForLongPress != null) {
          removeCallbacks(mPendingCheckForLongPress);
        }
    }
    private void removeUnsetPressCallback() {
        if ((mPrivateFlags & PRESSED) != 0 && mUnsetPressedState != null) {
            setPressed(false);
            removeCallbacks(mUnsetPressedState);
        }
    }
    private void removeTapCallback() {
        if (mPendingCheckForTap != null) {
            mPrivateFlags &= ~PREPRESSED;
            removeCallbacks(mPendingCheckForTap);
        }
    }
    public void cancelLongPress() {
        removeLongPressCallback();
        removeTapCallback();
    }
    public void setTouchDelegate(TouchDelegate delegate) {
        mTouchDelegate = delegate;
    }
    public TouchDelegate getTouchDelegate() {
        return mTouchDelegate;
    }
    void setFlags(int flags, int mask) {
        int old = mViewFlags;
        mViewFlags = (mViewFlags & ~mask) | (flags & mask);
        int changed = mViewFlags ^ old;
        if (changed == 0) {
            return;
        }
        int privateFlags = mPrivateFlags;
        if (((changed & FOCUSABLE_MASK) != 0) &&
                ((privateFlags & HAS_BOUNDS) !=0)) {
            if (((old & FOCUSABLE_MASK) == FOCUSABLE)
                    && ((privateFlags & FOCUSED) != 0)) {
                clearFocus();
            } else if (((old & FOCUSABLE_MASK) == NOT_FOCUSABLE)
                    && ((privateFlags & FOCUSED) == 0)) {
                if (mParent != null) mParent.focusableViewAvailable(this);
            }
        }
        if ((flags & VISIBILITY_MASK) == VISIBLE) {
            if ((changed & VISIBILITY_MASK) != 0) {
                mPrivateFlags |= DRAWN;
                needGlobalAttributesUpdate(true);
                if ((mParent != null) && (mBottom > mTop) && (mRight > mLeft)) {
                    mParent.focusableViewAvailable(this);
                }
            }
        }
        if ((changed & GONE) != 0) {
            needGlobalAttributesUpdate(false);
            requestLayout();
            invalidate();
            if (((mViewFlags & VISIBILITY_MASK) == GONE)) {
                if (hasFocus()) clearFocus();
                destroyDrawingCache();
            }
            if (mAttachInfo != null) {
                mAttachInfo.mViewVisibilityChanged = true;
            }
        }
        if ((changed & INVISIBLE) != 0) {
            needGlobalAttributesUpdate(false);
            invalidate();
            if (((mViewFlags & VISIBILITY_MASK) == INVISIBLE) && hasFocus()) {
                if (getRootView() != this) {
                    clearFocus();
                }
            }
            if (mAttachInfo != null) {
                mAttachInfo.mViewVisibilityChanged = true;
            }
        }
        if ((changed & VISIBILITY_MASK) != 0) {
            dispatchVisibilityChanged(this, (flags & VISIBILITY_MASK));
        }
        if ((changed & WILL_NOT_CACHE_DRAWING) != 0) {
            destroyDrawingCache();
        }
        if ((changed & DRAWING_CACHE_ENABLED) != 0) {
            destroyDrawingCache();
            mPrivateFlags &= ~DRAWING_CACHE_VALID;
        }
        if ((changed & DRAWING_CACHE_QUALITY_MASK) != 0) {
            destroyDrawingCache();
            mPrivateFlags &= ~DRAWING_CACHE_VALID;
        }
        if ((changed & DRAW_MASK) != 0) {
            if ((mViewFlags & WILL_NOT_DRAW) != 0) {
                if (mBGDrawable != null) {
                    mPrivateFlags &= ~SKIP_DRAW;
                    mPrivateFlags |= ONLY_DRAWS_BACKGROUND;
                } else {
                    mPrivateFlags |= SKIP_DRAW;
                }
            } else {
                mPrivateFlags &= ~SKIP_DRAW;
            }
            requestLayout();
            invalidate();
        }
        if ((changed & KEEP_SCREEN_ON) != 0) {
            if (mParent != null) {
                mParent.recomputeViewAttributes(this);
            }
        }
    }
    public void bringToFront() {
        if (mParent != null) {
            mParent.bringChildToFront(this);
        }
    }
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        mBackgroundSizeChanged = true;
        final AttachInfo ai = mAttachInfo;
        if (ai != null) {
            ai.mViewScrollChanged = true;
        }
    }
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    }
    protected void dispatchDraw(Canvas canvas) {
    }
    public final ViewParent getParent() {
        return mParent;
    }
    public final int getScrollX() {
        return mScrollX;
    }
    public final int getScrollY() {
        return mScrollY;
    }
    @ViewDebug.ExportedProperty
    public final int getWidth() {
        return mRight - mLeft;
    }
    @ViewDebug.ExportedProperty
    public final int getHeight() {
        return mBottom - mTop;
    }
    public void getDrawingRect(Rect outRect) {
        outRect.left = mScrollX;
        outRect.top = mScrollY;
        outRect.right = mScrollX + (mRight - mLeft);
        outRect.bottom = mScrollY + (mBottom - mTop);
    }
    public final int getMeasuredWidth() {
        return mMeasuredWidth;
    }
    public final int getMeasuredHeight() {
        return mMeasuredHeight;
    }
    @ViewDebug.CapturedViewProperty
    public final int getTop() {
        return mTop;
    }
    @ViewDebug.CapturedViewProperty
    public final int getBottom() {
        return mBottom;
    }
    @ViewDebug.CapturedViewProperty
    public final int getLeft() {
        return mLeft;
    }
    @ViewDebug.CapturedViewProperty
    public final int getRight() {
        return mRight;
    }
    public void getHitRect(Rect outRect) {
        outRect.set(mLeft, mTop, mRight, mBottom);
    }
    public void getFocusedRect(Rect r) {
        getDrawingRect(r);
    }
    public boolean getGlobalVisibleRect(Rect r, Point globalOffset) {
        int width = mRight - mLeft;
        int height = mBottom - mTop;
        if (width > 0 && height > 0) {
            r.set(0, 0, width, height);
            if (globalOffset != null) {
                globalOffset.set(-mScrollX, -mScrollY);
            }
            return mParent == null || mParent.getChildVisibleRect(this, r, globalOffset);
        }
        return false;
    }
    public final boolean getGlobalVisibleRect(Rect r) {
        return getGlobalVisibleRect(r, null);
    }
    public final boolean getLocalVisibleRect(Rect r) {
        Point offset = new Point();
        if (getGlobalVisibleRect(r, offset)) {
            r.offset(-offset.x, -offset.y); 
            return true;
        }
        return false;
    }
    public void offsetTopAndBottom(int offset) {
        mTop += offset;
        mBottom += offset;
    }
    public void offsetLeftAndRight(int offset) {
        mLeft += offset;
        mRight += offset;
    }
    @ViewDebug.ExportedProperty(deepExport = true, prefix = "layout_")
    public ViewGroup.LayoutParams getLayoutParams() {
        return mLayoutParams;
    }
    public void setLayoutParams(ViewGroup.LayoutParams params) {
        if (params == null) {
            throw new NullPointerException("params == null");
        }
        mLayoutParams = params;
        requestLayout();
    }
    public void scrollTo(int x, int y) {
        if (mScrollX != x || mScrollY != y) {
            int oldX = mScrollX;
            int oldY = mScrollY;
            mScrollX = x;
            mScrollY = y;
            onScrollChanged(mScrollX, mScrollY, oldX, oldY);
            if (!awakenScrollBars()) {
                invalidate();
            }
        }
    }
    public void scrollBy(int x, int y) {
        scrollTo(mScrollX + x, mScrollY + y);
    }
    protected boolean awakenScrollBars() {
        return mScrollCache != null &&
                awakenScrollBars(mScrollCache.scrollBarDefaultDelayBeforeFade, true);
    }
    private boolean initialAwakenScrollBars() {
        return mScrollCache != null &&
                awakenScrollBars(mScrollCache.scrollBarDefaultDelayBeforeFade * 4, true);
    }
    protected boolean awakenScrollBars(int startDelay) {
        return awakenScrollBars(startDelay, true);
    }
    protected boolean awakenScrollBars(int startDelay, boolean invalidate) {
        final ScrollabilityCache scrollCache = mScrollCache;
        if (scrollCache == null || !scrollCache.fadeScrollBars) {
            return false;
        }
        if (scrollCache.scrollBar == null) {
            scrollCache.scrollBar = new ScrollBarDrawable();
        }
        if (isHorizontalScrollBarEnabled() || isVerticalScrollBarEnabled()) {
            if (invalidate) {
                invalidate();
            }
            if (scrollCache.state == ScrollabilityCache.OFF) {
                final int KEY_REPEAT_FIRST_DELAY = 750;
                startDelay = Math.max(KEY_REPEAT_FIRST_DELAY, startDelay);
            }
            long fadeStartTime = AnimationUtils.currentAnimationTimeMillis() + startDelay;
            scrollCache.fadeStartTime = fadeStartTime;
            scrollCache.state = ScrollabilityCache.ON;
            if (mAttachInfo != null) {
                mAttachInfo.mHandler.removeCallbacks(scrollCache);
                mAttachInfo.mHandler.postAtTime(scrollCache, fadeStartTime);
            }
            return true;
        }
        return false;
    }
    public void invalidate(Rect dirty) {
        if (ViewDebug.TRACE_HIERARCHY) {
            ViewDebug.trace(this, ViewDebug.HierarchyTraceType.INVALIDATE);
        }
        if ((mPrivateFlags & (DRAWN | HAS_BOUNDS)) == (DRAWN | HAS_BOUNDS)) {
            mPrivateFlags &= ~DRAWING_CACHE_VALID;
            final ViewParent p = mParent;
            final AttachInfo ai = mAttachInfo;
            if (p != null && ai != null) {
                final int scrollX = mScrollX;
                final int scrollY = mScrollY;
                final Rect r = ai.mTmpInvalRect;
                r.set(dirty.left - scrollX, dirty.top - scrollY,
                        dirty.right - scrollX, dirty.bottom - scrollY);
                mParent.invalidateChild(this, r);
            }
        }
    }
    public void invalidate(int l, int t, int r, int b) {
        if (ViewDebug.TRACE_HIERARCHY) {
            ViewDebug.trace(this, ViewDebug.HierarchyTraceType.INVALIDATE);
        }
        if ((mPrivateFlags & (DRAWN | HAS_BOUNDS)) == (DRAWN | HAS_BOUNDS)) {
            mPrivateFlags &= ~DRAWING_CACHE_VALID;
            final ViewParent p = mParent;
            final AttachInfo ai = mAttachInfo;
            if (p != null && ai != null && l < r && t < b) {
                final int scrollX = mScrollX;
                final int scrollY = mScrollY;
                final Rect tmpr = ai.mTmpInvalRect;
                tmpr.set(l - scrollX, t - scrollY, r - scrollX, b - scrollY);
                p.invalidateChild(this, tmpr);
            }
        }
    }
    public void invalidate() {
        if (ViewDebug.TRACE_HIERARCHY) {
            ViewDebug.trace(this, ViewDebug.HierarchyTraceType.INVALIDATE);
        }
        if ((mPrivateFlags & (DRAWN | HAS_BOUNDS)) == (DRAWN | HAS_BOUNDS)) {
            mPrivateFlags &= ~DRAWN & ~DRAWING_CACHE_VALID;
            final ViewParent p = mParent;
            final AttachInfo ai = mAttachInfo;
            if (p != null && ai != null) {
                final Rect r = ai.mTmpInvalRect;
                r.set(0, 0, mRight - mLeft, mBottom - mTop);
                p.invalidateChild(this, r);
            }
        }
    }
    @ViewDebug.ExportedProperty
    public boolean isOpaque() {
        return (mPrivateFlags & OPAQUE_MASK) == OPAQUE_MASK;
    }
    private void computeOpaqueFlags() {
        if (mBGDrawable != null && mBGDrawable.getOpacity() == PixelFormat.OPAQUE) {
            mPrivateFlags |= OPAQUE_BACKGROUND;
        } else {
            mPrivateFlags &= ~OPAQUE_BACKGROUND;
        }
        final int flags = mViewFlags;
        if (((flags & SCROLLBARS_VERTICAL) == 0 && (flags & SCROLLBARS_HORIZONTAL) == 0) ||
                (flags & SCROLLBARS_STYLE_MASK) == SCROLLBARS_INSIDE_OVERLAY) {
            mPrivateFlags |= OPAQUE_SCROLLBARS;
        } else {
            mPrivateFlags &= ~OPAQUE_SCROLLBARS;
        }
    }
    protected boolean hasOpaqueScrollbars() {
        return (mPrivateFlags & OPAQUE_SCROLLBARS) == OPAQUE_SCROLLBARS;
    }
    public Handler getHandler() {
        if (mAttachInfo != null) {
            return mAttachInfo.mHandler;
        }
        return null;
    }
    public boolean post(Runnable action) {
        Handler handler;
        if (mAttachInfo != null) {
            handler = mAttachInfo.mHandler;
        } else {
            ViewRoot.getRunQueue().post(action);
            return true;
        }
        return handler.post(action);
    }
    public boolean postDelayed(Runnable action, long delayMillis) {
        Handler handler;
        if (mAttachInfo != null) {
            handler = mAttachInfo.mHandler;
        } else {
            ViewRoot.getRunQueue().postDelayed(action, delayMillis);
            return true;
        }
        return handler.postDelayed(action, delayMillis);
    }
    public boolean removeCallbacks(Runnable action) {
        Handler handler;
        if (mAttachInfo != null) {
            handler = mAttachInfo.mHandler;
        } else {
            ViewRoot.getRunQueue().removeCallbacks(action);
            return true;
        }
        handler.removeCallbacks(action);
        return true;
    }
    public void postInvalidate() {
        postInvalidateDelayed(0);
    }
    public void postInvalidate(int left, int top, int right, int bottom) {
        postInvalidateDelayed(0, left, top, right, bottom);
    }
    public void postInvalidateDelayed(long delayMilliseconds) {
        if (mAttachInfo != null) {
            Message msg = Message.obtain();
            msg.what = AttachInfo.INVALIDATE_MSG;
            msg.obj = this;
            mAttachInfo.mHandler.sendMessageDelayed(msg, delayMilliseconds);
        }
    }
    public void postInvalidateDelayed(long delayMilliseconds, int left, int top,
            int right, int bottom) {
        if (mAttachInfo != null) {
            final AttachInfo.InvalidateInfo info = AttachInfo.InvalidateInfo.acquire();
            info.target = this;
            info.left = left;
            info.top = top;
            info.right = right;
            info.bottom = bottom;
            final Message msg = Message.obtain();
            msg.what = AttachInfo.INVALIDATE_RECT_MSG;
            msg.obj = info;
            mAttachInfo.mHandler.sendMessageDelayed(msg, delayMilliseconds);
        }
    }
    public void computeScroll() {
    }
    public boolean isHorizontalFadingEdgeEnabled() {
        return (mViewFlags & FADING_EDGE_HORIZONTAL) == FADING_EDGE_HORIZONTAL;
    }
    public void setHorizontalFadingEdgeEnabled(boolean horizontalFadingEdgeEnabled) {
        if (isHorizontalFadingEdgeEnabled() != horizontalFadingEdgeEnabled) {
            if (horizontalFadingEdgeEnabled) {
                initScrollCache();
            }
            mViewFlags ^= FADING_EDGE_HORIZONTAL;
        }
    }
    public boolean isVerticalFadingEdgeEnabled() {
        return (mViewFlags & FADING_EDGE_VERTICAL) == FADING_EDGE_VERTICAL;
    }
    public void setVerticalFadingEdgeEnabled(boolean verticalFadingEdgeEnabled) {
        if (isVerticalFadingEdgeEnabled() != verticalFadingEdgeEnabled) {
            if (verticalFadingEdgeEnabled) {
                initScrollCache();
            }
            mViewFlags ^= FADING_EDGE_VERTICAL;
        }
    }
    protected float getTopFadingEdgeStrength() {
        return computeVerticalScrollOffset() > 0 ? 1.0f : 0.0f;
    }
    protected float getBottomFadingEdgeStrength() {
        return computeVerticalScrollOffset() + computeVerticalScrollExtent() <
                computeVerticalScrollRange() ? 1.0f : 0.0f;
    }
    protected float getLeftFadingEdgeStrength() {
        return computeHorizontalScrollOffset() > 0 ? 1.0f : 0.0f;
    }
    protected float getRightFadingEdgeStrength() {
        return computeHorizontalScrollOffset() + computeHorizontalScrollExtent() <
                computeHorizontalScrollRange() ? 1.0f : 0.0f;
    }
    public boolean isHorizontalScrollBarEnabled() {
        return (mViewFlags & SCROLLBARS_HORIZONTAL) == SCROLLBARS_HORIZONTAL;
    }
    public void setHorizontalScrollBarEnabled(boolean horizontalScrollBarEnabled) {
        if (isHorizontalScrollBarEnabled() != horizontalScrollBarEnabled) {
            mViewFlags ^= SCROLLBARS_HORIZONTAL;
            computeOpaqueFlags();
            recomputePadding();
        }
    }
    public boolean isVerticalScrollBarEnabled() {
        return (mViewFlags & SCROLLBARS_VERTICAL) == SCROLLBARS_VERTICAL;
    }
    public void setVerticalScrollBarEnabled(boolean verticalScrollBarEnabled) {
        if (isVerticalScrollBarEnabled() != verticalScrollBarEnabled) {
            mViewFlags ^= SCROLLBARS_VERTICAL;
            computeOpaqueFlags();
            recomputePadding();
        }
    }
    private void recomputePadding() {
        setPadding(mPaddingLeft, mPaddingTop, mUserPaddingRight, mUserPaddingBottom);
    }
    public void setScrollbarFadingEnabled(boolean fadeScrollbars) {
        initScrollCache();
        final ScrollabilityCache scrollabilityCache = mScrollCache;
        scrollabilityCache.fadeScrollBars = fadeScrollbars;
        if (fadeScrollbars) {
            scrollabilityCache.state = ScrollabilityCache.OFF;
        } else {
            scrollabilityCache.state = ScrollabilityCache.ON;
        }
    }
    public boolean isScrollbarFadingEnabled() {
        return mScrollCache != null && mScrollCache.fadeScrollBars; 
    }
    public void setScrollBarStyle(int style) {
        if (style != (mViewFlags & SCROLLBARS_STYLE_MASK)) {
            mViewFlags = (mViewFlags & ~SCROLLBARS_STYLE_MASK) | (style & SCROLLBARS_STYLE_MASK);
            computeOpaqueFlags();
            recomputePadding();
        }
    }
    public int getScrollBarStyle() {
        return mViewFlags & SCROLLBARS_STYLE_MASK;
    }
    protected int computeHorizontalScrollRange() {
        return getWidth();
    }
    protected int computeHorizontalScrollOffset() {
        return mScrollX;
    }
    protected int computeHorizontalScrollExtent() {
        return getWidth();
    }
    protected int computeVerticalScrollRange() {
        return getHeight();
    }
    protected int computeVerticalScrollOffset() {
        return mScrollY;
    }
    protected int computeVerticalScrollExtent() {
        return getHeight();
    }
    protected final void onDrawScrollBars(Canvas canvas) {
        final ScrollabilityCache cache = mScrollCache;
        if (cache != null) {
            int state = cache.state;
            if (state == ScrollabilityCache.OFF) {
                return;
            }
            boolean invalidate = false;
            if (state == ScrollabilityCache.FADING) {
                if (cache.interpolatorValues == null) {
                    cache.interpolatorValues = new float[1];
                }
                float[] values = cache.interpolatorValues;
                if (cache.scrollBarInterpolator.timeToValues(values) ==
                        Interpolator.Result.FREEZE_END) {
                    cache.state = ScrollabilityCache.OFF;
                } else {
                    cache.scrollBar.setAlpha(Math.round(values[0]));
                }
                invalidate = true;
            } else {
                cache.scrollBar.setAlpha(255);
            }
            final int viewFlags = mViewFlags;
            final boolean drawHorizontalScrollBar =
                (viewFlags & SCROLLBARS_HORIZONTAL) == SCROLLBARS_HORIZONTAL;
            final boolean drawVerticalScrollBar =
                (viewFlags & SCROLLBARS_VERTICAL) == SCROLLBARS_VERTICAL
                && !isVerticalScrollBarHidden();
            if (drawVerticalScrollBar || drawHorizontalScrollBar) {
                final int width = mRight - mLeft;
                final int height = mBottom - mTop;
                final ScrollBarDrawable scrollBar = cache.scrollBar;
                int size = scrollBar.getSize(false);
                if (size <= 0) {
                    size = cache.scrollBarSize;
                }
                final int scrollX = mScrollX;
                final int scrollY = mScrollY;
                final int inside = (viewFlags & SCROLLBARS_OUTSIDE_MASK) == 0 ? ~0 : 0;
                int left, top, right, bottom;
                if (drawHorizontalScrollBar) {
                    scrollBar.setParameters(computeHorizontalScrollRange(),
                                            computeHorizontalScrollOffset(),
                                            computeHorizontalScrollExtent(), false);
                    final int verticalScrollBarGap = drawVerticalScrollBar ?
                            getVerticalScrollbarWidth() : 0;
                    top = scrollY + height - size - (mUserPaddingBottom & inside);                         
                    left = scrollX + (mPaddingLeft & inside);
                    right = scrollX + width - (mUserPaddingRight & inside) - verticalScrollBarGap;
                    bottom = top + size;
                    onDrawHorizontalScrollBar(canvas, scrollBar, left, top, right, bottom);
                    if (invalidate) {
                        invalidate(left, top, right, bottom);
                    }
                }
                if (drawVerticalScrollBar) {
                    scrollBar.setParameters(computeVerticalScrollRange(),
                                            computeVerticalScrollOffset(),
                                            computeVerticalScrollExtent(), true);
                    left = scrollX + width - size - (mUserPaddingRight & inside);
                    top = scrollY + (mPaddingTop & inside);
                    right = left + size;
                    bottom = scrollY + height - (mUserPaddingBottom & inside);
                    onDrawVerticalScrollBar(canvas, scrollBar, left, top, right, bottom);
                    if (invalidate) {
                        invalidate(left, top, right, bottom);
                    }
                }
            }
        }
    }
    protected boolean isVerticalScrollBarHidden() {
        return false;
    }
    protected void onDrawHorizontalScrollBar(Canvas canvas,
                                             Drawable scrollBar,
                                             int l, int t, int r, int b) {
        scrollBar.setBounds(l, t, r, b);
        scrollBar.draw(canvas);
    }
    protected void onDrawVerticalScrollBar(Canvas canvas,
                                           Drawable scrollBar,
                                           int l, int t, int r, int b) {
        scrollBar.setBounds(l, t, r, b);
        scrollBar.draw(canvas);
    }
    protected void onDraw(Canvas canvas) {
    }
    void assignParent(ViewParent parent) {
        if (mParent == null) {
            mParent = parent;
        } else if (parent == null) {
            mParent = null;
        } else {
            throw new RuntimeException("view " + this + " being added, but"
                    + " it already has a parent");
        }
    }
    protected void onAttachedToWindow() {
        if ((mPrivateFlags & REQUEST_TRANSPARENT_REGIONS) != 0) {
            mParent.requestTransparentRegion(this);
        }
        if ((mPrivateFlags & AWAKEN_SCROLL_BARS_ON_ATTACH) != 0) {
            initialAwakenScrollBars();
            mPrivateFlags &= ~AWAKEN_SCROLL_BARS_ON_ATTACH;
        }
    }
    protected void onDetachedFromWindow() {
        mPrivateFlags &= ~CANCEL_NEXT_UP_EVENT;
        removeUnsetPressCallback();
        removeLongPressCallback();
        destroyDrawingCache();
    }
    protected int getWindowAttachCount() {
        return mWindowAttachCount;
    }
    public IBinder getWindowToken() {
        return mAttachInfo != null ? mAttachInfo.mWindowToken : null;
    }
    public IBinder getApplicationWindowToken() {
        AttachInfo ai = mAttachInfo;
        if (ai != null) {
            IBinder appWindowToken = ai.mPanelParentWindowToken;
            if (appWindowToken == null) {
                appWindowToken = ai.mWindowToken;
            }
            return appWindowToken;
        }
        return null;
    }
     IWindowSession getWindowSession() {
        return mAttachInfo != null ? mAttachInfo.mSession : null;
    }
    void dispatchAttachedToWindow(AttachInfo info, int visibility) {
        mAttachInfo = info;
        mWindowAttachCount++;
        if (mFloatingTreeObserver != null) {
            info.mTreeObserver.merge(mFloatingTreeObserver);
            mFloatingTreeObserver = null;
        }
        if ((mPrivateFlags&SCROLL_CONTAINER) != 0) {
            mAttachInfo.mScrollContainers.add(this);
            mPrivateFlags |= SCROLL_CONTAINER_ADDED;
        }
        performCollectViewAttributes(visibility);
        onAttachedToWindow();
        int vis = info.mWindowVisibility;
        if (vis != GONE) {
            onWindowVisibilityChanged(vis);
        }
    }
    void dispatchDetachedFromWindow() {
        AttachInfo info = mAttachInfo;
        if (info != null) {
            int vis = info.mWindowVisibility;
            if (vis != GONE) {
                onWindowVisibilityChanged(GONE);
            }
        }
        onDetachedFromWindow();
        if ((mPrivateFlags&SCROLL_CONTAINER_ADDED) != 0) {
            mAttachInfo.mScrollContainers.remove(this);
            mPrivateFlags &= ~SCROLL_CONTAINER_ADDED;
        }
        mAttachInfo = null;
    }
    public void saveHierarchyState(SparseArray<Parcelable> container) {
        dispatchSaveInstanceState(container);
    }
    protected void dispatchSaveInstanceState(SparseArray<Parcelable> container) {
        if (mID != NO_ID && (mViewFlags & SAVE_DISABLED_MASK) == 0) {
            mPrivateFlags &= ~SAVE_STATE_CALLED;
            Parcelable state = onSaveInstanceState();
            if ((mPrivateFlags & SAVE_STATE_CALLED) == 0) {
                throw new IllegalStateException(
                        "Derived class did not call super.onSaveInstanceState()");
            }
            if (state != null) {
                container.put(mID, state);
            }
        }
    }
    protected Parcelable onSaveInstanceState() {
        mPrivateFlags |= SAVE_STATE_CALLED;
        return BaseSavedState.EMPTY_STATE;
    }
    public void restoreHierarchyState(SparseArray<Parcelable> container) {
        dispatchRestoreInstanceState(container);
    }
    protected void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
        if (mID != NO_ID) {
            Parcelable state = container.get(mID);
            if (state != null) {
                mPrivateFlags &= ~SAVE_STATE_CALLED;
                onRestoreInstanceState(state);
                if ((mPrivateFlags & SAVE_STATE_CALLED) == 0) {
                    throw new IllegalStateException(
                            "Derived class did not call super.onRestoreInstanceState()");
                }
            }
        }
    }
    protected void onRestoreInstanceState(Parcelable state) {
        mPrivateFlags |= SAVE_STATE_CALLED;
        if (state != BaseSavedState.EMPTY_STATE && state != null) {
            throw new IllegalArgumentException("Wrong state class, expecting View State but "
                    + "received " + state.getClass().toString() + " instead. This usually happens "
                    + "when two views of different type have the same id in the same hierarchy. " 
                    + "This view's id is " + ViewDebug.resolveId(mContext, getId()) + ". Make sure " 
                    + "other views do not use the same id.");
        }
    }
    public long getDrawingTime() {
        return mAttachInfo != null ? mAttachInfo.mDrawingTime : 0;
    }
    public void setDuplicateParentStateEnabled(boolean enabled) {
        setFlags(enabled ? DUPLICATE_PARENT_STATE : 0, DUPLICATE_PARENT_STATE);
    }
    public boolean isDuplicateParentStateEnabled() {
        return (mViewFlags & DUPLICATE_PARENT_STATE) == DUPLICATE_PARENT_STATE;
    }
    public void setDrawingCacheEnabled(boolean enabled) {
        setFlags(enabled ? DRAWING_CACHE_ENABLED : 0, DRAWING_CACHE_ENABLED);
    }
    @ViewDebug.ExportedProperty
    public boolean isDrawingCacheEnabled() {
        return (mViewFlags & DRAWING_CACHE_ENABLED) == DRAWING_CACHE_ENABLED;
    }
    public Bitmap getDrawingCache() {
        return getDrawingCache(false);
    }
    public Bitmap getDrawingCache(boolean autoScale) {
        if ((mViewFlags & WILL_NOT_CACHE_DRAWING) == WILL_NOT_CACHE_DRAWING) {
            return null;
        }
        if ((mViewFlags & DRAWING_CACHE_ENABLED) == DRAWING_CACHE_ENABLED) {
            buildDrawingCache(autoScale);
        }
        return autoScale ? (mDrawingCache == null ? null : mDrawingCache.get()) :
                (mUnscaledDrawingCache == null ? null : mUnscaledDrawingCache.get());
    }
    public void destroyDrawingCache() {
        if (mDrawingCache != null) {
            final Bitmap bitmap = mDrawingCache.get();
            if (bitmap != null) bitmap.recycle();
            mDrawingCache = null;
        }
        if (mUnscaledDrawingCache != null) {
            final Bitmap bitmap = mUnscaledDrawingCache.get();
            if (bitmap != null) bitmap.recycle();
            mUnscaledDrawingCache = null;
        }
    }
    public void setDrawingCacheBackgroundColor(int color) {
        if (color != mDrawingCacheBackgroundColor) {
            mDrawingCacheBackgroundColor = color;
            mPrivateFlags &= ~DRAWING_CACHE_VALID;
        }
    }
    public int getDrawingCacheBackgroundColor() {
        return mDrawingCacheBackgroundColor;
    }
    public void buildDrawingCache() {
        buildDrawingCache(false);
    }
    public void buildDrawingCache(boolean autoScale) {
        if ((mPrivateFlags & DRAWING_CACHE_VALID) == 0 || (autoScale ?
                (mDrawingCache == null || mDrawingCache.get() == null) :
                (mUnscaledDrawingCache == null || mUnscaledDrawingCache.get() == null))) {
            if (ViewDebug.TRACE_HIERARCHY) {
                ViewDebug.trace(this, ViewDebug.HierarchyTraceType.BUILD_CACHE);
            }
            if (Config.DEBUG && ViewDebug.profileDrawing) {
                EventLog.writeEvent(60002, hashCode());
            }
            int width = mRight - mLeft;
            int height = mBottom - mTop;
            final AttachInfo attachInfo = mAttachInfo;
            final boolean scalingRequired = attachInfo != null && attachInfo.mScalingRequired;
            if (autoScale && scalingRequired) {
                width = (int) ((width * attachInfo.mApplicationScale) + 0.5f);
                height = (int) ((height * attachInfo.mApplicationScale) + 0.5f);
            }
            final int drawingCacheBackgroundColor = mDrawingCacheBackgroundColor;
            final boolean opaque = drawingCacheBackgroundColor != 0 || isOpaque();
            final boolean translucentWindow = attachInfo != null && attachInfo.mTranslucentWindow;
            if (width <= 0 || height <= 0 ||
                    (width * height * (opaque && !translucentWindow ? 2 : 4) >
                            ViewConfiguration.get(mContext).getScaledMaximumDrawingCacheSize())) {
                destroyDrawingCache();
                return;
            }
            boolean clear = true;
            Bitmap bitmap = autoScale ? (mDrawingCache == null ? null : mDrawingCache.get()) :
                    (mUnscaledDrawingCache == null ? null : mUnscaledDrawingCache.get());
            if (bitmap == null || bitmap.getWidth() != width || bitmap.getHeight() != height) {
                Bitmap.Config quality;
                if (!opaque) {
                    switch (mViewFlags & DRAWING_CACHE_QUALITY_MASK) {
                        case DRAWING_CACHE_QUALITY_AUTO:
                            quality = Bitmap.Config.ARGB_8888;
                            break;
                        case DRAWING_CACHE_QUALITY_LOW:
                            quality = Bitmap.Config.ARGB_4444;
                            break;
                        case DRAWING_CACHE_QUALITY_HIGH:
                            quality = Bitmap.Config.ARGB_8888;
                            break;
                        default:
                            quality = Bitmap.Config.ARGB_8888;
                            break;
                    }
                } else {
                    quality = translucentWindow ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;
                }
                if (bitmap != null) bitmap.recycle();
                try {
                    bitmap = Bitmap.createBitmap(width, height, quality);
                    bitmap.setDensity(getResources().getDisplayMetrics().densityDpi);
                    if (autoScale) {
                        mDrawingCache = new SoftReference<Bitmap>(bitmap);
                    } else {
                        mUnscaledDrawingCache = new SoftReference<Bitmap>(bitmap);
                    }
                    if (opaque && translucentWindow) bitmap.setHasAlpha(false);
                } catch (OutOfMemoryError e) {
                    if (autoScale) {
                        mDrawingCache = null;
                    } else {
                        mUnscaledDrawingCache = null;
                    }
                    return;
                }
                clear = drawingCacheBackgroundColor != 0;
            }
            Canvas canvas;
            if (attachInfo != null) {
                canvas = attachInfo.mCanvas;
                if (canvas == null) {
                    canvas = new Canvas();
                }
                canvas.setBitmap(bitmap);
                attachInfo.mCanvas = null;
            } else {
                canvas = new Canvas(bitmap);
            }
            if (clear) {
                bitmap.eraseColor(drawingCacheBackgroundColor);
            }
            computeScroll();
            final int restoreCount = canvas.save();
            if (autoScale && scalingRequired) {
                final float scale = attachInfo.mApplicationScale;
                canvas.scale(scale, scale);
            }
            canvas.translate(-mScrollX, -mScrollY);
            mPrivateFlags |= DRAWN;
            mPrivateFlags |= DRAWING_CACHE_VALID;
            if ((mPrivateFlags & SKIP_DRAW) == SKIP_DRAW) {
                if (ViewDebug.TRACE_HIERARCHY) {
                    ViewDebug.trace(this, ViewDebug.HierarchyTraceType.DRAW);
                }
                mPrivateFlags &= ~DIRTY_MASK;
                dispatchDraw(canvas);
            } else {
                draw(canvas);
            }
            canvas.restoreToCount(restoreCount);
            if (attachInfo != null) {
                attachInfo.mCanvas = canvas;
            }
        }
    }
    Bitmap createSnapshot(Bitmap.Config quality, int backgroundColor, boolean skipChildren) {
        int width = mRight - mLeft;
        int height = mBottom - mTop;
        final AttachInfo attachInfo = mAttachInfo;
        final float scale = attachInfo != null ? attachInfo.mApplicationScale : 1.0f;
        width = (int) ((width * scale) + 0.5f);
        height = (int) ((height * scale) + 0.5f);
        Bitmap bitmap = Bitmap.createBitmap(width > 0 ? width : 1, height > 0 ? height : 1, quality);
        if (bitmap == null) {
            throw new OutOfMemoryError();
        }
        bitmap.setDensity(getResources().getDisplayMetrics().densityDpi);
        Canvas canvas;
        if (attachInfo != null) {
            canvas = attachInfo.mCanvas;
            if (canvas == null) {
                canvas = new Canvas();
            }
            canvas.setBitmap(bitmap);
            attachInfo.mCanvas = null;
        } else {
            canvas = new Canvas(bitmap);
        }
        if ((backgroundColor & 0xff000000) != 0) {
            bitmap.eraseColor(backgroundColor);
        }
        computeScroll();
        final int restoreCount = canvas.save();
        canvas.scale(scale, scale);
        canvas.translate(-mScrollX, -mScrollY);
        int flags = mPrivateFlags;
        mPrivateFlags &= ~DIRTY_MASK;
        if ((mPrivateFlags & SKIP_DRAW) == SKIP_DRAW) {
            dispatchDraw(canvas);
        } else {
            draw(canvas);
        }
        mPrivateFlags = flags;
        canvas.restoreToCount(restoreCount);
        if (attachInfo != null) {
            attachInfo.mCanvas = canvas;
        }
        return bitmap;
    }
    public boolean isInEditMode() {
        return false;
    }
    protected boolean isPaddingOffsetRequired() {
        return false;
    }
    protected int getLeftPaddingOffset() {
        return 0;
    }
    protected int getRightPaddingOffset() {
        return 0;
    }
    protected int getTopPaddingOffset() {
        return 0;
    }
    protected int getBottomPaddingOffset() {
        return 0;
    }
    public void draw(Canvas canvas) {
        if (ViewDebug.TRACE_HIERARCHY) {
            ViewDebug.trace(this, ViewDebug.HierarchyTraceType.DRAW);
        }
        final int privateFlags = mPrivateFlags;
        final boolean dirtyOpaque = (privateFlags & DIRTY_MASK) == DIRTY_OPAQUE &&
                (mAttachInfo == null || !mAttachInfo.mIgnoreDirtyState);
        mPrivateFlags = (privateFlags & ~DIRTY_MASK) | DRAWN;
        int saveCount;
        if (!dirtyOpaque) {
            final Drawable background = mBGDrawable;
            if (background != null) {
                final int scrollX = mScrollX;
                final int scrollY = mScrollY;
                if (mBackgroundSizeChanged) {
                    background.setBounds(0, 0,  mRight - mLeft, mBottom - mTop);
                    mBackgroundSizeChanged = false;
                }
                if ((scrollX | scrollY) == 0) {
                    background.draw(canvas);
                } else {
                    canvas.translate(scrollX, scrollY);
                    background.draw(canvas);
                    canvas.translate(-scrollX, -scrollY);
                }
            }
        }
        final int viewFlags = mViewFlags;
        boolean horizontalEdges = (viewFlags & FADING_EDGE_HORIZONTAL) != 0;
        boolean verticalEdges = (viewFlags & FADING_EDGE_VERTICAL) != 0;
        if (!verticalEdges && !horizontalEdges) {
            if (!dirtyOpaque) onDraw(canvas);
            dispatchDraw(canvas);
            onDrawScrollBars(canvas);
            return;
        }
        boolean drawTop = false;
        boolean drawBottom = false;
        boolean drawLeft = false;
        boolean drawRight = false;
        float topFadeStrength = 0.0f;
        float bottomFadeStrength = 0.0f;
        float leftFadeStrength = 0.0f;
        float rightFadeStrength = 0.0f;
        int paddingLeft = mPaddingLeft;
        int paddingTop = mPaddingTop;
        final boolean offsetRequired = isPaddingOffsetRequired();
        if (offsetRequired) {
            paddingLeft += getLeftPaddingOffset();
            paddingTop += getTopPaddingOffset();
        }
        int left = mScrollX + paddingLeft;
        int right = left + mRight - mLeft - mPaddingRight - paddingLeft;
        int top = mScrollY + paddingTop;
        int bottom = top + mBottom - mTop - mPaddingBottom - paddingTop;
        if (offsetRequired) {
            right += getRightPaddingOffset();
            bottom += getBottomPaddingOffset();
        }
        final ScrollabilityCache scrollabilityCache = mScrollCache;
        int length = scrollabilityCache.fadingEdgeLength;
        if (verticalEdges && (top + length > bottom - length)) {
            length = (bottom - top) / 2;
        }
        if (horizontalEdges && (left + length > right - length)) {
            length = (right - left) / 2;
        }
        if (verticalEdges) {
            topFadeStrength = Math.max(0.0f, Math.min(1.0f, getTopFadingEdgeStrength()));
            drawTop = topFadeStrength >= 0.0f;
            bottomFadeStrength = Math.max(0.0f, Math.min(1.0f, getBottomFadingEdgeStrength()));
            drawBottom = bottomFadeStrength >= 0.0f;
        }
        if (horizontalEdges) {
            leftFadeStrength = Math.max(0.0f, Math.min(1.0f, getLeftFadingEdgeStrength()));
            drawLeft = leftFadeStrength >= 0.0f;
            rightFadeStrength = Math.max(0.0f, Math.min(1.0f, getRightFadingEdgeStrength()));
            drawRight = rightFadeStrength >= 0.0f;
        }
        saveCount = canvas.getSaveCount();
        int solidColor = getSolidColor();
        if (solidColor == 0) {
            final int flags = Canvas.HAS_ALPHA_LAYER_SAVE_FLAG;
            if (drawTop) {
                canvas.saveLayer(left, top, right, top + length, null, flags);
            }
            if (drawBottom) {
                canvas.saveLayer(left, bottom - length, right, bottom, null, flags);
            }
            if (drawLeft) {
                canvas.saveLayer(left, top, left + length, bottom, null, flags);
            }
            if (drawRight) {
                canvas.saveLayer(right - length, top, right, bottom, null, flags);
            }
        } else {
            scrollabilityCache.setFadeColor(solidColor);
        }
        if (!dirtyOpaque) onDraw(canvas);
        dispatchDraw(canvas);
        final Paint p = scrollabilityCache.paint;
        final Matrix matrix = scrollabilityCache.matrix;
        final Shader fade = scrollabilityCache.shader;
        final float fadeHeight = scrollabilityCache.fadingEdgeLength;
        if (drawTop) {
            matrix.setScale(1, fadeHeight * topFadeStrength);
            matrix.postTranslate(left, top);
            fade.setLocalMatrix(matrix);
            canvas.drawRect(left, top, right, top + length, p);
        }
        if (drawBottom) {
            matrix.setScale(1, fadeHeight * bottomFadeStrength);
            matrix.postRotate(180);
            matrix.postTranslate(left, bottom);
            fade.setLocalMatrix(matrix);
            canvas.drawRect(left, bottom - length, right, bottom, p);
        }
        if (drawLeft) {
            matrix.setScale(1, fadeHeight * leftFadeStrength);
            matrix.postRotate(-90);
            matrix.postTranslate(left, top);
            fade.setLocalMatrix(matrix);
            canvas.drawRect(left, top, left + length, bottom, p);
        }
        if (drawRight) {
            matrix.setScale(1, fadeHeight * rightFadeStrength);
            matrix.postRotate(90);
            matrix.postTranslate(right, top);
            fade.setLocalMatrix(matrix);
            canvas.drawRect(right - length, top, right, bottom, p);
        }
        canvas.restoreToCount(saveCount);
        onDrawScrollBars(canvas);
    }
    public int getSolidColor() {
        return 0;
    }
    private static String printFlags(int flags) {
        String output = "";
        int numFlags = 0;
        if ((flags & FOCUSABLE_MASK) == FOCUSABLE) {
            output += "TAKES_FOCUS";
            numFlags++;
        }
        switch (flags & VISIBILITY_MASK) {
        case INVISIBLE:
            if (numFlags > 0) {
                output += " ";
            }
            output += "INVISIBLE";
            break;
        case GONE:
            if (numFlags > 0) {
                output += " ";
            }
            output += "GONE";
            break;
        default:
            break;
        }
        return output;
    }
    private static String printPrivateFlags(int privateFlags) {
        String output = "";
        int numFlags = 0;
        if ((privateFlags & WANTS_FOCUS) == WANTS_FOCUS) {
            output += "WANTS_FOCUS";
            numFlags++;
        }
        if ((privateFlags & FOCUSED) == FOCUSED) {
            if (numFlags > 0) {
                output += " ";
            }
            output += "FOCUSED";
            numFlags++;
        }
        if ((privateFlags & SELECTED) == SELECTED) {
            if (numFlags > 0) {
                output += " ";
            }
            output += "SELECTED";
            numFlags++;
        }
        if ((privateFlags & IS_ROOT_NAMESPACE) == IS_ROOT_NAMESPACE) {
            if (numFlags > 0) {
                output += " ";
            }
            output += "IS_ROOT_NAMESPACE";
            numFlags++;
        }
        if ((privateFlags & HAS_BOUNDS) == HAS_BOUNDS) {
            if (numFlags > 0) {
                output += " ";
            }
            output += "HAS_BOUNDS";
            numFlags++;
        }
        if ((privateFlags & DRAWN) == DRAWN) {
            if (numFlags > 0) {
                output += " ";
            }
            output += "DRAWN";
        }
        return output;
    }
    public boolean isLayoutRequested() {
        return (mPrivateFlags & FORCE_LAYOUT) == FORCE_LAYOUT;
    }
    public final void layout(int l, int t, int r, int b) {
        boolean changed = setFrame(l, t, r, b);
        if (changed || (mPrivateFlags & LAYOUT_REQUIRED) == LAYOUT_REQUIRED) {
            if (ViewDebug.TRACE_HIERARCHY) {
                ViewDebug.trace(this, ViewDebug.HierarchyTraceType.ON_LAYOUT);
            }
            onLayout(changed, l, t, r, b);
            mPrivateFlags &= ~LAYOUT_REQUIRED;
        }
        mPrivateFlags &= ~FORCE_LAYOUT;
    }
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    }
    protected boolean setFrame(int left, int top, int right, int bottom) {
        boolean changed = false;
        if (DBG) {
            Log.d("View", this + " View.setFrame(" + left + "," + top + ","
                    + right + "," + bottom + ")");
        }
        if (mLeft != left || mRight != right || mTop != top || mBottom != bottom) {
            changed = true;
            int drawn = mPrivateFlags & DRAWN;
            invalidate();
            int oldWidth = mRight - mLeft;
            int oldHeight = mBottom - mTop;
            mLeft = left;
            mTop = top;
            mRight = right;
            mBottom = bottom;
            mPrivateFlags |= HAS_BOUNDS;
            int newWidth = right - left;
            int newHeight = bottom - top;
            if (newWidth != oldWidth || newHeight != oldHeight) {
                onSizeChanged(newWidth, newHeight, oldWidth, oldHeight);
            }
            if ((mViewFlags & VISIBILITY_MASK) == VISIBLE) {
                mPrivateFlags |= DRAWN;
                invalidate();
            }
            mPrivateFlags |= drawn;
            mBackgroundSizeChanged = true;
        }
        return changed;
    }
    protected void onFinishInflate() {
    }
    public Resources getResources() {
        return mResources;
    }
    public void invalidateDrawable(Drawable drawable) {
        if (verifyDrawable(drawable)) {
            final Rect dirty = drawable.getBounds();
            final int scrollX = mScrollX;
            final int scrollY = mScrollY;
            invalidate(dirty.left + scrollX, dirty.top + scrollY,
                    dirty.right + scrollX, dirty.bottom + scrollY);
        }
    }
    public void scheduleDrawable(Drawable who, Runnable what, long when) {
        if (verifyDrawable(who) && what != null && mAttachInfo != null) {
            mAttachInfo.mHandler.postAtTime(what, who, when);
        }
    }
    public void unscheduleDrawable(Drawable who, Runnable what) {
        if (verifyDrawable(who) && what != null && mAttachInfo != null) {
            mAttachInfo.mHandler.removeCallbacks(what, who);
        }
    }
    public void unscheduleDrawable(Drawable who) {
        if (mAttachInfo != null) {
            mAttachInfo.mHandler.removeCallbacksAndMessages(who);
        }
    }
    protected boolean verifyDrawable(Drawable who) {
        return who == mBGDrawable;
    }
    protected void drawableStateChanged() {
        Drawable d = mBGDrawable;
        if (d != null && d.isStateful()) {
            d.setState(getDrawableState());
        }
    }
    public void refreshDrawableState() {
        mPrivateFlags |= DRAWABLE_STATE_DIRTY;
        drawableStateChanged();
        ViewParent parent = mParent;
        if (parent != null) {
            parent.childDrawableStateChanged(this);
        }
    }
    public final int[] getDrawableState() {
        if ((mDrawableState != null) && ((mPrivateFlags & DRAWABLE_STATE_DIRTY) == 0)) {
            return mDrawableState;
        } else {
            mDrawableState = onCreateDrawableState(0);
            mPrivateFlags &= ~DRAWABLE_STATE_DIRTY;
            return mDrawableState;
        }
    }
    protected int[] onCreateDrawableState(int extraSpace) {
        if ((mViewFlags & DUPLICATE_PARENT_STATE) == DUPLICATE_PARENT_STATE &&
                mParent instanceof View) {
            return ((View) mParent).onCreateDrawableState(extraSpace);
        }
        int[] drawableState;
        int privateFlags = mPrivateFlags;
        int viewStateIndex = (((privateFlags & PRESSED) != 0) ? 1 : 0);
        viewStateIndex = (viewStateIndex << 1)
                + (((mViewFlags & ENABLED_MASK) == ENABLED) ? 1 : 0);
        viewStateIndex = (viewStateIndex << 1) + (isFocused() ? 1 : 0);
        viewStateIndex = (viewStateIndex << 1)
                + (((privateFlags & SELECTED) != 0) ? 1 : 0);
        final boolean hasWindowFocus = hasWindowFocus();
        viewStateIndex = (viewStateIndex << 1) + (hasWindowFocus ? 1 : 0);
        drawableState = VIEW_STATE_SETS[viewStateIndex];
        if (false) {
            Log.i("View", "drawableStateIndex=" + viewStateIndex);
            Log.i("View", toString()
                    + " pressed=" + ((privateFlags & PRESSED) != 0)
                    + " en=" + ((mViewFlags & ENABLED_MASK) == ENABLED)
                    + " fo=" + hasFocus()
                    + " sl=" + ((privateFlags & SELECTED) != 0)
                    + " wf=" + hasWindowFocus
                    + ": " + Arrays.toString(drawableState));
        }
        if (extraSpace == 0) {
            return drawableState;
        }
        final int[] fullState;
        if (drawableState != null) {
            fullState = new int[drawableState.length + extraSpace];
            System.arraycopy(drawableState, 0, fullState, 0, drawableState.length);
        } else {
            fullState = new int[extraSpace];
        }
        return fullState;
    }
    protected static int[] mergeDrawableStates(int[] baseState, int[] additionalState) {
        final int N = baseState.length;
        int i = N - 1;
        while (i >= 0 && baseState[i] == 0) {
            i--;
        }
        System.arraycopy(additionalState, 0, baseState, i + 1, additionalState.length);
        return baseState;
    }
    @RemotableViewMethod
    public void setBackgroundColor(int color) {
        setBackgroundDrawable(new ColorDrawable(color));
    }
    @RemotableViewMethod
    public void setBackgroundResource(int resid) {
        if (resid != 0 && resid == mBackgroundResource) {
            return;
        }
        Drawable d= null;
        if (resid != 0) {
            d = mResources.getDrawable(resid);
        }
        setBackgroundDrawable(d);
        mBackgroundResource = resid;
    }
    public void setBackgroundDrawable(Drawable d) {
        boolean requestLayout = false;
        mBackgroundResource = 0;
        if (mBGDrawable != null) {
            mBGDrawable.setCallback(null);
            unscheduleDrawable(mBGDrawable);
        }
        if (d != null) {
            Rect padding = sThreadLocal.get();
            if (padding == null) {
                padding = new Rect();
                sThreadLocal.set(padding);
            }
            if (d.getPadding(padding)) {
                setPadding(padding.left, padding.top, padding.right, padding.bottom);
            }
            if (mBGDrawable == null || mBGDrawable.getMinimumHeight() != d.getMinimumHeight() ||
                    mBGDrawable.getMinimumWidth() != d.getMinimumWidth()) {
                requestLayout = true;
            }
            d.setCallback(this);
            if (d.isStateful()) {
                d.setState(getDrawableState());
            }
            d.setVisible(getVisibility() == VISIBLE, false);
            mBGDrawable = d;
            if ((mPrivateFlags & SKIP_DRAW) != 0) {
                mPrivateFlags &= ~SKIP_DRAW;
                mPrivateFlags |= ONLY_DRAWS_BACKGROUND;
                requestLayout = true;
            }
        } else {
            mBGDrawable = null;
            if ((mPrivateFlags & ONLY_DRAWS_BACKGROUND) != 0) {
                mPrivateFlags &= ~ONLY_DRAWS_BACKGROUND;
                mPrivateFlags |= SKIP_DRAW;
            }
            requestLayout = true;
        }
        computeOpaqueFlags();
        if (requestLayout) {
            requestLayout();
        }
        mBackgroundSizeChanged = true;
        invalidate();
    }
    public Drawable getBackground() {
        return mBGDrawable;
    }
    public void setPadding(int left, int top, int right, int bottom) {
        boolean changed = false;
        mUserPaddingRight = right;
        mUserPaddingBottom = bottom;
        final int viewFlags = mViewFlags;
        if ((viewFlags & (SCROLLBARS_VERTICAL|SCROLLBARS_HORIZONTAL)) != 0) {
            if ((viewFlags & SCROLLBARS_VERTICAL) != 0) {
                right += (viewFlags & SCROLLBARS_INSET_MASK) == 0
                        ? 0 : getVerticalScrollbarWidth();
            }
            if ((viewFlags & SCROLLBARS_HORIZONTAL) == 0) {
                bottom += (viewFlags & SCROLLBARS_INSET_MASK) == 0
                        ? 0 : getHorizontalScrollbarHeight();
            }
        }
        if (mPaddingLeft != left) {
            changed = true;
            mPaddingLeft = left;
        }
        if (mPaddingTop != top) {
            changed = true;
            mPaddingTop = top;
        }
        if (mPaddingRight != right) {
            changed = true;
            mPaddingRight = right;
        }
        if (mPaddingBottom != bottom) {
            changed = true;
            mPaddingBottom = bottom;
        }
        if (changed) {
            requestLayout();
        }
    }
    public int getPaddingTop() {
        return mPaddingTop;
    }
    public int getPaddingBottom() {
        return mPaddingBottom;
    }
    public int getPaddingLeft() {
        return mPaddingLeft;
    }
    public int getPaddingRight() {
        return mPaddingRight;
    }
    public void setSelected(boolean selected) {
        if (((mPrivateFlags & SELECTED) != 0) != selected) {
            mPrivateFlags = (mPrivateFlags & ~SELECTED) | (selected ? SELECTED : 0);
            if (!selected) resetPressedState();
            invalidate();
            refreshDrawableState();
            dispatchSetSelected(selected);
        }
    }
    protected void dispatchSetSelected(boolean selected) {
    }
    @ViewDebug.ExportedProperty
    public boolean isSelected() {
        return (mPrivateFlags & SELECTED) != 0;
    }
    public ViewTreeObserver getViewTreeObserver() {
        if (mAttachInfo != null) {
            return mAttachInfo.mTreeObserver;
        }
        if (mFloatingTreeObserver == null) {
            mFloatingTreeObserver = new ViewTreeObserver();
        }
        return mFloatingTreeObserver;
    }
    public View getRootView() {
        if (mAttachInfo != null) {
            final View v = mAttachInfo.mRootView;
            if (v != null) {
                return v;
            }
        }
        View parent = this;
        while (parent.mParent != null && parent.mParent instanceof View) {
            parent = (View) parent.mParent;
        }
        return parent;
    }
    public void getLocationOnScreen(int[] location) {
        getLocationInWindow(location);
        final AttachInfo info = mAttachInfo;
        if (info != null) {
            location[0] += info.mWindowLeft;
            location[1] += info.mWindowTop;
        }
    }
    public void getLocationInWindow(int[] location) {
        if (location == null || location.length < 2) {
            throw new IllegalArgumentException("location must be an array of "
                    + "two integers");
        }
        location[0] = mLeft;
        location[1] = mTop;
        ViewParent viewParent = mParent;
        while (viewParent instanceof View) {
            final View view = (View)viewParent;
            location[0] += view.mLeft - view.mScrollX;
            location[1] += view.mTop - view.mScrollY;
            viewParent = view.mParent;
        }
        if (viewParent instanceof ViewRoot) {
            final ViewRoot vr = (ViewRoot)viewParent;
            location[1] -= vr.mCurScrollY;
        }
    }
    protected View findViewTraversal(int id) {
        if (id == mID) {
            return this;
        }
        return null;
    }
    protected View findViewWithTagTraversal(Object tag) {
        if (tag != null && tag.equals(mTag)) {
            return this;
        }
        return null;
    }
    public final View findViewById(int id) {
        if (id < 0) {
            return null;
        }
        return findViewTraversal(id);
    }
    public final View findViewWithTag(Object tag) {
        if (tag == null) {
            return null;
        }
        return findViewWithTagTraversal(tag);
    }
    public void setId(int id) {
        mID = id;
    }
    public void setIsRootNamespace(boolean isRoot) {
        if (isRoot) {
            mPrivateFlags |= IS_ROOT_NAMESPACE;
        } else {
            mPrivateFlags &= ~IS_ROOT_NAMESPACE;
        }
    }
    public boolean isRootNamespace() {
        return (mPrivateFlags&IS_ROOT_NAMESPACE) != 0;
    }
    @ViewDebug.CapturedViewProperty
    public int getId() {
        return mID;
    }
    @ViewDebug.ExportedProperty
    public Object getTag() {
        return mTag;
    }
    public void setTag(final Object tag) {
        mTag = tag;
    }
    public Object getTag(int key) {
        SparseArray<Object> tags = null;
        synchronized (sTagsLock) {
            if (sTags != null) {
                tags = sTags.get(this);
            }
        }
        if (tags != null) return tags.get(key);
        return null;
    }
    public void setTag(int key, final Object tag) {
        if ((key >>> 24) < 2) {
            throw new IllegalArgumentException("The key must be an application-specific "
                    + "resource id.");
        }
        setTagInternal(this, key, tag);
    }
    public void setTagInternal(int key, Object tag) {
        if ((key >>> 24) != 0x1) {
            throw new IllegalArgumentException("The key must be a framework-specific "
                    + "resource id.");
        }
        setTagInternal(this, key, tag);
    }
    private static void setTagInternal(View view, int key, Object tag) {
        SparseArray<Object> tags = null;
        synchronized (sTagsLock) {
            if (sTags == null) {
                sTags = new WeakHashMap<View, SparseArray<Object>>();
            } else {
                tags = sTags.get(view);
            }
        }
        if (tags == null) {
            tags = new SparseArray<Object>(2);
            synchronized (sTagsLock) {
                sTags.put(view, tags);
            }
        }
        tags.put(key, tag);
    }
    protected boolean dispatchConsistencyCheck(int consistency) {
        return onConsistencyCheck(consistency);
    }
    protected boolean onConsistencyCheck(int consistency) {
        boolean result = true;
        final boolean checkLayout = (consistency & ViewDebug.CONSISTENCY_LAYOUT) != 0;
        final boolean checkDrawing = (consistency & ViewDebug.CONSISTENCY_DRAWING) != 0;
        if (checkLayout) {
            if (getParent() == null) {
                result = false;
                android.util.Log.d(ViewDebug.CONSISTENCY_LOG_TAG,
                        "View " + this + " does not have a parent.");
            }
            if (mAttachInfo == null) {
                result = false;
                android.util.Log.d(ViewDebug.CONSISTENCY_LOG_TAG,
                        "View " + this + " is not attached to a window.");
            }
        }
        if (checkDrawing) {
            if ((mPrivateFlags & DRAWN) != DRAWN &&
                    (mPrivateFlags & DRAWING_CACHE_VALID) == DRAWING_CACHE_VALID) {
                result = false;
                android.util.Log.d(ViewDebug.CONSISTENCY_LOG_TAG,
                        "View " + this + " was invalidated but its drawing cache is valid.");
            }
        }
        return result;
    }
    public void debug() {
        debug(0);
    }
    protected void debug(int depth) {
        String output = debugIndent(depth - 1);
        output += "+ " + this;
        int id = getId();
        if (id != -1) {
            output += " (id=" + id + ")";
        }
        Object tag = getTag();
        if (tag != null) {
            output += " (tag=" + tag + ")";
        }
        Log.d(VIEW_LOG_TAG, output);
        if ((mPrivateFlags & FOCUSED) != 0) {
            output = debugIndent(depth) + " FOCUSED";
            Log.d(VIEW_LOG_TAG, output);
        }
        output = debugIndent(depth);
        output += "frame={" + mLeft + ", " + mTop + ", " + mRight
                + ", " + mBottom + "} scroll={" + mScrollX + ", " + mScrollY
                + "} ";
        Log.d(VIEW_LOG_TAG, output);
        if (mPaddingLeft != 0 || mPaddingTop != 0 || mPaddingRight != 0
                || mPaddingBottom != 0) {
            output = debugIndent(depth);
            output += "padding={" + mPaddingLeft + ", " + mPaddingTop
                    + ", " + mPaddingRight + ", " + mPaddingBottom + "}";
            Log.d(VIEW_LOG_TAG, output);
        }
        output = debugIndent(depth);
        output += "mMeasureWidth=" + mMeasuredWidth +
                " mMeasureHeight=" + mMeasuredHeight;
        Log.d(VIEW_LOG_TAG, output);
        output = debugIndent(depth);
        if (mLayoutParams == null) {
            output += "BAD! no layout params";
        } else {
            output = mLayoutParams.debug(output);
        }
        Log.d(VIEW_LOG_TAG, output);
        output = debugIndent(depth);
        output += "flags={";
        output += View.printFlags(mViewFlags);
        output += "}";
        Log.d(VIEW_LOG_TAG, output);
        output = debugIndent(depth);
        output += "privateFlags={";
        output += View.printPrivateFlags(mPrivateFlags);
        output += "}";
        Log.d(VIEW_LOG_TAG, output);
    }
    protected static String debugIndent(int depth) {
        StringBuilder spaces = new StringBuilder((depth * 2 + 3) * 2);
        for (int i = 0; i < (depth * 2) + 3; i++) {
            spaces.append(' ').append(' ');
        }
        return spaces.toString();
    }
    @ViewDebug.ExportedProperty
    public int getBaseline() {
        return -1;
    }
    public void requestLayout() {
        if (ViewDebug.TRACE_HIERARCHY) {
            ViewDebug.trace(this, ViewDebug.HierarchyTraceType.REQUEST_LAYOUT);
        }
        mPrivateFlags |= FORCE_LAYOUT;
        if (mParent != null && !mParent.isLayoutRequested()) {
            mParent.requestLayout();
        }
    }
    public void forceLayout() {
        mPrivateFlags |= FORCE_LAYOUT;
    }
    public final void measure(int widthMeasureSpec, int heightMeasureSpec) {
        if ((mPrivateFlags & FORCE_LAYOUT) == FORCE_LAYOUT ||
                widthMeasureSpec != mOldWidthMeasureSpec ||
                heightMeasureSpec != mOldHeightMeasureSpec) {
            mPrivateFlags &= ~MEASURED_DIMENSION_SET;
            if (ViewDebug.TRACE_HIERARCHY) {
                ViewDebug.trace(this, ViewDebug.HierarchyTraceType.ON_MEASURE);
            }
            onMeasure(widthMeasureSpec, heightMeasureSpec);
            if ((mPrivateFlags & MEASURED_DIMENSION_SET) != MEASURED_DIMENSION_SET) {
                throw new IllegalStateException("onMeasure() did not set the"
                        + " measured dimension by calling"
                        + " setMeasuredDimension()");
            }
            mPrivateFlags |= LAYOUT_REQUIRED;
        }
        mOldWidthMeasureSpec = widthMeasureSpec;
        mOldHeightMeasureSpec = heightMeasureSpec;
    }
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec),
                getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec));
    }
    protected final void setMeasuredDimension(int measuredWidth, int measuredHeight) {
        mMeasuredWidth = measuredWidth;
        mMeasuredHeight = measuredHeight;
        mPrivateFlags |= MEASURED_DIMENSION_SET;
    }
    public static int resolveSize(int size, int measureSpec) {
        int result = size;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize =  MeasureSpec.getSize(measureSpec);
        switch (specMode) {
        case MeasureSpec.UNSPECIFIED:
            result = size;
            break;
        case MeasureSpec.AT_MOST:
            result = Math.min(size, specSize);
            break;
        case MeasureSpec.EXACTLY:
            result = specSize;
            break;
        }
        return result;
    }
    public static int getDefaultSize(int size, int measureSpec) {
        int result = size;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize =  MeasureSpec.getSize(measureSpec);
        switch (specMode) {
        case MeasureSpec.UNSPECIFIED:
            result = size;
            break;
        case MeasureSpec.AT_MOST:
        case MeasureSpec.EXACTLY:
            result = specSize;
            break;
        }
        return result;
    }
    protected int getSuggestedMinimumHeight() {
        int suggestedMinHeight = mMinHeight;
        if (mBGDrawable != null) {
            final int bgMinHeight = mBGDrawable.getMinimumHeight();
            if (suggestedMinHeight < bgMinHeight) {
                suggestedMinHeight = bgMinHeight;
            }
        }
        return suggestedMinHeight;
    }
    protected int getSuggestedMinimumWidth() {
        int suggestedMinWidth = mMinWidth;
        if (mBGDrawable != null) {
            final int bgMinWidth = mBGDrawable.getMinimumWidth();
            if (suggestedMinWidth < bgMinWidth) {
                suggestedMinWidth = bgMinWidth;
            }
        }
        return suggestedMinWidth;
    }
    public void setMinimumHeight(int minHeight) {
        mMinHeight = minHeight;
    }
    public void setMinimumWidth(int minWidth) {
        mMinWidth = minWidth;
    }
    public Animation getAnimation() {
        return mCurrentAnimation;
    }
    public void startAnimation(Animation animation) {
        animation.setStartTime(Animation.START_ON_FIRST_FRAME);
        setAnimation(animation);
        invalidate();
    }
    public void clearAnimation() {
        if (mCurrentAnimation != null) {
            mCurrentAnimation.detach();
        }
        mCurrentAnimation = null;
    }
    public void setAnimation(Animation animation) {
        mCurrentAnimation = animation;
        if (animation != null) {
            animation.reset();
        }
    }
    protected void onAnimationStart() {
        mPrivateFlags |= ANIMATION_STARTED;
    }
    protected void onAnimationEnd() {
        mPrivateFlags &= ~ANIMATION_STARTED;
    }
    protected boolean onSetAlpha(int alpha) {
        return false;
    }
    public boolean gatherTransparentRegion(Region region) {
        final AttachInfo attachInfo = mAttachInfo;
        if (region != null && attachInfo != null) {
            final int pflags = mPrivateFlags;
            if ((pflags & SKIP_DRAW) == 0) {
                final int[] location = attachInfo.mTransparentLocation;
                getLocationInWindow(location);
                region.op(location[0], location[1], location[0] + mRight - mLeft,
                        location[1] + mBottom - mTop, Region.Op.DIFFERENCE);
            } else if ((pflags & ONLY_DRAWS_BACKGROUND) != 0 && mBGDrawable != null) {
                applyDrawableToTransparentRegion(mBGDrawable, region);
            }
        }
        return true;
    }
    public void playSoundEffect(int soundConstant) {
        if (mAttachInfo == null || mAttachInfo.mRootCallbacks == null || !isSoundEffectsEnabled()) {
            return;
        }
        mAttachInfo.mRootCallbacks.playSoundEffect(soundConstant);
    }
    public boolean performHapticFeedback(int feedbackConstant) {
        return performHapticFeedback(feedbackConstant, 0);
    }
    public boolean performHapticFeedback(int feedbackConstant, int flags) {
        if (mAttachInfo == null) {
            return false;
        }
        if ((flags&HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING) == 0
                && !isHapticFeedbackEnabled()) {
            return false;
        }
        return mAttachInfo.mRootCallbacks.performHapticFeedback(
                feedbackConstant,
                (flags&HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING) != 0);
    }
    public void onCloseSystemDialogs(String reason) {
    }
    public void applyDrawableToTransparentRegion(Drawable dr, Region region) {
        if (DBG) {
            Log.i("View", "Getting transparent region for: " + this);
        }
        final Region r = dr.getTransparentRegion();
        final Rect db = dr.getBounds();
        final AttachInfo attachInfo = mAttachInfo;
        if (r != null && attachInfo != null) {
            final int w = getRight()-getLeft();
            final int h = getBottom()-getTop();
            if (db.left > 0) {
                r.op(0, 0, db.left, h, Region.Op.UNION);
            }
            if (db.right < w) {
                r.op(db.right, 0, w, h, Region.Op.UNION);
            }
            if (db.top > 0) {
                r.op(0, 0, w, db.top, Region.Op.UNION);
            }
            if (db.bottom < h) {
                r.op(0, db.bottom, w, h, Region.Op.UNION);
            }
            final int[] location = attachInfo.mTransparentLocation;
            getLocationInWindow(location);
            r.translate(location[0], location[1]);
            region.op(r, Region.Op.INTERSECT);
        } else {
            region.op(db, Region.Op.DIFFERENCE);
        }
    }
    private void postCheckForLongClick(int delayOffset) {
        mHasPerformedLongPress = false;
        if (mPendingCheckForLongPress == null) {
            mPendingCheckForLongPress = new CheckForLongPress();
        }
        mPendingCheckForLongPress.rememberWindowAttachCount();
        postDelayed(mPendingCheckForLongPress,
                ViewConfiguration.getLongPressTimeout() - delayOffset);
    }
    private static int[] stateSetUnion(final int[] stateSet1,
                                       final int[] stateSet2) {
        final int stateSet1Length = stateSet1.length;
        final int stateSet2Length = stateSet2.length;
        final int[] newSet = new int[stateSet1Length + stateSet2Length];
        int k = 0;
        int i = 0;
        int j = 0;
        for (int viewState : R.styleable.ViewDrawableStates) {
            if (i < stateSet1Length && stateSet1[i] == viewState) {
                newSet[k++] = viewState;
                i++;
            } else if (j < stateSet2Length && stateSet2[j] == viewState) {
                newSet[k++] = viewState;
                j++;
            }
            if (k > 1) {
                assert(newSet[k - 1] > newSet[k - 2]);
            }
        }
        return newSet;
    }
    public static View inflate(Context context, int resource, ViewGroup root) {
        LayoutInflater factory = LayoutInflater.from(context);
        return factory.inflate(resource, root);
    }
    public static class MeasureSpec {
        private static final int MODE_SHIFT = 30;
        private static final int MODE_MASK  = 0x3 << MODE_SHIFT;
        public static final int UNSPECIFIED = 0 << MODE_SHIFT;
        public static final int EXACTLY     = 1 << MODE_SHIFT;
        public static final int AT_MOST     = 2 << MODE_SHIFT;
        public static int makeMeasureSpec(int size, int mode) {
            return size + mode;
        }
        public static int getMode(int measureSpec) {
            return (measureSpec & MODE_MASK);
        }
        public static int getSize(int measureSpec) {
            return (measureSpec & ~MODE_MASK);
        }
        public static String toString(int measureSpec) {
            int mode = getMode(measureSpec);
            int size = getSize(measureSpec);
            StringBuilder sb = new StringBuilder("MeasureSpec: ");
            if (mode == UNSPECIFIED)
                sb.append("UNSPECIFIED ");
            else if (mode == EXACTLY)
                sb.append("EXACTLY ");
            else if (mode == AT_MOST)
                sb.append("AT_MOST ");
            else
                sb.append(mode).append(" ");
            sb.append(size);
            return sb.toString();
        }
    }
    class CheckForLongPress implements Runnable {
        private int mOriginalWindowAttachCount;
        public void run() {
            if (isPressed() && (mParent != null)
                    && mOriginalWindowAttachCount == mWindowAttachCount) {
                if (performLongClick()) {
                    mHasPerformedLongPress = true;
                }
            }
        }
        public void rememberWindowAttachCount() {
            mOriginalWindowAttachCount = mWindowAttachCount;
        }
    }
    private final class CheckForTap implements Runnable {
        public void run() {
            mPrivateFlags &= ~PREPRESSED;
            mPrivateFlags |= PRESSED;
            refreshDrawableState();
            if ((mViewFlags & LONG_CLICKABLE) == LONG_CLICKABLE) {
                postCheckForLongClick(ViewConfiguration.getTapTimeout());
            }
        }
    }
    private final class PerformClick implements Runnable {
        public void run() {
            performClick();
        }
    }
    public interface OnKeyListener {
        boolean onKey(View v, int keyCode, KeyEvent event);
    }
    public interface OnTouchListener {
        boolean onTouch(View v, MotionEvent event);
    }
    public interface OnLongClickListener {
        boolean onLongClick(View v);
    }
    public interface OnFocusChangeListener {
        void onFocusChange(View v, boolean hasFocus);
    }
    public interface OnClickListener {
        void onClick(View v);
    }
    public interface OnCreateContextMenuListener {
        void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo);
    }
    private final class UnsetPressedState implements Runnable {
        public void run() {
            setPressed(false);
        }
    }
    public static class BaseSavedState extends AbsSavedState {
        public BaseSavedState(Parcel source) {
            super(source);
        }
        public BaseSavedState(Parcelable superState) {
            super(superState);
        }
        public static final Parcelable.Creator<BaseSavedState> CREATOR =
                new Parcelable.Creator<BaseSavedState>() {
            public BaseSavedState createFromParcel(Parcel in) {
                return new BaseSavedState(in);
            }
            public BaseSavedState[] newArray(int size) {
                return new BaseSavedState[size];
            }
        };
    }
    static class AttachInfo {
        interface Callbacks {
            void playSoundEffect(int effectId);
            boolean performHapticFeedback(int effectId, boolean always);
        }
        static class InvalidateInfo implements Poolable<InvalidateInfo> {
            private static final int POOL_LIMIT = 10;
            private static final Pool<InvalidateInfo> sPool = Pools.synchronizedPool(
                    Pools.finitePool(new PoolableManager<InvalidateInfo>() {
                        public InvalidateInfo newInstance() {
                            return new InvalidateInfo();
                        }
                        public void onAcquired(InvalidateInfo element) {
                        }
                        public void onReleased(InvalidateInfo element) {
                        }
                    }, POOL_LIMIT)
            );
            private InvalidateInfo mNext;
            View target;
            int left;
            int top;
            int right;
            int bottom;
            public void setNextPoolable(InvalidateInfo element) {
                mNext = element;
            }
            public InvalidateInfo getNextPoolable() {
                return mNext;
            }
            static InvalidateInfo acquire() {
                return sPool.acquire();
            }
            void release() {
                sPool.release(this);
            }
        }
        final IWindowSession mSession;
        final IWindow mWindow;
        final IBinder mWindowToken;
        final Callbacks mRootCallbacks;
        View mRootView;
        IBinder mPanelParentWindowToken;
        Surface mSurface;
        float mApplicationScale;
        boolean mScalingRequired;
        int mWindowLeft;
        int mWindowTop;
        boolean mTranslucentWindow;        
        final Rect mContentInsets = new Rect();
        final Rect mVisibleInsets = new Rect();
        final ViewTreeObserver.InternalInsetsInfo mGivenInternalInsets
                = new ViewTreeObserver.InternalInsetsInfo();
        final ArrayList<View> mScrollContainers = new ArrayList<View>();
        final KeyEvent.DispatcherState mKeyDispatchState
                = new KeyEvent.DispatcherState();
        boolean mHasWindowFocus;
        int mWindowVisibility;
        long mDrawingTime;
        boolean mIgnoreDirtyState;
        boolean mInTouchMode;
        boolean mRecomputeGlobalAttributes;
        boolean mKeepScreenOn;
        boolean mViewVisibilityChanged;
        boolean mViewScrollChanged;
        final int[] mTransparentLocation = new int[2];
        final int[] mInvalidateChildLocation = new int[2];
        final ViewTreeObserver mTreeObserver = new ViewTreeObserver();
        Canvas mCanvas;
        final Handler mHandler;
        static final int INVALIDATE_MSG = 0x1;
        static final int INVALIDATE_RECT_MSG = 0x2;
        final Rect mTmpInvalRect = new Rect();
        final ArrayList<View> mFocusablesTempList = new ArrayList<View>(24);
        AttachInfo(IWindowSession session, IWindow window,
                Handler handler, Callbacks effectPlayer) {
            mSession = session;
            mWindow = window;
            mWindowToken = window.asBinder();
            mHandler = handler;
            mRootCallbacks = effectPlayer;
        }
    }
    private static class ScrollabilityCache implements Runnable {
        public static final int OFF = 0;
        public static final int ON = 1;
        public static final int FADING = 2;
        public boolean fadeScrollBars;
        public int fadingEdgeLength;
        public int scrollBarDefaultDelayBeforeFade;
        public int scrollBarFadeDuration;
        public int scrollBarSize;
        public ScrollBarDrawable scrollBar;
        public float[] interpolatorValues;
        public View host;
        public final Paint paint;
        public final Matrix matrix;
        public Shader shader;
        public final Interpolator scrollBarInterpolator = new Interpolator(1, 2);
        private final float[] mOpaque = {255.0f};
        private final float[] mTransparent = {0.0f};
        public long fadeStartTime;
        public int state = OFF;
        private int mLastColor;
        public ScrollabilityCache(ViewConfiguration configuration, View host) {
            fadingEdgeLength = configuration.getScaledFadingEdgeLength();
            scrollBarSize = configuration.getScaledScrollBarSize();
            scrollBarDefaultDelayBeforeFade = ViewConfiguration.getScrollDefaultDelay();
            scrollBarFadeDuration = ViewConfiguration.getScrollBarFadeDuration();
            paint = new Paint();
            matrix = new Matrix();
            shader = new LinearGradient(0, 0, 0, 1, 0xFF000000, 0, Shader.TileMode.CLAMP);
            paint.setShader(shader);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
            this.host = host;
        }
        public void setFadeColor(int color) {
            if (color != 0 && color != mLastColor) {
                mLastColor = color;
                color |= 0xFF000000;
                shader = new LinearGradient(0, 0, 0, 1, color | 0xFF000000,
                        color & 0x00FFFFFF, Shader.TileMode.CLAMP);
                paint.setShader(shader);
                paint.setXfermode(null);
            }
        }
        public void run() {
            long now = AnimationUtils.currentAnimationTimeMillis();
            if (now >= fadeStartTime) {
                int nextFrame = (int) now;
                int framesCount = 0;
                Interpolator interpolator = scrollBarInterpolator;
                interpolator.setKeyFrame(framesCount++, nextFrame, mOpaque);
                nextFrame += scrollBarFadeDuration;
                interpolator.setKeyFrame(framesCount, nextFrame, mTransparent);
                state = FADING;
                host.invalidate();
            }
        }
    }
}
