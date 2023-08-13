@TestTargetClass(ActivityManager.MemoryInfo.class)
public class ActivityManagerMemoryInfoTest extends AndroidTestCase {
    protected ActivityManager.MemoryInfo mMemory;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mMemory = new ActivityManager.MemoryInfo();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "describeContents",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "ActivityManager.MemoryInfo",
            args = {}
        )
    })
    public void testDescribeContents() {
        assertEquals(0, mMemory.describeContents());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "writeToParcel",
        args = {android.os.Parcel.class, int.class}
    )
    public void testWriteToParcel() throws Exception {
        final long AVAILMEM = Process.getFreeMemory();
        mMemory.availMem = AVAILMEM;
        final long THRESHOLD = 500l;
        mMemory.threshold = THRESHOLD;
        final boolean LOWMEMORY = true;
        mMemory.lowMemory = LOWMEMORY;
        Parcel parcel = Parcel.obtain();
        mMemory.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        ActivityManager.MemoryInfo values =
            ActivityManager.MemoryInfo.CREATOR.createFromParcel(parcel);
        assertEquals(AVAILMEM, values.availMem);
        assertEquals(THRESHOLD, values.threshold);
        assertEquals(LOWMEMORY, values.lowMemory);
        try {
            mMemory.writeToParcel(null, 0);
            fail("writeToParcel should throw out NullPointerException when Parcel is null");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "readFromParcel",
        args = {android.os.Parcel.class}
    )
    public void testReadFromParcel() throws Exception {
        final long AVAILMEM = Process.getFreeMemory();
        mMemory.availMem = AVAILMEM;
        final long THRESHOLD = 500l;
        mMemory.threshold = THRESHOLD;
        final boolean LOWMEMORY = true;
        mMemory.lowMemory = LOWMEMORY;
        Parcel parcel = Parcel.obtain();
        mMemory.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        ActivityManager.MemoryInfo result = new ActivityManager.MemoryInfo();
        result.readFromParcel(parcel);
        assertEquals(AVAILMEM, result.availMem);
        assertEquals(THRESHOLD, result.threshold);
        assertEquals(LOWMEMORY, result.lowMemory);
        result = new ActivityManager.MemoryInfo();
        try {
            result.readFromParcel(null);
            fail("readFromParcel should throw out NullPointerException when Parcel is null");
        } catch (NullPointerException e) {
        }
    }
}
