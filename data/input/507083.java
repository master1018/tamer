public class EmailSyncAlarmReceiver extends BroadcastReceiver {
    final String[] MAILBOX_DATA_PROJECTION = {MessageColumns.MAILBOX_KEY};
    private static String TAG = "EmailSyncAlarm";
    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.v(TAG, "onReceive");
        new Thread(new Runnable() {
            public void run() {
                handleReceive(context);
            }
        }).start();
    }
    private void handleReceive(Context context) {
        ArrayList<Long> mailboxesToNotify = new ArrayList<Long>();
        ContentResolver cr = context.getContentResolver();
        int messageCount = 0;
        String selector = SyncManager.getEasAccountSelector();
        Cursor c = cr.query(Message.DELETED_CONTENT_URI, MAILBOX_DATA_PROJECTION, selector,
               null, null);
        try {
            while (c.moveToNext()) {
                messageCount++;
                long mailboxId = c.getLong(0);
                if (!mailboxesToNotify.contains(mailboxId)) {
                    mailboxesToNotify.add(mailboxId);
                }
            }
        } finally {
            c.close();
        }
        c = cr.query(Message.UPDATED_CONTENT_URI, MAILBOX_DATA_PROJECTION, selector,
                null, null);
        try {
            while (c.moveToNext()) {
                messageCount++;
                long mailboxId = c.getLong(0);
                if (!mailboxesToNotify.contains(mailboxId)) {
                    mailboxesToNotify.add(mailboxId);
                }
            }
        } finally {
            c.close();
        }
        for (Long mailboxId: mailboxesToNotify) {
            SyncManager.serviceRequest(mailboxId, SyncManager.SYNC_UPSYNC);
        }
        Log.v(TAG, "Changed/Deleted messages: " + messageCount + ", mailboxes: " +
                mailboxesToNotify.size());
    }
}
