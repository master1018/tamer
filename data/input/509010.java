public class ListWithScreenOfNoSelectablesTest extends ActivityInstrumentationTestCase<ListWithScreenOfNoSelectables> {
    private ListView mListView;
    public ListWithScreenOfNoSelectablesTest() {
        super("com.android.frameworks.coretests", ListWithScreenOfNoSelectables.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mListView = getActivity().getListView();
    }
    @MediumTest
    public void testPreconditions() {
        assertTrue("expecting first position to be selectable",
                mListView.getAdapter().isEnabled(0));
        final int numItems = mListView.getCount();
        for (int i = 1; i < numItems; i++) {
            assertFalse("expecting item to be unselectable (index " + i +")",
                    mListView.getAdapter().isEnabled(i));
        }
        assertTrue("expecting that not all views fit on screen",
                mListView.getChildCount() < mListView.getCount());
    }
    @MediumTest
    public void testGoFromSelectedViewExistsToNoSelectedViewExists() {
        View first = mListView.getChildAt(0);
        while (first.getParent() != null) {
            sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
        }
        assertEquals("selected position", ListView.INVALID_POSITION, mListView.getSelectedItemPosition());
        assertNull("selected view", mListView.getSelectedView());
    }
    @LargeTest
    public void testPanDownAcrossUnselectableChildrenToBottom() {
        final int lastPosition = mListView.getCount() - 1;
        final int maxDowns = 20;
        for(int count = 0; count < maxDowns && mListView.getLastVisiblePosition() <= lastPosition; count++) {
            sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
        }
        assertEquals("last visible position not the last position in the list even "
                + "after " + maxDowns + " downs", lastPosition, mListView.getLastVisiblePosition());
    }
    @MediumTest
    public void testGoFromNoSelectionToSelectionExists() {
        View first = mListView.getChildAt(0);
        while (first.getParent() != null) {
            sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
        }
        assertEquals("selected position", ListView.INVALID_POSITION, mListView.getSelectedItemPosition());
        assertNull("selected view", mListView.getSelectedView());
        sendKeys(KeyEvent.KEYCODE_DPAD_UP);
        assertEquals("first visible position", 0, mListView.getFirstVisiblePosition());
        assertEquals("selected position", ListView.INVALID_POSITION, mListView.getSelectedItemPosition());
        sendKeys(KeyEvent.KEYCODE_DPAD_UP);
        assertEquals("selected position", 0, mListView.getSelectedItemPosition());
    }
}
