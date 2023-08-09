public class SyncBaseInstrumentation extends InstrumentationTestCase {
    private Context mTargetContext;
    ContentResolver mContentResolver;
    private static final int MAX_TIME_FOR_SYNC_IN_MINS = 20;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mTargetContext = getInstrumentation().getTargetContext();
        mContentResolver = mTargetContext.getContentResolver();
    }
    protected void syncProvider(Uri uri, String accountName, String authority) throws Exception {
        Bundle extras = new Bundle();
        extras.putBoolean(ContentResolver.SYNC_EXTRAS_IGNORE_SETTINGS, true);
        Account account = new Account(accountName, "com.google");
        ContentResolver.requestSync(account, authority, extras);
        long startTimeInMillis = SystemClock.elapsedRealtime();
        long endTimeInMillis = startTimeInMillis + MAX_TIME_FOR_SYNC_IN_MINS * 60000;
        int counter = 0;
        while (counter < 2) {
            Thread.sleep(1000);
            if (SystemClock.elapsedRealtime() > endTimeInMillis) {
                break;
            }
            if (ContentResolver.isSyncActive(account, authority)) {
                counter = 0;
                continue;
            }
            counter++;
        }
    }
    protected void cancelSyncsandDisableAutoSync() {
        ContentResolver.setMasterSyncAutomatically(false);
        ContentResolver.cancelSync(null , null );
    }
}
