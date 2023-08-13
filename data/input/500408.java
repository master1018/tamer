public class SmsMessageReceiver extends BroadcastReceiver {
    private static final String TAG = "SmsMessageReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras == null)
            return;
        Object[] pdus = (Object[]) extras.get("pdus");
        for (int i = 0; i < pdus.length; i++) {
            SmsMessage message = SmsMessage.createFromPdu((byte[]) pdus[i]);
            String fromAddress = message.getOriginatingAddress();
            String fromDisplayName = fromAddress;
            Uri uri;
            String[] projection;
            uri = Uri.withAppendedPath(
                    ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                    Uri.encode(fromAddress));
            projection = new String[] { ContactsContract.PhoneLookup.DISPLAY_NAME };
            Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst())
                    fromDisplayName = cursor.getString(0);
                cursor.close();
            }
            Intent di = new Intent();
            di.setClass(context, SmsReceivedDialog.class);
            di.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            di.putExtra(SmsReceivedDialog.SMS_FROM_ADDRESS_EXTRA, fromAddress);
            di.putExtra(SmsReceivedDialog.SMS_FROM_DISPLAY_NAME_EXTRA, fromDisplayName);
            di.putExtra(SmsReceivedDialog.SMS_MESSAGE_EXTRA, message.getMessageBody().toString());
            context.startActivity(di);
            break;
        }
    }
}
