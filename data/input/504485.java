public class SubscriptSpan extends MetricAffectingSpan implements ParcelableSpan {
    public SubscriptSpan() {
    }
    public SubscriptSpan(Parcel src) {
    }
    public int getSpanTypeId() {
        return TextUtils.SUBSCRIPT_SPAN;
    }
    public int describeContents() {
        return 0;
    }
    public void writeToParcel(Parcel dest, int flags) {
    }
    @Override
    public void updateDrawState(TextPaint tp) {
        tp.baselineShift -= (int) (tp.ascent() / 2);
    }
    @Override
    public void updateMeasureState(TextPaint tp) {
        tp.baselineShift -= (int) (tp.ascent() / 2);
    }
}
