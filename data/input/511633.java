@TestTargetClass(DisplayNameComparator.class)
public class ApplicationInfo_DisplayNameComparatorTest extends AndroidTestCase {
    private static final String PACKAGE_NAME = "com.android.cts.stub";
    DisplayNameComparator mDisplayNameComparator;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mDisplayNameComparator = null;
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test constructor(s) of {@link ApplicationInfo.DisplayNameComparator}",
        method = "ApplicationInfo.DisplayNameComparator",
        args = {android.content.pm.PackageManager.class}
    )
    public void testConstructor() {
        PackageManager pm = getContext().getPackageManager();
        new DisplayNameComparator(pm);
        new DisplayNameComparator(null);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test compare(ApplicationInfo, ApplicationInfo)",
        method = "compare",
        args = {android.content.pm.ApplicationInfo.class, android.content.pm.ApplicationInfo.class}
    )
    @ToBeFixed(bug = "1417734", explanation = "NPE is not expected.")
    public void testCompare() throws NameNotFoundException {
        PackageManager pm = getContext().getPackageManager();
        mDisplayNameComparator = new ApplicationInfo.DisplayNameComparator(pm);
        ApplicationInfo info1 = new ApplicationInfo();
        ApplicationInfo info2 = new ApplicationInfo();
        info1.packageName = PACKAGE_NAME;
        info2.packageName = PACKAGE_NAME;
        assertEquals(0, mDisplayNameComparator.compare(info1, info2));
        info1 = mContext.getPackageManager().getApplicationInfo(PACKAGE_NAME, 0);
        info2.packageName = PACKAGE_NAME + ".2";
        assertTrue((mDisplayNameComparator.compare(info1, info2) < 0));
        info1 = new ApplicationInfo();
        info1.packageName = PACKAGE_NAME + ".1";
        info2 = mContext.getPackageManager().getApplicationInfo(PACKAGE_NAME, 0);
        assertTrue((mDisplayNameComparator.compare(info1, info2) > 0));
        try {
            mDisplayNameComparator.compare(null, null);
            fail("should throw NullPointerException.");
        } catch (NullPointerException e) {
        }
    }
}
