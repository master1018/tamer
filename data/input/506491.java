public class PackageManagerHostTests extends DeviceTestCase {
    private static final String LOG_TAG = "PackageManagerHostTests";
    private PackageManagerHostTestUtils mPMHostUtils = null;
    private String appPrivatePath = null;
    private String deviceAppPath = null;
    private String sdcardAppPath = null;
    private static final String SIMPLE_APK = "SimpleTestApp.apk";
    private static final String SIMPLE_PKG = "com.android.framework.simpletestapp";
    private static final String AUTO_LOC_APK = "AutoLocTestApp.apk";
    private static final String AUTO_LOC_PKG = "com.android.framework.autoloctestapp";
    private static final String INTERNAL_LOC_APK = "InternalLocTestApp.apk";
    private static final String INTERNAL_LOC_PKG = "com.android.framework.internalloctestapp";
    private static final String EXTERNAL_LOC_APK = "ExternalLocTestApp.apk";
    private static final String EXTERNAL_LOC_PKG = "com.android.framework.externalloctestapp";
    private static final String AUTO_LOC_VERSION_V1_APK = "AutoLocVersionedTestApp_v1.apk";
    private static final String AUTO_LOC_VERSION_V2_APK = "AutoLocVersionedTestApp_v2.apk";
    private static final String AUTO_LOC_VERSION_PKG =
            "com.android.framework.autolocversionedtestapp";
    private static final String EXTERNAL_LOC_VERSION_V1_APK = "ExternalLocVersionedTestApp_v1.apk";
    private static final String EXTERNAL_LOC_VERSION_V2_APK = "ExternalLocVersionedTestApp_v2.apk";
    private static final String EXTERNAL_LOC_VERSION_PKG =
            "com.android.framework.externallocversionedtestapp";
    private static final String NO_LOC_VERSION_V1_APK = "NoLocVersionedTestApp_v1.apk";
    private static final String NO_LOC_VERSION_V2_APK = "NoLocVersionedTestApp_v2.apk";
    private static final String NO_LOC_VERSION_PKG =
            "com.android.framework.nolocversionedtestapp";
    private static final String NO_LOC_APK = "NoLocTestApp.apk";
    private static final String NO_LOC_PKG = "com.android.framework.noloctestapp";
    private static final String UPDATE_EXTERNAL_LOC_V1_EXT_APK
            = "UpdateExternalLocTestApp_v1_ext.apk";
    private static final String UPDATE_EXTERNAL_LOC_V2_NONE_APK
            = "UpdateExternalLocTestApp_v2_none.apk";
    private static final String UPDATE_EXTERNAL_LOC_PKG
            = "com.android.framework.updateexternalloctestapp";
    private static final String UPDATE_EXT_TO_INT_LOC_V1_EXT_APK
            = "UpdateExtToIntLocTestApp_v1_ext.apk";
    private static final String UPDATE_EXT_TO_INT_LOC_V2_INT_APK
            = "UpdateExtToIntLocTestApp_v2_int.apk";
    private static final String UPDATE_EXT_TO_INT_LOC_PKG
            = "com.android.framework.updateexttointloctestapp";
    private static final String FL_PERMS_APK = "ExternalLocPermsFLTestApp.apk";
    private static final String FL_PERMS_PKG = "com.android.framework.externallocpermsfltestapp";
    private static final String ALL_PERMS_APK = "ExternalLocAllPermsTestApp.apk";
    private static final String ALL_PERMS_PKG = "com.android.framework.externallocallpermstestapp";
    private static final String VERSATILE_LOC_PKG = "com.android.framework.versatiletestapp";
    private static final String VERSATILE_LOC_INTERNAL_APK = "VersatileTestApp_Internal.apk";
    private static final String VERSATILE_LOC_EXTERNAL_APK = "VersatileTestApp_External.apk";
    private static final String VERSATILE_LOC_AUTO_APK = "VersatileTestApp_Auto.apk";
    private static final String VERSATILE_LOC_NONE_APK = "VersatileTestApp_None.apk";
    private static final String SHARED_PERMS_APK = "ExternalSharedPermsTestApp.apk";
    private static final String SHARED_PERMS_PKG
            = "com.android.framework.externalsharedpermstestapp";
    private static final String SHARED_PERMS_FL_APK = "ExternalSharedPermsFLTestApp.apk";
    private static final String SHARED_PERMS_FL_PKG
            = "com.android.framework.externalsharedpermsfltestapp";
    private static final String SHARED_PERMS_BT_APK = "ExternalSharedPermsBTTestApp.apk";
    private static final String SHARED_PERMS_BT_PKG
            = "com.android.framework.externalsharedpermsbttestapp";
    private static final String SHARED_PERMS_DIFF_KEY_APK = "ExternalSharedPermsDiffKeyTestApp.apk";
    private static final String SHARED_PERMS_DIFF_KEY_PKG
            = "com.android.framework.externalsharedpermsdiffkeytestapp";
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        assertNotNull(getTestAppPath());
        mPMHostUtils = new PackageManagerHostTestUtils(getDevice());
        appPrivatePath = mPMHostUtils.getAppPrivatePath();
        deviceAppPath = mPMHostUtils.getDeviceAppPath();
        sdcardAppPath = mPMHostUtils.getSDCardAppPath();
        mPMHostUtils.setDevicePreferredInstallLocation(
                PackageManagerHostTestUtils.InstallLocPreference.AUTO);
    }
    public String getTestAppFilePath(String fileName) {
        return String.format("%s%s%s", getTestAppPath(), File.separator, fileName);
    }
    public static Test suite() {
        return new DeviceTestSuite(PackageManagerHostTests.class);
    }
    public void testPushAppPrivate() throws IOException, InterruptedException {
        Log.i(LOG_TAG, "testing pushing an apk to /data/app-private");
        final String apkAppPrivatePath =  appPrivatePath + SIMPLE_APK;
        getDevice().uninstallPackage(SIMPLE_PKG);
        mPMHostUtils.executeShellCommand("stop");
        mPMHostUtils.pushFile(getTestAppFilePath(SIMPLE_APK), apkAppPrivatePath);
        assertTrue(mPMHostUtils.doesRemoteFileExist(apkAppPrivatePath));
        mPMHostUtils.executeShellCommand("start");
        mPMHostUtils.waitForPackageManager();
        assertFalse(mPMHostUtils.doesPackageExist(SIMPLE_PKG));
        assertFalse(mPMHostUtils.doesRemoteFileExist(apkAppPrivatePath));
    }
    private void doStandardInstall(String apkName, String pkgName,
            PackageManagerHostTestUtils.InstallLocation expectedLocation)
            throws IOException, InterruptedException {
        if (expectedLocation == PackageManagerHostTestUtils.InstallLocation.DEVICE) {
            mPMHostUtils.installAppAndVerifyExistsOnDevice(
                    getTestAppFilePath(apkName), pkgName, false);
        }
        else {
            mPMHostUtils.installAppAndVerifyExistsOnSDCard(
                    getTestAppFilePath(apkName), pkgName, false);
        }
    }
    public void installAppAutoLoc(PackageManagerHostTestUtils.InstallLocPreference preference,
            PackageManagerHostTestUtils.InstallLocation expectedLocation)
            throws IOException, InterruptedException {
        PackageManagerHostTestUtils.InstallLocPreference savedPref =
                PackageManagerHostTestUtils.InstallLocPreference.AUTO;
        try {
            savedPref = mPMHostUtils.getDevicePreferredInstallLocation();
            mPMHostUtils.setDevicePreferredInstallLocation(preference);
            doStandardInstall(AUTO_LOC_APK, AUTO_LOC_PKG, expectedLocation);
        }
        finally {
            mPMHostUtils.setDevicePreferredInstallLocation(savedPref);
            mPMHostUtils.uninstallApp(AUTO_LOC_PKG);
        }
    }
    public void testInstallAppAutoLocPrefIsAuto() throws IOException, InterruptedException {
        Log.i(LOG_TAG, "Test installLocation=auto, prefer=auto gets installed on device");
        installAppAutoLoc(PackageManagerHostTestUtils.InstallLocPreference.AUTO,
                PackageManagerHostTestUtils.InstallLocation.DEVICE);
    }
    public void testInstallAppAutoLocPrefIsInternal() throws IOException, InterruptedException {
        Log.i(LOG_TAG, "Test installLocation=auto, prefer=internal gets installed on device");
        installAppAutoLoc(PackageManagerHostTestUtils.InstallLocPreference.INTERNAL,
                PackageManagerHostTestUtils.InstallLocation.DEVICE);
    }
    public void testInstallAppAutoLocPrefIsExternal() throws IOException, InterruptedException {
        Log.i(LOG_TAG, "Test installLocation=auto, prefer=external gets installed on device");
        installAppAutoLoc(PackageManagerHostTestUtils.InstallLocPreference.EXTERNAL,
                PackageManagerHostTestUtils.InstallLocation.DEVICE);
    }
    public void installAppInternalLoc(PackageManagerHostTestUtils.InstallLocPreference preference,
            PackageManagerHostTestUtils.InstallLocation expectedLocation)
            throws IOException, InterruptedException {
        PackageManagerHostTestUtils.InstallLocPreference savedPref =
            PackageManagerHostTestUtils.InstallLocPreference.AUTO;
        try {
            savedPref = mPMHostUtils.getDevicePreferredInstallLocation();
            mPMHostUtils.setDevicePreferredInstallLocation(preference);
            doStandardInstall(INTERNAL_LOC_APK, INTERNAL_LOC_PKG, expectedLocation);
        }
        finally {
            mPMHostUtils.setDevicePreferredInstallLocation(savedPref);
            mPMHostUtils.uninstallApp(INTERNAL_LOC_PKG);
        }
    }
    public void testInstallAppInternalLocPrefIsAuto() throws IOException, InterruptedException {
        Log.i(LOG_TAG, "Test installLocation=internal, prefer=auto gets installed on device");
        installAppInternalLoc(PackageManagerHostTestUtils.InstallLocPreference.AUTO,
                PackageManagerHostTestUtils.InstallLocation.DEVICE);
    }
    public void testInstallAppInternalLocPrefIsInternal() throws IOException, InterruptedException {
        Log.i(LOG_TAG, "Test installLocation=internal, prefer=internal is installed on device");
        installAppInternalLoc(PackageManagerHostTestUtils.InstallLocPreference.INTERNAL,
                PackageManagerHostTestUtils.InstallLocation.DEVICE);
    }
    public void testInstallAppInternalLocPrefIsExternal() throws IOException, InterruptedException {
        Log.i(LOG_TAG, "Test installLocation=internal, prefer=external is installed on device");
        installAppInternalLoc(PackageManagerHostTestUtils.InstallLocPreference.EXTERNAL,
                PackageManagerHostTestUtils.InstallLocation.DEVICE);
    }
    public void installAppExternalLoc(PackageManagerHostTestUtils.InstallLocPreference preference,
            PackageManagerHostTestUtils.InstallLocation expectedLocation)
            throws IOException, InterruptedException {
        PackageManagerHostTestUtils.InstallLocPreference savedPref =
            PackageManagerHostTestUtils.InstallLocPreference.AUTO;
        try {
            savedPref = mPMHostUtils.getDevicePreferredInstallLocation();
            mPMHostUtils.setDevicePreferredInstallLocation(preference);
            doStandardInstall(EXTERNAL_LOC_APK, EXTERNAL_LOC_PKG, expectedLocation);
        }
        finally {
            mPMHostUtils.setDevicePreferredInstallLocation(savedPref);
            mPMHostUtils.uninstallApp(EXTERNAL_LOC_PKG);
        }
    }
    public void testInstallAppExternalLocPrefIsAuto() throws IOException, InterruptedException {
        Log.i(LOG_TAG, "Test installLocation=external, pref=auto gets installed on SD Card");
        installAppExternalLoc(PackageManagerHostTestUtils.InstallLocPreference.AUTO,
                PackageManagerHostTestUtils.InstallLocation.SDCARD);
    }
    public void testInstallAppExternalLocPrefIsInternal() throws IOException, InterruptedException {
        Log.i(LOG_TAG, "Test installLocation=external, pref=internal gets installed on SD Card");
        installAppExternalLoc(PackageManagerHostTestUtils.InstallLocPreference.INTERNAL,
                PackageManagerHostTestUtils.InstallLocation.SDCARD);
    }
    public void testInstallAppExternalLocPrefIsExternal() throws IOException, InterruptedException {
        Log.i(LOG_TAG, "Test installLocation=external, pref=external gets installed on SD Card");
        installAppExternalLoc(PackageManagerHostTestUtils.InstallLocPreference.EXTERNAL,
                PackageManagerHostTestUtils.InstallLocation.SDCARD);
    }
    public void testInstallAppNoLocPrefIsAuto() throws IOException, InterruptedException {
        Log.i(LOG_TAG, "Test an app with no installLocation gets installed on device");
        PackageManagerHostTestUtils.InstallLocPreference savedPref =
            PackageManagerHostTestUtils.InstallLocPreference.AUTO;
        try {
            savedPref = mPMHostUtils.getDevicePreferredInstallLocation();
            mPMHostUtils.setDevicePreferredInstallLocation(
                    PackageManagerHostTestUtils.InstallLocPreference.AUTO);
            mPMHostUtils.installAppAndVerifyExistsOnDevice(
                    getTestAppFilePath(NO_LOC_APK), NO_LOC_PKG, false);
        }
        finally {
            mPMHostUtils.setDevicePreferredInstallLocation(savedPref);
            mPMHostUtils.uninstallApp(NO_LOC_PKG);
        }
    }
    public void testInstallAppNoLocPrefIsExternal() throws IOException, InterruptedException {
        Log.i(LOG_TAG, "Test an app with no installLocation gets installed on SD card");
        PackageManagerHostTestUtils.InstallLocPreference savedPref =
            PackageManagerHostTestUtils.InstallLocPreference.AUTO;
        try {
            savedPref = mPMHostUtils.getDevicePreferredInstallLocation();
            mPMHostUtils.setDevicePreferredInstallLocation(
                    PackageManagerHostTestUtils.InstallLocPreference.EXTERNAL);
            mPMHostUtils.installAppAndVerifyExistsOnSDCard(
                    getTestAppFilePath(NO_LOC_APK), NO_LOC_PKG, false);
        }
        finally {
            mPMHostUtils.setDevicePreferredInstallLocation(savedPref);
            mPMHostUtils.uninstallApp(NO_LOC_PKG);
        }
    }
    public void testInstallAppNoLocPrefIsInternal() throws IOException, InterruptedException {
        Log.i(LOG_TAG, "Test an app with no installLocation gets installed on device");
        PackageManagerHostTestUtils.InstallLocPreference savedPref =
            PackageManagerHostTestUtils.InstallLocPreference.AUTO;
        try {
            savedPref = mPMHostUtils.getDevicePreferredInstallLocation();
            mPMHostUtils.setDevicePreferredInstallLocation(
                    PackageManagerHostTestUtils.InstallLocPreference.INTERNAL);
            mPMHostUtils.installAppAndVerifyExistsOnDevice(
                    getTestAppFilePath(NO_LOC_APK), NO_LOC_PKG, false);
        }
        finally {
            mPMHostUtils.setDevicePreferredInstallLocation(savedPref);
            mPMHostUtils.uninstallApp(NO_LOC_PKG);
        }
    }
    public void testInstallFwdLockedAppInternal() throws IOException, InterruptedException {
        Log.i(LOG_TAG, "Test an app with installLoc set to Internal gets installed to app-private");
        try {
            mPMHostUtils.installFwdLockedAppAndVerifyExists(
                    getTestAppFilePath(INTERNAL_LOC_APK), INTERNAL_LOC_PKG, false);
        }
        finally {
            mPMHostUtils.uninstallApp(INTERNAL_LOC_PKG);
        }
    }
    public void testInstallFwdLockedAppExternal() throws IOException, InterruptedException {
        Log.i(LOG_TAG, "Test an app with installLoc set to Internal gets installed to app-private");
        try {
            mPMHostUtils.installFwdLockedAppAndVerifyExists(
                    getTestAppFilePath(INTERNAL_LOC_APK), INTERNAL_LOC_PKG, false);
        }
        finally {
            mPMHostUtils.uninstallApp(INTERNAL_LOC_PKG);
        }
    }
    public void testInstallFwdLockedAppAuto() throws IOException, InterruptedException {
        Log.i(LOG_TAG, "Test an app with installLoc set to Auto gets installed to app-private");
        try {
            mPMHostUtils.installFwdLockedAppAndVerifyExists(
                    getTestAppFilePath(AUTO_LOC_APK), AUTO_LOC_PKG, false);
        }
        finally {
            mPMHostUtils.uninstallApp(AUTO_LOC_PKG);
        }
    }
    public void testInstallFwdLockedAppNone() throws IOException, InterruptedException {
        Log.i(LOG_TAG, "Test an app with no installLoc set gets installed to app-private");
        try {
            mPMHostUtils.installFwdLockedAppAndVerifyExists(
                    getTestAppFilePath(NO_LOC_APK), NO_LOC_PKG, false);
        }
        finally {
            mPMHostUtils.uninstallApp(NO_LOC_PKG);
        }
    }
    public void testReinstallInternalToExternal() throws IOException, InterruptedException {
        Log.i(LOG_TAG, "Test installing an app first to the device, then to the SD Card");
        try {
            mPMHostUtils.installAppAndVerifyExistsOnDevice(
                    getTestAppFilePath(VERSATILE_LOC_INTERNAL_APK), VERSATILE_LOC_PKG, false);
            mPMHostUtils.uninstallApp(VERSATILE_LOC_PKG);
            mPMHostUtils.installAppAndVerifyExistsOnSDCard(
                    getTestAppFilePath(VERSATILE_LOC_EXTERNAL_APK), VERSATILE_LOC_PKG, false);
        }
        finally {
            mPMHostUtils.uninstallApp(VERSATILE_LOC_PKG);
        }
    }
    public void testReinstallExternalToInternal() throws IOException, InterruptedException {
        Log.i(LOG_TAG, "Test installing an app first to the SD Care, then to the device");
        try {
            mPMHostUtils.installAppAndVerifyExistsOnSDCard(
                    getTestAppFilePath(VERSATILE_LOC_EXTERNAL_APK), VERSATILE_LOC_PKG, false);
            mPMHostUtils.uninstallApp(VERSATILE_LOC_PKG);
            mPMHostUtils.installAppAndVerifyExistsOnDevice(
                    getTestAppFilePath(VERSATILE_LOC_INTERNAL_APK), VERSATILE_LOC_PKG, false);
        }
        finally {
          mPMHostUtils.uninstallApp(VERSATILE_LOC_PKG);
        }
    }
    public void testUpdateBothExternal() throws IOException, InterruptedException {
        Log.i(LOG_TAG, "Test updating an app on the SD card stays on the SD card");
        try {
            mPMHostUtils.installAppAndVerifyExistsOnSDCard(getTestAppFilePath(
                    EXTERNAL_LOC_VERSION_V1_APK), EXTERNAL_LOC_VERSION_PKG, false);
            mPMHostUtils.installAppAndVerifyExistsOnSDCard(getTestAppFilePath(
                    EXTERNAL_LOC_VERSION_V2_APK), EXTERNAL_LOC_VERSION_PKG, true);
        }
        finally {
          mPMHostUtils.uninstallApp(EXTERNAL_LOC_VERSION_PKG);
        }
    }
    public void testUpdateToSDCard() throws IOException, InterruptedException {
        Log.i(LOG_TAG, "Test updating an app on the SD card stays on the SD card");
        try {
            mPMHostUtils.installAppAndVerifyExistsOnSDCard(getTestAppFilePath(
                    UPDATE_EXTERNAL_LOC_V1_EXT_APK), UPDATE_EXTERNAL_LOC_PKG, false);
            mPMHostUtils.installAppAndVerifyExistsOnSDCard(getTestAppFilePath(
                    UPDATE_EXTERNAL_LOC_V2_NONE_APK), UPDATE_EXTERNAL_LOC_PKG, true);
        }
        finally {
          mPMHostUtils.uninstallApp(UPDATE_EXTERNAL_LOC_PKG);
        }
    }
    public void testUpdateSDCardToDevice() throws IOException, InterruptedException {
        Log.i(LOG_TAG, "Test updating an app on the SD card to the Device through manifest change");
        try {
            mPMHostUtils.installAppAndVerifyExistsOnSDCard(getTestAppFilePath(
                    UPDATE_EXT_TO_INT_LOC_V1_EXT_APK), UPDATE_EXT_TO_INT_LOC_PKG, false);
            mPMHostUtils.installAppAndVerifyExistsOnDevice(getTestAppFilePath(
                    UPDATE_EXT_TO_INT_LOC_V2_INT_APK), UPDATE_EXT_TO_INT_LOC_PKG, true);
        }
        finally {
            mPMHostUtils.uninstallApp(UPDATE_EXT_TO_INT_LOC_PKG);
        }
    }
    public void testInstallAndUpdateExternalLocForwardLockedApp()
            throws IOException, InterruptedException {
        Log.i(LOG_TAG, "Test updating a forward-locked app marked preferExternal");
        try {
            mPMHostUtils.installFwdLockedAppAndVerifyExists(getTestAppFilePath(
                    EXTERNAL_LOC_VERSION_V1_APK), EXTERNAL_LOC_VERSION_PKG, false);
            mPMHostUtils.installFwdLockedAppAndVerifyExists(getTestAppFilePath(
                    EXTERNAL_LOC_VERSION_V2_APK), EXTERNAL_LOC_VERSION_PKG, true);
        }
        finally {
            mPMHostUtils.uninstallApp(EXTERNAL_LOC_VERSION_PKG);
        }
    }
    public void testInstallAndUpdateNoLocForwardLockedApp()
            throws IOException, InterruptedException {
        Log.i(LOG_TAG, "Test updating a forward-locked app with no installLocation pref set");
        try {
            mPMHostUtils.installFwdLockedAppAndVerifyExists(getTestAppFilePath(
                    NO_LOC_VERSION_V1_APK), NO_LOC_VERSION_PKG, false);
            mPMHostUtils.installFwdLockedAppAndVerifyExists(getTestAppFilePath(
                    NO_LOC_VERSION_V2_APK), NO_LOC_VERSION_PKG, true);
        }
        finally {
            mPMHostUtils.uninstallApp(NO_LOC_VERSION_PKG);
        }
    }
    public void testInstallAndLaunchAllPermsAppOnSD()
            throws IOException, InterruptedException {
        Log.i(LOG_TAG, "Test launching an app with all perms set, installed on SD card");
        try {
            mPMHostUtils.installAppAndVerifyExistsOnSDCard(getTestAppFilePath(
                    ALL_PERMS_APK), ALL_PERMS_PKG, false);
            boolean testsPassed = mPMHostUtils.runDeviceTestsDidAllTestsPass(ALL_PERMS_PKG);
            assert(testsPassed);
        }
        finally {
            mPMHostUtils.uninstallApp(ALL_PERMS_PKG);
        }
    }
    public void testInstallAndLaunchFLPermsAppOnSD()
            throws IOException, InterruptedException {
        Log.i(LOG_TAG, "Test launching an app with location perms set, installed on SD card");
        try {
            mPMHostUtils.installAppAndVerifyExistsOnSDCard(getTestAppFilePath(
                    SHARED_PERMS_FL_APK), SHARED_PERMS_FL_PKG, false);
            boolean testsPassed = mPMHostUtils.runDeviceTestsDidAllTestsPass(SHARED_PERMS_FL_PKG);
            assert(testsPassed);
        }
        finally {
            mPMHostUtils.uninstallApp(SHARED_PERMS_FL_PKG);
        }
    }
    public void testInstallAndLaunchBTPermsAppOnSD()
            throws IOException, InterruptedException {
        Log.i(LOG_TAG, "Test launching an app with bluetooth perms set, installed on SD card");
        try {
            mPMHostUtils.installAppAndVerifyExistsOnSDCard(getTestAppFilePath(
                    SHARED_PERMS_BT_APK), SHARED_PERMS_BT_PKG, false);
            boolean testsPassed = mPMHostUtils.runDeviceTestsDidAllTestsPass(SHARED_PERMS_BT_PKG);
            assert(testsPassed);
        }
        finally {
            mPMHostUtils.uninstallApp(SHARED_PERMS_BT_PKG);
        }
    }
    public void testInstallAndLaunchSharedPermsAppOnSD_NoPerms()
            throws IOException, InterruptedException {
        Log.i(LOG_TAG, "Test launching an app with no explicit perms set, installed on SD card");
        try {
            mPMHostUtils.uninstallApp(SHARED_PERMS_FL_PKG);
            mPMHostUtils.uninstallApp(SHARED_PERMS_BT_PKG);
            mPMHostUtils.installAppAndVerifyExistsOnSDCard(getTestAppFilePath(
                    SHARED_PERMS_APK), SHARED_PERMS_PKG, false);
            boolean testsPassed = mPMHostUtils.runDeviceTestsDidAllTestsPass(SHARED_PERMS_PKG);
            assertEquals("Shared perms app should fail to run", false, testsPassed);
        }
        finally {
            mPMHostUtils.uninstallApp(SHARED_PERMS_PKG);
        }
    }
    public void testInstallAndLaunchSharedPermsAppOnSD_GrantedPerms()
            throws IOException, InterruptedException {
        Log.i(LOG_TAG, "Test launching an app with no explicit perms set, installed on SD card");
        try {
            mPMHostUtils.installAppAndVerifyExistsOnSDCard(getTestAppFilePath(
                    SHARED_PERMS_FL_APK), SHARED_PERMS_FL_PKG, false);
            mPMHostUtils.installAppAndVerifyExistsOnSDCard(getTestAppFilePath(
                    SHARED_PERMS_BT_APK), SHARED_PERMS_BT_PKG, false);
            mPMHostUtils.installAppAndVerifyExistsOnSDCard(getTestAppFilePath(
                    SHARED_PERMS_APK), SHARED_PERMS_PKG, false);
            boolean testsPassed = mPMHostUtils.runDeviceTestsDidAllTestsPass(SHARED_PERMS_PKG);
            assert(testsPassed);
        }
        finally {
            mPMHostUtils.uninstallApp(SHARED_PERMS_PKG);
            mPMHostUtils.uninstallApp(SHARED_PERMS_BT_PKG);
            mPMHostUtils.uninstallApp(SHARED_PERMS_FL_PKG);
        }
    }
    public void testInstallAndLaunchFLPermsAppOnSD_Reboot()
            throws IOException, InterruptedException {
        Log.i(LOG_TAG, "Test launching an app with location perms set, installed on SD card");
        try {
            mPMHostUtils.installAppAndVerifyExistsOnSDCard(getTestAppFilePath(
                    SHARED_PERMS_FL_APK), SHARED_PERMS_FL_PKG, false);
            boolean testsPassed = mPMHostUtils.runDeviceTestsDidAllTestsPass(SHARED_PERMS_FL_PKG);
            assert(testsPassed);
            mPMHostUtils.rebootDevice();
            testsPassed = mPMHostUtils.runDeviceTestsDidAllTestsPass(SHARED_PERMS_FL_PKG);
            assert(testsPassed);
        }
        finally {
            mPMHostUtils.uninstallApp(SHARED_PERMS_FL_PKG);
        }
    }
    public void testInstallAndLaunchSharedPermsAppOnSD_Reboot()
            throws IOException, InterruptedException {
        Log.i(LOG_TAG, "Test launching an app on SD, with no explicit perms set after reboot");
        try {
            mPMHostUtils.installAppAndVerifyExistsOnSDCard(getTestAppFilePath(
                    SHARED_PERMS_FL_APK), SHARED_PERMS_FL_PKG, false);
            mPMHostUtils.installAppAndVerifyExistsOnSDCard(getTestAppFilePath(
                    SHARED_PERMS_BT_APK), SHARED_PERMS_BT_PKG, false);
            mPMHostUtils.installAppAndVerifyExistsOnSDCard(getTestAppFilePath(
                    SHARED_PERMS_APK), SHARED_PERMS_PKG, false);
            boolean testsPassed = mPMHostUtils.runDeviceTestsDidAllTestsPass(SHARED_PERMS_PKG);
            assert(testsPassed);
            mPMHostUtils.rebootDevice();
            testsPassed = mPMHostUtils.runDeviceTestsDidAllTestsPass(SHARED_PERMS_PKG);
            assert(testsPassed);
        }
        finally {
            mPMHostUtils.uninstallApp(SHARED_PERMS_PKG);
            mPMHostUtils.uninstallApp(SHARED_PERMS_BT_PKG);
            mPMHostUtils.uninstallApp(SHARED_PERMS_FL_PKG);
        }
    }
}
