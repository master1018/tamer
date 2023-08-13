public abstract class AbstractSource implements Source {
    private static final String TAG = "QSB.AbstractSource";
    private final Context mContext;
    private IconLoader mIconLoader;
    public AbstractSource(Context context) {
        mContext = context;
    }
    protected Context getContext() {
        return mContext;
    }
    protected IconLoader getIconLoader() {
        if (mIconLoader == null) {
            String iconPackage = getIconPackage();
            mIconLoader = new CachingIconLoader(new PackageIconLoader(mContext, iconPackage));
        }
        return mIconLoader;
    }
    protected abstract String getIconPackage();
    public boolean isVersionCodeCompatible(int version) {
        return getVersionCode() == version;
    }
    public Drawable getIcon(String drawableId) {
        return getIconLoader().getIcon(drawableId);
    }
    public Uri getIconUri(String drawableId) {
        return getIconLoader().getIconUri(drawableId);
    }
    public Intent createSearchIntent(String query, Bundle appData) {
        return createSourceSearchIntent(getIntentComponent(), query, appData);
    }
    public static Intent createSourceSearchIntent(ComponentName activity, String query,
            Bundle appData) {
        if (activity == null) {
            Log.w(TAG, "Tried to create search intent with no target activity");
            return null;
        }
        Intent intent = new Intent(Intent.ACTION_SEARCH);
        intent.setComponent(activity);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(SearchManager.USER_QUERY, query);
        intent.putExtra(SearchManager.QUERY, query);
        if (appData != null) {
            intent.putExtra(SearchManager.APP_DATA, appData);
        }
        return intent;
    }
    protected Intent createVoiceWebSearchIntent(Bundle appData) {
        return QsbApplication.get(mContext).getVoiceSearch()
                .createVoiceWebSearchIntent(appData);
    }
    @Override
    public boolean equals(Object o) {
        if (o != null && o.getClass().equals(this.getClass())) {
            AbstractSource s = (AbstractSource) o;
            return s.getName().equals(getName());
        }
        return false;
    }
    @Override
    public int hashCode() {
        return getName().hashCode();
    }
    @Override
    public String toString() {
        return "Source{name=" + getName() + "}";
    }
}
