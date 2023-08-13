public class ScrollingThroughListOfFocusablesTest extends InstrumentationTestCase {
    Rect mTempRect = new Rect();
    private ListOfInternalSelectionViews mActivity;
    private ListView mListView;
    private int mNumItems = 4;
    private int mNumRowsPerItem = 5;
    private double mScreenHeightFactor = 5 /4;
    @Override
    protected void setUp() throws Exception {
        mActivity = launchActivity(
                "com.android.frameworks.coretests",
                ListOfInternalSelectionViews.class,
                ListOfInternalSelectionViews.getBundleFor(
                    mNumItems,      
                    mNumRowsPerItem,      
                    mScreenHeightFactor)); 
        mListView = mActivity.getListView();
    }
    @Override
    protected void tearDown() throws Exception {
        mActivity.finish();
        super.tearDown();
    }
    @MediumTest
    public void testPreconditions() throws Exception {
        assertNotNull(mActivity);
        assertNotNull(mListView);
        assertEquals(mNumItems, mActivity.getNumItems());
        assertEquals(mNumRowsPerItem, mActivity.getNumRowsPerItem());
    }
    public void testScrollingDownInFirstItem() throws Exception {
        for (int i = 0; i < mNumRowsPerItem; i++) {
            assertEquals(0, mListView.getSelectedItemPosition());
            InternalSelectionView view = mActivity.getSelectedView();
            assertInternallySelectedRowOnScreen(view, i);
            if (i < mNumRowsPerItem - 1) {
                sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
                getInstrumentation().waitForIdleSync();
            }
        }
        {
            assertEquals(0, mListView.getSelectedItemPosition());
            InternalSelectionView view = (InternalSelectionView)
                    mListView.getSelectedView();
            final int fadingEdge = mListView.getBottom() - mListView.getVerticalFadingEdgeLength();
            assertTrue("bottom of view should be just above fading edge",
                    view.getBottom() >= fadingEdge - 1 &&
                    view.getBottom() <= fadingEdge);
        }
        {
            assertEquals("should be a second view visible due to the fading edge",
                            2, mListView.getChildCount());
            InternalSelectionView peekingChild = (InternalSelectionView)
                    mListView.getChildAt(1);
            assertNotNull(peekingChild);
            assertEquals("wrong value for peeking list item",
                    mActivity.getLabelForPosition(1), peekingChild.getLabel());
        }
    }
    @MediumTest
    public void testScrollingToSecondItem() throws Exception {
        for (int i = 0; i < mNumRowsPerItem; i++) {
            sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
            getInstrumentation().waitForIdleSync();
        }
        assertEquals("should have moved to second item",
                1, mListView.getSelectedItemPosition());
    }
    @LargeTest
    public void testNoFadingEdgeAtBottomOfLastItem() {
        for (int i = 0; i < mNumItems; i++) {
            for (int j = 0; j < mNumRowsPerItem; j++) {
                if (i < mNumItems - 1 || j < mNumRowsPerItem - 1) {
                    sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
                    getInstrumentation().waitForIdleSync();
                }
            }
        }
        assertEquals(mNumItems - 1, mListView.getSelectedItemPosition());
        InternalSelectionView view = mActivity.getSelectedView();
        assertEquals(mNumRowsPerItem - 1, view.getSelectedRow());
        view.getRectForRow(mTempRect, mNumRowsPerItem - 1);
        mListView.offsetDescendantRectToMyCoords(view, mTempRect);
        assertTrue("bottom of last row of last item should be at " +
                "the bottom of the list view (no fading edge)",
                mListView.getBottom() - mListView.getVerticalFadingEdgeLength() < mTempRect.bottom);
    }
    @LargeTest
    public void testNavigatingUpThroughInternalSelection() throws Exception {
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < mNumRowsPerItem; j++) {
                if (i < 1 || j < mNumRowsPerItem - 1) {
                    sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
                    getInstrumentation().waitForIdleSync();
                }
            }
        }
        {
            assertEquals(1, mListView.getSelectedItemPosition());
            InternalSelectionView view = mActivity.getSelectedView();
            assertEquals(mNumRowsPerItem - 1, view.getSelectedRow());
        }
        for (int i = mNumRowsPerItem - 1; i >= 0; i--) {
            assertEquals(1, mListView.getSelectedItemPosition());
            InternalSelectionView view = mActivity.getSelectedView();
            assertInternallySelectedRowOnScreen(view, i);
            if (i > 0) {
                sendKeys(KeyEvent.KEYCODE_DPAD_UP);
                getInstrumentation().waitForIdleSync();
            }
        }
        {
            assertEquals(1, mListView.getSelectedItemPosition());
            InternalSelectionView view = mActivity.getSelectedView();
            assertEquals(0, view.getSelectedRow());
            view.getDrawingRect(mTempRect);
            mListView.offsetDescendantRectToMyCoords(view, mTempRect);
            assertEquals("top of selected row should be just below top vertical fading edge",
                    mListView.getVerticalFadingEdgeLength(),
                    view.getTop());
        }
        {
            final InternalSelectionView view =
                    (InternalSelectionView) mListView.getChildAt(0);
            assertEquals(mActivity.getLabelForPosition(0), view.getLabel());
        }
    }
    private void assertInternallySelectedRowOnScreen(
            InternalSelectionView internalFocused,
            int row) {
        assertEquals("expecting selected row",
                row, internalFocused.getSelectedRow());
        internalFocused.getRectForRow(mTempRect, row);
        mListView.offsetDescendantRectToMyCoords(internalFocused, mTempRect);
        assertTrue("top of row " + row + " should be on sreen",
                mTempRect.top >= 0);
        assertTrue("bottom of row " + row + " should be on sreen",
                mTempRect.bottom < mActivity.getScreenHeight());
    }
}
