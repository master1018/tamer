public class GridSingleColumnTest extends ActivityInstrumentationTestCase<GridSingleColumn> {
    private GridSingleColumn mActivity;
    private GridView mGridView;
    public GridSingleColumnTest() {
        super("com.android.frameworks.coretests", GridSingleColumn.class);
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
}
