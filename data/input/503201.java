@TestTargetClass(PooledConnection.class)
public class PooledConnectionTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "addConnectionEventListener",
        args = {javax.sql.ConnectionEventListener.class}
    )
    public void testAddConnectionEventListener() {
        fail("Not yet implemented");
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "close",
        args = {}
    )
    public void testClose() {
        fail("Not yet implemented");
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "getConnection",
        args = {}
    )
    public void testGetConnection() {
        fail("Not yet implemented");
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "removeConnectionEventListener",
        args = {javax.sql.ConnectionEventListener.class}
    )
    public void testRemoveConnectionEventListener() {
        fail("Not yet implemented");
    }
}
