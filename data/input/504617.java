@TestTargetClass(KeyFactorySpi.class)
public class KeyFactorySpiTest extends TestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "KeyFactorySpi",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "engineGeneratePrivate",
            args = {java.security.spec.KeySpec.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "engineGeneratePublic",
            args = {java.security.spec.KeySpec.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "engineTranslateKey",
            args = {java.security.Key.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "engineGetKeySpec",
            args = {java.security.Key.class, java.lang.Class.class}
        )
    })
    public void testKeyFactorySpi() {
        MyKeyFactorySpi keyFSpi = new MyKeyFactorySpi();
        assertTrue(keyFSpi instanceof KeyFactorySpi);
        KeySpec ks = new MyKeySpec();
        KeySpec kss = new MyKeySpec();
        try {
            keyFSpi.engineGeneratePrivate(ks);
            keyFSpi.engineGeneratePublic(ks);
            keyFSpi.engineGetKeySpec(null, java.lang.Class.class);
            keyFSpi.engineTranslateKey(null);
        } catch (Exception e) {
            fail("Unexpected exception");
        }
    }
    public class MyKeyFactorySpi extends KeyFactorySpi {
        protected PrivateKey engineGeneratePrivate(KeySpec keySpec){
            return null;
        }
        protected PublicKey engineGeneratePublic(KeySpec keySpec){
            return null;
        }
        protected KeySpec engineGetKeySpec(Key key, Class keySpec){
            return null;
        }
        protected Key engineTranslateKey(Key key){
            return null;
        }
    }
    class MyKeySpec implements KeySpec {}
}
