public class MessageStatusReceiver extends BroadcastReceiver {
    public static final String MESSAGE_STATUS_RECEIVED_ACTION =
            "com.android.mms.transaction.MessageStatusReceiver.MESSAGE_STATUS_RECEIVED";
    private static final String[] ID_PROJECTION = new String[] { Sms._ID };
    private static final String LOG_TAG = "MessageStatusReceiver";
    private static final Uri STATUS_URI =
            Uri.parse("content:
    private Context mContext;
    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        if (MESSAGE_STATUS_RECEIVED_ACTION.equals(intent.getAction())) {
            Uri messageUri = intent.getData();
            byte[] pdu = (byte[]) intent.getExtra("pdu");
            boolean isStatusMessage = updateMessageStatus(context, messageUri, pdu);
            MessagingNotification.nonBlockingUpdateNewMessageIndicator(context,
                    true, isStatusMessage);
       }
    }
    private boolean updateMessageStatus(Context context, Uri messageUri, byte[] pdu) {
        boolean isStatusReport = false;
        Cursor cursor = SqliteWrapper.query(context, context.getContentResolver(),
                            messageUri, ID_PROJECTION, null, null, null);
        try {
            if (cursor.moveToFirst()) {
                int messageId = cursor.getInt(0);
                Uri updateUri = ContentUris.withAppendedId(STATUS_URI, messageId);
                SmsMessage message = SmsMessage.createFromPdu(pdu);
                int status = message.getStatus();
                isStatusReport = message.isStatusReportMessage();
                ContentValues contentValues = new ContentValues(1);
                if (Log.isLoggable(LogTag.TAG, Log.DEBUG)) {
                    log("updateMessageStatus: msgUrl=" + messageUri + ", status=" + status +
                            ", isStatusReport=" + isStatusReport);
                }
                contentValues.put(Sms.STATUS, status);
                SqliteWrapper.update(context, context.getContentResolver(),
                                    updateUri, contentValues, null, null);
            } else {
                error("Can't find message for status update: " + messageUri);
            }
        } finally {
            cursor.close();
        }
        return isStatusReport;
    }
    private void error(String message) {
        Log.e(LOG_TAG, "[MessageStatusReceiver] " + message);
    }
    private void log(String message) {
        Log.d(LOG_TAG, "[MessageStatusReceiver] " + message);
    }
}
