@TestTargetClass(SecureRandomSpi.class)
public class SecureRandomSpiTest extends TestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "SecureRandomSpi",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "engineGenerateSeed",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "engineNextBytes",
            args = {byte[].class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "engineSetSeed",
            args = {byte[].class}
        )
    })
    public void testSecureRandomSpi() {
        try {
            MySecureRandomSpi srs = new MySecureRandomSpi();
            assertTrue(srs instanceof SecureRandomSpi);
        } catch (Exception e) {
            fail("Unexpected exception");
        }
        try {
            MySecureRandomSpi srs = new MySecureRandomSpi();
            srs.engineGenerateSeed(10);
            srs.engineNextBytes(new byte[10]);
            srs.engineSetSeed(new byte[3]);
        } catch (Exception e) {
            fail("Unexpected exception");
        }
    }
    public class MySecureRandomSpi extends SecureRandomSpi {
        protected void engineSetSeed(byte[] seed) {}
        protected void engineNextBytes(byte[] bytes) {}
        protected byte[] engineGenerateSeed(int numBytes) {
            return null;
        }
    }
}