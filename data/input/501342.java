public class ListWithEditTextHeaderTest extends ActivityInstrumentationTestCase2<ListWithEditTextHeader> {
    private ListView mListView;
    public ListWithEditTextHeaderTest() {
        super(ListWithEditTextHeader.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mListView = getActivity().getListView();
    }
    @MediumTest
    public void testPreconditions() {
        assertTrue("listview.getItemsCanFocus()", mListView.getItemsCanFocus());
        assertFalse("out of touch-mode", mListView.isInTouchMode());
        assertEquals("header view count", 1, mListView.getHeaderViewsCount());
        assertTrue("header does not have focus", mListView.getChildAt(0).isFocused());
    }
    @FlakyTest(tolerance=2)
    @LargeTest
    public void testClickingHeaderKeepsFocus() {
        TouchUtils.clickView(this, mListView.getChildAt(0));
        assertTrue("header does not have focus", mListView.getChildAt(0).isFocused());
        assertEquals("something is selected", AbsListView.INVALID_POSITION, mListView.getSelectedItemPosition());
    }
    @LargeTest
    public void testClickingHeaderWhenOtherItemHasFocusGivesHeaderFocus() {
        sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
        assertEquals("selected position", 1, mListView.getSelectedItemPosition());
        TouchUtils.clickView(this, mListView.getChildAt(0));
        assertTrue("header does not have focus", mListView.getChildAt(0).isFocused());
        assertEquals("something is selected", AbsListView.INVALID_POSITION, mListView.getSelectedItemPosition());
    }
}
