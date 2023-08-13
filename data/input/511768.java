@TestTargetClass(PermissionInfo.class)
public class PermissionInfoTest extends AndroidTestCase {
    private static final String PERMISSION_NAME = "android.permission.INTERNET";
    private static final String DEFAULT_DISCPRIPTION =
        "Allows an application to create network sockets.";
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test describeContents",
            method = "describeContents",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test loadDescription",
            method = "loadDescription",
            args = {android.content.pm.PackageManager.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructors",
            method = "PermissionInfo",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructors",
            method = "PermissionInfo",
            args = {android.content.pm.PermissionInfo.class}
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
    public void testPermissionInfo() throws NameNotFoundException {
        PackageManager pm = getContext().getPackageManager();
        Parcel p = Parcel.obtain();
        new PermissionInfo();
        PermissionInfo permissionInfo = pm.getPermissionInfo(PERMISSION_NAME, 0);
        PermissionInfo infoFromExisted = new PermissionInfo(permissionInfo);
        checkInfoSame(permissionInfo, infoFromExisted);
        assertNotNull(permissionInfo.toString());
        assertEquals(0, permissionInfo.describeContents());
        assertEquals(DEFAULT_DISCPRIPTION, permissionInfo.loadDescription(pm));
        permissionInfo.writeToParcel(p, 0);
        p.setDataPosition(0);
        PermissionInfo infoFromParcel = PermissionInfo.CREATOR.createFromParcel(p);
        checkInfoSame(permissionInfo, infoFromParcel);
        p.recycle();
    }
    private void checkInfoSame(PermissionInfo expected, PermissionInfo actual) {
        assertEquals(expected.name, actual.name);
        assertEquals(expected.group, actual.group);
        assertEquals(expected.descriptionRes, actual.descriptionRes);
        assertEquals(expected.protectionLevel, actual.protectionLevel);
        assertEquals(expected.nonLocalizedDescription, actual.nonLocalizedDescription);
    }
}
