public class ContactAggregatorPerformanceTest extends AndroidTestCase {
    private static final String TAG = "ContactAggregatorPerformanceTest";
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
        RenamingDelegatingContext targetContextWrapper =
                new RenamingDelegatingContext(context, targetContext, "perf.");
        targetContextWrapper.makeExistingFilesAndDbsAccessible();
        IsolatedContext providerContext = new IsolatedContext(resolver, targetContextWrapper);
        SynchronousContactsProvider2 provider = new SynchronousContactsProvider2();
        provider.setDataWipeEnabled(false);
        provider.attachInfo(providerContext, null);
        resolver.addProvider(ContactsContract.AUTHORITY, provider);
        long rawContactCount = provider.getRawContactCount();
        if (rawContactCount == 0) {
            Log.w(TAG, "The test has not been set up. Use this command to copy a contact db"
                    + " to the device:\nadb push <large contacts2.db> "
                    + "data/data/com.android.providers.contacts/databases/perf.contacts2.db");
            return;
        }
        provider.prepareForFullAggregation(500);
        rawContactCount = provider.getRawContactCount();
        long start = System.currentTimeMillis();
        if (TRACE) {
            Debug.startMethodTracing("aggregation");
        }
        if (TRACE) {
            Debug.stopMethodTracing();
        }
        long end = System.currentTimeMillis();
        long contactCount = provider.getContactCount();
        Log.i(TAG, String.format("Aggregated contacts in %d ms.\n" +
                "Raw contacts: %d\n" +
                "Aggregated contacts: %d\n" +
                "Per raw contact: %.3f",
                end-start,
                rawContactCount,
                contactCount,
                ((double)(end-start)/rawContactCount)));
        provider.getDatabaseHelper().close();
    }
}
