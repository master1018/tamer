@TestTargetClass(ResolveInfo.class)
public class ResolveInfoTest extends AndroidTestCase {
    private static final String PACKAGE_NAME = "com.android.cts.stub";
    private static final String MAIN_ACTION_NAME = "android.intent.action.MAIN";
    private static final String ACTIVITY_NAME = "android.content.pm.cts.TestPmActivity";
    private static final String SERVICE_NAME = "android.content.pm.cts.activity.PMTEST_SERVICE";
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test describeContents",
            method = "describeContents",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test getIconResource",
            method = "getIconResource",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test loadIcon",
            method = "loadIcon",
            args = {android.content.pm.PackageManager.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test loadLabel",
            method = "loadLabel",
            args = {android.content.pm.PackageManager.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor",
            method = "ResolveInfo",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test toString",
            method = "toString",
            args = {}
        )
    })
    public final void testResolveInfo() {
        new ResolveInfo();
        PackageManager pm = getContext().getPackageManager();
        Intent intent = new Intent(MAIN_ACTION_NAME);
        intent.setComponent(new ComponentName(PACKAGE_NAME, ACTIVITY_NAME));
        ResolveInfo resolveInfo = pm.resolveActivity(intent, 0);
        String expectedLabel = "Android TestCase";
        assertEquals(expectedLabel, resolveInfo.loadLabel(pm).toString());
        assertNotNull(resolveInfo.loadIcon(pm));
        assertTrue(resolveInfo.getIconResource() != 0);
        assertNotNull(resolveInfo.toString());
        assertEquals(0, resolveInfo.describeContents());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test dump",
            method = "dump",
            args = {android.util.Printer.class, java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test writeToParcel",
            method = "writeToParcel",
            args = {android.os.Parcel.class, int.class}
        )
    })
    public final void testDump() {
        PackageManager pm = getContext().getPackageManager();
        Intent intent = new Intent(SERVICE_NAME);
        ResolveInfo resolveInfo = pm.resolveService(intent, PackageManager.GET_RESOLVED_FILTER);
        Parcel p = Parcel.obtain();
        resolveInfo.writeToParcel(p, 0);
        p.setDataPosition(0);
        ResolveInfo infoFromParcel = ResolveInfo.CREATOR.createFromParcel(p);
        assertEquals(resolveInfo.getIconResource(), infoFromParcel.getIconResource());
        assertEquals(resolveInfo.priority, infoFromParcel.priority);
        assertEquals(resolveInfo.preferredOrder, infoFromParcel.preferredOrder);
        assertEquals(resolveInfo.match, infoFromParcel.match);
        assertEquals(resolveInfo.specificIndex, infoFromParcel.specificIndex);
        assertEquals(resolveInfo.labelRes, infoFromParcel.labelRes);
        assertEquals(resolveInfo.nonLocalizedLabel, infoFromParcel.nonLocalizedLabel);
        assertEquals(resolveInfo.icon, infoFromParcel.icon);
        TestPrinter printer = new TestPrinter();
        String prefix = "TestResolveInfo";
        resolveInfo.dump(printer, prefix);
        assertTrue(printer.isPrintlnCalled);
    }
    private class TestPrinter implements Printer {
        public boolean isPrintlnCalled;
        public void println(String x) {
            isPrintlnCalled = true;
        }
    }
}
