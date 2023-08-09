public abstract class ProviderTestCase<T extends ContentProvider>
       extends InstrumentationTestCase {
    Class<T> mProviderClass;
    String mProviderAuthority;
    private IsolatedContext mProviderContext;
    private MockContentResolver mResolver;
    public ProviderTestCase(Class<T> providerClass, String providerAuthority) {
        mProviderClass = providerClass;
        mProviderAuthority = providerAuthority;
    }
    private T mProvider;
    public T getProvider() {
        return mProvider;
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mResolver = new MockContentResolver();
        final String filenamePrefix = "test.";
        RenamingDelegatingContext targetContextWrapper = new RenamingDelegatingContext(
                new MockContext(), 
                getInstrumentation().getTargetContext(), 
                filenamePrefix);
        mProviderContext = new IsolatedContext(mResolver, targetContextWrapper);
        mProvider = mProviderClass.newInstance();
        mProvider.attachInfo(mProviderContext, null);
        assertNotNull(mProvider);
        mResolver.addProvider(mProviderAuthority, getProvider());
    }
    public MockContentResolver getMockContentResolver() {
        return mResolver;
    }
    public IsolatedContext getMockContext() {
        return mProviderContext;
    }
    public static <T extends ContentProvider> ContentResolver newResolverWithContentProviderFromSql(
            Context targetContext, Class<T> providerClass, String authority,
            String databaseName, int databaseVersion, String sql)
            throws IllegalAccessException, InstantiationException {
        final String filenamePrefix = "test.";
        MockContentResolver resolver = new MockContentResolver();
        RenamingDelegatingContext targetContextWrapper = new RenamingDelegatingContext(
                new MockContext(), 
                targetContext, 
                filenamePrefix);
        Context context = new IsolatedContext(
                resolver, targetContextWrapper);
        DatabaseUtils.createDbFromSqlStatements(context, databaseName, databaseVersion, sql);
        T provider = providerClass.newInstance();
        provider.attachInfo(context, null);
        resolver.addProvider(authority, provider);
        return resolver;
    }
}
