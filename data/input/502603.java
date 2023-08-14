public class ListItemRequestRectAboveThinFirstItemTest
        extends ActivityInstrumentationTestCase<ListOfThinItems> {
    private ListView mListView;
    public ListItemRequestRectAboveThinFirstItemTest() {
        super("com.android.frameworks.coretests", ListOfThinItems.class);
    }
    protected void setUp() throws Exception {
        super.setUp();
        mListView = getActivity().getListView();
    }
    @MediumTest
    public void testPreconditions() {
        assertTrue("first child needs to be within fading edge height",
                mListView.getChildAt(0).getBottom() < mListView.getVerticalFadingEdgeLength());
        assertTrue("should be at least two visible children",
                mListView.getChildCount() >= 2);
    }
    @MediumTest
    public void testSecondItemRequestRectAboveTop() throws Exception {
        sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
        assertEquals("selected position", 1, mListView.getSelectedItemPosition());
        final View second = mListView.getChildAt(1);
        final Rect rect = new Rect();
        second.getDrawingRect(rect);
        rect.offset(0, -2 * second.getBottom());
        getActivity().requestRectangleOnScreen(1, rect);
        getInstrumentation().waitForIdleSync();        
        assertEquals("top of first item",
                mListView.getListPaddingTop(), mListView.getChildAt(0).getTop());
    }
    @LargeTest
    public void testSecondToLastItemRequestRectBelowBottom() throws Exception {
        final int secondToLastPos = mListView.getCount() - 2;
        while (mListView.getSelectedItemPosition() < secondToLastPos) {
            sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
        }
        assertEquals("selected position", secondToLastPos,
                mListView.getSelectedItemPosition());
        final View secondToLast = mListView.getSelectedView();
        final Rect rect = new Rect();
        secondToLast.getDrawingRect(rect);
        rect.offset(0,
                2 * (mListView.getBottom() - secondToLast.getTop()));
        final int secondToLastIndex = mListView.getChildCount() - 2;
        getActivity().requestRectangleOnScreen(secondToLastIndex, rect);
        getInstrumentation().waitForIdleSync();
        int listBottom = mListView.getHeight() - mListView.getPaddingBottom();
        assertEquals("bottom of last item should be at bottom of list",
                listBottom,
                mListView.getChildAt(mListView.getChildCount() - 1).getBottom());
    }
}
