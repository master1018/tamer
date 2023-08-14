@TestTargetClass(PackageItemInfo.class)
public class PackageItemInfoTest extends AndroidTestCase {
    private static final String PACKAGE_NAME = "com.android.cts.stub";
    private static final String ACTIVITY_NAME = "android.content.pm.cts.TestPmActivity";
    private static final String METADATA_NAME = "android.content.pm.cts.xmltest";
    private PackageManager mPackageManager;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mPackageManager = getContext().getPackageManager();
    }
    @TestTargets({
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
            notes = "Test loadXmlMetaData",
            method = "loadXmlMetaData",
            args = {android.content.pm.PackageManager.class, java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructors",
            method = "PackageItemInfo",
            args = {android.content.pm.PackageItemInfo.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructors",
            method = "PackageItemInfo",
            args = {}
        )
    })
    public void testLoadMethods() throws NameNotFoundException {
        ActivityInfo activityInfo = (ActivityInfo) getTestItemInfo();
        new PackageItemInfo();
        PackageItemInfo pkgItemInfo = new PackageItemInfo(activityInfo);
        checkInfoSame(activityInfo, pkgItemInfo);
        assertEquals(ACTIVITY_NAME, pkgItemInfo.loadLabel(mPackageManager));
        assertNotNull(pkgItemInfo.loadIcon(mPackageManager));
        XmlResourceParser parser = pkgItemInfo.loadXmlMetaData(mPackageManager, METADATA_NAME);
        assertNotNull(parser);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test dumpBack",
            method = "dumpBack",
            args = {android.util.Printer.class, java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test dumpFront",
            method = "dumpFront",
            args = {android.util.Printer.class, java.lang.String.class}
        )
    })
    public void testDump() {
        MockPackageItemInfo pkgItemInfo = new MockPackageItemInfo();
        MockPrinter printer = new MockPrinter();
        pkgItemInfo.dumpBack(printer, "");
        String prefix = "PackageItemInfoTest";
        pkgItemInfo.dumpFront(printer, prefix);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test writeToParcel",
            method = "writeToParcel",
            args = {android.os.Parcel.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructors",
            method = "PackageItemInfo",
            args = {Parcel.class}
        )
    })
    public void testWriteToParcel() throws NameNotFoundException {
        ActivityInfo activityInfo = (ActivityInfo) getTestItemInfo();
        PackageItemInfo expectedInfo = new PackageItemInfo(activityInfo);
        Parcel p = Parcel.obtain();
        expectedInfo.writeToParcel(p, 0);
        p.setDataPosition(0);
        MockPackageItemInfo infoFromParcel = new MockPackageItemInfo(p);
        checkInfoSame(expectedInfo, infoFromParcel);
        p.recycle();
    }
    private void checkInfoSame(PackageItemInfo expected, PackageItemInfo actual) {
        assertEquals(expected.name, actual.name);
        assertEquals(expected.packageName, actual.packageName);
        assertEquals(expected.labelRes, actual.labelRes);
        assertEquals(expected.nonLocalizedLabel, actual.nonLocalizedLabel);
        assertEquals(expected.icon, actual.icon);
        assertEquals(expected.metaData.size(), actual.metaData.size());
        assertEquals(R.xml.pm_test, actual.metaData.getInt(METADATA_NAME));
    }
    private PackageItemInfo getTestItemInfo() throws NameNotFoundException {
        ComponentName componentName = new ComponentName(PACKAGE_NAME, ACTIVITY_NAME);
        ActivityInfo activityInfo =
            mPackageManager.getActivityInfo(componentName, PackageManager.GET_META_DATA);
        return activityInfo;
    }
    private class MockPackageItemInfo extends PackageItemInfo {
        public MockPackageItemInfo() {
            super();
        }
        public MockPackageItemInfo(PackageItemInfo orig) {
            super(orig);
        }
        public MockPackageItemInfo(Parcel source) {
            super(source);
        }
        public void dumpFront(Printer pw, String prefix) {
            super.dumpFront(pw, prefix);
        }
        public void dumpBack(Printer pw, String prefix) {
            super.dumpBack(pw, prefix);
        }
    }
    private class MockPrinter implements Printer {
        public void println(String x) {
        }
    }
}
