public class PmPermissionsTests extends AndroidTestCase {
    private PackageManager mPm;
    private String mPkgName = "com.android.framework.permission.tests";
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mPm = getContext().getPackageManager();
    }
    @SmallTest
    public void testGetPackageSize() {
        try {
            mPm.getPackageSizeInfo(mPkgName, null);
            fail("PackageManager.getPackageSizeInfo" +
                    "did not throw SecurityException as expected");
        } catch (SecurityException e) {
        }
    }
    @SmallTest
    public void testDeleteApplicationCacheFiles() {
        try {
            mPm.deleteApplicationCacheFiles(mPkgName, null);
            fail("PackageManager.deleteApplicationCacheFiles" +
                    "did not throw SecurityException as expected");
        } catch (SecurityException e) {
        }
    }
    @SmallTest
    public void testInstallPackage() {
        try {
            mPm.installPackage(null, null, 0, null);
            fail("PackageManager.installPackage" +
                    "did not throw SecurityException as expected");
        } catch (SecurityException e) {
        }
    }
    @SmallTest
    public void testFreeStorage1() {
        try {
            mPm.freeStorage(100000, null);
            fail("PackageManager.freeStorage " +
                   "did not throw SecurityException as expected");
        } catch (SecurityException e) {
        }
    }
    @SmallTest
    public void testFreeStorage2() {
        try {
            mPm.freeStorageAndNotify(100000, null);
            fail("PackageManager.freeStorageAndNotify" +
                    " did not throw SecurityException as expected");
        } catch (SecurityException e) {
        }
    }
    @SmallTest
    public void testClearApplicationUserData() {
        try {
            mPm.clearApplicationUserData(mPkgName, null);
            fail("PackageManager.clearApplicationUserData" +
                    "did not throw SecurityException as expected");
        } catch (SecurityException e) {
        }
    }
    @SmallTest
    public void testDeletePackage() {
        try {
            mPm.deletePackage(mPkgName, null, 0);
            fail("PackageManager.deletePackage" +
                   "did not throw SecurityException as expected");
        } catch (SecurityException e) {
        }
    }
}