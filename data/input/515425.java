public class ListItemsExpandOnSelectionTest extends ActivityInstrumentationTestCase<ListItemsExpandOnSelection> {
    private ListView mListView;
    private int mListTop;
    private int mListBottom;
    private int mExpandedHeight;
    private int mNormalHeight;
    public ListItemsExpandOnSelectionTest() {
        super("com.android.frameworks.coretests",
                ListItemsExpandOnSelection.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mListView = getActivity().getListView();
        mListTop = mListView.getListPaddingTop();
        mListBottom = mListView.getHeight() - mListView.getListPaddingBottom();
        mExpandedHeight = mListView.getChildAt(0).getHeight();
        mNormalHeight = mListView.getChildAt(1).getHeight();
    }
    @MediumTest
    public void testPreconditions() {
        assertEquals(0, mListView.getSelectedItemPosition());
        assertEquals("selected item should be 1.5 times taller than the others",
                mExpandedHeight, (int) (mNormalHeight * 1.5));
    }
    @MediumTest
    public void testMoveSelectionDownNotRequiringScroll() {
        sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
        assertEquals(1, mListView.getSelectedItemPosition());
        assertEquals("first item's top should not have shifted",
                mListTop, mListView.getChildAt(0).getTop());
    }
    @MediumTest
    public void testMoveSelectionUpNotRequiringScroll() {
        sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
        assertEquals(1, mListView.getSelectedItemPosition());
        final int oldBottom = mListView.getSelectedView().getBottom();
        sendKeys(KeyEvent.KEYCODE_DPAD_UP);
        assertEquals("bottom of 2nd itme should have stayed the same when " +
                "moving back up",
                oldBottom,
                mListView.getChildAt(1).getBottom());
    }
    @MediumTest
    public void testMoveSelectionDownRequiringScroll() {
        int lastItemIndex = mListView.getChildCount() - 1;
        for(int i = 0; i < lastItemIndex; i++) {
            sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
        }
        getInstrumentation().waitForIdleSync();
        assertEquals("list position", lastItemIndex, mListView.getSelectedItemPosition());
        assertEquals("expanded height", mExpandedHeight, mListView.getSelectedView().getHeight());
        assertEquals("should be above bottom fading edge",
                mListBottom - mListView.getVerticalFadingEdgeLength(),
                mListView.getSelectedView().getBottom());
    }
    @LargeTest
    public void testMoveSelectionUpRequiringScroll() {
        int childrenPerScreen = mListView.getChildCount();
        for(int i = 0; i < childrenPerScreen; i++) {
            sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
        }
        for(int i = 0; i < childrenPerScreen - 1; i++) {
            sendKeys(KeyEvent.KEYCODE_DPAD_UP);
        }
        getInstrumentation().waitForIdleSync();
        assertEquals("list position", 1, mListView.getSelectedItemPosition());
        assertEquals("expanded height", mExpandedHeight, mListView.getSelectedView().getHeight());
        assertEquals("should be below top fading edge",
                mListTop + mListView.getVerticalFadingEdgeLength(),
                mListView.getSelectedView().getTop());
    }
}
