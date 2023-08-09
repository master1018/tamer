public class ListBottomGravityManyTest extends ActivityInstrumentationTestCase<ListBottomGravityMany> {
    private ListBottomGravityMany mActivity;
    private ListView mListView;
    public ListBottomGravityManyTest() {
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
}
