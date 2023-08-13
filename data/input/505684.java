public class ListUnspecifiedMeasure<T extends Activity> extends ActivityInstrumentationTestCase<T> {
    private T mActivity;
    private ListView mListView;
    protected ListUnspecifiedMeasure(Class<T> klass) {
        super("com.android.frameworks.coretests", klass);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mActivity = getActivity();
        mListView = (ListView) mActivity.findViewById(R.id.list);
    }
    @MediumTest
    public void testPreconditions() {
        assertNotNull(mActivity);
        assertNotNull(mListView);
    }
    @MediumTest
    public void testWasMeasured() {
        assertTrue(mListView.getMeasuredWidth() > 0);
        assertTrue(mListView.getWidth() > 0);
        assertTrue(mListView.getMeasuredHeight() > 0);
        assertTrue(mListView.getHeight() > 0);
    }
}
