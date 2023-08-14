public class ActionMenuButton extends TextView {
    private static final int CORNER_RADIUS = 8;
    private static final int PADDING_H = 5;
    private static final int PADDING_V = 1;
    private final RectF mRect = new RectF();
    private Paint mPaint;
    public ActionMenuButton(Context context) {
        super(context);
        init();
    }
    public ActionMenuButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    public ActionMenuButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }
    private void init() {
        setFocusable(true);
        setPadding(PADDING_H, 0, PADDING_H, PADDING_V);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(getContext().getResources()
                .getColor(R.color.bubble_dark_background));
    }
    @Override
    protected void drawableStateChanged() {
        invalidate();
        super.drawableStateChanged();
    }
    @Override
    public void draw(Canvas canvas) {
        final Layout layout = getLayout();
        final RectF rect = mRect;
        final int left = getCompoundPaddingLeft();
        final int top = getExtendedPaddingTop();
        rect.set(left + layout.getLineLeft(0) - PADDING_H,
                 top + layout.getLineTop(0) - PADDING_V,
                 Math.min(left + layout.getLineRight(0) + PADDING_H,
                          mScrollX + mRight - mLeft),
                 top + layout.getLineBottom(0) + PADDING_V);
        canvas.drawRoundRect(rect, CORNER_RADIUS, CORNER_RADIUS, mPaint);
        super.draw(canvas);
    }
}
