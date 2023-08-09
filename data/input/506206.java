@TestTargetClass(LastOwnerException.class)
public class LastOwnerException2Test extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "LastOwnerException",
        args = {}
    )
    public void test_Constructor() {
        try {
            throw new LastOwnerException();
        } catch (LastOwnerException e) {
            assertEquals("LastOwnerException.toString() should have been "
                    + "'java.security.acl.LastOwnerException' but was "
                    + e.toString(), "java.security.acl.LastOwnerException", e
                    .toString());
        }
    }
}
