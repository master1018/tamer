public class LifecycleTest extends ActivityTestsBase {
    private Intent mTopIntent;
    private Intent mTabIntent;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mTopIntent = mIntent;
        mTabIntent = new Intent(mContext, LaunchpadTabActivity.class);
        mTabIntent.putExtra("tab", new ComponentName(mContext,
                LaunchpadActivity.class));
    }
    @LargeTest
    public void testBasic() throws Exception {
        mIntent = mTopIntent;
        runLaunchpad(LaunchpadActivity.LIFECYCLE_BASIC);
    }
    @Suppress
    public void testTabBasic() throws Exception {
        mIntent = mTabIntent;
        runLaunchpad(LaunchpadActivity.LIFECYCLE_BASIC);
    }
    public void testScreen() throws Exception {
        mIntent = mTopIntent;
        runLaunchpad(LaunchpadActivity.LIFECYCLE_SCREEN);
    }
    @Suppress
    public void testTabScreen() throws Exception {
        mIntent = mTabIntent;
        runLaunchpad(LaunchpadActivity.LIFECYCLE_SCREEN);
    }
    public void testDialog() throws Exception {
        mIntent = mTopIntent;
        runLaunchpad(LaunchpadActivity.LIFECYCLE_DIALOG);
    }
    @Suppress
    public void testTabDialog() throws Exception {
        mIntent = mTabIntent;
        runLaunchpad(LaunchpadActivity.LIFECYCLE_DIALOG);
    }
    @MediumTest
    public void testFinishCreate() throws Exception {
        mIntent = mTopIntent;
        runLaunchpad(LaunchpadActivity.LIFECYCLE_FINISH_CREATE);
    }
    @Suppress
    public void testTabFinishCreate() throws Exception {
        mIntent = mTabIntent;
        runLaunchpad(LaunchpadActivity.LIFECYCLE_FINISH_CREATE);
    }
    @MediumTest
    public void testFinishStart() throws Exception {
        mIntent = mTopIntent;
        runLaunchpad(LaunchpadActivity.LIFECYCLE_FINISH_START);
    }
    @Suppress
    public void testTabFinishStart() throws Exception {
        mIntent = mTabIntent;
        runLaunchpad(LaunchpadActivity.LIFECYCLE_FINISH_START);
    }
}
