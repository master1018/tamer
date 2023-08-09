public class ListBottomGravityTest extends ActivityInstrumentationTestCase<ListBottomGravity> {
    private ListBottomGravity mActivity;
    private ListView mListView;
    public ListBottomGravityTest() {
        super("com.android.frameworks.coretests", ListBottomGravity.class);
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
