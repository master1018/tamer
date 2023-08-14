public class BridgeContentResolver extends ContentResolver {
    public BridgeContentResolver(Context context) {
        super(context);
    }
    @Override
    public IContentProvider acquireProvider(Context c, String name) {
        return null;
    }
    @Override
    public boolean releaseProvider(IContentProvider icp) {
        return false;
    }
    @Override
    public void registerContentObserver(Uri uri, boolean notifyForDescendents,
            ContentObserver observer) {
    }
    @Override
    public void unregisterContentObserver(ContentObserver observer) {
    }
    @Override
    public void notifyChange(Uri uri, ContentObserver observer, boolean syncToNetwork) {
    }
    @Override
    public void startSync(Uri uri, Bundle extras) {
    }
    @Override
    public void cancelSync(Uri uri) {
    }
}
