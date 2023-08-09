public class RequestPermissionActivity extends Activity implements
        DialogInterface.OnClickListener {
    private static final String TAG = "RequestPermissionActivity";
    private static final int MAX_DISCOVERABLE_TIMEOUT = 300;
     static final int RESULT_BT_STARTING_OR_STARTED = -1000;
    private static final int REQUEST_CODE_START_BT = 1;
    private LocalBluetoothManager mLocalManager;
    private int mTimeout = BluetoothDiscoverableEnabler.DEFAULT_DISCOVERABLE_TIMEOUT;
    private boolean mNeededToEnableBluetooth;
    private boolean mEnableOnly = false;
    private boolean mUserConfirmed = false;
    private AlertDialog mDialog = null;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null)
                return;
            if (mNeededToEnableBluetooth
                    && BluetoothAdapter.ACTION_STATE_CHANGED.equals(intent.getAction())) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothDevice.ERROR);
                if (state == BluetoothAdapter.STATE_ON) {
                    if (mUserConfirmed) {
                        proceedAndFinish();
                    }
                }
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (parseIntent()) {
            finish();
            return;
        }
        int btState = mLocalManager.getBluetoothState();
        switch (btState) {
            case BluetoothAdapter.STATE_OFF:
            case BluetoothAdapter.STATE_TURNING_OFF:
            case BluetoothAdapter.STATE_TURNING_ON:
                registerReceiver(mReceiver,
                        new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
                Intent i = new Intent();
                i.setClass(this, RequestPermissionHelperActivity.class);
                if (mEnableOnly) {
                    i.setAction(RequestPermissionHelperActivity.ACTION_INTERNAL_REQUEST_BT_ON);
                } else {
                    i.setAction(RequestPermissionHelperActivity.
                            ACTION_INTERNAL_REQUEST_BT_ON_AND_DISCOVERABLE);
                    i.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, mTimeout);
                }
                startActivityForResult(i, REQUEST_CODE_START_BT);
                mNeededToEnableBluetooth = true;
                break;
            case BluetoothAdapter.STATE_ON:
                if (mEnableOnly) {
                    proceedAndFinish();
                    return;
                } else {
                    createDialog();
                    break;
                }
        }
    }
    private void createDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(android.R.drawable.ic_dialog_info);
        builder.setTitle(getString(R.string.bluetooth_permission_request));
        if (mNeededToEnableBluetooth) {
            builder.setMessage(getString(R.string.bluetooth_turning_on));
            builder.setCancelable(false);
        } else {
            builder.setMessage(getString(R.string.bluetooth_ask_enablement_and_discovery, mTimeout));
            builder.setPositiveButton(getString(R.string.yes), this);
            builder.setNegativeButton(getString(R.string.no), this);
        }
        mDialog = builder.create();
        mDialog.show();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != REQUEST_CODE_START_BT) {
            Log.e(TAG, "Unexpected onActivityResult " + requestCode + " " + resultCode);
            setResult(Activity.RESULT_CANCELED);
            finish();
            return;
        }
        if (resultCode != RESULT_BT_STARTING_OR_STARTED) {
            setResult(resultCode);
            finish();
            return;
        }
        mUserConfirmed = true;
        if (mLocalManager.getBluetoothState() == BluetoothAdapter.STATE_ON) {
            proceedAndFinish();
        } else {
            createDialog();
        }
    }
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case DialogInterface.BUTTON_POSITIVE:
                proceedAndFinish();
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                setResult(Activity.RESULT_CANCELED);
                finish();
                break;
        }
    }
    private void proceedAndFinish() {
        int returnCode;
        if (mEnableOnly) {
            returnCode = Activity.RESULT_OK;
        } else if (mLocalManager.getBluetoothAdapter().setScanMode(
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE, mTimeout)) {
            persistDiscoverableEndTimestamp(System.currentTimeMillis() + mTimeout * 1000);
            returnCode = mTimeout;
            if (returnCode < Activity.RESULT_FIRST_USER) {
                returnCode = Activity.RESULT_FIRST_USER;
            }
        } else {
            returnCode = Activity.RESULT_CANCELED;
        }
        if (mDialog != null) {
            mDialog.dismiss();
        }
        setResult(returnCode);
        finish();
    }
    private boolean parseIntent() {
        Intent intent = getIntent();
        if (intent != null && intent.getAction().equals(BluetoothAdapter.ACTION_REQUEST_ENABLE)) {
            mEnableOnly = true;
        } else if (intent != null
                && intent.getAction().equals(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)) {
            mTimeout = intent.getIntExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,
                    BluetoothDiscoverableEnabler.DEFAULT_DISCOVERABLE_TIMEOUT);
            Log.e(TAG, "Timeout = " + mTimeout);
            if (mTimeout > MAX_DISCOVERABLE_TIMEOUT) {
                mTimeout = MAX_DISCOVERABLE_TIMEOUT;
            } else if (mTimeout <= 0) {
                mTimeout = BluetoothDiscoverableEnabler.DEFAULT_DISCOVERABLE_TIMEOUT;
            }
        } else {
            Log.e(TAG, "Error: this activity may be started only with intent "
                    + BluetoothAdapter.ACTION_REQUEST_ENABLE + " or "
                    + BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
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
    protected void onDestroy() {
        super.onDestroy();
        if (mNeededToEnableBluetooth) unregisterReceiver(mReceiver);
    }
    private void persistDiscoverableEndTimestamp(long endTimestamp) {
        SharedPreferences.Editor editor = mLocalManager.getSharedPreferences().edit();
        editor.putLong(
                BluetoothDiscoverableEnabler.SHARED_PREFERENCES_KEY_DISCOVERABLE_END_TIMESTAMP,
                endTimestamp);
        editor.commit();
    }
    @Override
    public void onBackPressed() {
        setResult(Activity.RESULT_CANCELED);
        super.onBackPressed();
    }
}
