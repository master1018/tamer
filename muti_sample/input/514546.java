public class ProcessErrorsTest extends AndroidTestCase {
    private final String TAG = "ProcessErrorsTest";
    protected ActivityManager mActivityManager;
    @Override
    public void setUp() throws Exception {
        super.setUp();
        mActivityManager = (ActivityManager) 
                getContext().getSystemService(Context.ACTIVITY_SERVICE);
    }
    public void testSetUpConditions() throws Exception {
        assertNotNull(mActivityManager);
    }
    public void testNoProcessErrors() throws Exception {
        List<ActivityManager.ProcessErrorStateInfo> errList;        
        errList = mActivityManager.getProcessesInErrorState();
        final String reportMsg = reportListContents(errList);
        if (reportMsg != null) {
            Log.w(TAG, reportMsg);
        }
        assertNull(reportMsg, errList);
    }
    private String reportListContents(List<ActivityManager.ProcessErrorStateInfo> errList) {
        if (errList == null) return null;
        StringBuilder builder = new StringBuilder();
        Iterator<ActivityManager.ProcessErrorStateInfo> iter = errList.iterator();
        while (iter.hasNext()) {
            ActivityManager.ProcessErrorStateInfo entry = iter.next();
            String condition;
            switch (entry.condition) {
            case ActivityManager.ProcessErrorStateInfo.CRASHED:
                condition = "CRASHED";
                break;
            case ActivityManager.ProcessErrorStateInfo.NOT_RESPONDING:
                condition = "ANR";
                break;
            default:
                condition = "<unknown>";
                break;
            }
            builder.append("Process error ").append(condition).append(" ");
            builder.append(" ").append(entry.shortMsg);
            builder.append(" detected in ").append(entry.processName).append(" ").append(entry.tag);
        }
        return builder.toString();
    }
}
