@TestTargetClass(android.database.sqlite.SQLiteMisuseException.class)
public class SQLiteMisuseExceptionTest extends AndroidTestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor",
            method = "SQLiteMisuseException",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor",
            method = "SQLiteMisuseException",
            args = {java.lang.String.class}
        )
    })
    public void testConstructor() {
        new SQLiteMisuseException();
        new SQLiteMisuseException("error");
    }
}
