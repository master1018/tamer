@TestTargetClass(android.database.sqlite.SQLiteQuery.class)
public class SQLiteQueryTest extends TestCase {
    @TestTargets ({
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            method = "bindDouble",
            args = {int.class, double.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            method = "bindLong",
            args = {int.class, long.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            method = "bindNull",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            method = "bindString",
            args = {int.class, java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            method = "close",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            method = "toString",
            args = {}
        )
    })
    @ToBeFixed(bug = "1686574", explanation = "can not get an instance of SQLiteQuery" +
            " or construct it directly for testing")
    public void testMethods() {
    }
}
