@TestTargetClass(RemoteException.class)
public class RemoteExceptionTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "RemoteException",
        args = {}
    )
    public void testRemoteException() throws Exception {
        new RemoteException();
    }
}
