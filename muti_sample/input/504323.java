@TestTargetClass(ApplicationInfo.class)
public class ApplicationInfoTest extends AndroidTestCase {
    private ApplicationInfo mApplicationInfo;
    private String mPackageName;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mPackageName = getContext().getPackageName();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "ApplicationInfo",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "ApplicationInfo",
            args = {android.content.pm.ApplicationInfo.class}
        )
    })
    public void testConstructor() {
        ApplicationInfo info = new ApplicationInfo();
        info.packageName = mPackageName;
        ApplicationInfo copy = new ApplicationInfo(info);
        assertEquals(info.packageName, copy.packageName);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "writeToParcel",
        args = {android.os.Parcel.class, int.class}
    )
    public void testWriteToParcel() throws NameNotFoundException {
        mApplicationInfo = mContext.getPackageManager().getApplicationInfo(mPackageName, 0);
        Parcel p = Parcel.obtain();
        mApplicationInfo.writeToParcel(p, 0);
        p.setDataPosition(0);
        ApplicationInfo info = ApplicationInfo.CREATOR.createFromParcel(p);
        assertEquals(mApplicationInfo.taskAffinity, info.taskAffinity);
        assertEquals(mApplicationInfo.permission, info.permission);
        assertEquals(mApplicationInfo.processName, info.processName);
        assertEquals(mApplicationInfo.className, info.className);
        assertEquals(mApplicationInfo.theme, info.theme);
        assertEquals(mApplicationInfo.flags, info.flags);
        assertEquals(mApplicationInfo.sourceDir, info.sourceDir);
        assertEquals(mApplicationInfo.publicSourceDir, info.publicSourceDir);
        assertEquals(mApplicationInfo.sharedLibraryFiles, info.sharedLibraryFiles);
        assertEquals(mApplicationInfo.dataDir, info.dataDir);
        assertEquals(mApplicationInfo.uid, info.uid);
        assertEquals(mApplicationInfo.enabled, info.enabled);
        assertEquals(mApplicationInfo.manageSpaceActivityName, info.manageSpaceActivityName);
        assertEquals(mApplicationInfo.descriptionRes, info.descriptionRes);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "toString",
        args = {}
    )
    public void testToString() {
        mApplicationInfo = new ApplicationInfo();
        assertNotNull(mApplicationInfo.toString());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "describeContents",
        args = {}
    )
    public void testDescribeContents() throws NameNotFoundException {
       mApplicationInfo = mContext.getPackageManager().getApplicationInfo(mPackageName, 0);
        assertEquals(0, mApplicationInfo.describeContents());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "dump",
        args = {android.util.Printer.class, java.lang.String.class}
    )
    public void testDump() {
        mApplicationInfo = new ApplicationInfo();
        StringBuilder sb = new StringBuilder();
        assertEquals(0, sb.length());
        StringBuilderPrinter p = new StringBuilderPrinter(sb);
        String prefix = "";
        mApplicationInfo.dump(p, prefix);
        assertNotNull(sb.toString());
        assertTrue(sb.length() > 0);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "loadDescription",
        args = {android.content.pm.PackageManager.class}
    )
    public void testLoadDescription() throws NameNotFoundException {
        mApplicationInfo = mContext.getPackageManager().getApplicationInfo(mPackageName, 0);
        assertNull(mApplicationInfo.loadDescription(mContext.getPackageManager()));
        mApplicationInfo.descriptionRes = R.string.hello_world;
        assertEquals(mContext.getResources().getString(R.string.hello_world),
                mApplicationInfo.loadDescription(mContext.getPackageManager()));
    }
}
