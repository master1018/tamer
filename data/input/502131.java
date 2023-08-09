public class LabelView extends View {
    public LabelView(Context context) {
        super(context);
        initLabelView();
    }
    private void initLabelView() {
        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextSize(16);
        mTextPaint.setColor(0xFF000000);
        mPaddingLeft = 3;
        mPaddingTop = 3;
        mPaddingRight = 3;
        mPaddingBottom = 3;
    }
    public void setText(String text) {
        mText = text;
        requestLayout();
        invalidate();
    }
    public void setTextSize(int size) {
        mTextPaint.setTextSize(size);
        requestLayout();
        invalidate();
    }
    public void setTextColor(int color) {
        mTextPaint.setColor(color);
        invalidate();
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(measureWidth(widthMeasureSpec),
                measureHeight(heightMeasureSpec));
    }
    private int measureWidth(int measureSpec) {
        int result;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            result = (int) mTextPaint.measureText(mText) + mPaddingLeft
                    + mPaddingRight;
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        return result;
    }
    private int measureHeight(int measureSpec) {
        int result;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        mAscent = (int) mTextPaint.ascent();
        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            result = (int) (-mAscent + mTextPaint.descent()) + mPaddingTop
                    + mPaddingBottom;
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        return result;
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawText(mText, mPaddingLeft, mPaddingTop - mAscent, mTextPaint);
    }
    private Paint mTextPaint;
    private String mText;
    private int mAscent;
}
