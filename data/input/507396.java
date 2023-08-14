public abstract class AbstractCorpusRanker implements CorpusRanker {
    private final DataSetObservable mDataSetObservable = new DataSetObservable();
    private final Corpora mCorpora;
    private List<Corpus> mRankedCorpora;
    public AbstractCorpusRanker(Corpora corpora) {
        mCorpora = corpora;
        mCorpora.registerDataSetObserver(new CorporaObserver());
    }
    protected abstract List<Corpus> rankCorpora(Corpora corpora);
    public List<Corpus> getRankedCorpora() {
        if (mRankedCorpora == null) {
            mRankedCorpora = Collections.unmodifiableList(rankCorpora(mCorpora));
        }
        return mRankedCorpora;
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
    private class CorporaObserver extends DataSetObserver {
        @Override
        public void onChanged() {
            mRankedCorpora = null;
            notifyDataSetChanged();
        }
    }
}
