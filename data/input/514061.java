@TestTargetClass(IdentityScope.class)
@SuppressWarnings("deprecation")
public class IdentityScopeTest extends TestCase {
    public static class MySecurityManager extends SecurityManager {
        public Permissions denied = new Permissions(); 
        public void checkPermission(Permission permission){
            if (denied!=null && denied.implies(permission)) throw new SecurityException();
        }
    }
    public static void main(String[] args) {
        junit.textui.TestRunner.run(IdentityScopeTest.class);
    }
    IdentityScope is;
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "toString",
        args = {}
    )
    public final void testToString() {
        assertNotNull(new IdentityScopeStub("Aleksei Semenov").toString());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "IdentityScope",
        args = {}
    )
    public final void testIdentityScope() {
        assertNotNull(new IdentityScopeStub());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Verifies just positive case with non null parameter",
        method = "IdentityScope",
        args = {java.lang.String.class}
    )
    public final void testIdentityScopeString() {
        is = new IdentityScopeStub("Aleksei Semenov");
        assertNotNull(is);
        assertEquals("Aleksei Semenov", is.getName());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Verifies just positive test with both non null parameters",
        method = "IdentityScope",
        args = {java.lang.String.class, java.security.IdentityScope.class}
    )
    public final void testIdentityScopeStringIdentityScope() throws Exception {
        IdentityScope scope = new IdentityScopeStub("my scope");
        is = new IdentityScopeStub("Aleksei Semenov", scope);
        assertNotNull(is);
        assertEquals("Aleksei Semenov", is.getName());
        assertEquals(scope.getName(), is.getScope().getName());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getSystemScope",
        args = {}
    )
    public final void testGetSystemScope() {
        String name = Security.getProperty("system.scope");
        assertNotNull(name);
        IdentityScope scope = IdentityScope.getSystemScope(); 
        assertNotNull(scope);
        assertEquals(name, scope.getClass().getName());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "setSystemScope",
            args = {java.security.IdentityScope.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getSystemScope",
            args = {}
        )
    })
    public final void testSetSystemScope() {
        IdentityScope systemScope = IdentityScope.getSystemScope();
        try {
            is = new IdentityScopeStub("Aleksei Semenov");
            IdentityScopeStub.mySetSystemScope(is);
            assertSame(is, IdentityScope.getSystemScope());
            MySecurityManager sm = new MySecurityManager();
            System.setSecurityManager(sm);
            try {
                is = new IdentityScopeStub("aaa");
                IdentityScopeStub.mySetSystemScope(is);
                assertSame(is, IdentityScope.getSystemScope());       
                sm.denied.add(new SecurityPermission("setSystemScope"));
                IdentityScope is2 = new IdentityScopeStub("bbb");
                try{
                    IdentityScopeStub.mySetSystemScope(is2); 
                    fail("SecurityException should be thrown");
                } catch (SecurityException e){
                    assertSame(is, IdentityScope.getSystemScope());
                }
            } finally {
                System.setSecurityManager(null);
                assertNull("Error, security manager is not removed!", System.getSecurityManager());
            }
        } finally {
            IdentityScopeStub.mySetSystemScope(systemScope);
        }
    }
}
