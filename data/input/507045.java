public class ListTouchTest extends ActivityInstrumentationTestCase<ListTopGravity> {
    private ListTopGravity mActivity;
    private ListView mListView;
    public ListTouchTest() {
        super("com.android.frameworks.coretests", ListTopGravity.class);
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
    @MediumTest
    public void testPullDown() {
        View firstChild = mListView.getChildAt(0);
        TouchUtils.dragViewToBottom(this, firstChild);
        assertEquals("Selection still available after touch", -1, 
                mListView.getSelectedItemPosition());
        firstChild = mListView.getChildAt(0);
        assertEquals("Item zero not the first child in the list", 0, firstChild.getId());
        assertEquals("Item zero not at the top of the list", mListView.getListPaddingTop(),
                firstChild.getTop());
    }
    @MediumTest
    public void testPushUp() {
        View lastChild = mListView.getChildAt(mListView.getChildCount() - 1);
        TouchUtils.dragViewToTop(this, lastChild);
        assertEquals("Selection still available after touch", -1, 
                mListView.getSelectedItemPosition());
        View firstChild = mListView.getChildAt(0);
        assertEquals("Item zero not the first child in the list", 0, firstChild.getId());
        assertEquals("Item zero not at the top of the list", mListView.getListPaddingTop(),
                firstChild.getTop());
    }
}
