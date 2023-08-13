public class Annotation implements ParcelableSpan {
    private final String mKey;
    private final String mValue;
    public Annotation(String key, String value) {
        mKey = key;
        mValue = value;
    }
    public Annotation(Parcel src) {
        mKey = src.readString();
        mValue = src.readString();
    }
    public int getSpanTypeId() {
        return TextUtils.ANNOTATION;
    }
    public int describeContents() {
        return 0;
    }
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mKey);
        dest.writeString(mValue);
    }
    public String getKey() {
        return mKey;
    }
    public String getValue() {
        return mValue;
    }
}
