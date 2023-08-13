public class CalendarReceiver extends BroadcastReceiver {
    static final String SCHEDULE = "com.android.providers.calendar.SCHEDULE_ALARM";
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        ContentResolver cr = context.getContentResolver();
        if (action.equals(SCHEDULE)) {
            cr.update(CalendarProvider2.SCHEDULE_ALARM_URI, null , null ,
                    null );
        } else if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            cr.update(CalendarProvider2.SCHEDULE_ALARM_REMOVE_URI,
                    null , null , null );
        }
    }
}
