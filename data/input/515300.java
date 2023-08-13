public class DialogTitle extends TextView {
    public DialogTitle(Context context, AttributeSet attrs,
            int defStyle) {
        super(context, attrs, defStyle);
    }
    public DialogTitle(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public DialogTitle(Context context) {
        super(context);
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final Layout layout = getLayout();
        if (layout != null) {
            final int lineCount = layout.getLineCount();
            if (lineCount > 0) {
                final int ellipsisCount = layout.getEllipsisCount(lineCount - 1);
                if (ellipsisCount > 0) {
                    setSingleLine(false);
                    TypedArray a = mContext.obtainStyledAttributes(
                            android.R.style.TextAppearance_Medium,
                            android.R.styleable.TextAppearance);
                    final int textSize = a.getDimensionPixelSize(
                            android.R.styleable.TextAppearance_textSize,
                            (int) (20 * getResources().getDisplayMetrics().density));
                    setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
                    setMaxLines(2);
                    super.onMeasure(widthMeasureSpec, heightMeasureSpec);      
                }
            }
        }
    }
}
