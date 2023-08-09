public class AndroidJUnitLaunchInfo {
    private final IProject mProject;
    private final String mAppPackage;
    private final String mRunner;
    private boolean mDebugMode = false;
    private IDevice mDevice = null;
    private String mTestPackage = null;
    private String mTestClass = null;
    private String mTestMethod = null;
    private ILaunch mLaunch = null;
    public AndroidJUnitLaunchInfo(IProject project, String appPackage, String runner) {
        mProject = project;
        mAppPackage = appPackage;
        mRunner = runner;
    }
    public IProject getProject() {
        return mProject;
    }
    public String getAppPackage() {
        return mAppPackage;
    }
    public String getRunner() {
        return mRunner;
    }
    public boolean isDebugMode() {
        return mDebugMode;
    }
    public void setDebugMode(boolean debugMode) {
        mDebugMode = debugMode;
    }
    public IDevice getDevice() {
        return mDevice;
    }
    public void setDevice(IDevice device) {
        mDevice = device;
    }
    public void setTestPackage(String testPackage) {
        mTestPackage = testPackage;
    }
    public String getTestPackage() {
        return mTestPackage;       
    }
    public void setTestClass(String testClass) {
        mTestClass = testClass;
    }
    public String getTestClass() {
        return mTestClass;
    }
    public void setTestMethod(String testMethod) {
        mTestMethod = testMethod;
    }
    public String getTestMethod() {
        return mTestMethod;
    }
    public ILaunch getLaunch() {
        return mLaunch;
    }
    public void setLaunch(ILaunch launch) {
        mLaunch = launch;
    }
}
