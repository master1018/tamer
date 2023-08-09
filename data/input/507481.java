@TestTargetClass(DomainCombiner.class)
public class DomainCombinerTest extends junit.framework.TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "combine",
        args = {java.security.ProtectionDomain[].class, java.security.ProtectionDomain[].class}
    )
    public void test_combine$Ljava_security_ProtectionDomain$Ljava_security_ProtectionDomain() {
        final boolean[] calledDomainCombiner = new boolean[] { false, false };
        class MyCombiner implements DomainCombiner {
            int i;
            MyCombiner(int i) {
                this.i = i;
            }
            public ProtectionDomain[] combine(
                    ProtectionDomain[] executionDomains,
                    ProtectionDomain[] parentDomains) {
                calledDomainCombiner[i] = true;
                PermissionCollection pc = new Permissions();
                pc.add(new AllPermission());
                ProtectionDomain pd;
                if (executionDomains.length > 0) {
                    pd = new ProtectionDomain(executionDomains[0]
                            .getCodeSource(), pc);
                } else {
                    pd = new ProtectionDomain(parentDomains[0].getCodeSource(),
                            pc);
                }
                return new ProtectionDomain[] { pd };
            }
        }
        ProtectionDomain[] domains = new ProtectionDomain[] { new ProtectionDomain(
                new CodeSource(null, (Certificate[]) null), new Permissions()) };
        AccessControlContext parent = new AccessControlContext(domains);
        AccessControlContext c0 = new AccessControlContext(parent,
                new MyCombiner(0));
        final AccessControlContext c1 = new AccessControlContext(parent,
                new MyCombiner(1));
                class TestPermission extends BasicPermission {
                    TestPermission(String s) {
                        super(s);
                    }
                }
        SecurityManager sm = new SecurityManager() {
            public void checkPermission(Permission p) {
                if( p instanceof TestPermission ) {
                    super.checkPermission(p);   
                }
            }
        };
        sm.checkPermission(new SecurityPermission("let it load"));
        System.setSecurityManager(sm);
        try {
            AccessController.doPrivileged(new PrivilegedAction<Object>() {
                public Object run() {
                    AccessController.checkPermission(new TestPermission(
                            "MyTest"));
                    AccessController.doPrivileged(new PrivilegedAction<Object>() {
                        public Object run() {
                            AccessController
                                    .checkPermission(new TestPermission(
                                            "MyTest"));
                            return null;
                        }
                    }, c1);
                    return null;
                }
            }, c0);
            assertTrue("Failed to combine domains for security permission",
                    calledDomainCombiner[0]);
            assertTrue("Failed to combine domains for security permission",
                    calledDomainCombiner[1]);
        } finally {
            System.setSecurityManager(null);
        }
    }
}