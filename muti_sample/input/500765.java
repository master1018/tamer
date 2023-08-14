public abstract class DefaultManifestAttributesTest extends AndroidTestCase {
    protected String packageName;
    private boolean expectedResult;
    private ApplicationInfo appInfo;
    protected ApplicationInfo getAppInfo() {
        return appInfo;
    }
    protected abstract String getPackageName();
    protected void setUp() {
        packageName = getPackageName();
        PackageManager pm = getContext().getPackageManager();
        try {
            appInfo = pm.getApplicationInfo(packageName, 0);
            if (appInfo.targetSdkVersion <= 3) {
                expectedResult = false;
            } else {
                expectedResult = true;
            }
        } catch (NameNotFoundException e) {
            fail("Should be able to find application info for this package");
        }
    }
    public void testSmallScreenDefault() {
        assertEquals(expectedResult,
                     (getAppInfo().flags & ApplicationInfo.FLAG_SUPPORTS_SMALL_SCREENS) != 0);
    }
    public void testNormalScreenDefault() {
        assertEquals(true,
                    (getAppInfo().flags & ApplicationInfo.FLAG_SUPPORTS_NORMAL_SCREENS) != 0);
    }
    public void testLargeScreenDefault() {
        assertEquals(expectedResult,
                    (getAppInfo().flags & ApplicationInfo.FLAG_SUPPORTS_LARGE_SCREENS) != 0);
    }
    public void testResizableDefault() {
        assertEquals(expectedResult,
                    (getAppInfo().flags & ApplicationInfo.FLAG_RESIZEABLE_FOR_SCREENS) != 0);
    }
    public void testAnyDensityDefault() {
        assertEquals(expectedResult,
                    (getAppInfo().flags & ApplicationInfo.FLAG_SUPPORTS_SCREEN_DENSITIES) != 0);
    }
}
