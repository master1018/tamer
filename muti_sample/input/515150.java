public abstract class MultiSourceCorpus extends AbstractCorpus {
    private final Executor mExecutor;
    private final ArrayList<Source> mSources;
    private boolean mSourcePropertiesValid;
    private int mQueryThreshold;
    private boolean mQueryAfterZeroResults;
    private boolean mVoiceSearchEnabled;
    private boolean mIsLocationAware;
    public MultiSourceCorpus(Context context, Config config,
            Executor executor, Source... sources) {
        super(context, config);
        mExecutor = executor;
        mSources = new ArrayList<Source>();
        for (Source source : sources) {
            addSource(source);
        }
    }
    protected void addSource(Source source) {
        if (source != null) {
            mSources.add(source);
            mSourcePropertiesValid = false;
        }
    }
    public Collection<Source> getSources() {
        return mSources;
    }
    protected Result createResult(String query, ArrayList<SourceResult> results, int latency) {
        return new Result(query, results, latency);
    }
    protected List<Source> getSourcesToQuery(String query, boolean onlyCorpus) {
        List<Source> sources = new ArrayList<Source>();
        for (Source candidate : getSources()) {
            if (candidate.getQueryThreshold() <= query.length()) {
                sources.add(candidate);
            }
        }
        return sources;
    }
    private void updateSourceProperties() {
        if (mSourcePropertiesValid) return;
        mQueryThreshold = Integer.MAX_VALUE;
        mQueryAfterZeroResults = false;
        mVoiceSearchEnabled = false;
        mIsLocationAware = false;
        for (Source s : getSources()) {
            mQueryThreshold = Math.min(mQueryThreshold, s.getQueryThreshold());
            mQueryAfterZeroResults |= s.queryAfterZeroResults();
            mVoiceSearchEnabled |= s.voiceSearchEnabled();
            mIsLocationAware |= s.isLocationAware();
        }
        if (mQueryThreshold == Integer.MAX_VALUE) {
            mQueryThreshold = 0;
        }
        mSourcePropertiesValid = true;
    }
    public int getQueryThreshold() {
        updateSourceProperties();
        return mQueryThreshold;
    }
    public boolean queryAfterZeroResults() {
        updateSourceProperties();
        return mQueryAfterZeroResults;
    }
    public boolean voiceSearchEnabled() {
        updateSourceProperties();
        return mVoiceSearchEnabled;
    }
    public boolean isLocationAware() {
        updateSourceProperties();
        return mIsLocationAware;
    }
    public CorpusResult getSuggestions(String query, int queryLimit, boolean onlyCorpus) {
        LatencyTracker latencyTracker = new LatencyTracker();
        List<Source> sources = getSourcesToQuery(query, onlyCorpus);
        BarrierConsumer<SourceResult> consumer =
                new BarrierConsumer<SourceResult>(sources.size());
        boolean onlySource = sources.size() == 1;
        for (Source source : sources) {
            QueryTask<SourceResult> task = new QueryTask<SourceResult>(query, queryLimit,
                    source, null, consumer, onlySource);
            mExecutor.execute(task);
        }
        ArrayList<SourceResult> results = consumer.getValues();
        int latency = latencyTracker.getLatency();
        Result result = createResult(query, results, latency);
        result.fill();
        return result;
    }
    protected class Result extends ListSuggestionCursor implements CorpusResult {
        private final ArrayList<SourceResult> mResults;
        private final int mLatency;
        public Result(String userQuery, ArrayList<SourceResult> results, int latency) {
            super(userQuery);
            mResults = results;
            mLatency = latency;
        }
        protected ArrayList<SourceResult> getResults() {
            return mResults;
        }
        public void fill() {
            for (SourceResult result : getResults()) {
                int count = result.getCount();
                for (int i = 0; i < count; i++) {
                    result.moveTo(i);
                    add(new SuggestionPosition(result));
                }
            }
        }
        public Corpus getCorpus() {
            return MultiSourceCorpus.this;
        }
        public int getLatency() {
            return mLatency;
        }
        @Override
        public void close() {
            super.close();
            for (SourceResult result : mResults) {
                result.close();
            }
        }
        @Override
        public String toString() {
            return getCorpus() + "[" + getUserQuery() + "]";
        }
    }
}
