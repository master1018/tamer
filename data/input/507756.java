public class RequestRectangleVisibleTest extends ActivityInstrumentationTestCase<RequestRectangleVisible> {
    private ScrollView mScrollView;
    private Button mClickToScrollFromAbove;
    private Button mClickToScrollToUpperBlob;
    private TextView mTopBlob;
    private View mChildToScrollTo;
    private TextView mBottomBlob;
    private Button mClickToScrollToBlobLowerBlob;
    private Button mClickToScrollFromBelow;
    public RequestRectangleVisibleTest() {
        super("com.android.frameworks.coretests", RequestRectangleVisible.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        RequestRectangleVisible a = getActivity();
        mScrollView = (ScrollView) a.findViewById(R.id.scrollView);
        mClickToScrollFromAbove = (Button) a.findViewById(R.id.scrollToRectFromTop);
        mClickToScrollToUpperBlob = (Button) a.findViewById(R.id.scrollToRectFromTop2);
        mTopBlob = (TextView) a.findViewById(R.id.topBlob);
        mChildToScrollTo = a.findViewById(R.id.childToMakeVisible);
        mBottomBlob = (TextView) a.findViewById(R.id.bottomBlob);
        mClickToScrollToBlobLowerBlob = (Button) a.findViewById(R.id.scrollToRectFromBottom2);
        mClickToScrollFromBelow = (Button) a.findViewById(R.id.scrollToRectFromBottom);
    }
    @MediumTest
    public void testPreconditions() {
        assertNotNull(mScrollView);
        assertNotNull(mClickToScrollFromAbove);
        assertNotNull(mClickToScrollToUpperBlob);
        assertNotNull(mTopBlob);
        assertNotNull(mChildToScrollTo);
        assertNotNull(mBottomBlob);
        assertNotNull(mClickToScrollToBlobLowerBlob);
        assertNotNull(mClickToScrollFromBelow);
        assertTrue("top blob needs to be taller than the screen for many of the "
                + "tests below to work.",
                mTopBlob.getHeight() > mScrollView.getHeight());
        assertTrue("bottom blob needs to be taller than the screen for many of the "
                + "tests below to work.",
                mBottomBlob.getHeight() > mScrollView.getHeight());
        assertTrue("top blob needs to be lower than the fading edge region",
                mTopBlob.getTop() > mScrollView.getVerticalFadingEdgeLength());
    }
    @MediumTest
    public void testScrollToOffScreenRectangleFromTop() {
        assertTrue(mClickToScrollFromAbove.hasFocus());
        ViewAsserts.assertOffScreenBelow(mScrollView, mChildToScrollTo);
        sendKeys(KeyEvent.KEYCODE_DPAD_CENTER);
        getInstrumentation().waitForIdleSync();  
        ViewAsserts.assertOnScreen(mScrollView, mChildToScrollTo);
        ViewAsserts.assertHasScreenCoordinates(
                mScrollView, mChildToScrollTo,
                0,
                mScrollView.getHeight()
                        - mChildToScrollTo.getHeight()
                        - mScrollView.getVerticalFadingEdgeLength());
    }
    @MediumTest
    public void testScrollToPartiallyOffScreenRectFromTop() {
        pressDownUntilViewInFocus(mClickToScrollToUpperBlob, 4);
        assertOnBottomEdgeOfScreen(mScrollView, mTopBlob);
        sendKeys(KeyEvent.KEYCODE_DPAD_CENTER);
        getInstrumentation().waitForIdleSync();  
        ViewAsserts.assertHasScreenCoordinates(
                mScrollView, mTopBlob, 0, mScrollView.getVerticalFadingEdgeLength());
    }
    @LargeTest
    public void testScrollToOffScreenRectangleFromBottom() {
        pressDownUntilViewInFocus(mClickToScrollFromBelow, 10);
        assertTrue(mClickToScrollFromBelow.hasFocus());
        ViewAsserts.assertOffScreenAbove(mScrollView, mChildToScrollTo);
        sendKeys(KeyEvent.KEYCODE_DPAD_CENTER);
        getInstrumentation().waitForIdleSync();  
        ViewAsserts.assertOnScreen(mScrollView, mChildToScrollTo);
        ViewAsserts.assertHasScreenCoordinates(
                mScrollView, mChildToScrollTo, 0, mScrollView.getVerticalFadingEdgeLength());
    }
    @LargeTest
    public void testScrollToPartiallyOffScreenRectFromBottom() {
        pressDownUntilViewInFocus(mClickToScrollToBlobLowerBlob, 10);
        assertOnTopEdgeOfScreen(mScrollView, mBottomBlob);
        sendKeys(KeyEvent.KEYCODE_DPAD_CENTER);
        getInstrumentation().waitForIdleSync();  
        ViewAsserts.assertHasScreenCoordinates(
                mScrollView, mBottomBlob,
                0,
                mScrollView.getHeight() - mBottomBlob.getHeight()
                    - mScrollView.getVerticalFadingEdgeLength());
    }
    private void pressDownUntilViewInFocus(View view, int maxKeyPress) {
        int count = 0;
        while(!view.hasFocus()) {
            sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
            getInstrumentation().waitForIdleSync();
            if (++count > maxKeyPress) {
                fail("couldn't move down to bottom button within "
                        + maxKeyPress + " key presses.");
            }
        }
    }
    static public void assertOnBottomEdgeOfScreen(View origin, View view) {
        int[] xy = new int[2];
        view.getLocationOnScreen(xy);
        int[] xyRoot = new int[2];
        origin.getLocationOnScreen(xyRoot);
        int bottom = xy[1] + view.getHeight();
        int bottomOfRoot = xyRoot[1] + origin.getHeight();
        assertTrue(bottom > bottomOfRoot);
        assertTrue(xy[1] < bottomOfRoot);
        assertTrue(bottom > bottomOfRoot);
    }
    static public void assertOnTopEdgeOfScreen(View origin, View view) {
        int[] xy = new int[2];
        view.getLocationOnScreen(xy);
        int[] xyRoot = new int[2];
        origin.getLocationOnScreen(xyRoot);
        int bottom = xy[1] + view.getHeight();
        int bottomOfRoot = xyRoot[1] + origin.getHeight();
        assertTrue(bottom < bottomOfRoot);
        assertTrue(bottom > xyRoot[1]);
        assertTrue(xy[1] < xyRoot[1]);
    }
}
