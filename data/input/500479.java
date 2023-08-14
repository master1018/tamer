@TestTargetClass(SoundEffectConstants.class)
public class SoundEffectConstantsTest extends AndroidTestCase {
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test getContantForFocusDirection.",
        method = "getContantForFocusDirection",
        args = {int.class}
    )
    public void testgetContantForFocusDirection() {
        assertEquals(SoundEffectConstants.NAVIGATION_RIGHT,
                SoundEffectConstants
                        .getContantForFocusDirection(View.FOCUS_RIGHT));
        assertEquals(SoundEffectConstants.NAVIGATION_DOWN, SoundEffectConstants
                .getContantForFocusDirection(View.FOCUS_DOWN));
        assertEquals(SoundEffectConstants.NAVIGATION_LEFT, SoundEffectConstants
                .getContantForFocusDirection(View.FOCUS_LEFT));
        assertEquals(SoundEffectConstants.NAVIGATION_UP, SoundEffectConstants
                .getContantForFocusDirection(View.FOCUS_UP));
        assertEquals(SoundEffectConstants.NAVIGATION_DOWN, SoundEffectConstants
                .getContantForFocusDirection(View.FOCUS_FORWARD));
        assertEquals(SoundEffectConstants.NAVIGATION_UP, SoundEffectConstants
                .getContantForFocusDirection(View.FOCUS_BACKWARD));
        try {
            SoundEffectConstants.getContantForFocusDirection(-1);
            fail("should throw exception");
        } catch (RuntimeException e) {
        }
    }
}
