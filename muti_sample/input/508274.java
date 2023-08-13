public class PackageManagerRequiringPermissionsTest extends AndroidTestCase {
    private static final String PACKAGE_NAME = "com.android.cts.stub";
    private PackageManager mPackageManager;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mPackageManager = getContext().getPackageManager();
        assertNotNull(mPackageManager);
    }
    public void testSetApplicationEnabledSetting() {
        try {
            mPackageManager.setApplicationEnabledSetting(PACKAGE_NAME,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP);
            fail("PackageManager.setApplicationEnabledSetting did not throw SecurityException as"
                    + "expected");
        } catch (SecurityException e) {
        }
    }
    public void testAddPackageToPreferred() {
        try {
            mPackageManager.addPackageToPreferred(null);
            fail("PackageManager.addPackageToPreferred did not throw SecurityException as"
                    + " expected");
        } catch (SecurityException e) {
        }
    }
    public void testRemovePackageFromPreferred() {
        try {
            mPackageManager.removePackageFromPreferred(null);
            fail("PackageManager.removePackageFromPreferred did not throw SecurityException as"
                    + "expected");
        } catch (SecurityException e) {
        }
    }
    public void testAddPreferredActivity() {
        try {
            mPackageManager.addPreferredActivity(null, 0, null, null);
            fail("PackageManager.addPreferredActivity did not throw" +
                    " SecurityException as expected");
        } catch (SecurityException e) {
        }
    }
    public void testClearPackagePreferredActivities() {
        try {
            mPackageManager.clearPackagePreferredActivities(null);
            fail("PackageManager.clearPackagePreferredActivities did not throw SecurityException"
                    + " as expected");
        } catch (SecurityException e) {
        }
    }
}
