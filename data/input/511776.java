public class SearchableCorpora implements Corpora {
    private static final boolean DBG = false;
    private static final String TAG = "QSB.DefaultCorpora";
    private final DataSetObservable mDataSetObservable = new DataSetObservable();
    private final Context mContext;
    private final CorpusFactory mCorpusFactory;
    private Sources mSources;
    private HashMap<String,Corpus> mCorporaByName;
    private HashMap<Source,Corpus> mCorporaBySource;
    private List<Corpus> mEnabledCorpora;
    private Corpus mWebCorpus;
    public SearchableCorpora(Context context, Sources sources, CorpusFactory corpusFactory) {
        mContext = context;
        mCorpusFactory = corpusFactory;
        mSources = sources;
    }
    protected Context getContext() {
        return mContext;
    }
    public Collection<Corpus> getAllCorpora() {
        return Collections.unmodifiableCollection(mCorporaByName.values());
    }
    public Collection<Corpus> getEnabledCorpora() {
        return mEnabledCorpora;
    }
    public Corpus getCorpus(String name) {
        return mCorporaByName.get(name);
    }
    public Corpus getWebCorpus() {
        return mWebCorpus;
    }
    public Corpus getCorpusForSource(Source source) {
        return mCorporaBySource.get(source);
    }
    public Source getSource(String name) {
        if (TextUtils.isEmpty(name)) {
            Log.w(TAG, "Empty source name");
            return null;
        }
        return mSources.getSource(name);
    }
    public void update() {
        mSources.update();
        Collection<Corpus> corpora = mCorpusFactory.createCorpora(mSources);
        mCorporaByName = new HashMap<String,Corpus>(corpora.size());
        mCorporaBySource = new HashMap<Source,Corpus>(corpora.size());
        mEnabledCorpora = new ArrayList<Corpus>(corpora.size());
        mWebCorpus = null;
        for (Corpus corpus : corpora) {
            mCorporaByName.put(corpus.getName(), corpus);
            for (Source source : corpus.getSources()) {
                mCorporaBySource.put(source, corpus);
            }
            if (corpus.isCorpusEnabled()) {
                mEnabledCorpora.add(corpus);
            }
            if (corpus.isWebCorpus()) {
                if (mWebCorpus != null) {
                    Log.w(TAG, "Multiple web corpora: " + mWebCorpus + ", " + corpus);
                }
                mWebCorpus = corpus;
            }
        }
        if (DBG) Log.d(TAG, "Updated corpora: " + mCorporaBySource.values());
        mEnabledCorpora = Collections.unmodifiableList(mEnabledCorpora);
        notifyDataSetChanged();
    }
    public void registerDataSetObserver(DataSetObserver observer) {
        mDataSetObservable.registerObserver(observer);
    }
    public void unregisterDataSetObserver(DataSetObserver observer) {
        mDataSetObservable.unregisterObserver(observer);
    }
    protected void notifyDataSetChanged() {
        mDataSetObservable.notifyChanged();
    }
}
