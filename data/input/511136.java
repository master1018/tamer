public class CompatibilityInfo {
    private static final boolean DBG = false;
    private static final String TAG = "CompatibilityInfo";
    public static final CompatibilityInfo DEFAULT_COMPATIBILITY_INFO = new CompatibilityInfo() {
        @Override
        public void setExpandable(boolean expandable) {
            throw new UnsupportedOperationException("trying to change default compatibility info");
        }
    };
    public static final int DEFAULT_PORTRAIT_WIDTH = 320;
    public static final int DEFAULT_PORTRAIT_HEIGHT = 480;
    private int mCompatibilityFlags;
    private static final int SCALING_REQUIRED = 1; 
    private static final int EXPANDABLE = 2;
    private static final int CONFIGURED_EXPANDABLE = 4; 
    private static final int LARGE_SCREENS = 8;
    private static final int CONFIGURED_LARGE_SCREENS = 16; 
    private static final int SCALING_EXPANDABLE_MASK = SCALING_REQUIRED | EXPANDABLE | LARGE_SCREENS;
    public final int applicationDensity;
    public final float applicationScale;
    public final float applicationInvertedScale;
    public final int appFlags;
    public CompatibilityInfo(ApplicationInfo appInfo) {
        appFlags = appInfo.flags;
        if ((appInfo.flags & ApplicationInfo.FLAG_SUPPORTS_LARGE_SCREENS) != 0) {
            mCompatibilityFlags |= LARGE_SCREENS | CONFIGURED_LARGE_SCREENS;
        }
        if ((appInfo.flags & ApplicationInfo.FLAG_RESIZEABLE_FOR_SCREENS) != 0) {
            mCompatibilityFlags |= EXPANDABLE | CONFIGURED_EXPANDABLE;
        }
        if ((appInfo.flags & ApplicationInfo.FLAG_SUPPORTS_SCREEN_DENSITIES) != 0) {
            applicationDensity = DisplayMetrics.DENSITY_DEVICE;
            applicationScale = 1.0f;
            applicationInvertedScale = 1.0f;
        } else {
            applicationDensity = DisplayMetrics.DENSITY_DEFAULT;
            applicationScale = DisplayMetrics.DENSITY_DEVICE
                    / (float) DisplayMetrics.DENSITY_DEFAULT;
            applicationInvertedScale = 1.0f / applicationScale;
            mCompatibilityFlags |= SCALING_REQUIRED;
        }
    }
    private CompatibilityInfo(int appFlags, int compFlags,
            int dens, float scale, float invertedScale) {
        this.appFlags = appFlags;
        mCompatibilityFlags = compFlags;
        applicationDensity = dens;
        applicationScale = scale;
        applicationInvertedScale = invertedScale;
    }
    private CompatibilityInfo() {
        this(ApplicationInfo.FLAG_SUPPORTS_SMALL_SCREENS
                | ApplicationInfo.FLAG_SUPPORTS_NORMAL_SCREENS
                | ApplicationInfo.FLAG_SUPPORTS_LARGE_SCREENS
                | ApplicationInfo.FLAG_RESIZEABLE_FOR_SCREENS,
                EXPANDABLE | CONFIGURED_EXPANDABLE,
                DisplayMetrics.DENSITY_DEVICE,
                1.0f,
                1.0f);
    }
    public CompatibilityInfo copy() {
        CompatibilityInfo info = new CompatibilityInfo(appFlags, mCompatibilityFlags,
                applicationDensity, applicationScale, applicationInvertedScale);
        return info;
    }
    public void setExpandable(boolean expandable) {
        if (expandable) {
            mCompatibilityFlags |= CompatibilityInfo.EXPANDABLE;
        } else {
            mCompatibilityFlags &= ~CompatibilityInfo.EXPANDABLE;
        }
    }
    public void setLargeScreens(boolean expandable) {
        if (expandable) {
            mCompatibilityFlags |= CompatibilityInfo.LARGE_SCREENS;
        } else {
            mCompatibilityFlags &= ~CompatibilityInfo.LARGE_SCREENS;
        }
    }
    public boolean isConfiguredExpandable() {
        return (mCompatibilityFlags & CompatibilityInfo.CONFIGURED_EXPANDABLE) != 0;
    }
    public boolean isConfiguredLargeScreens() {
        return (mCompatibilityFlags & CompatibilityInfo.CONFIGURED_LARGE_SCREENS) != 0;
    }
    public boolean isScalingRequired() {
        return (mCompatibilityFlags & SCALING_REQUIRED) != 0;
    }
    public boolean supportsScreen() {
        return (mCompatibilityFlags & (EXPANDABLE|LARGE_SCREENS))
                == (EXPANDABLE|LARGE_SCREENS);
    }
    @Override
    public String toString() {
        return "CompatibilityInfo{scale=" + applicationScale +
                ", supports screen=" + supportsScreen() + "}";
    }
    public Translator getTranslator() {
        return isScalingRequired() ? new Translator() : null;
    }
    public class Translator {
        final public float applicationScale;
        final public float applicationInvertedScale;
        private Rect mContentInsetsBuffer = null;
        private Rect mVisibleInsetsBuffer = null;
        Translator(float applicationScale, float applicationInvertedScale) {
            this.applicationScale = applicationScale;
            this.applicationInvertedScale = applicationInvertedScale;
        }
        Translator() {
            this(CompatibilityInfo.this.applicationScale,
                    CompatibilityInfo.this.applicationInvertedScale);
        }
        public void translateRectInScreenToAppWinFrame(Rect rect) {
            rect.scale(applicationInvertedScale);
        }
        public void translateRegionInWindowToScreen(Region transparentRegion) {
            transparentRegion.scale(applicationScale);
        }
        public void translateCanvas(Canvas canvas) {
            if (applicationScale == 1.5f) {
                final float tinyOffset = 2.0f / (3 * 255);
                canvas.translate(tinyOffset, tinyOffset);
            }
            canvas.scale(applicationScale, applicationScale);
        }
        public void translateEventInScreenToAppWindow(MotionEvent event) {
            event.scale(applicationInvertedScale);
        }
        public void translateWindowLayout(WindowManager.LayoutParams params) {
            params.scale(applicationScale);
        }
        public void translateRectInAppWindowToScreen(Rect rect) {
            rect.scale(applicationScale);
        }
        public void translateRectInScreenToAppWindow(Rect rect) {
            rect.scale(applicationInvertedScale);
        }
        public void translateLayoutParamsInAppWindowToScreen(LayoutParams params) {
            params.scale(applicationScale);
        }
        public Rect getTranslatedContentInsets(Rect contentInsets) {
            if (mContentInsetsBuffer == null) mContentInsetsBuffer = new Rect();
            mContentInsetsBuffer.set(contentInsets);
            translateRectInAppWindowToScreen(mContentInsetsBuffer);
            return mContentInsetsBuffer;
        }
        public Rect getTranslatedVisbileInsets(Rect visibleInsets) {
            if (mVisibleInsetsBuffer == null) mVisibleInsetsBuffer = new Rect();
            mVisibleInsetsBuffer.set(visibleInsets);
            translateRectInAppWindowToScreen(mVisibleInsetsBuffer);
            return mVisibleInsetsBuffer;
        }
    }
    public static void updateCompatibleScreenFrame(DisplayMetrics dm, int orientation,
            Rect outRect) {
        int width = dm.widthPixels;
        int portraitHeight = (int) (DEFAULT_PORTRAIT_HEIGHT * dm.density + 0.5f);
        int portraitWidth = (int) (DEFAULT_PORTRAIT_WIDTH * dm.density + 0.5f);
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            int xOffset = (width - portraitHeight) / 2 ;
            outRect.set(xOffset, 0, xOffset + portraitHeight, portraitWidth);
        } else {
            int xOffset = (width - portraitWidth) / 2 ;
            outRect.set(xOffset, 0, xOffset + portraitWidth, portraitHeight);
        }
    }
}
