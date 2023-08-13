public final class CompletionInfo implements Parcelable {
    static final String TAG = "CompletionInfo";
    final long mId;
    final int mPosition;
    final CharSequence mText;
    final CharSequence mLabel;
    public CompletionInfo(long id, int index, CharSequence text) {
        mId = id;
        mPosition = index;
        mText = text;
        mLabel = null;
    }
    public CompletionInfo(long id, int index, CharSequence text, CharSequence label) {
        mId = id;
        mPosition = index;
        mText = text;
        mLabel = label;
    }
    CompletionInfo(Parcel source) {
        mId = source.readLong();
        mPosition = source.readInt();
        mText = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(source);
        mLabel = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(source);
    }
    public long getId() {
        return mId;
    }
    public int getPosition() {
        return mPosition;
    }
    public CharSequence getText() {
        return mText;
    }
    public CharSequence getLabel() {
        return mLabel;
    }
    @Override
    public String toString() {
        return "CompletionInfo{#" + mPosition + " \"" + mText
                + "\" id=" + mId + " label=" + mLabel + "}";
    }
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(mId);
        dest.writeInt(mPosition);
        TextUtils.writeToParcel(mText, dest, flags);
        TextUtils.writeToParcel(mLabel, dest, flags);
    }
    public static final Parcelable.Creator<CompletionInfo> CREATOR
            = new Parcelable.Creator<CompletionInfo>() {
        public CompletionInfo createFromParcel(Parcel source) {
            return new CompletionInfo(source);
        }
        public CompletionInfo[] newArray(int size) {
            return new CompletionInfo[size];
        }
    };
    public int describeContents() {
        return 0;
    }
}
