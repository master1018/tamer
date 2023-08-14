@TestTargetClass(Policy.class)
public class PolicyTest extends TestCase {
    public static final String JAVA_SECURITY_POLICY = "java.security.policy";
    public static void main(String[] args) {
        junit.textui.TestRunner.run(PolicyTest.class);
    }
    @Override protected void tearDown() throws Exception {
        TestEnvironment.reset();
        super.tearDown();
    }
    @SuppressWarnings("cast")
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "Policy",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getPermissions",
            args = {java.security.CodeSource.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "refresh",
            args = {}
        )
    })
    public void test_constructor() {
        TestProvider tp;
        CodeSource cs = new CodeSource(null, (Certificate[]) null);
        try {
            tp = new TestProvider();
            assertTrue(tp instanceof Policy);
        } catch (Exception e) {
            fail("Unexpected exception " + e.getMessage());
        }
        try {
            tp = new TestProvider();
            tp.getPermissions(cs);
            tp.refresh();
        } catch (Exception e) {
            fail("Unexpected exception was thrown for abstract methods");
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "setPolicy",
            args = {java.security.Policy.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "getPolicy",
            args = {}
        )
    })
    public void test_setPolicyLjava_security_Policy() {
        SecurityManager old = System.getSecurityManager();
        Policy oldPolicy = Policy.getPolicy();
        try {
            SecurityChecker checker = new SecurityChecker(
                    new SecurityPermission("setPolicy"), true);
            System.setSecurityManager(checker);
            Policy custom = new TestProvider();
            Policy.setPolicy(custom);
            assertTrue(checker.checkAsserted);
            assertSame(custom, Policy.getPolicy());
            checker.reset();
            checker.enableAccess = false;
            try {
                Policy.setPolicy(new TestProvider());
                fail("SecurityException is intercepted");
            } catch (SecurityException ok) {
            }
        } finally {
            System.setSecurityManager(old);
            Policy.setPolicy(oldPolicy);
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "getPolicy",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "setPolicy",
            args = {java.security.Policy.class}
        )
    })
    public void test_getPolicy() {
        SecurityManager old = System.getSecurityManager();
        Policy oldPolicy = Policy.getPolicy();
        try {
            Policy.setPolicy(new TestProvider());
            SecurityChecker checker = new SecurityChecker(
                    new SecurityPermission("getPolicy"), true);
            System.setSecurityManager(checker);
            Policy.getPolicy();
            assertTrue(checker.checkAsserted);
            checker.reset();
            checker.enableAccess = false;
            try {
                Policy.getPolicy();
                fail("SecurityException is intercepted");
            } catch (SecurityException ok) {
            }
        } finally {
            System.setSecurityManager(old);
            Policy.setPolicy(oldPolicy);
        }
    }
    public static class TestProvider extends Policy {
        PermissionCollection pc;
        public PermissionCollection getPermissions(CodeSource cs) {
            return pc;
        }
        public void refresh() {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getPermissions",
        args = {java.security.ProtectionDomain.class}
    )
    public void testGetPermissions() {
        SecurityPermission sp = new SecurityPermission("abc");
        SecurityPermission sp2 = new SecurityPermission("fbdf");
        PermissionCollection spc = sp.newPermissionCollection();
        spc.add(sp2);
        ProtectionDomain pd = new ProtectionDomain(null, null);
        ProtectionDomain pd2 = new ProtectionDomain(null, spc);
        TestProvider policy = new TestProvider();
        policy.pc = sp.newPermissionCollection();
        PermissionCollection pc4pd = policy.getPermissions(pd);
        assertNotNull(pc4pd);
        Enumeration<Permission> en = pc4pd.elements();
        assertFalse(en.hasMoreElements());
        pc4pd = policy.getPermissions(pd2);
        assertNotNull(pc4pd);
        policy.pc.add(sp);
        pc4pd = policy.getPermissions(pd);
        assertNotNull(pc4pd);
        Collection<Permission> c = new HashSet<Permission>();
        for (en = pc4pd.elements(); en.hasMoreElements(); c.add(en
                .nextElement())) {
        }
        assertTrue(c.contains(sp));
        pc4pd = policy.getPermissions(pd2);
        assertNotNull(pc4pd);
        c = new HashSet<Permission>();
        for (en = pc4pd.elements(); en.hasMoreElements(); c.add(en
                .nextElement())) {
        }
        assertTrue(c.contains(sp));
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "setPolicy",
            args = {java.security.Policy.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "getPolicy",
            args = {}
        )
    })
    public void testResetingPolicyToDefault() {
        Policy oldPolicy = Policy.getPolicy();
        assertNotNull("Got a null system security policy", oldPolicy);
        try {
            Policy.setPolicy(null); 
            Policy newPolicy = Policy.getPolicy();
            assertNotNull(newPolicy);
            assertNotSame(oldPolicy, newPolicy);
            assertEquals("Policy class name", Security
                    .getProperty("policy.provider"), newPolicy.getClass()
                    .getName());
        } finally {
            Policy.setPolicy(oldPolicy);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "implies",
        args = {java.security.ProtectionDomain.class, java.security.Permission.class}
    )
    public void test_implies() {
        Policy policy = Policy.getPolicy();
        char s = File.separatorChar;
        URL url = null;
        try {
            url = new URL("http:
        } catch (MalformedURLException ex) {
            throw new Error(ex);
        }
        CodeSource cs = new CodeSource(url,
                (java.security.cert.Certificate[]) null);
        FilePermission perm[] = new FilePermission[7];
        perm[0] = new FilePermission("test1.file", "write");
        perm[1] = new FilePermission("test1.file",
                "read, write, execute,delete");
        perm[2] = new FilePermission(s + "tmp" + s + "test" + s + "*",
                "read,write");
        perm[3] = new FilePermission(s + "tmp" + s + "test" + s
                + "collection.file", "read");
        perm[4] = new FilePermission(s + "windows" + "*", "delete");
        perm[5] = new FilePermission("aFile.file", "read");
        perm[6] = new FilePermission("hello.file", "write");
        Permissions perms = new Permissions();
        for (int i = 0; i < perm.length; i++) {
            perms.add(perm[i]);
        }
        ProtectionDomain pd = new ProtectionDomain(cs, perms);
        assertTrue(policy.implies(pd, perm[0]));
        assertTrue(policy.implies(pd, perm[1]));
        assertTrue(policy.implies(pd, perm[2]));
        assertTrue(policy.implies(pd, perm[3]));
        assertTrue(policy.implies(pd, perm[4]));
        assertTrue(policy.implies(pd, perm[5]));
        assertTrue(policy.implies(pd, perm[6]));
        assertTrue(policy.implies(pd,
                new FilePermission("test1.file", "delete")));
        assertTrue(policy.implies(pd,
                new FilePermission(s + "tmp" + s + "test" + s + "test1.file", "read")));
        assertFalse(policy.implies(pd, new FilePermission("aFile.file",
                "delete")));
        assertFalse(policy.implies(pd, new FilePermission("hello.file",
                "delete")));
        assertFalse(policy.implies(pd, new FilePermission(s + "tmp" + s
                + "test" + s + "collection.file", "delete")));
        assertFalse(policy.implies(pd, new FilePermission(s + "tmp" + s
                + "test" + s + "*", "delete")));
        assertFalse(policy.implies(pd, new FilePermission("hello.file",
                "execute")));
        try {
            assertFalse(policy.implies(pd, null));
        } catch (NullPointerException e) {
        }
        try {
            assertFalse(policy.implies(null, new FilePermission("test1.file", "delete")));
        } catch (NullPointerException e) {
            fail("Unexpected NullPointerException");
        }
        try {
            assertFalse(policy.implies(null, null));
        } catch (NullPointerException e) {
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "setPolicy",
            args = {java.security.Policy.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "getPolicy",
            args = {}
        )
    })
    public void testPropertyExpansion() throws Exception {
        String policyFile = getClass().getClassLoader().getResource(
                "PolicyTest.txt").toString();
        String oldJavaPolicy = System.getProperty(JAVA_SECURITY_POLICY);
        Policy oldPolicy = Policy.getPolicy();
        try {
            System.setProperty(JAVA_SECURITY_POLICY, policyFile);
            assertCodeBasePropertyExpansion("/11111
    private void assertCodeBasePropertyExpansion(String codeSourceURL,
            String codeBaseURL) throws Exception {
        Policy.setPolicy(null); 
        System.setProperty("test.bin.dir", codeBaseURL);
        Policy p = Policy.getPolicy();
        CodeSource codeSource = new CodeSource(
                new URL("file:" + codeSourceURL),
                (java.security.cert.Certificate[]) null);
        PermissionCollection pCollection = p.getPermissions(codeSource);
        Enumeration<Permission> elements = pCollection.elements();
        SecurityPermission perm = new SecurityPermission(
                "codeBaseForPolicyTest");
        while (elements.hasMoreElements()) {
            if (elements.nextElement().equals(perm)) {
                return; 
            }
        }
        fail("Failed to find SecurityPermission for codeSource="
                + codeSourceURL + ", codeBase=" + codeBaseURL);
    }
}
