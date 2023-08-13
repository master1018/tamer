@TestTargetClass(EncodedKeySpec.class)
public class EncodedKeySpecTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "EncodedKeySpec",
        args = {byte[].class}
    )
    public final void testEncodedKeySpec() {
        byte[] encodedKey = new byte[] { (byte) 1, (byte) 2, (byte) 3, (byte) 4 };
        EncodedKeySpec eks = new MyEncodedKeySpec(encodedKey);
        assertTrue("wrong encoded key was returned", Arrays.equals(encodedKey,
                eks.getEncoded()));
        assertEquals("wrong name of encoding format", "My", eks.getFormat());
        encodedKey = null;
        try {
            eks = new MyEncodedKeySpec(encodedKey);
            fail("expected NullPointerException");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getEncoded",
        args = {}
    )
    public final void testGetEncoded() {
        byte[] encodedKey = new byte[] { (byte) 1, (byte) 2, (byte) 3, (byte) 4 };
        EncodedKeySpec meks = new MyEncodedKeySpec(encodedKey);
        byte[] ek = meks.getEncoded();
        boolean result = true;
        for (int i = 0; i < encodedKey.length; i++) {
            if (encodedKey[i] != ek[i]) {
                result = false;
            }
        }
        assertTrue(result);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "getEncoded",
        args = {}
    )
    public final void testIsStatePreserved1() {
        byte[] encodedKey = new byte[] { (byte) 1, (byte) 2, (byte) 3, (byte) 4 };
        EncodedKeySpec meks = new MyEncodedKeySpec(encodedKey);
        encodedKey[3] = (byte) 5;
        byte[] ek = meks.getEncoded();
        assertTrue(ek[3] == (byte) 4);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "getEncoded",
        args = {}
    )
    public final void testIsStatePreserved2() {
        byte[] encodedKey = new byte[] { (byte) 1, (byte) 2, (byte) 3, (byte) 4 };
        EncodedKeySpec meks = new MyEncodedKeySpec(encodedKey);
        byte[] ek = meks.getEncoded();
        ek[3] = (byte) 5;
        byte[] ek1 = meks.getEncoded();
        assertTrue(ek1[3] == (byte) 4);
    }
}
