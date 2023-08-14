public class SearchManagerService extends ISearchManager.Stub {
    private static final String TAG = "SearchManagerService";
    private final Context mContext;
    private Searchables mSearchables;
    public SearchManagerService(Context context)  {
        mContext = context;
        mContext.registerReceiver(new BootCompletedReceiver(),
                new IntentFilter(Intent.ACTION_BOOT_COMPLETED));
    }
    private synchronized Searchables getSearchables() {
        if (mSearchables == null) {
            Log.i(TAG, "Building list of searchable activities");
            new MyPackageMonitor().register(mContext, true);
            mSearchables = new Searchables(mContext);
            mSearchables.buildSearchableList();
        }
        return mSearchables;
    }
    private final class BootCompletedReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            new Thread() {
                @Override
                public void run() {
                    Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                    mContext.unregisterReceiver(BootCompletedReceiver.this);
                    getSearchables();
                }
            }.start();
        }
    }
    class MyPackageMonitor extends PackageMonitor {
        @Override
        public void onSomePackagesChanged() {
            getSearchables().buildSearchableList();
            Intent intent = new Intent(SearchManager.INTENT_ACTION_SEARCHABLES_CHANGED);
            intent.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
            mContext.sendBroadcast(intent);
        }
    }
    public SearchableInfo getSearchableInfo(final ComponentName launchActivity) {
        if (launchActivity == null) {
            Log.e(TAG, "getSearchableInfo(), activity == null");
            return null;
        }
        return getSearchables().getSearchableInfo(launchActivity);
    }
    public List<SearchableInfo> getSearchablesInGlobalSearch() {
        return getSearchables().getSearchablesInGlobalSearchList();
    }
    public ComponentName getGlobalSearchActivity() {
        return getSearchables().getGlobalSearchActivity();
    }
    public ComponentName getWebSearchActivity() {
        return getSearchables().getWebSearchActivity();
    }
}
