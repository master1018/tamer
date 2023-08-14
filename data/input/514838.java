@TestTargetClass(ProviderInfo.class)
public class ProviderInfoTest extends AndroidTestCase {
    private static final String PACKAGE_NAME = "com.android.cts.stub";
    private static final String PROVIDER_NAME = "android.content.cts.MockContentProvider";
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
            method = "ProviderInfo",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructors",
            method = "ProviderInfo",
            args = {android.content.pm.ProviderInfo.class}
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
    public void testProviderInfo() throws NameNotFoundException {
        PackageManager pm = getContext().getPackageManager();
        Parcel p = Parcel.obtain();
        new ProviderInfo();
        ApplicationInfo appInfo = pm.getApplicationInfo(PACKAGE_NAME, 0);
        List<ProviderInfo> providers = pm.queryContentProviders(PACKAGE_NAME, appInfo.uid, 0);
        Iterator<ProviderInfo> providerIterator = providers.iterator();
        ProviderInfo current;
        while (providerIterator.hasNext()) {
            current = providerIterator.next();
            if (current.name.equals(PROVIDER_NAME)) {
                checkProviderInfoMethods(current, p);
                break;
            }
        }
    }
    private void checkProviderInfoMethods(ProviderInfo providerInfo, Parcel p) {
        assertNotNull(providerInfo.toString());
        assertEquals(0, providerInfo.describeContents());
        ProviderInfo infoFromExisted = new ProviderInfo(providerInfo);
        checkInfoSame(providerInfo, infoFromExisted);
        providerInfo.writeToParcel(p, 0);
        p.setDataPosition(0);
        ProviderInfo infoFromParcel = ProviderInfo.CREATOR.createFromParcel(p);
        checkInfoSame(providerInfo, infoFromParcel);
        p.recycle();
    }
    private void checkInfoSame(ProviderInfo expected, ProviderInfo actual) {
        assertEquals(expected.name, actual.name);
        assertEquals(expected.authority, actual.authority);
        assertEquals(expected.readPermission, actual.readPermission);
        assertEquals(expected.writePermission, actual.writePermission);
        assertEquals(expected.grantUriPermissions, actual.grantUriPermissions);
        assertEquals(expected.uriPermissionPatterns, actual.uriPermissionPatterns);
        assertEquals(expected.multiprocess, actual.multiprocess);
        assertEquals(expected.initOrder, actual.initOrder);
        assertEquals(expected.isSyncable, actual.isSyncable);
    }
}
