public class GridInHorizontalTest extends ActivityInstrumentationTestCase<GridInHorizontal> {
    private GridInHorizontal mActivity;
    private GridView mGridView;
    public GridInHorizontalTest() {
        super("com.android.frameworks.coretests", GridInHorizontal.class);
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
