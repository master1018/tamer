public class ButtonAboveTallInternalSelectionViewTest extends
        ActivityInstrumentationTestCase<ButtonAboveTallInternalSelectionView> {
    public ButtonAboveTallInternalSelectionViewTest() {
        super("com.android.frameworks.coretests", ButtonAboveTallInternalSelectionView.class);
    }
    @MediumTest
    public void testPreconditions() {
        assertTrue("expecting the top button to have focus",
                getActivity().getButtonAbove().isFocused());
        assertEquals("scrollview scroll y",
                0,
                getActivity().getScrollView().getScrollY());
        assertTrue("internal selection view should be taller than screen",
                getActivity().getIsv().getHeight() > getActivity().getScrollView().getHeight());
        assertTrue("top of ISV should be on screen",
                getActivity().getIsv().getTop() >
                getActivity().getScrollView().getScrollY());
    }
    @MediumTest
    public void testMovingFocusDownToItemTallerThanScreenStillOnScreen() {
        sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
        getInstrumentation().waitForIdleSync();
        final InternalSelectionView isv = getActivity().getIsv();
        assertTrue("internal selection view should have taken focus",
                isv.isFocused());
        assertEquals("internal selection view selected row",
                0, isv.getSelectedRow());
        assertTrue("top of ISV should still be on screen",
                getActivity().getIsv().getTop() >
                getActivity().getScrollView().getScrollY());
    }
}
