public class Duration implements Parcelable {
    public int timeInterval;
    public TimeUnit timeUnit;
    public enum TimeUnit {
        MINUTE(0x00),
        SECOND(0x01),
        TENTH_SECOND(0x02);
        private int mValue;
        TimeUnit(int value) {
            mValue = value;
        }
        public int value() {
            return mValue;
        }
    }
    public Duration(int timeInterval, TimeUnit timeUnit) {
        this.timeInterval = timeInterval;
        this.timeUnit = timeUnit;
    }
    private Duration(Parcel in) {
        timeInterval = in.readInt();
        timeUnit = TimeUnit.values()[in.readInt()];
    }
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(timeInterval);
        dest.writeInt(timeUnit.ordinal());
    }
    public int describeContents() {
        return 0;
    }
    public static final Parcelable.Creator<Duration> CREATOR = new Parcelable.Creator<Duration>() {
        public Duration createFromParcel(Parcel in) {
            return new Duration(in);
        }
        public Duration[] newArray(int size) {
            return new Duration[size];
        }
    };
}
