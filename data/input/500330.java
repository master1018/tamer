public class DrawableMarginSpan
implements LeadingMarginSpan, LineHeightSpan
{
    public DrawableMarginSpan(Drawable b) {
        mDrawable = b;
    }
    public DrawableMarginSpan(Drawable b, int pad) {
        mDrawable = b;
        mPad = pad;
    }
    public int getLeadingMargin(boolean first) {
        return mDrawable.getIntrinsicWidth() + mPad;
    }
    public void drawLeadingMargin(Canvas c, Paint p, int x, int dir,
                                  int top, int baseline, int bottom,
                                  CharSequence text, int start, int end,
                                  boolean first, Layout layout) {
        int st = ((Spanned) text).getSpanStart(this);
        int ix = (int)x;
        int itop = (int)layout.getLineTop(layout.getLineForOffset(st));
        int dw = mDrawable.getIntrinsicWidth();
        int dh = mDrawable.getIntrinsicHeight();
        if (dir < 0)
            x -= dw;
        mDrawable.setBounds(ix, itop, ix+dw, itop+dh);
        mDrawable.draw(c);
    }
    public void chooseHeight(CharSequence text, int start, int end,
                             int istartv, int v,
                             Paint.FontMetricsInt fm) {
        if (end == ((Spanned) text).getSpanEnd(this)) {
            int ht = mDrawable.getIntrinsicHeight();
            int need = ht - (v + fm.descent - fm.ascent - istartv);
            if (need > 0)
                fm.descent += need;
            need = ht - (v + fm.bottom - fm.top - istartv);
            if (need > 0)
                fm.bottom += need;
        }
    }
    private Drawable mDrawable;
    private int mPad;
}
