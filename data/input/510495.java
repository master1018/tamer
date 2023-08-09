@TestTargetClass(ActivityManager.RunningServiceInfo.class)
public class ActivityManager_RunningServiceInfoTest extends AndroidTestCase {
    private ActivityManager.RunningServiceInfo mRunningServiceInfo;
    private ComponentName mService;
    private static final String PROCESS = "process";
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mRunningServiceInfo = new ActivityManager.RunningServiceInfo();
        mService = new ComponentName(getContext(), MockActivity.class);
        mRunningServiceInfo.service = mService;
        mRunningServiceInfo.pid = 1;
        mRunningServiceInfo.process = PROCESS;
        mRunningServiceInfo.foreground = true;
        mRunningServiceInfo.activeSince = 1l;
        mRunningServiceInfo.started = true;
        mRunningServiceInfo.clientCount = 2;
        mRunningServiceInfo.crashCount = 1;
        mRunningServiceInfo.lastActivityTime = 1l;
        mRunningServiceInfo.restarting = 1l;
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "ActivityManager.RunningServiceInfo",
        args = {}
    )
    public void testConstructor() {
        new ActivityManager.RunningServiceInfo();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "describeContents",
        args = {}
    )
    public void testDescribeContents() {
        assertEquals(0, mRunningServiceInfo.describeContents());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "writeToParcel",
        args = {android.os.Parcel.class, int.class}
    )
    public void testWriteToParcel() throws Exception {
        Parcel parcel = Parcel.obtain();
        mRunningServiceInfo.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        ActivityManager.RunningServiceInfo values =
            ActivityManager.RunningServiceInfo.CREATOR.createFromParcel(parcel);
        assertEquals(mService, values.service);
        assertEquals(1, values.pid);
        assertEquals(PROCESS, values.process);
        assertTrue(values.foreground);
        assertEquals(1l, values.activeSince);
        assertTrue(values.started);
        assertEquals(2, values.clientCount);
        assertEquals(1, values.crashCount);
        assertEquals(1l, values.lastActivityTime);
        assertEquals(1l, values.restarting);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "readFromParcel",
        args = {android.os.Parcel.class}
    )
    public void testReadFromParcel() throws Exception {
        Parcel parcel = Parcel.obtain();
        mRunningServiceInfo.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        ActivityManager.RunningServiceInfo values =
            new ActivityManager.RunningServiceInfo();
        values.readFromParcel(parcel);
        assertEquals(mService, values.service);
        assertEquals(1, values.pid);
        assertEquals(PROCESS, values.process);
        assertTrue(values.foreground);
        assertEquals(1l, values.activeSince);
        assertTrue(values.started);
        assertEquals(2, values.clientCount);
        assertEquals(1, values.crashCount);
        assertEquals(1l, values.lastActivityTime);
        assertEquals(1l, values.restarting);
    }
}
