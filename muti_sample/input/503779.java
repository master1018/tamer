public class TouchModeFocusableTest extends ActivityInstrumentationTestCase<LLEditTextThenButton> {
    private EditText mEditText;
    private Button mButton;
    public TouchModeFocusableTest() {
        super("com.android.frameworks.coretests", LLEditTextThenButton.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mEditText = getActivity().getEditText();
        mButton = getActivity().getButton();
    }
    @MediumTest
    public void testPreconditions() {
        assertFalse("should not be in touch mode to start off", mButton.isInTouchMode());
        assertTrue("edit text should have focus", mEditText.isFocused());
        assertTrue("edit text should be focusable in touch mode", mEditText.isFocusableInTouchMode());
    }
    @MediumTest
    public void testClickButtonEditTextKeepsFocus() {
        assertInTouchModeAfterTap(this, mButton);
        assertTrue("should be in touch mode", mButton.isInTouchMode());
        assertTrue("edit text should still have focus", mEditText.isFocused());
    }
    @LargeTest
    public void testClickEditTextGivesItFocus() {
        sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
        assertTrue("button should have focus", mButton.isFocused());
        assertInTouchModeAfterClick(this, mEditText);
        assertTrue("clicking edit text should have entered touch mode", mButton.isInTouchMode());
        assertTrue("clicking edit text should have given it focus", mEditText.isFocused());
    }
    @LargeTest
    public void testEnterTouchModeGivesFocusBackToFocusableInTouchMode() {
        sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
        assertTrue("button should have focus",
                mButton.isFocused());
        assertInTouchModeAfterClick(this, mButton);
        assertTrue("should be in touch mode", mButton.isInTouchMode());
        assertNull("nothing should have focus", getActivity().getCurrentFocus());
        assertFalse("layout should not have focus",
                getActivity().getLayout().hasFocus());
    }
}
