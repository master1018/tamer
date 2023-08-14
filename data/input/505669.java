public class GestureDetectorStubActivity extends Activity {
    public boolean isDown;
    public boolean isScroll;
    public boolean isFling;
    public boolean isSingleTapUp;
    public boolean onShowPress;
    public boolean onLongPress;
    public boolean onDoubleTap;
    public boolean onDoubleTapEvent;
    public boolean onSingleTapConfirmed;
    private GestureDetector mGestureDetector;
    private MockOnGestureListener mOnGestureListener;
    private Handler mHandler;
    private View mView;
    private Button mTop;
    private Button mButton;
    private ViewGroup mViewGroup;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mOnGestureListener = new MockOnGestureListener();
        mHandler = new Handler();
        mGestureDetector = new GestureDetector(this, mOnGestureListener, mHandler);
        mGestureDetector.setOnDoubleTapListener(mOnGestureListener);
        mView = new View(this);
        mButton = new Button(this);
        mTop = new Button(this);
        mView.setOnTouchListener(new MockOnTouchListener());
        mViewGroup = new ViewGroup(this) {
            @Override
            protected void onLayout(boolean changed, int l, int t, int r, int b) {
            }
        };
        mViewGroup.addView(mView);
        mViewGroup.addView(mTop);
        mViewGroup.addView(mButton);
        mViewGroup.setOnTouchListener(new MockOnTouchListener());
        setContentView(mViewGroup);
    }
    public View getView() {
        return mView;
    }
    public ViewGroup getViewGroup() {
        return mViewGroup;
    }
    public GestureDetector getGestureDetector() {
        return mGestureDetector;
    }
    public class MockOnGestureListener extends SimpleOnGestureListener {
        public boolean onDown(MotionEvent e) {
            isDown = true;
            return true;
        }
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            isFling = true;
            return true;
        }
        public void onLongPress(MotionEvent e) {
            onLongPress = true;
        }
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            isScroll = true;
            return true;
        }
        public void onShowPress(MotionEvent e) {
            onShowPress = true;
        }
        public boolean onSingleTapUp(MotionEvent e) {
            isSingleTapUp = true;
            return true;
        }
        public boolean onDoubleTap(MotionEvent e) {
            onDoubleTap = true;
            return false;
        }
        public boolean onDoubleTapEvent(MotionEvent e) {
            onDoubleTapEvent = true;
            return false;
        }
        public boolean onSingleTapConfirmed(MotionEvent e) {
            onSingleTapConfirmed = true;
            return false;
        }
    }
    class MockOnTouchListener implements OnTouchListener {
        public boolean onTouch(View v, MotionEvent event) {
            mGestureDetector.onTouchEvent(event);
            return true;
        }
    }
}
