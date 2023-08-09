@TestTargetClass(SQLite.Exception.class)
public class ExceptionTest extends SQLiteTest {
    private Database db = null;
    public void setUp() throws java.lang.Exception {
        super.setUp();
        db = new Database();
    }
    public void tearDown() {
        super.tearDown();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "constructor test",
        method = "Exception",
        args = {java.lang.String.class}
    )
    public void testException() {
        try {
            db.open(dbFile.getName(), 0);
        } catch (Exception e) {
            assertNotNull(e);
            assertNotNull(e.getMessage());
        }
    }
}
