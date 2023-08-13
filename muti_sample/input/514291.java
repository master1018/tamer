class UpdaterData {
    private String mOsSdkRoot;
    private final ISdkLog mSdkLog;
    private ITaskFactory mTaskFactory;
    private boolean mUserCanChangeSdkRoot;
    private SdkManager mSdkManager;
    private AvdManager mAvdManager;
    private final LocalSdkParser mLocalSdkParser = new LocalSdkParser();
    private final RepoSources mSources = new RepoSources();
    private final LocalSdkAdapter mLocalSdkAdapter = new LocalSdkAdapter(this);
    private final RepoSourcesAdapter mSourcesAdapter = new RepoSourcesAdapter(this);
    private ImageFactory mImageFactory;
    private final SettingsController mSettingsController;
    private final ArrayList<ISdkListener> mListeners = new ArrayList<ISdkListener>();
    private Shell mWindowShell;
    private AndroidLocationException mAvdManagerInitError;
    public UpdaterData(String osSdkRoot, ISdkLog sdkLog) {
        mOsSdkRoot = osSdkRoot;
        mSdkLog = sdkLog;
        mSettingsController = new SettingsController(this);
        initSdk();
    }
    public String getOsSdkRoot() {
        return mOsSdkRoot;
    }
    public void setTaskFactory(ITaskFactory taskFactory) {
        mTaskFactory = taskFactory;
    }
    public ITaskFactory getTaskFactory() {
        return mTaskFactory;
    }
    public void setUserCanChangeSdkRoot(boolean userCanChangeSdkRoot) {
        mUserCanChangeSdkRoot = userCanChangeSdkRoot;
    }
    public boolean canUserChangeSdkRoot() {
        return mUserCanChangeSdkRoot;
    }
    public RepoSources getSources() {
        return mSources;
    }
    public RepoSourcesAdapter getSourcesAdapter() {
        return mSourcesAdapter;
    }
    public LocalSdkParser getLocalSdkParser() {
        return mLocalSdkParser;
    }
    public LocalSdkAdapter getLocalSdkAdapter() {
        return mLocalSdkAdapter;
    }
    public ISdkLog getSdkLog() {
        return mSdkLog;
    }
    public void setImageFactory(ImageFactory imageFactory) {
        mImageFactory = imageFactory;
    }
    public ImageFactory getImageFactory() {
        return mImageFactory;
    }
    public SdkManager getSdkManager() {
        return mSdkManager;
    }
    public AvdManager getAvdManager() {
        return mAvdManager;
    }
    public SettingsController getSettingsController() {
        return mSettingsController;
    }
    public void addListeners(ISdkListener listener) {
        if (mListeners.contains(listener) == false) {
            mListeners.add(listener);
        }
    }
    public void removeListener(ISdkListener listener) {
        mListeners.remove(listener);
    }
    public void setWindowShell(Shell windowShell) {
        mWindowShell = windowShell;
    }
    public Shell getWindowShell() {
        return mWindowShell;
    }
    public boolean checkIfInitFailed() {
        if (mAvdManagerInitError != null) {
            String example;
            if (SdkConstants.currentPlatform() == SdkConstants.PLATFORM_WINDOWS) {
                example = "%USERPROFILE%";     
            } else {
                example = "~";                 
            }
            MessageDialog.openError(mWindowShell,
                "Android Virtual Devices Manager",
                String.format(
                    "The AVD manager normally uses the user's profile directory to store " +
                    "AVD files. However it failed to find the default profile directory. " +
                    "\n" +
                    "To fix this, please set the environment variable ANDROID_SDK_HOME to " +
                    "a valid path such as \"%s\".",
                    example));
            return true;
        }
        return false;
    }
    private void initSdk() {
        mSdkManager = SdkManager.createManager(mOsSdkRoot, mSdkLog);
        try {
            mAvdManager = null; 
            mAvdManager = new AvdManager(mSdkManager, mSdkLog);
        } catch (AndroidLocationException e) {
            mSdkLog.error(e, "Unable to read AVDs: " + e.getMessage());  
            mAvdManagerInitError = e;
        }
        notifyListeners(false );
    }
    public void reloadSdk() {
        mSdkManager.reloadSdk(mSdkLog);
        if (mAvdManager != null) {
            try {
                mAvdManager.reloadAvds(mSdkLog);
            } catch (AndroidLocationException e) {
            }
        }
        mLocalSdkParser.clearPackages();
        notifyListeners(false );
    }
    public void reloadAvds() {
        if (mAvdManager != null) {
            try {
                mAvdManager.reloadAvds(mSdkLog);
            } catch (AndroidLocationException e) {
                mSdkLog.error(e, null);
            }
        }
    }
    public Package[] getInstalledPackage() {
        LocalSdkParser parser = getLocalSdkParser();
        Package[] packages = parser.getPackages();
        if (packages == null) {
            packages = parser.parseSdk(getOsSdkRoot(), getSdkManager(), getSdkLog());
        }
        return packages;
    }
    public void notifyListeners(final boolean init) {
        if (mWindowShell != null && mListeners.size() > 0) {
            mWindowShell.getDisplay().syncExec(new Runnable() {
                public void run() {
                    for (ISdkListener listener : mListeners) {
                        try {
                            listener.onSdkChange(init);
                        } catch (Throwable t) {
                            mSdkLog.error(t, null);
                        }
                    }
                }
            });
        }
    }
    public void installArchives(final ArrayList<ArchiveInfo> result) {
        if (mTaskFactory == null) {
            throw new IllegalArgumentException("Task Factory is null");
        }
        final boolean forceHttp = getSettingsController().getForceHttp();
        mTaskFactory.start("Installing Archives", new ITask() {
            public void run(ITaskMonitor monitor) {
                final int progressPerArchive = 2 * Archive.NUM_MONITOR_INC;
                monitor.setProgressMax(result.size() * progressPerArchive);
                monitor.setDescription("Preparing to install archives");
                boolean installedAddon = false;
                boolean installedTools = false;
                HashSet<Archive> installedArchives = new HashSet<Archive>();
                for (Package p : getInstalledPackage()) {
                    for (Archive a : p.getArchives()) {
                        installedArchives.add(a);
                    }
                }
                int numInstalled = 0;
                nextArchive: for (ArchiveInfo ai : result) {
                    Archive archive = ai.getNewArchive();
                    if (archive == null) {
                        continue nextArchive;
                    }
                    int nextProgress = monitor.getProgress() + progressPerArchive;
                    try {
                        if (monitor.isCancelRequested()) {
                            break;
                        }
                        ArchiveInfo[] adeps = ai.getDependsOn();
                        if (adeps != null) {
                            for (ArchiveInfo adep : adeps) {
                                Archive na = adep.getNewArchive();
                                if (na == null) {
                                    monitor.setResult("Skipping '%1$s'; it depends on a missing package.",
                                            archive.getParentPackage().getShortDescription());
                                    continue nextArchive;
                                } else if (!installedArchives.contains(na)) {
                                    monitor.setResult("Skipping '%1$s'; it depends on '%2$s' which was not installed.",
                                            archive.getParentPackage().getShortDescription(),
                                            adep.getShortDescription());
                                    continue nextArchive;
                                }
                            }
                        }
                        if (archive.install(mOsSdkRoot, forceHttp, mSdkManager, monitor)) {
                            installedArchives.add(archive);
                            numInstalled++;
                            installedArchives.remove(ai.getReplaced());
                            if (archive.getParentPackage() instanceof AddonPackage) {
                                installedAddon = true;
                            } else if (archive.getParentPackage() instanceof ToolPackage) {
                                installedTools = true;
                            }
                        }
                    } catch (Throwable t) {
                        String msg = t.getMessage();
                        if (msg != null) {
                            monitor.setResult("Unexpected Error installing '%1$s': %2$s",
                                    archive.getParentPackage().getShortDescription(), msg);
                        } else {
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            t.printStackTrace(new PrintStream(baos));
                            monitor.setResult("Unexpected Error installing '%1$s'\n%2$s",
                                    archive.getParentPackage().getShortDescription(),
                                    baos.toString());
                        }
                    } finally {
                        monitor.incProgress(nextProgress - monitor.getProgress());
                    }
                }
                if (installedAddon) {
                    try {
                        mSdkManager.updateAdb();
                        monitor.setResult("Updated ADB to support the USB devices declared in the SDK add-ons.");
                    } catch (Exception e) {
                        mSdkLog.error(e, "Update ADB failed");
                        monitor.setResult("failed to update adb to support the USB devices declared in the SDK add-ons.");
                    }
                }
                if (installedAddon || installedTools) {
                    askForAdbRestart(monitor);
                }
                if (installedTools) {
                    notifyToolsNeedsToBeRestarted();
                }
                if (numInstalled == 0) {
                    monitor.setDescription("Done. Nothing was installed.");
                } else {
                    monitor.setDescription("Done. %1$d %2$s installed.",
                            numInstalled,
                            numInstalled == 1 ? "package" : "packages");
                    reloadSdk();
                }
            }
        });
    }
    private void askForAdbRestart(ITaskMonitor monitor) {
        final boolean[] canRestart = new boolean[] { true };
        if (getSettingsController().getAskBeforeAdbRestart()) {
            Display display = mWindowShell.getDisplay();
            display.syncExec(new Runnable() {
                public void run() {
                    canRestart[0] = MessageDialog.openQuestion(mWindowShell,
                            "ADB Restart",
                            "A package that depends on ADB has been updated. It is recommended " +
                            "to restart ADB. Is it OK to do it now? If not, you can restart it " +
                            "manually later.");
                }
            });
        }
        if (canRestart[0]) {
            AdbWrapper adb = new AdbWrapper(getOsSdkRoot(), monitor);
            adb.stopAdb();
            adb.startAdb();
        }
    }
    private void notifyToolsNeedsToBeRestarted() {
        Display display = mWindowShell.getDisplay();
        display.syncExec(new Runnable() {
            public void run() {
                MessageDialog.openInformation(mWindowShell,
                        "Android Tools Updated",
                        "The Android SDK and AVD Manager that you are currently using has been updated. " +
                        "It is recommended that you now close the manager window and re-open it. " +
                        "If you started this window from Eclipse, please check if the Android " +
                        "plug-in needs to be updated.");
            }
        });
    }
    public void updateOrInstallAll(Collection<Archive> selectedArchives) {
        if (selectedArchives == null) {
            refreshSources(true);
        }
        UpdaterLogic ul = new UpdaterLogic();
        ArrayList<ArchiveInfo> archives = ul.computeUpdates(
                selectedArchives,
                getSources(),
                getLocalSdkParser().getPackages());
        if (selectedArchives == null) {
            ul.addNewPlatforms(archives, getSources(), getLocalSdkParser().getPackages());
        }
        UpdateChooserDialog dialog = new UpdateChooserDialog(getWindowShell(), this, archives);
        dialog.open();
        ArrayList<ArchiveInfo> result = dialog.getResult();
        if (result != null && result.size() > 0) {
            installArchives(result);
        }
    }
    public void refreshSources(final boolean forceFetching) {
        assert mTaskFactory != null;
        final boolean forceHttp = getSettingsController().getForceHttp();
        mTaskFactory.start("Refresh Sources",new ITask() {
            public void run(ITaskMonitor monitor) {
                RepoSource[] sources = mSources.getSources();
                monitor.setProgressMax(sources.length);
                for (RepoSource source : sources) {
                    if (forceFetching ||
                            source.getPackages() != null ||
                            source.getFetchError() != null) {
                        source.load(monitor.createSubMonitor(1), forceHttp);
                    }
                    monitor.incProgress(1);
                }
            }
        });
    }
}
