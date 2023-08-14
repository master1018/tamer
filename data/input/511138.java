class LocationPoint {
    private double mLongitude;
    private double mLatitude;
    private boolean mHasElevation = false;
    private double mElevation;
    final void setLocation(double longitude, double latitude) {
        mLongitude = longitude;
        mLatitude = latitude;
    }
    public final double getLongitude() {
        return mLongitude;
    }
    public final double getLatitude() {
        return mLatitude;
    }
    final void setElevation(double elevation) {
        mElevation = elevation;
        mHasElevation = true;
    }
    public final boolean hasElevation() {
        return mHasElevation;
    }
    public final double getElevation() {
        return mElevation;
    }
}
