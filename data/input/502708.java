@TestTargetClass(java.security.Policy.class)
public class JavaSecurityPolicyTest extends TestCase {
    SecurityManager old;
    @Override
    protected void setUp() throws Exception {
        old = System.getSecurityManager();
        super.setUp();
    }
    @Override
    protected void tearDown() throws Exception {
        System.setSecurityManager(old);
        super.tearDown();
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Verifies that java.security.Policy.getPolicy() method calls checkPermission on security manager.",
        method = "getPolicy",
        args = {}
    )
    public void test_getPolicy() {
        class TestSecurityManager extends SecurityManager {
            boolean called = false;
            void reset(){
                called = false;
            }
            @Override
            public void checkPermission(Permission permission) {
                if(permission instanceof SecurityPermission && "getPolicy".equals(permission.getName())){
                    called = true;              
                }
            }
        }
        TestSecurityManager s = new TestSecurityManager();
        System.setSecurityManager(s);
        s.reset();
        Policy.getPolicy();
        assertTrue("java.security.Policy.getPolicy() must call checkPermission on security permissions", s.called);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Verifies that java.security.Policy.setPolicy() method calls checkPermission on security manager.",
        method = "setPolicy",
        args = {java.security.Policy.class}
    )
    public void test_setPolicy() {
        class TestSecurityManager extends SecurityManager {
            boolean called = false;
            void reset(){
                called = false;
            }
            @Override
            public void checkPermission(Permission permission) {
                if(permission instanceof SecurityPermission && "setPolicy".equals(permission.getName())){
                    called = true;              
                }
            }
        }
        Policy p = Policy.getPolicy();
        TestSecurityManager s = new TestSecurityManager();
        System.setSecurityManager(s);
        s.reset();
        Policy.setPolicy(p);
        assertTrue("java.security.Policy.setPolicy() must call checkPermission on security permissions", s.called);
    }
}
