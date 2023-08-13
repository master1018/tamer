public class ListOfShortTallShortTest extends ActivityInstrumentationTestCase<ListOfShortTallShort> {
    private ListView mListView;
    public ListOfShortTallShortTest() {
        super("com.android.frameworks.coretests", ListOfShortTallShort.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mListView = getActivity().getListView();
    }
    @MediumTest
    public void testPreconditions() {
        assertTrue("second item should be taller than screen",
                mListView.getChildAt(1).getHeight() > mListView.getHeight());
    }
    @MediumTest
    public void testGoDownFromShortToTall() {
        int topBeforeMove = mListView.getChildAt(1).getTop();
        sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
        assertEquals("selection should have moved to tall item below",
                1, mListView.getSelectedItemPosition());
        assertEquals("should not have scrolled; top should be the same.",
                topBeforeMove,
                mListView.getSelectedView().getTop());
    }
    @MediumTest
    public void testGoUpFromShortToTall() {
        int maxMoves = 8;
        while (mListView.getSelectedItemPosition() != 2 && maxMoves > 0) {
            sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
        }
        assertEquals("couldn't get to 3rd item",
                2,
                mListView.getSelectedItemPosition());
        assertEquals("should only be two items on screen",
                2, mListView.getChildCount());
        assertEquals("selected item should be last item on screen",
                mListView.getChildAt(1), mListView.getSelectedView());
        final int bottomBeforeMove = mListView.getChildAt(0).getBottom();
        sendKeys(KeyEvent.KEYCODE_DPAD_UP);
        assertEquals("should have moved selection to tall item above",
                1, mListView.getSelectedItemPosition());
        assertEquals("should not have scrolled, top should be the same",
                bottomBeforeMove,
                mListView.getChildAt(0).getBottom());
    }
}
