public class TypefaceSpan extends MetricAffectingSpan implements ParcelableSpan {
    private final String mFamily;
    public TypefaceSpan(String family) {
        mFamily = family;
    }
    public TypefaceSpan(Parcel src) {
        mFamily = src.readString();
    }
    public int getSpanTypeId() {
        return TextUtils.TYPEFACE_SPAN;
    }
    public int describeContents() {
        return 0;
    }
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mFamily);
    }
    public String getFamily() {
        return mFamily;
    }
    @Override
    public void updateDrawState(TextPaint ds) {
        apply(ds, mFamily);
    }
    @Override
    public void updateMeasureState(TextPaint paint) {
        apply(paint, mFamily);
    }
    private static void apply(Paint paint, String family) {
        int oldStyle;
        Typeface old = paint.getTypeface();
        if (old == null) {
            oldStyle = 0;
        } else {
            oldStyle = old.getStyle();
        }
        Typeface tf = Typeface.create(family, oldStyle);
        int fake = oldStyle & ~tf.getStyle();
        if ((fake & Typeface.BOLD) != 0) {
            paint.setFakeBoldText(true);
        }
        if ((fake & Typeface.ITALIC) != 0) {
            paint.setTextSkewX(-0.25f);
        }
        paint.setTypeface(tf);
    }
}
