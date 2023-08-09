public class MediaFormat extends Activity {
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
                final IMountService service =
                        IMountService.Stub.asInterface(ServiceManager.getService("mount"));
                if (service != null) {
                    new Thread() {
                        public void run() {
                            try {
                                service.formatVolume(Environment.getExternalStorageDirectory().toString());
                            } catch (Exception e) {
                                Log.w("MediaFormat", "Unable to invoke IMountService.formatMedia()");
                            }
                        }
                    }.start();
                } else {
                    Log.w("MediaFormat", "Unable to locate IMountService");
                }
            finish();
            }
        };
    private boolean runKeyguardConfirmation(int request) {
        return new ChooseLockSettingsHelper(this)
                .launchConfirmationActivity(request,
                        getText(R.string.media_format_gesture_prompt),
                        getText(R.string.media_format_gesture_explanation));
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
            mFinalView = mInflater.inflate(R.layout.media_format_final, null);
            mFinalButton =
                    (Button) mFinalView.findViewById(R.id.execute_media_format);
            mFinalButton.setOnClickListener(mFinalClickListener);
        }
        setContentView(mFinalView);
    }
    private void establishInitialState() {
        if (mInitialView == null) {
            mInitialView = mInflater.inflate(R.layout.media_format_primary, null);
            mInitiateButton =
                    (Button) mInitialView.findViewById(R.id.initiate_media_format);
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
