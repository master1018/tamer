public class Film extends Activity {
    private static final String LOG_TAG = "libRS_jni";
    private static final boolean DEBUG  = false;
    private static final boolean LOG_ENABLED = DEBUG ? Config.LOGD : Config.LOGV;
    private FilmView mView;
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mView = new FilmView(this);
        setContentView(mView);
    }
    @Override
    protected void onResume() {
        super.onResume();
        mView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        mView.onPause();
        Runtime.getRuntime().exit(0);
    }
    static void log(String message) {
        if (LOG_ENABLED) {
            Log.v(LOG_TAG, message);
        }
    }
}
