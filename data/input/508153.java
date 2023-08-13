public class AppWidgetManager {
    static final String TAG = "AppWidgetManager";
    public static final String ACTION_APPWIDGET_PICK = "android.appwidget.action.APPWIDGET_PICK";
    public static final String ACTION_APPWIDGET_CONFIGURE = "android.appwidget.action.APPWIDGET_CONFIGURE";
    public static final String EXTRA_APPWIDGET_ID = "appWidgetId";
    public static final String EXTRA_APPWIDGET_IDS = "appWidgetIds";
    public static final String EXTRA_CUSTOM_INFO = "customInfo";
    public static final String EXTRA_CUSTOM_EXTRAS = "customExtras";
    public static final int INVALID_APPWIDGET_ID = 0;
    public static final String ACTION_APPWIDGET_UPDATE = "android.appwidget.action.APPWIDGET_UPDATE";
    public static final String ACTION_APPWIDGET_DELETED = "android.appwidget.action.APPWIDGET_DELETED";
    public static final String ACTION_APPWIDGET_DISABLED = "android.appwidget.action.APPWIDGET_DISABLED";
    public static final String ACTION_APPWIDGET_ENABLED = "android.appwidget.action.APPWIDGET_ENABLED";
    public static final String META_DATA_APPWIDGET_PROVIDER = "android.appwidget.provider";
    public static final String META_DATA_APPWIDGET_OLD_NAME = "android.appwidget.oldName";
    static WeakHashMap<Context, WeakReference<AppWidgetManager>> sManagerCache = new WeakHashMap();
    static IAppWidgetService sService;
    Context mContext;
    private DisplayMetrics mDisplayMetrics;
    public static AppWidgetManager getInstance(Context context) {
        synchronized (sManagerCache) {
            if (sService == null) {
                IBinder b = ServiceManager.getService(Context.APPWIDGET_SERVICE);
                sService = IAppWidgetService.Stub.asInterface(b);
            }
            WeakReference<AppWidgetManager> ref = sManagerCache.get(context);
            AppWidgetManager result = null;
            if (ref != null) {
                result = ref.get();
            }
            if (result == null) {
                result = new AppWidgetManager(context);
                sManagerCache.put(context, new WeakReference(result));
            }
            return result;
        }
    }
    private AppWidgetManager(Context context) {
        mContext = context;
        mDisplayMetrics = context.getResources().getDisplayMetrics();
    }
    public void updateAppWidget(int[] appWidgetIds, RemoteViews views) {
        try {
            sService.updateAppWidgetIds(appWidgetIds, views);
        }
        catch (RemoteException e) {
            throw new RuntimeException("system server dead?", e);
        }
    }
    public void updateAppWidget(int appWidgetId, RemoteViews views) {
        updateAppWidget(new int[] { appWidgetId }, views);
    }
    public void updateAppWidget(ComponentName provider, RemoteViews views) {
        try {
            sService.updateAppWidgetProvider(provider, views);
        }
        catch (RemoteException e) {
            throw new RuntimeException("system server dead?", e);
        }
    }
    public List<AppWidgetProviderInfo> getInstalledProviders() {
        try {
            return sService.getInstalledProviders();
        }
        catch (RemoteException e) {
            throw new RuntimeException("system server dead?", e);
        }
    }
    public AppWidgetProviderInfo getAppWidgetInfo(int appWidgetId) {
        try {
            AppWidgetProviderInfo info = sService.getAppWidgetInfo(appWidgetId);
            if (info != null) {
                info.minWidth = 
                        TypedValue.complexToDimensionPixelSize(info.minWidth, mDisplayMetrics);
                info.minHeight =
                        TypedValue.complexToDimensionPixelSize(info.minHeight, mDisplayMetrics);
            }
            return info;
        }
        catch (RemoteException e) {
            throw new RuntimeException("system server dead?", e);
        }
    }
    public void bindAppWidgetId(int appWidgetId, ComponentName provider) {
        try {
            sService.bindAppWidgetId(appWidgetId, provider);
        }
        catch (RemoteException e) {
            throw new RuntimeException("system server dead?", e);
        }
    }
    public int[] getAppWidgetIds(ComponentName provider) {
        try {
            return sService.getAppWidgetIds(provider);
        }
        catch (RemoteException e) {
            throw new RuntimeException("system server dead?", e);
        }
    }
}
