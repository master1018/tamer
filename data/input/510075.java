@TestTargetClass(AccessController.class)
public class AccessControllerTest extends TestCase {
    private static void setProtectionDomain(Class<?> c, ProtectionDomain pd){
        Field fields[] = Class.class.getDeclaredFields();
        for(Field f : fields){
            if("pd".equals(f.getName())){
                f.setAccessible(true);
                try {
                    f.set(c, pd);
                } catch (IllegalArgumentException e) {
                    fail("Protection domain could not be set");
                } catch (IllegalAccessException e) {
                    fail("Protection domain could not be set");
                }
                break;
            }
        }
    }
    SecurityManager old;
    TestPermission p;
    CodeSource codeSource;
    PermissionCollection c0, c1, c2;
    public static void main(String[] args) throws Exception {
        AccessControllerTest t = new AccessControllerTest();
        t.setUp();
        t.test_do_privileged1();
        t.tearDown();
    }
    @Override
    protected void setUp() throws Exception {
        old = System.getSecurityManager();
        codeSource = null;
        p = new TestPermission();
        c0 = p.newPermissionCollection();
        c1 = p.newPermissionCollection();
        c2 = p.newPermissionCollection();
        super.setUp();
    }
    @Override
    protected void tearDown() throws Exception {
        System.setSecurityManager(old);
        super.tearDown();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies that checkPermission throws a SecurityException " +
                    "if a particular permission is not set in the protection domain " +
                    "of a class on the call stack.",
            method = "checkPermission",
            args = {Permission.class}
        )
    })
    public void test_do_privileged1() throws Exception {
        c1.add(p);
        c2.add(p);
        setProtectionDomain(T0.class, new ProtectionDomain(codeSource, c0));
        setProtectionDomain(T1.class, new ProtectionDomain(codeSource, c1));
        setProtectionDomain(T2.class, new ProtectionDomain(codeSource, c2));
        System.setSecurityManager(new SecurityManager());
        try {
            T0.f0();
            fail("expected java.security.AccessControlException");
        }
        catch(java.security.AccessControlException e){
        }
        catch(Exception e){
            fail("expected java.security.AccessControlException, got "+e.getClass().getName());
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies that checkPermission does not throw a SecurityException " +
                    "if all classes on the call stack refer to a protection domain " +
                    "which contains the necessary permissions.",
            method = "checkPermission",
            args = {Permission.class}
        )
    })
    public void test_do_privileged2() {
        c0.add(p);
        c1.add(p);
        c2.add(p);
        setProtectionDomain(T0.class, new ProtectionDomain(codeSource, c0));
        setProtectionDomain(T1.class, new ProtectionDomain(codeSource, c1));
        setProtectionDomain(T2.class, new ProtectionDomain(codeSource, c2));
        System.setSecurityManager(new SecurityManager());
        try {
            String res = T0.f0();
            assertEquals("ok", res);
        }
        catch(java.security.AccessControlException e){
            fail("expected no java.security.AccessControlException");
        }
        catch(Exception e){
            fail("expected no exception, got "+e.getClass().getName());
        }
    }
    @TestTargets({
        @TestTargetNew(
                level = TestLevel.PARTIAL_COMPLETE,
                notes = "Verifies that checkPermission does not throw a SecurityException " +
                        "if a method call is performed with doPrivileged, even if not all " +
                        "classes beyond the doPrivileged call have the necessary permissions " +
                        "set in their protection domains.",
                method = "checkPermission",
                args = {Permission.class}
        ),
        @TestTargetNew(
                level = TestLevel.PARTIAL_COMPLETE,
                notes = "Verifies that checkPermission does not throw a SecurityException " +
                        "if a method call is performed with doPrivileged, even if not all " +
                        "classes beyond the doPrivileged call have the necessary permissions " +
                        "set in their protection domains.",
                method = "doPrivileged",
                args = {PrivilegedAction.class}
        )
    })
    public void test_do_privileged3() {
        c1.add(p);
        c2.add(p);
        setProtectionDomain(T0.class, new ProtectionDomain(codeSource, c0));
        setProtectionDomain(T1.class, new ProtectionDomain(codeSource, c1));
        setProtectionDomain(T2.class, new ProtectionDomain(codeSource, c2));
        System.setSecurityManager(new SecurityManager());
        try {
            String res = T0.f0_priv();
            assertEquals("ok", res);
        }
        catch(java.security.AccessControlException e){
            fail("expected no java.security.AccessControlException");
        }
        catch(Exception e){
            fail("expected no exception, got "+e.getClass().getName());
        }
    }
    static class T0 {
        static String f0(){
            return T1.f1();
        }
        static String f0_priv(){
            return T1.f1_priv();
        }
    }
    static class T1 {
        static String f1(){
            return T2.f2();
        }
        static String f1_priv(){
            return AccessController.doPrivileged(
                new PrivilegedAction<String>(){
                    public String run() {
                        return T2.f2();
                    }
                }
            );
        }
    }
    static class T2 {
        static String f2(){
            SecurityManager s = System.getSecurityManager();
            assertNotNull(s);
            s.checkPermission(new TestPermission());
            return "ok";
        }
    }
    static class TestPermission extends BasicPermission {
        private static final long serialVersionUID = 1L;
        public TestPermission(){ super("TestPermission"); }
        @Override
        public boolean implies(Permission permission) {
            return permission instanceof TestPermission;
        }
    }
}
