public class ListWithNoFadingEdgeTest extends ActivityInstrumentationTestCase<ListWithNoFadingEdge> {
    private ListView mListView;
    public ListWithNoFadingEdgeTest() {
        super("com.android.frameworks.coretests", ListWithNoFadingEdge.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mListView = getActivity().getListView();
    }
    @MediumTest
    public void testPreconditions() {
        assertNotNull(mListView);
        assertEquals("listview vertical fading edge", 0, mListView.getVerticalFadingEdgeLength());
        assertTrue("expecting that not all views fit on screen",
                mListView.getChildCount() < mListView.getCount());
    }
    @MediumTest
    public void testScrollDownToBottom() {
        final int numItems = mListView.getCount();
        for (int i = 0; i < numItems; i++) {
            assertEquals("selected position", i, mListView.getSelectedItemPosition());
            sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
        }
        assertEquals("selected position", numItems - 1, mListView.getSelectedItemPosition());            
    }
    @LargeTest
    public void testScrollFromBottomToTop() {
        final int numItems = mListView.getCount();
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                mListView.setSelection(numItems - 1);
            }
        });
        getInstrumentation().waitForIdleSync();
        for (int i = numItems - 1; i >=0; i--) {
            assertEquals(i, mListView.getSelectedItemPosition());
            sendKeys(KeyEvent.KEYCODE_DPAD_UP);
        }
        assertEquals("selected position", 0, mListView.getSelectedItemPosition());
    }
}
