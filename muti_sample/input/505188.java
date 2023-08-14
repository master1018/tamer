public class GridInVerticalTest extends ActivityInstrumentationTestCase<GridInVertical> {
    private GridInVertical mActivity;
    private GridView mGridView;
    public GridInVerticalTest() {
        super("com.android.frameworks.coretests", GridInVertical.class);
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
        assertTrue("Grid has 0 width", mGridView.getMeasuredWidth() > 0);
        assertTrue("Grid has 0 height", mGridView.getMeasuredHeight() > 0);
    }
}
