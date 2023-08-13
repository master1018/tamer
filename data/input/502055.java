public class GridTouchStackFromBottomTest extends ActivityInstrumentationTestCase<GridStackFromBottom> {
    private GridStackFromBottom mActivity;
    private GridView mGridView;
    public GridTouchStackFromBottomTest() {
        super("com.android.frameworks.coretests", GridStackFromBottom.class);
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
    @MediumTest
    public void testPushUp() {
        TouchUtils.scrollToBottom(this, mGridView);
        assertEquals("Selection still available after touch", -1,
                mGridView.getSelectedItemPosition());
        View lastChild = mGridView.getChildAt(mGridView.getChildCount() - 1);
        assertEquals("Last item not the last child in the grid",
                mGridView.getAdapter().getCount() - 1, lastChild.getId());
        assertEquals("Last item not at the bottom of the grid",
                mGridView.getHeight() - mGridView.getListPaddingBottom(), lastChild.getBottom());
    }
    @MediumTest
    public void testPullDown() {
        TouchUtils.scrollToTop(this, mGridView);
        assertEquals("Selection still available after touch", -1,
                mGridView.getSelectedItemPosition());
        View lastChild = mGridView.getChildAt(mGridView.getChildCount() - 1);
        assertEquals("Last item not the last child in the grid",
                mGridView.getAdapter().getCount() - 1, lastChild.getId());
        assertEquals("Last item not at the bottom of the grid",
                mGridView.getHeight() - mGridView.getListPaddingBottom(), lastChild.getBottom());
    }
    @MediumTest
    public void testPushUpFast() {
        TouchUtils.dragViewToTop(this, mGridView.getChildAt(mGridView.getChildCount() - 1), 2);
        assertEquals("Selection still available after touch", -1,
                mGridView.getSelectedItemPosition());
        View lastChild = mGridView.getChildAt(mGridView.getChildCount() - 1);
        assertEquals("Last item not the last child in the grid",
                mGridView.getAdapter().getCount() - 1, lastChild.getId());
        assertEquals("Last item not at the bottom of the grid",
                mGridView.getHeight() - mGridView.getListPaddingBottom(), lastChild.getBottom());
    }
    @MediumTest
    public void testPullDownFast() {
        TouchUtils.dragViewToBottom(this, mGridView.getChildAt(0), 2);
        assertEquals("Selection still available after touch", -1,
                mGridView.getSelectedItemPosition());
        View lastChild = mGridView.getChildAt(mGridView.getChildCount() - 1);
        assertEquals("Last item not the last child in the grid",
                mGridView.getAdapter().getCount() - 1, lastChild.getId());
        assertEquals("Last item not at the bottom of the grid",
                mGridView.getHeight() - mGridView.getListPaddingBottom(), lastChild.getBottom());
    }
}
