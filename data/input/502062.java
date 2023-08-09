public abstract class ProviderTestCase2<T extends ContentProvider> extends AndroidTestCase {
    Class<T> mProviderClass;
    String mProviderAuthority;
    private IsolatedContext mProviderContext;
    private MockContentResolver mResolver;
       private class MockContext2 extends MockContext {
        @Override
        public Resources getResources() {
            return getContext().getResources();
        }
        @Override
        public File getDir(String name, int mode) {
            return getContext().getDir("mockcontext2_" + name, mode);
        }
    }
    public ProviderTestCase2(Class<T> providerClass, String providerAuthority) {
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
                new MockContext2(), 
                getContext(), 
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
            Context targetContext, String filenamePrefix, Class<T> providerClass, String authority,
            String databaseName, int databaseVersion, String sql)
            throws IllegalAccessException, InstantiationException {
        MockContentResolver resolver = new MockContentResolver();
        RenamingDelegatingContext targetContextWrapper = new RenamingDelegatingContext(
                new MockContext(), 
                targetContext, 
                filenamePrefix);
        Context context = new IsolatedContext(resolver, targetContextWrapper);
        DatabaseUtils.createDbFromSqlStatements(context, databaseName, databaseVersion, sql);
        T provider = providerClass.newInstance();
        provider.attachInfo(context, null);
        resolver.addProvider(authority, provider);
        return resolver;
    }
}
