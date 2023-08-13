public class Criteria implements Parcelable {
    public static final int NO_REQUIREMENT = 0;
    public static final int POWER_LOW = 1;
    public static final int POWER_MEDIUM = 2;
    public static final int POWER_HIGH = 3;
    public static final int ACCURACY_FINE = 1;
    public static final int ACCURACY_COARSE = 2;
    private int mAccuracy              = NO_REQUIREMENT;
    private int mPowerRequirement      = NO_REQUIREMENT;
    private boolean mAltitudeRequired  = false;
    private boolean mBearingRequired   = false;
    private boolean mSpeedRequired     = false;
    private boolean mCostAllowed       = false;
    public Criteria() {}
    public Criteria(Criteria criteria) {
        mAccuracy = criteria.mAccuracy;
        mPowerRequirement = criteria.mPowerRequirement;
        mAltitudeRequired = criteria.mAltitudeRequired;
        mBearingRequired = criteria.mBearingRequired;
        mSpeedRequired = criteria.mSpeedRequired;
        mCostAllowed = criteria.mCostAllowed;
    }
    public void setAccuracy(int accuracy) {
        if (accuracy < NO_REQUIREMENT && accuracy > ACCURACY_COARSE) {
            throw new IllegalArgumentException("accuracy=" + accuracy);
        }
        mAccuracy = accuracy;
    }
    public int getAccuracy() {
        return mAccuracy;
    }
    public void setPowerRequirement(int level) {
        if (level < NO_REQUIREMENT || level > POWER_HIGH) {
            throw new IllegalArgumentException("level=" + level);
        }
        mPowerRequirement = level;
    }
    public int getPowerRequirement() {
        return mPowerRequirement;
    }
    public void setCostAllowed(boolean costAllowed) {
        mCostAllowed = costAllowed;
    }
    public boolean isCostAllowed() {
        return mCostAllowed;
    }
    public void setAltitudeRequired(boolean altitudeRequired) {
        mAltitudeRequired = altitudeRequired;
    }
    public boolean isAltitudeRequired() {
        return mAltitudeRequired;
    }
    public void setSpeedRequired(boolean speedRequired) {
        mSpeedRequired = speedRequired;
    }
    public boolean isSpeedRequired() {
        return mSpeedRequired;
    }
    public void setBearingRequired(boolean bearingRequired) {
        mBearingRequired = bearingRequired;
    }
    public boolean isBearingRequired() {
        return mBearingRequired;
    }
    public static final Parcelable.Creator<Criteria> CREATOR =
        new Parcelable.Creator<Criteria>() {
        public Criteria createFromParcel(Parcel in) {
            Criteria c = new Criteria();
            c.mAccuracy = in.readInt();
            c.mPowerRequirement = in.readInt();
            c.mAltitudeRequired = in.readInt() != 0;
            c.mBearingRequired = in.readInt() != 0;
            c.mSpeedRequired = in.readInt() != 0;
            c.mCostAllowed = in.readInt() != 0;
            return c;
        }
        public Criteria[] newArray(int size) {
            return new Criteria[size];
        }
    };
    public int describeContents() {
        return 0;
    }
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(mAccuracy);
        parcel.writeInt(mPowerRequirement);
        parcel.writeInt(mAltitudeRequired ? 1 : 0);
        parcel.writeInt(mBearingRequired ? 1 : 0);
        parcel.writeInt(mSpeedRequired ? 1 : 0);
        parcel.writeInt(mCostAllowed ? 1 : 0);
    }
}
