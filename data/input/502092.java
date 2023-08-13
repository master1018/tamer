public class AppWidgetHostActivity extends Activity
{
    static final String TAG = "AppWidgetHostActivity";
    static final int DISCOVER_APPWIDGET_REQUEST = 1;
    static final int CONFIGURE_APPWIDGET_REQUEST = 2;
    static final int HOST_ID = 1234;
    static final String PENDING_APPWIDGET_ID = "pending_appwidget";
    AppWidgetManager mAppWidgetManager;
    AppWidgetContainerView mAppWidgetContainer;
    public AppWidgetHostActivity() {
        mAppWidgetManager = AppWidgetManager.getInstance(this);
    }
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.appwidget_host);
        findViewById(R.id.add_appwidget).setOnClickListener(mOnClickListener);
        mAppWidgetContainer = (AppWidgetContainerView)findViewById(R.id.appwidget_container);
        if (false) {
            if (false) {
                mHost.deleteHost();
            } else {
                AppWidgetHost.deleteAllHosts();
            }
        }
    }
    View.OnClickListener mOnClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            discoverAppWidget(DISCOVER_APPWIDGET_REQUEST);
        }
    };
    void discoverAppWidget(int requestCode) {
        Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_PICK);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mHost.allocateAppWidgetId());
        startActivityForResult(intent, requestCode);
    }
    void configureAppWidget(int requestCode, int appWidgetId, ComponentName configure) {
        Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
        intent.setComponent(configure);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        SharedPreferences.Editor prefs = getPreferences(0).edit();
        prefs.putInt(PENDING_APPWIDGET_ID, appWidgetId);
        prefs.commit();
        startActivityForResult(intent, requestCode);
    }
    void handleAppWidgetPickResult(int resultCode, Intent intent) {
        Bundle extras = intent.getExtras();
        int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID);
        if (resultCode == RESULT_OK) {
            AppWidgetProviderInfo appWidget = mAppWidgetManager.getAppWidgetInfo(appWidgetId);
            if (appWidget.configure != null) {
                configureAppWidget(CONFIGURE_APPWIDGET_REQUEST, appWidgetId, appWidget.configure);
            } else {
                addAppWidgetView(appWidgetId, appWidget);
            }
        } else {
            mHost.deleteAppWidgetId(appWidgetId);
        }
    }
    void handleAppWidgetConfigureResult(int resultCode, Intent data) {
        int appWidgetId = getPreferences(0).getInt(PENDING_APPWIDGET_ID, -1);
        Log.d(TAG, "resultCode=" + resultCode + " appWidgetId=" + appWidgetId);
        if (appWidgetId < 0) {
            Log.w(TAG, "was no preference for PENDING_APPWIDGET_ID");
            return;
        }
        if (resultCode == RESULT_OK) {
            AppWidgetProviderInfo appWidget = mAppWidgetManager.getAppWidgetInfo(appWidgetId);
            addAppWidgetView(appWidgetId, appWidget);
        } else {
            mHost.deleteAppWidgetId(appWidgetId);
        }
    }
    void addAppWidgetView(int appWidgetId, AppWidgetProviderInfo appWidget) {
        AppWidgetHostView view = mHost.createView(this, appWidgetId, appWidget);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        mAppWidgetContainer.addView(view, layoutParams);
        registerForContextMenu(view);
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
        case DISCOVER_APPWIDGET_REQUEST:
            handleAppWidgetPickResult(resultCode, data);
            break;
        case CONFIGURE_APPWIDGET_REQUEST:
            handleAppWidgetConfigureResult(resultCode, data);
        }
    }
    protected void onStart() {
        super.onStart();
        mHost.startListening();
    }
    protected void onStop() {
        super.onStop();
        mHost.stopListening();
    }
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenu.ContextMenuInfo menuInfo) {
        menu.add(ContextMenu.NONE, R.string.delete_appwidget, ContextMenu.NONE,
                R.string.delete_appwidget);
    }
    public boolean onContextItemSelected(MenuItem item) {
        MyAppWidgetView view = (MyAppWidgetView)item.getMenuInfo();
        switch (item.getItemId()) {
        case R.string.delete_appwidget:
            Log.d(TAG, "delete! " + view.appWidgetId);
            mAppWidgetContainer.removeView(view);
            mHost.deleteAppWidgetId(view.appWidgetId);
            break;
        }
        return true;
    }
    class MyAppWidgetView extends AppWidgetHostView implements ContextMenu.ContextMenuInfo {
        int appWidgetId;
        MyAppWidgetView(int appWidgetId) {
            super(AppWidgetHostActivity.this);
            this.appWidgetId = appWidgetId;
        }
        public ContextMenu.ContextMenuInfo getContextMenuInfo() {
            return this;
        }
    }
    AppWidgetHost mHost = new AppWidgetHost(this, HOST_ID) {
        protected AppWidgetHostView onCreateView(Context context, int appWidgetId, AppWidgetProviderInfo appWidget) {
            return new MyAppWidgetView(appWidgetId);
        }
    };
}
