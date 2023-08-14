@TestTargetClass(KeyStore.SecretKeyEntry.class)
public class KSSecretKeyEntryTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "SecretKeyEntry",
        args = {javax.crypto.SecretKey.class}
    )
    public void testSecretKeyEntry() {
        SecretKey sk = null;
        try {
            new KeyStore.SecretKeyEntry(sk);
            fail("NullPointerException must be thrown when secretKey is null");
        } catch(NullPointerException e) {
        }
        sk = new tmpSecretKey();
        try {
            KeyStore.SecretKeyEntry ske = new KeyStore.SecretKeyEntry(sk);
            assertNotNull(ske);
            assertTrue(ske instanceof KeyStore.SecretKeyEntry);
        } catch(Exception e) {
            fail("Unexpected exception was thrown when secretKey is not null");
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getSecretKey",
        args = {}
    )
    public void testGetSecretKey() {
        SecretKey sk = new tmpSecretKey();
        KeyStore.SecretKeyEntry ske = new KeyStore.SecretKeyEntry(sk);
        assertEquals("Incorrect SecretKey", sk, ske.getSecretKey());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "toString",
        args = {}
    )
    public void testToString() {
        SecretKey sk = new tmpSecretKey();
        KeyStore.SecretKeyEntry ske = new KeyStore.SecretKeyEntry(sk);
        assertNotNull("toString() returns null string", ske.toString());
    }
}
class tmpSecretKey implements SecretKey {
    public String getAlgorithm() {
        return "My algorithm";
    }
    public String getFormat() {
        return "My Format";
    }
    public byte[] getEncoded() {
        return new byte[1];
    }
}
