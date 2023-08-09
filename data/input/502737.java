@TestTargetClass(android.database.sqlite.SQLiteDatabaseCorruptException.class)
public class SQLiteDatabaseCorruptExceptionTest extends AndroidTestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor",
            method = "SQLiteDatabaseCorruptException",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor",
            method = "SQLiteDatabaseCorruptException",
            args = {java.lang.String.class}
        )
    })
    public void testConstructor() {
        new SQLiteDatabaseCorruptException();
        new SQLiteDatabaseCorruptException("error");
    }
}
