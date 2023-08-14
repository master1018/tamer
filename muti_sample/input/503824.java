public class ListWithOffScreenNextSelectableTest
        extends ActivityInstrumentationTestCase<ListWithOffScreenNextSelectable> {
    private ListView mListView;
    public ListWithOffScreenNextSelectableTest() {
        super("com.android.frameworks.coretests", ListWithOffScreenNextSelectable.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mListView = getActivity().getListView();
    }
    @MediumTest
    public void testPreconditions() {
        assertNotNull(mListView);
        assertEquals(5, mListView.getAdapter().getCount());
        assertFalse(mListView.getAdapter().areAllItemsEnabled());
        assertFalse(mListView.getAdapter().isEnabled(1));
        assertFalse(mListView.getAdapter().isEnabled(2));
        assertFalse(mListView.getAdapter().isEnabled(3));
        assertEquals("only 4 children should be on screen (so that next selectable is off " +
                "screen) for this test to be meaningful.",
                4, mListView.getChildCount());
        assertEquals(0, mListView.getSelectedItemPosition());
    }
    @MediumTest
    public void testGoDownToOffScreenSelectable() {
        final int listBottom = mListView.getHeight() - mListView.getListPaddingBottom();
        final View lastVisibleView = mListView.getChildAt(mListView.getChildCount() - 1);
        sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
        assertEquals("expecting view to be panned to just above fading edge",
                listBottom - mListView.getVerticalFadingEdgeLength(), lastVisibleView.getBottom());
        assertEquals("selection should not have moved yet",
                0, mListView.getSelectedItemPosition());
        sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
        assertEquals("selection should have moved",
                4, mListView.getSelectedItemPosition());
        assertEquals("wrong view created when scrolling",
                getActivity().getValueAtPosition(4), ((TextView) mListView.getSelectedView()).getText());
        assertEquals(listBottom, mListView.getSelectedView().getBottom());
    }
    @MediumTest
    public void testGoUpToOffScreenSelectable() {
        final int listBottom = mListView.getHeight() - mListView.getListPaddingBottom();
        final int listTop = mListView.getListPaddingTop();
        sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
        sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
        assertEquals(4, mListView.getSelectedItemPosition());
        assertEquals(listBottom, mListView.getSelectedView().getBottom());
        final View firstVisibleView = mListView.getChildAt(0);
        sendKeys(KeyEvent.KEYCODE_DPAD_UP);
        assertEquals("should have panned top view just below vertical fading edge",
                listTop + mListView.getVerticalFadingEdgeLength(), firstVisibleView.getTop());
        assertEquals("selection should not have moved yet",
                4, mListView.getSelectedItemPosition());
        sendKeys(KeyEvent.KEYCODE_DPAD_UP);
        assertEquals("selection should have moved",
                0, mListView.getSelectedItemPosition());
        assertEquals(getActivity().getValueAtPosition(0),((TextView) mListView.getSelectedView()).getText());
        assertEquals(listTop, mListView.getSelectedView().getTop());
    }
}
