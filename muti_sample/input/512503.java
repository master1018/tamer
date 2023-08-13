public class PackageManagerStressHostTests extends DeviceTestCase {
    private static final String LOG_TAG = "PackageManagerStressHostTests";
    private PackageManagerHostTestUtils mPMHostUtils = null;
    private static final String LARGE_APPS_DIRECTORY_NAME = "largeApps";
    private static final String MISC_APPS_DIRECTORY_NAME = "miscApps";
    private static final String VERSIONED_APPS_DIRECTORY_NAME = "versionedApps";
    private static final String MANY_APPS_DIRECTORY_NAME = "manyApps";
    private static String AppRepositoryPath = null;
    private static enum APK {
            FILENAME,
            PACKAGENAME;
    }
    private static final String[][] LARGE_APPS = {
       {"External1mb.apk", "com.appsonsd.mytests.External1mb"},
       {"External2mb.apk", "com.appsonsd.mytests.External2mb"},
       {"External3mb.apk", "com.appsonsd.mytests.External3mb"},
       {"External4mb.apk", "com.appsonsd.mytests.External4mb"},
       {"External5mb.apk", "com.appsonsd.mytests.External5mb"},
       {"External6mb.apk", "com.appsonsd.mytests.External6mb"},
       {"External7mb.apk", "com.appsonsd.mytests.External7mb"},
       {"External8mb.apk", "com.appsonsd.mytests.External8mb"},
       {"External9mb.apk", "com.appsonsd.mytests.External9mb"},
       {"External10mb.apk", "com.appsonsd.mytests.External10mb"},
       {"External16mb.apk", "com.appsonsd.mytests.External16mb"},
       {"External28mb.apk", "com.appsonsd.mytests.External28mb"},
       {"External34mb.apk", "com.appsonsd.mytests.External34mb"},
       {"External46mb.apk", "com.appsonsd.mytests.External46mb"},
       {"External58mb.apk", "com.appsonsd.mytests.External58mb"},
       {"External65mb.apk", "com.appsonsd.mytests.External65mb"},
       {"External72mb.apk", "com.appsonsd.mytests.External72mb"},
       {"External79mb.apk", "com.appsonsd.mytests.External79mb"},
       {"External86mb.apk", "com.appsonsd.mytests.External86mb"},
       {"External93mb.apk", "com.appsonsd.mytests.External93mb"}};
    private static final String AUTO_LOC_APK = "Auto241kb.apk";
    private static final String AUTO_LOC_PKG = "com.appsonsd.mytests.Auto241kb";
    private static final String INTERNAL_LOC_APK = "Internal781kb.apk";
    private static final String INTERNAL_LOC_PKG = "com.appsonsd.mytests.Internal781kb";
    private static final String EXTERNAL_LOC_APK = "External931kb.apk";
    private static final String EXTERNAL_LOC_PKG = "com.appsonsd.mytests.External931kb";
    private static final String NO_LOC_APK = "Internal751kb_EclairSDK.apk";
    private static final String NO_LOC_PKG = "com.appsonsd.mytests.Internal751kb_EclairSDK";
    private static final String VERSIONED_APPS_FILENAME_PREFIX = "External455kb_v";
    private static final String VERSIONED_APPS_PKG = "com.appsonsd.mytests.External455kb";
    private static final int VERSIONED_APPS_START_VERSION = 1;  
    private static final int VERSIONED_APPS_END_VERSION = 250;  
    private static final int MANY_APPS_START = 1;
    private static final int MANY_APPS_END = 100;
    private static final String MANY_APPS_PKG_PREFIX = "com.appsonsd.mytests.External49kb_";
    private static final String MANY_APPS_APK_PREFIX = "External49kb_";
    public static Test suite() {
        return new DeviceTestSuite(PackageManagerStressHostTests.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mPMHostUtils = new PackageManagerHostTestUtils(getDevice());
        AppRepositoryPath = System.getenv("ANDROID_TEST_APP_REPOSITORY");
        assertNotNull(AppRepositoryPath);
        if (!AppRepositoryPath.endsWith(File.separator)) {
            AppRepositoryPath += File.separator;
        }
    }
    private String getRepositoryTestAppFilePath(String fileDirectory, String fileName) {
        return String.format("%s%s%s%s", AppRepositoryPath, fileDirectory,
                File.separator, fileName);
    }
    public String getTestAppFilePath(String fileName) {
        return String.format("%s%s%s", getTestAppPath(), File.separator, fileName);
    }
    public void testUpdateAppManyTimesOnSD() throws IOException, InterruptedException {
        Log.i(LOG_TAG, "Test updating an app on SD numerous times");
        mPMHostUtils.uninstallApp(VERSIONED_APPS_PKG);
        assertFalse(mPMHostUtils.doesPackageExist(VERSIONED_APPS_PKG));
        try {
            for (int i = VERSIONED_APPS_START_VERSION; i <= VERSIONED_APPS_END_VERSION; ++i) {
                String currentApkName = String.format("%s%d.apk",
                        VERSIONED_APPS_FILENAME_PREFIX, i);
                Log.i(LOG_TAG, "Installing app " + currentApkName);
                mPMHostUtils.installFile(getRepositoryTestAppFilePath(VERSIONED_APPS_DIRECTORY_NAME,
                        currentApkName), true);
                mPMHostUtils.waitForPackageManager();
                assertTrue(mPMHostUtils.doesAppExistOnSDCard(VERSIONED_APPS_PKG));
                assertTrue(mPMHostUtils.doesPackageExist(VERSIONED_APPS_PKG));
            }
        }
        finally {
            mPMHostUtils.uninstallApp(VERSIONED_APPS_PKG);
            assertFalse(mPMHostUtils.doesPackageExist(VERSIONED_APPS_PKG));
        }
    }
    public void testUninstallReinstallAppOnSDManyTimes() throws IOException, InterruptedException {
        Log.i(LOG_TAG, "Test updating an app on the SD card stays on the SD card");
        mPMHostUtils.uninstallApp(EXTERNAL_LOC_PKG);
        assertFalse(mPMHostUtils.doesPackageExist(EXTERNAL_LOC_PKG));
        for (int i = 0; i <= 500; ++i) {
            Log.i(LOG_TAG, "Installing app");
            try {
                mPMHostUtils.installFile(getRepositoryTestAppFilePath(MISC_APPS_DIRECTORY_NAME,
                        EXTERNAL_LOC_APK), false);
                mPMHostUtils.waitForPackageManager();
                assertTrue(mPMHostUtils.doesAppExistOnSDCard(EXTERNAL_LOC_PKG));
                assertTrue(mPMHostUtils.doesPackageExist(EXTERNAL_LOC_PKG));
            }
            finally {
                Log.i(LOG_TAG, "Uninstalling app");
                mPMHostUtils.uninstallApp(EXTERNAL_LOC_PKG);
                mPMHostUtils.waitForPackageManager();
                assertFalse(mPMHostUtils.doesPackageExist(EXTERNAL_LOC_PKG));
            }
        }
    }
    public void testInstallManyLargeAppsOnSD() throws IOException, InterruptedException {
        Log.i(LOG_TAG, "Test installing 20 large apps onto the sd card");
        try {
            for (int i=0; i < LARGE_APPS.length; ++i) {
                String apkName = LARGE_APPS[i][APK.FILENAME.ordinal()];
                String pkgName = LARGE_APPS[i][APK.PACKAGENAME.ordinal()];
                mPMHostUtils.uninstallApp(pkgName);
                assertFalse(mPMHostUtils.doesPackageExist(pkgName));
                Log.i(LOG_TAG, "Installing app " + apkName);
                mPMHostUtils.installFile(getRepositoryTestAppFilePath(LARGE_APPS_DIRECTORY_NAME,
                        apkName), false);
                mPMHostUtils.waitForPackageManager();
                assertTrue(mPMHostUtils.doesAppExistOnSDCard(pkgName));
                assertTrue(mPMHostUtils.doesPackageExist(pkgName));
            }
        }
        finally {
            for (int i=0; i < LARGE_APPS.length; ++i) {
                String apkName = LARGE_APPS[i][APK.FILENAME.ordinal()];
                String pkgName = LARGE_APPS[i][APK.PACKAGENAME.ordinal()];
                Log.i(LOG_TAG, "Uninstalling app " + apkName);
                mPMHostUtils.uninstallApp(pkgName);
                assertFalse(mPMHostUtils.doesPackageExist(pkgName));
                assertFalse(mPMHostUtils.doesAppExistOnSDCard(pkgName));
            }
        }
    }
    public void testInstallManyAppsOnSD() throws IOException, InterruptedException {
        Log.i(LOG_TAG, "Test installing 500 small apps onto SD");
        try {
            for (int i = MANY_APPS_START; i <= MANY_APPS_END; ++i) {
                String currentPkgName = String.format("%s%d", MANY_APPS_PKG_PREFIX, i);
                mPMHostUtils.uninstallApp(currentPkgName);
                assertFalse(mPMHostUtils.doesPackageExist(currentPkgName));
                String currentApkName = String.format("%s%d.apk", MANY_APPS_APK_PREFIX, i);
                Log.i(LOG_TAG, "Installing app " + currentApkName);
                mPMHostUtils.installFile(getRepositoryTestAppFilePath(MANY_APPS_DIRECTORY_NAME,
                        currentApkName), true);
                mPMHostUtils.waitForPackageManager();
                assertTrue(mPMHostUtils.doesAppExistOnSDCard(currentPkgName));
                assertTrue(mPMHostUtils.doesPackageExist(currentPkgName));
            }
        }
        finally {
            for (int i = MANY_APPS_START; i <= MANY_APPS_END; ++i) {
                String currentPkgName = String.format("%s%d", MANY_APPS_PKG_PREFIX, i);
                mPMHostUtils.uninstallApp(currentPkgName);
                assertFalse(mPMHostUtils.doesPackageExist(currentPkgName));
            }
        }
    }
}