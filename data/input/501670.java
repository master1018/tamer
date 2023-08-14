@TestTargetClass(PackageStats.class)
public class PackageStatsTest extends AndroidTestCase {
    private static final String PACKAGE_NAME = "com.android.cts.stub";
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test describeContents",
            method = "describeContents",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructors",
            method = "PackageStats",
            args = {android.content.pm.PackageStats.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructors",
            method = "PackageStats",
            args = {android.os.Parcel.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructors",
            method = "PackageStats",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test toString",
            method = "toString",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test writeToParcel",
            method = "writeToParcel",
            args = {android.os.Parcel.class, int.class}
        )
    })
    public void testPackageStats() {
        long codeSize = 10000;
        long cacheSize = 10240;
        long dataSize = 4096;
        PackageStats stats = new PackageStats(PACKAGE_NAME);
        assertEquals(PACKAGE_NAME, stats.packageName);
        stats.cacheSize = codeSize;
        stats.codeSize = cacheSize;
        stats.dataSize = dataSize;
        PackageStats infoFromExisted = new PackageStats(stats);
        checkInfoSame(stats, infoFromExisted);
        assertNotNull(stats.toString());
        assertEquals(0, stats.describeContents());
        Parcel p = Parcel.obtain();
        stats.writeToParcel(p, 0);
        p.setDataPosition(0);
        PackageStats infoFromParcel = PackageStats.CREATOR.createFromParcel(p);
        checkInfoSame(stats, infoFromParcel);
        p.recycle();
    }
    private void checkInfoSame(PackageStats expected, PackageStats actual) {
        assertEquals(expected.packageName, actual.packageName);
        assertEquals(expected.cacheSize, actual.cacheSize);
        assertEquals(expected.dataSize, actual.dataSize);
        assertEquals(expected.codeSize, actual.codeSize);
    }
}
