public class ExceptionTextView extends EditText {
    private boolean mFailed = false;
    public ExceptionTextView(Context context) {
        super(context);
    }
    public ExceptionTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public ExceptionTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    public boolean isFailed() {
        return mFailed;
    }
    @Override
    protected void makeNewLayout(int w, int hintWidth,
                                 BoringLayout.Metrics boring,
                                 BoringLayout.Metrics hintMetrics,
                                 int ellipsizedWidth, boolean bringIntoView) {
        if (w < 0) {
            mFailed = true;
            w = 100;
        }
        super.makeNewLayout(w, hintWidth, boring, hintMetrics, ellipsizedWidth,
                            bringIntoView);
    }
}
