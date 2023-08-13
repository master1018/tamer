public class StartInTouchWithViewInFocusTest extends 
        ActivityInstrumentationTestCase2<LLEditTextThenButton> {
    private EditText mEditText;
    private Button mButton;
    public StartInTouchWithViewInFocusTest() {
        super("com.android.frameworks.coretests", LLEditTextThenButton.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        this.setActivityInitialTouchMode(true);
        mEditText = getActivity().getEditText();
        mButton = getActivity().getButton();
    }
    @MediumTest
    public void testPreconditions() {
        assertTrue("should start in touch mode", mEditText.isInTouchMode());
        assertTrue("edit text is focusable in touch mode, should have focus", mEditText.isFocused());
    }
    public void DISABLE_testKeyDownLeavesTouchModeAndGoesToNextView() {
        assertNotInTouchModeAfterKey(this, KeyEvent.KEYCODE_DPAD_DOWN, mEditText);
        assertFalse("should have left touch mode", mEditText.isInTouchMode());
        assertTrue("should have given focus to next view", mButton.isFocused());
    }
    public void DISABLE_testNonDirectionalKeyExitsTouchMode() {
        assertNotInTouchModeAfterKey(this, KeyEvent.KEYCODE_A, mEditText);
        assertFalse("should have left touch mode", mEditText.isInTouchMode());
        assertTrue("edit text should still have focus", mEditText.isFocused());        
    }
}
