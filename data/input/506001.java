public class GridTouchSetSelectionTest extends ActivityInstrumentationTestCase<GridSimple> {
    private GridSimple mActivity;
    private GridView mGridView;
    public GridTouchSetSelectionTest() {
        super("com.android.frameworks.coretests", GridSimple.class);
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
    }
    @LargeTest
    public void testSetSelection() {
        TouchUtils.dragQuarterScreenDown(this);
        TouchUtils.dragQuarterScreenUp(this);
        assertEquals("Selection still available after touch", -1, 
                mGridView.getSelectedItemPosition());
        final int targetPosition = mGridView.getAdapter().getCount() / 2;
        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                mGridView.setSelection(targetPosition);
            }
        });
        getInstrumentation().waitForIdleSync();
        boolean found = false;
        int childCount = mGridView.getChildCount();
        for (int i=0; i<childCount; i++) {
            View child = mGridView.getChildAt(i);
            if (child.getId() == targetPosition) {
                found = true;
                break;
            }
        }
        assertTrue("Selected item not visible in list", found);
    }
}
