public class SearchableCorporaTest extends AndroidTestCase {
    protected SearchableCorpora mCorpora;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockSources sources = new MockSources();
        sources.addSource(MockSource.SOURCE_1);
        sources.addSource(MockSource.SOURCE_2);
        mCorpora = new SearchableCorpora(mContext, sources, new MockCorpusFactory());
        mCorpora.update();
    }
    public void testGetAllCorpora() {
        assertNotEmpty(mCorpora.getAllCorpora());
    }
    public void testEnabledSuggestionSources() {
        assertNotNull(mCorpora.getEnabledCorpora());
    }
    public void testGetCorpusForSource() {
        assertNotNull(mCorpora.getCorpusForSource(MockSource.SOURCE_1));
        assertNotNull(mCorpora.getCorpusForSource(MockSource.SOURCE_2));
        assertNull(mCorpora.getCorpusForSource(MockSource.SOURCE_3));
    }
    static void assertEmpty(Collection<?> collection) {
        assertNotNull(collection);
        assertTrue(collection.isEmpty());
    }
    static void assertNotEmpty(Collection<?> collection) {
        assertNotNull(collection);
        assertFalse(collection.isEmpty());
    }
}
