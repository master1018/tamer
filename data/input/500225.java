public class SQLiteGeneralTest extends AndroidTestCase {
    private SQLiteDatabase mDatabase;
    private File mDatabaseFile;
    Boolean exceptionRecvd = false;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        exceptionRecvd = false;
        File dbDir = getContext().getDir(this.getClass().getName(), Context.MODE_PRIVATE);
        mDatabaseFile = new File(dbDir, "database_test.db");
        if (mDatabaseFile.exists()) {
            mDatabaseFile.delete();
        }
        mDatabase = SQLiteDatabase.openOrCreateDatabase(mDatabaseFile.getPath(), null);
        assertNotNull(mDatabase);
    }
    @Override
    protected void tearDown() throws Exception {
        mDatabase.close();
        mDatabaseFile.delete();
        super.tearDown();
    }
    @LargeTest
    public void testUseOfSameSqlStatementBy2Threads() throws Exception {
        mDatabase.execSQL("CREATE TABLE test_pstmt (i INTEGER PRIMARY KEY, j text);");
        final String stmt = "SELECT * FROM test_pstmt WHERE i = ?";
        class RunStmtThread extends Thread {
            private static final int N = 1000;
            @Override public void run() {
                int i = 0;
                try {
                    for (i = 0; i < N; i++) {
                        SQLiteStatement s1 = mDatabase.compileStatement(stmt);
                        s1.bindLong(1, i);
                        s1.execute();
                        s1.close();
                    }
                } catch (SQLiteException e) {
                    fail("SQLiteException: " + e.getMessage());
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                    fail("random unexpected exception: " + e.getMessage());
                    return;
                }
            }
        }
        RunStmtThread t1 = new RunStmtThread();
        t1.start();
        RunStmtThread t2 = new RunStmtThread();
        t2.start();
        while (t1.isAlive() || t2.isAlive()) {
            Thread.sleep(1000);
        }
    }
    @FlakyTest
    public void testUseOfSamePreparedStatementBy2Threads() throws Exception {
        mDatabase.execSQL("CREATE TABLE test_pstmt (i INTEGER PRIMARY KEY, j text);");
        final String stmt = "SELECT * FROM test_pstmt WHERE i = ?";
        final SQLiteStatement s1 = mDatabase.compileStatement(stmt);
        class RunStmtThread extends Thread {
            private static final int N = 1000;
            @Override public void run() {
                int i = 0;
                try {
                    for (i = 0; i < N; i++) {
                        s1.bindLong(1, i);
                        s1.execute();
                    }
                } catch (SQLiteException e) {
                    assertTrue(e.getMessage().contains("library routine called out of sequence:"));
                    exceptionRecvd = true;
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                    fail("random unexpected exception: " + e.getMessage());
                    return;
                }
            }
        }
        RunStmtThread t1 = new RunStmtThread();
        t1.start();
        RunStmtThread t2 = new RunStmtThread();
        t2.start();
        while (t1.isAlive() || t2.isAlive()) {
            Thread.sleep(1000);
        }
        assertTrue(exceptionRecvd);
    }
}
