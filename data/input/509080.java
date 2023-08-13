public class RunQueueTest extends ActivityInstrumentationTestCase<RunQueue> {
    public RunQueueTest() {
        super("com.android.frameworks.coretests", RunQueue.class);
    }
    @Override
    public void setUp() throws Exception {
        super.setUp();
    }
    @MediumTest
    public void testRunnableRan() throws Exception {
        final RunQueue activity = getActivity();
        getInstrumentation().waitForIdleSync();
        assertTrue("The runnable did not run", activity.runnableRan);
    }
    @MediumTest
    public void testRunnableCancelled() throws Exception {
        final RunQueue activity = getActivity();
        getInstrumentation().waitForIdleSync();
        assertTrue("The runnable was not cancelled", activity.runnableCancelled);
    }
    @MediumTest
    public void testListenerFired() throws Exception {
        final RunQueue activity = getActivity();
        getInstrumentation().waitForIdleSync();
        assertTrue("The global layout listener did not fire", activity.globalLayout);
    }
    @MediumTest
    public void testTreeObserverKilled() throws Exception {
        final RunQueue activity = getActivity();
        getInstrumentation().waitForIdleSync();
        assertFalse("The view tree observer is still alive", activity.viewTreeObserver.isAlive());
    }
}
