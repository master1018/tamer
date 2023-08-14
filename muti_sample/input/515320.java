public class AMReceiver extends MultiLineReceiver {
    private static final int MAX_ATTEMPT_COUNT = 5;
    private static final Pattern sAmErrorType = Pattern.compile("Error type (\\d+)"); 
    private final DelayedLaunchInfo mLaunchInfo;
    private final IDevice mDevice;
    private final ILaunchController mLaunchController;
    public AMReceiver(DelayedLaunchInfo launchInfo, IDevice device, 
            ILaunchController launchController) {
        mLaunchInfo = launchInfo;
        mDevice = device;
        mLaunchController = launchController;
    }
    @Override
    public void processNewLines(String[] lines) {
        ArrayList<String> array = new ArrayList<String>();
        boolean error = false;
        boolean warning = false;
        for (String s : lines) {
            if (s.length() == 0) {
                continue;
            }
            if (mLaunchInfo.getAttemptCount() < MAX_ATTEMPT_COUNT &&
                    mLaunchInfo.isCancelled() == false) {
                Matcher m = sAmErrorType.matcher(s);
                if (m.matches()) {
                    int type = Integer.parseInt(m.group(1));
                    final int waitTime = 3;
                    String msg;
                    switch (type) {
                        case 1:
                        case 2:
                            msg = String.format(
                                    "Device not ready. Waiting %1$d seconds before next attempt.",
                                    waitTime);
                            break;
                        case 3:
                            msg = String.format(
                                    "New package not yet registered with the system. Waiting %1$d seconds before next attempt.",
                                    waitTime);
                            break;
                        default:
                            msg = String.format(
                                    "Device not ready (%2$d). Waiting %1$d seconds before next attempt.",
                                    waitTime, type);
                        break;
                    }
                    AdtPlugin.printToConsole(mLaunchInfo.getProject(), msg);
                    new Thread("Delayed Launch attempt") {
                        @Override
                        public void run() {
                            try {
                                sleep(waitTime * 1000);
                            } catch (InterruptedException e) {
                            }
                            mLaunchController.launchApp(mLaunchInfo, mDevice);
                        }
                    }.start();
                    return;
                }
            }
            if (error == false && s.startsWith("Error:")) { 
                error = true;
            }
            if (warning == false && s.startsWith("Warning:")) { 
                warning = true;
            }
            array.add("ActivityManager: " + s); 
        }
        if (warning || error) {
            AdtPlugin.printErrorToConsole(mLaunchInfo.getProject(), array.toArray());
        } else {
            AdtPlugin.printToConsole(mLaunchInfo.getProject(), array.toArray());
        }
        if (error) {
            mLaunchController.stopLaunch(mLaunchInfo);
        }
    }
    public boolean isCancelled() {
        return mLaunchInfo.isCancelled();
    }
}
