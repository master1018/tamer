public class FixLaunchConfig extends Thread {
    private IProject mProject;
    private String mOldPackage;
    private String mNewPackage;
    private boolean mDisplayPrompt = true;
    public FixLaunchConfig(IProject project, String oldPackage, String newPackage) {
        super();
        mProject = project;
        mOldPackage = oldPackage;
        mNewPackage = newPackage;
    }
    public void setDisplayPrompt(boolean displayPrompt) {
        mDisplayPrompt = displayPrompt;
    }
    @Override
    public void run() {
        if (mDisplayPrompt) {
            boolean res = AdtPlugin.displayPrompt(
                    "Launch Configuration Update",
                    "The package definition in the manifest changed.\nDo you want to update your Launch Configuration(s)?");
            if (res == false) {
                return;
            }
        }
        String projectName = mProject.getName();
        ILaunchConfiguration[] configs = findConfigs(mProject.getName());
        for (ILaunchConfiguration config : configs) {
            try {
                ILaunchConfigurationWorkingCopy copy = config.getWorkingCopy();
                String activity = config.getAttribute(LaunchConfigDelegate.ATTR_ACTIVITY,
                        ""); 
                if (activity.startsWith(mOldPackage)) {
                    activity = mNewPackage + activity.substring(mOldPackage.length());
                    copy.setAttribute(LaunchConfigDelegate.ATTR_ACTIVITY, activity);
                    copy.doSave();
                }
            } catch (CoreException e) {
                String msg = String.format("Failed to modify %1$s: %2$s", projectName,
                        e.getMessage());
                AdtPlugin.printErrorToConsole(mProject, msg);
            }
        }
    }
    private static ILaunchConfiguration[] findConfigs(String projectName) {
        ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
        ILaunchConfigurationType configType = manager.
                getLaunchConfigurationType(LaunchConfigDelegate.ANDROID_LAUNCH_TYPE_ID);
        ArrayList<ILaunchConfiguration> list = new ArrayList<ILaunchConfiguration>();
        try {
            ILaunchConfiguration[] configs = manager.getLaunchConfigurations(configType);
            for (ILaunchConfiguration config : configs) {
                if (config.getAttribute(
                        IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME,
                        "").equals(projectName)) {  
                    list.add(config);
                }
            }
        } catch (CoreException e) {
        }
        return list.toArray(new ILaunchConfiguration[list.size()]);
    }
}
