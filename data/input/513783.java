public class Fountain extends Activity {
    private static final String LOG_TAG = "libRS_jni";
    private static final boolean DEBUG  = false;
    private static final boolean LOG_ENABLED = DEBUG ? Config.LOGD : Config.LOGV;
    private FountainView mView;
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mView = new FountainView(this);
        setContentView(mView);
    }
    @Override
    protected void onResume() {
        Log.e("rs", "onResume");
        super.onResume();
        mView.onResume();
    }
    @Override
    protected void onPause() {
        Log.e("rs", "onPause");
        super.onPause();
        mView.onPause();
    }
    static void log(String message) {
        if (LOG_ENABLED) {
            Log.v(LOG_TAG, message);
        }
    }
}
