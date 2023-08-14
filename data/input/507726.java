public class GridScrollListenerTest extends ActivityInstrumentationTestCase<GridScrollListener> implements
        AbsListView.OnScrollListener {
    private GridScrollListener mActivity;
    private GridView mGridView;
    private int mFirstVisibleItem = -1;
    private int mVisibleItemCount = -1;
    private int mTotalItemCount = -1;
    public GridScrollListenerTest() {
        super("com.android.frameworks.coretests", GridScrollListener.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mActivity = getActivity();
        mGridView = getActivity().getGridView();
        mGridView.setOnScrollListener(this);
    }
    @MediumTest
    public void testPreconditions() {
        assertNotNull(mActivity);
        assertNotNull(mGridView);
        assertEquals(0, mFirstVisibleItem);
    }
    @LargeTest
    public void testKeyScrolling() {
        Instrumentation inst = getInstrumentation();
        int firstVisibleItem = mFirstVisibleItem;
        for (int i = 0; i < mVisibleItemCount * 2; i++) {
            inst.sendCharacterSync(KeyEvent.KEYCODE_DPAD_DOWN);
        }
        inst.waitForIdleSync();
        assertTrue("Arrow scroll did not happen", mFirstVisibleItem > firstVisibleItem);
        firstVisibleItem = mFirstVisibleItem;
        inst.sendCharacterSync(KeyEvent.KEYCODE_SPACE);
        inst.waitForIdleSync();
        assertTrue("Page scroll did not happen", mFirstVisibleItem > firstVisibleItem);
        firstVisibleItem = mFirstVisibleItem;
        KeyEvent down = new KeyEvent(0, 0, KeyEvent.ACTION_DOWN, 
                KeyEvent.KEYCODE_DPAD_DOWN, 0, KeyEvent.META_ALT_ON);
        KeyEvent up = new KeyEvent(0, 0, KeyEvent.ACTION_UP, 
                KeyEvent.KEYCODE_DPAD_DOWN, 0, KeyEvent.META_ALT_ON);
        inst.sendKeySync(down);
        inst.sendKeySync(up);
        inst.waitForIdleSync();
        assertTrue("Full scroll did not happen", mFirstVisibleItem > firstVisibleItem);
        assertEquals("Full scroll did not happen", mTotalItemCount, 
                mFirstVisibleItem + mVisibleItemCount);    
    }
    @LargeTest
    public void testTouchScrolling() {
        Instrumentation inst = getInstrumentation();
        int firstVisibleItem = mFirstVisibleItem;
        TouchUtils.dragQuarterScreenUp(this);
        TouchUtils.dragQuarterScreenUp(this);
        assertTrue("Touch scroll did not happen", mFirstVisibleItem > firstVisibleItem);
    }
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        mFirstVisibleItem = firstVisibleItem;
        mVisibleItemCount = visibleItemCount;
        mTotalItemCount = totalItemCount;
    }
    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }
}
