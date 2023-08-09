@TestTargetClass(PublicKey.class)
public class PublicKeyTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Field testing",
        method = "!serialVersionUID",
        args = {}
    )
    public void testField() {
        checkPublicKey cPK = new checkPublicKey();
        assertEquals("Incorrect serialVersionUID", cPK.getSerVerUID(), 
                7187392471159151072L);
    }
    public class checkPublicKey implements PublicKey {
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
