@TestTargetClass(Permission.class)
public class PermissionTest extends TestCase {
    public static void main(String[] args) {
        junit.textui.TestRunner.run(PermissionTest.class);
    }
    static final class RealPermission extends Permission {
        public RealPermission(String name) {
            super(name);
        }
        public boolean equals(Object obj) {
            return false;
        }
        public String getActions() {
            return null;
        }
        public int hashCode() {
            return 0;
        }
        public boolean implies(Permission permission) {
            return false;
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Non null string parameter verified",
        method = "Permission",
        args = {java.lang.String.class}
    )
    public void testCtor() {
        String name = "testCtor123^%$#&^ &^$";
        Permission test = new RealPermission(name);
        assertEquals(name, test.getName());
        assertEquals("(" + test.getClass().getName() + " " + name + ")", test
            .toString());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "checkGuard",
        args = {java.lang.Object.class}
    )
    public void testCheckGuard() {
        final Permission test = new RealPermission("234234");
        SecurityManager old = System.getSecurityManager();
        try {
            System.setSecurityManager(null);
            test.checkGuard(this);
            final boolean[] callFlag = new boolean[] { false };
            System.setSecurityManager(new SecurityManager() {
                public void checkPermission(Permission p) {
                    if (p == test) {
                        callFlag[0] = true;
                    }
                }
            });
            test.checkGuard(null);
            assertTrue(callFlag[0]);
        } finally {
            System.setSecurityManager(old);
        }
        class TestSecurityManager extends SecurityManager {
            boolean called = false;
            private final String permissionName;
            public TestSecurityManager(String permissionName) {
                this.permissionName = permissionName;
            }
            @Override
            public void checkPermission(Permission permission) {
                if (permission instanceof SecurityPermission
                        && permissionName.equals(permission.getName())) {
                    called = true;
                    super.checkPermission(permission);
                }
            }
        }
        TestSecurityManager sm = new TestSecurityManager("testGuardPermission");
        try {
            System.setSecurityManager(sm);
            Permission p = new SecurityPermission("testGuardPermission");
            p.checkGuard(this);
            assertTrue("SecurityManager must be invoked", sm.called);
        } finally {
            System.setSecurityManager(old);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Returned parameter was tested.",
        method = "newPermissionCollection",
        args = {}
    )
    public void testCollection() {
        assertNull(new RealPermission("123").newPermissionCollection());
    }
}
