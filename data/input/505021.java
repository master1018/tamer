public class UnderlineSpan extends CharacterStyle
        implements UpdateAppearance, ParcelableSpan {
    public UnderlineSpan() {
    }
    public UnderlineSpan(Parcel src) {
    }
    public int getSpanTypeId() {
        return TextUtils.UNDERLINE_SPAN;
    }
    public int describeContents() {
        return 0;
    }
    public void writeToParcel(Parcel dest, int flags) {
    }
	@Override
	public void updateDrawState(TextPaint ds) {
		ds.setUnderlineText(true);
	}
}
