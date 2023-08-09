public class AbsoluteSizeSpan extends MetricAffectingSpan implements ParcelableSpan {
    private final int mSize;
    private boolean mDip;
    public AbsoluteSizeSpan(int size) {
        mSize = size;
    }
    public AbsoluteSizeSpan(int size, boolean dip) {
        mSize = size;
        mDip = dip;
    }
    public AbsoluteSizeSpan(Parcel src) {
        mSize = src.readInt();
        mDip = src.readInt() != 0;
    }
    public int getSpanTypeId() {
        return TextUtils.ABSOLUTE_SIZE_SPAN;
    }
    public int describeContents() {
        return 0;
    }
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mSize);
        dest.writeInt(mDip ? 1 : 0);
    }
    public int getSize() {
        return mSize;
    }
    public boolean getDip() {
        return mDip;
    }
    @Override
    public void updateDrawState(TextPaint ds) {
        if (mDip) {
            ds.setTextSize(mSize * ds.density);
        } else {
            ds.setTextSize(mSize);
        }
    }
    @Override
    public void updateMeasureState(TextPaint ds) {
        if (mDip) {
            ds.setTextSize(mSize * ds.density);
        } else {
            ds.setTextSize(mSize);
        }
    }
}
