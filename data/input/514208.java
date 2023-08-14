@TestTargetClass(PackageItemInfo.DisplayNameComparator.class)
public class PackageItemInfo_DisplayNameComparatorTest extends AndroidTestCase {
    private static final String PACKAGE_NAME = "com.android.cts.stub";
    private static final String ACTIVITY_NAME = "android.content.pm.cts.TestPmActivity";
    private static final String CMPACTIVITY_NAME = "android.content.pm.cts.TestPmCompare";
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test compare",
            method = "compare",
            args = {android.content.pm.PackageItemInfo.class,
                    android.content.pm.PackageItemInfo.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor",
            method = "PackageItemInfo.DisplayNameComparator",
            args = {android.content.pm.PackageManager.class}
        )
    })
    public void testDisplayNameComparator() throws NameNotFoundException {
        PackageManager pm = getContext().getPackageManager();
        DisplayNameComparator comparator = new DisplayNameComparator(pm);
        ComponentName componentName = new ComponentName(PACKAGE_NAME, ACTIVITY_NAME);
        ActivityInfo activityInfo = pm.getActivityInfo(componentName, 0);
        PackageItemInfo pkgItemInfo = new PackageItemInfo(activityInfo);
        componentName = new ComponentName(PACKAGE_NAME, CMPACTIVITY_NAME);
        activityInfo = pm.getActivityInfo(componentName, 0);
        PackageItemInfo cmpInfo = new PackageItemInfo(activityInfo);
        assertTrue(comparator.compare(pkgItemInfo, cmpInfo) < 0);
        assertTrue(comparator.compare(pkgItemInfo, pkgItemInfo) == 0);
        assertTrue(comparator.compare(cmpInfo, pkgItemInfo) > 0);
    }
}
