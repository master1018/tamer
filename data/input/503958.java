public class AppWidgetPickActivity extends ActivityPicker {
    private static final String TAG = "AppWidgetPickActivity";
    private static final boolean LOGD = false;
    private PackageManager mPackageManager;
    private AppWidgetManager mAppWidgetManager;
    private int mAppWidgetId;
    @Override
    public void onCreate(Bundle icicle) {
        mPackageManager = getPackageManager();
        mAppWidgetManager = AppWidgetManager.getInstance(this);
        super.onCreate(icicle);
        setResultData(RESULT_CANCELED, null);
        final Intent intent = getIntent();
        if (intent.hasExtra(AppWidgetManager.EXTRA_APPWIDGET_ID)) {
            mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        } else {
            finish();
        }
    }
    void putCustomAppWidgets(List<PickAdapter.Item> items) {
        final Bundle extras = getIntent().getExtras();
        ArrayList<AppWidgetProviderInfo> customInfo = null;
        ArrayList<Bundle> customExtras = null;
        try_custom_items: {
            customInfo = extras.getParcelableArrayList(AppWidgetManager.EXTRA_CUSTOM_INFO);
            if (customInfo == null || customInfo.size() == 0) {
                Log.i(TAG, "EXTRA_CUSTOM_INFO not present.");
                break try_custom_items;
            }
            int customInfoSize = customInfo.size();
            for (int i=0; i<customInfoSize; i++) {
                Parcelable p = customInfo.get(i);
                if (p == null || !(p instanceof AppWidgetProviderInfo)) {
                    customInfo = null;
                    Log.e(TAG, "error using EXTRA_CUSTOM_INFO index=" + i);
                    break try_custom_items;
                }
            }
            customExtras = extras.getParcelableArrayList(AppWidgetManager.EXTRA_CUSTOM_EXTRAS);
            if (customExtras == null) {
                customInfo = null;
                Log.e(TAG, "EXTRA_CUSTOM_INFO without EXTRA_CUSTOM_EXTRAS");
                break try_custom_items;
            }
            int customExtrasSize = customExtras.size();
            if (customInfoSize != customExtrasSize) {
                Log.e(TAG, "list size mismatch: EXTRA_CUSTOM_INFO: " + customInfoSize
                        + " EXTRA_CUSTOM_EXTRAS: " + customExtrasSize);
                break try_custom_items;
            }
            for (int i=0; i<customExtrasSize; i++) {
                Parcelable p = customExtras.get(i);
                if (p == null || !(p instanceof Bundle)) {
                    customInfo = null;
                    customExtras = null;
                    Log.e(TAG, "error using EXTRA_CUSTOM_EXTRAS index=" + i);
                    break try_custom_items;
                }
            }
        }
        if (LOGD) Log.d(TAG, "Using " + customInfo.size() + " custom items");
        putAppWidgetItems(customInfo, customExtras, items);
    }
    @Override
    public void onClick(DialogInterface dialog, int which) {
        Intent intent = getIntentForPosition(which);
        int result;
        if (intent.getExtras() != null) {
            setResultData(RESULT_OK, intent);
        } else {
            try {
                mAppWidgetManager.bindAppWidgetId(mAppWidgetId, intent.getComponent());
                result = RESULT_OK;
            } catch (IllegalArgumentException e) {
                result = RESULT_CANCELED;
            }
            setResultData(result, null);
        }
        finish();
    }
    void putAppWidgetItems(List<AppWidgetProviderInfo> appWidgets,
            List<Bundle> customExtras, List<PickAdapter.Item> items) {
        if (appWidgets == null) return;
        final int size = appWidgets.size();
        for (int i = 0; i < size; i++) {
            AppWidgetProviderInfo info = appWidgets.get(i);
            CharSequence label = info.label;
            Drawable icon = null;
            if (info.icon != 0) {
                icon = mPackageManager.getDrawable(info.provider.getPackageName(), info.icon, null);
                if (icon == null) {
                    Log.w(TAG, "Can't load icon drawable 0x" + Integer.toHexString(info.icon)
                            + " for provider: " + info.provider);
                }
            }
            PickAdapter.Item item = new PickAdapter.Item(this, label, icon);
            item.packageName = info.provider.getPackageName();
            item.className = info.provider.getClassName();
            if (customExtras != null) {
                item.extras = customExtras.get(i);
            }
            items.add(item);
        }
    }
    @Override
    protected List<PickAdapter.Item> getItems() {
        List<PickAdapter.Item> items = new ArrayList<PickAdapter.Item>();
        putInstalledAppWidgets(items);
        putCustomAppWidgets(items);
        Collections.sort(items, new Comparator<PickAdapter.Item>() {
                Collator mCollator = Collator.getInstance();
                public int compare(PickAdapter.Item lhs, PickAdapter.Item rhs) {
                    return mCollator.compare(lhs.label, rhs.label);
                }
            });
        return items;
    }
    void putInstalledAppWidgets(List<PickAdapter.Item> items) {
        List<AppWidgetProviderInfo> installed = mAppWidgetManager.getInstalledProviders();
        putAppWidgetItems(installed, null, items);
    }
    void setResultData(int code, Intent intent) {
        Intent result = intent != null ? intent : new Intent();
        result.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
        setResult(code, result);
    }
}
