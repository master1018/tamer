public class ShouldQueryStrategyTest extends AndroidTestCase {
    private ShouldQueryStrategy mShouldQuery;
    @Override
    public void setUp() throws Exception {
        super.setUp();
        mShouldQuery = new ShouldQueryStrategy();
    }
    public static final Corpus CORPUS_1 = new MockCorpus(MockSource.SOURCE_1) {
        @Override
        public int getQueryThreshold() {
            return 3;
        }
    };
    public static final Corpus CORPUS_2 = new MockCorpus(MockSource.SOURCE_2) {
        @Override
        public boolean queryAfterZeroResults() {
            return true;
        }
    };
    public void testRespectsQueryThreshold() {
        assertFalse(mShouldQuery.shouldQueryCorpus(CORPUS_1, "aa"));
        assertTrue(mShouldQuery.shouldQueryCorpus(CORPUS_1, "aaa"));
        assertTrue(mShouldQuery.shouldQueryCorpus(CORPUS_2, ""));
    }
    public void testQueriesAfterNoResultsWhenQueryAfterZeroIsTrue() {
        assertTrue(mShouldQuery.shouldQueryCorpus(CORPUS_2, "query"));
        mShouldQuery.onZeroResults(CORPUS_2, "query");
        assertTrue(mShouldQuery.shouldQueryCorpus(CORPUS_2, "query"));
        assertTrue(mShouldQuery.shouldQueryCorpus(CORPUS_2, "query123"));
    }
    public void testDoesntQueryLongerAfterNoResults() {
        assertTrue(mShouldQuery.shouldQueryCorpus(CORPUS_1, "query"));
        mShouldQuery.onZeroResults(CORPUS_1, "query");
        assertFalse(mShouldQuery.shouldQueryCorpus(CORPUS_1, "queryx"));
        assertFalse(mShouldQuery.shouldQueryCorpus(CORPUS_1, "queryxy"));
    }
    public void testDoesntQuerySameAfterNoResults() {
        assertTrue(mShouldQuery.shouldQueryCorpus(CORPUS_1, "query"));
        mShouldQuery.onZeroResults(CORPUS_1, "query");
        assertFalse(mShouldQuery.shouldQueryCorpus(CORPUS_1, "query"));
    }
    public void testQueriesDifferent() {
        assertTrue(mShouldQuery.shouldQueryCorpus(CORPUS_1, "query"));
        mShouldQuery.onZeroResults(CORPUS_1, "query");
        assertTrue(mShouldQuery.shouldQueryCorpus(CORPUS_1, "queen"));
        mShouldQuery.onZeroResults(CORPUS_1, "queen");
        assertFalse(mShouldQuery.shouldQueryCorpus(CORPUS_1, "queens"));
    }
    public void testShorterThenDifferent() {
        assertTrue(mShouldQuery.shouldQueryCorpus(CORPUS_1, "query"));
        mShouldQuery.onZeroResults(CORPUS_1, "query");
        assertTrue(mShouldQuery.shouldQueryCorpus(CORPUS_1, "que"));
        mShouldQuery.onZeroResults(CORPUS_1, "que");
        assertFalse(mShouldQuery.shouldQueryCorpus(CORPUS_1, "queen"));
    }
    public void testQueriesForShorterAfterNoResults() {
        assertTrue(mShouldQuery.shouldQueryCorpus(CORPUS_1, "query"));
        mShouldQuery.onZeroResults(CORPUS_1, "query");
        assertTrue(mShouldQuery.shouldQueryCorpus(CORPUS_1, "quer"));
    }
    public void testOutOfOrder1() {
        assertTrue(mShouldQuery.shouldQueryCorpus(CORPUS_1, "quer"));
        mShouldQuery.onZeroResults(CORPUS_1, "que");
        assertFalse(mShouldQuery.shouldQueryCorpus(CORPUS_1, "quer"));
    }
    public void testOutOfOrder2() {
        assertTrue(mShouldQuery.shouldQueryCorpus(CORPUS_1, "quer"));
        mShouldQuery.onZeroResults(CORPUS_1, "que");
        assertFalse(mShouldQuery.shouldQueryCorpus(CORPUS_1, "query"));
    }
}
