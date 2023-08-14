public class NetInitiatedActivity extends AlertActivity implements DialogInterface.OnClickListener {
    private static final String TAG = "NetInitiatedActivity";
    private static final boolean DEBUG = true;
    private static final boolean VERBOSE = false;
    private static final int POSITIVE_BUTTON = AlertDialog.BUTTON1;
    private static final int NEGATIVE_BUTTON = AlertDialog.BUTTON2;
    public static final String BUTTON_TEXT_ACCEPT = "Accept";
    public static final String BUTTON_TEXT_DENY = "Deny";
    private int notificationId = -1;
    private BroadcastReceiver mNetInitiatedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (DEBUG) Log.d(TAG, "NetInitiatedReceiver onReceive: " + intent.getAction());
            if (intent.getAction() == GpsNetInitiatedHandler.ACTION_NI_VERIFY) {
                handleNIVerify(intent);
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Intent intent = getIntent();
        final AlertController.AlertParams p = mAlertParams;
        p.mIconId = com.android.internal.R.drawable.ic_dialog_usb;
        p.mTitle = intent.getStringExtra(GpsNetInitiatedHandler.NI_INTENT_KEY_TITLE);
        p.mMessage = intent.getStringExtra(GpsNetInitiatedHandler.NI_INTENT_KEY_MESSAGE);
        p.mPositiveButtonText = BUTTON_TEXT_ACCEPT;
        p.mPositiveButtonListener = this;
        p.mNegativeButtonText = BUTTON_TEXT_DENY;
        p.mNegativeButtonListener = this;
        notificationId = intent.getIntExtra(GpsNetInitiatedHandler.NI_INTENT_KEY_NOTIF_ID, -1);
        if (DEBUG) Log.d(TAG, "onCreate, notifId: " + notificationId);
        setupAlert();
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (DEBUG) Log.d(TAG, "onResume");
        registerReceiver(mNetInitiatedReceiver, new IntentFilter(GpsNetInitiatedHandler.ACTION_NI_VERIFY));
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (DEBUG) Log.d(TAG, "onPause");
        unregisterReceiver(mNetInitiatedReceiver);
    }
    public void onClick(DialogInterface dialog, int which) {
        if (which == POSITIVE_BUTTON) {
            sendUserResponse(GpsNetInitiatedHandler.GPS_NI_RESPONSE_ACCEPT);
        }
        if (which == NEGATIVE_BUTTON) {
            sendUserResponse(GpsNetInitiatedHandler.GPS_NI_RESPONSE_DENY);
        }
        finish();
        notificationId = -1;
    }
    private void sendUserResponse(int response) {
        if (DEBUG) Log.d(TAG, "sendUserResponse, response: " + response);
        LocationManager locationManager = (LocationManager)
            this.getSystemService(Context.LOCATION_SERVICE);
        locationManager.sendNiResponse(notificationId, response);
    }
    private void handleNIVerify(Intent intent) {
        int notifId = intent.getIntExtra(GpsNetInitiatedHandler.NI_INTENT_KEY_NOTIF_ID, -1);
        notificationId = notifId;
        if (DEBUG) Log.d(TAG, "handleNIVerify action: " + intent.getAction());
    }
    private void showNIError() {
        Toast.makeText(this, "NI error" ,
                Toast.LENGTH_LONG).show();
    }
}
