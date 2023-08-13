public class MultiSourceCorpusTest extends AndroidTestCase {
    protected MultiSourceCorpus mCorpus;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Config config = new Config(getContext());
        Executor executor = Executors.newSingleThreadExecutor();
        mCorpus = new SkeletonMultiSourceCorpus(getContext(), config, executor,
                MockSource.SOURCE_1, MockSource.SOURCE_2);
    }
    public void testGetSources() {
        MoreAsserts.assertContentsInOrder(mCorpus.getSources(),
                MockSource.SOURCE_1, MockSource.SOURCE_2);
    }
    public void testGetSuggestions() {
        ListSuggestionCursor expected = concatSuggestionCursors("foo",
                MockSource.SOURCE_1.getSuggestions("foo", 50, false),
                MockSource.SOURCE_2.getSuggestions("foo", 50, false));
        CorpusResult observed = mCorpus.getSuggestions("foo", 50, false);
        SuggestionCursorUtil.assertSameSuggestions(expected, observed);
    }
    private static ListSuggestionCursor concatSuggestionCursors(String query,
            SuggestionCursor... cursors) {
        ListSuggestionCursor out = new ListSuggestionCursor("foo");
        for (SuggestionCursor cursor : cursors) {
            int count = cursor.getCount();
            for (int i = 0; i < count; i++) {
                out.add(new SuggestionPosition(cursor, i));
            }
        }
        return out;
    }
    private static class SkeletonMultiSourceCorpus extends MultiSourceCorpus {
        public SkeletonMultiSourceCorpus(Context context, Config config, Executor executor,
                Source... sources) {
            super(context, config, executor, sources);
        }
        public Intent createSearchIntent(String query, Bundle appData) {
            return null;
        }
        public SuggestionData createSearchShortcut(String query) {
            return null;
        }
        public Intent createVoiceSearchIntent(Bundle appData) {
            return null;
        }
        public Drawable getCorpusIcon() {
            return null;
        }
        public Uri getCorpusIconUri() {
            return null;
        }
        public CharSequence getHint() {
            return null;
        }
        public CharSequence getLabel() {
            return null;
        }
        @Override
        public int getQueryThreshold() {
            return 0;
        }
        public CharSequence getSettingsDescription() {
            return null;
        }
        public boolean isWebCorpus() {
            return false;
        }
        @Override
        public boolean queryAfterZeroResults() {
            return false;
        }
        @Override
        public boolean voiceSearchEnabled() {
            return false;
        }
        public String getName() {
            return "SkeletonMultiSourceCorpus";
        }
    }
}
