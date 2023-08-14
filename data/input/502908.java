@TestTargetClass(android.database.sqlite.SQLiteDoneException.class)
public class SQLiteDoneExceptionTest extends AndroidTestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor",
            method = "SQLiteDoneException",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor",
            method = "SQLiteDoneException",
            args = {java.lang.String.class}
        )
    })
    public void testConstructor() {
        new SQLiteDoneException();
        new SQLiteDoneException("error");
    }
}
