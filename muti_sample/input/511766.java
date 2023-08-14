@TestTargetClass(ActivityManager.ProcessErrorStateInfo.class)
public class ActivityManagerProcessErrorStateInfoTest extends AndroidTestCase {
    protected ActivityManager.ProcessErrorStateInfo mErrorStateInfo;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mErrorStateInfo = new ActivityManager.ProcessErrorStateInfo();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test constructor",
        method = "ActivityManager.ProcessErrorStateInfo",
        args = {}
    )
    public void testConstructor() {
        new ActivityManager.ProcessErrorStateInfo();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "describeContents",
        args = {}
    )
    public void testDescribeContents() {
        assertEquals(0, mErrorStateInfo.describeContents());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "writeToParcel",
        args = {android.os.Parcel.class, int.class}
    )
    public void testWriteToParcel() throws Exception {
        int condition = 1;
        String processName = "processName";
        int pid = 2;
        int uid = 3;
        String tag = "tag";
        String shortMsg = "shortMsg";
        String longMsg = "longMsg";
        mErrorStateInfo.condition = condition;
        mErrorStateInfo.processName = processName;
        mErrorStateInfo.pid = pid;
        mErrorStateInfo.uid = uid;
        mErrorStateInfo.tag = tag;
        mErrorStateInfo.shortMsg = shortMsg;
        mErrorStateInfo.longMsg = longMsg;
        Parcel parcel = Parcel.obtain();
        mErrorStateInfo.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        ActivityManager.ProcessErrorStateInfo values =
            ActivityManager.ProcessErrorStateInfo.CREATOR.createFromParcel(parcel);
        assertEquals(condition, values.condition);
        assertEquals(processName, values.processName);
        assertEquals(pid, values.pid);
        assertEquals(uid, values.uid);
        assertEquals(tag, values.tag);
        assertEquals(shortMsg, values.shortMsg);
        assertEquals(longMsg, values.longMsg);
        assertNull(values.crashData);  
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test readFromParcel method",
        method = "readFromParcel",
        args = {android.os.Parcel.class}
    )
    public void testReadFromParcel() throws Exception {
        int condition = 1;
        String processName = "processName";
        int pid = 2;
        int uid = 3;
        String tag = "tag";
        String shortMsg = "shortMsg";
        String longMsg = "longMsg";
        mErrorStateInfo.condition = condition;
        mErrorStateInfo.processName = processName;
        mErrorStateInfo.pid = pid;
        mErrorStateInfo.uid = uid;
        mErrorStateInfo.tag = tag;
        mErrorStateInfo.shortMsg = shortMsg;
        mErrorStateInfo.longMsg = longMsg;
        Parcel parcel = Parcel.obtain();
        mErrorStateInfo.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        ActivityManager.ProcessErrorStateInfo values = new ActivityManager.ProcessErrorStateInfo();
        values.readFromParcel(parcel);
        assertEquals(condition, values.condition);
        assertEquals(processName, values.processName);
        assertEquals(pid, values.pid);
        assertEquals(uid, values.uid);
        assertEquals(tag, values.tag);
        assertEquals(shortMsg, values.shortMsg);
        assertEquals(longMsg, values.longMsg);
        assertNull(values.crashData);  
    }
}
