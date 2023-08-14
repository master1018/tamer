public class SuggestionsProviderImplTest extends AndroidTestCase {
    private MockCorpora mCorpora;
    private MockNamedTaskExecutor mTaskExecutor;
    private SuggestionsProviderImpl mProvider;
    @Override
    protected void setUp() throws Exception {
        Config config = new Config(getContext());
        mTaskExecutor = new MockNamedTaskExecutor();
        Handler publishThread = new MockHandler();
        ShortcutRepository shortcutRepo = new MockShortcutRepository();
        mCorpora = new MockCorpora();
        mCorpora.addCorpus(MockCorpus.CORPUS_1);
        mCorpora.addCorpus(MockCorpus.CORPUS_2);
        CorpusRanker corpusRanker = new LexicographicalCorpusRanker(mCorpora);
        Logger logger = new NoLogger();
        mProvider = new SuggestionsProviderImpl(config,
                mTaskExecutor,
                publishThread,
                shortcutRepo,
                mCorpora,
                corpusRanker,
                logger);
        mProvider.setAllPromoter(new ConcatPromoter());
        mProvider.setSingleCorpusPromoter(new ConcatPromoter());
    }
    public void testSingleCorpus() {
        Suggestions suggestions = mProvider.getSuggestions("foo", MockCorpus.CORPUS_1, 3);
        try {
            assertEquals(1, suggestions.getExpectedResultCount());
            assertEquals(0, suggestions.getResultCount());
            assertEquals(0, suggestions.getPromoted().getCount());
            assertTrue(mTaskExecutor.runNext());
            assertEquals(1, suggestions.getExpectedResultCount());
            assertEquals(1, suggestions.getResultCount());
            assertEquals(MockCorpus.CORPUS_1.getSuggestions("foo", 3, true).getCount(),
                    suggestions.getPromoted().getCount());
            mTaskExecutor.assertDone();
        } finally {
            if (suggestions != null) suggestions.close();
        }
    }
    public void testMultipleCorpora() {
        Suggestions suggestions = mProvider.getSuggestions("foo", null, 6);
        try {
            int corpus1Count = MockCorpus.CORPUS_1.getSuggestions("foo", 3, true).getCount();
            int corpus2Count = MockCorpus.CORPUS_2.getSuggestions("foo", 3, true).getCount();
            assertEquals(mCorpora.getEnabledCorpora().size(), suggestions.getExpectedResultCount());
            assertEquals(0, suggestions.getResultCount());
            assertEquals(0, suggestions.getPromoted().getCount());
            assertTrue(mTaskExecutor.runNext());
            assertEquals(1, suggestions.getResultCount());
            assertEquals("Incorrect promoted: " + suggestions.getPromoted(),
                    corpus1Count, suggestions.getPromoted().getCount());
            assertTrue(mTaskExecutor.runNext());
            assertEquals(2, suggestions.getResultCount());
            assertEquals("Incorrect promoted: " + suggestions.getPromoted(),
                    corpus1Count + corpus2Count, suggestions.getPromoted().getCount());
            mTaskExecutor.assertDone();
        } finally {
            if (suggestions != null) suggestions.close();
        }
    }
}
