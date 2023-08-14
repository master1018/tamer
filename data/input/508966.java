@TestTargetClass(Build.VERSION.class)
public class BuildVersionTest extends TestCase {
    private static final String LOG_TAG = "BuildVersionTest";
    private static final String EXPECTED_RELEASE = "2.2";
    private static final String EXPECTED_SDK = "8";
    public void testReleaseVersion() {
        assertEquals(EXPECTED_RELEASE, Build.VERSION.RELEASE);
        assertEquals(EXPECTED_SDK, Build.VERSION.SDK);
    }
    public void testBuildFingerprint() {
        final String fingerprint = Build.FINGERPRINT;
        Log.i(LOG_TAG, String.format("Testing fingerprint %s", fingerprint));
        assertEquals("Build fingerprint must not include whitespace", -1,
                fingerprint.indexOf(' '));
        final String[] fingerprintSegs = fingerprint.split("/");
        assertEquals("Build fingerprint does not match expected format", 7, fingerprintSegs.length);
        assertEquals(Build.BRAND, fingerprintSegs[0]);
        assertEquals(Build.PRODUCT, fingerprintSegs[1]);
        assertEquals(Build.DEVICE, fingerprintSegs[2]);
        String[] bootloaderPlat = fingerprintSegs[3].split(":");
        assertEquals(Build.BOARD, bootloaderPlat[0]);
        assertEquals(Build.VERSION.RELEASE, bootloaderPlat[1]);
        assertEquals(Build.ID, fingerprintSegs[4]);
        assertTrue(fingerprintSegs[5].contains(":"));
    }
}
