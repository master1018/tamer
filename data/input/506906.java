@TestTargetClass(DHPrivateKey.class)
public class DHPrivateKeyTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "tests serialVersionUID for a fixed value",
        method = "!field:serialVersionUID"
    )
    public void testField() {
        checkDHPrivateKey key = new checkDHPrivateKey();
        assertEquals("Incorrect serialVersionUID",
                key.getSerVerUID(), 
                2211791113380396553L);
    }
@TestTargets({
    @TestTargetNew(
          level = TestLevel.COMPLETE,
          method = "getX",
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
        DHPrivateKey pk1 = (DHPrivateKey) kp1.getPrivate();
        DHPrivateKey pk2 = (DHPrivateKey) kp2.getPrivate();
        assertTrue(pk1.getX().getClass().getCanonicalName().equals("java.math.BigInteger"));
        assertTrue(pk1.getParams().getClass().getCanonicalName().equals("javax.crypto.spec.DHParameterSpec"));
        assertFalse(pk1.equals(pk2));
        assertTrue(pk1.getX().equals(pk1.getX()));
    }
    public class checkDHPrivateKey implements DHPrivateKey {
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
        public BigInteger getX() {
            return null;
        }
        public DHParameterSpec getParams() {
            return null;
        }
    }
}
