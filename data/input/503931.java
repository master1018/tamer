@TestTargetClass(Permission.class)
public class IPermissionTest extends TestCase {
    class MyPermission extends PermissionImpl {
        public MyPermission(String str) {
            super(str);
        }
    }    
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "equals",
        args = {java.lang.Object.class}
    )
    public void test_equals() {
        try {
            MyPermission mp1 = new MyPermission("TestPermission");
            MyPermission mp2 = new MyPermission("NewTestPermission");
            Object another = new Object();
            assertFalse(mp1.equals(another));
            assertFalse(mp1.equals(mp2));
            assertTrue(mp1.equals(new MyPermission("TestPermission")));
        } catch (Exception e) {
            fail("Unexpected exception - subtest1");
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "toString",
        args = {}
    )
    public void test_toString() {
        try {
            MyPermission obj = new MyPermission("TestPermission");
            String res = obj.toString();
            assertEquals(res, "TestPermission");
        } catch (Exception e) {
            fail("Unexpected exception - subtest2");
        }
    }
}