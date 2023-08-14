public class StrikethroughSpan extends CharacterStyle
        implements UpdateAppearance, ParcelableSpan {
    public StrikethroughSpan() {
    }
    public StrikethroughSpan(Parcel src) {
    }
    public int getSpanTypeId() {
        return TextUtils.STRIKETHROUGH_SPAN;
    }
    public int describeContents() {
        return 0;
    }
    public void writeToParcel(Parcel dest, int flags) {
    }
	@Override
	public void updateDrawState(TextPaint ds) {
		ds.setStrikeThruText(true);
	}
}
