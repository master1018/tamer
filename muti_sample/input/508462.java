public class ListHeterogeneousTest extends ActivityInstrumentationTestCase<ListHeterogeneous> {
    private ListHeterogeneous mActivity;
    private ListView mListView;
    public ListHeterogeneousTest() {
        super("com.android.frameworks.coretests", ListHeterogeneous.class);
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
    @LargeTest
    public void testKeyScrolling() {
        Instrumentation inst = getInstrumentation();
        int count = mListView.getAdapter().getCount();
        for (int i = 0; i < count - 1; i++) {
            inst.sendCharacterSync(KeyEvent.KEYCODE_DPAD_DOWN);
        }
        inst.waitForIdleSync();
        int convertMissesBefore = mActivity.getConvertMisses();
        assertEquals("Unexpected convert misses", 0, convertMissesBefore);
        for (int i = 0; i < count - 1; i++) {
            inst.sendCharacterSync(KeyEvent.KEYCODE_DPAD_UP);
        }
        inst.waitForIdleSync();
        int convertMissesAfter = mActivity.getConvertMisses();
        assertEquals("Unexpected convert misses", 0, convertMissesAfter);
    }
}
