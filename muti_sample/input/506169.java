public class PathPermission extends PatternMatcher {
    private final String mReadPermission;
    private final String mWritePermission;
    public PathPermission(String pattern, int type, String readPermission,
            String writePermission) {
        super(pattern, type);
        mReadPermission = readPermission;
        mWritePermission = writePermission;
    }
    public String getReadPermission() {
        return mReadPermission;
    }
    public String getWritePermission() {
        return mWritePermission;
    }
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(mReadPermission);
        dest.writeString(mWritePermission);
    }
    public PathPermission(Parcel src) {
        super(src);
        mReadPermission = src.readString();
        mWritePermission = src.readString();
    }
    public static final Parcelable.Creator<PathPermission> CREATOR
            = new Parcelable.Creator<PathPermission>() {
        public PathPermission createFromParcel(Parcel source) {
            return new PathPermission(source);
        }
        public PathPermission[] newArray(int size) {
            return new PathPermission[size];
        }
    };
}