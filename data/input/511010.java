@TestTargetClass(PackageManager.class)
public class PackageManagerTest extends AndroidTestCase {
    private PackageManager mPackageManager;
    private static final String PACKAGE_NAME = "com.android.cts.stub";
    private static final String CONTENT_PKG_NAME = "com.android.cts.content";
    private static final String ACTIVITY_ACTION_NAME = "android.intent.action.PMTEST";
    private static final String MAIN_ACTION_NAME = "android.intent.action.MAIN";
    private static final String SERVICE_ACTION_NAME =
                                "android.content.pm.cts.activity.PMTEST_SERVICE";
    private static final String PERMISSION_NAME = "android.permission.INTERNET";
    private static final String ACTIVITY_NAME = "android.content.pm.cts.TestPmActivity";
    private static final String SERVICE_NAME = "android.content.pm.cts.TestPmService";
    private static final String RECEIVER_NAME = "android.content.pm.cts.PmTestReceiver";
    private static final String INSTRUMENT_NAME = "android.content.pm.cts.TestPmInstrumentation";
    private static final String PROVIDER_NAME = "android.content.cts.MockContentProvider";
    private static final String PERMISSIONGROUP_NAME = "android.permission-group.COST_MONEY";
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mPackageManager = getContext().getPackageManager();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test queryIntentActivityOptions",
            method = "queryIntentActivityOptions",
            args = {android.content.ComponentName.class, android.content.Intent[].class,
                    android.content.Intent.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test queryIntentActivities",
            method = "queryIntentActivities",
            args = {android.content.Intent.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test queryInstrumentation",
            method = "queryInstrumentation",
            args = {java.lang.String.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test queryBroadcastReceivers",
            method = "queryBroadcastReceivers",
            args = {android.content.Intent.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test queryPermissionsByGroup",
            method = "queryPermissionsByGroup",
            args = {java.lang.String.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test queryContentProviders",
            method = "queryContentProviders",
            args = {java.lang.String.class, int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test queryIntentServices",
            method = "queryIntentServices",
            args = {android.content.Intent.class, int.class}
        )
    })
    public void testQuery() throws NameNotFoundException {
        Intent activityIntent = new Intent(ACTIVITY_ACTION_NAME);
        String cmpActivityName = "android.content.pm.cts.TestPmCompare";
        List<ResolveInfo> listWithDiff = mPackageManager.queryIntentActivityOptions(
                new ComponentName(PACKAGE_NAME, cmpActivityName), null, activityIntent, 0);
        checkActivityInfoName(ACTIVITY_NAME, listWithDiff);
        List<ResolveInfo> listInSame = mPackageManager.queryIntentActivityOptions(
                new ComponentName(PACKAGE_NAME, ACTIVITY_NAME), null, activityIntent, 0);
        assertEquals(0, listInSame.size());
        List<ResolveInfo> intentActivities =
                mPackageManager.queryIntentActivities(activityIntent, 0);
        assertTrue(intentActivities.size() > 0);
        checkActivityInfoName(ACTIVITY_NAME, intentActivities);
        String targetPackage = "android";
        List<InstrumentationInfo> instrumentations = mPackageManager.queryInstrumentation(
                targetPackage, 0);
        checkInstrumentationInfoName(INSTRUMENT_NAME, instrumentations);
        Intent serviceIntent = new Intent(SERVICE_ACTION_NAME);
        List<ResolveInfo> services = mPackageManager.queryIntentServices(serviceIntent,
                PackageManager.GET_INTENT_FILTERS);
        checkServiceInfoName(SERVICE_NAME, services);
        String receiverActionName = "android.content.pm.cts.PackageManagerTest.PMTEST_RECEIVER";
        Intent broadcastIntent = new Intent(receiverActionName);
        List<ResolveInfo> broadcastReceivers = new ArrayList<ResolveInfo>();
        broadcastReceivers = mPackageManager.queryBroadcastReceivers(broadcastIntent, 0);
        checkActivityInfoName(RECEIVER_NAME, broadcastReceivers);
        String testPermissionsGroup = "android.permission-group.NETWORK";
        List<PermissionInfo> permissions = mPackageManager.queryPermissionsByGroup(
                testPermissionsGroup, PackageManager.GET_META_DATA);
        checkPermissionInfoName(PERMISSION_NAME, permissions);
        ApplicationInfo appInfo = mPackageManager.getApplicationInfo(PACKAGE_NAME, 0);
        List<ProviderInfo> providers = mPackageManager.queryContentProviders(PACKAGE_NAME,
                appInfo.uid, 0);
        checkProviderInfoName(PROVIDER_NAME, providers);
    }
    private void checkActivityInfoName(String expectedName, List<ResolveInfo> resolves) {
        boolean isContained = false;
        Iterator<ResolveInfo> infoIterator = resolves.iterator();
        String current;
        while (infoIterator.hasNext()) {
            current = infoIterator.next().activityInfo.name;
            if (current.equals(expectedName)) {
                isContained = true;
                break;
            }
        }
        assertTrue(isContained);
    }
    private void checkServiceInfoName(String expectedName, List<ResolveInfo> resolves) {
        boolean isContained = false;
        Iterator<ResolveInfo> infoIterator = resolves.iterator();
        String current;
        while (infoIterator.hasNext()) {
            current = infoIterator.next().serviceInfo.name;
            if (current.equals(expectedName)) {
                isContained = true;
                break;
            }
        }
        assertTrue(isContained);
    }
    private void checkPermissionInfoName(String expectedName, List<PermissionInfo> permissions) {
        boolean isContained = false;
        Iterator<PermissionInfo> infoIterator = permissions.iterator();
        String current;
        while (infoIterator.hasNext()) {
            current = infoIterator.next().name;
            if (current.equals(expectedName)) {
                isContained = true;
                break;
            }
        }
        assertTrue(isContained);
    }
    private void checkProviderInfoName(String expectedName, List<ProviderInfo> providers) {
        boolean isContained = false;
        Iterator<ProviderInfo> infoIterator = providers.iterator();
        String current;
        while (infoIterator.hasNext()) {
            current = infoIterator.next().name;
            if (current.equals(expectedName)) {
                isContained = true;
                break;
            }
        }
        assertTrue(isContained);
    }
    private void checkInstrumentationInfoName(String expectedName,
            List<InstrumentationInfo> instrumentations) {
        boolean isContained = false;
        Iterator<InstrumentationInfo> infoIterator = instrumentations.iterator();
        String current;
        while (infoIterator.hasNext()) {
            current = infoIterator.next().name;
            if (current.equals(expectedName)) {
                isContained = true;
                break;
            }
        }
        assertTrue(isContained);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test getPackageInfo",
            method = "getPackageInfo",
            args = {java.lang.String.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test getApplicationInfo",
            method = "getApplicationInfo",
            args = {java.lang.String.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test getApplicationInfo",
            method = "getApplicationLabel",
            args = {android.content.pm.ApplicationInfo.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test getServiceInfo",
            method = "getServiceInfo",
            args = {android.content.ComponentName.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test getPackageArchiveInfo",
            method = "getPackageArchiveInfo",
            args = {java.lang.String.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test getPackagesForUid",
            method = "getPackagesForUid",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test getNameForUid",
            method = "getNameForUid",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test getActivityInfo",
            method = "getActivityInfo",
            args = {android.content.ComponentName.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test getPackageGids",
            method = "getPackageGids",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test getPermissionInfo",
            method = "getPermissionInfo",
            args = {java.lang.String.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test getPermissionGroupInfo",
            method = "getPermissionGroupInfo",
            args = {java.lang.String.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test getAllPermissionGroups",
            method = "getAllPermissionGroups",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test getInstalledApplications",
            method = "getInstalledApplications",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test getInstalledPackages",
            method = "getInstalledPackages",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test getInstrumentationInfo",
            method = "getInstrumentationInfo",
            args = {android.content.ComponentName.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test getReceiverInfo",
            method = "getReceiverInfo",
            args = {android.content.ComponentName.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test getText",
            method = "getText",
            args = {java.lang.String.class, int.class, android.content.pm.ApplicationInfo.class}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            notes = "Test getSystemSharedLibraryNames",
            method = "getSystemSharedLibraryNames",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test getLaunchIntentForPackage",
            method = "getLaunchIntentForPackage",
            args = {String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test isSafeMode",
            method = "isSafeMode",
            args = {}
        )
    })
    @ToBeFixed(bug="1686810", explanation="From the doc of function getSystemSharedLibraryNames()."
        + " It will return an String array or null(both are Ok).")
    public void testGetInfo() throws NameNotFoundException {
        ApplicationInfo appInfo = mPackageManager.getApplicationInfo(PACKAGE_NAME, 0);
        int discriptionRes = R.string.hello_android;
        String expectedDisciptionRes = "Hello, Android!";
        CharSequence appText = mPackageManager.getText(PACKAGE_NAME, discriptionRes, appInfo);
        assertEquals(expectedDisciptionRes, appText);
        ComponentName activityName = new ComponentName(PACKAGE_NAME, ACTIVITY_NAME);
        ComponentName serviceName = new ComponentName(PACKAGE_NAME, SERVICE_NAME);
        ComponentName receiverName = new ComponentName(PACKAGE_NAME, RECEIVER_NAME);
        ComponentName instrName = new ComponentName(PACKAGE_NAME, INSTRUMENT_NAME);
        PackageInfo packageInfo = mPackageManager.getPackageInfo(PACKAGE_NAME,
                PackageManager.GET_INSTRUMENTATION);
        assertEquals(PACKAGE_NAME, packageInfo.packageName);
        String appLabel = "Android TestCase";
        assertEquals(appLabel, mPackageManager.getApplicationLabel(appInfo));
        assertEquals(PACKAGE_NAME, appInfo.processName);
        assertEquals(SERVICE_NAME, mPackageManager.getServiceInfo(serviceName,
                PackageManager.GET_META_DATA).name);
        assertEquals(RECEIVER_NAME, mPackageManager.getReceiverInfo(receiverName, 0).name);
        final String apkRoute = getContext().getPackageCodePath();
        final String apkName = getContext().getPackageName();
        assertEquals(apkName, mPackageManager.getPackageArchiveInfo(apkRoute, 0).packageName);
        checkPackagesNameForUid(PACKAGE_NAME, mPackageManager.getPackagesForUid(appInfo.uid));
        assertEquals(PACKAGE_NAME, mPackageManager.getNameForUid(appInfo.uid));
        assertEquals(ACTIVITY_NAME, mPackageManager.getActivityInfo(activityName, 0).name);
        assertTrue(mPackageManager.getPackageGids(PACKAGE_NAME).length > 0);
        assertEquals(PERMISSION_NAME, mPackageManager.getPermissionInfo(PERMISSION_NAME, 0).name);
        assertEquals(PERMISSIONGROUP_NAME, mPackageManager.getPermissionGroupInfo(
                PERMISSIONGROUP_NAME, 0).name);
        List<PermissionGroupInfo> permissionGroups = mPackageManager.getAllPermissionGroups(0);
        checkPermissionGroupInfoName(PERMISSIONGROUP_NAME, permissionGroups);
        assertTrue(mPackageManager.getInstalledApplications(PackageManager.GET_META_DATA).size() > 0);
        assertTrue(mPackageManager.getInstalledPackages(0).size() > 0);
        assertEquals(INSTRUMENT_NAME, mPackageManager.getInstrumentationInfo(instrName, 0).name);
        mPackageManager.getSystemSharedLibraryNames();
        assertEquals(MAIN_ACTION_NAME, mPackageManager.getLaunchIntentForPackage(PACKAGE_NAME)
                .getAction());
        assertFalse(mPackageManager.isSafeMode());
    }
    private void checkPackagesNameForUid(String expectedName, String[] uid) {
        boolean isContained = false;
        for (int i = 0; i < uid.length; i++) {
            if (uid[i].equals(expectedName)) {
                isContained = true;
                break;
            }
        }
        assertTrue(isContained);
    }
    private void checkPermissionGroupInfoName(String expectedName,
            List<PermissionGroupInfo> permissionGroups) {
        boolean isContained = false;
        Iterator<PermissionGroupInfo> infoIterator = permissionGroups.iterator();
        String current;
        while (infoIterator.hasNext()) {
            current = infoIterator.next().name;
            if (current.equals(expectedName)) {
                isContained = true;
                break;
            }
        }
        assertTrue(isContained);
    }
    public void testGetPreferredActivities() {
        assertNoPreferredActivities();
    }
    private void assertNoPreferredActivities() {
        List<ComponentName> outActivities = new ArrayList<ComponentName>();
        List<IntentFilter> outFilters = new ArrayList<IntentFilter>();
        mPackageManager.getPreferredActivities(outFilters, outActivities, PACKAGE_NAME);
        assertEquals(0, outActivities.size());
        assertEquals(0, outFilters.size());
    }
    public void testAddPreferredActivity() {
        IntentFilter intentFilter = new IntentFilter(ACTIVITY_ACTION_NAME);
        ComponentName[] componentName = {new ComponentName(PACKAGE_NAME, ACTIVITY_NAME)};
        try {
            mPackageManager.addPreferredActivity(intentFilter, IntentFilter.MATCH_CATEGORY_HOST,
                    componentName, componentName[0]);
            fail("addPreferredActivity unexpectedly succeeded");
        } catch (SecurityException e) {
        }
        assertNoPreferredActivities();
    }
    public void testClearPackagePreferredActivities() {
        mPackageManager.clearPackagePreferredActivities(PACKAGE_NAME);
    }
    private void checkComponentName(String expectedName, List<ComponentName> componentNames) {
        boolean isContained = false;
        Iterator<ComponentName> nameIterator = componentNames.iterator();
        String current;
        while (nameIterator.hasNext()) {
            current = nameIterator.next().getClassName();
            if (current.equals(expectedName)) {
                isContained = true;
                break;
            }
        }
        assertTrue(isContained);
    }
    private void checkIntentFilterAction(String expectedName, List<IntentFilter> intentFilters) {
        boolean isContained = false;
        Iterator<IntentFilter> filterIterator = intentFilters.iterator();
        IntentFilter currentFilter;
        String currentAction;
        while (filterIterator.hasNext()) {
            currentFilter = filterIterator.next();
            for (int i = 0; i < currentFilter.countActions(); i++) {
                currentAction = currentFilter.getAction(i);
                if (currentAction.equals(expectedName)) {
                    isContained = true;
                    break;
                }
            }
        }
        assertTrue(isContained);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test setApplicationEnabledSetting",
            method = "setApplicationEnabledSetting",
            args = {java.lang.String.class, int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test getApplicationEnabledSetting",
            method = "getApplicationEnabledSetting",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test setComponentEnabledSetting",
            method = "setComponentEnabledSetting",
            args = {android.content.ComponentName.class, int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test getComponentEnabledSetting",
            method = "getComponentEnabledSetting",
            args = {android.content.ComponentName.class}
        )
    })
    public void testAccessEnabledSetting() {
        mPackageManager.setApplicationEnabledSetting(PACKAGE_NAME,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        assertEquals(PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                mPackageManager.getApplicationEnabledSetting(PACKAGE_NAME));
        ComponentName componentName = new ComponentName(PACKAGE_NAME, ACTIVITY_NAME);
        mPackageManager.setComponentEnabledSetting(componentName,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        assertEquals(PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                mPackageManager.getComponentEnabledSetting(componentName));
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test addPermission",
            method = "addPermission",
            args = {android.content.pm.PermissionInfo.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test removePermission",
            method = "removePermission",
            args = {java.lang.String.class}
        )
    })
    @ToBeFixed(bug="1561181", explanation="According to javadoc of these two methods," +
            " we created a permission-tree and permission under this tree in our" +
            " AndroidManifest.xml, then invoked addPermission method, but there was" +
            " 'java.lang.SecurityException: Not allowed to modify non-dynamic permission'" +
            " exception. We found that" +
            " {@link com.android.server# PackageManagerService.addPermission" +
            " (also removePermission) is the very method with function same to our target" +
            " method, and in PackageManagerService.addPermission, there is a if-branch to check" +
            " whether 'type' equals to BasePermission.TYPE_DYNAMIC(L1004), and the 'type' is" +
            " parsed from /data/system/packages.xml in emulator, but we found no 'type'" +
            " tag in packages.xml, as well as no other explicit ways to add this 'type' tag," +
            " so we can't add permission in dynamic way.")
    public void testOpPermission() {
        PermissionInfo permissionInfo = new PermissionInfo();
        String permissionName = "com.android.cts.stub.permission.TEST_DYNAMIC.ADD";
        permissionInfo.name = permissionName;
        permissionInfo.labelRes = R.string.permlab_testDynamic;
        permissionInfo.nonLocalizedLabel = "Test Tree";
    }
    public void testAddPackageToPreferred() {
        List<PackageInfo> pkgInfo = null;
        pkgInfo = mPackageManager.getPreferredPackages(0);
        int pkgInfoSize = pkgInfo.size();
        try {
            mPackageManager.addPackageToPreferred(CONTENT_PKG_NAME);
            fail("addPackageToPreferred unexpectedly succeeded");
        } catch (SecurityException e) {
        }
        pkgInfo = mPackageManager.getPreferredPackages(0);
        assertEquals(pkgInfo.size(), pkgInfoSize);
    }
    public void testRemovePackageFromPreferred() {
        try {
            mPackageManager.removePackageFromPreferred(CONTENT_PKG_NAME);
            fail("removePackageFromPreferred unexpectedly succeeded");
        } catch (SecurityException e) {
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test getActivityIcon",
            method = "getActivityIcon",
            args = {android.content.ComponentName.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test getActivityIcon",
            method = "getActivityIcon",
            args = {android.content.Intent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test getDrawable",
            method = "getDrawable",
            args = {java.lang.String.class, int.class, android.content.pm.ApplicationInfo.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test getDefaultActivityIcon",
            method = "getDefaultActivityIcon",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test getApplicationIcon",
            method = "getApplicationIcon",
            args = {android.content.pm.ApplicationInfo.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test getApplicationIcon",
            method = "getApplicationIcon",
            args = {java.lang.String.class}
        )
    })
    public void testGetIcon() throws NameNotFoundException {
        assertNotNull(mPackageManager.getApplicationIcon(PACKAGE_NAME));
        assertNotNull(mPackageManager.getApplicationIcon(mPackageManager.getApplicationInfo(
                PACKAGE_NAME, 0)));
        assertNotNull(mPackageManager
                .getActivityIcon(new ComponentName(PACKAGE_NAME, ACTIVITY_NAME)));
        assertNotNull(mPackageManager.getActivityIcon(new Intent(MAIN_ACTION_NAME)));
        assertNotNull(mPackageManager.getDefaultActivityIcon());
        int iconRes = R.drawable.start;
        ApplicationInfo appInfo = mPackageManager.getApplicationInfo(PACKAGE_NAME, 0);
        assertNotNull(mPackageManager.getDrawable(PACKAGE_NAME, iconRes, appInfo));
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test checkSignatures",
            method = "checkSignatures",
            args = {java.lang.String.class, java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test checkPermission",
            method = "checkPermission",
            args = {java.lang.String.class, java.lang.String.class}
        )
    })
    public void testCheckMethods() {
        assertEquals(PackageManager.SIGNATURE_MATCH, mPackageManager.checkSignatures(PACKAGE_NAME,
                CONTENT_PKG_NAME));
        assertEquals(PackageManager.PERMISSION_GRANTED,
                mPackageManager.checkPermission(PERMISSION_NAME, PACKAGE_NAME));
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test resolveActivity",
            method = "resolveActivity",
            args = {android.content.Intent.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test resolveContentProvider",
            method = "resolveContentProvider",
            args = {java.lang.String.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test resolveService",
            method = "resolveService",
            args = {android.content.Intent.class, int.class}
        )
    })
    public void testResolveMethods() {
        Intent intent = new Intent(ACTIVITY_ACTION_NAME);
        intent.setComponent(new ComponentName(PACKAGE_NAME, ACTIVITY_NAME));
        assertEquals(ACTIVITY_NAME, mPackageManager.resolveActivity(intent,
                PackageManager.MATCH_DEFAULT_ONLY).activityInfo.name);
        intent = new Intent(SERVICE_ACTION_NAME);
        intent.setComponent(new ComponentName(PACKAGE_NAME, SERVICE_NAME));
        ResolveInfo resolveInfo = mPackageManager.resolveService(intent,
                PackageManager.GET_INTENT_FILTERS);
        assertEquals(SERVICE_NAME, resolveInfo.serviceInfo.name);
        String providerAuthorities = "ctstest";
        assertEquals(PROVIDER_NAME,
                mPackageManager.resolveContentProvider(providerAuthorities, 0).name);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test getResourcesForApplication",
            method = "getResourcesForApplication",
            args = {android.content.pm.ApplicationInfo.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test getResourcesForApplication",
            method = "getResourcesForApplication",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test getResourcesForActivity",
            method = "getResourcesForActivity",
            args = {android.content.ComponentName.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test getXml",
            method = "getXml",
            args = {java.lang.String.class, int.class, android.content.pm.ApplicationInfo.class}
        )
    })
    public void testGetResources() throws NameNotFoundException {
        ComponentName componentName = new ComponentName(PACKAGE_NAME, ACTIVITY_NAME);
        int resourceId = R.xml.pm_test;
        String xmlName = "com.android.cts.stub:xml/pm_test";
        ApplicationInfo appInfo = mPackageManager.getApplicationInfo(PACKAGE_NAME, 0);
        assertNotNull(mPackageManager.getXml(PACKAGE_NAME, resourceId, appInfo));
        assertEquals(xmlName, mPackageManager.getResourcesForActivity(componentName)
                .getResourceName(resourceId));
        assertEquals(xmlName, mPackageManager.getResourcesForApplication(appInfo).getResourceName(
                resourceId));
        assertEquals(xmlName, mPackageManager.getResourcesForApplication(PACKAGE_NAME)
                .getResourceName(resourceId));
    }
}
