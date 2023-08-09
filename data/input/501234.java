@TestTargetClass(MutableContextWrapper.class)
public class MutableContextWrapperTest extends AndroidTestCase {
    MutableContextWrapper mMutableContextWrapper;
    Context mContext;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mMutableContextWrapper = null;
        mContext = getContext();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "MutableContextWrapper",
        args = {android.content.Context.class}
    )
    public void testConstructor() {
        mMutableContextWrapper = new MutableContextWrapper(mContext);
        assertNotNull(mMutableContextWrapper);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setBaseContext",
        args = {android.content.Context.class}
    )
    public void testSetBaseContext() {
        mMutableContextWrapper = new MutableContextWrapper(mContext);
        assertTrue(mContext.equals(mMutableContextWrapper.getBaseContext()));
        MockActivity actitity = new MockActivity();
        Context base = actitity;
        mMutableContextWrapper.setBaseContext(base);
        assertTrue(base.equals(mMutableContextWrapper.getBaseContext()));
    }
}
