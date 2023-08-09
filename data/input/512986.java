public class Focus2ActivityTest extends ActivityInstrumentationTestCase2<Focus2> {
    private Button mLeftButton;
    private Button mCenterButton;
    private Button mRightButton;
    public Focus2ActivityTest() {
        super(Focus2.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        final Focus2 a = getActivity();
        assertNotNull(a);
        mLeftButton = (Button) a.findViewById(R.id.leftButton);
        mCenterButton = (Button) a.findViewById(R.id.centerButton);
        mRightButton = (Button) a.findViewById(R.id.rightButton);
    }
    @MediumTest
    public void testPreconditions() {
        assertTrue("center button should be right of left button",
                mLeftButton.getRight() < mCenterButton.getLeft());
        assertTrue("right button should be right of center button",
                mCenterButton.getRight() < mRightButton.getLeft());
        assertTrue("left button should be focused", mLeftButton.isFocused());
    }
    @MediumTest
    public void testGoingRightFromLeftButtonJumpsOverCenterToRight() {
        sendKeys(KeyEvent.KEYCODE_DPAD_RIGHT);
        assertTrue("right button should be focused", mRightButton.isFocused());
    }
    @MediumTest
    public void testGoingLeftFromRightButtonGoesToCenter()  {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                mRightButton.requestFocus();
            }
        });
        getInstrumentation().waitForIdleSync();
        assertTrue(mRightButton.isFocused());
        sendKeys(KeyEvent.KEYCODE_DPAD_LEFT);
        assertTrue("center button should be focused", mCenterButton.isFocused());
    }
}
