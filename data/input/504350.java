public class MockPackageManager extends PackageManager {
    @Override
    public PackageInfo getPackageInfo(String packageName, int flags)
    throws NameNotFoundException {
        throw new UnsupportedOperationException();
    }
    @Override
    public String[] currentToCanonicalPackageNames(String[] names) {
        throw new UnsupportedOperationException();
    }
    @Override
    public String[] canonicalToCurrentPackageNames(String[] names) {
        throw new UnsupportedOperationException();
    }
    @Override
    public Intent getLaunchIntentForPackage(String packageName) {
        throw new UnsupportedOperationException();
    }
    @Override
    public int[] getPackageGids(String packageName) throws NameNotFoundException {
        throw new UnsupportedOperationException();
    }
    @Override
    public PermissionInfo getPermissionInfo(String name, int flags)
    throws NameNotFoundException {
        throw new UnsupportedOperationException();
    }
    @Override
    public List<PermissionInfo> queryPermissionsByGroup(String group, int flags)
            throws NameNotFoundException {
        throw new UnsupportedOperationException();
    }
    @Override
    public PermissionGroupInfo getPermissionGroupInfo(String name,
            int flags) throws NameNotFoundException {
        throw new UnsupportedOperationException();
    }
    @Override
    public List<PermissionGroupInfo> getAllPermissionGroups(int flags) {
        throw new UnsupportedOperationException();
    }
    @Override
    public ApplicationInfo getApplicationInfo(String packageName, int flags)
    throws NameNotFoundException {
        throw new UnsupportedOperationException();
    }
    @Override
    public ActivityInfo getActivityInfo(ComponentName className, int flags)
    throws NameNotFoundException {
        throw new UnsupportedOperationException();
    }
    @Override
    public ActivityInfo getReceiverInfo(ComponentName className, int flags)
    throws NameNotFoundException {
        throw new UnsupportedOperationException();
    }
    @Override
    public ServiceInfo getServiceInfo(ComponentName className, int flags)
    throws NameNotFoundException {
        throw new UnsupportedOperationException();
    }
    @Override
    public List<PackageInfo> getInstalledPackages(int flags) {
        throw new UnsupportedOperationException();
    }
    @Override
    public int checkPermission(String permName, String pkgName) {
        throw new UnsupportedOperationException();
    }
    @Override
    public boolean addPermission(PermissionInfo info) {
        throw new UnsupportedOperationException();
    }
    @Override
    public boolean addPermissionAsync(PermissionInfo info) {
        throw new UnsupportedOperationException();
    }
    @Override
    public void removePermission(String name) {
        throw new UnsupportedOperationException();
    }
    @Override
    public int checkSignatures(String pkg1, String pkg2) {
        throw new UnsupportedOperationException();
    }
    @Override
    public int checkSignatures(int uid1, int uid2) {
        throw new UnsupportedOperationException();
    }
    @Override
    public String[] getPackagesForUid(int uid) {
        throw new UnsupportedOperationException();
    }
    @Override
    public String getNameForUid(int uid) {
        throw new UnsupportedOperationException();
    }
    @Override
    public int getUidForSharedUser(String sharedUserName) {
        throw new UnsupportedOperationException();
    }
    @Override
    public List<ApplicationInfo> getInstalledApplications(int flags) {
        throw new UnsupportedOperationException();
    }
    @Override
    public ResolveInfo resolveActivity(Intent intent, int flags) {
        throw new UnsupportedOperationException();
    }
    @Override
    public List<ResolveInfo> queryIntentActivities(Intent intent, int flags) {
        throw new UnsupportedOperationException();
    }
    @Override
    public List<ResolveInfo> queryIntentActivityOptions(ComponentName caller,
            Intent[] specifics, Intent intent, int flags) {
        throw new UnsupportedOperationException();
    }
    @Override
    public List<ResolveInfo> queryBroadcastReceivers(Intent intent, int flags) {
        throw new UnsupportedOperationException();
    }
    @Override
    public ResolveInfo resolveService(Intent intent, int flags) {
        throw new UnsupportedOperationException();
    }
    @Override
    public List<ResolveInfo> queryIntentServices(Intent intent, int flags) {
        throw new UnsupportedOperationException();
    }
    @Override
    public ProviderInfo resolveContentProvider(String name, int flags) {
        throw new UnsupportedOperationException();
    }
    @Override
    public List<ProviderInfo> queryContentProviders(String processName, int uid, int flags) {
        throw new UnsupportedOperationException();
    }
    @Override
    public InstrumentationInfo getInstrumentationInfo(ComponentName className, int flags)
    throws NameNotFoundException {
        throw new UnsupportedOperationException();
    }
    @Override
    public List<InstrumentationInfo> queryInstrumentation(
            String targetPackage, int flags) {
        throw new UnsupportedOperationException();
    }
    @Override
    public Drawable getDrawable(String packageName, int resid, ApplicationInfo appInfo) {
        throw new UnsupportedOperationException();
    }
    @Override
    public Drawable getActivityIcon(ComponentName activityName)
    throws NameNotFoundException {
        throw new UnsupportedOperationException();
    }
    @Override
    public Drawable getActivityIcon(Intent intent) throws NameNotFoundException {
        throw new UnsupportedOperationException();
    }
    @Override
    public Drawable getDefaultActivityIcon() {
        throw new UnsupportedOperationException();
    }
    @Override
    public Drawable getApplicationIcon(ApplicationInfo info) {
        throw new UnsupportedOperationException();
    }
    @Override
    public Drawable getApplicationIcon(String packageName) throws NameNotFoundException {
        throw new UnsupportedOperationException();
    }
    @Override
    public CharSequence getText(String packageName, int resid, ApplicationInfo appInfo) {
        throw new UnsupportedOperationException();
    }
    @Override
    public XmlResourceParser getXml(String packageName, int resid,
            ApplicationInfo appInfo) {
        throw new UnsupportedOperationException();
    }
    @Override
    public CharSequence getApplicationLabel(ApplicationInfo info) {
        throw new UnsupportedOperationException();
    }
    @Override
    public Resources getResourcesForActivity(ComponentName activityName)
    throws NameNotFoundException {
        throw new UnsupportedOperationException();
    }
    @Override
    public Resources getResourcesForApplication(ApplicationInfo app) {
        throw new UnsupportedOperationException();
    }
    @Override
    public Resources getResourcesForApplication(String appPackageName)
    throws NameNotFoundException {
        throw new UnsupportedOperationException();
    }
    @Override
    public PackageInfo getPackageArchiveInfo(String archiveFilePath, int flags) {
        throw new UnsupportedOperationException();
    }
    @Override
    public void installPackage(Uri packageURI, IPackageInstallObserver observer,
            int flags, String installerPackageName) {
        throw new UnsupportedOperationException();
    }
    @Override
    public void movePackage(String packageName, IPackageMoveObserver observer, int flags) {
        throw new UnsupportedOperationException();
    }
    @Override
    public String getInstallerPackageName(String packageName) {
        throw new UnsupportedOperationException();
    }
    @Override
    public void clearApplicationUserData(
            String packageName, IPackageDataObserver observer) {
        throw new UnsupportedOperationException();
    }
    @Override
    public void deleteApplicationCacheFiles(
            String packageName, IPackageDataObserver observer) {
        throw new UnsupportedOperationException();
    }
    @Override
    public void freeStorageAndNotify(
            long idealStorageSize, IPackageDataObserver observer) {
        throw new UnsupportedOperationException();
    }
    @Override
    public void freeStorage(
            long idealStorageSize, IntentSender pi) {
        throw new UnsupportedOperationException();
    }
    @Override
    public void deletePackage(
            String packageName, IPackageDeleteObserver observer, int flags) {
        throw new UnsupportedOperationException();
    }
    @Override
    public void addPackageToPreferred(String packageName) {
        throw new UnsupportedOperationException();
    }
    @Override
    public void removePackageFromPreferred(String packageName) {
        throw new UnsupportedOperationException();
    }
    @Override
    public List<PackageInfo> getPreferredPackages(int flags) {
        throw new UnsupportedOperationException();
    }
    @Override
    public void setComponentEnabledSetting(ComponentName componentName,
            int newState, int flags) {
        throw new UnsupportedOperationException();
    }
    @Override
    public int getComponentEnabledSetting(ComponentName componentName) {
        throw new UnsupportedOperationException();
    }
    @Override
    public void setApplicationEnabledSetting(String packageName, int newState, int flags) {
        throw new UnsupportedOperationException();
    }
    @Override
    public int getApplicationEnabledSetting(String packageName) {
        throw new UnsupportedOperationException();
    }
    @Override
    public void addPreferredActivity(IntentFilter filter,
            int match, ComponentName[] set, ComponentName activity) {
        throw new UnsupportedOperationException();
    }
    @Override
    public void replacePreferredActivity(IntentFilter filter,
            int match, ComponentName[] set, ComponentName activity) {
        throw new UnsupportedOperationException();
    }
    @Override
    public void clearPackagePreferredActivities(String packageName) {
        throw new UnsupportedOperationException();
    }
    @Override
    public void getPackageSizeInfo(String packageName, IPackageStatsObserver observer) {
        throw new UnsupportedOperationException();
    }
    @Override
    public int getPreferredActivities(List<IntentFilter> outFilters,
            List<ComponentName> outActivities, String packageName) {
        throw new UnsupportedOperationException();
    }
    @Override
    public String[] getSystemSharedLibraryNames() {
        throw new UnsupportedOperationException();
    }
    @Override
    public FeatureInfo[] getSystemAvailableFeatures() {
        throw new UnsupportedOperationException();
    }
    @Override
    public boolean hasSystemFeature(String name) {
        throw new UnsupportedOperationException();
    }
    @Override
    public boolean isSafeMode() {
        throw new UnsupportedOperationException();
    }
}
