public class IconMarginSpan
implements LeadingMarginSpan, LineHeightSpan
{
    public IconMarginSpan(Bitmap b) {
        mBitmap = b;
    }
    public IconMarginSpan(Bitmap b, int pad) {
        mBitmap = b;
        mPad = pad;
    }
    public int getLeadingMargin(boolean first) {
        return mBitmap.getWidth() + mPad;
    }
    public void drawLeadingMargin(Canvas c, Paint p, int x, int dir,
                                  int top, int baseline, int bottom,
                                  CharSequence text, int start, int end,
                                  boolean first, Layout layout) {
        int st = ((Spanned) text).getSpanStart(this);
        int itop = layout.getLineTop(layout.getLineForOffset(st));
        if (dir < 0)
            x -= mBitmap.getWidth();
        c.drawBitmap(mBitmap, x, itop, p);
    }
    public void chooseHeight(CharSequence text, int start, int end,
                             int istartv, int v,
                             Paint.FontMetricsInt fm) {
        if (end == ((Spanned) text).getSpanEnd(this)) {
            int ht = mBitmap.getHeight();
            int need = ht - (v + fm.descent - fm.ascent - istartv);
            if (need > 0)
                fm.descent += need;
            need = ht - (v + fm.bottom - fm.top - istartv);
            if (need > 0)
                fm.bottom += need;
        }
    }
    private Bitmap mBitmap;
    private int mPad;
}
