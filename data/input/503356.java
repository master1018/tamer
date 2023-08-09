@TestTargetClass(ResolveInfo.DisplayNameComparator.class)
public class ResolveInfo_DisplayNameComparatorTest extends AndroidTestCase {
    private static final String MAIN_ACTION_NAME = "android.intent.action.MAIN";
    private static final String SERVICE_NAME = "android.content.pm.cts.activity.PMTEST_SERVICE";
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test compare",
            method = "compare",
            args = {android.content.pm.ResolveInfo.class, android.content.pm.ResolveInfo.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor",
            method = "ResolveInfo.DisplayNameComparator",
            args = {android.content.pm.PackageManager.class}
        )
    })
    public void testDisplayNameComparator() {
        PackageManager pm = getContext().getPackageManager();
        DisplayNameComparator dnc = new DisplayNameComparator(pm);
        Intent intent = new Intent(MAIN_ACTION_NAME);
        ResolveInfo activityInfo = pm.resolveActivity(intent, 0);
        intent = new Intent(SERVICE_NAME);
        ResolveInfo serviceInfo = pm.resolveService(intent, PackageManager.GET_RESOLVED_FILTER);
        assertTrue(dnc.compare(activityInfo, serviceInfo) < 0);
        assertTrue(dnc.compare(activityInfo, activityInfo) == 0);
        assertTrue(dnc.compare(serviceInfo, activityInfo) > 0);
    }
}
