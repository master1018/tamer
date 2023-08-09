public class RequestRectangleVisibleWithInternalScrollTest
        extends ActivityInstrumentationTestCase<RequestRectangleVisibleWithInternalScroll> {
    private TextView mTextBlob;
    private Button mScrollToBlob;
    private ScrollView mScrollView;
    public RequestRectangleVisibleWithInternalScrollTest() {
        super("com.android.frameworks.coretests",
                RequestRectangleVisibleWithInternalScroll.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mTextBlob = getActivity().getTextBlob();
        mScrollToBlob = getActivity().getScrollToBlob();
        mScrollView = (ScrollView) getActivity().findViewById(R.id.scrollView);
    }
    public void testPreconditions() {
        assertNotNull(mTextBlob);
        assertNotNull(mScrollToBlob);
        assertEquals(getActivity().getScrollYofBlob(), mTextBlob.getScrollY());
    }
    public void testMoveToChildWithScrollYBelow() {
        assertTrue(mScrollToBlob.hasFocus());
        ViewAsserts.assertOffScreenBelow(mScrollView, mTextBlob);
        sendKeys(KeyEvent.KEYCODE_DPAD_CENTER);
        getInstrumentation().waitForIdleSync();  
        ViewAsserts.assertOnScreen(mScrollView, mTextBlob);
        ViewAsserts.assertHasScreenCoordinates(
                mScrollView, mTextBlob,
                0,
                mScrollView.getHeight()
                        - mTextBlob.getHeight()
                        - mScrollView.getVerticalFadingEdgeLength());
    }
}
