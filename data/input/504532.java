public final class GpsSatellite {
    boolean mValid;
    boolean mHasEphemeris;
    boolean mHasAlmanac;
    boolean mUsedInFix;
    int mPrn;
    float mSnr;
    float mElevation;
    float mAzimuth;
    GpsSatellite(int prn) {
        mPrn = prn;
    }
    void setStatus(GpsSatellite satellite) {
        mValid = satellite.mValid;
        mHasEphemeris = satellite.mHasEphemeris;
        mHasAlmanac = satellite.mHasAlmanac;
        mUsedInFix = satellite.mUsedInFix;
        mSnr = satellite.mSnr;
        mElevation = satellite.mElevation;
        mAzimuth = satellite.mAzimuth;
    }
    public int getPrn() {
        return mPrn;
    }
    public float getSnr() {
        return mSnr;
    }
    public float getElevation() {
        return mElevation;
    }
    public float getAzimuth() {
        return mAzimuth;
    }
    public boolean hasEphemeris() {
        return mHasEphemeris;
    }
    public boolean hasAlmanac() {
        return mHasAlmanac;
    }
    public boolean usedInFix() {
        return mUsedInFix;
    }
}
