public class GridTouchVerticalSpacingTest extends ActivityInstrumentationTestCase<GridVerticalSpacing> {
    private GridVerticalSpacing mActivity;
    private GridView mGridView;
    public GridTouchVerticalSpacingTest() {
        super("com.android.frameworks.coretests", GridVerticalSpacing.class);
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
        assertEquals(0, mGridView.getSelectedItemPosition());
    }
    @MediumTest
    public void testNoScroll() {
        View firstChild = mGridView.getChildAt(0);
        View lastChild = mGridView.getChildAt(mGridView.getChildCount() - 1);
        int firstTop = firstChild.getTop();
        TouchUtils.dragViewBy(this, lastChild, Gravity.TOP | Gravity.LEFT, 
                0, -(ViewConfiguration.getTouchSlop()));
        View newFirstChild = mGridView.getChildAt(0);
        assertEquals("View scrolled too early", firstTop, newFirstChild.getTop());
        assertEquals("Wrong view in first position", 0, newFirstChild.getId());
    }
    public void testShortScroll() {
        View firstChild = mGridView.getChildAt(0);
        View lastChild = mGridView.getChildAt(mGridView.getChildCount() - 1);
        int firstTop = firstChild.getTop();
        TouchUtils.dragViewBy(this, lastChild, Gravity.TOP | Gravity.LEFT,
                0, -(ViewConfiguration.getTouchSlop() + 1 + 10));
        View newFirstChild = mGridView.getChildAt(0);
        assertEquals("View scrolled to wrong position", firstTop, newFirstChild.getTop() + 10);
        assertEquals("Wrong view in first position", 0, newFirstChild.getId());
    }
    public void testLongScroll() {
        View lastChild = mGridView.getChildAt(mGridView.getChildCount() - 1);
        int lastTop = lastChild.getTop();
        int distance = TouchUtils.dragViewToY(this, lastChild, Gravity.TOP | Gravity.LEFT,
                mGridView.getTop());
        assertEquals("View scrolled to wrong position", 
                lastTop - (distance - ViewConfiguration.getTouchSlop() - 1), lastChild.getTop());
    }
    @LargeTest
    public void testManyScrolls() {
        int originalCount = mGridView.getChildCount();
        View firstChild;
        int firstId = Integer.MIN_VALUE;
        int firstTop = Integer.MIN_VALUE; 
        int prevId;
        int prevTop; 
        do {
            prevId = firstId;
            prevTop = firstTop;
            TouchUtils.dragQuarterScreenUp(this);
            assertTrue(String.format("Too many children created: %d expected no more than %d", 
                    mGridView.getChildCount(), originalCount + 4), 
                    mGridView.getChildCount() <= originalCount + 4);
            firstChild = mGridView.getChildAt(0);
            firstId = firstChild.getId();
            firstTop = firstChild.getTop(); 
        } while ((prevId != firstId) || (prevTop != firstTop));
        View lastChild = mGridView.getChildAt(mGridView.getChildCount() - 1);
        assertEquals("Grid is not scrolled to the bottom", mGridView.getAdapter().getCount() - 1,
                lastChild.getId());
        firstId = Integer.MIN_VALUE;
        firstTop = Integer.MIN_VALUE; 
        do {
            prevId = firstId;
            prevTop = firstTop;
            TouchUtils.dragQuarterScreenDown(this);
            assertTrue(String.format("Too many children created: %d expected no more than %d", 
                    mGridView.getChildCount(), originalCount + 4), 
                    mGridView.getChildCount() <= originalCount + 4);
            firstChild = mGridView.getChildAt(0);
            firstId = firstChild.getId();
            firstTop = firstChild.getTop(); 
        } while ((prevId != firstId) || (prevTop != firstTop));
        firstChild = mGridView.getChildAt(0);
        assertEquals("Grid is not scrolled to the top", 0, firstChild.getId());
    } 
}
