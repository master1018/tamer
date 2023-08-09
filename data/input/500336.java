public class ScrollViewButtonsAndLabelsTest
        extends ActivityInstrumentationTestCase<ScrollViewButtonsAndLabels> {
    private ScrollView mScrollView;
    private LinearLayout mLinearLayout;
    private int mScreenBottom;
    private int mScreenTop;
    public ScrollViewButtonsAndLabelsTest() {
        super("com.android.frameworks.coretests",
              ScrollViewButtonsAndLabels.class);
    }
    @Override
    public void setUp() throws Exception {
        super.setUp();
        mScrollView = getActivity().getScrollView();
        mLinearLayout = getActivity().getLinearLayout();
        int origin[] = {0, 0};
        mScrollView.getLocationOnScreen(origin);
        mScreenTop = origin[1];
        mScreenBottom = origin[1] + mScrollView.getHeight();
    }
    @MediumTest
    public void testPreconditions() {
        assertTrue("vertical fading edge width needs to be non-zero for this "
                + "test to be worth anything",
                mScrollView.getVerticalFadingEdgeLength() > 0);
    }
    @LargeTest
    public void testArrowScrollDownOffScreenVerticalFadingEdge() {
        int offScreenIndex = findFirstButtonOffScreenTop2Bottom();
        Button firstButtonOffScreen = getActivity().getButton(offScreenIndex);
        for (int i = 0; i < offScreenIndex; i++) {
            sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
        }
        getInstrumentation().waitForIdleSync();
        assertTrue(firstButtonOffScreen.hasFocus());
        assertTrue("the button we've moved to off screen must not be the last "
                + "button in the scroll view for this test to work (since we "
                + "are expecting the fading edge to be there).",
                offScreenIndex < getActivity().getNumButtons());
        int buttonLoc[] = {0, 0};
        firstButtonOffScreen.getLocationOnScreen(buttonLoc);
        int buttonBottom = buttonLoc[1] + firstButtonOffScreen.getHeight();
        int verticalFadingEdgeLength = mScrollView
                .getVerticalFadingEdgeLength();
        assertEquals("bottom of button should be verticalFadingEdgeLength "
                + "above the bottom of the screen",
                buttonBottom, mScreenBottom - verticalFadingEdgeLength);
    }
    @LargeTest
    public void testArrowScrollDownToBottomElementOnScreen() {
        int numGroups = getActivity().getNumButtons();
        Button lastButton = getActivity().getButton(numGroups - 1);
        assertEquals("button needs to be at the very bottom of the layout for "
                + "this test to work",
                mLinearLayout.getHeight(), lastButton.getBottom());
        for (int i = 0; i < numGroups; i++) {
            sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
        }
        getInstrumentation().waitForIdleSync();
        assertTrue("last button should have focus", lastButton.hasFocus());
        int buttonLoc[] = {0, 0};
        lastButton.getLocationOnScreen(buttonLoc);
        int buttonBottom = buttonLoc[1] + lastButton.getHeight();
        assertEquals("button should be at very bottom of screen",
                mScreenBottom, buttonBottom);
    }
    @LargeTest
    public void testArrowScrollUpOffScreenVerticalFadingEdge() {
        int numGroups = goToBottomButton();
        int offScreenIndex = findFirstButtonOffScreenBottom2Top();
        Button offScreenButton = getActivity().getButton(offScreenIndex);
        int clicksToOffScreenIndex = numGroups - offScreenIndex - 1;
        for (int i = 0; i < clicksToOffScreenIndex; i++) {
            sendKeys(KeyEvent.KEYCODE_DPAD_UP);
        }
        getInstrumentation().waitForIdleSync();
        assertTrue("we want to be at offScreenButton", offScreenButton.hasFocus());
        int buttonLoc[] = {0, 0};
        offScreenButton.getLocationOnScreen(buttonLoc);
        assertEquals("top should take into account fading edge",
            mScreenTop + mScrollView.getVerticalFadingEdgeLength(), buttonLoc[1]);
    }
    @LargeTest
    public void testArrowScrollUpToTopElementOnScreen() {
        int numButtons = goToBottomButton();
        for (int i = 0; i < numButtons; i++) {
            sendKeys(KeyEvent.KEYCODE_DPAD_UP);
        }
        getInstrumentation().waitForIdleSync();
        Button topButton = getActivity().getButton(0);
        assertTrue("should be back at top button", topButton.hasFocus());
        int buttonLoc[] = {0, 0};
        topButton.getLocationOnScreen(buttonLoc);
        assertEquals("top of top button should be at top of screen; no need to take"
                + " into account vertical fading edge.",
                mScreenTop, buttonLoc[1]);
    }
    private int goToBottomButton() {
        int numButtons = getActivity().getNumButtons();
        Button lastButton = getActivity().getButton(numButtons - 1);
        for (int i = 0; i < numButtons; i++) {
          sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
        }
        getInstrumentation().waitForIdleSync();
        assertTrue("we want to be at the last button", lastButton.hasFocus());
        return numButtons;
    }
    private int findFirstButtonOffScreenTop2Bottom() {
        int origin[] = {0, 0};
        mScrollView.getLocationOnScreen(origin);
        int screenHeight = mScrollView.getHeight();
        for (int i = 0; i < getActivity().getNumButtons(); i++) {
            int buttonLoc[] = {0, 0};
            Button button = getActivity().getButton(i);
            button.getLocationOnScreen(buttonLoc);
            if (buttonLoc[1] - origin[1] > screenHeight) {
                return i;
            }
        }
        fail("couldn't find first button off screen");
        return -1; 
    }
    private int findFirstButtonOffScreenBottom2Top() {
        int origin[] = {0, 0};
        mScrollView.getLocationOnScreen(origin);
        for (int i = getActivity().getNumButtons() - 1; i >= 0; i--) {
            int buttonLoc[] = {0, 0};
            Button button = getActivity().getButton(i);
            button.getLocationOnScreen(buttonLoc);
            if (buttonLoc[1] < 0) {
                return i;
            }
        }
        fail("couldn't find first button off screen");
        return -1; 
    }
}
