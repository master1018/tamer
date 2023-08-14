public class ActivityLaunchAction implements IAndroidLaunchAction {
    private final String mActivity;
    private final ILaunchController mLaunchController;
    public ActivityLaunchAction(String activity, ILaunchController controller) {
        mActivity = activity;
        mLaunchController = controller;
    }
    public boolean doLaunchAction(DelayedLaunchInfo info, IDevice device) {
        try {
            String msg = String.format("Starting activity %1$s on device ", mActivity,
                    device);
            AdtPlugin.printToConsole(info.getProject(), msg);
            info.incrementAttemptCount();
            device.executeShellCommand("am start" 
                    + (info.isDebugMode() ? " -D" 
                            : "") 
                    + " -n " 
                    + info.getPackageName() + "/" 
                    + mActivity.replaceAll("\\$", "\\\\\\$") 
                    + " -a android.intent.action.MAIN"  
                    + " -c android.intent.category.LAUNCHER",  
                    new AMReceiver(info, device, mLaunchController));
            if (info.isDebugMode() == false) {
                return false;
            }
        } catch (IOException e) {
            AdtPlugin.printErrorToConsole(info.getProject(),
                    String.format("Launch error: %s", e.getMessage()));
            return false;
        }
        return true;
    }
    public String getLaunchDescription() {
       return String.format("%1$s activity launch", mActivity);
    }
}
