public class GridStackFromBottomManyTest extends ActivityInstrumentationTestCase<GridStackFromBottomMany> {
    private GridStackFromBottomMany mActivity;
    private GridView mGridView;
    public GridStackFromBottomManyTest() {
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
}
