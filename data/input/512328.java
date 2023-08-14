public class SingleSourceCorpus extends AbstractCorpus {
    private final Source mSource;
    public SingleSourceCorpus(Context context, Config config, Source source) {
        super(context, config);
        mSource = source;
    }
    public Drawable getCorpusIcon() {
        return mSource.getSourceIcon();
    }
    public Uri getCorpusIconUri() {
        return mSource.getSourceIconUri();
    }
    public CharSequence getLabel() {
        return mSource.getLabel();
    }
    public CharSequence getHint() {
        return mSource.getHint();
    }
    public CharSequence getSettingsDescription() {
        return mSource.getSettingsDescription();
    }
    public CorpusResult getSuggestions(String query, int queryLimit, boolean onlyCorpus) {
        LatencyTracker latencyTracker = new LatencyTracker();
        SourceResult sourceResult = mSource.getSuggestions(query, queryLimit, true);
        int latency = latencyTracker.getLatency();
        return new SingleSourceCorpusResult(this, query, sourceResult, latency);
    }
    public String getName() {
        return mSource.getName();
    }
    public boolean queryAfterZeroResults() {
        return mSource.queryAfterZeroResults();
    }
    public int getQueryThreshold() {
        return mSource.getQueryThreshold();
    }
    public boolean voiceSearchEnabled() {
        return mSource.voiceSearchEnabled();
    }
    public Intent createSearchIntent(String query, Bundle appData) {
        return mSource.createSearchIntent(query, appData);
    }
    public Intent createVoiceSearchIntent(Bundle appData) {
        return mSource.createVoiceSearchIntent(appData);
    }
    public SuggestionData createSearchShortcut(String query) {
        return null;
    }
    public boolean isWebCorpus() {
        return false;
    }
    public boolean isLocationAware() {
        return mSource.isLocationAware();
    }
    public Collection<Source> getSources() {
        return Collections.singletonList(mSource);
    }
}
