public class GlobalFocusChangeTest extends ActivityInstrumentationTestCase<GlobalFocusChange> {
    private GlobalFocusChange mActivity;
    private View mLeft;
    private View mRight;
    public GlobalFocusChangeTest() {
        super("com.android.frameworks.coretests", GlobalFocusChange.class);
    }
    @Override
    public void setUp() throws Exception {
        super.setUp();
        mActivity = getActivity();
        mLeft = mActivity.findViewById(R.id.left);
        mRight = mActivity.findViewById(R.id.right);
    }
    @Override
    protected void tearDown() throws Exception {
        mActivity.reset();
        super.tearDown();
    }
    @FlakyTest(tolerance = 4)
    @LargeTest
    public void testFocusChange() throws Exception {
        sendKeys(KeyEvent.KEYCODE_DPAD_RIGHT);
        assertFalse(mLeft.isFocused());
        assertTrue(mRight.isFocused());
        assertSame(mLeft, mActivity.mOldFocus);
        assertSame(mRight, mActivity.mNewFocus);        
    }
    @FlakyTest(tolerance = 4)
    @MediumTest
    public void testEnterTouchMode() throws Exception {
        assertTrue(mLeft.isFocused());
        TouchUtils.tapView(this, mLeft);
        assertSame(mLeft, mActivity.mOldFocus);
        assertSame(null, mActivity.mNewFocus);        
    }
    @FlakyTest(tolerance = 4)
    @MediumTest
    public void testLeaveTouchMode() throws Exception {
        assertTrue(mLeft.isFocused());
        TouchUtils.tapView(this, mLeft);
        sendKeys(KeyEvent.KEYCODE_DPAD_RIGHT);
        assertTrue(mLeft.isFocused());
        assertSame(null, mActivity.mOldFocus);
        assertSame(mLeft, mActivity.mNewFocus);
    }
}
