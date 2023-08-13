public class ForwardingTest extends ActivityUnitTestCase<Forwarding> {
    private Intent mStartIntent;
    private Button mButton;
    public ForwardingTest() {
        super(Forwarding.class);
      }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mStartIntent = new Intent(Intent.ACTION_MAIN);
    }
    @MediumTest
    public void testPreconditions() {
        startActivity(mStartIntent, null, null);
        mButton = (Button) getActivity().findViewById(R.id.go);
        assertNotNull(getActivity());
        assertNotNull(mButton);
    }
    @MediumTest
    public void testSubLaunch() {
        Forwarding activity = startActivity(mStartIntent, null, null);
        mButton = (Button) activity.findViewById(R.id.go);
        mButton.performClick();
        assertNotNull(getStartedActivityIntent());
        assertTrue(isFinishCalled());
    }
    @MediumTest
    public void testLifeCycleCreate() {
        Forwarding activity = startActivity(mStartIntent, null, null);
        getInstrumentation().callActivityOnStart(activity);
        getInstrumentation().callActivityOnResume(activity);
        getInstrumentation().callActivityOnPause(activity);
        getInstrumentation().callActivityOnStop(activity);
    }
}
