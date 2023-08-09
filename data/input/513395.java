public class GridTouchStackFromBottomManyTest extends ActivityInstrumentationTestCase<GridStackFromBottomMany> {
    private GridStackFromBottomMany mActivity;
    private GridView mGridView;
    public GridTouchStackFromBottomManyTest() {
        super("com.android.frameworks.coretests", GridStackFromBottomMany.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mActivity = getActivity();
        mGridView = getActivity().getGridView();
    }
    @MediumTest
    public void testPreconditions() {
        assertNotNull(mActivity);
        assertNotNull(mGridView);
        assertEquals(mGridView.getAdapter().getCount() - 1, mGridView.getSelectedItemPosition());
    }
    @LargeTest
    public void testScrollToTop() {
        View firstChild;
        TouchUtils.scrollToTop(this, mGridView);
        assertEquals("Selection still available after touch", -1,
                mGridView.getSelectedItemPosition());
        firstChild = mGridView.getChildAt(0);
        assertEquals("Item zero not the first child in the grid", 0, firstChild.getId());
        assertEquals("Item zero not at the top of the grid",
                mGridView.getListPaddingTop(), firstChild.getTop());
    }
    @MediumTest
    public void testScrollToBottom() {
        TouchUtils.scrollToBottom(this, mGridView);
        assertEquals("Selection still available after touch", -1,
                mGridView.getSelectedItemPosition());
        View lastChild = mGridView.getChildAt(mGridView.getChildCount() - 1);
        assertEquals("Grid is not scrolled to the bottom", mGridView.getAdapter().getCount() - 1,
                lastChild.getId());
        assertEquals("Last item is not touching the bottom edge",
                mGridView.getHeight() - mGridView.getListPaddingBottom(), lastChild.getBottom());
    }
}
