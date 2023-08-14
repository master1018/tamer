public class MockContentResolver extends ContentResolver {
    Map<String, ContentProvider> mProviders;
    public MockContentResolver() {
        super(null);
        mProviders = Maps.newHashMap();
    }
    public void addProvider(String name, ContentProvider provider) {
        mProviders.put(name, provider);
    }
    @Override
    protected IContentProvider acquireProvider(Context context, String name) {
        final ContentProvider provider = mProviders.get(name);
        if (provider != null) {
            return provider.getIContentProvider();
        } else {
            return null;
        }
    }
    @Override
    public boolean releaseProvider(IContentProvider provider) {
        return true;
    }
    @Override
    public void notifyChange(Uri uri, ContentObserver observer, boolean syncToNetwork) {
    }
}
