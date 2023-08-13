public class Suggestions {
    private static final boolean DBG = false;
    private static final String TAG = "QSB.Suggestions";
    private static int sId = 0;
    private final int mId;
    private final int mMaxPromoted;
    private final String mQuery;
    private final List<Corpus> mExpectedCorpora;
    private Corpus mSingleCorpusFilter;
    private final DataSetObservable mDataSetObservable = new DataSetObservable();
    private final ArrayList<CorpusResult> mCorpusResults;
    private ShortcutCursor mShortcuts;
    private final MyShortcutsObserver mShortcutsObserver = new MyShortcutsObserver();
    private boolean mClosed = false;
    private final Promoter mPromoter;
    private SuggestionCursor mPromoted;
    public Suggestions(Promoter promoter, int maxPromoted,
            String query, List<Corpus> expectedCorpora) {
        mPromoter = promoter;
        mMaxPromoted = maxPromoted;
        mQuery = query;
        mExpectedCorpora = expectedCorpora;
        mCorpusResults = new ArrayList<CorpusResult>(mExpectedCorpora.size());
        mPromoted = null;  
        mId = sId++;
        if (DBG) {
            Log.d(TAG, "new Suggestions [" + mId + "] query \"" + query
                    + "\" expected corpora: " + mExpectedCorpora);
        }
    }
    @VisibleForTesting
    public String getQuery() {
        return mQuery;
    }
    public List<Corpus> getExpectedCorpora() {
        return mExpectedCorpora;
    }
    @VisibleForTesting
    int getExpectedResultCount() {
        return mExpectedCorpora.size();
    }
    public void registerDataSetObserver(DataSetObserver observer) {
        if (mClosed) {
            throw new IllegalStateException("registerDataSetObserver() when closed");
        }
        mDataSetObservable.registerObserver(observer);
    }
    public void unregisterDataSetObserver(DataSetObserver observer) {
        mDataSetObservable.unregisterObserver(observer);
    }
    public SuggestionCursor getPromoted() {
        if (mPromoted == null) {
            updatePromoted();
        }
        if (DBG) Log.d(TAG, "getPromoted() = " + mPromoted);
        return mPromoted;
    }
    public Set<Corpus> getIncludedCorpora() {
        HashSet<Corpus> corpora = new HashSet<Corpus>();
        for (CorpusResult result : mCorpusResults) {
            corpora.add(result.getCorpus());
        }
        return corpora;
    }
    private void notifyDataSetChanged() {
        if (DBG) Log.d(TAG, "notifyDataSetChanged()");
        mDataSetObservable.notifyChanged();
    }
    public void close() {
        if (DBG) Log.d(TAG, "close() [" + mId + "]");
        if (mClosed) {
            throw new IllegalStateException("Double close()");
        }
        mDataSetObservable.unregisterAll();
        mClosed = true;
        if (mShortcuts != null) {
            mShortcuts.close();
            mShortcuts = null;
        }
        for (CorpusResult result : mCorpusResults) {
            result.close();
        }
        mCorpusResults.clear();
    }
    public boolean isClosed() {
        return mClosed;
    }
    @Override
    protected void finalize() {
        if (!mClosed) {
            Log.e(TAG, "LEAK! Finalized without being closed: Suggestions[" + mQuery + "]");
        }
    }
    public boolean isDone() {
        return mCorpusResults.size() >= mExpectedCorpora.size();
    }
    public void setShortcuts(ShortcutCursor shortcuts) {
        if (DBG) Log.d(TAG, "setShortcuts(" + shortcuts + ")");
        mShortcuts = shortcuts;
        if (shortcuts != null) {
            mShortcuts.registerDataSetObserver(mShortcutsObserver);
        }
    }
    public void addCorpusResults(List<CorpusResult> corpusResults) {
        if (mClosed) {
            for (CorpusResult corpusResult : corpusResults) {
                corpusResult.close();
            }
            return;
        }
        for (CorpusResult corpusResult : corpusResults) {
            if (DBG) {
                Log.d(TAG, "addCorpusResult["+ mId + "] corpus:" +
                        corpusResult.getCorpus().getName() + " results:" + corpusResult.getCount());
            }
            if (!mQuery.equals(corpusResult.getUserQuery())) {
              throw new IllegalArgumentException("Got result for wrong query: "
                    + mQuery + " != " + corpusResult.getUserQuery());
            }
            mCorpusResults.add(corpusResult);
        }
        mPromoted = null;
        notifyDataSetChanged();
    }
    private void updatePromoted() {
        if (mSingleCorpusFilter == null) {
            ListSuggestionCursor promoted = new ListSuggestionCursorNoDuplicates(mQuery);
            mPromoted = promoted;
            if (mPromoter == null) {
                return;
            }
            mPromoter.pickPromoted(mShortcuts, mCorpusResults, mMaxPromoted, promoted);
            if (DBG) {
                Log.d(TAG, "pickPromoted(" + mShortcuts + "," + mCorpusResults + ","
                        + mMaxPromoted + ") = " + mPromoted);
            }
            refreshShortcuts();
        } else {
            mPromoted = getCorpusResult(mSingleCorpusFilter);
            if (mPromoted == null) {
                mPromoted = new ListSuggestionCursor(mQuery);
            }
        }
    }
    private void refreshShortcuts() {
        if (DBG) Log.d(TAG, "refreshShortcuts(" + mPromoted + ")");
        for (int i = 0; i < mPromoted.getCount(); ++i) {
            mPromoted.moveTo(i);
            if (mPromoted.isSuggestionShortcut()) {
                mShortcuts.refresh(mPromoted);
            }
        }
    }
    private CorpusResult getCorpusResult(Corpus corpus) {
        for (CorpusResult result : mCorpusResults) {
            if (result.getCorpus().equals(mSingleCorpusFilter)) {
                return result;
            }
        }
        return null;
    }
    public int getResultCount() {
        if (mClosed) {
            throw new IllegalStateException("Called getSourceCount() when closed.");
        }
        return mCorpusResults == null ? 0 : mCorpusResults.size();
    }
    public void filterByCorpus(Corpus singleCorpus) {
        if (mSingleCorpusFilter == singleCorpus) {
            return;
        }
        mSingleCorpusFilter = singleCorpus;
        if ((mExpectedCorpora.size() == 1) && (mExpectedCorpora.get(0) == singleCorpus)) {
            return;
        }
        updatePromoted();
        notifyDataSetChanged();
    }
    @Override
    public String toString() {
        return "Suggestions{expectedCorpora=" + mExpectedCorpora
                + ",mCorpusResults.size()=" + mCorpusResults.size() + "}";
    }
    private class MyShortcutsObserver extends DataSetObserver {
        @Override
        public void onChanged() {
            mPromoted = null;
            notifyDataSetChanged();
        }
    }
}
