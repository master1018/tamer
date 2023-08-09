@TestTargetClass(UnresolvedPermission.class)
public class UnresolvedPermissionTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "UnresolvedPermission",
        args = {java.lang.String.class, java.lang.String.class, java.lang.String.class, java.security.cert.Certificate[].class}
    )
    public void testCtor() {
        String type = "laskjhlsdk 2345346";
        String name = "^%#UHVKU^%V  887y";
        String action = "JHB ^%(*&T klj3h4";
        UnresolvedPermission up = new UnresolvedPermission(type, name, action,
                null);
        assertEquals(type, up.getName());
        assertEquals("", up.getActions());
        assertEquals("(unresolved " + type + " " + name + " " + action + ")",
                up.toString());
        up = new UnresolvedPermission(type, null, null, null);
        assertEquals(type, up.getName());
        assertEquals("", up.getActions());
        assertEquals("(unresolved " + type + " null null)", up.toString());
        up = new UnresolvedPermission(type, "", "",
                new java.security.cert.Certificate[0]);
        assertEquals(type, up.getName());
        assertEquals("", up.getActions());
        assertEquals("(unresolved " + type + "  )", up.toString());
        try {
            new UnresolvedPermission(null, name, action, null);
            fail("No expected NullPointerException");
        } catch (NullPointerException ok) {
        }
        up = new UnresolvedPermission("", "name", "action", null);
        assertEquals("", up.getName());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "implies",
        args = {java.security.Permission.class}
    )
    public void testImplies() {
        UnresolvedPermission up = new UnresolvedPermission(
                "java.security.SecurityPermission", "a.b.c", null, null);
        assertFalse(up.implies(up));
        assertFalse(up.implies(new AllPermission()));
        assertFalse(up.implies(new SecurityPermission("a.b.c")));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "!SerializationSelf",
        args = {}
    )
    public void testSerialization() throws Exception {
        UnresolvedPermission up = new UnresolvedPermission(
                "java.security.SecurityPermission", "a.b.c", "actions", null);
        assertEquals("java.security.SecurityPermission", up.getUnresolvedType());
        assertEquals("a.b.c", up.getUnresolvedName());
        assertEquals("actions", up.getUnresolvedActions());
        assertNull(up.getUnresolvedCerts());
        UnresolvedPermission deserializedUp = (UnresolvedPermission) SerializationTester
                .getDeserilizedObject(up);
        assertEquals("java.security.SecurityPermission", deserializedUp
                .getUnresolvedType());
        assertEquals("a.b.c", deserializedUp.getUnresolvedName());
        assertEquals("actions", deserializedUp.getUnresolvedActions());
        assertNull(deserializedUp.getUnresolvedCerts());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "!SerializationGolden",
        args = {}
    )
    public void testSerialization_Compatibility() throws Exception {
        UnresolvedPermission up = new UnresolvedPermission(
                "java.security.SecurityPermission", "a.b.c", "actions", null);
        assertEquals("java.security.SecurityPermission", up.getUnresolvedType());
        assertEquals("a.b.c", up.getUnresolvedName());
        assertEquals("actions", up.getUnresolvedActions());
        assertNull(up.getUnresolvedCerts());
        SerializationTest.verifyGolden(this, up, new SerializableAssert() {
            public void assertDeserialized(Serializable orig, Serializable ser) {
                UnresolvedPermission deserializedUp = (UnresolvedPermission) ser;
                assertEquals("java.security.SecurityPermission", deserializedUp
                        .getUnresolvedType());
                assertEquals("a.b.c", deserializedUp.getUnresolvedName());
                assertEquals("actions", deserializedUp.getUnresolvedActions());
                assertNull(deserializedUp.getUnresolvedCerts());
            }
        });
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "equals",
        args = {java.lang.Object.class}
    )
    public void testEquals() {
        UnresolvedPermission up1 = new UnresolvedPermission("type1", "name1",
                "action1", null);
        UnresolvedPermission up2 = new UnresolvedPermission("type1", "name1",
                "action1", null);
        UnresolvedPermission up3 = new UnresolvedPermission("type3", "name3",
                "action3", null);
        UnresolvedPermission up4 = null;
        assertTrue(up1.equals(up1));
        assertTrue(up2.equals(up2));
        assertTrue(up3.equals(up3));
        assertTrue(!up1.equals(null));
        assertTrue(!up2.equals(null));
        assertTrue(!up3.equals(null));
        assertTrue(up1.equals(up2));
        assertTrue(!up1.equals(up3));
        assertTrue(up2.equals(up1));
        assertTrue(!up2.equals(up3));
        assertTrue(!up3.equals(up1));
        assertTrue(!up3.equals(up2));
        try {
            assertTrue(up4.equals(up1));
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getActions",
        args = {}
    )
    public void testGetActions() {
        UnresolvedPermission up1 = new UnresolvedPermission("type1", "name1",
                "action1", null);
        UnresolvedPermission up2 = null;
        assertEquals("", up1.getActions());
        try {
            up2.getActions();
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getUnresolvedActions",
        args = {}
    )
    public void testGetUnresolvedActions() {
        UnresolvedPermission up1 = new UnresolvedPermission("type1", "name1",
                "action1 @#$%^&*", null);
        UnresolvedPermission up2 = null;
        assertEquals("action1 @#$%^&*", up1.getUnresolvedActions());
        try {
            up2.getUnresolvedActions();
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getUnresolvedCerts",
        args = {}
    )
    public void testGetUnresolvedCerts() {
        Certificate[] certificate = new java.security.cert.Certificate[] {
                new Certificate(null) {
                    @Override
                    public byte[] getEncoded()
                            throws CertificateEncodingException {
                        return null;
                    }
                    @Override
                    public PublicKey getPublicKey() {
                        return null;
                    }
                    @Override
                    public String toString() {
                        return null;
                    }
                    @Override
                    public void verify(PublicKey key)
                            throws CertificateException,
                            NoSuchAlgorithmException, InvalidKeyException,
                            NoSuchProviderException, SignatureException {
                    }
                    @Override
                    public void verify(PublicKey key, String sigProvider)
                            throws CertificateException,
                            NoSuchAlgorithmException, InvalidKeyException,
                            NoSuchProviderException, SignatureException {
                    }
                }
        };
        UnresolvedPermission up1 = new UnresolvedPermission("type1", "name1",
                "action1 @#$%^&*", null);
        UnresolvedPermission up2 = null;
        UnresolvedPermission up3 = new UnresolvedPermission("type3", "name3",
                "action3", certificate);
        assertNull(up1.getUnresolvedCerts());
        assertTrue(Arrays.equals(certificate, up3.getUnresolvedCerts()));
        try {
            up2.getUnresolvedCerts();
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getUnresolvedName",
        args = {}
    )
    public void testGetUnresolvedName() {
        UnresolvedPermission up1 = new UnresolvedPermission("type1", "name1!@#$%^&&* )(",
                "action1 @#$%^&*", null);
        UnresolvedPermission up2 = null;
        assertEquals("name1!@#$%^&&* )(", up1.getUnresolvedName());
        try {
            up2.getUnresolvedName();
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getUnresolvedType",
        args = {}
    )
    public void testGetUnresolvedType() {
        UnresolvedPermission up1 = new UnresolvedPermission("type1@#$%^&* )(", "name1",
                "action1", null);
        UnresolvedPermission up2 = null;
        assertEquals("type1@#$%^&* )(", up1.getUnresolvedType());
        try {
            up2.getUnresolvedType();
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "hashCode",
        args = {}
    )
    public void testHashCode() {
        UnresolvedPermission up1 = new UnresolvedPermission("type1", "name1",
                "action1", null);
        UnresolvedPermission up2 = new UnresolvedPermission("type1", "name1",
                "action1", null);
        UnresolvedPermission up3 = new UnresolvedPermission("type3", "name3",
                "action3", null);
        UnresolvedPermission up4 = null;
        assertTrue(up1.hashCode() == up2.hashCode());
        assertTrue(up1.hashCode() != up3.hashCode());
        assertTrue(up2.hashCode() != up3.hashCode());
        try {
            up4.hashCode();
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "newPermissionCollection",
        args = {}
    )
    public void testNewPermissionCollection() {
        UnresolvedPermission up1 = new UnresolvedPermission("type1", "name1",
                "action1", null);
        UnresolvedPermission up2 = new UnresolvedPermission("type1", "name1",
                "action1", null);
        UnresolvedPermission up3 = null;
        PermissionCollection pc = up1.newPermissionCollection();
        assertTrue(!pc.isReadOnly());
        pc.add(up1);
        pc.add(up2);
        Enumeration<Permission> permissions = pc.elements();
        assertNotNull(permissions);
        assertTrue("Should imply", !pc.implies(up1));
        assertTrue("Should not imply", !pc.implies(up3));
        try {
            up3.newPermissionCollection();
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "toString",
        args = {}
    )
    public void testToString() {
        UnresolvedPermission up1 = new UnresolvedPermission("type1", "name1",
                "action1", null);
        UnresolvedPermission up2 = new UnresolvedPermission("type1", "name1",
                "action1", null);
        UnresolvedPermission up3 = null;
        assertTrue(up1.toString().contains(""));
        assertTrue(up2.toString().contains(""));
        try {
            up3.toString();
            fail("NullPointerException expected");
        }catch (NullPointerException e) {
        }
    }
}
