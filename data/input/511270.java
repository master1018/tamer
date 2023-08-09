public class DefaultManifestAttributesSdkTest extends DefaultManifestAttributesTest {
    protected String getPackageName() {
        return "com.android.cts.dpi";
    }
    public void testPackageHasExpectedSdkVersion() {
        assertEquals(Build.VERSION.SDK_INT, getAppInfo().targetSdkVersion);
    }
}
