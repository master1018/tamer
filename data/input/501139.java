public class ContactsUpgradeReceiver extends BroadcastReceiver {
    static final String TAG = "ContactsUpgradeReceiver";
    static final String PREF_DB_VERSION = "db_version";
    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            long startTime = System.currentTimeMillis();
            SharedPreferences prefs = context.getSharedPreferences(TAG, Context.MODE_PRIVATE);
            int prefVersion = prefs.getInt(PREF_DB_VERSION, 0);
            if (prefVersion != ContactsDatabaseHelper.DATABASE_VERSION) {
                prefs.edit().putInt(PREF_DB_VERSION, ContactsDatabaseHelper.DATABASE_VERSION).commit();
                Log.i(TAG, "Creating or opening contacts database");
                ContactsDatabaseHelper helper = ContactsDatabaseHelper.getInstance(context);
                helper.getWritableDatabase();
                helper.close();
                EventLogTags.writeContactsUpgradeReceiver(System.currentTimeMillis() - startTime);
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
