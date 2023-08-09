public class BrowserProviderTests extends AndroidTestCase {
    private ArrayList<Uri> mDeleteUris;
    @Override
    protected void setUp() throws Exception {
        mDeleteUris = new ArrayList<Uri>();
        super.setUp();
    }
    @Override
    protected void tearDown() throws Exception {
        for (Uri uri : mDeleteUris) {
            deleteUri(uri);
        }
        super.tearDown();
    }
    public void testHasDefaultBookmarks() {
        Cursor c = getBookmarksSuggest("");
        try {
            assertTrue("No default bookmarks", c.getCount() > 0);
        } finally {
            c.close();
        }
    }
    public void testPartialFirstTitleWord() {
        assertInsertQuery("http:
    }
    public void testFullFirstTitleWord() {
        assertInsertQuery("http:
    }
    public void testFullFirstTitleWordPartialSecond() {
        assertInsertQuery("http:
    }
    public void testFullTitle() {
        assertInsertQuery("http:
    }
    public void testFullTitleJapanese() {
        String title = "\u30ae\u30e3\u30e9\u30ea\u30fc\u30fcGoogle\u691c\u7d22";
        assertInsertQuery("http:
    }
    public void testPartialTitleJapanese() {
        String title = "\u30ae\u30e3\u30e9\u30ea\u30fc\u30fcGoogle\u691c\u7d22";
        String query = "\u30ae\u30e3\u30e9\u30ea\u30fc";
        assertInsertQuery("http:
    }
    public void testSoundmarkTitleJapanese() {
        String title = "\u30ae\u30e3\u30e9\u30ea\u30fc\u30fcGoogle\u691c\u7d22";
        String query = "\u30ad\u30e3\u30e9\u30ea\u30fc";
        assertInsertQuery("http:
    }
    private void assertInsertQuery(String url, String title, String query) {
        addBookmark(url, title);
        assertQueryReturns(url, title, query);
    }
    private void assertQueryReturns(String url, String title, String query) {
        Cursor c = getBookmarksSuggest(query);
        try {
            assertTrue(title + " not matched by " + query, c.getCount() > 0);
            assertTrue("More than one result for " + query, c.getCount() == 1);
            while (c.moveToNext()) {
                String text1 = getCol(c, SearchManager.SUGGEST_COLUMN_TEXT_1);
                assertNotNull(text1);
                assertEquals("Bad title", title, text1);
                String text2 = getCol(c, SearchManager.SUGGEST_COLUMN_TEXT_2);
                assertNotNull(text2);
                String data = getCol(c, SearchManager.SUGGEST_COLUMN_INTENT_DATA);
                assertNotNull(data);
                assertEquals("Bad URL", url, data);
            }
        } finally {
            c.close();
        }
    }
    private Cursor getBookmarksSuggest(String query) {
        Uri suggestUri = Uri.parse("content:
        String[] selectionArgs = { query };
        Cursor c = getContext().getContentResolver().query(suggestUri, null, "url LIKE ?",
                selectionArgs, null);
        assertNotNull(c);
        return c;
    }
    private void addBookmark(String url, String title) {
        Uri uri = insertBookmark(url, title);
        assertNotNull(uri);
        assertFalse(android.provider.Browser.BOOKMARKS_URI.equals(uri));
        mDeleteUris.add(uri);
    }
    private Uri insertBookmark(String url, String title) {
        ContentValues values = new ContentValues();
        values.put("title", title);
        values.put("url", url);
        values.put("visits", 0);
        values.put("date", 0);
        values.put("created", 0);
        values.put("bookmark", 1);
        return getContext().getContentResolver().insert(android.provider.Browser.BOOKMARKS_URI,
                values);
    }
    private void deleteUri(Uri uri) {
        int count = getContext().getContentResolver().delete(uri, null, null);
        assertEquals("Failed to delete " + uri, 1, count);
    }
    private static String getCol(Cursor c, String name) {
        int col = c.getColumnIndex(name);
        String msg = "Column " + name + " not found, columns: "
                + Arrays.toString(c.getColumnNames());
        assertTrue(msg, col >= 0);
        return c.getString(col);
    }
}
