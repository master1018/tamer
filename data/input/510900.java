public class BuildWidget extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
            int[] appWidgetIds) {
        context.startService(new Intent(context, UpdateService.class));
    }
    public static class UpdateService extends Service {
        @Override
        public void onStart(Intent intent, int startId) {
            RemoteViews updateViews = buildUpdate(this);
            ComponentName thisWidget = new ComponentName(this, BuildWidget.class);
            AppWidgetManager manager = AppWidgetManager.getInstance(this);
            manager.updateAppWidget(thisWidget, updateViews);
        }
        public RemoteViews buildUpdate(Context context) {
            Resources res = context.getResources();
            RemoteViews updateViews = new RemoteViews(
                context.getPackageName(), R.layout.widget);
            PendingIntent pendingIntent = PendingIntent.getActivity(context,
                    0 , 
                    new Intent(android.provider.Settings.ACTION_DEVICE_INFO_SETTINGS),
                    0 );
            updateViews.setOnClickPendingIntent(R.id.widget, pendingIntent);
            updateViews.setTextViewText(R.id.build_info, 
                android.os.Build.VERSION.CODENAME + " " +
                android.os.Build.ID);
            updateViews.setTextViewText(R.id.build_changelist,
                android.os.Build.FINGERPRINT
                );
            return updateViews;
        }
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }
    }
}
