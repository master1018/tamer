public class MasterClear extends Activity {
    private static final int KEYGUARD_REQUEST = 55;
    private LayoutInflater mInflater;
    private LockPatternUtils mLockUtils;
    private View mInitialView;
    private Button mInitiateButton;
    private View mFinalView;
    private Button mFinalButton;
    private Button.OnClickListener mFinalClickListener = new Button.OnClickListener() {
            public void onClick(View v) {
                if (Utils.isMonkeyRunning()) {
                    return;
                }
                sendBroadcast(new Intent("android.intent.action.MASTER_CLEAR"));
            }
        };
    private boolean runKeyguardConfirmation(int request) {
        return new ChooseLockSettingsHelper(this)
                .launchConfirmationActivity(request,
                        getText(R.string.master_clear_gesture_prompt),
                        getText(R.string.master_clear_gesture_explanation));
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != KEYGUARD_REQUEST) {
            return;
        }
        if (resultCode == Activity.RESULT_OK) {
            establishFinalConfirmationState();
        } else if (resultCode == Activity.RESULT_CANCELED) {
            finish();
        } else {
            establishInitialState();
        }
    }
    private Button.OnClickListener mInitiateListener = new Button.OnClickListener() {
            public void onClick(View v) {
                if (!runKeyguardConfirmation(KEYGUARD_REQUEST)) {
                    establishFinalConfirmationState();
                }
            }
        };
    private void establishFinalConfirmationState() {
        if (mFinalView == null) {
            mFinalView = mInflater.inflate(R.layout.master_clear_final, null);
            mFinalButton =
                    (Button) mFinalView.findViewById(R.id.execute_master_clear);
            mFinalButton.setOnClickListener(mFinalClickListener);
        }
        setContentView(mFinalView);
    }
    private void establishInitialState() {
        if (mInitialView == null) {
            mInitialView = mInflater.inflate(R.layout.master_clear_primary, null);
            mInitiateButton =
                    (Button) mInitialView.findViewById(R.id.initiate_master_clear);
            mInitiateButton.setOnClickListener(mInitiateListener);
        }
        setContentView(mInitialView);
    }
    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        mInitialView = null;
        mFinalView = null;
        mInflater = LayoutInflater.from(this);
        mLockUtils = new LockPatternUtils(this);
        establishInitialState();
    }
    @Override
    public void onPause() {
        super.onPause();
        establishInitialState();
    }
}
