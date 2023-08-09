public class FillInWrapTest extends ActivityInstrumentationTestCase<FillInWrap> {
    private View mChild;
    private View mContainer;
    public FillInWrapTest() {
        super("com.android.frameworks.coretests", FillInWrap.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        final Activity activity = getActivity();
        mChild = activity.findViewById(R.id.data);
        mContainer = activity.findViewById(R.id.layout);
    }
    @MediumTest
    public void testPreconditions() {
        assertNotNull(mChild);
        assertNotNull(mContainer);
    }
    @MediumTest
    public void testLayout() {
        assertTrue("the child's height should be less than the parent's",
                mChild.getMeasuredHeight() < mContainer.getMeasuredHeight());
    }
}
