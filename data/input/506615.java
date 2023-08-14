public class ButtonGridLayout extends ViewGroup {
    static private final String TAG = "ButtonGridLayout";
    static private final int COLUMNS = 3;
    static private final int ROWS = 4;
    static private final int NUM_CHILDREN = ROWS * COLUMNS;
    private View[] mButtons = new View[NUM_CHILDREN];
    private int mButtonWidth;
    private int mButtonHeight;
    private int mWidthInc;
    private int mHeightInc;
    private int mWidth;
    private int mHeight;
    public ButtonGridLayout(Context context) {
        super(context);
    }
    public ButtonGridLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public ButtonGridLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    @Override
    protected void onFinishInflate () {
        super.onFinishInflate();
        final View[] buttons = mButtons;
        for (int i = 0; i < NUM_CHILDREN; i++) {
            buttons[i] = getChildAt(i);
            buttons[i].measure(MeasureSpec.UNSPECIFIED , MeasureSpec.UNSPECIFIED);
        }
        final View child = buttons[0];
        mButtonWidth = child.getMeasuredWidth();
        mButtonHeight = child.getMeasuredHeight();
        mWidthInc = mButtonWidth + mPaddingLeft + mPaddingRight;
        mHeightInc = mButtonHeight + mPaddingTop + mPaddingBottom;
        mWidth = COLUMNS * mWidthInc;
        mHeight = ROWS * mHeightInc;
    }
    public void setChildrenBackgroundResource(int resid) {
        final View[] buttons = mButtons;
        for (int i = 0; i < NUM_CHILDREN; i++) {
            buttons[i].setBackgroundResource(resid);
        }
    }
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        final View[] buttons = mButtons;
        final int paddingLeft = mPaddingLeft;
        final int buttonWidth = mButtonWidth;
        final int buttonHeight = mButtonHeight;
        final int widthInc = mWidthInc;
        final int heightInc = mHeightInc;
        int i = 0;
        int y = (bottom - top) - mHeight + mPaddingTop;
        for (int row = 0; row < ROWS; row++) {
            int x = paddingLeft;
            for (int col = 0; col < COLUMNS; col++) {
                buttons[i].layout(x, y, x + buttonWidth, y + buttonHeight);
                x += widthInc;
                i++;
            }
            y += heightInc;
        }
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final int width = resolveSize(mWidth, widthMeasureSpec);
        final int height = resolveSize(mHeight, heightMeasureSpec);
        setMeasuredDimension(width, height);
    }
}
