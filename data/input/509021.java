@TestTargetClass(android.database.sqlite.SQLiteAbortException.class)
public class SQLiteAbortExceptionTest extends AndroidTestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructors",
            method = "SQLiteAbortException",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructors",
            method = "SQLiteAbortException",
            args = {java.lang.String.class}
        )
    })
    public void testConstructor() {
        new SQLiteAbortException();
        new SQLiteAbortException("error");
    }
}
