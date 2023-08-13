@TestTargetClass(ActivityManager.RunningAppProcessInfo.class)
public class ActivityManager_RunningAppProcessInfoTest extends AndroidTestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "describeContents",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "readFromParcel",
            args = {android.os.Parcel.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "ActivityManager.RunningAppProcessInfo",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "ActivityManager.RunningAppProcessInfo",
            args = {java.lang.String.class, int.class, java.lang.String[].class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "writeToParcel",
            args = {android.os.Parcel.class, int.class}
        )
    })
    public void testRunningAppProcessInfo() {
        new RunningAppProcessInfo();
        new RunningAppProcessInfo("test", 100, new String[]{"com.android", "com.android.test"});
        final ActivityManager am = (ActivityManager)
                    getContext().getSystemService(Context.ACTIVITY_SERVICE);
        final List<RunningAppProcessInfo> list = am.getRunningAppProcesses();
        final RunningAppProcessInfo rap = list.get(0);
        assertEquals(0, rap.describeContents());
        final Parcel p = Parcel.obtain();
        rap.writeToParcel(p, 0);
        final RunningAppProcessInfo r = new RunningAppProcessInfo();
        p.setDataPosition(0);
        r.readFromParcel(p);
        assertEquals(rap.pid, r.pid);
        assertEquals(rap.processName, r.processName);
        assertEquals(rap.pkgList.length, r.pkgList.length);
        for (int i = 0; i < rap.pkgList.length; i++) {
            assertEquals(rap.pkgList[i], r.pkgList[i]);
        }
    }
}
