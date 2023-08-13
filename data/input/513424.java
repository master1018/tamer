class AndroidJUnitLaunchAction implements IAndroidLaunchAction {
    private final AndroidJUnitLaunchInfo mLaunchInfo;
    public AndroidJUnitLaunchAction(AndroidJUnitLaunchInfo launchInfo) {
        mLaunchInfo = launchInfo;
    }
    public boolean doLaunchAction(DelayedLaunchInfo info, IDevice device) {
        String msg = String.format(LaunchMessages.AndroidJUnitLaunchAction_LaunchInstr_2s,
                mLaunchInfo.getRunner(), device.getSerialNumber());
        AdtPlugin.printToConsole(info.getProject(), msg);
        try {
           mLaunchInfo.setDebugMode(info.isDebugMode());
           mLaunchInfo.setDevice(info.getDevice());
           JUnitLaunchDelegate junitDelegate = new JUnitLaunchDelegate(mLaunchInfo);
           final String mode = info.isDebugMode() ? ILaunchManager.DEBUG_MODE : 
               ILaunchManager.RUN_MODE; 
           junitDelegate.launch(info.getLaunch().getLaunchConfiguration(), mode, info.getLaunch(),
                   info.getMonitor());
        } catch (CoreException e) {
            AdtPlugin.printErrorToConsole(info.getProject(),
                    LaunchMessages.AndroidJUnitLaunchAction_LaunchFail);
        }
        return true;
    }
    public String getLaunchDescription() {
        return String.format(LaunchMessages.AndroidJUnitLaunchAction_LaunchDesc_s,
                mLaunchInfo.getRunner());
    }
    private static class JUnitLaunchDelegate extends JUnitLaunchConfigurationDelegate {
        private AndroidJUnitLaunchInfo mLaunchInfo;
        public JUnitLaunchDelegate(AndroidJUnitLaunchInfo launchInfo) {
            mLaunchInfo = launchInfo;
        }
        @Override
        public synchronized void launch(ILaunchConfiguration configuration, String mode,
                ILaunch launch, IProgressMonitor monitor) throws CoreException {
            super.launch(configuration, mode, launch, monitor);
        }
        @Override
        public String verifyMainTypeName(ILaunchConfiguration configuration) {
            return "com.android.ide.eclipse.adt.junit.internal.runner.RemoteAndroidTestRunner"; 
        }
        @Override
        public IVMRunner getVMRunner(ILaunchConfiguration configuration, String mode) {
            return new VMTestRunner(mLaunchInfo);
        }
        @Override
        public ILaunch getLaunch(ILaunchConfiguration configuration, String mode) {
            return mLaunchInfo.getLaunch();
        }     
    }
    private static class VMTestRunner implements IVMRunner {
        private final AndroidJUnitLaunchInfo mJUnitInfo;
        VMTestRunner(AndroidJUnitLaunchInfo info) {
            mJUnitInfo = info;
        }
        public void run(final VMRunnerConfiguration config, ILaunch launch,
                IProgressMonitor monitor) throws CoreException {
            TestRunnerProcess runnerProcess = 
                new TestRunnerProcess(config, mJUnitInfo);
            launch.addProcess(runnerProcess);
            runnerProcess.run();
        }
    }
    private static class TestRunnerProcess implements IProcess  {
        private final VMRunnerConfiguration mRunConfig;
        private final AndroidJUnitLaunchInfo mJUnitInfo;
        private RemoteAdtTestRunner mTestRunner = null;
        private boolean mIsTerminated = false;
        TestRunnerProcess(VMRunnerConfiguration runConfig, AndroidJUnitLaunchInfo info) {
            mRunConfig = runConfig;
            mJUnitInfo = info;
        }
        public String getAttribute(String key) {
            return null;
        }
        public int getExitValue() {
            return 0;
        }
        public String getLabel() {
            return mJUnitInfo.getLaunch().getLaunchMode();
        }
        public ILaunch getLaunch() {
            return mJUnitInfo.getLaunch();
        }
        public IStreamsProxy getStreamsProxy() {
            return null;
        }
        public void setAttribute(String key, String value) {
        }
        @SuppressWarnings("unchecked")
        public Object getAdapter(Class adapter) {
            return null;
        }
        public boolean canTerminate() {
            return true;
        }
        public boolean isTerminated() {
            return mIsTerminated;
        }
        public void terminate() {
            if (mTestRunner != null) {
                mTestRunner.terminate();
            }    
            mIsTerminated = true;
        } 
        public void run() {
            if (Display.getCurrent() != null) {
                AdtPlugin.log(IStatus.ERROR, "Adt test runner executed on UI thread");
                AdtPlugin.printErrorToConsole(mJUnitInfo.getProject(),
                        "Test launch failed due to internal error: Running tests on UI thread");
                terminate();
                return;
            }
            mTestRunner = new RemoteAdtTestRunner();
            mTestRunner.runTests(mRunConfig.getProgramArguments(), mJUnitInfo);
        }
    }
}
