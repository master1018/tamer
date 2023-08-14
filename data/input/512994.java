public class RelativeSizeSpan extends MetricAffectingSpan implements ParcelableSpan {
	private final float mProportion;
	public RelativeSizeSpan(float proportion) {
		mProportion = proportion;
	}
    public RelativeSizeSpan(Parcel src) {
        mProportion = src.readFloat();
    }
    public int getSpanTypeId() {
        return TextUtils.RELATIVE_SIZE_SPAN;
    }
    public int describeContents() {
        return 0;
    }
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloat(mProportion);
    }
	public float getSizeChange() {
		return mProportion;
	}
	@Override
	public void updateDrawState(TextPaint ds) {
		ds.setTextSize(ds.getTextSize() * mProportion);
	}
	@Override
	public void updateMeasureState(TextPaint ds) {
		ds.setTextSize(ds.getTextSize() * mProportion);
	}
}
