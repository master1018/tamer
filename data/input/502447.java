public class ViewStubTest extends ActivityInstrumentationTestCase<StubbedView> {
    public ViewStubTest() {
        super("com.android.frameworks.coretests", StubbedView.class);
    }
    @Override
    public void setUp() throws Exception {
        super.setUp();
    }
    @MediumTest
    public void testStubbed() throws Exception {
        final StubbedView activity = getActivity();
        final View stub = activity.findViewById(R.id.viewStub);
        assertNotNull("The ViewStub does not exist", stub);
    }
    @UiThreadTest
    @MediumTest
    public void testInflated() throws Exception {
        final StubbedView activity = getActivity();
        final ViewStub stub = (ViewStub) activity.findViewById(R.id.viewStub);
        final View swapped = stub.inflate();
        assertNotNull("The inflated view is null", swapped);
    }
    @UiThreadTest
    @MediumTest
    public void testInflatedId() throws Exception {
        final StubbedView activity = getActivity();
        final ViewStub stub = (ViewStub) activity.findViewById(R.id.viewStubWithId);
        final View swapped = stub.inflate();
        assertNotNull("The inflated view is null", swapped);
        assertTrue("The inflated view has no id", swapped.getId() != View.NO_ID);
        assertTrue("The inflated view has the wrong id", swapped.getId() == R.id.stub_inflated);
    }
    @UiThreadTest
    @MediumTest
    public void testInflatedLayoutParams() throws Exception {
        final StubbedView activity = getActivity();
        final ViewStub stub = (ViewStub) activity.findViewById(R.id.viewStubWithId);
        final View swapped = stub.inflate();
        assertNotNull("The inflated view is null", swapped);
        assertEquals("Both stub and inflated should same width",
                stub.getLayoutParams().width, swapped.getLayoutParams().width);
        assertEquals("Both stub and inflated should same height",
                stub.getLayoutParams().height, swapped.getLayoutParams().height);
    }
}
