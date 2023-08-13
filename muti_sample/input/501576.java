public class SuggestionsTest extends AndroidTestCase {
    private Suggestions mSuggestions;
    private MockDataSetObserver mObserver;
    private List<Corpus> mExpectedCorpora;
    @Override
    protected void setUp() throws Exception {
        mExpectedCorpora = Arrays.asList(new Corpus[]{null,null});
        mSuggestions = new Suggestions(null, 0, "foo", mExpectedCorpora);
        mObserver = new MockDataSetObserver();
        mSuggestions.registerDataSetObserver(mObserver);
    }
    @Override
    protected void tearDown() throws Exception {
        mSuggestions.close();
        mSuggestions = null;
    }
    public void testGetExpectedResultCount() {
        assertEquals(mExpectedCorpora.size(), mSuggestions.getExpectedResultCount());
    }
    public void testGetExpectedCorpora() {
        List<Corpus> expectedCorpora = mSuggestions.getExpectedCorpora();
        assertEquals(mExpectedCorpora.size(), expectedCorpora.size());
        for (int i=0; i<mExpectedCorpora.size(); ++i) {
            assertEquals(mExpectedCorpora.get(i), expectedCorpora.get(i));
        }
    }
    public void testGetUserQuery() {
        assertEquals("foo", mSuggestions.getQuery());
    }
    public void testGetIncludedCorpora() {
        Corpus corpus = MockCorpus.CORPUS_1;
        mSuggestions.addCorpusResults(
                Collections.singletonList(corpus.getSuggestions("foo", 50, true)));
        Set<Corpus> includedCorpora = mSuggestions.getIncludedCorpora();
        assertEquals(includedCorpora.size(), 1);
        assertTrue(includedCorpora.contains(corpus));
    }
    public void testObserverNotified() {
        Corpus corpus = MockCorpus.CORPUS_1;
        mObserver.assertNotChanged();
        mObserver.assertNotInvalidated();
        mSuggestions.addCorpusResults(
                Collections.singletonList(corpus.getSuggestions("foo", 50, true)));
        mObserver.assertChanged();
        mObserver.assertNotInvalidated();
    }
}
