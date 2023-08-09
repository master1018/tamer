public class EmergencyCallHandler extends Activity {
    public static final String EMERGENCY_CALL_RETRY_KEY = "emergency_call_retry_count";
    public static final int INITIAL_ATTEMPT = -1;
    public static final int NUMBER_OF_RETRIES = 6;
    public static final int TIME_BETWEEN_RETRIES_MS = 5000;
    private static final int EVENT_SERVICE_STATE_CHANGED = 100;
    private static final int EVENT_TIMEOUT_EMERGENCY_CALL = 200;
    private static class EmergencyCallInfo {
        public Phone phone;
        public Intent intent;
        public ProgressDialog dialog;
        public Application app;
    }
    private static EmergencyCallEventHandler sHandler;
    private static class EmergencyCallEventHandler extends Handler {
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case EVENT_SERVICE_STATE_CHANGED: {
                        ServiceState state = (ServiceState) ((AsyncResult) msg.obj).result;
                        if (state.getState() != ServiceState.STATE_POWER_OFF) {
                            EmergencyCallInfo eci = 
                                (EmergencyCallInfo) ((AsyncResult) msg.obj).userObj;
                            eci.phone.unregisterForServiceStateChanged(this);
                            eci.app.startActivity(eci.intent);
                            eci.dialog.dismiss();
                        }
                    }
                    break;
                case EVENT_TIMEOUT_EMERGENCY_CALL: {
                        EmergencyCallInfo eci = (EmergencyCallInfo) msg.obj;
                        eci.app.startActivity(eci.intent);
                        eci.dialog.dismiss();
                    }
                    break;
            }
        }
    }
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Phone phone = PhoneFactory.getDefaultPhone();
        int retryCount = getIntent().getIntExtra(EMERGENCY_CALL_RETRY_KEY, INITIAL_ATTEMPT);
        EmergencyCallInfo eci = new EmergencyCallInfo();
        eci.phone = phone;
        eci.app = getApplication();
        eci.dialog = constructDialog(retryCount);
        eci.intent = getIntent().setComponent(null);
        eci.intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (sHandler == null) {
            sHandler = new EmergencyCallEventHandler();
        }
        if (retryCount == INITIAL_ATTEMPT) {
            eci.intent.putExtra(EMERGENCY_CALL_RETRY_KEY, NUMBER_OF_RETRIES);
            phone.registerForServiceStateChanged(sHandler, 
                    EVENT_SERVICE_STATE_CHANGED, eci);
            if (Settings.System.getInt(getContentResolver(), 
                    Settings.System.AIRPLANE_MODE_ON, 0) > 0) {
                Settings.System.putInt(getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0);
                Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
                intent.putExtra("state", false);
                sendBroadcast(intent);
            } else {
                phone.setRadioPower(true);
            }
        } else {
            eci.intent.putExtra(EMERGENCY_CALL_RETRY_KEY, (retryCount - 1));
            Message m = sHandler.obtainMessage(EVENT_TIMEOUT_EMERGENCY_CALL);
            m.obj = eci;
            sHandler.sendMessageDelayed(m, TIME_BETWEEN_RETRIES_MS);
        }
        finish();
    }
    private ProgressDialog constructDialog(int retryCount) {
        int msgId = (retryCount == INITIAL_ATTEMPT) ? 
                R.string.emergency_enable_radio_dialog_message :
                R.string.emergency_enable_radio_dialog_retry;
        ProgressDialog pd = new ProgressDialog(getApplication());
        pd.setTitle(getText(R.string.emergency_enable_radio_dialog_title));
        pd.setMessage(getText(msgId));
        pd.setIndeterminate(true);
        pd.setCancelable(false);
        pd.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG);
        pd.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        pd.show();
        return pd;
    }
}
