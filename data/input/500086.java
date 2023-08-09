public class BoringLayout extends Layout implements TextUtils.EllipsizeCallback {
    public static BoringLayout make(CharSequence source,
                        TextPaint paint, int outerwidth,
                        Alignment align,
                        float spacingmult, float spacingadd,
                        BoringLayout.Metrics metrics, boolean includepad) {
        return new BoringLayout(source, paint, outerwidth, align,
                                spacingmult, spacingadd, metrics,
                                includepad);
    }
    public static BoringLayout make(CharSequence source,
                        TextPaint paint, int outerwidth,
                        Alignment align,
                        float spacingmult, float spacingadd,
                        BoringLayout.Metrics metrics, boolean includepad,
                        TextUtils.TruncateAt ellipsize, int ellipsizedWidth) {
        return new BoringLayout(source, paint, outerwidth, align,
                                spacingmult, spacingadd, metrics,
                                includepad, ellipsize, ellipsizedWidth);
    }
    public BoringLayout replaceOrMake(CharSequence source, TextPaint paint,
                                      int outerwidth, Alignment align,
                                      float spacingmult, float spacingadd,
                                      BoringLayout.Metrics metrics,
                                      boolean includepad) {
        replaceWith(source, paint, outerwidth, align, spacingmult,
                    spacingadd);
        mEllipsizedWidth = outerwidth;
        mEllipsizedStart = 0;
        mEllipsizedCount = 0;
        init(source, paint, outerwidth, align, spacingmult, spacingadd,
             metrics, includepad, true);
        return this;
    }
    public BoringLayout replaceOrMake(CharSequence source, TextPaint paint,
                                      int outerwidth, Alignment align,
                                      float spacingmult, float spacingadd,
                                      BoringLayout.Metrics metrics,
                                      boolean includepad,
                                      TextUtils.TruncateAt ellipsize,
                                      int ellipsizedWidth) {
        boolean trust;
        if (ellipsize == null || ellipsize == TextUtils.TruncateAt.MARQUEE) {
            replaceWith(source, paint, outerwidth, align, spacingmult,
                        spacingadd);
            mEllipsizedWidth = outerwidth;
            mEllipsizedStart = 0;
            mEllipsizedCount = 0;
            trust = true;
        } else {
            replaceWith(TextUtils.ellipsize(source, paint, ellipsizedWidth,
                                           ellipsize, true, this),
                        paint, outerwidth, align, spacingmult,
                        spacingadd);
            mEllipsizedWidth = ellipsizedWidth;
            trust = false;
        }
        init(getText(), paint, outerwidth, align, spacingmult, spacingadd,
             metrics, includepad, trust);
        return this;
    }
    public BoringLayout(CharSequence source,
                        TextPaint paint, int outerwidth,
                        Alignment align,
                        float spacingmult, float spacingadd,
                        BoringLayout.Metrics metrics, boolean includepad) {
        super(source, paint, outerwidth, align, spacingmult, spacingadd);
        mEllipsizedWidth = outerwidth;
        mEllipsizedStart = 0;
        mEllipsizedCount = 0;
        init(source, paint, outerwidth, align, spacingmult, spacingadd,
             metrics, includepad, true);
    }
    public BoringLayout(CharSequence source,
                        TextPaint paint, int outerwidth,
                        Alignment align,
                        float spacingmult, float spacingadd,
                        BoringLayout.Metrics metrics, boolean includepad,
                        TextUtils.TruncateAt ellipsize, int ellipsizedWidth) {
        super(source, paint, outerwidth, align, spacingmult, spacingadd);
        boolean trust;
        if (ellipsize == null || ellipsize == TextUtils.TruncateAt.MARQUEE) {
            mEllipsizedWidth = outerwidth;
            mEllipsizedStart = 0;
            mEllipsizedCount = 0;
            trust = true;
        } else {
            replaceWith(TextUtils.ellipsize(source, paint, ellipsizedWidth,
                                           ellipsize, true, this),
                        paint, outerwidth, align, spacingmult,
                        spacingadd);
            mEllipsizedWidth = ellipsizedWidth;
            trust = false;
        }
        init(getText(), paint, outerwidth, align, spacingmult, spacingadd,
             metrics, includepad, trust);
    }
     void init(CharSequence source,
                            TextPaint paint, int outerwidth,
                            Alignment align,
                            float spacingmult, float spacingadd,
                            BoringLayout.Metrics metrics, boolean includepad,
                            boolean trustWidth) {
        int spacing;
        if (source instanceof String && align == Layout.Alignment.ALIGN_NORMAL) {
            mDirect = source.toString();
        } else {
            mDirect = null;
        }
        mPaint = paint;
        if (includepad) {
            spacing = metrics.bottom - metrics.top;
        } else {
            spacing = metrics.descent - metrics.ascent;
        }
        if (spacingmult != 1 || spacingadd != 0) {
            spacing = (int)(spacing * spacingmult + spacingadd + 0.5f);
        }
        mBottom = spacing;
        if (includepad) {
            mDesc = spacing + metrics.top;
        } else {
            mDesc = spacing + metrics.ascent;
        }
        if (trustWidth) {
            mMax = metrics.width;
        } else {
            synchronized (sTemp) {
                mMax = (int) (FloatMath.ceil(Styled.measureText(paint, sTemp,
                                                source, 0, source.length(),
                                                null)));
            }
        }
        if (includepad) {
            mTopPadding = metrics.top - metrics.ascent;
            mBottomPadding = metrics.bottom - metrics.descent;
        }
    }
    public static Metrics isBoring(CharSequence text,
                                   TextPaint paint) {
        return isBoring(text, paint, null);
    }
    public static Metrics isBoring(CharSequence text, TextPaint paint,
                                   Metrics metrics) {
        char[] temp = TextUtils.obtain(500);
        int len = text.length();
        boolean boring = true;
        outer:
        for (int i = 0; i < len; i += 500) {
            int j = i + 500;
            if (j > len)
                j = len;
            TextUtils.getChars(text, i, j, temp, 0);
            int n = j - i;
            for (int a = 0; a < n; a++) {
                char c = temp[a];
                if (c == '\n' || c == '\t' || c >= FIRST_RIGHT_TO_LEFT) {
                    boring = false;
                    break outer;
                }
            }
        }
        TextUtils.recycle(temp);
        if (boring && text instanceof Spanned) {
            Spanned sp = (Spanned) text;
            Object[] styles = sp.getSpans(0, text.length(), ParagraphStyle.class);
            if (styles.length > 0) {
                boring = false;
            }
        }
        if (boring) {
            Metrics fm = metrics;
            if (fm == null) {
                fm = new Metrics();
            }
            int wid;
            synchronized (sTemp) {
                wid = (int) (FloatMath.ceil(Styled.measureText(paint, sTemp,
                                                text, 0, text.length(), fm)));
            }
            fm.width = wid;
            return fm;
        } else {
            return null;
        }
    }
    @Override public int getHeight() {
        return mBottom;
    }
    @Override public int getLineCount() {
        return 1;
    }
    @Override public int getLineTop(int line) {
        if (line == 0)
            return 0;
        else
            return mBottom;
    }
    @Override public int getLineDescent(int line) {
        return mDesc;
    }
    @Override public int getLineStart(int line) {
        if (line == 0)
            return 0;
        else
            return getText().length();
    }
    @Override public int getParagraphDirection(int line) {
        return DIR_LEFT_TO_RIGHT;
    }
    @Override public boolean getLineContainsTab(int line) {
        return false;
    }
    @Override public float getLineMax(int line) {
        return mMax;
    }
    @Override public final Directions getLineDirections(int line) {
        return Layout.DIRS_ALL_LEFT_TO_RIGHT;
    }
    public int getTopPadding() {
        return mTopPadding;
    }
    public int getBottomPadding() {
        return mBottomPadding;
    }
    @Override
    public int getEllipsisCount(int line) {
        return mEllipsizedCount;
    }
    @Override
    public int getEllipsisStart(int line) {
        return mEllipsizedStart;
    }
    @Override
    public int getEllipsizedWidth() {
        return mEllipsizedWidth;
    }
    @Override
    public void draw(Canvas c, Path highlight, Paint highlightpaint,
                     int cursorOffset) {
        if (mDirect != null && highlight == null) {
            c.drawText(mDirect, 0, mBottom - mDesc, mPaint);
        } else {
            super.draw(c, highlight, highlightpaint, cursorOffset);
        }
    }
    public void ellipsized(int start, int end) {
        mEllipsizedStart = start;
        mEllipsizedCount = end - start;
    }
    private static final char FIRST_RIGHT_TO_LEFT = '\u0590';
    private String mDirect;
    private Paint mPaint;
     int mBottom, mDesc;   
    private int mTopPadding, mBottomPadding;
    private float mMax;
    private int mEllipsizedWidth, mEllipsizedStart, mEllipsizedCount;
    private static final TextPaint sTemp =
                                new TextPaint();
    public static class Metrics extends Paint.FontMetricsInt {
        public int width;
        @Override public String toString() {
            return super.toString() + " width=" + width;
        }
    }
}
