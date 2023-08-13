public class RankAwarePromoterTest extends AndroidTestCase {
    public static final int NUM_SUGGESTIONS_ABOVE_KEYBOARD = 4;
    public static final int MAX_PROMOTED_CORPORA = 3;
    public static final int MAX_PROMOTED_SUGGESTIONS = 8;
    public static final String TEST_QUERY = "query";
    private CorpusRanker mRanker;
    private RankAwarePromoter mPromoter;
    @Override
    public void setUp() {
        Corpora corpora = createMockCorpora(5, MAX_PROMOTED_CORPORA);
        mRanker = new LexicographicalCorpusRanker(corpora);
        mPromoter = new RankAwarePromoter(new Config(mContext){
            @Override
            public int getNumSuggestionsAboveKeyboard() {
                return NUM_SUGGESTIONS_ABOVE_KEYBOARD;
            }
        }, corpora);
    }
    public void testPromotesExpectedSuggestions() {
        ArrayList<CorpusResult> suggestions = getSuggestions(TEST_QUERY);
        ListSuggestionCursor promoted = new ListSuggestionCursor(TEST_QUERY);
        mPromoter.pickPromoted(null, suggestions, MAX_PROMOTED_SUGGESTIONS, promoted);
        assertEquals(MAX_PROMOTED_SUGGESTIONS, promoted.getCount());
        int[] expectedSource = {0, 1, 2, 0, 1, 2, 3, 4};
        int[] expectedSuggestion = {1, 1, 1, 2, 2, 2, 1, 1};
        for (int i = 0; i < promoted.getCount(); i++) {
            promoted.moveTo(i);
            assertEquals("Source in position " + i,
                    "MockSource Source" + expectedSource[i],
                    promoted.getSuggestionSource().getLabel());
            assertEquals("Suggestion in position " + i,
                    TEST_QUERY + "_" + expectedSuggestion[i],
                    promoted.getSuggestionText1());
        }
    }
    private List<Corpus> getRankedCorpora() {
        return mRanker.getRankedCorpora();
    }
    private ArrayList<CorpusResult> getSuggestions(String query) {
        ArrayList<CorpusResult> suggestions = new ArrayList<CorpusResult>();
        for (Corpus corpus : getRankedCorpora()) {
            suggestions.add(corpus.getSuggestions(query, 10, false));
        }
        return suggestions;
    }
    private static MockCorpora createMockCorpora(int count, int defaultCount) {
        MockCorpora corpora = new MockCorpora();
        for (int i = 0; i < count; i++) {
            Source mockSource = new MockSource("Source" + i);
            Corpus mockCorpus = new MockCorpus(mockSource, i < defaultCount);
            corpora.addCorpus(mockCorpus);
        }
        return corpora;
    }
}
