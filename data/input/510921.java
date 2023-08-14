public class ListGetSelectedViewTest extends ActivityInstrumentationTestCase<ListGetSelectedView> {
    private ListGetSelectedView mActivity;
    private ListView mListView;
    public ListGetSelectedViewTest() {
        super("com.android.frameworks.coretests", ListGetSelectedView.class);
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
        assertEquals(0, mListView.getSelectedItemPosition());
    }
    @LargeTest
    public void testGetSelectedView() {
        View last = mListView.getChildAt(1);
        TouchUtils.clickView(this, last);
        assertNull(mListView.getSelectedItem());
        assertNull(mListView.getSelectedView());
        assertEquals(-1, mListView.getSelectedItemPosition());
    }
}
