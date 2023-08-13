public class FocusableInTouchModeClickTest extends ActivityInstrumentationTestCase2<LLOfTwoFocusableInTouchMode> {
    public FocusableInTouchModeClickTest() {
        super("com.android.frameworks.coretests", LLOfTwoFocusableInTouchMode.class);
    }
    protected void setUp() throws Exception {
        super.setUp();
        setActivityInitialTouchMode(true);
    }
    @MediumTest
    public void testPreconditions() {
        assertTrue("should start in touch mode", getActivity().getButton1().isInTouchMode());
        assertTrue(getActivity().getButton1().isFocused());
    }
    @LargeTest
    public void testClickGivesFocusNoClickFired() {
        TouchUtils.clickView(this, getActivity().getButton2());
        assertTrue("click should give focusable in touch mode focus",
                getActivity().getButton2().isFocused());
        assertFalse("getting focus should result in no on click",
                getActivity().isB2Fired());
        TouchUtils.clickView(this, getActivity().getButton2());
        assertTrue("subsequent click while focused should fire on click",
                getActivity().isB2Fired());
    }
    @MediumTest
    public void testTapGivesFocusNoClickFired() {
        TouchUtils.touchAndCancelView(this, getActivity().getButton2());
        assertFalse("button shouldn't have fired click", getActivity().isB2Fired());
        assertFalse("button shouldn't have focus", getActivity().getButton2().isFocused());
    }
}
