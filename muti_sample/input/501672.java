public class ActivityManagerTest extends AndroidTestCase {
    protected Context mContext;
    protected ActivityManager mActivityManager;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mContext = getContext();
        mActivityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
    }
    @Suppress
    public void disabledTestErrorTasksEmpty() throws Exception {
        List<ActivityManager.ProcessErrorStateInfo> errList;
        errList = mActivityManager.getProcessesInErrorState();
        assertNull(errList);
    }
    @SmallTest
    public void testErrorTasksWithError() throws Exception {
        List<ActivityManager.ProcessErrorStateInfo> errList;
        errList = mActivityManager.getProcessesInErrorState();
        checkErrorListSanity(errList);
    }
    @SmallTest
    public void testErrorTasksWithANR() throws Exception {
        List<ActivityManager.ProcessErrorStateInfo> errList;
        errList = mActivityManager.getProcessesInErrorState();
        checkErrorListSanity(errList);
    }
    @SmallTest
    public void testGetDeviceConfigurationInfo() throws Exception {
        ConfigurationInfo config = mActivityManager.getDeviceConfigurationInfo();
        assertNotNull(config);
        Configuration vconfig = mContext.getResources().getConfiguration();
        assertNotNull(vconfig);
        assertEquals(config.reqKeyboardType, vconfig.keyboard);
        assertEquals(config.reqTouchScreen, vconfig.touchscreen);
        assertEquals(config.reqNavigation, vconfig.navigation);
        if (vconfig.navigation == Configuration.NAVIGATION_NONAV) {
            assertNotNull(config.reqInputFeatures & ConfigurationInfo.INPUT_FEATURE_FIVE_WAY_NAV);
        }
        if (vconfig.keyboard != Configuration.KEYBOARD_UNDEFINED) {
            assertNotNull(config.reqInputFeatures & ConfigurationInfo.INPUT_FEATURE_HARD_KEYBOARD);
        }    
    }
    private void checkErrorListSanity(List<ActivityManager.ProcessErrorStateInfo> errList) {
        if (errList == null) return;
        Iterator<ActivityManager.ProcessErrorStateInfo> iter = errList.iterator();
        while (iter.hasNext()) {
            ActivityManager.ProcessErrorStateInfo info = iter.next();
            assertNotNull(info);
            assertTrue((info.condition == ActivityManager.ProcessErrorStateInfo.CRASHED) ||
                       (info.condition == ActivityManager.ProcessErrorStateInfo.NOT_RESPONDING));
            assertNotNull(info.processName);
            assertNotNull(info.longMsg);
            assertNotNull(info.shortMsg);
        }
    }
}
