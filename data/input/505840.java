@TestTargetClass(AclNotFoundException.class)
public class AclNotFoundException2Test extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "AclNotFoundException",
        args = {}
    )
    public void test_Constructor() {
        try {
            if (true) {
                throw new AclNotFoundException();
            }
            fail("Should have thrown AclNotFoundException");
        } catch (AclNotFoundException e) {
            assertEquals("AclNotFoundException.toString() should have been "
                    + "'java.security.acl.AclNotFoundException' but was "
                    + e.toString(),
                    "java.security.acl.AclNotFoundException",
                    e.toString());
        }
    }
}
