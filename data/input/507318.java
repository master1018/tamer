@TestTargetClass(android.database.sqlite.SQLiteDiskIOException.class)
public class SQLiteDiskIOExceptionTest extends AndroidTestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor",
            method = "SQLiteDiskIOException",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor",
            method = "SQLiteDiskIOException",
            args = {java.lang.String.class}
        )
    })
    public void testConstructor() {
        new SQLiteDiskIOException();
        new SQLiteDiskIOException("error");
    }
}
