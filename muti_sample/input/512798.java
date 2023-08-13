public class ButtonsWithTallTextViewInBetweenTest
        extends ActivityInstrumentationTestCase<ButtonsWithTallTextViewInBetween> {
    private ScrollView mScrollView;
    private Button mTopButton;
    private TextView mMiddleFiller;
    private TextView mBottomButton;
    public ButtonsWithTallTextViewInBetweenTest() {
        super("com.android.frameworks.coretests", ButtonsWithTallTextViewInBetween.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mScrollView = getActivity().getScrollView();
        mTopButton = getActivity().getTopButton();
        mMiddleFiller = getActivity().getMiddleFiller();
        mBottomButton = getActivity().getBottomButton();
    }
    private Rect mTempRect = new Rect();
    private int getTopWithinScrollView(View descendant) {
        descendant.getDrawingRect(mTempRect);
        mScrollView.offsetDescendantRectToMyCoords(descendant, mTempRect);
        return mTempRect.top;
    }
    private int getBottomWithinScrollView(View descendant) {
        descendant.getDrawingRect(mTempRect);
        mScrollView.offsetDescendantRectToMyCoords(descendant, mTempRect);
        return mTempRect.bottom;
    }
    @MediumTest
    public void testPreconditions() {
        assertTrue("top button should be shorter than max scroll amount",
                mTopButton.getHeight() <
                mScrollView.getMaxScrollAmount());
        assertTrue("bottom button should be further than max scroll amount off screen",
                getTopWithinScrollView(mBottomButton)- mScrollView.getBottom() > mScrollView.getMaxScrollAmount());
    }
    @MediumTest
    public void testPanTopButtonOffScreenLosesFocus() {
        sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
        assertEquals("scroll view should be scrolled by the max amount for one "
                + "arrow navigation",
                mScrollView.getMaxScrollAmount(),
                mScrollView.getScrollY());
        assertTrue("top button should be off screen",
                getBottomWithinScrollView(mTopButton) < mScrollView.getScrollY());
        assertFalse("top button should have lost focus",
                mTopButton.isFocused());
        assertTrue("scroll view should be focused", mScrollView.isFocused());
    }
    @MediumTest
    public void testScrollDownToBottomButton() throws Exception {
        final int screenBottom = mScrollView.getScrollY() + mScrollView.getHeight();
        final int numDownsToButtonButton =
                ((getBottomWithinScrollView(mBottomButton) - screenBottom)) / mScrollView.getMaxScrollAmount() + 1;
        for (int i = 0; i < numDownsToButtonButton; i++) {
            sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
        }
        assertTrue("bottombutton.isFocused", mBottomButton.isFocused());
        assertEquals("should be fully scrolled to bottom",
                getActivity().getLinearLayout().getHeight() - mScrollView.getHeight(),
                mScrollView.getScrollY());
    }
    @MediumTest
    public void testPanBottomButtonOffScreenLosesFocus() throws Exception {
        mBottomButton.post(new Runnable() {
            public void run() {
                mBottomButton.requestFocus();
            }
        });
        getInstrumentation().waitForIdleSync();
        assertTrue("bottombutton.isFocused", mBottomButton.isFocused());
        final int maxScroll = getActivity().getLinearLayout().getHeight()
                - mScrollView.getHeight();
        assertEquals("should be fully scrolled to bottom",
                maxScroll,
                mScrollView.getScrollY());
        sendKeys(KeyEvent.KEYCODE_DPAD_UP);
        assertEquals("scroll view should have scrolled by the max amount for one "
                + "arrow navigation",
                maxScroll - mScrollView.getMaxScrollAmount(),
                mScrollView.getScrollY());
        assertTrue("bottom button should be off screen",
                getTopWithinScrollView(mBottomButton) > mScrollView.getScrollY() + mScrollView.getHeight());
        assertFalse("bottom button should have lost focus",
                mBottomButton.isFocused());
        assertTrue("scroll view should be focused", mScrollView.isFocused());
    }
}
