public class RequestPermissionHelperActivity extends AlertActivity implements
        DialogInterface.OnClickListener {
    private static final String TAG = "RequestPermissionHelperActivity";
    public static final String ACTION_INTERNAL_REQUEST_BT_ON =
        "com.android.settings.bluetooth.ACTION_INTERNAL_REQUEST_BT_ON";
    public static final String ACTION_INTERNAL_REQUEST_BT_ON_AND_DISCOVERABLE =
        "com.android.settings.bluetooth.ACTION_INTERNAL_REQUEST_BT_ON_AND_DISCOVERABLE";
    private LocalBluetoothManager mLocalManager;
    private int mTimeout;
    private boolean mEnableOnly;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (parseIntent()) {
            finish();
            return;
        }
        createDialog();
    }
    void createDialog() {
        final AlertController.AlertParams p = mAlertParams;
        p.mIconId = android.R.drawable.ic_dialog_info;
        p.mTitle = getString(R.string.bluetooth_permission_request);
        View view = getLayoutInflater().inflate(R.layout.bluetooth_discoverable, null);
        p.mView = view;
        TextView tv = (TextView) view.findViewById(R.id.message);
        if (mEnableOnly) {
            tv.setText(getString(R.string.bluetooth_ask_enablement));
        } else {
            tv.setText(getString(R.string.bluetooth_ask_enablement_and_discovery, mTimeout));
        }
        p.mPositiveButtonText = getString(R.string.yes);
        p.mPositiveButtonListener = this;
        p.mNegativeButtonText = getString(R.string.no);
        p.mNegativeButtonListener = this;
        setupAlert();
    }
    public void onClick(DialogInterface dialog, int which) {
        int returnCode;
        switch (which) {
            case DialogInterface.BUTTON_POSITIVE:
                int btState = 0;
                try {
                    int retryCount = 30;
                    do {
                        btState = mLocalManager.getBluetoothState();
                        Thread.sleep(100);
                    } while (btState == BluetoothAdapter.STATE_TURNING_OFF && --retryCount > 0);
                } catch (InterruptedException e) {
                }
                if (btState == BluetoothAdapter.STATE_TURNING_ON
                        || btState == BluetoothAdapter.STATE_ON
                        || mLocalManager.getBluetoothAdapter().enable()) {
                    returnCode = RequestPermissionActivity.RESULT_BT_STARTING_OR_STARTED;
                } else {
                    returnCode = Activity.RESULT_CANCELED;
                }
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                returnCode = Activity.RESULT_CANCELED;
                break;
            default:
                return;
        }
        setResult(returnCode);
    }
    private boolean parseIntent() {
        Intent intent = getIntent();
        if (intent != null && intent.getAction().equals(ACTION_INTERNAL_REQUEST_BT_ON)) {
            mEnableOnly = true;
        } else if (intent != null
                && intent.getAction().equals(ACTION_INTERNAL_REQUEST_BT_ON_AND_DISCOVERABLE)) {
            mEnableOnly = false;
            mTimeout = intent.getIntExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,
                    BluetoothDiscoverableEnabler.DEFAULT_DISCOVERABLE_TIMEOUT);
        } else {
            setResult(Activity.RESULT_CANCELED);
            return true;
        }
        mLocalManager = LocalBluetoothManager.getInstance(this);
        if (mLocalManager == null) {
            Log.e(TAG, "Error: there's a problem starting bluetooth");
            setResult(Activity.RESULT_CANCELED);
            return true;
        }
        return false;
    }
    @Override
    public void onBackPressed() {
        setResult(Activity.RESULT_CANCELED);
        super.onBackPressed();
    }
}
