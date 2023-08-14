public class MockCorpora implements Corpora {
    private static final String TAG = "QSB.MockCorpora";
    private final DataSetObservable mDataSetObservable = new DataSetObservable();
    private HashMap<String,Corpus> mCorporaByName = new HashMap<String,Corpus>();
    private HashSet<Corpus> mDefaultCorpora = new HashSet<Corpus>();
    private Corpus mWebCorpus;
    public void addCorpus(Corpus corpus) {
        Corpus oldCorpus = mCorporaByName.put(corpus.getName(), corpus);
        if (oldCorpus != null) {
            Log.d(TAG, "Replaced " + oldCorpus + " with " + corpus);
        }
        notifyDataSetChanged();
    }
    public void setWebCorpus(Corpus webCorpus) {
        mWebCorpus = webCorpus;
    }
    public void addDefaultCorpus(Corpus corpus) {
        mDefaultCorpora.add(corpus);
    }
    public Collection<Corpus> getAllCorpora() {
        return Collections.unmodifiableCollection(mCorporaByName.values());
    }
    public Corpus getCorpus(String name) {
        return mCorporaByName.get(name);
    }
    public Corpus getWebCorpus() {
        return mWebCorpus;
    }
    public Corpus getCorpusForSource(Source source) {
        for (Corpus corpus : mCorporaByName.values()) {
            for (Source corpusSource : corpus.getSources()) {
                if (corpusSource.equals(source)) {
                    return corpus;
                }
            }
        }
        return null;
    }
    public Collection<Corpus> getEnabledCorpora() {
        return getAllCorpora();
    }
    public Source getSource(String name) {
        for (Corpus corpus : mCorporaByName.values()) {
            for (Source source : corpus.getSources()) {
                if (source.getName().equals(name)) {
                    return source;
                }
            }
        }
        return null;
    }
    public boolean isCorpusDefaultEnabled(Corpus corpus) {
        return mDefaultCorpora.contains(corpus);
    }
    public boolean isCorpusEnabled(Corpus corpus) {
        return true;
    }
    public void update() {
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
