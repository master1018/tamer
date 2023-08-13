public class BackgroundColorSpan extends CharacterStyle
        implements UpdateAppearance, ParcelableSpan {
    private final int mColor;
	public BackgroundColorSpan(int color) {
		mColor = color;
	}
    public BackgroundColorSpan(Parcel src) {
        mColor = src.readInt();
    }
    public int getSpanTypeId() {
        return TextUtils.BACKGROUND_COLOR_SPAN;
    }
    public int describeContents() {
        return 0;
    }
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mColor);
    }
	public int getBackgroundColor() {
		return mColor;
	}
	@Override
	public void updateDrawState(TextPaint ds) {
		ds.bgColor = mColor;
	}
}
