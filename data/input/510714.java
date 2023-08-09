public class BluetoothOppBtEnablingActivity extends AlertActivity {
    private static final String TAG = "BluetoothOppEnablingActivity";
    private static final boolean D = Constants.DEBUG;
    private static final boolean V = Constants.VERBOSE;
    private static final int BT_ENABLING_TIMEOUT = 0;
    private static final int BT_ENABLING_TIMEOUT_VALUE = 20000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mBluetoothReceiver, filter);
        final AlertController.AlertParams p = mAlertParams;
        p.mIconId = android.R.drawable.ic_dialog_info;
        p.mTitle = getString(R.string.enabling_progress_title);
        p.mView = createView();
        setupAlert();
        mTimeoutHandler.sendMessageDelayed(mTimeoutHandler.obtainMessage(BT_ENABLING_TIMEOUT),
                BT_ENABLING_TIMEOUT_VALUE);
    }
    private View createView() {
        View view = getLayoutInflater().inflate(R.layout.bt_enabling_progress, null);
        TextView contentView = (TextView)view.findViewById(R.id.progress_info);
        contentView.setText(getString(R.string.enabling_progress_content));
        return view;
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (D) Log.d(TAG, "onKeyDown() called; Key: back key");
            mTimeoutHandler.removeMessages(BT_ENABLING_TIMEOUT);
            cancelSendingProgress();
        }
        return true;
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBluetoothReceiver);
    }
    private final Handler mTimeoutHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BT_ENABLING_TIMEOUT:
                    if (V) Log.v(TAG, "Received BT_ENABLING_TIMEOUT msg.");
                    cancelSendingProgress();
                    break;
                default:
                    break;
            }
        }
    };
    private final BroadcastReceiver mBluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (V) Log.v(TAG, "Received intent: " + action) ;
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                switch (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)) {
                    case BluetoothAdapter.STATE_ON:
                        mTimeoutHandler.removeMessages(BT_ENABLING_TIMEOUT);
                        finish();
                        break;
                    default:
                        break;
                }
            }
        }
    };
    private void cancelSendingProgress() {
        BluetoothOppManager mOppManager = BluetoothOppManager.getInstance(this);
        if (mOppManager.mSendingFlag) {
            mOppManager.mSendingFlag = false;
        }
        finish();
    }
}
