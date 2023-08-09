public class DismissAllAlarmsService extends IntentService {
    private static final String[] PROJECTION = new String[] {
            CalendarAlerts.STATE,
    };
    private static final int COLUMN_INDEX_STATE = 0;
    public DismissAllAlarmsService() {
        super("DismissAllAlarmsService");
    }
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @SuppressWarnings("deprecation")
    @Override
    public void onHandleIntent(Intent intent) {
        Uri uri = CalendarAlerts.CONTENT_URI;
        String selection = CalendarAlerts.STATE + "=" + CalendarAlerts.FIRED;
        ContentResolver resolver = getContentResolver();
        ContentValues values = new ContentValues();
        values.put(PROJECTION[COLUMN_INDEX_STATE], CalendarAlerts.DISMISSED);
        resolver.update(uri, values, selection, null);
        stopSelf();
    }
}
