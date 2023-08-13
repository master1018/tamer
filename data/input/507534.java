@TestTargetClass(AclNotFoundException.class)
public class AclNotFoundExceptionTest extends TestCase {
    public static void main(String[] args) {
        junit.textui.TestRunner.run(AclNotFoundExceptionTest.class);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "AclNotFoundException",
        args = {}
    )
    public void testAclNotFoundException() {
        assertNotNull(new AclNotFoundException());
        assertNull(new AclNotFoundException().getMessage());
        assertNull(new AclNotFoundException().getCause());
    }
}
