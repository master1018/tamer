@TestTargetClass(PermissionGroupInfo.class)
public class PermissionGroupInfoTest extends AndroidTestCase {
    private static final String PERMISSIONGROUP_NAME = "android.permission-group.COST_MONEY";
    private static final String DEFAULT_DISCRIPTION =
        "Allow applications to do things that can cost you money.";
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
            method = "PermissionGroupInfo",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructors",
            method = "PermissionGroupInfo",
            args = {android.content.pm.PermissionGroupInfo.class}
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
    public void testPermissionGroupInfo() throws NameNotFoundException {
        PackageManager pm = getContext().getPackageManager();
        Parcel p = Parcel.obtain();
        new PermissionGroupInfo();
        PermissionGroupInfo permissionGroupInfo = pm
                .getPermissionGroupInfo(PERMISSIONGROUP_NAME, 0);
        PermissionGroupInfo infoFromExisted = new PermissionGroupInfo(permissionGroupInfo);
        checkInfoSame(permissionGroupInfo, infoFromExisted);
        assertNotNull(permissionGroupInfo.toString());
        assertEquals(0, permissionGroupInfo.describeContents());
        assertEquals(DEFAULT_DISCRIPTION, permissionGroupInfo.loadDescription(pm));
        permissionGroupInfo.writeToParcel(p, 0);
        p.setDataPosition(0);
        PermissionGroupInfo infoFromParcel = PermissionGroupInfo.CREATOR.createFromParcel(p);
        checkInfoSame(permissionGroupInfo, infoFromParcel);
        p.recycle();
    }
    private void checkInfoSame(PermissionGroupInfo expected, PermissionGroupInfo actual) {
        assertEquals(expected.descriptionRes, actual.descriptionRes);
        assertEquals(expected.nonLocalizedDescription, actual.nonLocalizedDescription);
    }
}
