public class ScaleXSpan extends MetricAffectingSpan implements ParcelableSpan {
	private final float mProportion;
	public ScaleXSpan(float proportion) {
		mProportion = proportion;
	}
    public ScaleXSpan(Parcel src) {
        mProportion = src.readFloat();
    }
    public int getSpanTypeId() {
        return TextUtils.SCALE_X_SPAN;
    }
    public int describeContents() {
        return 0;
    }
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloat(mProportion);
    }
	public float getScaleX() {
		return mProportion;
	}
	@Override
	public void updateDrawState(TextPaint ds) {
		ds.setTextScaleX(ds.getTextScaleX() * mProportion);
	}
	@Override
	public void updateMeasureState(TextPaint ds) {
		ds.setTextScaleX(ds.getTextScaleX() * mProportion);
	}
}
