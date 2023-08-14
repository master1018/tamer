public class GraphicalEditorPart extends EditorPart implements IGraphicalLayoutEditor {
    private final LayoutEditor mLayoutEditor;
    private IFile mEditedFile;
    private Clipboard mClipboard;
    private ConfigurationComposite mConfigComposite;
    private SashForm mSashPalette;
    private SashForm mSashError;
    private PaletteComposite mPalette;
    private LayoutCanvas mLayoutCanvas;
    private RulesEngine mRulesEngine;
    private StyledText mErrorLabel;
    private Map<String, Map<String, IResourceValue>> mConfiguredFrameworkRes;
    private Map<String, Map<String, IResourceValue>> mConfiguredProjectRes;
    private ProjectCallback mProjectCallback;
    private ILayoutLog mLogger;
    private boolean mNeedsXmlReload = false;
    private boolean mNeedsRecompute = false;
    private TargetListener mTargetListener;
    private ConfigListener mConfigListener;
    private ReloadListener mReloadListener;
    protected boolean mUseExplodeMode;
    public GraphicalEditorPart(LayoutEditor layoutEditor) {
        mLayoutEditor = layoutEditor;
        setPartName("Graphical Layout");
    }
    @Override
    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        setSite(site);
        useNewEditorInput(input);
        if (mTargetListener == null) {
            mTargetListener = new TargetListener();
            AdtPlugin.getDefault().addTargetListener(mTargetListener);
        }
    }
    private void useNewEditorInput(IEditorInput input) throws PartInitException {
        if (!(input instanceof FileEditorInput)) {
            throw new PartInitException("Input is not of type FileEditorInput: " +  
                    input == null ? "null" : input.toString());                     
        }
    }
    @Override
    public void createPartControl(Composite parent) {
        Display d = parent.getDisplay();
        mClipboard = new Clipboard(d);
        GridLayout gl = new GridLayout(1, false);
        parent.setLayout(gl);
        gl.marginHeight = gl.marginWidth = 0;
        CustomToggle[] toggles = new CustomToggle[] {
                new CustomToggle(
                        "-",
                        null, 
                        "Canvas zoom out."
                        ) {
                    @Override
                    public void onSelected(boolean newState) {
                        rescale(-1);
                    }
                },
                new CustomToggle(
                        "+",
                        null, 
                        "Canvas zoom in."
                        ) {
                    @Override
                    public void onSelected(boolean newState) {
                        rescale(+1);
                    }
                },
                new CustomToggle(
                        "Explode",
                        null, 
                        "Displays extra margins in the layout."
                        ) {
                    @Override
                    public void onSelected(boolean newState) {
                        mUseExplodeMode = newState;
                        recomputeLayout();
                    }
                },
                new CustomToggle(
                        "Outline",
                        null, 
                        "Shows the of all views in the layout."
                        ) {
                    @Override
                    public void onSelected(boolean newState) {
                        mLayoutCanvas.setShowOutline(newState);
                    }
                }
        };
        mConfigListener = new ConfigListener();
        mConfigComposite = new ConfigurationComposite(mConfigListener, toggles, parent, SWT.BORDER);
        mSashPalette = new SashForm(parent, SWT.HORIZONTAL);
        mSashPalette.setLayoutData(new GridData(GridData.FILL_BOTH));
        mPalette = new PaletteComposite(mSashPalette);
        mSashError = new SashForm(mSashPalette, SWT.VERTICAL | SWT.BORDER);
        mSashError.setLayoutData(new GridData(GridData.FILL_BOTH));
        mLayoutCanvas = new LayoutCanvas(mRulesEngine, mSashError, SWT.NONE);
        mErrorLabel = new StyledText(mSashError, SWT.READ_ONLY);
        mErrorLabel.setEditable(false);
        mErrorLabel.setBackground(d.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
        mErrorLabel.setForeground(d.getSystemColor(SWT.COLOR_INFO_FOREGROUND));
        mSashPalette.setWeights(new int[] { 20, 80 });
        mSashError.setWeights(new int[] { 80, 20 });
        mSashError.setMaximizedControl(mLayoutCanvas);
        setupEditActions();
        reloadPalette();
    }
    private void rescale(int direction) {
        double s = mLayoutCanvas.getScale();
        if (direction > 0) {
            s = s * 2;
        } else {
            s = s / 2;
        }
        mLayoutCanvas.setScale(s);
    }
    private void setupEditActions() {
        IActionBars actionBars = getEditorSite().getActionBars();
        actionBars.setGlobalActionHandler(ActionFactory.COPY.getId(), new Action("Copy") {
            @Override
            public void run() {
                mLayoutCanvas.onCopy(mClipboard);
            }
        });
        actionBars.setGlobalActionHandler(ActionFactory.CUT.getId(), new Action("Cut") {
            @Override
            public void run() {
                mLayoutCanvas.onCut(mClipboard);
            }
        });
        actionBars.setGlobalActionHandler(ActionFactory.PASTE.getId(), new Action("Paste") {
            @Override
            public void run() {
                mLayoutCanvas.onPaste(mClipboard);
            }
        });
        actionBars.setGlobalActionHandler(ActionFactory.SELECT_ALL.getId(),
                new Action("Select All") {
            @Override
            public void run() {
                mLayoutCanvas.onSelectAll();
            }
        });
    }
    private void displayError(String errorFormat, Object...parameters) {
        if (errorFormat != null) {
            mErrorLabel.setText(String.format(errorFormat, parameters));
        }
        mSashError.setMaximizedControl(null);
    }
    private void hideError() {
        mSashError.setMaximizedControl(mLayoutCanvas);
    }
    @Override
    public void dispose() {
        if (mTargetListener != null) {
            AdtPlugin.getDefault().removeTargetListener(mTargetListener);
            mTargetListener = null;
        }
        if (mReloadListener != null) {
            LayoutReloadMonitor.getMonitor().removeListener(mReloadListener);
            mReloadListener = null;
        }
        if (mClipboard != null) {
            mClipboard.dispose();
            mClipboard = null;
        }
        super.dispose();
    }
    private class ConfigListener implements IConfigListener {
        public void onConfigurationChange() {
            mConfiguredFrameworkRes = mConfiguredProjectRes = null;
            if (mEditedFile == null || mConfigComposite.getEditedConfig() == null) {
                return;
            }
            if (mLayoutEditor.isCreatingPages()) {
                recomputeLayout();
            } else {
                ProjectResources resources = ResourceManager.getInstance().getProjectResources(
                        mEditedFile.getProject());
                ResourceFile match = null;
                if (resources != null) {
                    match = resources.getMatchingFile(mEditedFile.getName(),
                                                      ResourceFolderType.LAYOUT,
                                                      mConfigComposite.getCurrentConfig());
                }
                if (match != null) {
                    IFileWrapper iFileWrapper = (IFileWrapper) match.getFile();
                    IFile iFile = iFileWrapper.getIFile();
                    if (iFile.equals(mEditedFile) == false) {
                        try {
                            mLayoutEditor.setNewFileOnConfigChange(true);
                            IDE.openEditor(getSite().getWorkbenchWindow().getActivePage(), iFile);
                            return;
                        } catch (PartInitException e) {
                        }
                    }
                    mConfigComposite.storeState();
                    recomputeLayout();
                } else {
                    FolderConfiguration currentConfig = mConfigComposite.getCurrentConfig();
                    displayError(
                            "No resources match the configuration\n \n\t%1$s\n \nChange the configuration or create:\n \n\tres/%2$s/%3$s\n \nYou can also click the 'Create' button above.",
                            currentConfig.toDisplayString(),
                            currentConfig.getFolderName(ResourceFolderType.LAYOUT,
                                    Sdk.getCurrent().getTarget(mEditedFile.getProject())),
                            mEditedFile.getName());
                }
            }
        }
        public void onThemeChange() {
            mConfigComposite.storeState();
            recomputeLayout();
        }
        public void onClippingChange() {
            recomputeLayout();
        }
        public void onCreate() {
            LayoutCreatorDialog dialog = new LayoutCreatorDialog(mConfigComposite.getShell(),
                    mEditedFile.getName(),
                    Sdk.getCurrent().getTarget(mEditedFile.getProject()),
                    mConfigComposite.getCurrentConfig());
            if (dialog.open() == Dialog.OK) {
                final FolderConfiguration config = new FolderConfiguration();
                dialog.getConfiguration(config);
                createAlternateLayout(config);
            }
        }
        public Map<String, Map<String, IResourceValue>> getConfiguredFrameworkResources() {
            if (mConfiguredFrameworkRes == null && mConfigComposite != null) {
                ProjectResources frameworkRes = getFrameworkResources();
                if (frameworkRes == null) {
                    AdtPlugin.log(IStatus.ERROR, "Failed to get ProjectResource for the framework");
                } else {
                    mConfiguredFrameworkRes = frameworkRes.getConfiguredResources(
                            mConfigComposite.getCurrentConfig());
                }
            }
            return mConfiguredFrameworkRes;
        }
        public Map<String, Map<String, IResourceValue>> getConfiguredProjectResources() {
            if (mConfiguredProjectRes == null && mConfigComposite != null) {
                ProjectResources project = getProjectResources();
                project.loadAll();
                mConfiguredProjectRes = project.getConfiguredResources(
                        mConfigComposite.getCurrentConfig());
            }
            return mConfiguredProjectRes;
        }
        public ProjectResources getFrameworkResources() {
            if (mEditedFile != null) {
                Sdk currentSdk = Sdk.getCurrent();
                if (currentSdk != null) {
                    IAndroidTarget target = currentSdk.getTarget(mEditedFile.getProject());
                    if (target != null) {
                        AndroidTargetData data = currentSdk.getTargetData(target);
                        if (data != null) {
                            return data.getFrameworkResources();
                        }
                    }
                }
            }
            return null;
        }
        public ProjectResources getProjectResources() {
            if (mEditedFile != null) {
                ResourceManager manager = ResourceManager.getInstance();
                return manager.getProjectResources(mEditedFile.getProject());
            }
            return null;
        }
        private void createAlternateLayout(final FolderConfiguration config) {
            new Job("Create Alternate Resource") {
                @Override
                protected IStatus run(IProgressMonitor monitor) {
                    String folderName = config.getFolderName(ResourceFolderType.LAYOUT,
                            Sdk.getCurrent().getTarget(mEditedFile.getProject()));
                    try {
                        IFolder res = (IFolder)mEditedFile.getParent().getParent();
                        String path = res.getLocation().toOSString();
                        File newLayoutFolder = new File(path + File.separator + folderName);
                        if (newLayoutFolder.isFile()) {
                            String message = String.format("File 'res/%1$s' is in the way!",
                                    folderName);
                            AdtPlugin.displayError("Layout Creation", message);
                            return new Status(IStatus.ERROR, AdtPlugin.PLUGIN_ID, message);
                        } else if (newLayoutFolder.exists() == false) {
                            newLayoutFolder.mkdir();
                        }
                        File newLayoutFile = new File(newLayoutFolder.getAbsolutePath() +
                                    File.separator + mEditedFile.getName());
                        newLayoutFile.createNewFile();
                        InputStream input = mEditedFile.getContents();
                        FileOutputStream fos = new FileOutputStream(newLayoutFile);
                        byte[] data = new byte[512];
                        int count;
                        while ((count = input.read(data)) != -1) {
                            fos.write(data, 0, count);
                        }
                        input.close();
                        fos.close();
                        res.refreshLocal(IResource.DEPTH_INFINITE, new IProgressMonitor() {
                            public void done() {
                                mConfigComposite.getDisplay().asyncExec(new Runnable() {
                                    public void run() {
                                        onConfigurationChange();
                                    }
                                });
                            }
                            public void beginTask(String name, int totalWork) {
                            }
                            public void internalWorked(double work) {
                            }
                            public boolean isCanceled() {
                                return false;
                            }
                            public void setCanceled(boolean value) {
                            }
                            public void setTaskName(String name) {
                            }
                            public void subTask(String name) {
                            }
                            public void worked(int work) {
                            }
                        });
                    } catch (IOException e2) {
                        String message = String.format(
                                "Failed to create File 'res/%1$s/%2$s' : %3$s",
                                folderName, mEditedFile.getName(), e2.getMessage());
                        AdtPlugin.displayError("Layout Creation", message);
                        return new Status(IStatus.ERROR, AdtPlugin.PLUGIN_ID,
                                message, e2);
                    } catch (CoreException e2) {
                        String message = String.format(
                                "Failed to create File 'res/%1$s/%2$s' : %3$s",
                                folderName, mEditedFile.getName(), e2.getMessage());
                        AdtPlugin.displayError("Layout Creation", message);
                        return e2.getStatus();
                    }
                    return Status.OK_STATUS;
                }
            }.schedule();
        }
    }
    private class TargetListener implements ITargetChangeListener {
        public void onProjectTargetChange(IProject changedProject) {
            if (changedProject != null && changedProject.equals(getProject())) {
                updateEditor();
            }
        }
        public void onTargetLoaded(IAndroidTarget target) {
            IProject project = getProject();
            if (target != null && target.equals(Sdk.getCurrent().getTarget(project))) {
                updateEditor();
            }
        }
        public void onSdkLoaded() {
            Sdk currentSdk = Sdk.getCurrent();
            if (currentSdk != null) {
                IAndroidTarget target = currentSdk.getTarget(mEditedFile.getProject());
                if (target != null) {
                    mConfigComposite.onSdkLoaded(target);
                    mConfigListener.onConfigurationChange();
                }
            }
        }
        private void updateEditor() {
            mLayoutEditor.commitPages(false );
            mConfiguredFrameworkRes = mConfiguredProjectRes = null;
            mProjectCallback = null;
            mLayoutEditor.initUiRootNode(true );
        }
        private IProject getProject() {
            return getLayoutEditor().getProject();
        }
    }
    @Override
    public void doSave(IProgressMonitor monitor) {
    }
    @Override
    public void doSaveAs() {
    }
    @Override
    public boolean isDirty() {
        return false;
    }
    @Override
    public boolean isSaveAsAllowed() {
        return false;
    }
    @Override
    public void setFocus() {
    }
    public void activated() {
        if (mNeedsRecompute || mNeedsXmlReload) {
            recomputeLayout();
        }
    }
    public void deactivated() {
    }
    public void openFile(IFile file) {
        mEditedFile = file;
        mConfigComposite.setFile(mEditedFile);
        if (mReloadListener == null) {
            mReloadListener = new ReloadListener();
            LayoutReloadMonitor.getMonitor().addListener(mEditedFile.getProject(), mReloadListener);
        }
        if (mRulesEngine == null) {
            mRulesEngine = new RulesEngine(mEditedFile.getProject());
            if (mLayoutCanvas != null) {
                mLayoutCanvas.setRulesEngine(mRulesEngine);
            }
        }
    }
    public void replaceFile(IFile file) {
        mEditedFile = file;
        mConfigComposite.replaceFile(mEditedFile);
    }
    public void changeFileOnNewConfig(IFile file) {
        mEditedFile = file;
        mConfigComposite.changeFileOnNewConfig(mEditedFile);
    }
    public void onTargetChange() {
        mConfigComposite.onXmlModelLoaded();
        mConfigListener.onConfigurationChange();
    }
    public void onSdkChange() {
        Sdk currentSdk = Sdk.getCurrent();
        if (currentSdk != null) {
            IAndroidTarget target = currentSdk.getTarget(mEditedFile.getProject());
            if (target != null) {
                mConfigComposite.onSdkLoaded(target);
                mConfigListener.onConfigurationChange();
            }
        }
    }
    public Clipboard getClipboard() {
        return mClipboard;
    }
    public LayoutEditor getLayoutEditor() {
        return mLayoutEditor;
    }
    public UiDocumentNode getModel() {
        return mLayoutEditor.getUiRootNode();
    }
    public SelectionSynchronizer getSelectionSynchronizer() {
        return null;
    }
    public void onXmlModelChanged() {
        if (mLayoutEditor.isGraphicalEditorActive()) {
            doXmlReload(true );
            recomputeLayout();
        } else {
            mNeedsXmlReload = true;
        }
    }
    private void doXmlReload(boolean force) {
        if (force || mNeedsXmlReload) {
            mNeedsXmlReload = false;
        }
    }
    public void recomputeLayout() {
        doXmlReload(false );
        try {
            if (mEditedFile.exists() == false) {
                displayError("Resource '%1$s' does not exist.",
                             mEditedFile.getFullPath().toString());
                return;
            }
            IProject iProject = mEditedFile.getProject();
            if (mEditedFile.isSynchronized(IResource.DEPTH_ZERO) == false) {
                String message = String.format("%1$s is out of sync. Please refresh.",
                        mEditedFile.getName());
                displayError(message);
                AdtPlugin.printErrorToConsole(iProject.getName(), message);
                return;
            }
            Sdk currentSdk = Sdk.getCurrent();
            if (currentSdk != null) {
                IAndroidTarget target = currentSdk.getTarget(mEditedFile.getProject());
                if (target == null) {
                    displayError("The project target is not set.");
                    return;
                }
                AndroidTargetData data = currentSdk.getTargetData(target);
                if (data == null) {
                    LoadStatus targetLoadStatus = currentSdk.checkAndLoadTargetData(target, null);
                    switch (targetLoadStatus) {
                        case LOADING:
                            displayError("The project target (%1$s) is still loading.\n%2$s will refresh automatically once the process is finished.",
                                    target.getName(), mEditedFile.getName());
                            break;
                        case FAILED: 
                        case LOADED: 
                            displayError("The project target (%s) was not properly loaded.",
                                    target.getName());
                            break;
                    }
                    return;
                }
                UiDocumentNode model = getModel();
                if (model.getUiChildren().size() == 0) {
                    displayError("No Xml content. Go to the Outline view and add nodes.");
                    return;
                }
                LayoutBridge bridge = data.getLayoutBridge();
                if (bridge.bridge != null) { 
                    ResourceManager resManager = ResourceManager.getInstance();
                    ProjectResources projectRes = resManager.getProjectResources(iProject);
                    if (projectRes == null) {
                        displayError("Missing project resources.");
                        return;
                    }
                    Map<String, Map<String, IResourceValue>> configuredProjectRes =
                        mConfigListener.getConfiguredProjectResources();
                    Map<String, Map<String, IResourceValue>> frameworkResources =
                        mConfigListener.getConfiguredFrameworkResources();
                    if (configuredProjectRes != null && frameworkResources != null) {
                        if (mProjectCallback == null) {
                            mProjectCallback = new ProjectCallback(
                                    bridge.classLoader, projectRes, iProject);
                        }
                        if (mLogger == null) {
                            mLogger = new ILayoutLog() {
                                public void error(String message) {
                                    AdtPlugin.printErrorToConsole(mEditedFile.getName(), message);
                                }
                                public void error(Throwable error) {
                                    String message = error.getMessage();
                                    if (message == null) {
                                        message = error.getClass().getName();
                                    }
                                    PrintStream ps = new PrintStream(AdtPlugin.getErrorStream());
                                    error.printStackTrace(ps);
                                }
                                public void warning(String message) {
                                    AdtPlugin.printToConsole(mEditedFile.getName(), message);
                                }
                            };
                        }
                        String theme = mConfigComposite.getTheme();
                        if (theme != null) {
                            Rectangle rect = getBounds();
                            int width = rect.width;
                            int height = rect.height;
                            if (mUseExplodeMode) {
                                List<UiElementNode> children = getModel().getUiChildren();
                                if (children.size() == 1) {
                                    ExplodedRenderingHelper helper = new ExplodedRenderingHelper(
                                            children.get(0).getXmlNode(), iProject);
                                    int paddingValue = ExplodedRenderingHelper.PADDING_VALUE * 2;
                                    width += helper.getWidthPadding() * paddingValue;
                                    height += helper.getHeightPadding() * paddingValue;
                                }
                            }
                            int density = mConfigComposite.getDensity().getDpiValue();
                            float xdpi = mConfigComposite.getXDpi();
                            float ydpi = mConfigComposite.getYDpi();
                            boolean isProjectTheme = mConfigComposite.isProjectTheme();
                            UiElementPullParser parser = new UiElementPullParser(getModel(),
                                    mUseExplodeMode, density, xdpi, iProject);
                            ILayoutResult result = computeLayout(bridge, parser,
                                    iProject ,
                                    width, height, !mConfigComposite.getClipping(),
                                    density, xdpi, ydpi,
                                    theme, isProjectTheme,
                                    configuredProjectRes, frameworkResources, mProjectCallback,
                                    mLogger);
                            mLayoutCanvas.setResult(result);
                            if (result.getSuccess() == ILayoutResult.SUCCESS) {
                                hideError();
                            } else {
                                displayError(result.getErrorMessage());
                            }
                            model.refreshUi();
                        }
                    }
                } else {
                    if (bridge.status == LoadStatus.LOADING) {
                        displayError("Eclipse is loading framework information and the layout library from the SDK folder.\n%1$s will refresh automatically once the process is finished.",
                                     mEditedFile.getName());
                    } else {
                        displayError("Eclipse failed to load the framework information and the layout library!");
                    }
                }
            } else {
                displayError("Eclipse is loading the SDK.\n%1$s will refresh automatically once the process is finished.",
                             mEditedFile.getName());
            }
        } finally {
            mNeedsRecompute = false;
        }
    }
    @SuppressWarnings("deprecation")
    private static ILayoutResult computeLayout(LayoutBridge bridge,
            IXmlPullParser layoutDescription, Object projectKey,
            int screenWidth, int screenHeight, boolean renderFullSize,
            int density, float xdpi, float ydpi,
            String themeName, boolean isProjectTheme,
            Map<String, Map<String, IResourceValue>> projectResources,
            Map<String, Map<String, IResourceValue>> frameworkResources,
            IProjectCallback projectCallback, ILayoutLog logger) {
        if (bridge.apiLevel >= ILayoutBridge.API_CURRENT) {
            return bridge.bridge.computeLayout(layoutDescription,
                    projectKey, screenWidth, screenHeight, renderFullSize,
                    density, xdpi, ydpi,
                    themeName, isProjectTheme,
                    projectResources, frameworkResources, projectCallback,
                    logger);
        } else if (bridge.apiLevel == 3) {
            return bridge.bridge.computeLayout(layoutDescription,
                    projectKey, screenWidth, screenHeight, density, xdpi, ydpi,
                    themeName, isProjectTheme,
                    projectResources, frameworkResources, projectCallback,
                    logger);
        } else if (bridge.apiLevel == 2) {
            return bridge.bridge.computeLayout(layoutDescription,
                    projectKey, screenWidth, screenHeight, themeName, isProjectTheme,
                    projectResources, frameworkResources, projectCallback,
                    logger);
        } else {
            if (isProjectTheme) {
                themeName = "*" + themeName; 
            }
            return bridge.bridge.computeLayout(layoutDescription,
                    projectKey, screenWidth, screenHeight, themeName,
                    projectResources, frameworkResources, projectCallback,
                    logger);
        }
    }
    public Rectangle getBounds() {
        return mConfigComposite.getScreenBounds();
    }
    public void reloadPalette() {
        if (mPalette != null) {
            mPalette.reloadPalette(mLayoutEditor.getTargetData());
        }
    }
    public void selectModel(UiElementNode uiNodeModel) {
    }
    private class ReloadListener implements ILayoutReloadListener {
        public void reloadLayout(ChangeFlags flags, boolean libraryChanged) {
            boolean recompute = false;
            if (flags.rClass) {
                recompute = true;
                if (mEditedFile != null) {
                    ProjectResources projectRes = ResourceManager.getInstance().getProjectResources(
                            mEditedFile.getProject());
                    if (projectRes != null) {
                        projectRes.resetDynamicIds();
                    }
                }
            }
            if (flags.localeList) {
                mLayoutCanvas.getDisplay().asyncExec(new Runnable() {
                    public void run() {
                        mConfigComposite.updateLocales();
                    }
                });
            }
            if (flags.resources || (libraryChanged && flags.layout)) {
                recompute = true;
                mConfiguredProjectRes = null;
                IAndroidTarget target = Sdk.getCurrent().getTarget(mEditedFile.getProject());
                if (target != null) {
                    AndroidTargetData data = Sdk.getCurrent().getTargetData(target);
                    if (data != null) {
                        LayoutBridge bridge = data.getLayoutBridge();
                        if (bridge.bridge != null) {
                            bridge.bridge.clearCaches(mEditedFile.getProject());
                        }
                    }
                }
            }
            if (flags.code) {
                if (mProjectCallback != null && mProjectCallback.isUsed()) {
                    mProjectCallback = null;
                    recompute = true;
                }
            }
            if (recompute) {
                mLayoutCanvas.getDisplay().asyncExec(new Runnable() {
                    public void run() {
                        if (mLayoutEditor.isGraphicalEditorActive()) {
                            recomputeLayout();
                        } else {
                            mNeedsRecompute = true;
                        }
                    }
                });
            }
        }
    }
}
