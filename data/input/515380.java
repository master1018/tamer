public abstract class AbstractCorpus implements Corpus {
    private final Context mContext;
    private final Config mConfig;
    public AbstractCorpus(Context context, Config config) {
        mContext = context;
        mConfig = config;
    }
    protected Context getContext() {
        return mContext;
    }
    public boolean isCorpusEnabled() {
        boolean defaultEnabled = isCorpusDefaultEnabled();
        String sourceEnabledPref = SearchSettings.getCorpusEnabledPreference(this);
        SharedPreferences prefs = SearchSettings.getSearchPreferences(mContext);
        return prefs.getBoolean(sourceEnabledPref, defaultEnabled);
    }
    public boolean isCorpusDefaultEnabled() {
        return mConfig.isCorpusEnabledByDefault(getName());
    }
    public boolean isCorpusHidden() {
        return mConfig.isCorpusHidden(getName());
    }
    @Override
    public String toString() {
        return getName();
    }
    @Override
    public boolean equals(Object o) {
        if (o != null && getClass().equals(o.getClass())) {
            return getName().equals(((Corpus) o).getName());
        } else {
            return false;
        }
    }
    @Override
    public int hashCode() {
        return getName().hashCode();
    }
}
