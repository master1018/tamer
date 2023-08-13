public class LaunchConfigDelegate extends LaunchConfigurationDelegate {
    final static int INVALID_DEBUG_PORT = -1;
    public final static String ANDROID_LAUNCH_TYPE_ID =
        "com.android.ide.eclipse.adt.debug.LaunchConfigType"; 
    public static final String ATTR_TARGET_MODE = AdtPlugin.PLUGIN_ID + ".target"; 
    public static final TargetMode DEFAULT_TARGET_MODE = TargetMode.AUTO;
    public final static String ATTR_LAUNCH_ACTION = AdtPlugin.PLUGIN_ID + ".action"; 
    public final static int ACTION_DEFAULT = 0;
    public final static int ACTION_ACTIVITY = 1;
    public final static int ACTION_DO_NOTHING = 2;
    public final static int DEFAULT_LAUNCH_ACTION = ACTION_DEFAULT;
    public static final String ATTR_ACTIVITY = AdtPlugin.PLUGIN_ID + ".activity"; 
    public static final String ATTR_AVD_NAME = AdtPlugin.PLUGIN_ID + ".avd"; 
    public static final String ATTR_SPEED = AdtPlugin.PLUGIN_ID + ".speed"; 
    public static final int DEFAULT_SPEED = 0;
    public static final String ATTR_DELAY = AdtPlugin.PLUGIN_ID + ".delay"; 
    public static final int DEFAULT_DELAY = 0;
    public static final String ATTR_COMMANDLINE = AdtPlugin.PLUGIN_ID + ".commandline"; 
    public static final String ATTR_WIPE_DATA = AdtPlugin.PLUGIN_ID + ".wipedata"; 
    public static final boolean DEFAULT_WIPE_DATA = false;
    public static final String ATTR_NO_BOOT_ANIM = AdtPlugin.PLUGIN_ID + ".nobootanim"; 
    public static final boolean DEFAULT_NO_BOOT_ANIM = false;
    public static final String ATTR_DEBUG_PORT =
        AdtPlugin.PLUGIN_ID + ".debugPort"; 
    public void launch(ILaunchConfiguration configuration, String mode,
            ILaunch launch, IProgressMonitor monitor) throws CoreException {
        int debugPort = AndroidLaunchController.getPortForConfig(configuration);
        IProject project = getProject(configuration);
        AndroidLaunch androidLaunch = null;
        if (launch instanceof AndroidLaunch) {
            androidLaunch = (AndroidLaunch)launch;
        } else {
            AdtPlugin.printErrorToConsole(project, "Wrong Launch Type!");
            return;
        }
        if (debugPort != INVALID_DEBUG_PORT) {
            AndroidLaunchController.launchRemoteDebugger(debugPort, androidLaunch, monitor);
            return;
        }
        if (project == null) {
            AdtPlugin.printErrorToConsole("Couldn't get project object!");
            androidLaunch.stopLaunch();
            return;
        }
        if (ProjectHelper.hasError(project, true)) {
            AdtPlugin.displayError("Android Launch",
                    "Your project contains error(s), please fix them before running your application.");
            return;
        }
        AdtPlugin.printToConsole(project, "------------------------------"); 
        AdtPlugin.printToConsole(project, "Android Launch!");
        if (checkAndroidProject(project) == false) {
            AdtPlugin.printErrorToConsole(project, "Project is not an Android Project. Aborting!");
            androidLaunch.stopLaunch();
            return;
        }
        AndroidDebugBridge bridge = AndroidDebugBridge.getBridge();
        if (bridge == null || bridge.isConnected() == false) {
            try {
                int connections = -1;
                int restarts = -1;
                if (bridge != null) {
                    connections = bridge.getConnectionAttemptCount();
                    restarts = bridge.getRestartAttemptCount();
                }
                if (connections == -1 || restarts == -1) {
                    AdtPlugin.printErrorToConsole(project,
                            "The connection to adb is down, and a severe error has occured.",
                            "You must restart adb and Eclipse.",
                            String.format(
                                    "Please ensure that adb is correctly located at '%1$s' and can be executed.",
                                    AdtPlugin.getOsAbsoluteAdb()));
                    return;
                }
                if (restarts == 0) {
                    AdtPlugin.printErrorToConsole(project,
                            "Connection with adb was interrupted.",
                            String.format("%1$s attempts have been made to reconnect.", connections),
                            "You may want to manually restart adb from the Devices view.");
                } else {
                    AdtPlugin.printErrorToConsole(project,
                            "Connection with adb was interrupted, and attempts to reconnect have failed.",
                            String.format("%1$s attempts have been made to restart adb.", restarts),
                            "You may want to manually restart adb from the Devices view.");
                }
                return;
            } finally {
                androidLaunch.stopLaunch();
            }
        }
        AdtPlugin.printToConsole(project, "adb is running normally.");
        AndroidLaunchConfiguration config = new AndroidLaunchConfiguration();
        config.set(configuration);
        AndroidLaunchController controller = AndroidLaunchController.getInstance();
        IFile applicationPackage = ProjectHelper.getApplicationPackage(project);
        if (applicationPackage == null) {
            androidLaunch.stopLaunch();
            return;
        }
        AndroidManifestParser manifestParser = AndroidManifestParser.parse(
                BaseProjectHelper.getJavaProject(project), null ,
                true , false );
        if (manifestParser == null) {
            AdtPlugin.printErrorToConsole(project, "Failed to parse AndroidManifest: aborting!");
            androidLaunch.stopLaunch();
            return;
        }
        doLaunch(configuration, mode, monitor, project, androidLaunch, config, controller,
                applicationPackage, manifestParser);
    }
    protected void doLaunch(ILaunchConfiguration configuration, String mode,
            IProgressMonitor monitor, IProject project, AndroidLaunch androidLaunch,
            AndroidLaunchConfiguration config, AndroidLaunchController controller,
            IFile applicationPackage, AndroidManifestParser manifestParser) {
       String activityName = null;
        if (config.mLaunchAction == ACTION_ACTIVITY) {
            activityName = getActivityName(configuration);
            Activity[] activities = manifestParser.getActivities();
            if (activities.length == 0) {
                AdtPlugin.printErrorToConsole(project,
                        "The Manifest defines no activity!",
                        "The launch will only sync the application package on the device!");
                config.mLaunchAction = ACTION_DO_NOTHING;
            } else if (activityName == null) {
                AdtPlugin.printErrorToConsole(project,
                        "No activity specified! Getting the launcher activity.");
                Activity launcherActivity = manifestParser.getLauncherActivity();
                if (launcherActivity != null) {
                    activityName = launcherActivity.getName();
                }
                if (activityName == null) {
                    revertToNoActionLaunch(project, config);
                }
            } else {
                boolean match = false;
                for (Activity a : activities) {
                    if (a != null && a.getName().equals(activityName)) {
                        match = true;
                        break;
                    }
                }
                if (match == false) {
                    AdtPlugin.printErrorToConsole(project,
                            "The specified activity does not exist! Getting the launcher activity.");
                    Activity launcherActivity = manifestParser.getLauncherActivity();
                    if (launcherActivity != null) {
                        activityName = launcherActivity.getName();
                    } else {
                        revertToNoActionLaunch(project, config);
                    }
                }
            }
        } else if (config.mLaunchAction == ACTION_DEFAULT) {
            Activity launcherActivity = manifestParser.getLauncherActivity();
            if (launcherActivity != null) {
                activityName = launcherActivity.getName();
            }
            if (activityName == null) {
                revertToNoActionLaunch(project, config);
            }
        }
        IAndroidLaunchAction launchAction = null;
        if (config.mLaunchAction == ACTION_DO_NOTHING || activityName == null) {
            launchAction = new EmptyLaunchAction();
        } else {
            launchAction = new ActivityLaunchAction(activityName, controller);
        }
        controller.launch(project, mode, applicationPackage,manifestParser.getPackage(),
                manifestParser.getPackage(), manifestParser.getDebuggable(),
                manifestParser.getApiLevelRequirement(), launchAction, config, androidLaunch,
                monitor);
    }
    @Override
    public boolean buildForLaunch(ILaunchConfiguration configuration,
            String mode, IProgressMonitor monitor) throws CoreException {
        IProject project = getProject(configuration);
        if (project != null) {
            return true;
        }
        throw new CoreException(new Status(IStatus.ERROR, AdtPlugin.PLUGIN_ID,
                        1 , "Can't find the project!", null ));
    }
    @Override
    public ILaunch getLaunch(ILaunchConfiguration configuration, String mode)
            throws CoreException {
        return new AndroidLaunch(configuration, mode, null);
    }
    private IProject getProject(ILaunchConfiguration configuration){
        String projectName;
        try {
            projectName = configuration.getAttribute(
                    IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, "");
        } catch (CoreException e) {
            return null;
        }
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        return workspace.getRoot().getProject(projectName);
    }
    private boolean checkAndroidProject(IProject project) throws CoreException {
        if (project.hasNature(JavaCore.NATURE_ID) == false) {
            String msg = String.format("%1$s is not a Java project!", project.getName());
            AdtPlugin.displayError("Android Launch", msg);
            return false;
        }
        if (project.hasNature(AndroidConstants.NATURE) == false) {
            String msg = String.format("%1$s is not an Android project!", project.getName());
            AdtPlugin.displayError("Android Launch", msg);
            return false;
        }
        return true;
    }
    private String getActivityName(ILaunchConfiguration configuration) {
        String empty = "";
        String activityName;
        try {
            activityName = configuration.getAttribute(ATTR_ACTIVITY, empty);
        } catch (CoreException e) {
            return null;
        }
        return (activityName != empty) ? activityName : null;
    }
    private final void revertToNoActionLaunch(IProject project, AndroidLaunchConfiguration config) {
        AdtPlugin.printErrorToConsole(project,
                "No Launcher activity found!",
                "The launch will only sync the application package on the device!");
        config.mLaunchAction = ACTION_DO_NOTHING;
    }
}
