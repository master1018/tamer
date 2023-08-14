public class TimeChangeReceiver extends BroadcastReceiver {
    static final String TAG = "TimeChangeReceiver";
    static final boolean LOGD = false;
    CalendarAppWidgetProvider mAppWidgetProvider = CalendarAppWidgetProvider.getInstance();
    @Override
    public void onReceive(Context context, Intent intent) {
        if (LOGD) Log.d(TAG, "Received time changed action=" + intent.getAction());
        boolean considerIgnore = (Intent.ACTION_TIME_CHANGED.equals(intent.getAction()));
        mAppWidgetProvider.timeUpdated(context, considerIgnore);
    }
}
