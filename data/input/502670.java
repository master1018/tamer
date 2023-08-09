public class TestActivity extends Activity {
    private final static String TAG = "TestActivity";
    TestView mView;
    boolean mToggle;
    int mCount;
    final static int PAUSE_DELAY = 100;
    Runnable mRunnable = new Runnable() {
        public void run() {
        if (mToggle) {
            Log.w(TAG, "****** step " + mCount + " resume");
            mCount++;
            mView.onResume();
        } else {
            Log.w(TAG, "step " + mCount + " pause");
            mView.onPause();
        }
        mToggle = ! mToggle;
        mView.postDelayed(mRunnable, PAUSE_DELAY);
        }
    };
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mView = new TestView(getApplication());
	    mView.setFocusableInTouchMode(true);
	    setContentView(mView);
        mView.postDelayed(mRunnable, PAUSE_DELAY);
    }
    @Override
    protected void onPause() {
        super.onPause();
        mView.onPause();
    }
    @Override
    protected void onResume() {
        super.onResume();
        mView.onResume();
    }
}
