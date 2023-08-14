@TestTargetClass(android.database.sqlite.SQLiteConstraintException.class)
public class SQLiteConstraintExceptionTest extends AndroidTestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructors",
            method = "SQLiteConstraintException",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructors",
            method = "SQLiteConstraintException",
            args = {java.lang.String.class}
        )
    })
    public void testConstructor() {
        new SQLiteConstraintException();
        new SQLiteConstraintException("error");
    }
}
