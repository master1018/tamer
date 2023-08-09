public class MmsApp extends Application {
    public static final String LOG_TAG = "Mms";
    private SearchRecentSuggestions mRecentSuggestions;
    private TelephonyManager mTelephonyManager;
    private static MmsApp sMmsApp = null;
    @Override
    public void onCreate() {
        super.onCreate();
        sMmsApp = this;
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        MmsConfig.init(this);
        Contact.init(this);
        DraftCache.init(this);
        Conversation.init(this);
        DownloadManager.init(this);
        RateController.init(this);
        DrmUtils.cleanupStorage(this);
        LayoutManager.init(this);
        SmileyParser.init(this);
        MessagingNotification.init(this);
    }
    synchronized public static MmsApp getApplication() {
        return sMmsApp;
    }
    @Override
    public void onTerminate() {
        DrmUtils.cleanupStorage(this);
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        LayoutManager.getInstance().onConfigurationChanged(newConfig);
    }
    public TelephonyManager getTelephonyManager() {
        if (mTelephonyManager == null) {
            mTelephonyManager = (TelephonyManager)getApplicationContext()
                    .getSystemService(Context.TELEPHONY_SERVICE);
        }
        return mTelephonyManager;
    }
    public SearchRecentSuggestions getRecentSuggestions() {
        return mRecentSuggestions;
    }
}
