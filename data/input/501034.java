public class ChangeTouchModeTest extends ActivityInstrumentationTestCase<LLOfButtons1> {
    public ChangeTouchModeTest() {
        super("com.android.frameworks.coretests", LLOfButtons1.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }
    @MediumTest
    public void testPreconditions() throws Exception {
        assertFalse("touch mode", getActivity().isInTouchMode());
    }
    @MediumTest
    public void testTouchingScreenEntersTouchMode() throws Exception {
        assertInTouchModeAfterTap(this, getActivity().getFirstButton());
        assertTrue("touch mode", getActivity().isInTouchMode());
    }
    public void DISABLE_testDpadDirectionLeavesTouchMode() throws Exception {
        assertInTouchModeAfterClick(this, getActivity().getFirstButton());
        sendKeys(KeyEvent.KEYCODE_DPAD_RIGHT);
        assertNotInTouchModeAfterKey(this, KeyEvent.KEYCODE_DPAD_RIGHT, getActivity().getFirstButton());
        assertFalse("touch mode", getActivity().isInTouchMode());
    }
    public void TODO_touchTrackBallMovementLeavesTouchMode() throws Exception {
    }
    @MediumTest
    public void testTouchModeFalseAcrossActivites() throws Exception {
        getInstrumentation().waitForIdleSync();
        LLOfButtons2 otherActivity = null;
        try {
            otherActivity =
                    launchActivity("com.android.frameworks.coretests", LLOfButtons2.class, null);
            assertNotNull(otherActivity);
            assertFalse(otherActivity.isInTouchMode());
        } finally {
            if (otherActivity != null) {
                otherActivity.finish();
            }
        }
    }
    @LargeTest
    public void testTouchModeTrueAcrossActivites() throws Exception {
        assertInTouchModeAfterClick(this, getActivity().getFirstButton());
        LLOfButtons2 otherActivity = null;
        try {
            otherActivity =
                    launchActivity("com.android.frameworks.coretests", LLOfButtons2.class, null);
            assertNotNull(otherActivity);
            assertTrue(otherActivity.isInTouchMode());
        } finally {
            if (otherActivity != null) {
                otherActivity.finish();
            }
        }
    }
    @LargeTest
    public void testTouchModeChangedInOtherActivity() throws Exception {
        assertFalse("touch mode", getActivity().isInTouchMode());
        LLOfButtons2 otherActivity = null;
        try {
            otherActivity =
                    launchActivity("com.android.frameworks.coretests", LLOfButtons2.class, null);
            assertNotNull(otherActivity);
            assertFalse(otherActivity.isInTouchMode());
            assertInTouchModeAfterClick(this, otherActivity.getFirstButton());
            assertTrue(otherActivity.isInTouchMode());
        } finally {
            if (otherActivity != null) {
                otherActivity.finish();
            }
        }
        Thread.sleep(200);
        assertTrue("touch mode", getActivity().isInTouchMode());
    }
}
