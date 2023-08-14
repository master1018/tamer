public class TallTextAboveButtonTest extends ActivityInstrumentationTestCase<TallTextAboveButton> {
    private ScrollView mScrollView;
    private TextView mTopText;
    private TextView mBottomButton;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mScrollView = getActivity().getScrollView();
        mTopText = getActivity().getContentChildAt(0);
        mBottomButton = getActivity().getContentChildAt(1);
    }
    public TallTextAboveButtonTest() {
        super("com.android.frameworks.coretests", TallTextAboveButton.class);
    }
    @MediumTest
    public void testPreconditions() {
        assertTrue("top text should be larger than screen",
                mTopText.getHeight() > mScrollView.getHeight());
        assertTrue("scroll view should have focus (because nothing else focusable "
                + "is on screen), but " + getActivity().getScrollView().findFocus() + " does instead",
                getActivity().getScrollView().isFocused());
    }
    @MediumTest
    public void testGainFocusAsScrolledOntoScreen() {
        sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
        assertTrue("button should have scrolled onto screen",
                mBottomButton.getBottom() >= mScrollView.getBottom());
        assertTrue("button should have gained focus as it was scrolled completely "
                + "into view", mBottomButton.isFocused());
        sendKeys(KeyEvent.KEYCODE_DPAD_UP);
        assertTrue("scroll view should have focus, but " + getActivity().getScrollView().findFocus() + " does instead",
                getActivity().getScrollView().isFocused());
    }
    @MediumTest
    public void testScrollingButtonOffScreenLosesFocus() {
        sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
        assertTrue("button should have focus", mBottomButton.isFocused());
        sendKeys(KeyEvent.KEYCODE_DPAD_UP);
        assertTrue("scroll view should have focus, but " + getActivity().getScrollView().findFocus() + " does instead",
                getActivity().getScrollView().isFocused());
    }
}
