public class ListTouchBottomGravityManyTest extends ActivityInstrumentationTestCase<ListBottomGravityMany> {
    private ListBottomGravityMany mActivity;
    private ListView mListView;
    public ListTouchBottomGravityManyTest() {
        super("com.android.frameworks.coretests", ListBottomGravityMany.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mActivity = getActivity();
        mListView = getActivity().getListView();
    }
    @MediumTest
    public void testPreconditions() {
        assertNotNull(mActivity);
        assertNotNull(mListView);
        assertEquals(mListView.getAdapter().getCount() - 1, mListView.getSelectedItemPosition());
    }
    @LargeTest
    public void testPullDown() {     
        int originalCount = mListView.getChildCount();
        TouchUtils.scrollToTop(this, mListView);
        assertEquals("Selection still available after touch", -1, 
                mListView.getSelectedItemPosition());
        View firstChild = mListView.getChildAt(0);
        assertEquals("Item zero not the first child in the list", 0, firstChild.getId());
        assertEquals("Item zero not at the top of the list", mListView.getListPaddingTop(),
                firstChild.getTop());
        assertTrue(String.format("Too many children created: %d expected no more than %d", 
                mListView.getChildCount(), originalCount + 1), 
                mListView.getChildCount() <= originalCount + 1);
    }
    @MediumTest
    public void testPushUp() {
        TouchUtils.scrollToBottom(this, mListView);
        assertEquals("Selection still available after touch", -1, 
                mListView.getSelectedItemPosition());
        View lastChild = mListView.getChildAt(mListView.getChildCount() - 1);
        assertEquals("List is not scrolled to the bottom", mListView.getAdapter().getCount() - 1,
                lastChild.getId());
        assertEquals("Last item is not touching the bottom edge", 
                mListView.getHeight() - mListView.getListPaddingBottom(), lastChild.getBottom());
    }
    @MediumTest
    public void testNoScroll() {
        View firstChild = mListView.getChildAt(0);
        View lastChild = mListView.getChildAt(mListView.getChildCount() - 1);
        int lastTop = lastChild.getTop();
        TouchUtils.dragViewBy(this, firstChild, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL,
                0, ViewConfiguration.getTouchSlop());
        View newLastChild = mListView.getChildAt(mListView.getChildCount() - 1);
        assertEquals("View scrolled too early", lastTop, newLastChild.getTop());
        assertEquals("Wrong view in last position", mListView.getAdapter().getCount() - 1, 
                newLastChild.getId());
    }
    public void testShortScroll() {
        View firstChild = mListView.getChildAt(0);
        if (firstChild.getTop() < this.mListView.getListPaddingTop()) {
            firstChild = mListView.getChildAt(1);
        }
        View lastChild = mListView.getChildAt(mListView.getChildCount() - 1);
        int lastTop = lastChild.getTop();
        TouchUtils.dragViewBy(this, firstChild, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL,
                0, ViewConfiguration.getTouchSlop() + 1 + 10);
        View newLastChild = mListView.getChildAt(mListView.getChildCount() - 1);
        assertEquals("View scrolled to wrong position", lastTop, newLastChild.getTop() - 10);
        assertEquals("Wrong view in last position", mListView.getAdapter().getCount() - 1,
                newLastChild.getId());
    }
    public void testLongScroll() {
        View firstChild = mListView.getChildAt(0);
        if (firstChild.getTop() < mListView.getListPaddingTop()) {
            firstChild = mListView.getChildAt(1);
        }
        int firstTop = firstChild.getTop();
        int distance = TouchUtils.dragViewBy(this, firstChild, 
                Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 
                (int)(mActivity.getWindowManager().getDefaultDisplay().getHeight() * 0.75f));
        assertEquals("View scrolled to wrong position", firstTop
                + (distance - ViewConfiguration.getTouchSlop() - 1), firstChild.getTop());
    } 
}
