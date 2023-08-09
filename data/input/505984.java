@TestTargetClass(X509EncodedKeySpec.class)
public class X509EncodedKeySpecTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "X509EncodedKeySpec",
        args = {byte[].class}
    )
    public final void testX509EncodedKeySpec() {
        byte[] encodedKey = new byte[] {(byte)1,(byte)2,(byte)3,(byte)4};
        EncodedKeySpec eks = new X509EncodedKeySpec(encodedKey);
        assertTrue(eks instanceof X509EncodedKeySpec);
        try {
            eks = new X509EncodedKeySpec(null);
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
        byte[] encodedKey = new byte[] {(byte)1,(byte)2,(byte)3,(byte)4};
        X509EncodedKeySpec eks = new X509EncodedKeySpec(encodedKey);
        byte[] ek = eks.getEncoded();
        assertTrue(Arrays.equals(encodedKey, ek));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getFormat",
        args = {}
    )
    public final void testGetFormat() {
        byte[] encodedKey = new byte[] {(byte)1,(byte)2,(byte)3,(byte)4};
        X509EncodedKeySpec meks = new X509EncodedKeySpec(encodedKey);
        assertEquals("X.509", meks.getFormat());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "getEncoded",
        args = {}
    )
    public final void testIsStatePreserved1() {
        byte[] encodedKey = new byte[] {(byte)1,(byte)2,(byte)3,(byte)4};
        byte[] encodedKeyCopy = encodedKey.clone();
        X509EncodedKeySpec meks = new X509EncodedKeySpec(encodedKeyCopy);
        encodedKeyCopy[3] = (byte)5;
        byte[] ek = meks.getEncoded();
        assertTrue(Arrays.equals(encodedKey, ek));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "getEncoded",
        args = {}
    )
    public final void testIsStatePreserved2() {
        byte[] encodedKey = new byte[] {(byte)1,(byte)2,(byte)3,(byte)4};
        byte[] encodedKeyCopy = encodedKey.clone();
        X509EncodedKeySpec meks = new X509EncodedKeySpec(encodedKeyCopy);
        byte[] ek = meks.getEncoded();        
        ek[3] = (byte)5;
        byte[] ek1 = meks.getEncoded();
        assertTrue(Arrays.equals(encodedKey, ek1));
    }
}
