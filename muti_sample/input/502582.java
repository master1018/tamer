public class AlertReceiver extends BroadcastReceiver {
    private static final String TAG = "AlertReceiver";
    private static final String DELETE_ACTION = "delete";
    static final Object mStartingServiceSync = new Object();
    static PowerManager.WakeLock mStartingService;
    @Override
    public void onReceive(Context context, Intent intent) {
        if (AlertService.DEBUG) {
            Log.d(TAG, "onReceive: a=" + intent.getAction() + " " + intent.toString());
        }
        if (DELETE_ACTION.equals(intent.getAction())) {
            Intent serviceIntent = new Intent(context, DismissAllAlarmsService.class);
            context.startService(serviceIntent);
        } else {
            Intent i = new Intent();
            i.setClass(context, AlertService.class);
            i.putExtras(intent);
            i.putExtra("action", intent.getAction());
            Uri uri = intent.getData();
            if (uri != null) {
                i.putExtra("uri", uri.toString());
            }
            beginStartingService(context, i);
        }
    }
    public static void beginStartingService(Context context, Intent intent) {
        synchronized (mStartingServiceSync) {
            if (mStartingService == null) {
                PowerManager pm =
                    (PowerManager)context.getSystemService(Context.POWER_SERVICE);
                mStartingService = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                        "StartingAlertService");
                mStartingService.setReferenceCounted(false);
            }
            mStartingService.acquire();
            context.startService(intent);
        }
    }
    public static void finishStartingService(Service service, int startId) {
        synchronized (mStartingServiceSync) {
            if (mStartingService != null) {
                if (service.stopSelfResult(startId)) {
                    mStartingService.release();
                }
            }
        }
    }
    public static Notification makeNewAlertNotification(Context context, String title,
            String location, int numReminders) {
        Resources res = context.getResources();
        Intent clickIntent = new Intent();
        clickIntent.setClass(context, AlertActivity.class);
        clickIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Intent deleteIntent = new Intent();
        deleteIntent.setClass(context, AlertReceiver.class);
        deleteIntent.setAction(DELETE_ACTION);
        if (title == null || title.length() == 0) {
            title = res.getString(R.string.no_title_label);
        }
        String helperString;
        if (numReminders > 1) {
            String format;
            if (numReminders == 2) {
                format = res.getString(R.string.alert_missed_events_single);
            } else {
                format = res.getString(R.string.alert_missed_events_multiple);
            }
            helperString = String.format(format, numReminders - 1);
        } else {
            helperString = location;
        }
        Notification notification = new Notification(
                R.drawable.stat_notify_calendar,
                null,
                System.currentTimeMillis());
        notification.setLatestEventInfo(context,
                title,
                helperString,
                PendingIntent.getActivity(context, 0, clickIntent, 0));
        notification.deleteIntent = PendingIntent.getBroadcast(context, 0, deleteIntent, 0);
        return notification;
    }
}
