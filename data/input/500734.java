public class Sensor {
    public static final int TYPE_ACCELEROMETER  = 1;
    public static final int TYPE_MAGNETIC_FIELD = 2;
    @Deprecated
    public static final int TYPE_ORIENTATION    = 3;
    public static final int TYPE_GYROSCOPE      = 4;
    public static final int TYPE_LIGHT          = 5;
    public static final int TYPE_PRESSURE       = 6;
    public static final int TYPE_TEMPERATURE    = 7;
    public static final int TYPE_PROXIMITY      = 8;
    public static final int TYPE_ALL             = -1;
    private String  mName;
    private String  mVendor;
    private int     mVersion;
    private int     mHandle;
    private int     mType;
    private float   mMaxRange;
    private float   mResolution;
    private float   mPower;
    private int     mLegacyType;
    Sensor() {
    }
    public String getName() {
        return mName;
    }
    public String getVendor() {
        return mVendor;
    }
    public int getType() {
        return mType;
    }
    public int getVersion() {
        return mVersion;
    }
    public float getMaximumRange() {
        return mMaxRange;
    }
    public float getResolution() {
        return mResolution;
    }
    public float getPower() {
        return mPower;
    }
    int getHandle() {
        return mHandle;
    }
    void setRange(float max, float res) {
        mMaxRange = max;
        mResolution = res;
    }
    void setLegacyType(int legacyType) {
        mLegacyType = legacyType;
    }
    int getLegacyType() {
        return mLegacyType;
    }
}
