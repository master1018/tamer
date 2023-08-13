public class ShortcutLimitingPromoterTest extends AndroidTestCase {
    private String mQuery;
    private Suggestion mS11;
    private Suggestion mS12;
    private Suggestion mS21;
    private Suggestion mS22;
    private Suggestion mWeb1;
    private Suggestion mWeb2;
    private ShortcutTrap mShortcutTrap;
    @Override
    protected void setUp() throws Exception {
        mQuery = "foo";
        mS11 = MockSource.SOURCE_1.createSuggestion(mQuery + "_1_1");
        mS12 = MockSource.SOURCE_1.createSuggestion(mQuery + "_1_2");
        mS21 = MockSource.SOURCE_2.createSuggestion(mQuery + "_1_1");
        mS22 = MockSource.SOURCE_2.createSuggestion(mQuery + "_1_2");
        mWeb1 = MockSource.WEB_SOURCE.createSuggestion(mQuery + "_web_1");
        mWeb2 = MockSource.WEB_SOURCE.createSuggestion(mQuery + "_web_2");
        mShortcutTrap = new ShortcutTrap();
    }
    public void testZeroShortcutsPerSource() {
        SuggestionCursor promoted = promote(cursor(mS11, mS12, mS21, mS22), 0, 0);
        SuggestionCursor expected = cursor();
        assertSameSuggestions(expected, promoted);
    }
    public void testOneShortcutPerSource() {
        SuggestionCursor promoted = promote(cursor(mS11, mS12, mS21, mS22), 1, 1);
        SuggestionCursor expected = cursor(mS11, mS21);
        assertSameSuggestions(expected, promoted);
    }
    public void testTwoShortcutsPerSource() {
        SuggestionCursor promoted = promote(cursor(mS11, mS12, mS21, mS22), 2, 2);
        SuggestionCursor expected = cursor(mS11, mS12, mS21, mS22);
        assertSameSuggestions(expected, promoted);
    }
    public void testThreeShortcutsPerSource() {
        SuggestionCursor promoted = promote(cursor(mS11, mS12, mS21, mS22), 3, 3);
        SuggestionCursor expected = cursor(mS11, mS12, mS21, mS22);
        assertSameSuggestions(expected, promoted);
    }
    public void testOneSourceZeroPromoted() {
        SuggestionCursor promoted = promote(cursor(mS11, mS12), 0, 0);
        SuggestionCursor expected = cursor();
        assertSameSuggestions(expected, promoted);
    }
    public void testOneSourceOnePromoted() {
        SuggestionCursor promoted = promote(cursor(mS11, mS12), 1, 1);
        SuggestionCursor expected = cursor(mS11);
        assertSameSuggestions(expected, promoted);
    }
    public void testOneSourceTwoPromoted() {
        SuggestionCursor promoted = promote(cursor(mS11, mS12), 2, 2);
        SuggestionCursor expected = cursor(mS11, mS12);
        assertSameSuggestions(expected, promoted);
    }
    public void testNoShortcuts() {
        SuggestionCursor promoted = promote(cursor(), 2, 2);
        SuggestionCursor expected = cursor();
        assertSameSuggestions(expected, promoted);
    }
    public void testZeroWebShortcuts() {
        SuggestionCursor promoted = promote(cursor(mS11, mS12, mWeb1, mWeb2), 0, 2);
        SuggestionCursor expected = cursor(mS11, mS12);
        assertSameSuggestions(expected, promoted);
    }
    public void testTwoWebShortcuts() {
        SuggestionCursor promoted = promote(cursor(mS11, mS12, mWeb1, mWeb2), 2, 2);
        SuggestionCursor expected = cursor(mS11, mS12, mWeb1, mWeb2);
        assertSameSuggestions(expected, promoted);
    }
    private SuggestionCursor promote(SuggestionCursor shortcuts, int maxShortcutsPerWebSource,
            int maxShortcutsPerNonWebSource) {
        Promoter promoter = new ShortcutLimitingPromoter(maxShortcutsPerWebSource,
                maxShortcutsPerNonWebSource, mShortcutTrap);
        int maxPromoted = 10;
        ListSuggestionCursor promoted = new ListSuggestionCursor(mQuery);
        promoter.pickPromoted(shortcuts, null, maxPromoted, promoted);
        return promoted;
    }
    private class ShortcutTrap implements Promoter {
        public void pickPromoted(SuggestionCursor shortcuts, ArrayList<CorpusResult> suggestions,
                int maxPromoted, ListSuggestionCursor promoted) {
            SuggestionCursorUtil.addAll(promoted, shortcuts);
        }
    }
    private ListSuggestionCursor cursor(Suggestion... suggestions) {
        return new ListSuggestionCursor(mQuery, suggestions);
    }
}
