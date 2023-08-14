public class OutgoingCallBroadcaster extends Activity {
    private static final String PERMISSION = android.Manifest.permission.PROCESS_OUTGOING_CALLS;
    private static final String TAG = "OutgoingCallBroadcaster";
    private static final boolean DBG =
            (PhoneApp.DBG_LEVEL >= 1) && (SystemProperties.getInt("ro.debuggable", 0) == 1);
    public static final String EXTRA_ALREADY_CALLED = "android.phone.extra.ALREADY_CALLED";
    public static final String EXTRA_ORIGINAL_URI = "android.phone.extra.ORIGINAL_URI";
    public static final String EXTRA_SEND_EMPTY_FLASH = "com.android.phone.extra.SEND_EMPTY_FLASH";
    public class OutgoingCallReceiver extends BroadcastReceiver {
        private static final String TAG = "OutgoingCallReceiver";
        public void onReceive(Context context, Intent intent) {
            doReceive(context, intent);
            finish();
        }
        public void doReceive(Context context, Intent intent) {
            if (DBG) Log.v(TAG, "doReceive: " + intent);
            boolean alreadyCalled;
            String number;
            String originalUri;
            alreadyCalled = intent.getBooleanExtra(
                    OutgoingCallBroadcaster.EXTRA_ALREADY_CALLED, false);
            if (alreadyCalled) {
                if (DBG) Log.v(TAG, "CALL already placed -- returning.");
                return;
            }
            number = getResultData();
            final PhoneApp app = PhoneApp.getInstance();
            int phoneType = app.phone.getPhoneType();
            if (phoneType == Phone.PHONE_TYPE_CDMA) {
                boolean activateState = (app.cdmaOtaScreenState.otaScreenState
                        == OtaUtils.CdmaOtaScreenState.OtaScreenState.OTA_STATUS_ACTIVATION);
                boolean dialogState = (app.cdmaOtaScreenState.otaScreenState
                        == OtaUtils.CdmaOtaScreenState.OtaScreenState
                        .OTA_STATUS_SUCCESS_FAILURE_DLG);
                boolean isOtaCallActive = false;
                if ((app.cdmaOtaScreenState.otaScreenState
                        == OtaUtils.CdmaOtaScreenState.OtaScreenState.OTA_STATUS_PROGRESS)
                        || (app.cdmaOtaScreenState.otaScreenState
                        == OtaUtils.CdmaOtaScreenState.OtaScreenState.OTA_STATUS_LISTENING)) {
                    isOtaCallActive = true;
                }
                if (activateState || dialogState) {
                    if (dialogState) app.dismissOtaDialogs();
                    app.clearOtaState();
                    app.clearInCallScreenMode();
                } else if (isOtaCallActive) {
                    if (DBG) Log.v(TAG, "OTA call is active, a 2nd CALL cancelled -- returning.");
                    return;
                }
            }
            if (number == null) {
                if (DBG) Log.v(TAG, "CALL cancelled (null number), returning...");
                return;
            } else if ((phoneType == Phone.PHONE_TYPE_CDMA)
                    && ((app.phone.getState() != Phone.State.IDLE)
                    && (app.phone.isOtaSpNumber(number)))) {
                if (DBG) Log.v(TAG, "Call is active, a 2nd OTA call cancelled -- returning.");
                return;
            } else if (PhoneNumberUtils.isEmergencyNumber(number)) {
                Log.w(TAG, "Cannot modify outgoing call to emergency number " + number + ".");
                return;
            }
            originalUri = intent.getStringExtra(
                    OutgoingCallBroadcaster.EXTRA_ORIGINAL_URI);
            if (originalUri == null) {
                Log.e(TAG, "Intent is missing EXTRA_ORIGINAL_URI -- returning.");
                return;
            }
            Uri uri = Uri.parse(originalUri);
            if (DBG) Log.v(TAG, "CALL to " + number + " proceeding.");
            Intent newIntent = new Intent(Intent.ACTION_CALL, uri);
            newIntent.putExtra(Intent.EXTRA_PHONE_NUMBER, number);
            PhoneUtils.checkAndCopyPhoneProviderExtras(intent, newIntent);
            newIntent.setClass(context, InCallScreen.class);
            newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (DBG) Log.v(TAG, "doReceive(): calling startActivity: " + newIntent);
            context.startActivity(newIntent);
        }
    }
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Intent intent = getIntent();
        final Configuration configuration = getResources().getConfiguration();
        if (DBG) Log.v(TAG, "onCreate: this = " + this + ", icicle = " + icicle);
        if (DBG) Log.v(TAG, " - getIntent() = " + intent);
        if (DBG) Log.v(TAG, " - configuration = " + configuration);
        if (icicle != null) {
            Log.i(TAG, "onCreate: non-null icicle!  "
                  + "Bailing out, not sending NEW_OUTGOING_CALL broadcast...");
            return;
        }
        String action = intent.getAction();
        String number = PhoneNumberUtils.getNumberFromIntent(intent, this);
        if (number != null) {
            number = PhoneNumberUtils.convertKeypadLettersToDigits(number);
            number = PhoneNumberUtils.stripSeparators(number);
        }
        final boolean emergencyNumber =
                (number != null) && PhoneNumberUtils.isEmergencyNumber(number);
        boolean callNow;
        if (getClass().getName().equals(intent.getComponent().getClassName())) {
            if (!Intent.ACTION_CALL.equals(intent.getAction())) {
                Log.w(TAG, "Attempt to deliver non-CALL action; forcing to CALL");
                intent.setAction(Intent.ACTION_CALL);
            }
        }
        if (Intent.ACTION_CALL_PRIVILEGED.equals(action)) {
            action = emergencyNumber
                    ? Intent.ACTION_CALL_EMERGENCY
                    : Intent.ACTION_CALL;
            intent.setAction(action);
        }
        if (Intent.ACTION_CALL.equals(action)) {
            if (emergencyNumber) {
                Log.w(TAG, "Cannot call emergency number " + number
                        + " with CALL Intent " + intent + ".");
                Intent invokeFrameworkDialer = new Intent();
                invokeFrameworkDialer.setClassName("com.android.contacts",
                                                   "com.android.contacts.DialtactsActivity");
                invokeFrameworkDialer.setAction(Intent.ACTION_DIAL);
                invokeFrameworkDialer.setData(intent.getData());
                if (DBG) Log.v(TAG, "onCreate(): calling startActivity for Dialer: "
                               + invokeFrameworkDialer);
                startActivity(invokeFrameworkDialer);
                finish();
                return;
            }
            callNow = false;
        } else if (Intent.ACTION_CALL_EMERGENCY.equals(action)) {
            if (!emergencyNumber) {
                Log.w(TAG, "Cannot call non-emergency number " + number
                        + " with EMERGENCY_CALL Intent " + intent + ".");
                finish();
                return;
            }
            callNow = true;
        } else {
            Log.e(TAG, "Unhandled Intent " + intent + ".");
            finish();
            return;
        }
        PhoneApp.getInstance().wakeUpScreen();
        if (number == null || TextUtils.isEmpty(number)) {
            if (intent.getBooleanExtra(EXTRA_SEND_EMPTY_FLASH, false)) {
                Log.i(TAG, "onCreate: SEND_EMPTY_FLASH...");
                PhoneUtils.sendEmptyFlash(PhoneApp.getInstance().phone);
                finish();
                return;
            } else {
                Log.i(TAG, "onCreate: null or empty number, setting callNow=true...");
                callNow = true;
            }
        }
        if (callNow) {
            intent.setClass(this, InCallScreen.class);
            if (DBG) Log.v(TAG, "onCreate(): callNow case, calling startActivity: " + intent);
            startActivity(intent);
        }
        Intent broadcastIntent = new Intent(Intent.ACTION_NEW_OUTGOING_CALL);
        if (number != null) {
            broadcastIntent.putExtra(Intent.EXTRA_PHONE_NUMBER, number);
        }
        PhoneUtils.checkAndCopyPhoneProviderExtras(intent, broadcastIntent);
        broadcastIntent.putExtra(EXTRA_ALREADY_CALLED, callNow);
        broadcastIntent.putExtra(EXTRA_ORIGINAL_URI, intent.getData().toString());
        if (DBG) Log.v(TAG, "Broadcasting intent " + broadcastIntent + ".");
        sendOrderedBroadcast(broadcastIntent, PERMISSION,
                new OutgoingCallReceiver(), null, Activity.RESULT_OK, number, null);
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (DBG) Log.v(TAG, "onConfigurationChanged: newConfig = " + newConfig);
    }
}
