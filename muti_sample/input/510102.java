@TestTargetClass(PrivateKey.class)
public class PrivateKeyTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Field testing",
        method = "!serialVersionUID",
        args = {}
    )
    public void testField() {
        checkPrivateKey cPrKey = new checkPrivateKey(); 
        assertEquals("Incorrect serialVersionUID", cPrKey.getSerVerUID(), 
                6034044314589513430L);
    }
    public class checkPrivateKey implements PrivateKey {
        public String getAlgorithm() {
            return "PublicKey";
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
    }
}
