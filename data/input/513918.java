public class MockCorpus implements Corpus {
    public static final Corpus CORPUS_1 = new MockCorpus(MockSource.SOURCE_1);
    public static final Corpus CORPUS_2 = new MockCorpus(MockSource.SOURCE_2);
    private final String mName;
    private final Source mSource;
    private final boolean mDefaultEnabled;
    public MockCorpus(Source source) {
        this(source, true);
    }
    public MockCorpus(Source source, boolean defaultEnabled) {
        mName = "corpus_" + source.getName();
        mSource = source;
        mDefaultEnabled = defaultEnabled;
    }
    public Intent createSearchIntent(String query, Bundle appData) {
        return null;
    }
    public Intent createVoiceSearchIntent(Bundle appData) {
        return null;
    }
    public SuggestionData createSearchShortcut(String query) {
        return null;
    }
    public Drawable getCorpusIcon() {
        return null;
    }
    public Uri getCorpusIconUri() {
        return null;
    }
    public CharSequence getLabel() {
        return mName;
    }
    public CharSequence getHint() {
        return null;
    }
    public String getName() {
        return mName;
    }
    public int getQueryThreshold() {
        return 0;
    }
    public Collection<Source> getSources() {
        return Collections.singletonList(mSource);
    }
    public CharSequence getSettingsDescription() {
        return null;
    }
    public CorpusResult getSuggestions(String query, int queryLimit, boolean onlyCorpus) {
        return new Result(query, mSource.getSuggestions(query, queryLimit, true));
    }
    @Override
    public String toString() {
        return getName();
    }
    @Override
    public boolean equals(Object o) {
        if (o != null && o.getClass().equals(this.getClass())) {
            MockCorpus s = (MockCorpus) o;
            return s.mName.equals(mName);
        }
        return false;
    }
    @Override
    public int hashCode() {
        return mName.hashCode();
    }
    private class Result extends SuggestionCursorWrapper implements CorpusResult {
        public Result(String userQuery, SuggestionCursor cursor) {
            super(userQuery, cursor);
        }
        public Corpus getCorpus() {
            return MockCorpus.this;
        }
        public int getLatency() {
            return 0;
        }
    }
    public boolean isWebCorpus() {
        return false;
    }
    public boolean queryAfterZeroResults() {
        return false;
    }
    public boolean voiceSearchEnabled() {
        return false;
    }
    public boolean isCorpusDefaultEnabled() {
        return mDefaultEnabled;
    }
    public boolean isCorpusEnabled() {
        return true;
    }
    public boolean isCorpusHidden() {
        return false;
    }
    public boolean isLocationAware() {
        return false;
    }
}
