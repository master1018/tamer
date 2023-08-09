@TestTargetClass(Signer.class)
public class SignerTest extends TestCase {
    public static class MySecurityManager extends SecurityManager {
        public Permissions denied = new Permissions(); 
        public void checkPermission(Permission permission){
            if (denied!=null && denied.implies(permission)) throw new SecurityException();
        }
    }    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(SignerTest.class);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "toString",
        args = {}
    )
    public void test_toString() throws Exception {
        Signer s1 = new SignerStub("testToString1");
        assertEquals("[Signer]testToString1", s1.toString());
        Signer s2 = new SignerStub("testToString2", IdentityScope.getSystemScope());
        s2.toString();
        KeyPair kp = new KeyPair(new PublicKeyStub("public", "SignerTest.testToString", null),
                new PrivateKeyStub("private", "SignerTest.testToString", null));
        s1.setKeyPair(kp);
        s1.toString();
        s2.setKeyPair(kp);
        s2.toString();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "Signer",
        args = {}
    )
    public void testSigner() {
        Signer s = new SignerStub();
        assertNotNull(s);
        assertNull(s.getPrivateKey());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "Signer",
        args = {java.lang.String.class}
    )
    public void testSignerString() throws Exception {
        Signer s = new SignerStub("sss3");
        assertNotNull(s);
        assertEquals("sss3", s.getName());      
        assertNull(s.getPrivateKey());
        Signer s2 = new SignerStub(null);
        assertNull(s2.getName());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "Signer",
        args = {java.lang.String.class, java.security.IdentityScope.class}
    )
    public void testSignerStringIdentityScope() throws Exception {
        Signer s = new SignerStub("sss4", IdentityScope.getSystemScope());
        assertNotNull(s);
        assertEquals("sss4", s.getName());
        assertSame(IdentityScope.getSystemScope(), s.getScope());
        assertNull(s.getPrivateKey());
        try {
            Signer s2 = new SignerStub("sss4", IdentityScope.getSystemScope());
            fail("expected KeyManagementException not thrown");
        } catch (KeyManagementException e)
        {
        }
        Signer s2 = new SignerStub(null);
        assertNull(s2.getName());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "getPrivateKey",
        args = {}
    )
    public void testGetPrivateKey() throws Exception {
        byte [] privateKeyData = { 1, 2, 3, 4, 5};  
        PrivateKeyStub privateKey = new PrivateKeyStub("private", "fff", privateKeyData);
        PublicKeyStub publicKey = new PublicKeyStub("public", "fff", null);
        KeyPair kp = new KeyPair(publicKey, privateKey);
        Signer s = new SignerStub("sss5");
        assertNull(s.getPrivateKey());
        s.setKeyPair(kp);                
        assertSame(privateKey, s.getPrivateKey());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "getPrivateKey",
        args = {}
    )
    public void testGetPrivateKey_denied() throws Exception {
        MySecurityManager sm = new MySecurityManager();
        sm.denied.add(new SecurityPermission("getSignerPrivateKey"));
        System.setSecurityManager(sm);
        try {
            Signer s = new SignerStub("sss6");
            s.setKeyPair(new KeyPair(new PublicKeyStub("public", "fff", null), new PrivateKeyStub(
                    "private", "fff", null)));
            try {
                s.getPrivateKey();
                fail("SecurityException should be thrown");
            } catch (SecurityException ok) {
            }
        } finally {
            System.setSecurityManager(null);
        }
    }
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        notes = "KeyException checking missed",
        method = "setKeyPair",
        args = {java.security.KeyPair.class}
    )
    public void test_setKeyPairLjava_security_KeyPair() throws Exception {
        try {
            new SignerStub("name").setKeyPair(null);
            fail("No expected NullPointerException");
        } catch (NullPointerException e) {
        }
        SecurityManager oldSm = System.getSecurityManager();
        MySecurityManager sm = new MySecurityManager();
        sm.denied.add(new SecurityPermission("setSignerKeyPair"));
        System.setSecurityManager(sm);
        try {
            Signer s = new SignerStub("sss7");
            try {
                s.setKeyPair(new KeyPair(new PublicKeyStub("public", "fff",
                        null), new PrivateKeyStub("private", "fff", null)));
                fail("SecurityException should be thrown");
            } catch (SecurityException ok) {
            }
        } finally {
            System.setSecurityManager(oldSm);
        }
        try {
            KeyPair kp = new KeyPair(null, null);
            SignerStub s = new SignerStub("name");
            s.setKeyPair(kp);
        } catch (InvalidParameterException e) {
        }
    }
}
