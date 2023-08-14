public class SearchableCorpusFactory implements CorpusFactory {
    private static final String TAG = "QSB.SearchableCorpusFactory";
    private final Context mContext;
    private final Config mConfig;
    private final Factory<Executor> mWebCorpusExecutorFactory;
    public SearchableCorpusFactory(Context context, Config config,
            Factory<Executor> webCorpusExecutorFactory) {
        mContext = context;
        mConfig = config;
        mWebCorpusExecutorFactory = webCorpusExecutorFactory;
    }
    public Collection<Corpus> createCorpora(Sources sources) {
        ArrayList<Corpus> corpora = new ArrayList<Corpus>();
        addSpecialCorpora(corpora, sources);
        addSingleSourceCorpora(corpora, sources);
        return corpora;
    }
    protected Context getContext() {
        return mContext;
    }
    protected Config getConfig() {
        return mConfig;
    }
    protected Executor createWebCorpusExecutor() {
        return mWebCorpusExecutorFactory.create();
    }
    protected void addSpecialCorpora(ArrayList<Corpus> corpora, Sources sources) {
        addCorpus(corpora, createWebCorpus(sources));
        addCorpus(corpora, createAppsCorpus(sources));
    }
    protected void addSingleSourceCorpora(ArrayList<Corpus> corpora, Sources sources) {
        HashSet<Source> claimedSources = new HashSet<Source>();
        for (Corpus specialCorpus : corpora) {
            claimedSources.addAll(specialCorpus.getSources());
        }
        for (Source source : sources.getSources()) {
            if (!claimedSources.contains(source)) {
                addCorpus(corpora, createSingleSourceCorpus(source));
            }
        }
    }
    private void addCorpus(ArrayList<Corpus> corpora, Corpus corpus) {
        if (corpus != null) corpora.add(corpus);
    }
    protected Corpus createWebCorpus(Sources sources) {
        Source webSource = sources.getWebSearchSource();
        if (webSource != null && !webSource.canRead()) {
            Log.w(TAG, "Can't read web source " + webSource.getName());
            webSource = null;
        }
        Source browserSource = getBrowserSource(sources);
        if (browserSource != null && !browserSource.canRead()) {
            Log.w(TAG, "Can't read browser source " + browserSource.getName());
            browserSource = null;
        }
        Executor executor = createWebCorpusExecutor();
        return new WebCorpus(mContext, mConfig, executor, webSource, browserSource);
    }
    protected Corpus createAppsCorpus(Sources sources) {
        Source appsSource = getAppsSource(sources);
        return new AppsCorpus(mContext, mConfig, appsSource);
    }
    protected Corpus createSingleSourceCorpus(Source source) {
        if (!source.canRead()) return null;
        return new SingleSourceCorpus(mContext, mConfig, source);
    }
    protected Source getBrowserSource(Sources sources) {
        String name = getContext().getString(R.string.browser_search_component);
        return sources.getSource(name);
    }
    protected Source getAppsSource(Sources sources) {
        String name = getContext().getString(R.string.installed_apps_component);
        return sources.getSource(name);
    }
}
