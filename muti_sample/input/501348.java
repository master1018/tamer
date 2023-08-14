@TestTargetClass( KeyPair.class)
public class KeyPairTest extends TestCase {
    private static class TestKeyPair {
        static PublicKey getPublic() {
            return new PublicKey() {
                public String getAlgorithm() {
                    return "never mind";
                }
                public String getFormat() {
                    return "never mind";
                }
                public byte[] getEncoded() {
                    return null;
                }                
            };
        }
        static PrivateKey getPrivate() {
            return new PrivateKey() {
                public String getAlgorithm() {
                    return "never mind";
                }
                public String getFormat() {
                    return "never mind";
                }
                public byte[] getEncoded() {
                    return null;
                }                
            };                
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verification when parameter is null",
        method = "KeyPair",
        args = {java.security.PublicKey.class, java.security.PrivateKey.class}
    )
    public final void testKeyPair01() {
        Object kp = new KeyPair(null, null);
        assertTrue(kp instanceof KeyPair);
        kp = new KeyPair(null, TestKeyPair.getPrivate());
        assertTrue(kp instanceof KeyPair);
        kp = new KeyPair(TestKeyPair.getPublic(), null);
        assertTrue(kp instanceof KeyPair);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verification when parameter is not null",
        method = "KeyPair",
        args = {java.security.PublicKey.class, java.security.PrivateKey.class}
    )
    public final void testKeyPair02() throws InvalidKeySpecException {
        Object kp = new KeyPair(TestKeyPair.getPublic(), TestKeyPair.getPrivate());
        assertTrue(kp instanceof KeyPair);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "getPrivate",
        args = {}
    )
    public final void testGetPrivate01() {
        KeyPair kp = new KeyPair(null, null);
        assertNull(kp.getPrivate());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "getPrivate",
        args = {}
    )
    public final void testGetPrivate02() throws InvalidKeySpecException {
        PrivateKey pk = TestKeyPair.getPrivate();
        KeyPair kp = new KeyPair(null, pk);
        assertSame(pk, kp.getPrivate());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "getPublic",
        args = {}
    )
    public final void testGetPublic01() {
        KeyPair kp = new KeyPair(null, null);
        assertNull(kp.getPublic());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "getPublic",
        args = {}
    )
    public final void testGetPublic02() throws InvalidKeySpecException {
        PublicKey pk = TestKeyPair.getPublic();
        KeyPair kp = new KeyPair(pk, null);
        assertSame(pk, kp.getPublic());
    }
}
