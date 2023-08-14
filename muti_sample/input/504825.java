@TestTargetClass(InstrumentationInfo.class)
public class InstrumentationInfoTest extends AndroidTestCase {
    private static final String PACKAGE_NAME = "com.android.cts.stub";
    private static final String INSTRUMENTATION_NAME =
            "android.content.pm.cts.TestPmInstrumentation";
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
            method = "InstrumentationInfo",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructors",
            method = "InstrumentationInfo",
            args = {android.content.pm.InstrumentationInfo.class}
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
    public void testInstrumentationInfo() throws NameNotFoundException {
        PackageManager pm = getContext().getPackageManager();
        ComponentName componentName = new ComponentName(PACKAGE_NAME, INSTRUMENTATION_NAME);
        Parcel p = Parcel.obtain();
        new InstrumentationInfo();
        InstrumentationInfo instrInfo = pm.getInstrumentationInfo(componentName, 0);
        InstrumentationInfo infoFromExisted = new InstrumentationInfo(instrInfo);
        checkInfoSame(instrInfo, infoFromExisted);
        assertNotNull(instrInfo.toString());
        assertEquals(0, instrInfo.describeContents());
        instrInfo.writeToParcel(p, 0);
        p.setDataPosition(0);
        InstrumentationInfo infoFromParcel = InstrumentationInfo.CREATOR.createFromParcel(p);
        checkInfoSame(instrInfo, infoFromParcel);
        p.recycle();
    }
    private void checkInfoSame(InstrumentationInfo expected, InstrumentationInfo actual) {
        assertEquals(expected.name, actual.name);
        assertEquals(expected.dataDir, actual.dataDir);
        assertEquals(expected.handleProfiling, actual.handleProfiling);
        assertEquals(expected.functionalTest, actual.functionalTest);
        assertEquals(expected.targetPackage, actual.targetPackage);
        assertEquals(expected.sourceDir, actual.sourceDir);
        assertEquals(expected.publicSourceDir, actual.publicSourceDir);
    }
}
