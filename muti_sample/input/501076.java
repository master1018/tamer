public class ImPluginInfo implements Parcelable {
    public String mProviderName;
    public String mPackageName;
    public String mClassName;
    public String mSrcPath;
    public ImPluginInfo(String providerName, String packageName,
            String className, String srcPath) {
        mProviderName = providerName;
        mPackageName = packageName;
        mClassName = className;
        mSrcPath = srcPath;
    }
    public ImPluginInfo(Parcel source) {
        mProviderName = source.readString();
        mPackageName = source.readString();
        mClassName = source.readString();
        mSrcPath = source.readString();
    }
    public int describeContents() {
        return 0;
    }
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mProviderName);
        dest.writeString(mPackageName);
        dest.writeString(mClassName);
        dest.writeString(mSrcPath);
    }
    public static final Parcelable.Creator<ImPluginInfo> CREATOR
            = new Parcelable.Creator<ImPluginInfo>() {
        public ImPluginInfo createFromParcel(Parcel source) {
            return new ImPluginInfo(source);
        }
        public ImPluginInfo[] newArray(int size) {
            return new ImPluginInfo[size];
        }
    };
}
