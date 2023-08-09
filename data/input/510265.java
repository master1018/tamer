@TestTargetClass(Key.class)
public class KeyTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Field testing",
        method = "!serialVersionUID",
        args = {}
    )
    public void testField() {
        checkKey mk = new checkKey();
        assertEquals("Incorrect serialVersionUID", mk.getSerVerUID(), 
                6603384152749567654L);
    }
    public class checkKey implements Key {
        public String getAlgorithm() {
            return "Key";
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
