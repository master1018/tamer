public class NoReceiveSmsPermissionTest extends AndroidTestCase {
    private static final int WAIT_TIME = 2*60*1000;
    private static final String TELEPHONY_SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
    private static final String MESSAGE_STATUS_RECEIVED_ACTION =
        "com.android.cts.permission.sms.MESSAGE_STATUS_RECEIVED_ACTION";
    private static final String MESSAGE_SENT_ACTION =
        "com.android.cts.permission.sms.MESSAGE_SENT";
    private static final String LOG_TAG = "NoReceiveSmsPermissionTest";
    public void testReceiveTextMessage() {
        IllegalSmsReceiver receiver = new IllegalSmsReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(TELEPHONY_SMS_RECEIVED);
        filter.addAction(MESSAGE_SENT_ACTION);
        filter.addAction(MESSAGE_STATUS_RECEIVED_ACTION);
        getContext().registerReceiver(receiver, filter);
        sendSMSToSelf();
        synchronized(receiver) {
            try {
                receiver.wait(WAIT_TIME);
            } catch (InterruptedException e) {
                Log.w(LOG_TAG, "wait for sms interrupted");
            }
        }
        assertTrue("Sms not sent successfully, test environment problem?",
                receiver.isMessageSent());
        assertFalse("Sms received without proper permissions", receiver.isSmsReceived());
    }
    private void sendSMSToSelf() {
        PendingIntent sentIntent = PendingIntent.getBroadcast(getContext(), 0,
                new Intent(MESSAGE_SENT_ACTION), PendingIntent.FLAG_ONE_SHOT);
        PendingIntent deliveryIntent = PendingIntent.getBroadcast(getContext(), 0,
                new Intent(MESSAGE_STATUS_RECEIVED_ACTION), PendingIntent.FLAG_ONE_SHOT);
        TelephonyManager telephony = (TelephonyManager)
                 getContext().getSystemService(Context.TELEPHONY_SERVICE);
        String currentNumber = telephony.getLine1Number();
        Log.i(LOG_TAG, String.format("Sending SMS to self: %s", currentNumber));
        sendSms(currentNumber, "test message", sentIntent, deliveryIntent);
    }
    protected void sendSms(String currentNumber, String text, PendingIntent sentIntent,
            PendingIntent deliveryIntent) {
        SmsManager.getDefault().sendTextMessage(currentNumber, null, text, sentIntent,
                deliveryIntent);
    }
    public class IllegalSmsReceiver extends BroadcastReceiver {
        private boolean mIsSmsReceived = false;
        private boolean mIsMessageSent = false;
        public void onReceive(Context context, Intent intent) {
            if (TELEPHONY_SMS_RECEIVED.equals(intent.getAction())) {
                setSmsReceived();
            } else if (MESSAGE_STATUS_RECEIVED_ACTION.equals(intent.getAction())) {
                handleResultCode(getResultCode(), "delivery");
            } else if (MESSAGE_SENT_ACTION.equals(intent.getAction())) {
                handleResultCode(getResultCode(), "sent");
            } else {
                Log.w(LOG_TAG, String.format("unknown intent received: %s", intent.getAction()));
            }
        }
        public boolean isSmsReceived() {
            return mIsSmsReceived;
        }
        private synchronized void setSmsReceived() {
            mIsSmsReceived = true;
            notify();
        }
        public boolean isMessageSent() {
            return mIsMessageSent;
        }
        private void handleResultCode(int resultCode, String action) {
            if (resultCode == Activity.RESULT_OK) {
                Log.i(LOG_TAG, String.format("message %1$s successful", action));
                setMessageSentSuccess();
            } else {
                setMessageSentFailure();
                String reason = getErrorReason(resultCode);
                Log.e(LOG_TAG, String.format("message %1$s failed: %2$s", action, reason));
            }
        }
        private synchronized void setMessageSentSuccess() {
            mIsMessageSent = true;
        }
        private synchronized void setMessageSentFailure() {
            mIsMessageSent = false;
            notify();
        }
        private String getErrorReason(int resultCode) {
            switch (resultCode) {
                case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                    return "generic failure";
                case SmsManager.RESULT_ERROR_NO_SERVICE:
                    return "no service";
                case SmsManager.RESULT_ERROR_NULL_PDU:
                    return "null pdu";
                case SmsManager.RESULT_ERROR_RADIO_OFF:
                    return "Radio off";
            }
            return "unknown";
        }
    }
}
