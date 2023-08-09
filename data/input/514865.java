@TestTargetClass(javax.security.auth.SubjectDomainCombiner.class)
public class JavaxSecurityAuthSubjectDomainCombiner extends TestCase {
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
        notes = "Verifies that getSubject() calls checkPermission on security permissions.",
        method = "getSubject",
        args = {}
    )
    public void test_getSubject() {
        class TestSecurityManager extends SecurityManager {
            boolean called = false;
            void reset() {
                called = false;
            }
            @Override
            public void checkPermission(Permission permission) {
                if (permission instanceof AuthPermission
                        && "getSubjectFromDomainCombiner".equals(permission.getName())) {
                    called = true;
                }
            }
        }
        Subject subject = new Subject();
        TestSecurityManager s = new TestSecurityManager();
        System.setSecurityManager(s);
        s.reset();
        SubjectDomainCombiner sdc = new SubjectDomainCombiner(subject);
        sdc.getSubject();
        assertTrue(
                "javax.security.auth.SubjectDomainCombiner.getSubject() must call checkPermission on security manager",
                s.called);
    }
}
