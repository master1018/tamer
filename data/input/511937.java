public class LaunchTest extends ActivityTestsBase {
    @LargeTest
    public void testColdActivity() throws Exception {
        mIntent.putExtra("component", new ComponentName(getContext(), TestedActivity.class));
        runLaunchpad(LaunchpadActivity.LAUNCH);
    }
    @LargeTest
    public void testLocalActivity() throws Exception {
        mIntent.putExtra("component", new ComponentName(getContext(), LocalActivity.class));
        runLaunchpad(LaunchpadActivity.LAUNCH);
    }
    @LargeTest
    public void testColdScreen() throws Exception {
        mIntent.putExtra("component", new ComponentName(getContext(), TestedScreen.class));
        runLaunchpad(LaunchpadActivity.LAUNCH);
    }
    @LargeTest
    public void testLocalScreen() throws Exception {
        mIntent.putExtra("component", new ComponentName(getContext(), LocalScreen.class));
        runLaunchpad(LaunchpadActivity.LAUNCH);
    }
    @LargeTest
    public void testForwardResult() throws Exception {
        runLaunchpad(LaunchpadActivity.FORWARD_RESULT);
    }
    public void xxtestBadParcelable() throws Exception {
        runLaunchpad(LaunchpadActivity.BAD_PARCELABLE);
    }
    @LargeTest
    public void testClearTopInCreate() throws Exception {
        mIntent.putExtra("component", new ComponentName(getContext(), ClearTop.class));
        runLaunchpad(LaunchpadActivity.LAUNCH);
    }
    @LargeTest
    public void testClearTopWhileResumed() throws Exception {
        mIntent.putExtra("component", new ComponentName(getContext(), ClearTop.class));
        mIntent.putExtra(ClearTop.WAIT_CLEAR_TASK, true);
        runLaunchpad(LaunchpadActivity.LAUNCH);
    }
}
