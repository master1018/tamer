public class CalendarUpgradeReceiver extends BroadcastReceiver {
    static final String TAG = "CalendarUpgradeReceiver";
    static final String PREF_DB_VERSION = "db_version";
    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            long startTime = System.currentTimeMillis();
            SharedPreferences prefs = context.getSharedPreferences(TAG, Context.MODE_PRIVATE);
            int prefVersion = prefs.getInt(PREF_DB_VERSION, 0);
            if (prefVersion != CalendarDatabaseHelper.DATABASE_VERSION) {
                prefs.edit().putInt(PREF_DB_VERSION, CalendarDatabaseHelper.DATABASE_VERSION).commit();
                Log.i(TAG, "Creating or opening calendar database");
                CalendarDatabaseHelper helper = CalendarDatabaseHelper.getInstance(context);
                helper.getWritableDatabase();
                helper.close();
                EventLogTags.writeCalendarUpgradeReceiver(System.currentTimeMillis() - startTime);
            }
        } catch (Throwable t) {
            Log.wtf(TAG, "Error during upgrade attempt. Disabling receiver.", t);
            context.getPackageManager().setComponentEnabledSetting(
                    new ComponentName(context, getClass()),
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP);
        }
    }
}
