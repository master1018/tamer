public abstract class DynamicDrawableSpan extends ReplacementSpan {
    private static final String TAG = "DynamicDrawableSpan";
    public static final int ALIGN_BOTTOM = 0;
    public static final int ALIGN_BASELINE = 1;
    protected final int mVerticalAlignment;
    public DynamicDrawableSpan() {
        mVerticalAlignment = ALIGN_BOTTOM;
    }
    protected DynamicDrawableSpan(int verticalAlignment) {
        mVerticalAlignment = verticalAlignment;
    }
    public int getVerticalAlignment() {
        return mVerticalAlignment;
    }
    public abstract Drawable getDrawable();
    @Override
    public int getSize(Paint paint, CharSequence text,
                         int start, int end,
                         Paint.FontMetricsInt fm) {
        Drawable d = getCachedDrawable();
        Rect rect = d.getBounds();
        if (fm != null) {
            fm.ascent = -rect.bottom; 
            fm.descent = 0; 
            fm.top = fm.ascent;
            fm.bottom = 0;
        }
        return rect.right;
    }
    @Override
    public void draw(Canvas canvas, CharSequence text,
                     int start, int end, float x, 
                     int top, int y, int bottom, Paint paint) {
        Drawable b = getCachedDrawable();
        canvas.save();
        int transY = bottom - b.getBounds().bottom;
        if (mVerticalAlignment == ALIGN_BASELINE) {
            transY -= paint.getFontMetricsInt().descent;
        }
        canvas.translate(x, transY);
        b.draw(canvas);
        canvas.restore();
    }
    private Drawable getCachedDrawable() {
        WeakReference<Drawable> wr = mDrawableRef;
        Drawable d = null;
        if (wr != null)
            d = wr.get();
        if (d == null) {
            d = getDrawable();
            mDrawableRef = new WeakReference<Drawable>(d);
        }
        return d;
    }
    private WeakReference<Drawable> mDrawableRef;
}
