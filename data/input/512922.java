public class SmsRejectedReceiver extends BroadcastReceiver {
    public static final int SMS_REJECTED_NOTIFICATION_ID = 239;
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Settings.Secure.getInt(context.getContentResolver(),
                Settings.Secure.DEVICE_PROVISIONED, 0) == 1 &&
                Telephony.Sms.Intents.SMS_REJECTED_ACTION.equals(intent.getAction())) {
            int reason = intent.getIntExtra("result", -1);
            boolean outOfMemory = reason == Telephony.Sms.Intents.RESULT_SMS_OUT_OF_MEMORY;
            if (!outOfMemory) {
                return;
            }
            NotificationManager nm = (NotificationManager)
            context.getSystemService(Context.NOTIFICATION_SERVICE);
            Intent viewConvIntent = new Intent(context, ConversationList.class);
            viewConvIntent.setAction(Intent.ACTION_VIEW);
            viewConvIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_SINGLE_TOP
                    | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    context, 0, viewConvIntent, 0);
            Notification notification = new Notification();
            notification.icon = R.drawable.stat_sys_no_sim;
            int titleId;
            int bodyId;
            if (outOfMemory) {
                titleId = R.string.sms_full_title;
                bodyId = R.string.sms_full_body;
            } else {
                titleId = R.string.sms_rejected_title;
                bodyId = R.string.sms_rejected_body;
            }
            notification.tickerText = context.getString(titleId);
            notification.defaults = Notification.DEFAULT_ALL;
            notification.setLatestEventInfo(
                    context, context.getString(titleId),
                    context.getString(bodyId),
                    pendingIntent);
            nm.notify(SMS_REJECTED_NOTIFICATION_ID, notification);
        }
    }
}
