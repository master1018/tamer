public class HVGALayoutParameters implements LayoutParameters {
    private static final String TAG = "HVGALayoutParameters";
    private static final boolean DEBUG = false;
    private static final boolean LOCAL_LOGV = DEBUG ? Config.LOGD : Config.LOGV;
    private int mType = -1;
    private static final int IMAGE_HEIGHT_LANDSCAPE = 240;
    private static final int TEXT_HEIGHT_LANDSCAPE  = 80;
    private static final int IMAGE_HEIGHT_PORTRAIT  = 320;
    private static final int TEXT_HEIGHT_PORTRAIT   = 160;
    public HVGALayoutParameters(int type) {
        if ((type != HVGA_LANDSCAPE) && (type != HVGA_PORTRAIT)) {
            throw new IllegalArgumentException(
                    "Bad layout type detected: " + type);
        }
        if (LOCAL_LOGV) {
            Log.v(TAG, "HVGALayoutParameters.<init>(" + type + ").");
        }
        mType = type;
    }
    public int getWidth() {
        return mType == HVGA_LANDSCAPE ? HVGA_LANDSCAPE_WIDTH
                                       : HVGA_PORTRAIT_WIDTH;
    }
    public int getHeight() {
        return mType == HVGA_LANDSCAPE ? HVGA_LANDSCAPE_HEIGHT
                                       : HVGA_PORTRAIT_HEIGHT;
    }
    public int getImageHeight() {
        return mType == HVGA_LANDSCAPE ? IMAGE_HEIGHT_LANDSCAPE
                                       : IMAGE_HEIGHT_PORTRAIT;
    }
    public int getTextHeight() {
        return mType == HVGA_LANDSCAPE ? TEXT_HEIGHT_LANDSCAPE
                                       : TEXT_HEIGHT_PORTRAIT;
    }
    public int getType() {
        return mType;
    }
    public String getTypeDescription() {
        return mType == HVGA_LANDSCAPE ? "HVGA-L" : "HVGA-P";
    }
}
