public class ForegroundColorSpan extends CharacterStyle
        implements UpdateAppearance, ParcelableSpan {
    private final int mColor;
	public ForegroundColorSpan(int color) {
		mColor = color;
	}
    public ForegroundColorSpan(Parcel src) {
        mColor = src.readInt();
    }
    public int getSpanTypeId() {
        return TextUtils.FOREGROUND_COLOR_SPAN;
    }
    public int describeContents() {
        return 0;
    }
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mColor);
    }
	public int getForegroundColor() {
		return mColor;
	}
	@Override
	public void updateDrawState(TextPaint ds) {
		ds.setColor(mColor);
	}
}
