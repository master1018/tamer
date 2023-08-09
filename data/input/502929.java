public class ShortcutPromoterTest extends AndroidTestCase {
    private String mQuery;
    private SuggestionCursor mShortcuts;
    private ArrayList<CorpusResult> mSuggestions;
    private int mSuggestionCount;
    @Override
    protected void setUp() throws Exception {
        mQuery = "foo";
        List<Corpus> corpora = Arrays.asList(MockCorpus.CORPUS_1, MockCorpus.CORPUS_2);
        mShortcuts = new MockShortcutRepository().getShortcutsForQuery(mQuery, null);
        mSuggestions = new ArrayList<CorpusResult>();
        for (Corpus corpus : corpora) {
            mSuggestions.add(corpus.getSuggestions(mQuery, 10, false));
        }
        mSuggestionCount = countSuggestions(mSuggestions);
    }
    @Override
    protected void tearDown() throws Exception {
        mQuery = null;
        mShortcuts.close();
        for (SuggestionCursor c : mSuggestions) {
            c.close();
        }
        mSuggestions = null;
    }
    public void testPickPromotedNoNext() {
        maxPromotedTestNoNext(0);
        maxPromotedTestNoNext(1);
        maxPromotedTestNoNext(2);
        maxPromotedTestNoNext(5);
    }
    public void testPickPromotedConcatNext() {
        maxPromotedTestConcatNext(0);
        maxPromotedTestConcatNext(1);
        maxPromotedTestConcatNext(2);
        maxPromotedTestConcatNext(6);
        maxPromotedTestConcatNext(7);
    }
    private void maxPromotedTestNoNext(int maxPromoted) {
        Promoter promoter = new ShortcutPromoter(null);
        int expectedCount = Math.min(maxPromoted, mShortcuts.getCount());
        ListSuggestionCursor promoted = new ListSuggestionCursor(mQuery);
        promoter.pickPromoted(mShortcuts, mSuggestions, maxPromoted, promoted);
        assertEquals(expectedCount, promoted.getCount());
        int count = Math.min(maxPromoted, mShortcuts.getCount());
        assertSameSuggestions(slice(promoted, 0, count), slice(mShortcuts, 0, count));
    }
    private void maxPromotedTestConcatNext(int maxPromoted) {
        Promoter promoter = new ShortcutPromoter(new ConcatPromoter());
        int expectedCount = Math.min(maxPromoted, mShortcuts.getCount() + mSuggestionCount);
        ListSuggestionCursor promoted = new ListSuggestionCursor(mQuery);
        promoter.pickPromoted(mShortcuts, mSuggestions, maxPromoted, promoted);
        assertEquals(expectedCount, promoted.getCount());
        int count = Math.min(maxPromoted, mShortcuts.getCount());
        assertSameSuggestions(slice(promoted, 0, count), slice(mShortcuts, 0, count));
        if (mShortcuts.getCount() < expectedCount) {
            assertSameSuggestion("wrong suggestion after shortcuts",
                    promoted, mShortcuts.getCount(), mSuggestions.get(0), 0);
        }
    }
    private static int countSuggestions(ArrayList<CorpusResult> suggestions) {
        int count = 0;
        for (SuggestionCursor c : suggestions) {
            count += c.getCount();
        }
        return count;
    }
}
