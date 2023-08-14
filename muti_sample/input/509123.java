public class DisplayMetrics {
    public static final int DENSITY_LOW = 120;
    public static final int DENSITY_MEDIUM = 160;
    public static final int DENSITY_HIGH = 240;
    public static final int DENSITY_DEFAULT = DENSITY_MEDIUM;
    public static final int DENSITY_DEVICE = getDeviceDensity();
    public int widthPixels;
    public int heightPixels;
    public float density;
    public int densityDpi;
    public float scaledDensity;
    public float xdpi;
    public float ydpi;
    public DisplayMetrics() {
    }
    public void setTo(DisplayMetrics o) {
        widthPixels = o.widthPixels;
        heightPixels = o.heightPixels;
        density = o.density;
        densityDpi = o.densityDpi;
        scaledDensity = o.scaledDensity;
        xdpi = o.xdpi;
        ydpi = o.ydpi;
    }
    public void setToDefaults() {
        widthPixels = 0;
        heightPixels = 0;
        density = DENSITY_DEVICE / (float) DENSITY_DEFAULT;
        densityDpi = DENSITY_DEVICE;
        scaledDensity = density;
        xdpi = DENSITY_DEVICE;
        ydpi = DENSITY_DEVICE;
    }
    public void updateMetrics(CompatibilityInfo compatibilityInfo, int orientation,
            int screenLayout) {
        boolean expandable = compatibilityInfo.isConfiguredExpandable();
        boolean largeScreens = compatibilityInfo.isConfiguredLargeScreens();
        if (!expandable) {
            if ((screenLayout&Configuration.SCREENLAYOUT_COMPAT_NEEDED) == 0) {
                expandable = true;
                compatibilityInfo.setExpandable(true);
            } else {
                compatibilityInfo.setExpandable(false);
            }
        }
        if (!largeScreens) {
            if ((screenLayout&Configuration.SCREENLAYOUT_SIZE_MASK)
                    != Configuration.SCREENLAYOUT_SIZE_LARGE) {
                largeScreens = true;
                compatibilityInfo.setLargeScreens(true);
            } else {
                compatibilityInfo.setLargeScreens(false);
            }
        }
        if (!expandable || !largeScreens) {
            int defaultWidth;
            int defaultHeight;
            switch (orientation) {
                case Configuration.ORIENTATION_LANDSCAPE: {
                    defaultWidth = (int)(CompatibilityInfo.DEFAULT_PORTRAIT_HEIGHT * density +
                            0.5f);
                    defaultHeight = (int)(CompatibilityInfo.DEFAULT_PORTRAIT_WIDTH * density +
                            0.5f);
                    break;
                }
                case Configuration.ORIENTATION_PORTRAIT:
                case Configuration.ORIENTATION_SQUARE:
                default: {
                    defaultWidth = (int)(CompatibilityInfo.DEFAULT_PORTRAIT_WIDTH * density +
                            0.5f);
                    defaultHeight = (int)(CompatibilityInfo.DEFAULT_PORTRAIT_HEIGHT * density +
                            0.5f);
                    break;
                }
                case Configuration.ORIENTATION_UNDEFINED: {
                    return;
                }
            }
            if (defaultWidth < widthPixels) {
                widthPixels = defaultWidth;
            }
            if (defaultHeight < heightPixels) {
                heightPixels = defaultHeight;
            }
        }
        if (compatibilityInfo.isScalingRequired()) {
            float invertedRatio = compatibilityInfo.applicationInvertedScale;
            density *= invertedRatio;
            densityDpi = (int)((density*DisplayMetrics.DENSITY_DEFAULT)+.5f);
            scaledDensity *= invertedRatio;
            xdpi *= invertedRatio;
            ydpi *= invertedRatio;
            widthPixels = (int) (widthPixels * invertedRatio + 0.5f);
            heightPixels = (int) (heightPixels * invertedRatio + 0.5f);
        }
    }
    @Override
    public String toString() {
        return "DisplayMetrics{density=" + density + ", width=" + widthPixels +
            ", height=" + heightPixels + ", scaledDensity=" + scaledDensity +
            ", xdpi=" + xdpi + ", ydpi=" + ydpi + "}";
    }
    private static int getDeviceDensity() {
        return SystemProperties.getInt("qemu.sf.lcd_density",
                SystemProperties.getInt("ro.sf.lcd_density", DENSITY_DEFAULT));
    }
}
