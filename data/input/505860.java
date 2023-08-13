public class QuoteSpan implements LeadingMarginSpan, ParcelableSpan {
    private static final int STRIPE_WIDTH = 2;
    private static final int GAP_WIDTH = 2;
    private final int mColor;
    public QuoteSpan() {
        super();
        mColor = 0xff0000ff;
    }
    public QuoteSpan(int color) {
        super();
        mColor = color;
    }
    public QuoteSpan(Parcel src) {
        mColor = src.readInt();
    }
    public int getSpanTypeId() {
        return TextUtils.QUOTE_SPAN;
    }
    public int describeContents() {
        return 0;
    }
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mColor);
    }
    public int getColor() {
        return mColor;
    }
    public int getLeadingMargin(boolean first) {
        return STRIPE_WIDTH + GAP_WIDTH;
    }
    public void drawLeadingMargin(Canvas c, Paint p, int x, int dir,
                                  int top, int baseline, int bottom,
                                  CharSequence text, int start, int end,
                                  boolean first, Layout layout) {
        Paint.Style style = p.getStyle();
        int color = p.getColor();
        p.setStyle(Paint.Style.FILL);
        p.setColor(mColor);
        c.drawRect(x, top, x + dir * STRIPE_WIDTH, bottom, p);
        p.setStyle(style);
        p.setColor(color);
    }
}
