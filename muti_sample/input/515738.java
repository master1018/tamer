public class DatabaseLocaleTest extends TestCase {
    private SQLiteDatabase mDatabase;
    private static final String[] STRINGS = {
        "c\u00f4t\u00e9",
        "cote",
        "c\u00f4te",
        "cot\u00e9",
        "boy",
        "dog",
        "COTE",
    };
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mDatabase = SQLiteDatabase.create(null);
        mDatabase.execSQL(
                "CREATE TABLE test (id INTEGER PRIMARY KEY, data TEXT COLLATE LOCALIZED);");
    }
    private void insertStrings() {
        for (String s : STRINGS) {
            mDatabase.execSQL("INSERT INTO test (data) VALUES('" + s + "');");
        }
    }
    @Override
    protected void tearDown() throws Exception {
        mDatabase.close();
        super.tearDown();
    }
    private String[] query(String sql) {
        Log.i("LocaleTest", "Querying: " + sql);
        Cursor c = mDatabase.rawQuery(sql, null);
        assertNotNull(c);
        ArrayList<String> items = new ArrayList<String>();
        while (c.moveToNext()) {
            items.add(c.getString(0));
            Log.i("LocaleTest", "...." + c.getString(0));
        }
        String[] result = items.toArray(new String[items.size()]);
        assertEquals(STRINGS.length, result.length);
        c.close();
        return result;
    }
    @MediumTest
    public void testLocaleInsertOrder() throws Exception {
        insertStrings();
        String[] results = query("SELECT data FROM test");
        MoreAsserts.assertEquals(STRINGS, results);
    }
    @MediumTest
    public void testLocaleenUS() throws Exception {
        insertStrings();
        Log.i("LocaleTest", "about to call setLocale en_US");
        mDatabase.setLocale(new Locale("en", "US"));
        String[] results;
        results = query("SELECT data FROM test ORDER BY data COLLATE LOCALIZED ASC");
        MoreAsserts.assertEquals(results, new String[] {
                STRINGS[4],  
                STRINGS[0],  
                STRINGS[1],
                STRINGS[2],
                STRINGS[3],
                STRINGS[6],  
                STRINGS[5],  
        });
    }
    @SmallTest
    public void testHoge() throws Exception {
        Cursor cursor = null;
        try {
            String expectedString = new String(new int[] {0xFE000}, 0, 1);
            mDatabase.execSQL("INSERT INTO test(id, data) VALUES(1, '" + expectedString + "')");
            cursor = mDatabase.rawQuery("SELECT data FROM test WHERE id = 1", null);
            assertNotNull(cursor);
            assertTrue(cursor.moveToFirst());
            String actualString = cursor.getString(0);
            assertEquals(expectedString.length(), actualString.length());
            for (int i = 0; i < expectedString.length(); i++) {
                assertEquals((int)expectedString.charAt(i), (int)actualString.charAt(i));
            }
            assertEquals(expectedString, actualString);
        } finally {
            if (cursor != null) cursor.close();
        }
    }
}
