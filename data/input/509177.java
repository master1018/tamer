public class DatabaseCursorTest extends AndroidTestCase implements PerformanceTestCase {
    private static final String sString1 = "this is a test";
    private static final String sString2 = "and yet another test";
    private static final String sString3 = "this string is a little longer, but still a test";
    private static final int CURRENT_DATABASE_VERSION = 42;
    private SQLiteDatabase mDatabase;
    private File mDatabaseFile;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
	File dbDir = getContext().getDir("tests", Context.MODE_PRIVATE);
	mDatabaseFile = new File(dbDir, "database_test.db");
        if (mDatabaseFile.exists()) {
            mDatabaseFile.delete();
        }
        mDatabase = SQLiteDatabase.openOrCreateDatabase(mDatabaseFile.getPath(), null);
        assertNotNull(mDatabase);
        mDatabase.setVersion(CURRENT_DATABASE_VERSION);
    }
    @Override
    protected void tearDown() throws Exception {
        mDatabase.close();
        mDatabaseFile.delete();
        super.tearDown();
    }
    public boolean isPerformanceOnly() {
        return false;
    }
    public int startPerformance(Intermediates intermediates) {
        return 1;
    }
    private void populateDefaultTable() {
        mDatabase.execSQL("CREATE TABLE test (_id INTEGER PRIMARY KEY, data TEXT);");
        mDatabase.execSQL("INSERT INTO test (data) VALUES ('" + sString1 + "');");
        mDatabase.execSQL("INSERT INTO test (data) VALUES ('" + sString2 + "');");
        mDatabase.execSQL("INSERT INTO test (data) VALUES ('" + sString3 + "');");
    }
    @MediumTest
    public void testCursorUpdate() {
        mDatabase.execSQL(
            "CREATE TABLE test (_id INTEGER PRIMARY KEY, d INTEGER, s INTEGER);");
        for(int i = 0; i < 20; i++) {
            mDatabase.execSQL("INSERT INTO test (d, s) VALUES (" + i + 
                "," + i%2 + ");");
        }
        Cursor c = mDatabase.query("test", null, "s = 0", null, null, null, null);
        int dCol = c.getColumnIndexOrThrow("d");
        int sCol = c.getColumnIndexOrThrow("s");
        int count = 0;
        while (c.moveToNext()) {
            assertTrue(c.updateInt(dCol, 3));
            count++;
        }
        assertEquals(10, count);
        assertTrue(c.commitUpdates());
        assertTrue(c.requery());
        count = 0;
        while (c.moveToNext()) {
            assertEquals(3, c.getInt(dCol));
            count++;
        }
        assertEquals(10, count);
        assertTrue(c.moveToFirst());
        assertTrue(c.deleteRow());
        assertEquals(9, c.getCount());
        c.close();
    }
    @MediumTest
    public void testBlob() throws Exception {
        mDatabase.execSQL(
            "CREATE TABLE test (_id INTEGER PRIMARY KEY, s TEXT, d REAL, l INTEGER, b BLOB);");
        Object[] args = new Object[4];
        byte[] blob = new byte[1000];
        byte value = 99;
        Arrays.fill(blob, value);        
        args[3] = blob;
        String s = new String("text");        
        args[0] = s;
        Double d = 99.9;
        args[1] = d;
        Long l = (long)1000;
        args[2] = l;
        String sql = "INSERT INTO test (s, d, l, b) VALUES (?,?,?,?)";
        mDatabase.execSQL(sql, args);
        Cursor c = mDatabase.query("test", null, null, null, null, null, null);        
        c.moveToNext();
        ContentValues cv = new ContentValues();
        DatabaseUtils.cursorRowToContentValues(c, cv);
        int bCol = c.getColumnIndexOrThrow("b");
        int sCol = c.getColumnIndexOrThrow("s");
        int dCol = c.getColumnIndexOrThrow("d");
        int lCol = c.getColumnIndexOrThrow("l");
        byte[] cBlob =  c.getBlob(bCol);
        assertTrue(Arrays.equals(blob, cBlob));
        assertEquals(s, c.getString(sCol));
        assertEquals((double)d, c.getDouble(dCol));
        assertEquals((long)l, c.getLong(lCol));
        byte[] newblob = new byte[1000];
        value = 98;
        Arrays.fill(blob, value);        
        c.updateBlob(bCol, newblob);
        cBlob =  c.getBlob(bCol);
        assertTrue(Arrays.equals(newblob, cBlob));
        assertTrue(c.commitUpdates());
        assertTrue(c.requery());
        c.moveToNext();
        cBlob =  c.getBlob(bCol);
        assertTrue(Arrays.equals(newblob, cBlob));        
        c.close();
    }
    @MediumTest
    public void testRealColumns() throws Exception {
        mDatabase.execSQL("CREATE TABLE test (_id INTEGER PRIMARY KEY, data REAL);");
        ContentValues values = new ContentValues();
        values.put("data", 42.11);
        long id = mDatabase.insert("test", "data", values);
        assertTrue(id > 0);
        Cursor c = mDatabase.rawQuery("SELECT data FROM test", null);
        assertNotNull(c);
        assertTrue(c.moveToFirst());
        assertEquals(42.11, c.getDouble(0));
        c.close();
    }
    @MediumTest
    public void testCursor1() throws Exception {
        populateDefaultTable();
        Cursor c = mDatabase.query("test", null, null, null, null, null, null);
        int dataColumn = c.getColumnIndexOrThrow("data");
        int dataColumn2 = c.getColumnIndexOrThrow("junk.data");
        assertEquals(dataColumn, dataColumn2);
        assertSame(3, c.getCount());
        assertTrue(c.isBeforeFirst());
        try {
            c.getInt(0);
            fail("CursorIndexOutOfBoundsException expected");
        } catch (CursorIndexOutOfBoundsException ex) {
        }
        c.moveToNext();
        assertEquals(1, c.getInt(0));
        String s = c.getString(dataColumn);
        assertEquals(sString1, s);
        c.moveToNext();
        s = c.getString(dataColumn);
        assertEquals(sString2, s);
        c.moveToNext();
        s = c.getString(dataColumn);
        assertEquals(sString3, s);
        c.moveToPosition(-1);
        c.moveToNext();
        s = c.getString(dataColumn);
        assertEquals(sString1, s);
        c.moveToPosition(2);
        s = c.getString(dataColumn);
        assertEquals(sString3, s);
        int i;
        for (c.moveToFirst(), i = 0; !c.isAfterLast(); c.moveToNext(), i++) {
            c.getInt(0);
        }
        assertEquals(3, i);
        try {
            c.getInt(0);
            fail("CursorIndexOutOfBoundsException expected");
        } catch (CursorIndexOutOfBoundsException ex) {
        }
        c.close();
    }
    @MediumTest
    public void testCursor2() throws Exception {
        populateDefaultTable();
        Cursor c = mDatabase.query("test", null, "_id > 1000", null, null, null, null);
        assertEquals(0, c.getCount());
        assertTrue(c.isBeforeFirst());
        try {
            c.getInt(0);
            fail("CursorIndexOutOfBoundsException expected");
        } catch (CursorIndexOutOfBoundsException ex) {
        }
        int i;
        for (c.moveToFirst(), i = 0; !c.isAfterLast(); c.moveToNext(), i++) {
            c.getInt(0);
        }
        assertEquals(0, i);
        try {
            c.getInt(0);
            fail("CursorIndexOutOfBoundsException expected");
        } catch (CursorIndexOutOfBoundsException ex) {
        }
        c.close();
    }
    @MediumTest
    public void testLargeField() throws Exception {
        mDatabase.execSQL("CREATE TABLE test (_id INTEGER PRIMARY KEY, data TEXT);");
        StringBuilder sql = new StringBuilder(2100);
        sql.append("INSERT INTO test (data) VALUES ('");
        Random random = new Random(System.currentTimeMillis());
        StringBuilder randomString = new StringBuilder(1979);
        for (int i = 0; i < 1979; i++) {
            randomString.append((random.nextInt() & 0xf) % 10);
        }
        sql.append(randomString);
        sql.append("');");
        mDatabase.execSQL(sql.toString());
        Cursor c = mDatabase.query("test", null, null, null, null, null, null);
        assertNotNull(c);
        assertEquals(1, c.getCount());
        assertTrue(c.moveToFirst());
        assertEquals(0, c.getPosition());
        String largeString = c.getString(c.getColumnIndexOrThrow("data"));
        assertNotNull(largeString);
        assertEquals(randomString.toString(), largeString);
        c.close();
    }
    class TestObserver extends DataSetObserver {
        int total;
        SQLiteCursor c;
        boolean quit = false;
        public TestObserver(int total_, SQLiteCursor cursor) {
            c = cursor;
            total = total_;
        }
        @Override
        public void onChanged() {
            int count = c.getCount();
            if (total == count) {
                int i = 0;
                while (c.moveToNext()) {
                    assertEquals(i, c.getInt(1));
                    i++;
                }
                assertEquals(count, i);
                quit = true;
                Looper.myLooper().quit();
            }
        }
        @Override
        public void onInvalidated() {
        }
    }
    @Suppress
    public void testLoadingThreadDelayRegisterData() throws Exception {
        mDatabase.execSQL("CREATE TABLE test (_id INTEGER PRIMARY KEY, data INT);");
        final int count = 505; 
        String sql = "INSERT INTO test (data) VALUES (?);";
        SQLiteStatement s = mDatabase.compileStatement(sql);
        for (int i = 0; i < count; i++) {
            s.bindLong(1, i);
            s.execute();
        }
        int maxRead = 500;
        int initialRead = 5;
        SQLiteCursor c = (SQLiteCursor)mDatabase.rawQuery("select * from test;",
                null, initialRead, maxRead);
        TestObserver observer = new TestObserver(count, c);
        c.getCount();
        c.registerDataSetObserver(observer);
        if (!observer.quit) {
            Looper.loop();
        }
        c.close();
    }
    @BrokenTest("Consistently times out")
    @Suppress
    public void testLoadingThread() throws Exception {
        mDatabase.execSQL("CREATE TABLE test (_id INTEGER PRIMARY KEY, data INT);");
        final int count = 50000; 
        String sql = "INSERT INTO test (data) VALUES (?);";
        SQLiteStatement s = mDatabase.compileStatement(sql);
        for (int i = 0; i < count; i++) {
            s.bindLong(1, i);
            s.execute();
        }
        int maxRead = 1000;
        int initialRead = 5;
        SQLiteCursor c = (SQLiteCursor)mDatabase.rawQuery("select * from test;",
                null, initialRead, maxRead);
        TestObserver observer = new TestObserver(count, c);
        c.registerDataSetObserver(observer);
        c.getCount();
        Looper.loop();
        c.close();
    } 
    @BrokenTest("Consistently times out")
    @Suppress
    public void testLoadingThreadClose() throws Exception {
        mDatabase.execSQL("CREATE TABLE test (_id INTEGER PRIMARY KEY, data INT);");
        final int count = 1000; 
        String sql = "INSERT INTO test (data) VALUES (?);";
        SQLiteStatement s = mDatabase.compileStatement(sql);
        for (int i = 0; i < count; i++) {
            s.bindLong(1, i);
            s.execute();
        }
        int maxRead = 11;
        int initialRead = 5;
        SQLiteCursor c = (SQLiteCursor)mDatabase.rawQuery("select * from test;",
                null, initialRead, maxRead);
        TestObserver observer = new TestObserver(count, c);
        c.registerDataSetObserver(observer);
        c.getCount();       
        c.close();
    }
    @LargeTest
    public void testLoadingThreadDeactivate() throws Exception {
        mDatabase.execSQL("CREATE TABLE test (_id INTEGER PRIMARY KEY, data INT);");
        final int count = 1000; 
        String sql = "INSERT INTO test (data) VALUES (?);";
        SQLiteStatement s = mDatabase.compileStatement(sql);
        for (int i = 0; i < count; i++) {
            s.bindLong(1, i);
            s.execute();
        }
        int maxRead = 11;
        int initialRead = 5;
        SQLiteCursor c = (SQLiteCursor)mDatabase.rawQuery("select * from test;",
                null, initialRead, maxRead);
        TestObserver observer = new TestObserver(count, c);
        c.registerDataSetObserver(observer);
        c.getCount();       
        c.deactivate();
        c.close();
    }
    @LargeTest
    public void testManyRowsLong() throws Exception {
        mDatabase.execSQL("CREATE TABLE test (_id INTEGER PRIMARY KEY, data INT);");
        final int count = 36799; 
        mDatabase.execSQL("BEGIN Transaction;");
        for (int i = 0; i < count; i++) {
            mDatabase.execSQL("INSERT INTO test (data) VALUES (" + i + ");");
        }
        mDatabase.execSQL("COMMIT;");
        Cursor c = mDatabase.query("test", new String[]{"data"}, null, null, null, null, null);
        assertNotNull(c);
        int i = 0;
        while (c.moveToNext()) {
            assertEquals(i, c.getInt(0));
            i++;
        }
        assertEquals(count, i);
        assertEquals(count, c.getCount());
        Log.d("testManyRows", "count " + Integer.toString(i));
        c.close();
    }
    @LargeTest
    public void testManyRowsTxt() throws Exception {
        mDatabase.execSQL("CREATE TABLE test (_id INTEGER PRIMARY KEY, data TEXT);");
        StringBuilder sql = new StringBuilder(2100);
        sql.append("INSERT INTO test (data) VALUES ('");
        Random random = new Random(System.currentTimeMillis());
        StringBuilder randomString = new StringBuilder(1979);
        for (int i = 0; i < 1979; i++) {
            randomString.append((random.nextInt() & 0xf) % 10);
        }
        sql.append(randomString);
        sql.append("');");
        final int count = 600; 
        mDatabase.execSQL("BEGIN Transaction;");
        for (int i = 0; i < count; i++) {
            mDatabase.execSQL(sql.toString());
        }
        mDatabase.execSQL("COMMIT;");
        Cursor c = mDatabase.query("test", new String[]{"data"}, null, null, null, null, null);
        assertNotNull(c);
        int i = 0;
        while (c.moveToNext()) {
            assertEquals(randomString.toString(), c.getString(0));
            i++;
        }
        assertEquals(count, i);
        assertEquals(count, c.getCount());
        c.close();
    }
    @LargeTest
    public void testManyRowsTxtLong() throws Exception {
        mDatabase.execSQL("CREATE TABLE test (_id INTEGER PRIMARY KEY, txt TEXT, data INT);");
        Random random = new Random(System.currentTimeMillis());
        StringBuilder randomString = new StringBuilder(1979);
        for (int i = 0; i < 1979; i++) {
            randomString.append((random.nextInt() & 0xf) % 10);
        }
        final int count = 600;
        mDatabase.execSQL("BEGIN Transaction;");
        for (int i = 0; i < count; i++) {
            StringBuilder sql = new StringBuilder(2100);
            sql.append("INSERT INTO test (txt, data) VALUES ('");
            sql.append(randomString);
            sql.append("','");
            sql.append(i);
            sql.append("');");
            mDatabase.execSQL(sql.toString());
        }
        mDatabase.execSQL("COMMIT;");
        Cursor c = mDatabase.query("test", new String[]{"txt", "data"}, null, null, null, null, null);
        assertNotNull(c);
        int i = 0;
        while (c.moveToNext()) {
            assertEquals(randomString.toString(), c.getString(0));
            assertEquals(i, c.getInt(1));
            i++;
        }
        assertEquals(count, i);
        assertEquals(count, c.getCount());
        c.close();
    }
    @MediumTest
    public void testRequery() throws Exception {
        populateDefaultTable();
        Cursor c = mDatabase.rawQuery("SELECT * FROM test", null);
        assertNotNull(c);
        assertEquals(3, c.getCount());
        c.deactivate();
        c.requery();
        assertEquals(3, c.getCount());
        c.close();
    }
    @MediumTest
    public void testRequeryWithSelection() throws Exception {
        populateDefaultTable();
        Cursor c = mDatabase.rawQuery("SELECT data FROM test WHERE data = '" + sString1 + "'",
                null);
        assertNotNull(c);
        assertEquals(1, c.getCount());
        assertTrue(c.moveToFirst());
        assertEquals(sString1, c.getString(0));
        c.deactivate();
        c.requery();
        assertEquals(1, c.getCount());
        assertTrue(c.moveToFirst());
        assertEquals(sString1, c.getString(0));
        c.close();
    }
    @MediumTest
    public void testRequeryWithSelectionArgs() throws Exception {
        populateDefaultTable();
        Cursor c = mDatabase.rawQuery("SELECT data FROM test WHERE data = ?",
                new String[]{sString1});
        assertNotNull(c);
        assertEquals(1, c.getCount());
        assertTrue(c.moveToFirst());
        assertEquals(sString1, c.getString(0));
        c.deactivate();
        c.requery();
        assertEquals(1, c.getCount());
        assertTrue(c.moveToFirst());
        assertEquals(sString1, c.getString(0));
        c.close();
    }
    @MediumTest
    public void testRequeryWithAlteredSelectionArgs() throws Exception {
        populateDefaultTable();
        SQLiteDatabase.CursorFactory factory = new SQLiteDatabase.CursorFactory() {
            public Cursor newCursor(
                    SQLiteDatabase db, SQLiteCursorDriver masterQuery, String editTable,
                    SQLiteQuery query) {
                return new SQLiteCursor(db, masterQuery, editTable, query) {
                    @Override
                    public boolean requery() {
                        setSelectionArguments(new String[]{"2"});
                        return super.requery();
                    }
                };
            }
        };
        Cursor c = mDatabase.rawQueryWithFactory(
                factory, "SELECT data FROM test WHERE _id <= ?", new String[]{"1"},
                null);
        assertNotNull(c);
        assertEquals(1, c.getCount());
        assertTrue(c.moveToFirst());
        assertEquals(sString1, c.getString(0));
        c.requery();
        assertEquals(2, c.getCount());
        assertTrue(c.moveToFirst());
        assertEquals(sString1, c.getString(0));
        assertTrue(c.moveToNext());
        assertEquals(sString2, c.getString(0));
        c.deactivate();
        c.requery();
    }
}
