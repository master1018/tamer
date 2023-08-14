public class GoneParentFocusedChildTest
        extends ActivityInstrumentationTestCase<GoneParentFocusedChild> {
    public GoneParentFocusedChildTest() {
        super("com.android.frameworks.coretests", GoneParentFocusedChild.class);
    }
    @MediumTest
    public void testPreconditinos() {
        assertNotNull(getActivity().getLayout());
        assertNotNull(getActivity().getGoneGroup());
        assertNotNull(getActivity().getButton());
        assertTrue("button should have focus",
                getActivity().getButton().hasFocus());
        assertEquals("gone group should be, well, gone!",
                View.GONE,
                getActivity().getGoneGroup().getVisibility());
        assertFalse("the activity should have received no key events",
                getActivity().isUnhandledKeyEvent());
    }
    @MediumTest
    public void testKeyEventGoesToActivity() {
        sendKeys(KeyEvent.KEYCODE_J);
        assertTrue(getActivity().isUnhandledKeyEvent());
    }
}
