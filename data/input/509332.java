public class QsbApplication {
    private final Context mContext;
    private int mVersionCode;
    private Handler mUiThreadHandler;
    private Config mConfig;
    private Corpora mCorpora;
    private CorpusRanker mCorpusRanker;
    private ShortcutRepository mShortcutRepository;
    private ShortcutRefresher mShortcutRefresher;
    private NamedTaskExecutor mSourceTaskExecutor;
    private ThreadFactory mQueryThreadFactory;
    private SuggestionsProvider mSuggestionsProvider;
    private SuggestionViewFactory mSuggestionViewFactory;
    private CorpusViewFactory mCorpusViewFactory;
    private GoogleSource mGoogleSource;
    private VoiceSearch mVoiceSearch;
    private Logger mLogger;
    private SuggestionFormatter mSuggestionFormatter;
    private TextAppearanceFactory mTextAppearanceFactory;
    public QsbApplication(Context context) {
        mContext = context;
    }
    public static boolean isFroyoOrLater() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO;
    }
    public static QsbApplication get(Context context) {
        return ((QsbApplicationWrapper) context.getApplicationContext()).getApp();
    }
    protected Context getContext() {
        return mContext;
    }
    public int getVersionCode() {
        if (mVersionCode == 0) {
            try {
                PackageManager pm = getContext().getPackageManager();
                PackageInfo pkgInfo = pm.getPackageInfo(getContext().getPackageName(), 0);
                mVersionCode = pkgInfo.versionCode;
            } catch (PackageManager.NameNotFoundException ex) {
                throw new RuntimeException(ex);
            }
        }
        return mVersionCode;
    }
    protected void checkThread() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw new IllegalStateException("Accessed Application object from thread "
                    + Thread.currentThread().getName());
        }
    }
    protected void close() {
        checkThread();
        if (mConfig != null) {
            mConfig.close();
            mConfig = null;
        }
        if (mShortcutRepository != null) {
            mShortcutRepository.close();
            mShortcutRepository = null;
        }
        if (mSourceTaskExecutor != null) {
            mSourceTaskExecutor.close();
            mSourceTaskExecutor = null;
        }
        if (mSuggestionsProvider != null) {
            mSuggestionsProvider.close();
            mSuggestionsProvider = null;
        }
    }
    public synchronized Handler getMainThreadHandler() {
        if (mUiThreadHandler == null) {
            mUiThreadHandler = new Handler(Looper.getMainLooper());
        }
        return mUiThreadHandler;
    }
    public void runOnUiThread(Runnable action) {
        getMainThreadHandler().post(action);
    }
    public void onStartupComplete() {
    }
    public synchronized Config getConfig() {
        if (mConfig == null) {
            mConfig = createConfig();
        }
        return mConfig;
    }
    protected Config createConfig() {
        return new Config(getContext());
    }
    public Corpora getCorpora() {
        checkThread();
        if (mCorpora == null) {
            mCorpora = createCorpora();
        }
        return mCorpora;
    }
    protected Corpora createCorpora() {
        SearchableCorpora corpora = new SearchableCorpora(getContext(), createSources(),
                createCorpusFactory());
        corpora.update();
        return corpora;
    }
    public void updateCorpora() {
        checkThread();
        if (mCorpora != null) {
            mCorpora.update();
        }
    }
    protected Sources createSources() {
        return new SearchableSources(getContext());
    }
    protected CorpusFactory createCorpusFactory() {
        int numWebCorpusThreads = getConfig().getNumWebCorpusThreads();
        return new SearchableCorpusFactory(getContext(), getConfig(),
                createExecutorFactory(numWebCorpusThreads));
    }
    protected Factory<Executor> createExecutorFactory(final int numThreads) {
        final ThreadFactory threadFactory = getQueryThreadFactory();
        return new Factory<Executor>() {
            public Executor create() {
                return Executors.newFixedThreadPool(numThreads, threadFactory);
            }
        };
    }
    public CorpusRanker getCorpusRanker() {
        checkThread();
        if (mCorpusRanker == null) {
            mCorpusRanker = createCorpusRanker();
        }
        return mCorpusRanker;
    }
    protected CorpusRanker createCorpusRanker() {
        return new DefaultCorpusRanker(getCorpora(), getShortcutRepository());
    }
    public ShortcutRepository getShortcutRepository() {
        checkThread();
        if (mShortcutRepository == null) {
            mShortcutRepository = createShortcutRepository();
        }
        return mShortcutRepository;
    }
    protected ShortcutRepository createShortcutRepository() {
        ThreadFactory logThreadFactory = new NamingThreadFactory("ShortcutRepositoryWriter #%d",
                new PriorityThreadFactory(Process.THREAD_PRIORITY_BACKGROUND));
        Executor logExecutor = Executors.newSingleThreadExecutor(logThreadFactory);
        return ShortcutRepositoryImplLog.create(getContext(), getConfig(), getCorpora(),
            getShortcutRefresher(), getMainThreadHandler(), logExecutor);
    }
    public ShortcutRefresher getShortcutRefresher() {
        checkThread();
        if (mShortcutRefresher == null) {
            mShortcutRefresher = createShortcutRefresher();
        }
        return mShortcutRefresher;
    }
    protected ShortcutRefresher createShortcutRefresher() {
        return new SourceShortcutRefresher(createSourceTaskExecutor());
    }
    public NamedTaskExecutor getSourceTaskExecutor() {
        checkThread();
        if (mSourceTaskExecutor == null) {
            mSourceTaskExecutor = createSourceTaskExecutor();
        }
        return mSourceTaskExecutor;
    }
    protected NamedTaskExecutor createSourceTaskExecutor() {
        ThreadFactory queryThreadFactory = getQueryThreadFactory();
        return new PerNameExecutor(SingleThreadNamedTaskExecutor.factory(queryThreadFactory));
    }
    protected ThreadFactory getQueryThreadFactory() {
        checkThread();
        if (mQueryThreadFactory == null) {
            mQueryThreadFactory = createQueryThreadFactory();
        }
        return mQueryThreadFactory;
    }
    protected ThreadFactory createQueryThreadFactory() {
        String nameFormat = "QSB #%d";
        int priority = getConfig().getQueryThreadPriority();
        return new NamingThreadFactory(nameFormat,
                new PriorityThreadFactory(priority));
    }
    protected SuggestionsProvider getSuggestionsProvider() {
        checkThread();
        if (mSuggestionsProvider == null) {
            mSuggestionsProvider = createSuggestionsProvider();
        }
        return mSuggestionsProvider;
    }
    protected SuggestionsProvider createSuggestionsProvider() {
        int maxShortcutsPerWebSource = getConfig().getMaxShortcutsPerWebSource();
        int maxShortcutsPerNonWebSource = getConfig().getMaxShortcutsPerNonWebSource();
        Promoter allPromoter = new ShortcutLimitingPromoter(
                maxShortcutsPerWebSource,
                maxShortcutsPerNonWebSource,
                new ShortcutPromoter(
                        new RankAwarePromoter(getConfig(), getCorpora())));
        Promoter singleCorpusPromoter = new ShortcutPromoter(new ConcatPromoter());
        SuggestionsProviderImpl provider = new SuggestionsProviderImpl(getConfig(),
                getSourceTaskExecutor(),
                getMainThreadHandler(),
                getShortcutRepository(),
                getCorpora(),
                getCorpusRanker(),
                getLogger());
        provider.setAllPromoter(allPromoter);
        provider.setSingleCorpusPromoter(singleCorpusPromoter);
        return provider;
    }
    public SuggestionViewFactory getSuggestionViewFactory() {
        checkThread();
        if (mSuggestionViewFactory == null) {
            mSuggestionViewFactory = createSuggestionViewFactory();
        }
        return mSuggestionViewFactory;
    }
    protected SuggestionViewFactory createSuggestionViewFactory() {
        return new SuggestionViewInflater(getContext());
    }
    public CorpusViewFactory getCorpusViewFactory() {
        checkThread();
        if (mCorpusViewFactory == null) {
            mCorpusViewFactory = createCorpusViewFactory();
        }
        return mCorpusViewFactory;
    }
    protected CorpusViewFactory createCorpusViewFactory() {
        return new CorpusViewInflater(getContext());
    }
    public SuggestionsAdapter createSuggestionsAdapter() {
        SuggestionViewFactory viewFactory = getSuggestionViewFactory();
        DelayingSuggestionsAdapter adapter = new DelayingSuggestionsAdapter(viewFactory);
        return adapter;
    }
    public GoogleSource getGoogleSource() {
        checkThread();
        if (mGoogleSource == null) {
            mGoogleSource = createGoogleSource();
        }
        return mGoogleSource;
    }
    protected GoogleSource createGoogleSource() {
        return new GoogleSuggestClient(getContext());
    }
    public VoiceSearch getVoiceSearch() {
        checkThread();
        if (mVoiceSearch == null) {
            mVoiceSearch = createVoiceSearch();
        }
        return mVoiceSearch;
    }
    protected VoiceSearch createVoiceSearch() {
        return new VoiceSearch(getContext());
    }
    public Logger getLogger() {
        checkThread();
        if (mLogger == null) {
            mLogger = createLogger();
        }
        return mLogger;
    }
    protected Logger createLogger() {
        return new EventLogLogger(getContext(), getConfig());
    }
    public SuggestionFormatter getSuggestionFormatter() {
        if (mSuggestionFormatter == null) {
            mSuggestionFormatter = createSuggestionFormatter();
        }
        return mSuggestionFormatter;
    }
    protected SuggestionFormatter createSuggestionFormatter() {
        return new LevenshteinSuggestionFormatter(getTextAppearanceFactory());
    }
    public TextAppearanceFactory getTextAppearanceFactory() {
        if (mTextAppearanceFactory == null) {
            mTextAppearanceFactory = createTextAppearanceFactory();
        }
        return mTextAppearanceFactory;
    }
    protected TextAppearanceFactory createTextAppearanceFactory() {
        return new TextAppearanceFactory(getContext());
    }
}
