public class AlarmReceiver extends BroadcastReceiver {
    private final static int STALE_WINDOW = 60 * 30;
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Alarms.ALARM_KILLED.equals(intent.getAction())) {
            updateNotification(context, (Alarm)
                    intent.getParcelableExtra(Alarms.ALARM_INTENT_EXTRA),
                    intent.getIntExtra(Alarms.ALARM_KILLED_TIMEOUT, -1));
            return;
        } else if (Alarms.CANCEL_SNOOZE.equals(intent.getAction())) {
            Alarms.saveSnoozeAlert(context, -1, -1);
            return;
        }
        Alarm alarm = null;
        final byte[] data = intent.getByteArrayExtra(Alarms.ALARM_RAW_DATA);
        if (data != null) {
            Parcel in = Parcel.obtain();
            in.unmarshall(data, 0, data.length);
            in.setDataPosition(0);
            alarm = Alarm.CREATOR.createFromParcel(in);
        }
        if (alarm == null) {
            Log.v("AlarmReceiver failed to parse the alarm from the intent");
            return;
        }
        long now = System.currentTimeMillis();
        SimpleDateFormat format =
                new SimpleDateFormat("HH:mm:ss.SSS aaa");
        Log.v("AlarmReceiver.onReceive() id " + alarm.id + " setFor "
                + format.format(new Date(alarm.time)));
        if (now > alarm.time + STALE_WINDOW * 1000) {
            if (Log.LOGV) {
                Log.v("AlarmReceiver ignoring stale alarm");
            }
            return;
        }
        AlarmAlertWakeLock.acquireCpuWakeLock(context);
        Intent closeDialogs = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        context.sendBroadcast(closeDialogs);
        Class c = AlarmAlert.class;
        KeyguardManager km = (KeyguardManager) context.getSystemService(
                Context.KEYGUARD_SERVICE);
        if (km.inKeyguardRestrictedInputMode()) {
            c = AlarmAlertFullScreen.class;
        }
        Intent alarmAlert = new Intent(context, c);
        alarmAlert.putExtra(Alarms.ALARM_INTENT_EXTRA, alarm);
        alarmAlert.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_NO_USER_ACTION);
        context.startActivity(alarmAlert);
        Alarms.disableSnoozeAlert(context, alarm.id);
        if (!alarm.daysOfWeek.isRepeatSet()) {
            Alarms.enableAlarm(context, alarm.id, false);
        } else {
            Alarms.setNextAlert(context);
        }
        Intent playAlarm = new Intent(Alarms.ALARM_ALERT_ACTION);
        playAlarm.putExtra(Alarms.ALARM_INTENT_EXTRA, alarm);
        context.startService(playAlarm);
        Intent notify = new Intent(context, AlarmAlert.class);
        notify.putExtra(Alarms.ALARM_INTENT_EXTRA, alarm);
        PendingIntent pendingNotify = PendingIntent.getActivity(context,
                alarm.id, notify, 0);
        String label = alarm.getLabelOrDefault(context);
        Notification n = new Notification(R.drawable.stat_notify_alarm,
                label, alarm.time);
        n.setLatestEventInfo(context, label,
                context.getString(R.string.alarm_notify_text),
                pendingNotify);
        n.flags |= Notification.FLAG_SHOW_LIGHTS
                | Notification.FLAG_ONGOING_EVENT;
        n.defaults |= Notification.DEFAULT_LIGHTS;
        NotificationManager nm = getNotificationManager(context);
        nm.notify(alarm.id, n);
    }
    private NotificationManager getNotificationManager(Context context) {
        return (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
    }
    private void updateNotification(Context context, Alarm alarm, int timeout) {
        NotificationManager nm = getNotificationManager(context);
        if (alarm == null) {
            if (Log.LOGV) {
                Log.v("Cannot update notification for killer callback");
            }
            return;
        }
        Intent viewAlarm = new Intent(context, SetAlarm.class);
        viewAlarm.putExtra(Alarms.ALARM_ID, alarm.id);
        PendingIntent intent =
                PendingIntent.getActivity(context, alarm.id, viewAlarm, 0);
        String label = alarm.getLabelOrDefault(context);
        Notification n = new Notification(R.drawable.stat_notify_alarm,
                label, alarm.time);
        n.setLatestEventInfo(context, label,
                context.getString(R.string.alarm_alert_alert_silenced, timeout),
                intent);
        n.flags |= Notification.FLAG_AUTO_CANCEL;
        nm.cancel(alarm.id);
        nm.notify(alarm.id, n);
    }
}
