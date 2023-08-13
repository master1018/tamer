@TestTargetClass(android.database.sqlite.SQLiteException.class)
public class SQLiteExceptionTest extends AndroidTestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor",
            method = "SQLiteException",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor",
            method = "SQLiteException",
            args = {java.lang.String.class}
        )
    })
    public void testConstructor() {
        new SQLiteException();
        new SQLiteException("error");
    }
}
