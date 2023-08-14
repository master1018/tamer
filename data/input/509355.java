@TestTargetClass(PKCS8EncodedKeySpec.class)
public class PKCS8EncodedKeySpecTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "PKCS8EncodedKeySpec",
        args = {byte[].class}
    )
    public final void testPKCS8EncodedKeySpec() {
        byte[] encodedKey = new byte[] {(byte)1,(byte)2,(byte)3,(byte)4};
        EncodedKeySpec eks = new PKCS8EncodedKeySpec(encodedKey);
        assertTrue(eks instanceof PKCS8EncodedKeySpec);
        try {
            eks = new PKCS8EncodedKeySpec(null);
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
        PKCS8EncodedKeySpec meks = new PKCS8EncodedKeySpec(encodedKey);
        byte[] ek = meks.getEncoded();
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
        PKCS8EncodedKeySpec meks = new PKCS8EncodedKeySpec(encodedKey);
        assertEquals("PKCS#8", meks.getFormat());
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
        PKCS8EncodedKeySpec meks = new PKCS8EncodedKeySpec(encodedKeyCopy);
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
        PKCS8EncodedKeySpec meks = new PKCS8EncodedKeySpec(encodedKeyCopy);
        byte[] ek = meks.getEncoded();        
        ek[3] = (byte)5;
        byte[] ek1 = meks.getEncoded();
        assertTrue(Arrays.equals(encodedKey, ek1));
    }
}
