public class BluetoothOppIncomingFileConfirmActivity extends AlertActivity implements
        DialogInterface.OnClickListener {
    private static final String TAG = "BluetoothIncomingFileConfirmActivity";
    private static final boolean D = Constants.DEBUG;
    private static final boolean V = Constants.VERBOSE;
    private static final int DISMISS_TIMEOUT_DIALOG = 0;
    private static final int DISMISS_TIMEOUT_DIALOG_VALUE = 2000;
    private static final String PREFERENCE_USER_TIMEOUT = "user_timeout";
    private BluetoothOppTransferInfo mTransInfo;
    private Uri mUri;
    private ContentValues mUpdateValues;
    private TextView mContentView;
    private boolean mTimeout = false;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!BluetoothShare.USER_CONFIRMATION_TIMEOUT_ACTION.equals(intent.getAction())) {
                return;
            }
            onTimeout();
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        mUri = intent.getData();
        mTransInfo = new BluetoothOppTransferInfo();
        mTransInfo = BluetoothOppUtility.queryRecord(this, mUri);
        if (mTransInfo == null) {
            if (V) Log.e(TAG, "Error: Can not get data from db");
            finish();
            return;
        }
        final AlertController.AlertParams p = mAlertParams;
        p.mIconId = android.R.drawable.ic_dialog_info;
        p.mTitle = getString(R.string.incoming_file_confirm_title);
        p.mView = createView();
        p.mPositiveButtonText = getString(R.string.incoming_file_confirm_ok);
        p.mPositiveButtonListener = this;
        p.mNegativeButtonText = getString(R.string.incoming_file_confirm_cancel);
        p.mNegativeButtonListener = this;
        setupAlert();
        if (V) Log.v(TAG, "mTimeout: " + mTimeout);
        if (mTimeout) {
            onTimeout();
        }
        if (V) Log.v(TAG, "BluetoothIncomingFileConfirmActivity: Got uri:" + mUri);
        registerReceiver(mReceiver, new IntentFilter(
                BluetoothShare.USER_CONFIRMATION_TIMEOUT_ACTION));
    }
    private View createView() {
        View view = getLayoutInflater().inflate(R.layout.confirm_dialog, null);
        mContentView = (TextView)view.findViewById(R.id.content);
        String text = getString(R.string.incoming_file_confirm_content, mTransInfo.mDeviceName,
                mTransInfo.mFileName, Formatter.formatFileSize(this, mTransInfo.mTotalBytes));
        mContentView.setText(text);
        return view;
    }
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case DialogInterface.BUTTON_POSITIVE:
                if (!mTimeout) {
                    mUpdateValues = new ContentValues();
                    mUpdateValues.put(BluetoothShare.USER_CONFIRMATION,
                            BluetoothShare.USER_CONFIRMATION_CONFIRMED);
                    this.getContentResolver().update(mUri, mUpdateValues, null, null);
                    Toast.makeText(this, getString(R.string.bt_toast_1), Toast.LENGTH_SHORT).show();
                }
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                mUpdateValues = new ContentValues();
                mUpdateValues.put(BluetoothShare.USER_CONFIRMATION,
                        BluetoothShare.USER_CONFIRMATION_DENIED);
                this.getContentResolver().update(mUri, mUpdateValues, null, null);
                break;
        }
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (D) Log.d(TAG, "onKeyDown() called; Key: back key");
            mUpdateValues = new ContentValues();
            mUpdateValues.put(BluetoothShare.VISIBILITY, BluetoothShare.VISIBILITY_HIDDEN);
            this.getContentResolver().update(mUri, mUpdateValues, null, null);
            Toast.makeText(this, getString(R.string.bt_toast_2), Toast.LENGTH_SHORT).show();
            finish();
        }
        return true;
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mTimeout = savedInstanceState.getBoolean(PREFERENCE_USER_TIMEOUT);
        if (V) Log.v(TAG, "onRestoreInstanceState() mTimeout: " + mTimeout);
        if (mTimeout) {
            onTimeout();
        }
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (V) Log.v(TAG, "onSaveInstanceState() mTimeout: " + mTimeout);
        outState.putBoolean(PREFERENCE_USER_TIMEOUT, mTimeout);
    }
    private void onTimeout() {
        mTimeout = true;
        mContentView.setText(getString(R.string.incoming_file_confirm_timeout_content,
                mTransInfo.mDeviceName));
        mAlert.getButton(DialogInterface.BUTTON_NEGATIVE).setVisibility(View.GONE);
        mAlert.getButton(DialogInterface.BUTTON_POSITIVE).setText(
                getString(R.string.incoming_file_confirm_timeout_ok));
        mTimeoutHandler.sendMessageDelayed(mTimeoutHandler.obtainMessage(DISMISS_TIMEOUT_DIALOG),
                DISMISS_TIMEOUT_DIALOG_VALUE);
    }
    private final Handler mTimeoutHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DISMISS_TIMEOUT_DIALOG:
                    if (V) Log.v(TAG, "Received DISMISS_TIMEOUT_DIALOG msg.");
                    finish();
                    break;
                default:
                    break;
            }
        }
    };
}
