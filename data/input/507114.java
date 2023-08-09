public class ListLastItemPartiallyVisibleTest extends ActivityInstrumentationTestCase<ListLastItemPartiallyVisible> {
    private ListView mListView;
    private int mListBottom;
    public ListLastItemPartiallyVisibleTest() {
        super("com.android.frameworks.coretests", ListLastItemPartiallyVisible.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mListView = getActivity().getListView();
        mListBottom = mListView.getHeight() - mListView.getPaddingBottom();
    }
    @MediumTest
    public void testPreconditions() {
        assertEquals("number of elements visible should be the same as number of items " +
                "in adapter", mListView.getCount(), mListView.getChildCount());
        final View lastChild = mListView.getChildAt(mListView.getChildCount() - 1);
        assertTrue("last item should be partially off screen",
                lastChild.getBottom() > mListBottom);
        assertEquals("selected position", 0, mListView.getSelectedItemPosition());
    }
    @MediumTest
    public void testMovingDownToFullyVisibleNoScroll() {
        final View firstChild = mListView.getChildAt(0);
        final int firstBottom = firstChild.getBottom();
        sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
        assertEquals("shouldn't have scrolled: bottom of first child changed.",
                firstBottom, firstChild.getBottom());
    }
    @MediumTest
    public void testMovingUpToFullyVisibleNoScroll() {
        int numMovesToLast = mListView.getCount() - 1;
        for (int i = 0; i < numMovesToLast; i++) {
            sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
        }
        assertEquals("should have moved to last position",
                mListView.getChildCount() - 1, mListView.getSelectedItemPosition());
        final View lastChild = mListView.getSelectedView();
        final int lastTop = lastChild.getTop();
        sendKeys(KeyEvent.KEYCODE_DPAD_UP);
        assertEquals("shouldn't have scrolled: top of last child changed.",
                lastTop, lastChild.getTop());
    }
}
