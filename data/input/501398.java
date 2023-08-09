@TestTargetClass(ConnectionEventListener.class)
public class ConnectionEventListenerTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "connectionClosed",
        args = {javax.sql.ConnectionEvent.class}
    )
    public void testConnectionClosed() {
        fail("Not yet implemented"); 
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "connectionErrorOccurred",
        args = {javax.sql.ConnectionEvent.class}
    )
    public void testConnectionErrorOccurred() {
        fail("Not yet implemented"); 
    }
}
