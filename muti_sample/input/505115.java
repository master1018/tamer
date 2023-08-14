public class ListTouchBottomGravityTest extends ActivityInstrumentationTestCase<ListBottomGravity> {
    private ListBottomGravity mActivity;
    private ListView mListView;
    public ListTouchBottomGravityTest() {
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
    @MediumTest
    public void testPullDown() {
        View firstChild = mListView.getChildAt(0);
        TouchUtils.dragViewToBottom(this, firstChild);
        View lastChild = mListView.getChildAt(mListView.getChildCount() - 1);
        assertEquals("Selection still available after touch", -1, 
                mListView.getSelectedItemPosition());
        assertEquals("List is not scrolled to the bottom", mListView.getAdapter().getCount() - 1,
                lastChild.getId());
        assertEquals("Last item is not touching the bottom edge", 
                mListView.getHeight() - mListView.getListPaddingBottom(), lastChild.getBottom());
    }
    @MediumTest
    public void testPushUp() {
        View lastChild = mListView.getChildAt(mListView.getChildCount() - 1);
        TouchUtils.dragViewToTop(this, lastChild);
        lastChild = mListView.getChildAt(mListView.getChildCount() - 1);
        assertEquals("Selection still available after touch", -1, 
                mListView.getSelectedItemPosition());
        assertEquals("List is not scrolled to the bottom", mListView.getAdapter().getCount() - 1,
                lastChild.getId());
        assertEquals("Last item is not touching the bottom edge",  
                mListView.getHeight() - mListView.getListPaddingBottom(), lastChild.getBottom());
    }
}
