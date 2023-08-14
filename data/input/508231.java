public class SubActivityTest extends ActivityTestsBase {
    public void testPendingResult() throws Exception {
        mIntent.putExtra("component", new ComponentName(getContext(), SubActivityScreen.class));
        mIntent.putExtra("mode", SubActivityScreen.PENDING_RESULT_MODE);
        runLaunchpad(LaunchpadActivity.LAUNCH);
    }
    public void testNoResult() throws Exception {
        mIntent.putExtra("component", new ComponentName(getContext(), SubActivityScreen.class));
        mIntent.putExtra("mode", SubActivityScreen.NO_RESULT_MODE);
        runLaunchpad(LaunchpadActivity.LAUNCH);
    }
    public void testResult() throws Exception {
        mIntent.putExtra("component", new ComponentName(getContext(), SubActivityScreen.class));
        mIntent.putExtra("mode", SubActivityScreen.RESULT_MODE);
        runLaunchpad(LaunchpadActivity.LAUNCH);
    }
    public void testFinishSub() throws Exception {
        mIntent.putExtra("component",
                new ComponentName(getContext(), RemoteSubActivityScreen.class));
        mIntent.putExtra("mode", SubActivityScreen.FINISH_SUB_MODE);
        runLaunchpad(LaunchpadActivity.LAUNCH);
    }
    public void testRemoteNoResult() throws Exception {
        mIntent.putExtra("component",
                new ComponentName(getContext(), RemoteSubActivityScreen.class));
        mIntent.putExtra("mode", SubActivityScreen.NO_RESULT_MODE);
        runLaunchpad(LaunchpadActivity.LAUNCH);
    }
    public void testRemoteResult() throws Exception {
        mIntent.putExtra("component",
                new ComponentName(getContext(), RemoteSubActivityScreen.class));
        mIntent.putExtra("mode", SubActivityScreen.RESULT_MODE);
        runLaunchpad(LaunchpadActivity.LAUNCH);
    }
    public void testRemoteFinishSub() throws Exception {
        mIntent.putExtra("component", new ComponentName(getContext(), SubActivityScreen.class));
        mIntent.putExtra("mode", SubActivityScreen.FINISH_SUB_MODE);
        runLaunchpad(LaunchpadActivity.LAUNCH);
    }
    public void testRemoteRestartNoResult() throws Exception {
        mIntent.putExtra("component",
                new ComponentName(getContext(), RemoteSubActivityScreen.class));
        mIntent.putExtra("mode", SubActivityScreen.NO_RESULT_MODE);
        mIntent.putExtra("kill", true);
        runLaunchpad(LaunchpadActivity.LAUNCH);
    }
    public void testRemoteRestartResult() throws Exception {
        mIntent.putExtra("component",
                new ComponentName(getContext(), RemoteSubActivityScreen.class));
        mIntent.putExtra("mode", SubActivityScreen.RESULT_MODE);
        mIntent.putExtra("kill", true);
        runLaunchpad(LaunchpadActivity.LAUNCH);
    }
    public void testRemoteRestartFinishSub() throws Exception {
        mIntent.putExtra("component", new ComponentName(getContext(), SubActivityScreen.class));
        mIntent.putExtra("mode", SubActivityScreen.FINISH_SUB_MODE);
        mIntent.putExtra("kill", true);
        runLaunchpad(LaunchpadActivity.LAUNCH);
    }
}
