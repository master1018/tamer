public class ShutdownActivity extends Activity {
    private static final String TAG = "ShutdownActivity";
    private boolean mConfirm;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mConfirm = getIntent().getBooleanExtra(Intent.EXTRA_KEY_CONFIRM, false);
        Slog.i(TAG, "onCreate(): confirm=" + mConfirm);
        Handler h = new Handler();
        h.post(new Runnable() {
            public void run() {
                ShutdownThread.shutdown(ShutdownActivity.this, mConfirm);
            }
        });
    }
}
