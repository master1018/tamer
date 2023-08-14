public class EmergencyCallbackModeService extends Service {
    private static final int DEFAULT_ECM_EXIT_TIMER_VALUE = 300000;
    private static final String LOG_TAG = "EmergencyCallbackModeService";
    private NotificationManager mNotificationManager = null;
    private CountDownTimer mTimer = null;
    private long mTimeLeft = 0;
    private Phone mPhone = null;
    private boolean mInEmergencyCall = false;
    private static final int ECM_TIMER_RESET = 1;
    private Handler mHandler = new Handler () {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ECM_TIMER_RESET:
                    resetEcmTimer((AsyncResult) msg.obj);
                    break;
            }
        }
    };
    @Override
    public void onCreate() {
        if (PhoneFactory.getDefaultPhone().getPhoneType() != Phone.PHONE_TYPE_CDMA) {
            Log.e(LOG_TAG, "Error! Emergency Callback Mode not supported for " +
                    PhoneFactory.getDefaultPhone().getPhoneName() + " phones");
            stopSelf();
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(TelephonyIntents.ACTION_EMERGENCY_CALLBACK_MODE_CHANGED);
        filter.addAction(TelephonyIntents.ACTION_SHOW_NOTICE_ECM_BLOCK_OTHERS);
        registerReceiver(mEcmReceiver, filter);
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mPhone = PhoneFactory.getDefaultPhone();
        mPhone.registerForEcmTimerReset(mHandler, ECM_TIMER_RESET, null);
        startTimerNotification();
    }
    @Override
    public void onDestroy() {
        unregisterReceiver(mEcmReceiver);
        mPhone.unregisterForEcmTimerReset(mHandler);
        mNotificationManager.cancel(R.string.phone_in_ecm_notification_title);
        mTimer.cancel();
    }
    private BroadcastReceiver mEcmReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(
                    TelephonyIntents.ACTION_EMERGENCY_CALLBACK_MODE_CHANGED)) {
                if (intent.getBooleanExtra("phoneinECMState", false) == false) {
                    stopSelf();
                }
            }
            else if (intent.getAction().equals(
                    TelephonyIntents.ACTION_SHOW_NOTICE_ECM_BLOCK_OTHERS)) {
                    context.startActivity(
                            new Intent(TelephonyIntents.ACTION_SHOW_NOTICE_ECM_BLOCK_OTHERS)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            }
        }
    };
    private void startTimerNotification() {
        long ecmTimeout = SystemProperties.getLong(
                    TelephonyProperties.PROPERTY_ECM_EXIT_TIMER, DEFAULT_ECM_EXIT_TIMER_VALUE);
        showNotification(ecmTimeout);
        mTimer = new CountDownTimer(ecmTimeout, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                mTimeLeft = millisUntilFinished;
                EmergencyCallbackModeService.this.showNotification(millisUntilFinished);
            }
            @Override
            public void onFinish() {
            }
        }.start();
    }
    private void showNotification(long millisUntilFinished) {
        Notification notification = new Notification(
                R.drawable.picture_emergency25x25,
                getText(R.string.phone_entered_ecm_text), 0);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(EmergencyCallbackModeExitDialog.ACTION_SHOW_ECM_EXIT_DIALOG), 0);
        String text = null;
        if(mInEmergencyCall) {
            text = getText(R.string.phone_in_ecm_call_notification_text).toString();
        } else {
            int minutes = (int)(millisUntilFinished / 60000);
            String time = String.format("%d:%02d", minutes, (millisUntilFinished % 60000) / 1000);
            text = String.format(getResources().getQuantityText(
                     R.plurals.phone_in_ecm_notification_time, minutes).toString(), time);
        }
        notification.setLatestEventInfo(this, getText(R.string.phone_in_ecm_notification_title),
                text, contentIntent);
        notification.flags = Notification.FLAG_ONGOING_EVENT;
        mNotificationManager.notify(R.string.phone_in_ecm_notification_title, notification);
    }
    private void resetEcmTimer(AsyncResult r) {
        boolean isTimerCanceled = ((Boolean)r.result).booleanValue();
        if (isTimerCanceled) {
            mInEmergencyCall = true;
            mTimer.cancel();
            showNotification(0);
        } else {
            mInEmergencyCall = false;
            startTimerNotification();
        }
    }
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
    private final IBinder mBinder = new LocalBinder();
    public class LocalBinder extends Binder {
        EmergencyCallbackModeService getService() {
            return EmergencyCallbackModeService.this;
        }
    }
    public long getEmergencyCallbackModeTimeout() {
        return mTimeLeft;
    }
    public boolean getEmergencyCallbackModeCallState() {
        return mInEmergencyCall;
    }
}
