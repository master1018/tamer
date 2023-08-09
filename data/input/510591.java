public class LauncherApplication extends Application {
    public LauncherModel mModel;
    public IconCache mIconCache;
    @Override
    public void onCreate() {
        VMRuntime.getRuntime().setMinimumHeapSize(4 * 1024 * 1024);
        super.onCreate();
        mIconCache = new IconCache(this);
        mModel = new LauncherModel(this, mIconCache);
        IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
        filter.addDataScheme("package");
        registerReceiver(mModel, filter);
        filter = new IntentFilter();
        filter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE);
        filter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE);
        registerReceiver(mModel, filter);
        ContentResolver resolver = getContentResolver();
        resolver.registerContentObserver(LauncherSettings.Favorites.CONTENT_URI, true,
                mFavoritesObserver);
    }
    @Override
    public void onTerminate() {
        super.onTerminate();
        unregisterReceiver(mModel);
        ContentResolver resolver = getContentResolver();
        resolver.unregisterContentObserver(mFavoritesObserver);
    }
    private final ContentObserver mFavoritesObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            mModel.startLoader(LauncherApplication.this, false);
        }
    };
    LauncherModel setLauncher(Launcher launcher) {
        mModel.initialize(launcher);
        return mModel;
    }
    IconCache getIconCache() {
        return mIconCache;
    }
    LauncherModel getModel() {
        return mModel;
    }
}
