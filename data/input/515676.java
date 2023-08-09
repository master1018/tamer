final class ProjectCheckPage extends ExportWizardPage {
    private final static String IMG_ERROR = "error.png"; 
    private final static String IMG_WARNING = "warning.png"; 
    private final ExportWizard mWizard;
    private Display mDisplay;
    private Image mError;
    private Image mWarning;
    private boolean mHasMessage = false;
    private Composite mTopComposite;
    private Composite mErrorComposite;
    private Text mProjectText;
    private ProjectChooserHelper mProjectChooserHelper;
    private boolean mFirstOnShow = true;
    protected ProjectCheckPage(ExportWizard wizard, String pageName) {
        super(pageName);
        mWizard = wizard;
        setTitle("Project Checks");
        setDescription("Performs a set of checks to make sure the application can be exported.");
    }
    public void createControl(Composite parent) {
        mProjectChooserHelper = new ProjectChooserHelper(parent.getShell(),
                new NonLibraryProjectOnlyFilter());
        mDisplay = parent.getDisplay();
        GridLayout gl = null;
        GridData gd = null;
        mTopComposite = new Composite(parent, SWT.NONE);
        mTopComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
        mTopComposite.setLayout(new GridLayout(1, false));
        Composite projectComposite = new Composite(mTopComposite, SWT.NONE);
        projectComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        projectComposite.setLayout(gl = new GridLayout(3, false));
        gl.marginHeight = gl.marginWidth = 0;
        Label label = new Label(projectComposite, SWT.NONE);
        label.setLayoutData(gd = new GridData(GridData.FILL_HORIZONTAL));
        gd.horizontalSpan = 3;
        label.setText("Select the project to export:");
        new Label(projectComposite, SWT.NONE).setText("Project:");
        mProjectText = new Text(projectComposite, SWT.BORDER);
        mProjectText.setLayoutData(gd = new GridData(GridData.FILL_HORIZONTAL));
        mProjectText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                handleProjectNameChange();
            }
        });
        Button browseButton = new Button(projectComposite, SWT.PUSH);
        browseButton.setText("Browse...");
        browseButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                IJavaProject javaProject = mProjectChooserHelper.chooseJavaProject(
                        mProjectText.getText().trim(),
                        "Please select a project to export");
                if (javaProject != null) {
                    IProject project = javaProject.getProject();
                    mProjectText.setText(project.getName());
                }
            }
        });
        setControl(mTopComposite);
    }
    @Override
    void onShow() {
        if (mFirstOnShow) {
            IProject project = mWizard.getProject();
            if (project != null) {
                mProjectText.setText(project.getName());
            }
            mFirstOnShow = false;
        }
    }
    private void buildErrorUi(IProject project) {
        setErrorMessage(null);
        setMessage(null);
        setPageComplete(true);
        mHasMessage = false;
        GridLayout gl = null;
        mErrorComposite = new Composite(mTopComposite, SWT.NONE);
        mErrorComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        gl = new GridLayout(2, false);
        gl.marginHeight = gl.marginWidth = 0;
        gl.verticalSpacing *= 3; 
        mErrorComposite.setLayout(gl);
        if (project == null) {
            setErrorMessage("Select project to export.");
            mHasMessage = true;
        } else {
            try {
                if (project.hasNature(AndroidConstants.NATURE) == false) {
                    addError(mErrorComposite, "Project is not an Android project.");
                } else {
                    if (ProjectHelper.hasError(project, true))  {
                        addError(mErrorComposite, "Project has compilation error(s)");
                    }
                    IFolder outputIFolder = BaseProjectHelper.getOutputFolder(project);
                    if (outputIFolder != null) {
                        String outputOsPath =  outputIFolder.getLocation().toOSString();
                        String apkFilePath =  outputOsPath + File.separator + project.getName() +
                                AndroidConstants.DOT_ANDROID_PACKAGE;
                        File f = new File(apkFilePath);
                        if (f.isFile() == false) {
                            addError(mErrorComposite,
                                    String.format("%1$s/%2$s/%1$s%3$s does not exists!",
                                            project.getName(),
                                            outputIFolder.getName(),
                                            AndroidConstants.DOT_ANDROID_PACKAGE));
                        }
                    } else {
                        addError(mErrorComposite,
                                "Unable to get the output folder of the project!");
                    }
                    AndroidManifestParser manifestParser = AndroidManifestParser.parse(
                            BaseProjectHelper.getJavaProject(project), null ,
                            true , false );
                    Boolean debuggable = manifestParser.getDebuggable();
                    if (debuggable != null && debuggable == Boolean.TRUE) {
                        addWarning(mErrorComposite,
                                "The manifest 'debuggable' attribute is set to true.\nYou should set it to false for applications that you release to the public.");
                    }
                }
            } catch (CoreException e) {
                addError(mErrorComposite, "Unable to get project nature");
            }
        }
        if (mHasMessage == false) {
            Label label = new Label(mErrorComposite, SWT.NONE);
            GridData gd = new GridData(GridData.FILL_HORIZONTAL);
            gd.horizontalSpan = 2;
            label.setLayoutData(gd);
            label.setText("No errors found. Click Next.");
        }
        mTopComposite.layout();
    }
    private void addError(Composite parent, String message) {
        if (mError == null) {
            mError = AdtPlugin.getImageLoader().loadImage(IMG_ERROR, mDisplay);
        }
        new Label(parent, SWT.NONE).setImage(mError);
        Label label = new Label(parent, SWT.NONE);
        label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        label.setText(message);
        setErrorMessage("Application cannot be exported due to the error(s) below.");
        setPageComplete(false);
        mHasMessage = true;
    }
    private void addWarning(Composite parent, String message) {
        if (mWarning == null) {
            mWarning = AdtPlugin.getImageLoader().loadImage(IMG_WARNING, mDisplay);
        }
        new Label(parent, SWT.NONE).setImage(mWarning);
        Label label = new Label(parent, SWT.NONE);
        label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        label.setText(message);
        mHasMessage = true;
    }
    private void handleProjectNameChange() {
        setPageComplete(false);
        if (mErrorComposite != null) {
            mErrorComposite.dispose();
            mErrorComposite = null;
        }
        mWizard.setProject(null);
        String text = mProjectText.getText().trim();
        if (text.length() == 0) {
            setErrorMessage("Select project to export.");
        } else if (text.matches("[a-zA-Z0-9_ \\.-]+") == false) {
            setErrorMessage("Project name contains unsupported characters!");
        } else {
            IJavaProject[] projects = mProjectChooserHelper.getAndroidProjects(null);
            IProject found = null;
            for (IJavaProject javaProject : projects) {
                if (javaProject.getProject().getName().equals(text)) {
                    found = javaProject.getProject();
                    break;
                }
            }
            if (found != null) {
                setErrorMessage(null);
                mWizard.setProject(found);
                buildErrorUi(found);
            } else {
                setErrorMessage(String.format("There is no android project named '%1$s'",
                        text));
            }
        }
    }
}
