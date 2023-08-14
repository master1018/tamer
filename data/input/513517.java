public class PackageInfoLite implements Parcelable {
    public String packageName;
    public int recommendedInstallLocation;
    public int installLocation;
    public PackageInfoLite() {
    }
    public String toString() {
        return "PackageInfoLite{"
            + Integer.toHexString(System.identityHashCode(this))
            + " " + packageName + "}";
    }
    public int describeContents() {
        return 0;
    }
    public void writeToParcel(Parcel dest, int parcelableFlags) {
        dest.writeString(packageName);
        dest.writeInt(recommendedInstallLocation);
        dest.writeInt(installLocation);
    }
    public static final Parcelable.Creator<PackageInfoLite> CREATOR
            = new Parcelable.Creator<PackageInfoLite>() {
        public PackageInfoLite createFromParcel(Parcel source) {
            return new PackageInfoLite(source);
        }
        public PackageInfoLite[] newArray(int size) {
            return new PackageInfoLite[size];
        }
    };
    private PackageInfoLite(Parcel source) {
        packageName = source.readString();
        recommendedInstallLocation = source.readInt();
        installLocation = source.readInt();
    }
}
