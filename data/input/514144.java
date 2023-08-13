public class ListWithFooterViewAndNewLabelsTest
        extends ActivityInstrumentationTestCase<ListWithFooterViewAndNewLabels> {
    private Button mButton;
    private ListAdapter mAdapter;
    private ListView mListView;
    public ListWithFooterViewAndNewLabelsTest() {
        super("com.android.frameworks.coretests",
                ListWithFooterViewAndNewLabels.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ListWithFooterViewAndNewLabels a = getActivity();
        mButton = (Button) a.findViewById(R.id.button);
        mAdapter = a.getListAdapter();
        mListView = a.getListView();
    }
    public void FAILING_testPreconditions() {
        assertNotNull(mButton);
        assertNotNull(mAdapter);
        assertNotNull(mListView);
        assertTrue(mButton.hasFocus());
        assertEquals("expected list adapter to have 1 item",
                1, mAdapter.getCount());
        assertEquals("expected list view to have 2 items (1 in adapter, plus " 
                + "the footer view).",
                2, mListView.getCount());
        assertEquals("Expecting the selected index to be 0, the first non footer "
                + "view item.",
                0, mListView.getSelectedItemPosition());
    }
}
