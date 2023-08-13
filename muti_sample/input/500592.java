@TestTargetClass(java.util.Locale.class)
public class JavaUtilLocale extends TestCase {
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
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies that java.util.Locale.setDefault(Locale) method calls checkPermission on security manager.",
        method = "setDefault",
        args = {java.util.Locale.class}
    )
    public void test_setDefault() {
        class TestSecurityManager extends SecurityManager {
            boolean called = false;
            void reset(){
                called = false;
            }
            @Override
            public void checkPermission(Permission permission) {
                if(permission instanceof PropertyPermission 
                        && "user.language".equals(permission.getName())
                        && "write".equals(permission.getActions())){
                    called = true;              
                }
                super.checkPermission(permission);
            }
        }
        Locale loc = Locale.getDefault();
        TestSecurityManager s = new TestSecurityManager();
        System.setSecurityManager(s);
        s.reset();
        Locale.setDefault(loc);
        assertTrue("java.util.Locale.setDefault(Locale) must call checkPermission on security permissions", s.called);
    }
}
