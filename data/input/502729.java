public class InternalSelectionView extends View {
    private Paint mPainter = new Paint();
    private Paint mTextPaint = new Paint();
    private Rect mTempRect = new Rect();
    private int mNumRows = 5;
    private int mSelectedRow = 0;
    private final int mEstimatedPixelHeight = 10;
    private Integer mDesiredHeight = null;
    private String mLabel = null;
    public InternalSelectionView(Context context, int numRows, String label) {
        super(context);
        mNumRows = numRows;
        mLabel = label;
        init();
    }
    public InternalSelectionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a =
                context.obtainStyledAttributes(
                        attrs, R.styleable.SelectableRowView);
        mNumRows = a.getInt(R.styleable.SelectableRowView_numRows, 5);
        init();
    }
    private void init() {
        setFocusable(true);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextSize(10);
        mTextPaint.setColor(Color.WHITE);
    }
    public int getNumRows() {
        return mNumRows;
    }
    public int getSelectedRow() {
        return mSelectedRow;
    }
    public void setDesiredHeight(int desiredHeight) {
        mDesiredHeight = desiredHeight;
    }
    public String getLabel() {
        return mLabel;
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(
            measureWidth(widthMeasureSpec),
            measureHeight(heightMeasureSpec));
    }
    private int measureWidth(int measureSpec) {
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        int desiredWidth = 300 + mPaddingLeft + mPaddingRight;
        if (specMode == MeasureSpec.EXACTLY) {
            return specSize;
        } else if (specMode == MeasureSpec.AT_MOST) {
            return desiredWidth < specSize ? desiredWidth : specSize;
        } else {
            return desiredWidth;
        }
    }
    private int measureHeight(int measureSpec) {
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        int desiredHeight = mDesiredHeight != null ?
                mDesiredHeight :
                mNumRows * mEstimatedPixelHeight + mPaddingTop + mPaddingBottom;
        if (specMode == MeasureSpec.EXACTLY) {
            return specSize;
        } else if (specMode == MeasureSpec.AT_MOST) {
            return desiredHeight < specSize ? desiredHeight : specSize;
        } else {
            return desiredHeight;
        }
    }
    @Override
    protected void onDraw(Canvas canvas) {
        int rowHeight = getRowHeight();
        int rectTop = mPaddingTop;
        int rectLeft = mPaddingLeft;
        int rectRight = getWidth() - mPaddingRight;
        for (int i = 0; i < mNumRows; i++) {
            mPainter.setColor(Color.BLACK);
            mPainter.setAlpha(0x20);
            mTempRect.set(rectLeft, rectTop, rectRight, rectTop + rowHeight);
            canvas.drawRect(mTempRect, mPainter);
            if (i == mSelectedRow && hasFocus()) {
                mPainter.setColor(Color.RED);
                mPainter.setAlpha(0xF0);
                mTextPaint.setAlpha(0xFF);
            } else {
                mPainter.setColor(Color.BLACK);
                mPainter.setAlpha(0x40);
                mTextPaint.setAlpha(0xF0);
            }
            mTempRect.set(rectLeft + 2, rectTop + 2,
                    rectRight - 2, rectTop + rowHeight - 2);
            canvas.drawRect(mTempRect, mPainter);
            canvas.drawText(
                    Integer.toString(i),
                    rectLeft + 2,
                    rectTop + 2 - (int) mTextPaint.ascent(),
                    mTextPaint);
            rectTop += rowHeight;
        }
    }
    private int getRowHeight() {
        return (getHeight() - mPaddingTop - mPaddingBottom) / mNumRows;
    }
    public void getRectForRow(Rect rect, int row) {
        final int rowHeight = getRowHeight();
        final int top = mPaddingTop + row * rowHeight;
        rect.set(mPaddingLeft,
                top,
                getWidth() - mPaddingRight,
                top + rowHeight);
    }
    void ensureRectVisible() {
        getRectForRow(mTempRect, mSelectedRow);
        requestRectangleOnScreen(mTempRect);
    }
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch(event.getKeyCode()) {
            case KeyEvent.KEYCODE_DPAD_UP:
                if (mSelectedRow > 0) {
                    mSelectedRow--;
                    invalidate();
                    ensureRectVisible();
                    return true;
                }
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                if (mSelectedRow < (mNumRows - 1)) {
                    mSelectedRow++;
                    invalidate();
                    ensureRectVisible();
                    return true;
                }
                break;
        }
        return false;
    }
    @Override
    public void getFocusedRect(Rect r) {
        getRectForRow(r, mSelectedRow);
    }
    @Override
    protected void onFocusChanged(boolean focused, int direction,
            Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        if (focused) {
            switch (direction) {
                case View.FOCUS_DOWN:
                    mSelectedRow = 0;
                    break;
                case View.FOCUS_UP:
                    mSelectedRow = mNumRows - 1;
                    break;
                case View.FOCUS_LEFT:  
                case View.FOCUS_RIGHT:
                    if (previouslyFocusedRect != null) {
                        int y = previouslyFocusedRect.top
                                + (previouslyFocusedRect.height() / 2);
                        int yPerRow = getHeight() / mNumRows;
                        mSelectedRow = y / yPerRow;
                    } else {
                        mSelectedRow = 0;
                    }
                    break;
                default:
                    return;
            }
            invalidate();
        }
    }
    @Override
    public String toString() {
        if (mLabel != null) {
            return mLabel;
        }
        return super.toString();
    }
}
