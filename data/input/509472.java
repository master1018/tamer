public class LinearLayoutEditTextsTest extends ActivityInstrumentationTestCase<LinearLayoutEditTexts> {
    private View mChild;
    private View mContainer;
    public LinearLayoutEditTextsTest() {
        super("com.android.frameworks.coretests", LinearLayoutEditTexts.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        final Activity activity = getActivity();
        mChild = activity.findViewById(R.id.editText1);
        mContainer = activity.findViewById(R.id.layout);
    }
    @MediumTest
    public void testPreconditions() {
        assertNotNull(mChild);
        assertNotNull(mContainer);
    }
    @MediumTest
    public void testLayout() {
        final int childHeight = mChild.getHeight();
        final int containerHeight = mContainer.getHeight();
        assertEquals(containerHeight, childHeight);
    }
}
