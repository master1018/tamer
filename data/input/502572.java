@TestTargetClass(ProtectionDomain.class)
public class ProtectionDomainTest extends TestCase {
    public static void main(String[] args) {
        junit.textui.TestRunner.run(ProtectionDomainTest.class);
    }
    private final AllPermission allperm = new AllPermission();
    private URL url = null;
    private CodeSource cs = null;
    private PermissionCollection perms = null;
    private ClassLoader classldr = null;
    private Principal[] principals = null; 
    protected void setUp() throws Exception {
        super.setUp();
        try {
            url = new URL("http:
        } catch (MalformedURLException ex) {
            throw new Error(ex);
        }
        cs = new CodeSource(url, (java.security.cert.Certificate[]) null);
        perms = allperm.newPermissionCollection();
        perms.add(allperm);
        classldr = URLClassLoader.newInstance(new URL[] { url });
        principals = new Principal[] { new TestPrincipal("0"),
                new TestPrincipal("1"), new TestPrincipal("2"),
                new TestPrincipal("3"), new TestPrincipal("4"), };
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "ProtectionDomain",
        args = {java.security.CodeSource.class, java.security.PermissionCollection.class}
    )
    public void testProtectionDomainCodeSourcePermissionCollection_00() {
        new ProtectionDomain(null, null);
        new ProtectionDomain(cs, null);
        new ProtectionDomain(cs, perms);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "ProtectionDomain",
        args = {java.security.CodeSource.class, java.security.PermissionCollection.class}
    )
    public void testProtectionDomainCodeSourcePermissionCollection_01() {
        assertFalse(perms.isReadOnly());
        new ProtectionDomain(null, perms);
        assertTrue(perms.isReadOnly());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "ProtectionDomain",
        args = {java.security.CodeSource.class, java.security.PermissionCollection.class, java.lang.ClassLoader.class, java.security.Principal[].class}
    )
    public void testProtectionDomainCodeSourcePermissionCollectionClassLoaderPrincipalArray() {
        new ProtectionDomain(null, null, null, null);
        new ProtectionDomain(cs, null, null, null);
        new ProtectionDomain(null, perms, null, null);
        new ProtectionDomain(null, null, classldr, null);
        new ProtectionDomain(null, null, null, principals);
        new ProtectionDomain(cs, perms, classldr, principals);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getClassLoader",
        args = {}
    )
    public void testGetClassLoader() {
        assertNull(new ProtectionDomain(null, null).getClassLoader());
        assertSame(new ProtectionDomain(null, null, classldr, null)
                .getClassLoader(), classldr);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getCodeSource",
        args = {}
    )
    public void testGetCodeSource() {
        assertNull(new ProtectionDomain(null, null).getCodeSource());
        assertSame(new ProtectionDomain(cs, null).getCodeSource(), cs);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getPermissions",
        args = {}
    )
    public void testGetPermissions() {
        assertNull(new ProtectionDomain(null, null).getPermissions());
        assertSame(new ProtectionDomain(null, perms).getPermissions(), perms);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "getPrincipals",
        args = {}
    )
    public void testGetPrincipals_00() {
        assertNotNull(new ProtectionDomain(null, null).getPrincipals());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "getPrincipals",
        args = {}
    )
    public void testGetPrincipals_01() {
        ProtectionDomain pd = new ProtectionDomain(null, null, null, principals);
        Principal[] got = pd.getPrincipals();
        assertNotNull(got);
        assertNotSame(got, principals);
        assertNotSame(got, pd.getPrincipals());
        assertTrue(got.length == principals.length);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "implies",
        args = {java.security.Permission.class}
    )
    public void testImplies_00() {
        assertFalse(new ProtectionDomain(null, null).implies(allperm));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "implies",
        args = {java.security.Permission.class}
    )
    public void testImplies_01() {
        assertTrue(new ProtectionDomain(null, perms).implies(allperm));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "implies",
        args = {java.security.Permission.class}
    )
    public void testImplies_02() {
        TestPolicy policy = new TestPolicy();
        ProtectionDomain pd = new ProtectionDomain(cs, null);
        policy.setTrackPD(pd);
        try {
            Policy.setPolicy(policy);
            pd.implies(allperm);
        } finally {
            Policy.setPolicy(null);
        }
        assertFalse(policy.getPdTracked());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "implies",
        args = {java.security.Permission.class}
    )
    public void testImplies_03() {
        TestPolicy policy = new TestPolicy();
        ProtectionDomain pd = new ProtectionDomain(cs, null, ClassLoader
                .getSystemClassLoader(), principals);
        policy.setTrackPD(pd);
        try {
            Policy.setPolicy(policy);
            pd.implies(allperm);
        } finally {
            Policy.setPolicy(null);
        }
        assertTrue(policy.getPdTracked());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "toString",
        args = {}
    )
    public void testToString() {
        String res;
        res = new ProtectionDomain(null, null).toString();
        assertTrue(res.contains("ProtectionDomain"));
        res = new ProtectionDomain(cs, perms).toString();
        assertTrue(res.contains("ProtectionDomain"));
        res = new ProtectionDomain(null, null, null, null).toString();
        assertTrue(res.contains("ProtectionDomain"));
        res = new ProtectionDomain(cs, perms, classldr, principals).toString();
        assertTrue(res.contains("ProtectionDomain"));
    }
    private static class TestPrincipal implements Principal {
        private String name;
        TestPrincipal(String name) {
            this.name = name;
        }
        public String getName() {
            return "TestPrincipal: " + name;
        }
    }
    private static class TestPolicy extends Policy {
        ProtectionDomain trackPD = null;
        boolean pdTracked = false;
        ProtectionDomain setTrackPD(ProtectionDomain pd) {
            ProtectionDomain tmp = trackPD;
            trackPD = pd;
            pdTracked = false;
            return tmp;
        }
        boolean getPdTracked() {
            return pdTracked;
        }
        public PermissionCollection getPermissions(CodeSource cs) {
            return new Permissions();
        }
        public boolean implies(ProtectionDomain domain, Permission permission) {
            if (trackPD != null && trackPD == domain) {
                pdTracked = true;
            }
            return super.implies(domain, permission);
        }
        public void refresh() {
        }
    }
}
