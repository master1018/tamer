public class DatabaseStressTest extends AndroidTestCase {
    private static final String TAG = "DatabaseStressTest";    
    private static final int CURRENT_DATABASE_VERSION = 1;
    private SQLiteDatabase mDatabase;
    private File mDatabaseFile;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Context c = getContext();
        mDatabaseFile = c.getDatabasePath("database_test.db");
        if (mDatabaseFile.exists()) {
            mDatabaseFile.delete();
        }
        mDatabase = c.openOrCreateDatabase("database_test.db", 0, null);            
        assertNotNull(mDatabase);
        mDatabase.setVersion(CURRENT_DATABASE_VERSION);
        mDatabase.execSQL("CREATE TABLE IF NOT EXISTS test (_id INTEGER PRIMARY KEY, data TEXT);");
    }
    @Override
    protected void tearDown() throws Exception {
        mDatabase.close();
        mDatabaseFile.delete();
        super.tearDown();
    }
    public void testSingleThreadInsertDelete() {        
        int i = 0;
        char[] ch = new char[100000];
        String str = new String(ch);
        String[] strArr = new String[1];
        strArr[0] = str;
        for (; i < 10000; ++i) {
            try {
                mDatabase.execSQL("INSERT INTO test (data) VALUES (?)", strArr);
                mDatabase.execSQL("delete from test;");
            } catch (Exception e) {
                Log.e(TAG, "exception " + e.getMessage());                
            }
        }        
    }
    public void testOutOfSpace() {
        int i = 0;
        char[] ch = new char[100000];
        String str = new String(ch);
        String[] strArr = new String[1];
        strArr[0] = str;
        for (; i < 10000; ++i) {
            try {
                mDatabase.execSQL("INSERT INTO test (data) VALUES (?)", strArr);
            } catch (Exception e) {
                Log.e(TAG, "exception " + e.getMessage());                
            }
        }        
    }
}
