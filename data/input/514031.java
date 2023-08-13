public class CalendarAppWidgetProvider extends AppWidgetProvider {
    static final String TAG = "CalendarAppWidgetProvider";
    static final boolean LOGD = false;
    static final String ACTION_CALENDAR_APPWIDGET_UPDATE =
            "com.android.providers.calendar.APPWIDGET_UPDATE";
    static final long UPDATE_THRESHOLD = DateUtils.MINUTE_IN_MILLIS;
    static final long WAKE_LOCK_TIMEOUT = DateUtils.MINUTE_IN_MILLIS;
    static final String PACKAGE_THIS_APPWIDGET =
        "com.android.providers.calendar";
    static final String CLASS_THIS_APPWIDGET =
        "com.android.providers.calendar.CalendarAppWidgetProvider";
    private static CalendarAppWidgetProvider sInstance;
    static synchronized CalendarAppWidgetProvider getInstance() {
        if (sInstance == null) {
            sInstance = new CalendarAppWidgetProvider();
        }
        return sInstance;
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (ACTION_CALENDAR_APPWIDGET_UPDATE.equals(action)) {
            performUpdate(context, null ,
                    null , false );
        } else {
            super.onReceive(context, intent);
        }
    }
    @Override
    public void onEnabled(Context context) {
        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(
                new ComponentName(context, TimeChangeReceiver.class),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }
    @Override
    public void onDisabled(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingUpdate = getUpdateIntent(context);
        am.cancel(pendingUpdate);
        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(
                new ComponentName(context, TimeChangeReceiver.class),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        performUpdate(context, appWidgetIds, null , false );
    }
    private boolean hasInstances(Context context) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName thisAppWidget = getComponentName(context);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);
        return (appWidgetIds.length > 0);
    }
    static ComponentName getComponentName(Context context) {
        return new ComponentName(PACKAGE_THIS_APPWIDGET, CLASS_THIS_APPWIDGET);
    }
    void providerUpdated(Context context, long changedEventId) {
        if (hasInstances(context)) {
            long[] changedEventIds = null;
            if (changedEventId != -1) {
                changedEventIds = new long[] { changedEventId };
            }
            performUpdate(context, null , changedEventIds, false );
        }
    }
    void timeUpdated(Context context, boolean considerIgnore) {
        if (hasInstances(context)) {
            performUpdate(context, null , null , considerIgnore);
        }
    }
    private void performUpdate(Context context, int[] appWidgetIds,
            long[] changedEventIds, boolean considerIgnore) {
        synchronized (AppWidgetShared.sLock) {
            long now = System.currentTimeMillis();
            if (considerIgnore && AppWidgetShared.sLastRequest != -1) {
                long delta = Math.abs(now - AppWidgetShared.sLastRequest);
                if (delta < UPDATE_THRESHOLD) {
                    if (LOGD) Log.d(TAG, "Ignoring update request because delta=" + delta);
                    return;
                }
            }
            if (AppWidgetShared.sWakeLock == null ||
                    !AppWidgetShared.sWakeLock.isHeld()) {
                if (LOGD) Log.d(TAG, "no held wakelock found, so acquiring new one");
                PowerManager powerManager = (PowerManager)
                        context.getSystemService(Context.POWER_SERVICE);
                AppWidgetShared.sWakeLock =
                        powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
                AppWidgetShared.sWakeLock.setReferenceCounted(false);
                AppWidgetShared.sWakeLock.acquire(WAKE_LOCK_TIMEOUT);
            }
            if (LOGD) Log.d(TAG, "setting request now=" + now);
            AppWidgetShared.sLastRequest = now;
            AppWidgetShared.sUpdateRequested = true;
            AppWidgetShared.mergeAppWidgetIdsLocked(appWidgetIds);
            AppWidgetShared.mergeChangedEventIdsLocked(changedEventIds);
            final Intent updateIntent = new Intent(context, CalendarAppWidgetService.class);
            context.startService(updateIntent);
        }
    }
    static PendingIntent getUpdateIntent(Context context) {
        Intent updateIntent = new Intent(ACTION_CALENDAR_APPWIDGET_UPDATE);
        updateIntent.setComponent(new ComponentName(context, CalendarAppWidgetProvider.class));
        return PendingIntent.getBroadcast(context, 0 ,
                updateIntent, 0 );
    }
}
