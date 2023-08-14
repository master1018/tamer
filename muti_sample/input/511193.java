class ShouldQueryStrategy {
    private static final boolean DBG = false;
    private static final String TAG = "QSB.ShouldQueryStrategy";
    private String mLastQuery = "";
    private final HashMap<Corpus, Integer> mEmptyCorpora
            = new HashMap<Corpus, Integer>();
    public boolean shouldQueryCorpus(Corpus corpus, String query) {
        updateQuery(query);
        if (query.length() >= corpus.getQueryThreshold()) {
            if (!corpus.queryAfterZeroResults() && mEmptyCorpora.containsKey(corpus)) {
                if (DBG) Log.i(TAG, "Not querying " + corpus + ", returned 0 after "
                        + mEmptyCorpora.get(corpus));
                return false;
            }
            return true;
        }
        if (DBG) Log.d(TAG, "Query too short for corpus " + corpus);
        return false;
    }
    public void onZeroResults(Corpus corpus, String query) {
        if (mLastQuery.startsWith(query) && !corpus.queryAfterZeroResults()
                && !TextUtils.isEmpty(query)) {
            if (DBG) Log.d(TAG, corpus + " returned 0 results for '" + query + "'");
            mEmptyCorpora.put(corpus, query.length());
        }
    }
    private void updateQuery(String query) {
        if (query.startsWith(mLastQuery)) {
        } else if (mLastQuery.startsWith(query)) {
            Iterator<Map.Entry<Corpus, Integer>> iter = mEmptyCorpora.entrySet().iterator();
            while (iter.hasNext()) {
                if (iter.next().getValue() > query.length()) {
                    iter.remove();
                }
            }
        } else {
            mEmptyCorpora.clear();
        }
        mLastQuery = query;
    }
}
