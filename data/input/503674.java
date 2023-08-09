public class GridSetSelectionBaseTest<T extends GridScenario> extends ActivityInstrumentationTestCase<T> {
    private T mActivity;
    private GridView mGridView;
    protected GridSetSelectionBaseTest(Class<T> klass) {
        super("com.android.frameworks.coretests", klass);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mActivity = getActivity();
        mGridView = getActivity().getGridView();
    }
    @MediumTest
    public void testPreconditions() {
        assertNotNull(mActivity);
        assertNotNull(mGridView);
        if (mGridView.isStackFromBottom()) {
            assertEquals(mGridView.getAdapter().getCount() - 1,
                    mGridView.getSelectedItemPosition());
        } else {
            assertEquals(0, mGridView.getSelectedItemPosition());
        }
    }
    @MediumTest
    public void testSetSelectionToTheEnd() {
        final int target = mGridView.getAdapter().getCount() - 1;
        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                mGridView.setSelection(target);
            }
        });
        getInstrumentation().waitForIdleSync();
        assertEquals(mGridView.getSelectedItemPosition(), target);
        assertNotNull(mGridView.getSelectedView());
        ViewAsserts.assertOnScreen(mGridView, mGridView.getSelectedView());
    }
    @MediumTest
    public void testSetSelectionToMiddle() {
        final int target = mGridView.getAdapter().getCount() / 2;
        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                mGridView.setSelection(target);
            }
        });
        getInstrumentation().waitForIdleSync();
        assertEquals(mGridView.getSelectedItemPosition(), target);
        assertNotNull(mGridView.getSelectedView());
        ViewAsserts.assertOnScreen(mGridView, mGridView.getSelectedView());
    }
    @MediumTest
    public void testSetSelectionToTheTop() {
        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                mGridView.setSelection(0);
            }
        });
        getInstrumentation().waitForIdleSync();
        assertEquals(mGridView.getSelectedItemPosition(), 0);
        assertNotNull(mGridView.getSelectedView());
        ViewAsserts.assertOnScreen(mGridView, mGridView.getSelectedView());
    }
}
