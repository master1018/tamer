public class SingleSourceCorpusResult extends SuggestionCursorWrapper implements CorpusResult {
    private final Corpus mCorpus;
    private final int mLatency;
    public SingleSourceCorpusResult(Corpus corpus, String userQuery, SuggestionCursor cursor,
            int latency) {
        super(userQuery, cursor);
        mCorpus = corpus;
        mLatency = latency;
    }
    public Corpus getCorpus() {
        return mCorpus;
    }
    public int getLatency() {
        return mLatency;
    }
    @Override
    public String toString() {
        return getCorpus() + "[" + getUserQuery() + "]";
    }
}
