public class ListOfTouchablesTest extends ActivityInstrumentationTestCase<ListOfTouchables> {
    private ListOfTouchables mActivity;
    private ListView mListView;
    public ListOfTouchablesTest() {
        super("com.android.frameworks.coretests", ListOfTouchables.class);
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
    }
    public void testShortScroll() {
        View firstChild = mListView.getChildAt(0);
        View lastChild = mListView.getChildAt(mListView.getChildCount() - 1);
        int firstTop = firstChild.getTop();
        TouchUtils.dragViewBy(this, lastChild, Gravity.TOP | Gravity.LEFT,
                0, -(ViewConfiguration.getTouchSlop() + 1 + 10));
        View newFirstChild = mListView.getChildAt(0);
        assertEquals("View scrolled too early", firstTop, newFirstChild.getTop() + 10);
        assertEquals("Wrong view in first position", 0, newFirstChild.getId());
    }
    public void testLongScroll() {
        View lastChild = mListView.getChildAt(mListView.getChildCount() - 1);
        int lastTop = lastChild.getTop();
        int distance = TouchUtils.dragViewToY(this, lastChild, 
                Gravity.TOP | Gravity.LEFT, mListView.getTop());
        assertEquals("View scrolled to wrong position", 
                lastTop - (distance - ViewConfiguration.getTouchSlop() - 1), lastChild.getTop());
    } 
}
