@TestTargetClass(PBEKey.class)
public class PBEKeyTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "tests serialVersionUID for a fixed value",
        method = "!field:serialVersionUID"
    )    
    public void testField() {
        checkPBEKey key = new checkPBEKey();
        assertEquals("Incorrect serialVersionUID", 
                key.getSerVerUID(), 
                -1430015993304333921L);
    }
@TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getIterationCount",
        args = {}
      )
    public void test_getIterationCount() throws Exception {
        checkPBEKey key = new checkPBEKey();
        key.getIterationCount();
    }
@TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getPassword",
        args = {}
      )
    public void test_getPassword() throws Exception {
        checkPBEKey key = new checkPBEKey();
        key.getPassword();
    }
@TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getSalt",
        args = {}
      )
    public void test_getSalt() throws Exception {
        checkPBEKey key = new checkPBEKey();
        key.getSalt();
    }
    public class checkPBEKey implements PBEKey {
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
        public int getIterationCount() {
            return 0;
        }
        public byte[] getSalt() {
            return new byte[0];
        }
        public char[] getPassword() {
            return new char[0];
        }
    }
}
