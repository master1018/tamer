public class RestoreSet implements Parcelable {
    public String name;
    public String device;
    public long token;
    public RestoreSet() {
    }
    public RestoreSet(String _name, String _dev, long _token) {
        name = _name;
        device = _dev;
        token = _token;
    }
    public int describeContents() {
        return 0;
    }
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(name);
        out.writeString(device);
        out.writeLong(token);
    }
    public static final Parcelable.Creator<RestoreSet> CREATOR
            = new Parcelable.Creator<RestoreSet>() {
        public RestoreSet createFromParcel(Parcel in) {
            return new RestoreSet(in);
        }
        public RestoreSet[] newArray(int size) {
            return new RestoreSet[size];
        }
    };
    private RestoreSet(Parcel in) {
        name = in.readString();
        device = in.readString();
        token = in.readLong();
    }
}
