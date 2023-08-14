class TestProvider extends SearchRecentSuggestionsProvider {
    final static String AUTHORITY = "android.content.TestProvider";
    final static int MODE = DATABASE_MODE_QUERIES + DATABASE_MODE_2LINES;
    public TestProvider() {
        super();
        setupSuggestions(AUTHORITY, MODE);
    }
}
@Suppress
public class SearchRecentSuggestionsProviderTest extends ProviderTestCase2<TestProvider> {
    SearchRecentSuggestions mSearchHelper;
    public SearchRecentSuggestionsProviderTest() {
        super(TestProvider.class, TestProvider.AUTHORITY);
    }
    @Override
    public void setUp() throws Exception {
        super.setUp();
        mSearchHelper = new SearchRecentSuggestions(getMockContext(), 
                TestProvider.AUTHORITY, TestProvider.MODE);
        checkOpenCursorCount(0);
    }
    public void testSetup() {
        assertTrue(true);
    }
    public void testOneQuery() {
        final String TEST_LINE1 = "test line 1";
        final String TEST_LINE2 = "test line 2";
        mSearchHelper.saveRecentQuery(TEST_LINE1, TEST_LINE2);
        checkOpenCursorCount(1);
        checkResultCounts(null, 1, 1, TEST_LINE1, TEST_LINE2);
        checkResultCounts(TEST_LINE1, 1, 1, TEST_LINE1, TEST_LINE2);
        checkResultCounts(TEST_LINE2, 1, 1, TEST_LINE1, TEST_LINE2);
        checkResultCounts("bad filter", 0, 0, null, null);
    }
    public void testMixedQueries() {
        final String TEST_GROUP_1 = "query ";
        final String TEST_GROUP_2 = "test ";
        final String TEST_LINE2 = "line2 ";
        final int GROUP_COUNT = 10;
        writeEntries(GROUP_COUNT, TEST_GROUP_1, TEST_LINE2);
        writeEntries(GROUP_COUNT, TEST_GROUP_2, TEST_LINE2);
        checkOpenCursorCount(2 * GROUP_COUNT);
        checkResultCounts(TEST_GROUP_1, GROUP_COUNT, GROUP_COUNT, null, null);
        checkResultCounts(TEST_GROUP_2, GROUP_COUNT, GROUP_COUNT, null, null);
        checkResultCounts(TEST_LINE2, 2 * GROUP_COUNT, 2 * GROUP_COUNT, null, null);
    }
    public void testReordering() {
        final int GROUP_1_COUNT = 10;
        final String GROUP_1_QUERY = "group1 ";
        final String GROUP_1_LINE2 = "line2 ";
        writeEntries(GROUP_1_COUNT, GROUP_1_QUERY, GROUP_1_LINE2);
        checkOpenCursorCount(GROUP_1_COUNT);
        writeDelay();
        final int GROUP_2_COUNT = 10;
        final String GROUP_2_QUERY = "group2 ";
        final String GROUP_2_LINE2 = "line2 ";
        writeEntries(GROUP_2_COUNT, GROUP_2_QUERY, GROUP_2_LINE2);
        checkOpenCursorCount(GROUP_1_COUNT + GROUP_2_COUNT);
        writeDelay();
        final int GROUP_3_COUNT = 5;
        final String GROUP_3_QUERY = GROUP_1_QUERY;
        final String GROUP_3_LINE2 = "refreshed ";
        writeEntries(GROUP_3_COUNT, GROUP_3_QUERY, GROUP_3_LINE2);
        checkOpenCursorCount(GROUP_1_COUNT + GROUP_2_COUNT);
        int newGroup1Count = GROUP_1_COUNT - GROUP_3_COUNT;
        checkResultCounts(GROUP_1_QUERY, newGroup1Count, newGroup1Count, null, GROUP_1_LINE2);
        checkResultCounts(GROUP_2_QUERY, GROUP_2_COUNT, GROUP_2_COUNT, null, null);
        checkResultCounts(GROUP_3_QUERY, GROUP_3_COUNT, GROUP_3_COUNT, null, GROUP_3_LINE2);
        Cursor c = getQueryCursor(null);
        int colQuery = c.getColumnIndexOrThrow(SearchManager.SUGGEST_COLUMN_QUERY);
        int colDisplay1 = c.getColumnIndexOrThrow(SearchManager.SUGGEST_COLUMN_TEXT_1);
        int colDisplay2 = c.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_2);
        c.moveToPosition(0);
        assertTrue("group 3 did not properly reorder to head of list",
                checkRow(c, colQuery, colDisplay1, colDisplay2, GROUP_3_QUERY, GROUP_3_LINE2));
        c.move(GROUP_3_COUNT - 1);
        assertTrue("group 3 did not properly reorder to head of list",
                checkRow(c, colQuery, colDisplay1, colDisplay2, GROUP_3_QUERY, GROUP_3_LINE2));
        c.move(1);
        assertTrue("group 2 not in expected position after reordering",
                checkRow(c, colQuery, colDisplay1, colDisplay2, GROUP_2_QUERY, GROUP_2_LINE2));
        c.move(GROUP_2_COUNT - 1);
        assertTrue("group 2 not in expected position after reordering",
                checkRow(c, colQuery, colDisplay1, colDisplay2, GROUP_2_QUERY, GROUP_2_LINE2));
        c.move(1);
        assertTrue("group 1 not in expected position after reordering",
                checkRow(c, colQuery, colDisplay1, colDisplay2, GROUP_1_QUERY, GROUP_1_LINE2));
        c.move(newGroup1Count - 1);
        assertTrue("group 1 not in expected position after reordering",
                checkRow(c, colQuery, colDisplay1, colDisplay2, GROUP_1_QUERY, GROUP_1_LINE2));
        c.close();
    }
    public void testPruning() {
        final int GROUP_1_COUNT = 50;
        final String GROUP_1_QUERY = "group1 ";
        final String GROUP_1_LINE2 = "line2 ";
        writeEntries(GROUP_1_COUNT, GROUP_1_QUERY, GROUP_1_LINE2);
        checkOpenCursorCount(GROUP_1_COUNT);
        writeDelay();
        final int GROUP_2_COUNT = 200;
        final String GROUP_2_QUERY = "group2 ";
        final String GROUP_2_LINE2 = "line2 ";
        writeEntries(GROUP_2_COUNT, GROUP_2_QUERY, GROUP_2_LINE2);
        checkOpenCursorCount(GROUP_1_COUNT + GROUP_2_COUNT);
        final int GROUP_3_COUNT = 10;
        final String GROUP_3_QUERY = "group3 ";
        final String GROUP_3_LINE2 = "line2 ";
        writeEntries(GROUP_3_COUNT, GROUP_3_QUERY, GROUP_3_LINE2);
        checkOpenCursorCount(GROUP_1_COUNT + GROUP_2_COUNT);
        int group1NewCount = GROUP_1_COUNT-GROUP_3_COUNT;
        checkResultCounts(GROUP_1_QUERY, group1NewCount, group1NewCount, null, null);
        checkResultCounts(GROUP_2_QUERY, GROUP_2_COUNT, GROUP_2_COUNT, null, null);
        checkResultCounts(GROUP_3_QUERY, GROUP_3_COUNT, GROUP_3_COUNT, null, null);
    }
    public void testClear() {
        final int GROUP_1_COUNT = 10;
        final String GROUP_1_QUERY = "group1 ";
        final String GROUP_1_LINE2 = "line2 ";
        writeEntries(GROUP_1_COUNT, GROUP_1_QUERY, GROUP_1_LINE2);
        final int GROUP_2_COUNT = 10;
        final String GROUP_2_QUERY = "group2 ";
        final String GROUP_2_LINE2 = "line2 ";
        writeEntries(GROUP_2_COUNT, GROUP_2_QUERY, GROUP_2_LINE2);
        checkOpenCursorCount(GROUP_1_COUNT + GROUP_2_COUNT);
        mSearchHelper.clearHistory();
        checkOpenCursorCount(0);
    }
    private void writeEntries(int groupCount, String line1Base, String line2Base) {
        for (int i = 0; i < groupCount; i++) {
            final String line1 = line1Base + i;
            final String line2 = line2Base + i;
            mSearchHelper.saveRecentQuery(line1, line2);
        }
    }
    private void writeDelay() {
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            fail("Interrupted sleep.");
        }
    }
    private void checkOpenCursorCount(int expectCount) {
        Cursor c = getQueryCursor(null);
        assertEquals(expectCount, c.getCount());
        c.close();
    }
    private void checkResultCounts(String queryString, int minRows, int maxRows,
            String matchDisplay1, String matchDisplay2) {
        Cursor c = getQueryCursor(queryString);
        assertNotNull(c);
        assertTrue("Insufficient rows in filtered cursor", c.getCount() >= minRows);
        int colQuery = c.getColumnIndexOrThrow(SearchManager.SUGGEST_COLUMN_QUERY);
        int colDisplay1 = c.getColumnIndexOrThrow(SearchManager.SUGGEST_COLUMN_TEXT_1);
        int colDisplay2 = c.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_2);
        int foundRows = 0;
        c.moveToFirst();
        while (!c.isAfterLast()) {
            if (checkRow(c, colQuery, colDisplay1, colDisplay2, matchDisplay1, matchDisplay2)) {
                foundRows++;
            }            
            c.moveToNext();
        }
        assertTrue(minRows <= foundRows);
        assertTrue(foundRows <= maxRows);
        c.close();
    }
    private boolean checkRow(Cursor c, int colQuery, int colDisp1, int colDisp2,
            String matchDisplay1, String matchDisplay2) {
        String query = c.getString(colQuery);
        String display1 = c.getString(colDisp1);
        String display2 = (colDisp2 >= 0) ? c.getString(colDisp2) : null;
        assertEquals(query, display1);
        boolean result = true;
        if (matchDisplay1 != null) {
            result = result && (display1 != null) && display1.startsWith(matchDisplay1);
        }
        if (matchDisplay2 != null) {
            result = result && (display2 != null) && display2.startsWith(matchDisplay2);
        }
        return result;
    }
    private Cursor getQueryCursor(String queryString) {
        ContentResolver cr = getMockContext().getContentResolver();
        String uriStr = "content:
        '/' + SearchManager.SUGGEST_URI_PATH_QUERY;
        Uri contentUri = Uri.parse(uriStr);
        String[] selArgs = new String[] {queryString};
        Cursor c = cr.query(contentUri, null, null, selArgs, null);
        assertNotNull(c);
        return c;
    }
}
