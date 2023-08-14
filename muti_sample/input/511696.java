public class TextViewPerformanceTest extends AndroidTestCase {
    private String mString = "The quick brown fox";
    private Canvas mCanvas;
    private PerformanceTextView mTextView;
    private Paint mPaint;
    private PerformanceLabelView mLabelView;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Bitmap mBitmap = Bitmap.createBitmap(320, 240, Bitmap.Config.RGB_565);
        mCanvas = new Canvas(mBitmap);
        ViewGroup.LayoutParams p = new ViewGroup.LayoutParams(320, 240);
        mLabelView = new PerformanceLabelView(mContext);
        mLabelView.setText(mString);
        mLabelView.measure(View.MeasureSpec.AT_MOST | 320, View.MeasureSpec.AT_MOST | 240);
        mLabelView.mySetFrame(320, 240);
        mLabelView.setLayoutParams(p);
        mLabelView.myDraw(mCanvas);
        mPaint = new Paint();
        mCanvas.save();
        mTextView = new PerformanceTextView(mContext);
        mTextView.setLayoutParams(p);
        mTextView.setText(mString);
        mTextView.mySetFrame(320, 240);
        mTextView.measure(View.MeasureSpec.AT_MOST | 320, View.MeasureSpec.AT_MOST | 240);
    }
    @MediumTest
    public void testDrawTextViewLine() throws Exception {
        mTextView.myDraw(mCanvas);
        mTextView.myDraw(mCanvas);
        mTextView.myDraw(mCanvas);
        mTextView.myDraw(mCanvas);
        mTextView.myDraw(mCanvas);
        mTextView.myDraw(mCanvas);
        mTextView.myDraw(mCanvas);
        mTextView.myDraw(mCanvas);
        mTextView.myDraw(mCanvas);
        mTextView.myDraw(mCanvas);
    }
    @SmallTest
    public void testSpan() throws Exception {
        CharSequence charSeq = new SpannedString(mString);
        mTextView.setText(charSeq);
        mTextView.myDraw(mCanvas);
        mTextView.myDraw(mCanvas);
        mTextView.myDraw(mCanvas);
        mTextView.myDraw(mCanvas);
        mTextView.myDraw(mCanvas);
        mTextView.myDraw(mCanvas);
        mTextView.myDraw(mCanvas);
        mTextView.myDraw(mCanvas);
        mTextView.myDraw(mCanvas);
        mTextView.myDraw(mCanvas);
    }
    @SmallTest
    public void testCanvasDrawText() throws Exception {
        mCanvas.drawText(mString, 30, 30, mPaint);
    }
    @SmallTest
    public void testLabelViewDraw() throws Exception {
        mLabelView.myDraw(mCanvas);
    }
    private class PerformanceTextView extends TextView {
        public PerformanceTextView(Context context) {
            super(context);
        }
        final void myDraw(Canvas c) {
            super.onDraw(c);
        }
        final void mySetFrame(int w, int h) {
            super.setFrame(0, 0, w, h);
        }
    }
    private class PerformanceLabelView extends LabelView {
        public PerformanceLabelView(Context context) {
            super(context);
        }
        final void myDraw(Canvas c) {
            super.onDraw(c);
        }
        final void mySetFrame(int w, int h) {
            super.setFrame(0, 0, w, h);
        }
    }
}
