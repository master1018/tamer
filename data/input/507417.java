@TestTargetClass(ActivityManager.RecentTaskInfo.class)
public class ActivityManagerRecentTaskInfoTest extends AndroidTestCase {
    protected ActivityManager.RecentTaskInfo mRecentTaskInfo;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mRecentTaskInfo = new ActivityManager.RecentTaskInfo();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test constructor",
        method = "ActivityManager.RecentTaskInfo",
        args = {}
    )
    public void testConstructor() {
        new ActivityManager.RecentTaskInfo();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "describeContents",
        args = {}
    )
    public void testDescribeContents() {
        assertEquals(0, mRecentTaskInfo.describeContents());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "writeToParcel",
        args = {android.os.Parcel.class, int.class}
    )
    public void testWriteToParcel() throws Exception {
        int id = 1;
        Intent baseIntent = null;
        ComponentName origActivity = null;
        mRecentTaskInfo.id = id;
        mRecentTaskInfo.baseIntent = baseIntent;
        mRecentTaskInfo.origActivity = origActivity;
        Parcel parcel = Parcel.obtain();
        mRecentTaskInfo.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        ActivityManager.RecentTaskInfo values = ActivityManager.RecentTaskInfo.CREATOR
                .createFromParcel(parcel);
        assertEquals(id, values.id);
        assertEquals(null, values.baseIntent);
        assertEquals(null, values.origActivity);
        baseIntent = new Intent();
        baseIntent.setAction(Intent.ACTION_CALL);
        origActivity = new ComponentName(mContext, this.getClass());
        mRecentTaskInfo.id = -1;
        mRecentTaskInfo.baseIntent = baseIntent;
        mRecentTaskInfo.origActivity = origActivity;
        parcel = Parcel.obtain();
        mRecentTaskInfo.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        values = ActivityManager.RecentTaskInfo.CREATOR
                .createFromParcel(parcel);
        assertEquals(-1, values.id);
        assertNotNull(values.baseIntent);
        assertEquals(Intent.ACTION_CALL, values.baseIntent.getAction());
        assertEquals(origActivity, values.origActivity);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "readFromParcel",
        args = {android.os.Parcel.class}
    )
    public void testReadFromParcel() throws Exception {
        int id = 1;
        Intent baseIntent = null;
        ComponentName origActivity = null;
        mRecentTaskInfo.id = id;
        mRecentTaskInfo.baseIntent = baseIntent;
        mRecentTaskInfo.origActivity = origActivity;
        Parcel parcel = Parcel.obtain();
        mRecentTaskInfo.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        ActivityManager.RecentTaskInfo values = new ActivityManager.RecentTaskInfo();
        values.readFromParcel(parcel);
        assertEquals(id, values.id);
        assertEquals(null, values.baseIntent);
        assertEquals(null, values.origActivity);
        baseIntent = new Intent();
        baseIntent.setAction(Intent.ACTION_CALL);
        origActivity = new ComponentName(mContext, this.getClass());
        mRecentTaskInfo.id = -1;
        mRecentTaskInfo.baseIntent = baseIntent;
        mRecentTaskInfo.origActivity = origActivity;
        parcel = Parcel.obtain();
        mRecentTaskInfo.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        values.readFromParcel(parcel);
        assertEquals(-1, values.id);
        assertNotNull(values.baseIntent);
        assertEquals(Intent.ACTION_CALL, values.baseIntent.getAction());
        assertEquals(origActivity, values.origActivity);
    }
}
