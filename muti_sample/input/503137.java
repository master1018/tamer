public class MailboxAlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        long mid = intent.getLongExtra("mailbox", -1);
        SyncManager.log("Alarm received for: " + SyncManager.alarmOwner(mid));
        SyncManager.alert(context, mid);
    }
}
