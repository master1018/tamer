public class ServiceInfo extends ComponentInfo
        implements Parcelable {
    public String permission;
    public ServiceInfo() {
    }
    public ServiceInfo(ServiceInfo orig) {
        super(orig);
        permission = orig.permission;
    }
    public void dump(Printer pw, String prefix) {
        super.dumpFront(pw, prefix);
        pw.println(prefix + "permission=" + permission);
    }
    public String toString() {
        return "ServiceInfo{"
            + Integer.toHexString(System.identityHashCode(this))
            + " " + name + "}";
    }
    public int describeContents() {
        return 0;
    }
    public void writeToParcel(Parcel dest, int parcelableFlags) {
        super.writeToParcel(dest, parcelableFlags);
        dest.writeString(permission);
    }
    public static final Creator<ServiceInfo> CREATOR =
        new Creator<ServiceInfo>() {
        public ServiceInfo createFromParcel(Parcel source) {
            return new ServiceInfo(source);
        }
        public ServiceInfo[] newArray(int size) {
            return new ServiceInfo[size];
        }
    };
    private ServiceInfo(Parcel source) {
        super(source);
        permission = source.readString();
    }
}
