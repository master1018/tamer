@TestTargetClass(android.database.CursorJoiner.class)
public class CursorJoinerTest extends AndroidTestCase {
    private static final int TEST_ITEM_COUNT = 10;
    private static final int DEFAULT_TABLE1_VALUE_BEGINS = 1;
    private static final int DEFAULT_TABLE2_VALUE_BEGINS = 11;
    private static final int EQUAL_START = 18;
    private static final int UNIQUE_COUNT = 7;
    private static final int MAX_VALUE = 20;
    private static final int EQUAL_VALUE_COUNT = MAX_VALUE - EQUAL_START + 1;
    private static final String TABLE_NAME_1 = "test1";
    private static final String TABLE_NAME_2 = "test2";
    private static final String TABLE1_COLUMNS = " number TEXT";
    private static final String TABLE2_COLUMNS = " number TEXT, int_number INTEGER";
    private SQLiteDatabase mDatabase;
    private File mDatabaseFile;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        setupDatabase();
    }
    @Override
    protected void tearDown() throws Exception {
        mDatabase.close();
        mDatabaseFile.delete();
        super.tearDown();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "CursorJoiner",
            args = {android.database.Cursor.class, java.lang.String[].class,
                    android.database.Cursor.class, java.lang.String[].class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "iterator",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "it always throws UnsupportedOperationException.",
            method = "remove",
            args = {}
        )
    })
    public void testCursorJoinerAndIterator() {
        Cursor cursor1 = getCursor(TABLE_NAME_1, null, null);
        Cursor cursor2 = getCursor(TABLE_NAME_2, null, null);
        try {
            new CursorJoiner(cursor1, cursor1.getColumnNames(), cursor2, cursor2.getColumnNames());
            fail("CursorJoiner's constructor should throws  IllegalArgumentException here.");
        } catch (IllegalArgumentException e) {
        }
        closeCursor(cursor1);
        closeCursor(cursor2);
        String[] columnNames = new String[] { "number" };
        cursor1 = getCursor(TABLE_NAME_1, null, columnNames);
        cursor2 = getCursor(TABLE_NAME_2, null, columnNames);
        CursorJoiner cursorJoiner = new CursorJoiner(cursor1, cursor1.getColumnNames(), cursor2,
                cursor2.getColumnNames());
        try {
            cursorJoiner.remove();
            fail("remove() should throws UnsupportedOperationException here");
        } catch (UnsupportedOperationException e) {
        }
        assertEquals(TEST_ITEM_COUNT, cursor1.getCount());
        assertEquals(TEST_ITEM_COUNT, cursor2.getCount());
        for (CursorJoiner.Result joinResult : cursorJoiner) {
            switch (joinResult) {
            case LEFT:
                assertTrue(cursor1.getString(0).compareTo(cursor2.getString(0)) < 0);
                addValueIntoTable(TABLE_NAME_2, cursor1.getString(0));
                break;
            case RIGHT:
                assertTrue(cursor1.getString(0).compareTo(cursor2.getString(0)) > 0);
                addValueIntoTable(TABLE_NAME_1, cursor2.getString(0));
                break;
            case BOTH:
                assertEquals(cursor1.getString(0), cursor2.getString(0));
                deleteValueFromTable(TABLE_NAME_1, cursor1.getString(0));
                deleteValueFromTable(TABLE_NAME_2, cursor2.getString(0));
                break;
            }
        }
        cursor1.requery();
        cursor2.requery();
        assertEquals(UNIQUE_COUNT * 2, cursor1.getCount());
        assertEquals(UNIQUE_COUNT * 2, cursor2.getCount());
        cursor1.moveToFirst();
        cursor2.moveToFirst();
        for (int i = 0; i < UNIQUE_COUNT; i++) {
            assertEquals(getOrderNumberString(DEFAULT_TABLE1_VALUE_BEGINS + i, MAX_VALUE),
                    cursor1.getString(0));
            assertEquals(cursor1.getString(0), cursor2.getString(0));
            cursor1.moveToNext();
            cursor2.moveToNext();
        }
        closeCursor(cursor2);
        closeCursor(cursor1);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Fisrt UNIQUE_COUNT 'next' operations, the joiner controls cursor1 to move;" +
                    " and the second UNIQUE_COUNT 'next' ops, the joiner controlscursor2" +
                    " to move, the last EQUAL_VALUE_COUNT 'next' ops, the joiner control" +
                    " bothcursor1 and cursor2 to move.",
            method = "next",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "hasNext",
            args = {}
        )
    })
    public void testNext() {
        String[] columnNames = new String[] { "number" };
        Cursor cursor1 = getCursor(TABLE_NAME_1, null, columnNames);
        Cursor cursor2 = getCursor(TABLE_NAME_2, null, columnNames);
        assertEquals(TEST_ITEM_COUNT, cursor1.getCount());
        assertEquals(TEST_ITEM_COUNT, cursor2.getCount());
        CursorJoiner cursorJoiner = new CursorJoiner(cursor1, cursor1.getColumnNames(), cursor2,
                cursor2.getColumnNames());
        for (int i = 0; i < UNIQUE_COUNT; i++) {
            assertTrue(cursorJoiner.hasNext());
            assertEquals(Result.LEFT, cursorJoiner.next());
            assertEquals(getOrderNumberString(DEFAULT_TABLE1_VALUE_BEGINS + i, MAX_VALUE), cursor1
                    .getString(0));
            assertEquals(getOrderNumberString(DEFAULT_TABLE2_VALUE_BEGINS, MAX_VALUE), cursor2
                  .getString(0));
        }
        for (int i = 0; i < UNIQUE_COUNT; i++) {
            assertTrue(cursorJoiner.hasNext());
            assertEquals(Result.RIGHT, cursorJoiner.next());
            assertEquals(getOrderNumberString(EQUAL_START, MAX_VALUE), cursor1.getString(0));
            assertEquals(getOrderNumberString(DEFAULT_TABLE2_VALUE_BEGINS + i, MAX_VALUE), cursor2
                    .getString(0));
        }
        for (int i = 0; i < EQUAL_VALUE_COUNT; i++) {
            assertTrue(cursorJoiner.hasNext());
            assertEquals(Result.BOTH, cursorJoiner.next());
            assertEquals(getOrderNumberString(EQUAL_START + i, MAX_VALUE), cursor1.getString(0));
            assertEquals(getOrderNumberString(EQUAL_START + i, MAX_VALUE), cursor2.getString(0));
        }
        closeCursor(cursor1);
        closeCursor(cursor2);
    }
    private String getOrderNumberString(int value, int maxValue) {
        int maxLength = Integer.toString(maxValue).length();
        int basicLength = Integer.toString(value).length();
        String placeHolders = "";
        for (int i = 0; i < (maxLength - basicLength); i++) {
            placeHolders += "0";
        }
        return placeHolders + Integer.toString(value);
    }
    private void initializeTables() {
        addValuesIntoTable(TABLE_NAME_1, DEFAULT_TABLE1_VALUE_BEGINS,
                DEFAULT_TABLE1_VALUE_BEGINS + UNIQUE_COUNT - 1);
        addValuesIntoTable(TABLE_NAME_1, DEFAULT_TABLE2_VALUE_BEGINS + UNIQUE_COUNT, MAX_VALUE);
        addValuesIntoTable(TABLE_NAME_2, DEFAULT_TABLE2_VALUE_BEGINS,
                DEFAULT_TABLE2_VALUE_BEGINS + UNIQUE_COUNT - 1);
        addValuesIntoTable(TABLE_NAME_2, DEFAULT_TABLE2_VALUE_BEGINS + UNIQUE_COUNT, MAX_VALUE);
    }
    private void setupDatabase() {
        File dbDir = getContext().getDir("tests", Context.MODE_PRIVATE);
        mDatabaseFile = new File(dbDir, "database_test.db");
        if (mDatabaseFile.exists()) {
            mDatabaseFile.delete();
        }
        mDatabase = SQLiteDatabase.openOrCreateDatabase(mDatabaseFile.getPath(), null);
        assertNotNull(mDatabaseFile);
        createTable(TABLE_NAME_1, TABLE1_COLUMNS);
        createTable(TABLE_NAME_2, TABLE2_COLUMNS);
        initializeTables();
    }
    private void closeCursor(Cursor cursor) {
        if (null != cursor) {
            cursor.close();
            cursor = null;
        }
    }
    private void createTable(String tableName, String columnNames) {
        String sql = "Create TABLE " + tableName + " (_id INTEGER PRIMARY KEY, " + columnNames
                + " );";
        mDatabase.execSQL(sql);
    }
    private void addValuesIntoTable(String tableName, int start, int end) {
        for (int i = start; i <= end; i++) {
            mDatabase.execSQL("INSERT INTO " + tableName + "(number) VALUES ('"
                    + getOrderNumberString(i, MAX_VALUE) + "');");
        }
    }
    private void addValueIntoTable(String tableName, String value) {
        mDatabase.execSQL("INSERT INTO " + tableName + "(number) VALUES ('" + value + "');");
    }
    private void deleteValueFromTable(String tableName, String value) {
        mDatabase.execSQL("DELETE FROM " + tableName + " WHERE number = '" + value + "';");
    }
    private Cursor getCursor(String tableName, String selection, String[] columnNames) {
        return mDatabase.query(tableName, columnNames, selection, null, null, null, "number");
    }
}
