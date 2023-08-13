@TestTargetClass(SignedObject.class)
public class SignedObjectTest extends TestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "SignedObject",
            args = {java.io.Serializable.class, java.security.PrivateKey.class, java.security.Signature.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getAlgorithm",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getObject",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getSignature",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "verify",
            args = {java.security.PublicKey.class, java.security.Signature.class}
        )
    })
    public void testSignedObject() throws Exception {
        TestKeyPair tkp = null;
        Properties prop;
        Signature sig = Signature.getInstance("SHA1withDSA");
        try {
            tkp = new TestKeyPair("DSA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return;
        }
        prop = new Properties();
        prop.put("aaa", "bbb");
        SignedObject so = new SignedObject(prop, tkp.getPrivate(), sig);
        assertEquals("SHA1withDSA", so.getAlgorithm());
        assertEquals(prop, so.getObject());
        assertTrue("verify() failed", so.verify(tkp.getPublic(), sig));
        assertNotNull("signature is null", so.getSignature());
    }
}
