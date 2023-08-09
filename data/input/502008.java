public final class AndroidLaunchController implements IDebugBridgeChangeListener,
        IDeviceChangeListener, IClientChangeListener, ILaunchController {
    private static final String FLAG_AVD = "-avd"; 
    private static final String FLAG_NETDELAY = "-netdelay"; 
    private static final String FLAG_NETSPEED = "-netspeed"; 
    private static final String FLAG_WIPE_DATA = "-wipe-data"; 
    private static final String FLAG_NO_BOOT_ANIM = "-no-boot-anim"; 
    private static final HashMap<ILaunchConfiguration, Integer> sRunningAppMap =
        new HashMap<ILaunchConfiguration, Integer>();
    private static final Object sListLock = sRunningAppMap;
    private final ArrayList<DelayedLaunchInfo> mWaitingForEmulatorLaunches =
        new ArrayList<DelayedLaunchInfo>();
    private final ArrayList<DelayedLaunchInfo> mWaitingForReadyEmulatorList =
        new ArrayList<DelayedLaunchInfo>();
    private final ArrayList<DelayedLaunchInfo> mWaitingForDebuggerApplications =
        new ArrayList<DelayedLaunchInfo>();
    private final ArrayList<Client> mUnknownClientsWaitingForDebugger = new ArrayList<Client>();
    private static AndroidLaunchController sThis = new AndroidLaunchController();
    private AndroidLaunchController() {
        AndroidDebugBridge.addDebugBridgeChangeListener(this);
        AndroidDebugBridge.addDeviceChangeListener(this);
        AndroidDebugBridge.addClientChangeListener(this);
    }
    public static AndroidLaunchController getInstance() {
        return sThis;
    }
    public static void debugRunningApp(IProject project, int debugPort) {
        ILaunchConfiguration config = AndroidLaunchController.getLaunchConfig(project);
        if (config != null) {
            setPortLaunchConfigAssociation(config, debugPort);
            DebugUITools.launch(config, ILaunchManager.DEBUG_MODE);
        }
    }
    public static ILaunchConfiguration getLaunchConfig(IProject project) {
        ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
        ILaunchConfigurationType configType = manager.getLaunchConfigurationType(
                        LaunchConfigDelegate.ANDROID_LAUNCH_TYPE_ID);
        String name = project.getName();
        ILaunchConfiguration config = findConfig(manager, configType, name);
        if (config == null) {
            ILaunchConfigurationWorkingCopy wc = null;
            try {
                wc = configType.newInstance(null,
                        manager.generateUniqueLaunchConfigurationNameFrom(name));
                wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, name);
                wc.setAttribute(LaunchConfigDelegate.ATTR_LAUNCH_ACTION,
                        LaunchConfigDelegate.DEFAULT_LAUNCH_ACTION);
                wc.setAttribute(LaunchConfigDelegate.ATTR_TARGET_MODE,
                        LaunchConfigDelegate.DEFAULT_TARGET_MODE.getValue());
                wc.setAttribute(LaunchConfigDelegate.ATTR_AVD_NAME, (String) null);
                wc.setAttribute(LaunchConfigDelegate.ATTR_SPEED,
                        LaunchConfigDelegate.DEFAULT_SPEED);
                wc.setAttribute(LaunchConfigDelegate.ATTR_DELAY,
                        LaunchConfigDelegate.DEFAULT_DELAY);
                wc.setAttribute(LaunchConfigDelegate.ATTR_WIPE_DATA,
                        LaunchConfigDelegate.DEFAULT_WIPE_DATA);
                wc.setAttribute(LaunchConfigDelegate.ATTR_NO_BOOT_ANIM,
                        LaunchConfigDelegate.DEFAULT_NO_BOOT_ANIM);
                IPreferenceStore store = AdtPlugin.getDefault().getPreferenceStore();
                String emuOptions = store.getString(AdtPrefs.PREFS_EMU_OPTIONS);
                wc.setAttribute(LaunchConfigDelegate.ATTR_COMMANDLINE, emuOptions);
                wc.setMappedResources(getResourcesToMap(project));
                return wc.doSave();
            } catch (CoreException e) {
                String msg = String.format(
                        "Failed to create a Launch config for project '%1$s': %2$s",
                        project.getName(), e.getMessage());
                AdtPlugin.printErrorToConsole(project, msg);
                return null;
            }
        }
        return config;
    }
    public static IResource[] getResourcesToMap(IProject project) {
        ArrayList<IResource> array = new ArrayList<IResource>(2);
        array.add(project);
        IFile manifest = AndroidManifestParser.getManifest(project);
        if (manifest != null) {
            array.add(manifest);
        }
        return array.toArray(new IResource[array.size()]);
    }
    public void launch(final IProject project, String mode, IFile apk,
            String packageName, String debugPackageName, Boolean debuggable,
            String requiredApiVersionNumber, final IAndroidLaunchAction launchAction,
            final AndroidLaunchConfiguration config, final AndroidLaunch launch,
            IProgressMonitor monitor) {
        String message = String.format("Performing %1$s", launchAction.getLaunchDescription());
        AdtPlugin.printToConsole(project, message);
        final DelayedLaunchInfo launchInfo = new DelayedLaunchInfo(project, packageName,
                debugPackageName, launchAction, apk, debuggable, requiredApiVersionNumber, launch,
                monitor);
        launchInfo.setDebugMode(mode.equals(ILaunchManager.DEBUG_MODE));
        Sdk currentSdk = Sdk.getCurrent();
        AvdManager avdManager = currentSdk.getAvdManager();
        try {
            avdManager.reloadAvds(NullSdkLog.getLogger());
        } catch (AndroidLocationException e1) {
            config.mTargetMode = TargetMode.MANUAL;
        }
        final IAndroidTarget projectTarget = currentSdk.getTarget(project);
        final DeviceChooserResponse response = new DeviceChooserResponse();
        if (config.mTargetMode == TargetMode.AUTO) {
            IDevice[] devices = AndroidDebugBridge.getBridge().getDevices();
            AvdInfo preferredAvd = null;
            if (config.mAvdName != null) {
                preferredAvd = avdManager.getAvd(config.mAvdName, true );
                if (projectTarget.canRunOn(preferredAvd.getTarget()) == false) {
                    preferredAvd = null;
                    AdtPlugin.printErrorToConsole(project, String.format(
                            "Preferred AVD '%1$s' is not compatible with the project target '%2$s'. Looking for a compatible AVD...",
                            config.mAvdName, projectTarget.getName()));
                }
            }
            if (preferredAvd != null) {
                for (IDevice d : devices) {
                    String deviceAvd = d.getAvdName();
                    if (deviceAvd != null && deviceAvd.equals(config.mAvdName)) {
                        response.setDeviceToUse(d);
                        AdtPlugin.printToConsole(project, String.format(
                                "Automatic Target Mode: Preferred AVD '%1$s' is available on emulator '%2$s'",
                                config.mAvdName, d));
                        continueLaunch(response, project, launch, launchInfo, config);
                        return;
                    }
                }
                response.setAvdToLaunch(preferredAvd);
                AdtPlugin.printToConsole(project, String.format(
                        "Automatic Target Mode: Preferred AVD '%1$s' is not available. Launching new emulator.",
                        config.mAvdName));
                continueLaunch(response, project, launch, launchInfo, config);
                return;
            }
            HashMap<IDevice, AvdInfo> compatibleRunningAvds = new HashMap<IDevice, AvdInfo>();
            boolean hasDevice = false; 
            for (IDevice d : devices) {
                String deviceAvd = d.getAvdName();
                if (deviceAvd != null) { 
                    AvdInfo info = avdManager.getAvd(deviceAvd, true );
                    if (info != null && projectTarget.canRunOn(info.getTarget())) {
                        compatibleRunningAvds.put(d, info);
                    }
                } else {
                    if (projectTarget.isPlatform()) { 
                        AndroidVersion deviceVersion = Sdk.getDeviceVersion(d);
                        if (deviceVersion != null &&
                                deviceVersion.canRun(projectTarget.getVersion())) {
                            compatibleRunningAvds.put(d, null);
                            continue;
                        }
                    } else {
                    }
                    hasDevice = true;
                }
            }
            if (hasDevice == false && compatibleRunningAvds.size() == 0) {
                AvdInfo defaultAvd = findMatchingAvd(avdManager, projectTarget);
                if (defaultAvd != null) {
                    response.setAvdToLaunch(defaultAvd);
                    AdtPlugin.printToConsole(project, String.format(
                            "Automatic Target Mode: launching new emulator with compatible AVD '%1$s'",
                            defaultAvd.getName()));
                    continueLaunch(response, project, launch, launchInfo, config);
                    return;
                } else {
                    AdtPlugin.printToConsole(project, String.format(
                            "Failed to find an AVD compatible with target '%1$s'.",
                            projectTarget.getName()));
                    final Display display = AdtPlugin.getDisplay();
                    final boolean[] searchAgain = new boolean[] { false };
                    display.syncExec(new Runnable() {
                        public void run() {
                            Shell shell = display.getActiveShell();
                            if (MessageDialog.openQuestion(shell, "Android AVD Error",
                                    "No compatible targets were found. Do you wish to a add new Android Virtual Device?")) {
                                AvdManagerAction action = new AvdManagerAction();
                                action.run(null );
                                searchAgain[0] = true;
                            }
                        }
                    });
                    if (searchAgain[0]) {
                        defaultAvd = findMatchingAvd(avdManager, projectTarget);
                        if (defaultAvd == null) {
                            AdtPlugin.printErrorToConsole(project, String.format(
                                    "Still no compatible AVDs with target '%1$s': Aborting launch.",
                                    projectTarget.getName()));
                            stopLaunch(launchInfo);
                        } else {
                            response.setAvdToLaunch(defaultAvd);
                            AdtPlugin.printToConsole(project, String.format(
                                    "Launching new emulator with compatible AVD '%1$s'",
                                    defaultAvd.getName()));
                            continueLaunch(response, project, launch, launchInfo, config);
                            return;
                        }
                    }
                }
            } else if (hasDevice == false && compatibleRunningAvds.size() == 1) {
                Entry<IDevice, AvdInfo> e = compatibleRunningAvds.entrySet().iterator().next();
                response.setDeviceToUse(e.getKey());
                AvdInfo avdInfo = e.getValue();
                if (avdInfo != null) {
                    message = String.format("Automatic Target Mode: using existing emulator '%1$s' running compatible AVD '%2$s'",
                            response.getDeviceToUse(), e.getValue().getName());
                } else {
                    message = String.format("Automatic Target Mode: using device '%1$s'",
                            response.getDeviceToUse());
                }
                AdtPlugin.printToConsole(project, message);
                continueLaunch(response, project, launch, launchInfo, config);
                return;
            }
            if (compatibleRunningAvds.size() >= 2) {
                message = "Automatic Target Mode: Several compatible targets. Please select a target device.";
            } else if (hasDevice) {
                message = "Automatic Target Mode: Unable to detect device compatibility. Please select a target device.";
            }
            AdtPlugin.printToConsole(project, message);
        }
        AdtPlugin.getDisplay().asyncExec(new Runnable() {
            public void run() {
                try {
                    DeviceChooserDialog dialog = new DeviceChooserDialog(
                            AdtPlugin.getDisplay().getActiveShell(),
                            response, launchInfo.getPackageName(), projectTarget);
                    if (dialog.open() == Dialog.OK) {
                        AndroidLaunchController.this.continueLaunch(response, project, launch,
                                launchInfo, config);
                    } else {
                        AdtPlugin.printErrorToConsole(project, "Launch canceled!");
                        stopLaunch(launchInfo);
                        return;
                    }
                } catch (Exception e) {
                    String msg = e.getMessage();
                    if (msg == null) {
                        msg = e.getClass().getCanonicalName();
                    }
                    AdtPlugin.printErrorToConsole(project,
                            String.format("Error during launch: %s", msg));
                    stopLaunch(launchInfo);
                }
            }
        });
    }
    private AvdInfo findMatchingAvd(AvdManager avdManager, final IAndroidTarget projectTarget) {
        AvdInfo[] avds = avdManager.getValidAvds();
        AvdInfo defaultAvd = null;
        for (AvdInfo avd : avds) {
            if (projectTarget.canRunOn(avd.getTarget())) {
                if (defaultAvd == null ||
                        avd.getTarget().getVersion().getApiLevel() <
                            defaultAvd.getTarget().getVersion().getApiLevel()) {
                    defaultAvd = avd;
                }
            }
        }
        return defaultAvd;
    }
    void continueLaunch(final DeviceChooserResponse response, final IProject project,
            final AndroidLaunch launch, final DelayedLaunchInfo launchInfo,
            final AndroidLaunchConfiguration config) {
        new Thread() {
            @Override
            public void run() {
                if (response.getAvdToLaunch() != null) {
                    synchronized (sListLock) {
                        AvdInfo info = response.getAvdToLaunch();
                        mWaitingForEmulatorLaunches.add(launchInfo);
                        AdtPlugin.printToConsole(project, String.format(
                                "Launching a new emulator with Virtual Device '%1$s'",
                                info.getName()));
                        boolean status = launchEmulator(config, info);
                        if (status == false) {
                            AdtPlugin.displayError("Emulator Launch",
                                    "Couldn't launch the emulator! Make sure the SDK directory is properly setup and the emulator is not missing.");
                            mWaitingForEmulatorLaunches.remove(launchInfo);
                            AdtPlugin.printErrorToConsole(project, "Launch canceled!");
                            stopLaunch(launchInfo);
                            return;
                        }
                        return;
                    }
                } else if (response.getDeviceToUse() != null) {
                    launchInfo.setDevice(response.getDeviceToUse());
                    simpleLaunch(launchInfo, launchInfo.getDevice());
                }
            }
        }.start();
    }
    static int getPortForConfig(ILaunchConfiguration launchConfig) {
        synchronized (sListLock) {
            Integer port = sRunningAppMap.get(launchConfig);
            if (port != null) {
                sRunningAppMap.remove(launchConfig);
                return port;
            }
        }
        return LaunchConfigDelegate.INVALID_DEBUG_PORT;
    }
    private static void setPortLaunchConfigAssociation(ILaunchConfiguration launchConfig,
            int port) {
        synchronized (sListLock) {
            sRunningAppMap.put(launchConfig, port);
        }
    }
    private boolean checkBuildInfo(DelayedLaunchInfo launchInfo, IDevice device) {
        if (device != null) {
            String deviceVersion = device.getProperty(IDevice.PROP_BUILD_VERSION);
            String deviceApiLevelString = device.getProperty(IDevice.PROP_BUILD_API_LEVEL);
            String deviceCodeName = device.getProperty(IDevice.PROP_BUILD_CODENAME);
            int deviceApiLevel = -1;
            try {
                deviceApiLevel = Integer.parseInt(deviceApiLevelString);
            } catch (NumberFormatException e) {
            }
            String requiredApiString = launchInfo.getRequiredApiVersionNumber();
            if (requiredApiString != null) {
                int requiredApi = -1;
                try {
                    requiredApi = Integer.parseInt(requiredApiString);
                } catch (NumberFormatException e) {
                }
                if (requiredApi == -1) {
                    if (requiredApiString.equals(deviceCodeName) == false) {
                        AdtPlugin.printErrorToConsole(launchInfo.getProject(), String.format(
                            "ERROR: Application requires a device running '%1$s'!",
                            requiredApiString));
                        return false;
                    }
                } else {
                    if (deviceApiLevel == -1) {
                        AdtPlugin.printToConsole(launchInfo.getProject(),
                                "WARNING: Unknown device API version!");
                    } else if (deviceApiLevel < requiredApi) {
                        String msg = String.format(
                                "ERROR: Application requires API version %1$d. Device API version is %2$d (Android %3$s).",
                                requiredApi, deviceApiLevel, deviceVersion);
                        AdtPlugin.printErrorToConsole(launchInfo.getProject(), msg);
                        return false;
                    }
                }
            } else {
                AdtPlugin.printErrorToConsole(launchInfo.getProject(),
                        "WARNING: Application does not specify an API level requirement!");
                if (deviceApiLevel == -1) {
                    AdtPlugin.printErrorToConsole(launchInfo.getProject(),
                            "WARNING: Unknown device API version!");
                } else {
                    AdtPlugin.printErrorToConsole(launchInfo.getProject(), String.format(
                            "Device API version is %1$d (Android %2$s)", deviceApiLevel,
                            deviceVersion));
                }
            }
            if (device.isEmulator() == false && launchInfo.isDebugMode()) {
                String debuggableDevice = device.getProperty(IDevice.PROP_DEBUGGABLE);
                if (debuggableDevice != null && debuggableDevice.equals("0")) { 
                    if (launchInfo.getDebuggable() == null) {
                        String message1 = String.format(
                                "Device '%1$s' requires that applications explicitely declare themselves as debuggable in their manifest.",
                                device.getSerialNumber());
                        String message2 = String.format("Application '%1$s' does not have the attribute 'debuggable' set to TRUE in its manifest and cannot be debugged.",
                                launchInfo.getPackageName());
                        AdtPlugin.printErrorToConsole(launchInfo.getProject(), message1, message2);
                        launchInfo.setDebugMode(false);
                    } else if (launchInfo.getDebuggable() == Boolean.FALSE) {
                        String message = String.format("Application '%1$s' has its 'debuggable' attribute set to FALSE and cannot be debugged.",
                                launchInfo.getPackageName());
                        AdtPlugin.printErrorToConsole(launchInfo.getProject(), message);
                        launchInfo.setDebugMode(false);
                    }
                }
            }
        }
        return true;
    }
    private boolean simpleLaunch(DelayedLaunchInfo launchInfo, IDevice device) {
        if (checkBuildInfo(launchInfo, device) == false) {
            AdtPlugin.printErrorToConsole(launchInfo.getProject(), "Launch canceled!");
            stopLaunch(launchInfo);
            return false;
        }
        if (syncApp(launchInfo, device) == false) {
            AdtPlugin.printErrorToConsole(launchInfo.getProject(), "Launch canceled!");
            stopLaunch(launchInfo);
            return false;
        }
        launchApp(launchInfo, device);
        return true;
    }
    private boolean syncApp(DelayedLaunchInfo launchInfo, IDevice device) {
        boolean alreadyInstalled = ApkInstallManager.getInstance().isApplicationInstalled(
                launchInfo.getProject(), launchInfo.getPackageName(), device);
        if (alreadyInstalled) {
            AdtPlugin.printToConsole(launchInfo.getProject(),
            "Application already deployed. No need to reinstall.");
        } else {
            if (doSyncApp(launchInfo, device) == false) {
                return false;
            }
        }
        for (DelayedLaunchInfo dependentLaunchInfo : getDependenciesLaunchInfo(launchInfo)) {
            String msg = String.format("Project dependency found, installing: %s",
                    dependentLaunchInfo.getProject().getName());
            AdtPlugin.printToConsole(launchInfo.getProject(), msg);
            if (syncApp(dependentLaunchInfo, device) == false) {
                return false;
            }
        }
        return true;
    }
    private boolean doSyncApp(DelayedLaunchInfo launchInfo, IDevice device) {
        IPath path = launchInfo.getPackageFile().getLocation();
        String fileName = path.lastSegment();
        try {
            String message = String.format("Uploading %1$s onto device '%2$s'",
                    fileName, device.getSerialNumber());
            AdtPlugin.printToConsole(launchInfo.getProject(), message);
            String remotePackagePath = device.syncPackageToDevice(path.toOSString());
            boolean installResult = installPackage(launchInfo, remotePackagePath, device);
            device.removeRemotePackage(remotePackagePath);
            if (installResult) {
               ApkInstallManager.getInstance().registerInstallation(
                       launchInfo.getProject(), launchInfo.getPackageName(), device);
            }
            return installResult;
        }
        catch (IOException e) {
            String msg = String.format("Failed to upload %1$s on device '%2$s'", fileName,
                    device.getSerialNumber());
            AdtPlugin.printErrorToConsole(launchInfo.getProject(), msg, e);
        }
        return false;
    }
    public List<DelayedLaunchInfo> getDependenciesLaunchInfo(DelayedLaunchInfo launchInfo) {
        List<DelayedLaunchInfo> dependencies = new ArrayList<DelayedLaunchInfo>();
        IJavaProject javaProject;
        try {
            javaProject = BaseProjectHelper.getJavaProject(launchInfo.getProject());
        } catch (CoreException e) {
            AdtPlugin.printErrorToConsole(launchInfo.getProject(), e);
            return dependencies;
        }
        List<IJavaProject> androidProjectList;
        try {
            androidProjectList = ProjectHelper.getAndroidProjectDependencies(javaProject);
        } catch (JavaModelException e) {
            AdtPlugin.printErrorToConsole(launchInfo.getProject(), e);
            return dependencies;
        }
        for (IJavaProject androidProject : androidProjectList) {
            AndroidManifestParser manifestParser;
            try {
                manifestParser = AndroidManifestParser.parse(
                        androidProject, null ,
                        true , false );
            } catch (CoreException e) {
                AdtPlugin.printErrorToConsole(
                        launchInfo.getProject(),
                        String.format("Error parsing manifest of %s",
                                androidProject.getElementName()));
                continue;
            }
            IFile apk = ProjectHelper.getApplicationPackage(androidProject.getProject());
            if (apk == null) {
                continue;
            }
            DelayedLaunchInfo delayedLaunchInfo = new DelayedLaunchInfo(
                    androidProject.getProject(),
                    manifestParser.getPackage(),
                    manifestParser.getPackage(),
                    launchInfo.getLaunchAction(),
                    apk,
                    manifestParser.getDebuggable(),
                    manifestParser.getApiLevelRequirement(),
                    launchInfo.getLaunch(),
                    launchInfo.getMonitor());
            dependencies.add(delayedLaunchInfo);
        }
        return dependencies;
    }
    private boolean installPackage(DelayedLaunchInfo launchInfo, final String remotePath,
            final IDevice device) {
        String message = String.format("Installing %1$s...", launchInfo.getPackageFile().getName());
        AdtPlugin.printToConsole(launchInfo.getProject(), message);
        try {
            String result = doInstall(launchInfo, remotePath, device, true );
            return checkInstallResult(result, device, launchInfo, remotePath,
                    InstallRetryMode.ALWAYS);
        } catch (IOException e) {
            String msg = String.format(
                    "Failed to install %1$s on device '%2$s!",
                    launchInfo.getPackageFile().getName(), device.getSerialNumber());
            AdtPlugin.printErrorToConsole(launchInfo.getProject(), msg, e.getMessage());
        }
        return false;
    }
    private boolean checkInstallResult(String result, IDevice device, DelayedLaunchInfo launchInfo,
            String remotePath, InstallRetryMode retryMode) throws IOException {
        if (result == null) {
            AdtPlugin.printToConsole(launchInfo.getProject(), "Success!");
            return true;
        }
        else if (result.equals("INSTALL_FAILED_ALREADY_EXISTS")) { 
            if (retryMode == InstallRetryMode.PROMPT) {
                boolean prompt = AdtPlugin.displayPrompt("Application Install",
                        "A previous installation needs to be uninstalled before the new package can be installed.\nDo you want to uninstall?");
                if (prompt) {
                    retryMode = InstallRetryMode.ALWAYS;
                } else {
                    AdtPlugin.printErrorToConsole(launchInfo.getProject(),
                        "Installation error! The package already exists.");
                    return false;
                }
            }
            if (retryMode == InstallRetryMode.ALWAYS) {
                AdtPlugin.printToConsole(launchInfo.getProject(),
                        "Application already exists. Attempting to re-install instead...");
                String res = doInstall(launchInfo, remotePath, device, true  );
                return checkInstallResult(res, device, launchInfo, remotePath,
                        InstallRetryMode.NEVER);
            }
            AdtPlugin.printErrorToConsole(launchInfo.getProject(),
                    "Installation error! The package already exists.");
        } else if (result.equals("INSTALL_FAILED_INVALID_APK")) { 
            AdtPlugin.printErrorToConsole(launchInfo.getProject(),
                "Installation failed due to invalid APK file!",
                "Please check logcat output for more details.");
        } else if (result.equals("INSTALL_FAILED_INVALID_URI")) { 
            AdtPlugin.printErrorToConsole(launchInfo.getProject(),
                "Installation failed due to invalid URI!",
                "Please check logcat output for more details.");
        } else if (result.equals("INSTALL_FAILED_COULDNT_COPY")) { 
            AdtPlugin.printErrorToConsole(launchInfo.getProject(),
                String.format("Installation failed: Could not copy %1$s to its final location!",
                        launchInfo.getPackageFile().getName()),
                "Please check logcat output for more details.");
        } else if (result.equals("INSTALL_PARSE_FAILED_INCONSISTENT_CERTIFICATES")) {
            AdtPlugin.printErrorToConsole(launchInfo.getProject(),
                    "Re-installation failed due to different application signatures.",
                    "You must perform a full uninstall of the application. WARNING: This will remove the application data!",
                    String.format("Please execute 'adb uninstall %1$s' in a shell.", launchInfo.getPackageName()));
        } else {
            AdtPlugin.printErrorToConsole(launchInfo.getProject(),
                String.format("Installation error: %1$s", result),
                "Please check logcat output for more details.");
        }
        return false;
    }
    @SuppressWarnings("unused")
    private String doUninstall(IDevice device, DelayedLaunchInfo launchInfo) throws IOException {
        try {
            return device.uninstallPackage(launchInfo.getPackageName());
        } catch (IOException e) {
            String msg = String.format(
                    "Failed to uninstall %1$s: %2$s", launchInfo.getPackageName(), e.getMessage());
            AdtPlugin.printErrorToConsole(launchInfo.getProject(), msg);
            throw e;
        }
    }
    private String doInstall(DelayedLaunchInfo launchInfo, final String remotePath,
            final IDevice device, boolean reinstall) throws IOException {
        return device.installRemotePackage(remotePath, reinstall);
    }
    public void launchApp(final DelayedLaunchInfo info, IDevice device) {
        if (info.isDebugMode()) {
            synchronized (sListLock) {
                if (mWaitingForDebuggerApplications.contains(info) == false) {
                    mWaitingForDebuggerApplications.add(info);
                }
            }
        }
        if (info.getLaunchAction().doLaunchAction(info, device)) {
            if (info.isDebugMode() == false) {
                stopLaunch(info);
            }
        } else {
            stopLaunch(info);
        }
    }
    private boolean launchEmulator(AndroidLaunchConfiguration config, AvdInfo avdToLaunch) {
        ArrayList<String> customArgs = new ArrayList<String>();
        boolean hasWipeData = false;
        if (config.mEmulatorCommandLine != null && config.mEmulatorCommandLine.length() > 0) {
            String[] segments = config.mEmulatorCommandLine.split("\\s+"); 
            for (String s : segments) {
                if (s.length() > 0) {
                    customArgs.add(s);
                    if (!hasWipeData && s.equals(FLAG_WIPE_DATA)) {
                        hasWipeData = true;
                    }
                }
            }
        }
        boolean needsWipeData = config.mWipeData && !hasWipeData;
        if (needsWipeData) {
            if (!AdtPlugin.displayPrompt("Android Launch", "Are you sure you want to wipe all user data when starting this emulator?")) {
                needsWipeData = false;
            }
        }
        ArrayList<String> list = new ArrayList<String>();
        list.add(AdtPlugin.getOsAbsoluteEmulator());
        list.add(FLAG_AVD);
        list.add(avdToLaunch.getName());
        if (config.mNetworkSpeed != null) {
            list.add(FLAG_NETSPEED);
            list.add(config.mNetworkSpeed);
        }
        if (config.mNetworkDelay != null) {
            list.add(FLAG_NETDELAY);
            list.add(config.mNetworkDelay);
        }
        if (needsWipeData) {
            list.add(FLAG_WIPE_DATA);
        }
        if (config.mNoBootAnim) {
            list.add(FLAG_NO_BOOT_ANIM);
        }
        list.addAll(customArgs);
        String[] command = list.toArray(new String[list.size()]);
        try {
            Process process = Runtime.getRuntime().exec(command);
            grabEmulatorOutput(process);
        } catch (IOException e) {
            return false;
        }
        return true;
    }
    private static ILaunchConfiguration findConfig(ILaunchManager manager,
            ILaunchConfigurationType type, String projectName) {
        try {
            ILaunchConfiguration[] configs = manager.getLaunchConfigurations(type);
            for (ILaunchConfiguration config : configs) {
                if (config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME,
                        "").equals(projectName)) {  
                    return config;
                }
            }
        } catch (CoreException e) {
            MessageDialog.openError(AdtPlugin.getDisplay().getActiveShell(),
                    "Launch Error", e.getStatus().getMessage());
        }
        return null;
    }
    @SuppressWarnings("deprecation")
    public static boolean connectRemoteDebugger(int debugPort,
            AndroidLaunch launch, IProgressMonitor monitor)
                throws CoreException {
        int connectTimeout = JavaRuntime.getPreferences().getInt(JavaRuntime.PREF_CONNECT_TIMEOUT);
        HashMap<String, String> newMap = new HashMap<String, String>();
        newMap.put("hostname", "localhost");  
        newMap.put("port", Integer.toString(debugPort)); 
        newMap.put("timeout", Integer.toString(connectTimeout));
        IVMConnector connector = JavaRuntime.getDefaultVMConnector();
        connector.connect(newMap, monitor, launch);
        if (monitor.isCanceled()) {
            IDebugTarget[] debugTargets = launch.getDebugTargets();
            for (IDebugTarget target : debugTargets) {
                if (target.canDisconnect()) {
                    target.disconnect();
                }
            }
            return false;
        }
        return true;
    }
    public static void launchRemoteDebugger(final int debugPort, final AndroidLaunch androidLaunch,
            final IProgressMonitor monitor) {
        new Thread("Debugger connection") { 
            @Override
            public void run() {
                try {
                    connectRemoteDebugger(debugPort, androidLaunch, monitor);
                } catch (CoreException e) {
                    androidLaunch.stopLaunch();
                }
                monitor.done();
            }
        }.start();
    }
    public void bridgeChanged(AndroidDebugBridge bridge) {
        String message = "adb server change: cancelling '%1$s'!";
        synchronized (sListLock) {
            for (DelayedLaunchInfo launchInfo : mWaitingForReadyEmulatorList) {
                AdtPlugin.printErrorToConsole(launchInfo.getProject(),
                    String.format(message, launchInfo.getLaunchAction().getLaunchDescription()));
                stopLaunch(launchInfo);
            }
            for (DelayedLaunchInfo launchInfo : mWaitingForDebuggerApplications) {
                AdtPlugin.printErrorToConsole(launchInfo.getProject(),
                        String.format(message,
                                launchInfo.getLaunchAction().getLaunchDescription()));
                stopLaunch(launchInfo);
            }
            mWaitingForReadyEmulatorList.clear();
            mWaitingForDebuggerApplications.clear();
        }
    }
    public void deviceConnected(IDevice device) {
        synchronized (sListLock) {
            if (mWaitingForEmulatorLaunches.size() > 0) {
                DelayedLaunchInfo launchInfo = mWaitingForEmulatorLaunches.get(0);
                mWaitingForEmulatorLaunches.remove(0);
                launchInfo.setDevice(device);
                mWaitingForReadyEmulatorList.add(launchInfo);
                AdtPlugin.printToConsole(launchInfo.getProject(),
                        String.format("New emulator found: %1$s", device.getSerialNumber()));
                AdtPlugin.printToConsole(launchInfo.getProject(),
                        String.format("Waiting for HOME ('%1$s') to be launched...",
                            AdtPlugin.getDefault().getPreferenceStore().getString(
                                    AdtPrefs.PREFS_HOME_PACKAGE)));
            }
        }
    }
    @SuppressWarnings("unchecked")
    public void deviceDisconnected(IDevice device) {
        String message = "%1$s disconnected! Cancelling '%2$s'!";
        synchronized (sListLock) {
            ArrayList<DelayedLaunchInfo> copyList =
                (ArrayList<DelayedLaunchInfo>) mWaitingForReadyEmulatorList.clone();
            for (DelayedLaunchInfo launchInfo : copyList) {
                if (launchInfo.getDevice() == device) {
                    AdtPlugin.printErrorToConsole(launchInfo.getProject(),
                            String.format(message, device.getSerialNumber(),
                                    launchInfo.getLaunchAction().getLaunchDescription()));
                    stopLaunch(launchInfo);
                }
            }
            copyList = (ArrayList<DelayedLaunchInfo>) mWaitingForDebuggerApplications.clone();
            for (DelayedLaunchInfo launchInfo : copyList) {
                if (launchInfo.getDevice() == device) {
                    AdtPlugin.printErrorToConsole(launchInfo.getProject(),
                            String.format(message, device.getSerialNumber(),
                                    launchInfo.getLaunchAction().getLaunchDescription()));
                    stopLaunch(launchInfo);
                }
            }
        }
    }
    public void deviceChanged(IDevice device, int changeMask) {
    }
    public void clientChanged(final Client client, int changeMask) {
        boolean connectDebugger = false;
        if ((changeMask & Client.CHANGE_NAME) == Client.CHANGE_NAME) {
            String applicationName = client.getClientData().getClientDescription();
            if (applicationName != null) {
                IPreferenceStore store = AdtPlugin.getDefault().getPreferenceStore();
                String home = store.getString(AdtPrefs.PREFS_HOME_PACKAGE);
                if (home.equals(applicationName)) {
                    IDevice device = client.getDevice();
                    synchronized (sListLock) {
                        for (int i = 0; i < mWaitingForReadyEmulatorList.size(); ) {
                            DelayedLaunchInfo launchInfo = mWaitingForReadyEmulatorList.get(i);
                            if (launchInfo.getDevice() == device) {
                                mWaitingForReadyEmulatorList.remove(i);
                                if (checkBuildInfo(launchInfo, device) == false) {
                                    AdtPlugin.printErrorToConsole(launchInfo.getProject(),
                                            "Launch canceled!");
                                    stopLaunch(launchInfo);
                                    return;
                                }
                                AdtPlugin.printToConsole(launchInfo.getProject(),
                                        String.format("HOME is up on device '%1$s'",
                                                device.getSerialNumber()));
                                if (syncApp(launchInfo, device)) {
                                    launchApp(launchInfo, device);
                                } else {
                                    AdtPlugin.printErrorToConsole(launchInfo.getProject(),
                                    "Launch canceled!");
                                    stopLaunch(launchInfo);
                                }
                                break;
                            } else {
                                i++;
                            }
                        }
                    }
                }
                if (client.getClientData().getDebuggerConnectionStatus() == DebuggerStatus.WAITING) {
                    synchronized (sListLock) {
                        int index = mUnknownClientsWaitingForDebugger.indexOf(client);
                        if (index != -1) {
                            connectDebugger = true;
                            mUnknownClientsWaitingForDebugger.remove(client);
                        }
                    }
                }
            }
        }
        if ((changeMask & Client.CHANGE_DEBUGGER_STATUS) == Client.CHANGE_DEBUGGER_STATUS) {
            ClientData clientData = client.getClientData();
            String applicationName = client.getClientData().getClientDescription();
            if (clientData.getDebuggerConnectionStatus() == DebuggerStatus.WAITING) {
                if (applicationName == null) {
                    synchronized (sListLock) {
                        mUnknownClientsWaitingForDebugger.add(client);
                    }
                    return;
                } else {
                    connectDebugger = true;
                }
            }
        }
        if (connectDebugger) {
            Log.d("adt", "Debugging " + client);
            String applicationName = client.getClientData().getClientDescription();
            Log.d("adt", "App Name: " + applicationName);
            synchronized (sListLock) {
                for (int i = 0; i < mWaitingForDebuggerApplications.size(); ) {
                    final DelayedLaunchInfo launchInfo = mWaitingForDebuggerApplications.get(i);
                    if (client.getDevice() == launchInfo.getDevice() &&
                            applicationName.equals(launchInfo.getDebugPackageName())) {
                        mWaitingForDebuggerApplications.remove(i);
                        String msg = String.format(
                                "Attempting to connect debugger to '%1$s' on port %2$d",
                                launchInfo.getDebugPackageName(), client.getDebuggerListenPort());
                        AdtPlugin.printToConsole(launchInfo.getProject(), msg);
                        new Thread("Debugger Connection") { 
                            @Override
                            public void run() {
                                try {
                                    if (connectRemoteDebugger(
                                            client.getDebuggerListenPort(),
                                            launchInfo.getLaunch(),
                                            launchInfo.getMonitor()) == false) {
                                        return;
                                    }
                                } catch (CoreException e) {
                                    AdtPlugin.printErrorToConsole(launchInfo.getProject(),
                                            String.format("Launch error: %s", e.getMessage()));
                                    stopLaunch(launchInfo);
                                }
                                launchInfo.getMonitor().done();
                            }
                        }.start();
                        return;
                    } else {
                        i++;
                    }
                }
            }
            IProject project = ProjectHelper.findAndroidProjectByAppName(applicationName);
            if (project != null) {
                debugRunningApp(project, client.getDebuggerListenPort());
            }
        }
    }
    private void grabEmulatorOutput(final Process process) {
        new Thread("") { 
            @Override
            public void run() {
                InputStreamReader is = new InputStreamReader(process.getErrorStream());
                BufferedReader errReader = new BufferedReader(is);
                try {
                    while (true) {
                        String line = errReader.readLine();
                        if (line != null) {
                            AdtPlugin.printErrorToConsole("Emulator", line);
                        } else {
                            break;
                        }
                    }
                } catch (IOException e) {
                }
            }
        }.start();
        new Thread("") { 
            @Override
            public void run() {
                InputStreamReader is = new InputStreamReader(process.getInputStream());
                BufferedReader outReader = new BufferedReader(is);
                try {
                    while (true) {
                        String line = outReader.readLine();
                        if (line != null) {
                            AdtPlugin.printToConsole("Emulator", line);
                        } else {
                            break;
                        }
                    }
                } catch (IOException e) {
                }
            }
        }.start();
    }
    public void stopLaunch(DelayedLaunchInfo launchInfo) {
        launchInfo.getLaunch().stopLaunch();
        synchronized (sListLock) {
            mWaitingForReadyEmulatorList.remove(launchInfo);
            mWaitingForDebuggerApplications.remove(launchInfo);
        }
    }
}
