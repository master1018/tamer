public class ListRetainsFocusAcrossLayoutsTest extends ActivityInstrumentationTestCase<ListItemFocusablesClose> {
    public ListRetainsFocusAcrossLayoutsTest() {
        super("com.android.frameworks.coretests", ListItemFocusablesClose.class);
    }
    private void requestLayoutOnList() {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                getActivity().getListView().requestLayout();
            }
        });
    }
    @MediumTest
    public void testPreconditions() {
        assertTrue("top button at position 0 should be focused",
                getActivity().getChildOfItem(0, 0).isFocused());
    }
    @MediumTest
    public void testBottomButtonRetainsFocusAfterLayout() throws Exception {
        sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
        assertTrue("bottom botton at position 0 should be focused",
                getActivity().getChildOfItem(0, 2).isFocused());
        requestLayoutOnList();
        getInstrumentation().waitForIdleSync();
        assertTrue("bottom botton at position 0 should be focused after layout",
                getActivity().getChildOfItem(0, 2).isFocused());
    }
    @MediumTest
    public void testTopButtonOfSecondPositionRetainsFocusAfterLayout() {
        sendRepeatedKeys(2, KeyEvent.KEYCODE_DPAD_DOWN);
        assertTrue("top botton at position 1 should be focused",
                getActivity().getChildOfItem(1, 0).isFocused());
        requestLayoutOnList();
        getInstrumentation().waitForIdleSync();
        assertTrue("top botton at position 1 should be focused after layout",
                getActivity().getChildOfItem(1, 0).isFocused());
    }
    @MediumTest
    public void testBottomButtonOfSecondPositionRetainsFocusAfterLayout() {
        sendRepeatedKeys(3, KeyEvent.KEYCODE_DPAD_DOWN);
        assertTrue("bottom botton at position 1 should be focused",
                getActivity().getChildOfItem(1, 2).isFocused());
        requestLayoutOnList();
        getInstrumentation().waitForIdleSync();
        assertTrue("bottom botton at position 1 should be focused after layout",
                getActivity().getChildOfItem(1, 2).isFocused());
    }
}
