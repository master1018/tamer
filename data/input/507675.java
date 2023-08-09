public class WeightSumTest extends ActivityInstrumentationTestCase<WeightSum> {
    private View mChild;
    private View mContainer;
    public WeightSumTest() {
        super("com.android.frameworks.coretests", WeightSum.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        final Activity activity = getActivity();
        mChild = activity.findViewById(R.id.child);
        mContainer = activity.findViewById(R.id.container);
    }
    @MediumTest
    public void testPreconditions() {
        assertNotNull(mChild);
        assertNotNull(mContainer);
    }
    @MediumTest
    public void testLayout() {
        final int childWidth = mChild.getWidth();
        final int containerWidth = mContainer.getWidth();
        assertEquals(containerWidth / 2, childWidth);
    }
}
