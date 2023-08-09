@TestTargetClass(android.database.sqlite.SQLiteFullException.class)
public class SQLiteFullExceptionTest extends AndroidTestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor",
            method = "SQLiteFullException",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor",
            method = "SQLiteFullException",
            args = {java.lang.String.class}
        )
    })
    public void testConstructor() {
        new SQLiteFullException();
        new SQLiteFullException("error");
    }
}
