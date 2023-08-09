public class LegacyContactImporterPerformanceTest extends AndroidTestCase {
    private static final String TAG = "LegacyContactImporterPerformanceTest";
    private static final boolean TRACE = false;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        SynchronousContactsProvider2.resetOpenHelper();
    }
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        SynchronousContactsProvider2.resetOpenHelper();
    }
    public void testPerformance() {
        final Context targetContext = getContext();
        MockContentResolver resolver = new MockContentResolver();
        MockContext context = new MockContext() {
            @Override
            public Resources getResources() {
                return targetContext.getResources();
            }
            @Override
            public String getPackageName() {
                return "no.package";
            }
        };
        RenamingDelegatingContext targetContextWrapper = new RenamingDelegatingContext(context,
                targetContext, "perf_imp.");
        targetContextWrapper.makeExistingFilesAndDbsAccessible();
        IsolatedContext providerContext = new IsolatedContext(resolver, targetContextWrapper);
        SynchronousContactsProvider2 provider = new SynchronousContactsProvider2();
        provider.setDataWipeEnabled(false);
        provider.attachInfo(providerContext, null);
        resolver.addProvider(ContactsContract.AUTHORITY, provider);
        ContentProvider callLogProvider = new TestCallLogProvider();
        callLogProvider.attachInfo(providerContext, null);
        resolver.addProvider(CallLog.AUTHORITY, callLogProvider);
        LegacyContactImporter importer = new LegacyContactImporter(providerContext, provider);
        provider.wipeData();
        long start = System.currentTimeMillis();
        if (TRACE) {
            Debug.startMethodTracing("import");
        }
        provider.importLegacyContacts(importer);
        if (TRACE) {
            Debug.stopMethodTracing();
        }
        long end = System.currentTimeMillis();
        long contactCount = provider.getRawContactCount();
        Log.i(TAG, String.format("Imported contacts in %d ms.\n"
                + "Contacts: %d\n"
                + "Per contact: %.3f",
                end - start,
                contactCount,
                ((double)(end - start) / contactCount)));
    }
    public static class TestCallLogProvider extends CallLogProvider {
        private static ContactsDatabaseHelper mDbHelper;
        @Override
        protected ContactsDatabaseHelper getDatabaseHelper(final Context context) {
            if (mDbHelper == null) {
                mDbHelper = new ContactsDatabaseHelper(context);
            }
            return mDbHelper;
        }
    }
}
