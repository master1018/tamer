public class ContentProviderResult implements Parcelable {
    public final Uri uri;
    public final Integer count;
    public ContentProviderResult(Uri uri) {
        if (uri == null) throw new IllegalArgumentException("uri must not be null");
        this.uri = uri;
        this.count = null;
    }
    public ContentProviderResult(int count) {
        this.count = count;
        this.uri = null;
    }
    public ContentProviderResult(Parcel source) {
        int type = source.readInt();
        if (type == 1) {
            count = source.readInt();
            uri = null;
        } else {
            count = null;
            uri = Uri.CREATOR.createFromParcel(source);
        }
    }
    public void writeToParcel(Parcel dest, int flags) {
        if (uri == null) {
            dest.writeInt(1);
            dest.writeInt(count);
        } else {
            dest.writeInt(2);
            uri.writeToParcel(dest, 0);
        }
    }
    public int describeContents() {
        return 0;
    }
    public static final Creator<ContentProviderResult> CREATOR =
            new Creator<ContentProviderResult>() {
        public ContentProviderResult createFromParcel(Parcel source) {
            return new ContentProviderResult(source);
        }
        public ContentProviderResult[] newArray(int size) {
            return new ContentProviderResult[size];
        }
    };
    public String toString() {
        if (uri != null) {
            return "ContentProviderResult(uri=" + uri.toString() + ")";
        }
        return "ContentProviderResult(count=" + count + ")";
    }
}