@TestTargetClass(android.net.Credentials.class)
public class CredentialsTest extends AndroidTestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "Credentials",
            args = {int.class, int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getGid",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getPid",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getUid",
            args = {}
        )
    })
    public void testCredentials() {
        Credentials cred = new Credentials(0, 0, 0);
        assertEquals(0, cred.getGid());
        assertEquals(0, cred.getPid());
        assertEquals(0, cred.getUid());
        cred = new Credentials(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, cred.getGid());
        assertEquals(Integer.MAX_VALUE, cred.getPid());
        assertEquals(Integer.MAX_VALUE, cred.getUid());
        cred = new Credentials(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
        assertEquals(Integer.MIN_VALUE, cred.getGid());
        assertEquals(Integer.MIN_VALUE, cred.getPid());
        assertEquals(Integer.MIN_VALUE, cred.getUid());
    }
}
