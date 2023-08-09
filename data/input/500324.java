public class ListManagedCursorTest extends ActivityInstrumentationTestCase<ListManagedCursor> {
    private ListManagedCursor mActivity;
    private ListView mListView;
    public ListManagedCursorTest() {
        super("com.android.frameworks.coretests", ListManagedCursor.class);
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
        assertEquals(0, mListView.getFirstVisiblePosition());
    }
    @LargeTest
    public void testKeyScrolling() {
        Instrumentation inst = getInstrumentation();
        int firstVisiblePosition = arrowScroll(inst);
        inst.sendCharacterSync(KeyEvent.KEYCODE_BACK);
        inst.waitForIdleSync();
        assertTrue("List changed to touch mode", !mListView.isInTouchMode()); 
        assertTrue("List did not preserve scroll position", 
                firstVisiblePosition == mListView.getFirstVisiblePosition()); 
    }
    @LargeTest
    public void testTouchScrolling() {
        Instrumentation inst = getInstrumentation();
       int firstVisiblePosition = touchScroll(inst);
        inst.sendCharacterSync(KeyEvent.KEYCODE_BACK);
        inst.waitForIdleSync();
        assertTrue("List not in touch mode", mListView.isInTouchMode()); 
        assertTrue("List did not preserve scroll position", 
                firstVisiblePosition == mListView.getFirstVisiblePosition()); 
    }
    @LargeTest
    public void testKeyScrollingToTouchMode() {
        Instrumentation inst = getInstrumentation();
        int firstVisiblePosition = arrowScroll(inst);
        TouchUtils.dragQuarterScreenUp(this);
        inst.sendCharacterSync(KeyEvent.KEYCODE_BACK);
        inst.waitForIdleSync();
        assertTrue("List did not change to touch mode", mListView.isInTouchMode()); 
        assertTrue("List did not preserve scroll position", 
                firstVisiblePosition == mListView.getFirstVisiblePosition()); 
    }
    @FlakyTest(tolerance=3)
    @LargeTest
    public void testTouchScrollingToTrackballMode() {
        Instrumentation inst = getInstrumentation();
        int firstVisiblePosition = touchScroll(inst);
        inst.sendCharacterSync(KeyEvent.KEYCODE_DPAD_DOWN);
        inst.waitForIdleSync();
        inst.sendCharacterSync(KeyEvent.KEYCODE_DPAD_DOWN);
        inst.waitForIdleSync();
        inst.sendCharacterSync(KeyEvent.KEYCODE_BACK);
        inst.waitForIdleSync();
        assertTrue("List not in trackball mode", !mListView.isInTouchMode());
        assertTrue("List did not preserve scroll position", firstVisiblePosition == mListView
                .getFirstVisiblePosition());
    }
    public int arrowScroll(Instrumentation inst) {
        int count = mListView.getChildCount();
        for (int i = 0; i < count * 2; i++) {
            inst.sendCharacterSync(KeyEvent.KEYCODE_DPAD_DOWN);
        }
        inst.waitForIdleSync();
        int firstVisiblePosition = mListView.getFirstVisiblePosition();
        assertTrue("Arrow scroll did not happen", firstVisiblePosition > 0);
        assertTrue("List still in touch mode", !mListView.isInTouchMode());
        inst.sendCharacterSync(KeyEvent.KEYCODE_DPAD_CENTER);
        inst.waitForIdleSync();
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return firstVisiblePosition;
    }
    public int touchScroll(Instrumentation inst) {
        TouchUtils.dragQuarterScreenUp(this);
        inst.waitForIdleSync();
        TouchUtils.dragQuarterScreenUp(this);
        inst.waitForIdleSync();
        TouchUtils.dragQuarterScreenUp(this);
        inst.waitForIdleSync();
        TouchUtils.dragQuarterScreenUp(this);
        inst.waitForIdleSync();
        int firstVisiblePosition = mListView.getFirstVisiblePosition();
        assertTrue("Touch scroll did not happen", firstVisiblePosition > 0);
        assertTrue("List not in touch mode", mListView.isInTouchMode());
        TouchUtils.clickView(this, mListView.getChildAt(mListView.getChildCount() - 1));
        inst.waitForIdleSync();
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return firstVisiblePosition;
    }
}
