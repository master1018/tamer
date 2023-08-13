public class TouchModeFocusChangeTest extends ActivityInstrumentationTestCase<LLOfButtons1> {
    private LLOfButtons1 mActivity;
    private Button mFirstButton;
    public TouchModeFocusChangeTest() {
        super("com.android.frameworks.coretests", LLOfButtons1.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mActivity = getActivity();
        mFirstButton = mActivity.getFirstButton();
    }
    @MediumTest
    public void testPreconditions() {
        assertFalse("we should not be in touch mode", mActivity.isInTouchMode());
        assertTrue("top button should have focus", mFirstButton.isFocused());
    }
    @MediumTest
    public void testTouchButtonNotTakeFocus() {
        assertInTouchModeAfterTap(this, mFirstButton);
        assertTrue("should be in touch mode", mActivity.isInTouchMode());
        assertFalse("button.isFocused",
                mFirstButton.isFocused());
        assertFalse("button.hasFocus",
                mFirstButton.hasFocus());
        assertNull("activity shouldn't have focus", mActivity.getCurrentFocus());
        assertFalse("linear layout should not have focus",
                mActivity.getLayout().hasFocus());
        assertTrue("button's onClickListener should have fired",
                mActivity.buttonClickListenerFired());
    }
    public void DISABLE_testLeaveTouchModeWithDpadEvent() {
        assertInTouchModeAfterClick(this, mFirstButton);
        assertTrue("should be in touch mode", mActivity.isInTouchMode());
        assertFalse("button should not have focus when touched",
                mFirstButton.isFocused());
        assertNotInTouchModeAfterKey(this, KeyEvent.KEYCODE_DPAD_RIGHT, mFirstButton);
        assertFalse("should be out of touch mode", mActivity.isInTouchMode());
        assertTrue("first button (the top most focusable) should have gained focus",
                mFirstButton.isFocused());
    }
}
