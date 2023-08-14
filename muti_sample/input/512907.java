public class ListOfShortShortTallShortShortTest extends ActivityInstrumentationTestCase<ListOfShortShortTallShortShort> {
    private ListView mListView;
    private ListUtil mListUtil;
    public ListOfShortShortTallShortShortTest() {
        super("com.android.frameworks.coretests", ListOfShortShortTallShortShort.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mListView = getActivity().getListView();
        mListUtil = new ListUtil(mListView, getInstrumentation());
    }
    @MediumTest
    public void testPreconditions() {
        assertEquals("list item count", 5, mListView.getCount());
        assertEquals("list visible child count", 3, mListView.getChildCount());
        int firstTwoHeight = mListView.getChildAt(0).getHeight() + mListView.getChildAt(1).getHeight();
        assertTrue("first two items should fit within fading edge",
                firstTwoHeight <= mListView.getVerticalFadingEdgeLength());
        assertTrue("first two items should fit within list max scroll",
                firstTwoHeight <= mListView.getMaxScrollAmount());
    }
    @MediumTest
    public void testFadeTopTwoItemsOut() {
        sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
        sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
        sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
        assertEquals("selected item position", 2, mListView.getSelectedItemPosition());
        assertTrue("selected item top should be above list top",
                mListView.getSelectedView().getTop() < mListUtil.getListTop());
        assertTrue("selected item bottom should be below list bottom",
                mListView.getSelectedView().getBottom() > mListUtil.getListBottom());
        assertEquals("should only be 1 child of list (2 should have been scrolled off and removed",
                1, mListView.getChildCount());
    }
    @LargeTest
    public void testFadeInTwoBottomItems() {
        sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
        sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
        sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
        assertEquals("number of list children", 1, mListView.getChildCount());
        sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
        assertEquals("should have scrolled two extra views onto screen",
                3, mListView.getChildCount());
        assertEquals("new view position", 3, mListView.getChildAt(1).getId());
        assertEquals("new view position", 4, mListView.getChildAt(2).getId());
        assertTrue("bottom most view shouldn't be above list bottom",
                mListView.getChildAt(2).getBottom() >= mListUtil.getListBottom());
    }
    @LargeTest
    public void testFadeOutBottomTwoItems() throws Exception {
        mListUtil.arrowScrollToSelectedPosition(4);
        sendKeys(KeyEvent.KEYCODE_DPAD_UP);
        sendKeys(KeyEvent.KEYCODE_DPAD_UP);
        sendKeys(KeyEvent.KEYCODE_DPAD_UP);
        assertEquals("selected item position", 2, mListView.getSelectedItemPosition());
        assertTrue("selected item top should be at or above list top",
                mListView.getSelectedView().getTop() <= mListUtil.getListTop());
        assertTrue("selected item bottom should be below list bottom",
                mListView.getSelectedView().getBottom() > mListUtil.getListBottom());
        assertEquals("should only be 1 child of list (2 should have been scrolled off and removed",
                1, mListView.getChildCount());        
    }
    @LargeTest
    public void testFadeInTopTwoItems() throws Exception {
        mListUtil.arrowScrollToSelectedPosition(4);
        sendKeys(KeyEvent.KEYCODE_DPAD_UP);
        sendKeys(KeyEvent.KEYCODE_DPAD_UP);
        sendKeys(KeyEvent.KEYCODE_DPAD_UP);
        assertEquals("number of list children", 1, mListView.getChildCount());
        sendKeys(KeyEvent.KEYCODE_DPAD_UP);
        assertEquals("should have scrolled two extra views onto screen",
                3, mListView.getChildCount());
        assertEquals("new view position", 0, mListView.getChildAt(0).getId());
        assertEquals("new view position", 1, mListView.getChildAt(1).getId());
        assertTrue("top most view shouldn't be above list top",
                mListView.getChildAt(0).getTop() <= mListUtil.getListTop());
    }
}
