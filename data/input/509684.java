public class BookmarkWidgetProvider extends AppWidgetProvider {
    static final String TAG = "BookmarkWidgetProvider";
    @Override
    public void onUpdate(Context context, AppWidgetManager mngr, int[] ids) {
        context.startService(new Intent(BookmarkWidgetService.UPDATE, null,
                    context, BookmarkWidgetService.class));
    }
    @Override
    public void onEnabled(Context context) {
        context.startService(new Intent(context, BookmarkWidgetService.class));
    }
    @Override
    public void onDisabled(Context context) {
        context.stopService(new Intent(context, BookmarkWidgetService.class));
    }
}
