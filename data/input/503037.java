@TestTargetClass(android.database.SQLException.class)
public class SQLExceptionTest extends AndroidTestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructors of SQLException.",
            method = "SQLException",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructors of SQLException.",
            method = "SQLException",
            args = {java.lang.String.class}
        )
    })
    public void testConstructors() {
        String expected1 = "Expected exception message";
        try {
            throw new SQLException();
        } catch (SQLException e) {
            assertNull(e.getMessage());
        }
        try {
            throw new SQLException(expected1);
        } catch (SQLException e) {
            assertEquals(expected1, e.getMessage());
        }
    }
}
