public class ListItemFocusablesFarApartTest extends ActivityInstrumentationTestCase<ListItemFocusablesFarApart> {
    private ListView mListView;
    private int mListTop;
    private int mListBottom;
    public ListItemFocusablesFarApartTest() {
        super("com.android.frameworks.coretests", ListItemFocusablesFarApart.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mListView = getActivity().getListView();
        mListTop = mListView.getListPaddingTop();
        mListBottom = mListView.getHeight() - mListView.getListPaddingBottom();
    }
    public View getChildOfItem(int listIndex, int index) {
        return ((ViewGroup) mListView.getChildAt(listIndex)).getChildAt(index);
    }
    public int getTopOfChildOfItem(int listIndex, int index) {
        ViewGroup listItem = (ViewGroup) mListView.getChildAt(listIndex);
        View child = listItem.getChildAt(index);
        return child.getTop() + listItem.getTop();
    }
    public int getBottomOfChildOfItem(int listIndex, int index) {
        ViewGroup listItem = (ViewGroup) mListView.getChildAt(listIndex);
        View child = listItem.getChildAt(index);
        return child.getBottom() + listItem.getTop();
    }
    @MediumTest
    public void testPreconditions() {
        assertNotNull(mListView);
        assertEquals("should only be one visible list item",
                1, mListView.getChildCount());
        int topOfFirstButton = getTopOfChildOfItem(0, 0);
        int topOfSecondButton = getTopOfChildOfItem(0, 2);
        assertTrue("second button should be more than max scroll away from first",
                topOfSecondButton - topOfFirstButton > mListView.getMaxScrollAmount());
    }
    @MediumTest
    public void testPanWhenNextFocusableTooFarDown() {
        int expectedTop = mListView.getChildAt(0).getTop();
        final Button topButton = (Button) getChildOfItem(0, 0);
        int counter = 0;
        while(getTopOfChildOfItem(0, 2) > mListBottom) {
            if (counter > 5) fail("couldn't reach next button within " + counter + " downs");
            if (getBottomOfChildOfItem(0, 0) < mListTop) {
                assertFalse("after " + counter + " downs, top button not visible, should not have focus",
                        topButton.isFocused());
                assertFalse("after " + counter + " downs, neither top button nor botom button visible, nothng within first list " +
                        "item should have focus", mListView.getChildAt(0).hasFocus());
            } else {
                assertTrue("after " + counter + " downs, top button still visible, should have focus",
                        topButton.isFocused());
            }
            assertEquals("after " + counter + " downs, " +
                    "should have panned by max scroll amount",
                    expectedTop, mListView.getChildAt(0).getTop());
            sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
            expectedTop -= mListView.getMaxScrollAmount();
            counter++;
        }
        assertTrue("second button should have focus",
                getChildOfItem(0, 2).isFocused());
    }
}
