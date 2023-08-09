public class ListEmptyViewTest extends ActivityInstrumentationTestCase<ListWithEmptyView> {
    private ListWithEmptyView mActivity;
    private ListView mListView;
    public ListEmptyViewTest() {
        super("com.android.frameworks.coretests", ListWithEmptyView.class);
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
        assertTrue("Empty view not shown", mListView.getVisibility() == View.GONE);
    }
    @MediumTest
    public void testZeroToOne() {
        Instrumentation inst = getInstrumentation();
        inst.invokeMenuActionSync(mActivity, mActivity.MENU_ADD, 0);
        inst.waitForIdleSync();
        assertTrue("Empty view still shown", mActivity.getEmptyView().getVisibility() == View.GONE);
        assertTrue("List not shown", mActivity.getListView().getVisibility() == View.VISIBLE);
    }
    @MediumTest
    public void testZeroToOneForwardBack() {
        Instrumentation inst = getInstrumentation();
        inst.invokeMenuActionSync(mActivity, mActivity.MENU_ADD, 0);
        inst.waitForIdleSync();
        assertTrue("Empty view still shown", mActivity.getEmptyView().getVisibility() == View.GONE);
        assertTrue("List not shown", mActivity.getListView().getVisibility() == View.VISIBLE);
        Intent intent = new Intent();
        intent.setClass(mActivity, ListWithEmptyView.class);
        mActivity.startActivity(intent);
        inst.sendCharacterSync(KeyEvent.KEYCODE_BACK);
        inst.waitForIdleSync();
        assertTrue("Empty view still shown", mActivity.getEmptyView().getVisibility() == View.GONE);
        assertTrue("List not shown", mActivity.getListView().getVisibility() == View.VISIBLE);
    }
    @LargeTest
    public void testZeroToManyToZero() {
        Instrumentation inst = getInstrumentation();
        int i;
        for (i = 0; i < 10; i++) {
            inst.invokeMenuActionSync(mActivity, mActivity.MENU_ADD, 0);
            inst.waitForIdleSync();
            assertTrue("Empty view still shown",
                    mActivity.getEmptyView().getVisibility() == View.GONE);
            assertTrue("List not shown", mActivity.getListView().getVisibility() == View.VISIBLE);
        }
        for (i = 0; i < 10; i++) {
            inst.invokeMenuActionSync(mActivity, mActivity.MENU_REMOVE, 0);
            inst.waitForIdleSync();
            if (i < 9) {
                assertTrue("Empty view still shown",
                        mActivity.getEmptyView().getVisibility() == View.GONE);
                assertTrue("List not shown",
                        mActivity.getListView().getVisibility() == View.VISIBLE);
            } else {
                assertTrue("Empty view not shown",
                        mActivity.getEmptyView().getVisibility() == View.VISIBLE);
                assertTrue("List still shown",
                        mActivity.getListView().getVisibility() == View.GONE);
            }
        }
        Intent intent = new Intent();
        intent.setClass(mActivity, ListWithEmptyView.class);
        mActivity.startActivity(intent);
        inst.sendCharacterSync(KeyEvent.KEYCODE_BACK);
        inst.waitForIdleSync();
        assertTrue("Empty view not shown", mActivity.getEmptyView().getVisibility() == View.VISIBLE);
        assertTrue("List still shown", mActivity.getListView().getVisibility() == View.GONE);
    }
}
