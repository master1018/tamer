public class ListOfItemsTallerThanScreenTest
        extends ActivityInstrumentationTestCase<ListOfItemsTallerThanScreen> {
    private ListView mListView;
    private ListOfItemsTallerThanScreen mActivity;
    protected void setUp() throws Exception {
        super.setUp();
        mActivity = getActivity();
        mListView = getActivity().getListView();
    }
    public ListOfItemsTallerThanScreenTest() {
        super("com.android.frameworks.coretests", ListOfItemsTallerThanScreen.class);
    }
    @MediumTest
    public void testPreconditions() {
        assertNotNull(mListView);
        assertEquals("should only be one visible child", 1, mListView.getChildCount());
        final int amountOffScreen = mListView.getChildAt(0).getBottom() - (mListView.getBottom() - mListView.getListPaddingBottom());
        assertTrue("must be more than max scroll off screen for this test to work",
                amountOffScreen > mListView.getMaxScrollAmount());
    }
    @MediumTest
    public void testScrollDownAcrossItem() {
        final View view = mListView.getSelectedView();
        assertTrue(view.isSelected());
        assertEquals(mListView.getListPaddingTop(),
                view.getTop());
        assertTrue("view must be taller than screen for this test to be worth anything",
                view.getBottom() > mListView.getBottom());
        int numScrollsUntilNextViewVisible = getNumDownPressesToScrollDownAcrossSelected();
        for (int i = 0; i < numScrollsUntilNextViewVisible; i++) {
            assertEquals("after " + i + " down scrolls across tall item",
                    mListView.getListPaddingTop() - mListView.getMaxScrollAmount() * i,
                    view.getTop());
            sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
        }
        assertEquals("child count", 2, mListView.getChildCount());
        assertEquals("selected position", 0, mListView.getSelectedItemPosition());
        assertTrue("same view should be selected", view.isSelected());
        final View peekingView = mListView.getChildAt(1);
        assertEquals(view.getBottom(), peekingView.getTop());
    }
    @MediumTest
    public void testScrollDownToNextItem() {
        final int numPresses = getNumDownPressesToScrollDownAcrossSelected();
        assertEquals(1, mListView.getChildCount());
        for (int i = 0; i < numPresses; i++) {
            sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
        }
        final int topOfPeekingNext = mListView.getChildAt(1).getTop();
        sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
        assertFalse(mListView.getChildAt(0).isSelected());
        assertEquals(2, mListView.getChildCount());
        final View next = mListView.getChildAt(1);
        assertTrue("has selection", next.isSelected());
        assertEquals(topOfPeekingNext - (mListView.getMaxScrollAmount()), next.getTop());
    }
    @MediumTest
    public void testScrollFirstItemOffScreen() {
        int numDownsToGetFirstItemOffScreen =
                (mListView.getSelectedView().getHeight() / mListView.getMaxScrollAmount()) + 1;
        for (int i = 0; i < numDownsToGetFirstItemOffScreen; i++) {
            sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
        }
        getInstrumentation().waitForIdleSync();
        assertEquals("should be at next item",
                1, mListView.getSelectedItemPosition());
        final int listTop = mListView.getTop() + mListView.getListPaddingTop();
        assertTrue("top of selected view should be above top of list",
                mListView.getSelectedView().getTop() < listTop);
        assertEquals("off screen item shouldn't be a child of list view",
                1, mListView.getChildCount());
    }
    @LargeTest
    public void testScrollDownToLastItem() {
        final int numItems = mListView.getAdapter().getCount();
        int maxDowns = 20;
        while (mListView.getSelectedItemPosition() < (numItems - 1)) {
            sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
            if (--maxDowns <= 0) {
                fail("couldn't get to last item within 20 down arrows");
            }
        }
        getInstrumentation().waitForIdleSync();
        final int numDownsLeft = getNumDownPressesToScrollDownAcrossSelected();
        assertTrue(numDownsLeft > 0);
        for (int i = 0; i < numDownsLeft; i++) {
            sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
        }
        sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
        getInstrumentation().waitForIdleSync();
        assertEquals(numItems - 1, mListView.getSelectedItemPosition());
        final int realBottom = mListView.getHeight() - mListView.getListPaddingBottom();
        assertEquals(realBottom, mListView.getSelectedView().getBottom());
        assertEquals("views scrolled off screen should be removed from view group",
                1, mListView.getChildCount());
    }
    @MediumTest
    public void testScrollUpAcrossFirstItem() {
        final int listTop = mListView.getListPaddingTop();
        assertEquals(listTop, mListView.getSelectedView().getTop());
        final int numPresses = getNumDownPressesToScrollDownAcrossSelected();
        for (int i = 0; i < numPresses; i++) {
            sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
        }
        assertEquals(2, mListView.getChildCount());
        for (int i = 0; i < numPresses; i++) {
            sendKeys(KeyEvent.KEYCODE_DPAD_UP);
            assertEquals(1, mListView.getChildCount());
        }
        assertEquals(listTop, mListView.getSelectedView().getTop());
    }
    private int getNumDownPressesToScrollDownAcrossSelected() {
        View selected = mListView.getSelectedView();
        int realBottom = mListView.getBottom() - mListView.getListPaddingBottom();
        assertTrue("view should be overlapping bottom",
                selected.getBottom() > realBottom);
        assertTrue("view should be overlapping bottom",
                selected.getTop() < realBottom);
        int pixelsOffScreen = selected.getBottom() - realBottom;
        return (pixelsOffScreen / mListView.getMaxScrollAmount()) + 1;
    }
}
