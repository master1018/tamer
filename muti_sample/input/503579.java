public class Display
{
    public static final int DEFAULT_DISPLAY = 0;
    Display(int display) {
        synchronized (mStaticInit) {
            if (!mInitialized) {
                nativeClassInit();
                mInitialized = true;
            }
        }
        mDisplay = display;
        init(display);
    }
    public int getDisplayId() {
        return mDisplay;
    }
    native static int getDisplayCount();
    native public int getWidth();
    native public int getHeight();
    public int getRotation() {
        return getOrientation();
    }
    @Deprecated native public int getOrientation();
    public int getPixelFormat() {
        return mPixelFormat;
    }
    public float getRefreshRate() {
        return mRefreshRate;
    }
    public void getMetrics(DisplayMetrics outMetrics) {
        outMetrics.widthPixels  = getWidth();
        outMetrics.heightPixels = getHeight();
        outMetrics.density      = mDensity;
        outMetrics.densityDpi   = (int)((mDensity*DisplayMetrics.DENSITY_DEFAULT)+.5f);
        outMetrics.scaledDensity= outMetrics.density;
        outMetrics.xdpi         = mDpiX;
        outMetrics.ydpi         = mDpiY;
    }
    native private static void nativeClassInit();
    private native void init(int display);
    private int         mDisplay;
    private int         mPixelFormat;
    private float       mRefreshRate;
    private float       mDensity;
    private float       mDpiX;
    private float       mDpiY;
    private static final Object mStaticInit = new Object();
    private static boolean mInitialized = false;
    public static Display createMetricsBasedDisplay(int displayId, DisplayMetrics metrics) {
        return new CompatibleDisplay(displayId, metrics);
    }
    private static class CompatibleDisplay extends Display {
        private final DisplayMetrics mMetrics;
        private CompatibleDisplay(int displayId, DisplayMetrics metrics) {
            super(displayId);
            mMetrics = metrics;
        }
        @Override
        public int getWidth() {
            return mMetrics.widthPixels;
        }
        @Override
        public int getHeight() {
            return mMetrics.heightPixels;
        }
    }
}
