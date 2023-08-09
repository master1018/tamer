public class DefaultManifestAttributesCupcakeTest extends DefaultManifestAttributesTest {
    protected String getPackageName() {
        return "com.android.cts.dpi2";
    }
    public void testPackageHasExpectedSdkVersion() {
        assertEquals(3, getAppInfo().targetSdkVersion);
    }
}
