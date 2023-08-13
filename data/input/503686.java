public class KeyguardManagerActivity extends Activity {
    private static final String TAG = "KeyguardManagerActivity";
    public static final boolean DEBUG = false;
    private KeyguardManager mKeyguardManager;
    private KeyguardManager.KeyguardLock mKeyLock;
    public int keyCode;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mKeyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        mKeyLock = null;
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (DEBUG) {
            Log.d(TAG, "onResume");
        }
        if (mKeyLock == null) {
            mKeyLock = mKeyguardManager.newKeyguardLock(TAG);
            mKeyLock.disableKeyguard();
            if (DEBUG) {
                Log.d(TAG, "disableKeyguard");
            }
        }
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        this.keyCode = keyCode;
        if (keyCode == KeyEvent.KEYCODE_0 && mKeyLock != null) {
            mKeyLock.reenableKeyguard();
            mKeyLock = null;
            if (DEBUG) {
                Log.d(TAG, "reenableKeyguard");
            }
        }
        if (DEBUG) {
            Log.d(TAG, "onKeyDown");
        }
        return super.onKeyDown(keyCode, event);
    }
}
