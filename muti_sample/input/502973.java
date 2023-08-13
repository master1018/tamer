@TestTargetClass(Identity.class)
public class IdentityTest extends TestCase {
    public static class MySecurityManager extends SecurityManager {
        public Permissions denied = new Permissions(); 
        public void checkPermission(Permission permission){
            if (denied!=null && denied.implies(permission)) throw new SecurityException();
        }
    }
    public static void main(String[] args) {
        junit.textui.TestRunner.run(IdentityTest.class);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Method's returned variable is not checked",
        method = "hashCode",
        args = {}
    )
    public void testHashCode() {
        new IdentityStub("testHashCode").hashCode();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "equals",
        args = {java.lang.Object.class}
    )
    public void testEquals() throws Exception {
        IdentityStub i1 = new IdentityStub("testEquals");
        Object value[] =  {
                null, Boolean.FALSE,
                new Object(), Boolean.FALSE,
                i1, Boolean.TRUE,
                new IdentityStub(i1.getName()), Boolean.TRUE
        };
        for (int k=0; k<value.length; k+=2) {
            assertEquals(value[k+1], new Boolean(i1.equals(value[k])));
            if (Boolean.TRUE.equals(value[k+1])) assertEquals(i1.hashCode(), value[k].hashCode());
        }
        Identity i2 = new IdentityStub("testEquals", IdentityScope.getSystemScope());
        assertEquals(i1.identityEquals(i2), i1.equals(i2));
        Identity i3 = new IdentityStub("testEquals3");
        assertEquals(i1.identityEquals(i3), i1.equals(i3));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "toString",
        args = {}
    )
    public void testToString1() {
        MySecurityManager sm = new MySecurityManager();
        sm.denied.add(new SecurityPermission("printIdentity"));
        System.setSecurityManager(sm);
        try {
            new IdentityStub("testToString").toString();
            fail("SecurityException should be thrown");
        } catch (SecurityException ok) {
        } finally {
            System.setSecurityManager(null);
        }          
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "toString",
        args = {}
    )
     public void testToString2() {    
        assertNotNull(new IdentityStub("testToString2").toString());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "Identity",
        args = {}
    )
    public void testIdentity() {
        assertNotNull(new IdentityStub());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Empty string for parameter is not tested",
        method = "Identity",
        args = {java.lang.String.class}
    )
    public void testIdentityString() {
        Identity i = new IdentityStub("iii");
        assertNotNull(i);
        assertEquals("iii", i.getName());
        i=new IdentityStub(null);
        assertNotNull(i);
        assertNull(i.getName());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "KeyManagementException checking missed. Null parameters are not checked.",
        method = "Identity",
        args = {java.lang.String.class, java.security.IdentityScope.class}
    )
    public void testIdentityStringIdentityScope() throws Exception {
        IdentityScope s = IdentityScope.getSystemScope();        
        Identity i = new IdentityStub("iii2", s);
        assertNotNull(i);
        assertEquals("iii2", i.getName());
        assertSame(s, i.getScope());
        assertSame(i, s.getIdentity(i.getName()));        
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "addCertificate",
        args = {java.security.Certificate.class}
    )
    public void testAddCertificate1() throws Exception {
        Identity i = new IdentityStub("iii");
        PublicKeyStub pk1 = new PublicKeyStub("kkk", "fff", new byte[]{1,2,3,4,5});
        i.setPublicKey(pk1);
        CertificateStub c1 = new CertificateStub("fff", null, null, pk1);
        i.addCertificate(c1);
        assertSame(c1, i.certificates()[0]);
        try {
            i.addCertificate(new CertificateStub("ccc", null, null, new PublicKeyStub("k2", "fff", new byte[]{6,7,8,9,0})));
            fail("KeyManagementException should be thrown");
        } catch (KeyManagementException ok) {}        
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "addCertificate",
        args = {java.security.Certificate.class}
    )
    public void testAddCertificate2() throws Exception {
        Identity i = new IdentityStub("iii");
        PublicKeyStub pk1 = new PublicKeyStub("kkk", "fff", null);        
        CertificateStub c1 = new CertificateStub("fff", null, null, pk1);
        i.addCertificate(c1);
        assertSame(c1, i.certificates()[0]);
        assertSame(pk1, i.getPublicKey());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "addCertificate",
        args = {java.security.Certificate.class}
    )
    public void testAddCertificate3() throws Exception {
        MySecurityManager sm = new MySecurityManager();
        sm.denied.add(new SecurityPermission("addIdentityCertificate"));
        System.setSecurityManager(sm);
        try {
            new IdentityStub("iii").addCertificate(new CertificateStub("ccc", null, null, null));
            fail("SecurityException should be thrown");
        } catch (SecurityException ok) {
        } finally {
            System.setSecurityManager(null);
        }        
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "addCertificate",
        args = {java.security.Certificate.class}
    )
    public void testAddCertificate4() throws Exception {
        try {
            new IdentityStub("aaa").addCertificate(null);
            fail("KeyManagementException should be thrown");
        } catch (KeyManagementException ok) {
        } catch (NullPointerException ok) {}
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Checks SecurityException.",
        method = "removeCertificate",
        args = {java.security.Certificate.class}
    )
    public void testRemoveCertificate2() throws Exception{
        MySecurityManager sm = new MySecurityManager();
        sm.denied.add(new SecurityPermission("removeIdentityCertificate"));
        Identity i = new IdentityStub("iii");
        i.addCertificate(new CertificateStub("ccc", null, null, null));
        System.setSecurityManager(sm);
        try {
            i.removeCertificate(i.certificates()[0]);
            fail("SecurityException should be thrown");
        } catch (SecurityException ok) {
        } finally {
            System.setSecurityManager(null);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "certificates",
        args = {}
    )
    public void testCertificates() throws Exception {
        Identity i = new IdentityStub("iii");
        PublicKeyStub pk1 = new PublicKeyStub("kkk", "fff", null);        
        CertificateStub c1 = new CertificateStub("fff", null, null, pk1);
        CertificateStub c2 = new CertificateStub("zzz", null, null, pk1);
        i.addCertificate(c1);
        i.addCertificate(c2);
        java.security.Certificate[] s = i.certificates();
        assertEquals(2, s.length);
        assertTrue(c1.equals(s[0]) || c2.equals(s[0]));
        assertTrue(c1.equals(s[1]) || c2.equals(s[1]));
        s[0] = null;
        s[1] = null;
        s = i.certificates();
        assertEquals(2, s.length);
        assertTrue(c1.equals(s[0]) || c2.equals(s[0]));
        assertTrue(c1.equals(s[1]) || c2.equals(s[1]));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "identityEquals",
        args = {java.security.Identity.class}
    )
    public void testIdentityEquals() throws Exception {
        String name = "nnn";
        PublicKey pk = new PublicKeyStub("aaa", "fff", new byte[]{1,2,3,4,5});
        IdentityStub i = new IdentityStub(name);
        i.setPublicKey(pk);
        Object[] value = {
                new IdentityStub("111"), Boolean.FALSE,
                new IdentityStub(name), Boolean.FALSE,
                new IdentityStub(name, IdentityScope.getSystemScope()), Boolean.FALSE,
                i, Boolean.TRUE,
                new IdentityStub(name, pk), Boolean.TRUE                
        };
        for (int k=0; k<value.length; k+=2){
            assertEquals(value[k+1], new Boolean(i.identityEquals((Identity)value[k])));
            if (Boolean.TRUE.equals(value[k+1])) assertEquals(i.hashCode(), value[k].hashCode());
        }
        Identity i2 = IdentityScope.getSystemScope().getIdentity(name); 
        i2.setPublicKey(pk);        
        assertTrue(i.identityEquals(i2));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Method's returned value is not checked. SecurityException checking missed.",
        method = "toString",
        args = {boolean.class}
    )
    public void testToStringboolean() throws Exception {
        new IdentityStub("aaa").toString(false);
        new IdentityStub("aaa2", IdentityScope.getSystemScope()).toString(false);
        new IdentityStub("bbb").toString(true);
        new IdentityStub("bbb2", IdentityScope.getSystemScope()).toString(true);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getScope",
        args = {}
    )
    public void testGetScope() throws Exception {
       Identity i = new IdentityStub("testGetScope");
       assertNull(i.getScope());
       IdentityScope s = IdentityScope.getSystemScope();
       Identity i2 = new IdentityStub("testGetScope2", s);
       assertSame(s, i2.getScope());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "setPublicKey",
        args = {java.security.PublicKey.class}
    )
    public void testSetPublicKey1() throws Exception {
        MySecurityManager sm = new MySecurityManager();
        sm.denied.add(new SecurityPermission("setIdentityPublicKey"));
        System.setSecurityManager(sm);
        try {
            new IdentityStub("testSetPublicKey1").setPublicKey(new PublicKeyStub("kkk", "testSetPublicKey1", null));
            fail("SecurityException should be thrown");
        } catch (SecurityException ok) {
        } finally {
            System.setSecurityManager(null);
        }        
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "setPublicKey",
        args = {java.security.PublicKey.class}
    )
    public void testSetPublicKey2() throws Exception {
        Identity i2 = new IdentityStub("testSetPublicKey2_2", IdentityScope.getSystemScope());
        new PublicKeyStub("kkk", "testSetPublicKey2", new byte[]{1,2,3,4,5});
        try {
            i2.setPublicKey(null);
        } catch (KeyManagementException ok) {}
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "setPublicKey",
        args = {java.security.PublicKey.class}
    )
    public void testSetPublicKey4() throws Exception {
        Identity i = new IdentityStub("testSetPublicKey4");
        PublicKeyStub pk1 = new PublicKeyStub("kkk", "Identity.testSetPublicKey4", null);        
        CertificateStub c1 = new CertificateStub("fff", null, null, pk1);
        CertificateStub c2 = new CertificateStub("zzz", null, null, pk1);
        i.addCertificate(c1);
        i.addCertificate(c2);
        assertEquals(2, i.certificates().length);
        assertSame(pk1, i.getPublicKey());
        PublicKeyStub pk2 = new PublicKeyStub("zzz", "Identity.testSetPublicKey4", null);    
        i.setPublicKey(pk2);
        assertSame(pk2, i.getPublicKey());
        assertEquals(0, i.certificates().length);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getPublicKey",
        args = {}
    )
    public void testGetPublicKey() throws Exception {
        Identity i = new IdentityStub("testGetPublicKey");
        assertNull(i.getPublicKey());
        PublicKey pk = new PublicKeyStub("kkk", "Identity.testGetPublicKey", null);
        i.setPublicKey(pk);
        assertSame(pk, i.getPublicKey());        
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Just SecurityException verification",
        method = "setInfo",
        args = {java.lang.String.class}
    )
    public void testSetInfo() throws Exception {
        MySecurityManager sm = new MySecurityManager();
        sm.denied.add(new SecurityPermission("setIdentityInfo"));
        System.setSecurityManager(sm);
        try {
            new IdentityStub("testSetInfo").setInfo("some info");
            fail("SecurityException should be thrown");
        } catch (SecurityException ok) {
        } finally {
            System.setSecurityManager(null);
        }        
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Both method were verified",
            method = "getInfo",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Both method were verified",
            method = "setInfo",
            args = {java.lang.String.class}
        )
    })
    public void testGetInfo() {
        Identity i = new IdentityStub("testGetInfo");
        i.setInfo("some info");
        assertEquals("some info", i.getInfo());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getName",
        args = {}
    )
    public void testGetName() {
        Identity i = new IdentityStub("testGetName");
        assertEquals ("testGetName", i.getName());
    }
}
