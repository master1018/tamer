public class StyleSpan extends MetricAffectingSpan implements ParcelableSpan {
	private final int mStyle;
	public StyleSpan(int style) {
		mStyle = style;
	}
    public StyleSpan(Parcel src) {
        mStyle = src.readInt();
    }
    public int getSpanTypeId() {
        return TextUtils.STYLE_SPAN;
    }
    public int describeContents() {
        return 0;
    }
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mStyle);
    }
	public int getStyle() {
		return mStyle;
	}
	@Override
    public void updateDrawState(TextPaint ds) {
        apply(ds, mStyle);
    }
	@Override
    public void updateMeasureState(TextPaint paint) {
        apply(paint, mStyle);
    }
    private static void apply(Paint paint, int style) {
        int oldStyle;
        Typeface old = paint.getTypeface();
        if (old == null) {
            oldStyle = 0;
        } else {
            oldStyle = old.getStyle();
        }
        int want = oldStyle | style;
        Typeface tf;
        if (old == null) {
            tf = Typeface.defaultFromStyle(want);
        } else {
            tf = Typeface.create(old, want);
        }
        int fake = want & ~tf.getStyle();
        if ((fake & Typeface.BOLD) != 0) {
            paint.setFakeBoldText(true);
        }
        if ((fake & Typeface.ITALIC) != 0) {
            paint.setTextSkewX(-0.25f);
        }
        paint.setTypeface(tf);
    }
}
