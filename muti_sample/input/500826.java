public class ListViewHeightTest extends ActivityInstrumentationTestCase<ListViewHeight> {
    private ListViewHeight mActivity;
    public ListViewHeightTest() {
        super("com.android.frameworks.coretests", ListViewHeight.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mActivity = getActivity();
    }
    @MediumTest
    public void testPreconditions() {
        assertNotNull(mActivity);
    }
    @MediumTest
    public void testButtons() {
        Instrumentation inst = getInstrumentation();
        final Button button1 = (Button) mActivity.findViewById(R.id.button1);
        final Button button2 = (Button) mActivity.findViewById(R.id.button2);
        final Button button3 = (Button) mActivity.findViewById(R.id.button3);
        ListView list = (ListView) mActivity.findViewById(R.id.inner_list);
        assertEquals("Unexpected items in adapter", 0, list.getCount());
        assertEquals("Unexpected children in list view", 0, list.getChildCount());
        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                button1.performClick();
            }
        });
        inst.waitForIdleSync();
        assertTrue("List not be visible after clicking button1", list.isShown());
        assertTrue("List incorrect height", list.getHeight() == 200);
        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                button2.performClick();
            }
        });
        inst.waitForIdleSync();
        assertTrue("List not be visible after clicking button2", list.isShown());
        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                button3.performClick();
            }
        });
        inst.waitForIdleSync();
        assertFalse("List should not be visible clicking button3", list.isShown());
    }
}
