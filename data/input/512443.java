@TestTargetClass(DHPublicKey.class)
public class DHPublicKeyTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "tests serialVersionUID for a fixed value",
        method = "!field:serialVersionUID"
    )    
    public void testField() {
        checkDHPublicKey key = new checkDHPublicKey();
        assertEquals("Incorrect serialVersionUID",
                key.getSerVerUID(), 
                -6628103563352519193L);
    }
@TestTargets({
    @TestTargetNew(
          level = TestLevel.COMPLETE,
          method = "getY",
          args = {}
        ),
    @TestTargetNew(
          level = TestLevel.COMPLETE,
          clazz = DHKey.class,
          method = "getParams",
          args = {}
        )
    })
    @BrokenTest("Too slow - disabling for now")
    public void test_getParams() throws Exception {
        KeyPairGenerator kg = KeyPairGenerator.getInstance("DH");
        kg.initialize(1024);
        KeyPair kp1 = kg.genKeyPair();
        KeyPair kp2 = kg.genKeyPair();
        DHPublicKey pk1 = (DHPublicKey) kp1.getPublic();
        DHPublicKey pk2 = (DHPublicKey) kp2.getPublic();
        assertTrue(pk1.getY().getClass().getCanonicalName().equals("java.math.BigInteger"));
        assertTrue(pk2.getParams().getClass().getCanonicalName().equals("javax.crypto.spec.DHParameterSpec"));
        assertFalse(pk1.equals(pk2));
        assertTrue(pk1.getY().equals(pk1.getY()));
    }
    public class checkDHPublicKey implements DHPublicKey {
        public String getAlgorithm() {
            return "SecretKey";
        }
        public String getFormat() {
            return "Format";
        }
        public byte[] getEncoded() {
            return new byte[0];
        }
        public long getSerVerUID() {
            return serialVersionUID;
        }
        public BigInteger getY() {
            return null;
        }
        public DHParameterSpec getParams() {
            return null;
        }
    }
}
