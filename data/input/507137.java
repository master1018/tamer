public class AppWidgetHost {
    static final int HANDLE_UPDATE = 1;
    static final int HANDLE_PROVIDER_CHANGED = 2;
    final static Object sServiceLock = new Object();
    static IAppWidgetService sService;
    Context mContext;
    String mPackageName;
    class Callbacks extends IAppWidgetHost.Stub {
        public void updateAppWidget(int appWidgetId, RemoteViews views) {
            Message msg = mHandler.obtainMessage(HANDLE_UPDATE);
            msg.arg1 = appWidgetId;
            msg.obj = views;
            msg.sendToTarget();
        }
        public void providerChanged(int appWidgetId, AppWidgetProviderInfo info) {
            Message msg = mHandler.obtainMessage(HANDLE_PROVIDER_CHANGED);
            msg.arg1 = appWidgetId;
            msg.obj = info;
            msg.sendToTarget();
        }
    }
    class UpdateHandler extends Handler {
        public UpdateHandler(Looper looper) {
            super(looper);
        }
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HANDLE_UPDATE: {
                    updateAppWidgetView(msg.arg1, (RemoteViews)msg.obj);
                    break;
                }
                case HANDLE_PROVIDER_CHANGED: {
                    onProviderChanged(msg.arg1, (AppWidgetProviderInfo)msg.obj);
                    break;
                }
            }
        }
    }
    Handler mHandler;
    int mHostId;
    Callbacks mCallbacks = new Callbacks();
    final HashMap<Integer,AppWidgetHostView> mViews = new HashMap<Integer, AppWidgetHostView>();
    public AppWidgetHost(Context context, int hostId) {
        mContext = context;
        mHostId = hostId;
        mHandler = new UpdateHandler(context.getMainLooper());
        synchronized (sServiceLock) {
            if (sService == null) {
                IBinder b = ServiceManager.getService(Context.APPWIDGET_SERVICE);
                sService = IAppWidgetService.Stub.asInterface(b);
            }
        }
    }
    public void startListening() {
        int[] updatedIds;
        ArrayList<RemoteViews> updatedViews = new ArrayList<RemoteViews>();
        try {
            if (mPackageName == null) {
                mPackageName = mContext.getPackageName();
            }
            updatedIds = sService.startListening(mCallbacks, mPackageName, mHostId, updatedViews);
        }
        catch (RemoteException e) {
            throw new RuntimeException("system server dead?", e);
        }
        final int N = updatedIds.length;
        for (int i=0; i<N; i++) {
            updateAppWidgetView(updatedIds[i], updatedViews.get(i));
        }
    }
    public void stopListening() {
        try {
            sService.stopListening(mHostId);
        }
        catch (RemoteException e) {
            throw new RuntimeException("system server dead?", e);
        }
    }
    public int allocateAppWidgetId() {
        try {
            if (mPackageName == null) {
                mPackageName = mContext.getPackageName();
            }
            return sService.allocateAppWidgetId(mPackageName, mHostId);
        }
        catch (RemoteException e) {
            throw new RuntimeException("system server dead?", e);
        }
    }
    public void deleteAppWidgetId(int appWidgetId) {
        synchronized (mViews) {
            mViews.remove(appWidgetId);
            try {
                sService.deleteAppWidgetId(appWidgetId);
            }
            catch (RemoteException e) {
                throw new RuntimeException("system server dead?", e);
            }
        }
    }
    public void deleteHost() {
        try {
            sService.deleteHost(mHostId);
        }
        catch (RemoteException e) {
            throw new RuntimeException("system server dead?", e);
        }
    }
    public static void deleteAllHosts() {
        try {
            sService.deleteAllHosts();
        }
        catch (RemoteException e) {
            throw new RuntimeException("system server dead?", e);
        }
    }
    public final AppWidgetHostView createView(Context context, int appWidgetId,
            AppWidgetProviderInfo appWidget) {
        AppWidgetHostView view = onCreateView(context, appWidgetId, appWidget);
        view.setAppWidget(appWidgetId, appWidget);
        synchronized (mViews) {
            mViews.put(appWidgetId, view);
        }
        RemoteViews views;
        try {
            views = sService.getAppWidgetViews(appWidgetId);
        } catch (RemoteException e) {
            throw new RuntimeException("system server dead?", e);
        }
        view.updateAppWidget(views);
        return view;
    }
    protected AppWidgetHostView onCreateView(Context context, int appWidgetId,
            AppWidgetProviderInfo appWidget) {
        return new AppWidgetHostView(context);
    }
    protected void onProviderChanged(int appWidgetId, AppWidgetProviderInfo appWidget) {
        AppWidgetHostView v;
        synchronized (mViews) {
            v = mViews.get(appWidgetId);
        }
        if (v != null) {
            v.updateAppWidget(null, AppWidgetHostView.UPDATE_FLAGS_RESET);
        }
    }
    void updateAppWidgetView(int appWidgetId, RemoteViews views) {
        AppWidgetHostView v;
        synchronized (mViews) {
            v = mViews.get(appWidgetId);
        }
        if (v != null) {
            v.updateAppWidget(views, 0);
        }
    }
}
