@TestTargetClass(SQLiteOpenHelper.class)
public class SQLiteOpenHelperTest extends AndroidTestCase {
    private static final String TEST_DATABASE_NAME = "database_test.db";
    private static final int TEST_VERSION = 1;
    private static final int TEST_ILLEGAL_VERSION = 0;
    private MockOpenHelper mOpenHelper;
    private SQLiteDatabase.CursorFactory mFactory = new SQLiteDatabase.CursorFactory() {
        public Cursor newCursor(SQLiteDatabase db, SQLiteCursorDriver masterQuery,
                String editTable, SQLiteQuery query) {
            return new MockCursor(db, masterQuery, editTable, query);
        }
    };
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mOpenHelper = getOpenHelper();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "SQLiteOpenHelper",
        args = {android.content.Context.class, java.lang.String.class,
                android.database.sqlite.SQLiteDatabase.CursorFactory.class, int.class}
    )
    public void testConstructor() {
        new MockOpenHelper(mContext, TEST_DATABASE_NAME, mFactory, TEST_VERSION);
        try {
            new MockOpenHelper(mContext, TEST_DATABASE_NAME, mFactory, TEST_ILLEGAL_VERSION);
            fail("Constructor of SQLiteOpenHelp should throws a IllegalArgumentException here.");
        } catch (IllegalArgumentException e) {
        }
        new MockOpenHelper(mContext, TEST_DATABASE_NAME, null, TEST_VERSION);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onOpen",
            args = {android.database.sqlite.SQLiteDatabase.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getReadableDatabase",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getWritableDatabase",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "close",
            args = {}
        )
    })
    public void testGetDatabase() {
        SQLiteDatabase database = null;
        assertFalse(mOpenHelper.hasCalledOnOpen());
        database = mOpenHelper.getReadableDatabase();
        assertNotNull(database);
        assertTrue(database.isOpen());
        assertTrue(mOpenHelper.hasCalledOnOpen());
        mOpenHelper.resetStatus();
        assertFalse(mOpenHelper.hasCalledOnOpen());
        SQLiteDatabase database2 = mOpenHelper.getWritableDatabase();
        assertSame(database, database2);
        assertTrue(database.isOpen());
        assertFalse(mOpenHelper.hasCalledOnOpen());
        mOpenHelper.close();
        assertFalse(database.isOpen());
        mOpenHelper.resetStatus();
        assertFalse(mOpenHelper.hasCalledOnOpen());
        SQLiteDatabase database3 = mOpenHelper.getWritableDatabase();
        assertNotNull(database);
        assertNotSame(database, database3);
        assertTrue(mOpenHelper.hasCalledOnOpen());
        assertTrue(database3.isOpen());
        mOpenHelper.close();
        assertFalse(database3.isOpen());
    }
    private MockOpenHelper getOpenHelper() {
        return new MockOpenHelper(mContext, TEST_DATABASE_NAME, mFactory, TEST_VERSION);
    }
    private class MockOpenHelper extends SQLiteOpenHelper {
        private boolean mHasCalledOnOpen = false;
        public MockOpenHelper(Context context, String name, CursorFactory factory, int version) {
            super(context, name, factory, version);
        }
        @Override
        public void onCreate(SQLiteDatabase db) {
        }
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }
        @Override
        public void onOpen(SQLiteDatabase db) {
            mHasCalledOnOpen = true;
        }
        public boolean hasCalledOnOpen() {
            return mHasCalledOnOpen;
        }
        public void resetStatus() {
            mHasCalledOnOpen = false;
        }
    }
    private class MockCursor extends SQLiteCursor {
        public MockCursor(SQLiteDatabase db, SQLiteCursorDriver driver, String editTable,
                SQLiteQuery query) {
            super(db, driver, editTable, query);
        }
    }
}
