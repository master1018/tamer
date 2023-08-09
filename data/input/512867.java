public final class ProviderInfo extends ComponentInfo
        implements Parcelable {
    public String readPermission = null;
    public String writePermission = null;
    public boolean grantUriPermissions = false;
    public PatternMatcher[] uriPermissionPatterns = null;
    public PathPermission[] pathPermissions = null;
    public boolean multiprocess = false;
    public int initOrder = 0;
    @Deprecated
    public boolean isSyncable = false;
    public ProviderInfo() {
    }
    public ProviderInfo(ProviderInfo orig) {
        super(orig);
        authority = orig.authority;
        readPermission = orig.readPermission;
        writePermission = orig.writePermission;
        grantUriPermissions = orig.grantUriPermissions;
        uriPermissionPatterns = orig.uriPermissionPatterns;
        pathPermissions = orig.pathPermissions;
        multiprocess = orig.multiprocess;
        initOrder = orig.initOrder;
        isSyncable = orig.isSyncable;
    }
    public int describeContents() {
        return 0;
    }
    @Override public void writeToParcel(Parcel out, int parcelableFlags) {
        super.writeToParcel(out, parcelableFlags);
        out.writeString(authority);
        out.writeString(readPermission);
        out.writeString(writePermission);
        out.writeInt(grantUriPermissions ? 1 : 0);
        out.writeTypedArray(uriPermissionPatterns, parcelableFlags);
        out.writeTypedArray(pathPermissions, parcelableFlags);
        out.writeInt(multiprocess ? 1 : 0);
        out.writeInt(initOrder);
        out.writeInt(isSyncable ? 1 : 0);
    }
    public static final Parcelable.Creator<ProviderInfo> CREATOR
            = new Parcelable.Creator<ProviderInfo>() {
        public ProviderInfo createFromParcel(Parcel in) {
            return new ProviderInfo(in);
        }
        public ProviderInfo[] newArray(int size) {
            return new ProviderInfo[size];
        }
    };
    public String toString() {
        return "ContentProviderInfo{name=" + authority + " className=" + name
            + " isSyncable=" + (isSyncable ? "true" : "false") + "}";
    }
    private ProviderInfo(Parcel in) {
        super(in);
        authority = in.readString();
        readPermission = in.readString();
        writePermission = in.readString();
        grantUriPermissions = in.readInt() != 0;
        uriPermissionPatterns = in.createTypedArray(PatternMatcher.CREATOR);
        pathPermissions = in.createTypedArray(PathPermission.CREATOR);
        multiprocess = in.readInt() != 0;
        initOrder = in.readInt();
        isSyncable = in.readInt() != 0;
    }
}
