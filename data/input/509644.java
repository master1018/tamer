public final class DelayedLaunchInfo {
    enum InstallRetryMode {
        NEVER, ALWAYS, PROMPT;
    }
    private IDevice mDevice = null;
    private final IProject mProject;
    private final String mPackageName;
    private final String mDebugPackageName;
    private final IFile mPackageFile;
    private final Boolean mDebuggable;
    private final String mRequiredApiVersionNumber;
    private InstallRetryMode mRetryMode = InstallRetryMode.NEVER;
    private final IAndroidLaunchAction mLaunchAction;
    private final AndroidLaunch mLaunch;
    private final IProgressMonitor mMonitor;
    private boolean mDebugMode;
    private int mAttemptCount = 0;
    private boolean mCancelled = false;
    public DelayedLaunchInfo(IProject project, String packageName, String debugPackageName,
            IAndroidLaunchAction launchAction, IFile pack, Boolean debuggable,
            String requiredApiVersionNumber, AndroidLaunch launch, IProgressMonitor monitor) {
        mProject = project;
        mPackageName = packageName;
        mDebugPackageName = debugPackageName;
        mPackageFile = pack;
        mLaunchAction = launchAction;
        mLaunch = launch;
        mMonitor = monitor;
        mDebuggable = debuggable;
        mRequiredApiVersionNumber = requiredApiVersionNumber;
    }
    public IDevice getDevice() {
        return mDevice;
    }
    public void setDevice(IDevice device) {
        mDevice = device;
    }
    public IProject getProject() {
        return mProject;
    }
    public String getPackageName() {
        return mPackageName;
    }
    public String getDebugPackageName() {
        if (mDebugPackageName == null) {
            return getPackageName();
        }
        return mDebugPackageName;
    }
    public IFile getPackageFile() {
        return mPackageFile;
    }
    public Boolean getDebuggable() {
        return mDebuggable;
    }
    public String getRequiredApiVersionNumber() {
        return mRequiredApiVersionNumber;
    }
    public void setRetryMode(InstallRetryMode retryMode) {
        this.mRetryMode = retryMode;
    }
    public InstallRetryMode getRetryMode() {
        return mRetryMode;
    }
    public IAndroidLaunchAction getLaunchAction() {
        return mLaunchAction;
    }
    public AndroidLaunch getLaunch() {
        return mLaunch;
    }
    public IProgressMonitor getMonitor() {
        return mMonitor;
    }
    public void setDebugMode(boolean debugMode) {
        this.mDebugMode = debugMode;
    }
    public boolean isDebugMode() {
        return mDebugMode;
    }
    public void incrementAttemptCount() {
        mAttemptCount++;
    }
    public int getAttemptCount() {
        return mAttemptCount;
    }
    public void setCancelled(boolean cancelled) {
        this.mCancelled = cancelled;
    }
    public boolean isCancelled() {
        return mCancelled;
    }
}
