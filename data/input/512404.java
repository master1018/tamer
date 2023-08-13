@TestTargetClass(ShortcutIconResource.class)
public class Intent_ShortcutIconResourceTest extends AndroidTestCase {
    ShortcutIconResource mShortcutIconResource;
    Context mContext;
    final int resourceId = com.android.cts.stub.R.string.notify;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mShortcutIconResource = null;
        mContext = getContext();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "toString",
        args = {}
    )
    public void testToString() {
        String resourceName = mContext.getResources().getResourceName(
                resourceId);
        mShortcutIconResource = ShortcutIconResource.fromContext(mContext,
                resourceId);
        assertNotNull(mShortcutIconResource);
        assertNotNull(mShortcutIconResource.toString());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "fromContext",
        args = {android.content.Context.class, int.class}
    )
    public void testFromContext() {
        String resourceName = mContext.getResources().getResourceName(
                resourceId);
        mShortcutIconResource = ShortcutIconResource.fromContext(mContext,
                resourceId);
        assertNotNull(mShortcutIconResource);
        assertEquals(resourceName, mShortcutIconResource.resourceName);
        assertEquals(mContext.getPackageName(),
                mShortcutIconResource.packageName);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "writeToParcel",
        args = {android.os.Parcel.class, int.class}
    )
    public void testWriteToParcel() {
        mShortcutIconResource = ShortcutIconResource.fromContext(mContext,
                com.android.cts.stub.R.string.notify);
        assertNotNull(mShortcutIconResource);
        Parcel parce = Parcel.obtain();
        mShortcutIconResource.writeToParcel(parce, 1);
        parce.setDataPosition(0);
        ShortcutIconResource target = ShortcutIconResource.CREATOR
                .createFromParcel(parce);
        assertEquals(mShortcutIconResource.packageName, target.packageName);
        assertEquals(mShortcutIconResource.resourceName, target.resourceName);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "describeContents",
        args = {}
    )
    public void testDescribeContents() {
        int expected = 0;
        mShortcutIconResource = new Intent.ShortcutIconResource();
        assertNotNull(mShortcutIconResource);
        assertEquals(expected, mShortcutIconResource.describeContents());
    }
}
