public class AlarmInitReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (Log.LOGV) Log.v("AlarmInitReceiver" + action);
        if (context.getContentResolver() == null) {
            Log.e("AlarmInitReceiver: FAILURE unable to get content resolver.  Alarms inactive.");
            return;
        }
        if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            Alarms.saveSnoozeAlert(context, -1, -1);
            Alarms.disableExpiredAlarms(context);
        }
        Alarms.setNextAlert(context);
    }
}
