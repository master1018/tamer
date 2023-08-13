@TestTargetClass(NotOwnerException.class)
public class NotOwnerException2Test extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "NotOwnerException",
        args = {}
    )
    public void test_Constructor() {
        try {
            throw new NotOwnerException();
        } catch (NotOwnerException e) {
            assertEquals("NotOwnerException.toString() should have been "
                    + "'java.security.acl.NotOwnerException' but was "
                    + e.toString(), "java.security.acl.NotOwnerException", e
                    .toString());
        }
    }
}
