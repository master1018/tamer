public class StubProjectCreationPage extends NewProjectCreationPage {
    private final String mProjectName;
    private final String mLocation;
    private final IAndroidTarget mTarget;
    public StubProjectCreationPage(String projectName, String projectLocation, IAndroidTarget target) {
        super();
        this.mProjectName = projectName;
        this.mLocation = projectLocation;
        this.mTarget = target;
        setTestInfo(null);
    }
    @Override
    public IMainInfo getMainInfo() {
        return new IMainInfo() {
            public String getProjectName() {
                return mProjectName;
            }
            public String getPackageName() {
                return "com.android.samples";
            }
            public String getActivityName() {
                return mProjectName;
            }
            public String getApplicationName() {
                return mProjectName;
            }
            public boolean isNewProject() {
                return false;
            }
            public String getSourceFolder() {
                return "src";
            }
            public IPath getLocationPath() {
                return new Path(mLocation);
            }
            public String getMinSdkVersion() {
                return null;
            }
            public IAndroidTarget getSdkTarget() {
                return mTarget;
            }
            public boolean isCreateActivity() {
                return false;
            }
            public boolean useDefaultLocation() {
                return false;
            }
        };
    }
}
