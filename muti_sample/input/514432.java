@TestTargetClass(PackageInfo.class)
public class PackageInfoTest extends AndroidTestCase {
    private PackageManager mPackageManager;
    private PackageInfo mPackageInfo;
    private PackageInfo mPackageInfoCmp;
    private static final String PACKAGE_NAME = "com.android.cts.stub";
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mPackageManager = getContext().getPackageManager();
        mPackageInfo = mPackageManager.getPackageInfo(PACKAGE_NAME, PackageManager.GET_ACTIVITIES
                | PackageManager.GET_GIDS | PackageManager.GET_CONFIGURATIONS
                | PackageManager.GET_INSTRUMENTATION | PackageManager.GET_PERMISSIONS
                | PackageManager.GET_PROVIDERS | PackageManager.GET_RECEIVERS
                | PackageManager.GET_SERVICES | PackageManager.GET_SIGNATURES
                | PackageManager.GET_UNINSTALLED_PACKAGES);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test describeContents",
            method = "describeContents",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test writeToParcel",
            method = "writeToParcel",
            args = {android.os.Parcel.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test toString",
            method = "toString",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test PackageInfo",
            method = "PackageInfo",
            args = {}
        )
    })
    public void testPackageInfoOp() {
        new PackageInfo();
        assertEquals(0, mPackageInfo.describeContents());
        assertNotNull(mPackageInfo.toString());
        Parcel p = Parcel.obtain();
        mPackageInfo.writeToParcel(p, 0);
        p.setDataPosition(0);
        mPackageInfoCmp = PackageInfo.CREATOR.createFromParcel(p);
        checkPkgInfoSame(mPackageInfo, mPackageInfoCmp);
        p.recycle();
    }
    private void checkPkgInfoSame(PackageInfo expected, PackageInfo actual) {
        assertEquals(expected.packageName, actual.packageName);
        assertEquals(expected.versionCode, actual.versionCode);
        assertEquals(expected.versionName, actual.versionName);
        assertEquals(expected.sharedUserId, actual.sharedUserId);
        assertEquals(expected.sharedUserLabel, actual.sharedUserLabel);
        if (expected.applicationInfo != null) {
            assertNotNull(actual.applicationInfo);
            checkAppInfo(expected.applicationInfo, actual.applicationInfo);
        } else {
            assertNull(actual.applicationInfo);
        }
        assertTrue(Arrays.equals(expected.gids, actual.gids));
        checkInfoArray(expected.activities, actual.activities);
        checkInfoArray(expected.receivers, actual.receivers);
        checkInfoArray(expected.services, actual.services);
        checkInfoArray(expected.providers, actual.providers);
        checkInfoArray(expected.instrumentation, actual.instrumentation);
        checkInfoArray(expected.permissions, actual.permissions);
        assertTrue(Arrays.equals(expected.requestedPermissions, actual.requestedPermissions));
        checkSignatureInfo(expected.signatures, actual.signatures);
        checkConfigInfo(expected.configPreferences, actual.configPreferences);
    }
    private void checkAppInfo(ApplicationInfo expected, ApplicationInfo actual) {
        assertEquals(expected.taskAffinity, actual.taskAffinity);
        assertEquals(expected.permission, actual.permission);
        assertEquals(expected.processName, actual.processName);
        assertEquals(expected.className, actual.className);
        assertEquals(expected.theme, actual.theme);
        assertEquals(expected.flags, actual.flags);
        assertEquals(expected.sourceDir, actual.sourceDir);
        assertEquals(expected.publicSourceDir, actual.publicSourceDir);
        assertEquals(expected.sharedLibraryFiles, actual.sharedLibraryFiles);
        assertEquals(expected.dataDir, actual.dataDir);
        assertEquals(expected.uid, actual.uid);
        assertEquals(expected.enabled, actual.enabled);
        assertEquals(expected.manageSpaceActivityName, actual.manageSpaceActivityName);
        assertEquals(expected.descriptionRes, actual.descriptionRes);
    }
    private void checkInfoArray(PackageItemInfo[] expected, PackageItemInfo[] actual) {
        if (expected != null && expected.length > 0) {
            assertNotNull(actual);
            assertEquals(expected.length, actual.length);
            for (int i = 0; i < expected.length; i++) {
                assertEquals(expected[i].name, actual[i].name);
            }
        } else if (expected == null) {
            assertNull(actual);
        } else {
            assertEquals(0, actual.length);
        }
    }
    private void checkSignatureInfo(Signature[] expected, Signature[] actual) {
        if (expected != null && expected.length > 0) {
            assertNotNull(actual);
            assertEquals(expected.length, actual.length);
            for (int i = 0; i < expected.length; i++) {
                actual[i].equals(expected[i]);
            }
        } else if (expected == null) {
            assertNull(actual);
        } else {
            assertEquals(0, actual.length);
        }
    }
    private void checkConfigInfo(ConfigurationInfo[] expected, ConfigurationInfo[] actual) {
        if (expected != null && expected.length > 0) {
            assertNotNull(actual);
            assertEquals(expected.length, actual.length);
            for (int i = 0; i < expected.length; i++) {
                assertEquals(expected[i].reqKeyboardType, actual[i].reqKeyboardType);
                assertEquals(expected[i].reqTouchScreen, actual[i].reqTouchScreen);
                assertEquals(expected[i].reqInputFeatures, actual[i].reqInputFeatures);
                assertEquals(expected[i].reqNavigation, actual[i].reqNavigation);
            }
        } else if (expected == null) {
            assertNull(actual);
        } else {
            assertEquals(0, actual.length);
        }
    }
}
