class ExtractStringInputPage extends UserInputWizardPage implements IWizardPage {
    private static HashMap<String, String> sLastResFilePath = new HashMap<String, String>();
    private final IProject mProject;
    private Combo mStringIdCombo;
    private Text mStringValueField;
    private ConfigurationSelector mConfigSelector;
    private Combo mResFileCombo;
    private static final Pattern RES_XML_FILE_REGEX = Pattern.compile(
                                     "/res/[a-z][a-zA-Z0-9_-]+/[^.]+\\.xml");  
    private static final String RES_FOLDER_ABS =
        AndroidConstants.WS_RESOURCES + AndroidConstants.WS_SEP;
    private static final String RES_FOLDER_REL =
        SdkConstants.FD_RESOURCES + AndroidConstants.WS_SEP;
    private static final String DEFAULT_RES_FILE_PATH = "/res/values/strings.xml";  
    private XmlStringFileHelper mXmlHelper = new XmlStringFileHelper();
    public ExtractStringInputPage(IProject project) {
        super("ExtractStringInputPage");  
        mProject = project;
    }
    public void createControl(Composite parent) {
        Composite content = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 1;
        content.setLayout(layout);
        createStringGroup(content);
        createResFileGroup(content);
        validatePage();
        setControl(content);
    }
    public void createStringGroup(Composite content) {
        final ExtractStringRefactoring ref = getOurRefactoring();
        Group group = new Group(content, SWT.NONE);
        group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        if (ref.getMode() == ExtractStringRefactoring.Mode.EDIT_SOURCE) {
            group.setText("String Replacement");
        } else {
            group.setText("New String");
        }
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        group.setLayout(layout);
        Label label = new Label(group, SWT.NONE);
        label.setText("&String");
        String selectedString = ref.getTokenString();
        mStringValueField = new Text(group, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        mStringValueField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        mStringValueField.setText(selectedString != null ? selectedString : "");  
        ref.setNewStringValue(mStringValueField.getText());
        mStringValueField.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                validatePage();
            }
        });
        label = new Label(group, SWT.NONE);
        if (ref.getMode() == ExtractStringRefactoring.Mode.EDIT_SOURCE) {
            label.setText("&Replace by R.string.");
        } else if (ref.getMode() == ExtractStringRefactoring.Mode.SELECT_NEW_ID) {
            label.setText("New &R.string.");
        } else {
            label.setText("ID &R.string.");
        }
        mStringIdCombo = new Combo(group, SWT.SINGLE | SWT.LEFT | SWT.BORDER | SWT.DROP_DOWN);
        mStringIdCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        mStringIdCombo.setText(guessId(selectedString));
        mStringIdCombo.forceFocus();
        ref.setNewStringId(mStringIdCombo.getText().trim());
        mStringIdCombo.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                validatePage();
            }
        });
        mStringIdCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                validatePage();
            }
        });
    }
    private void createResFileGroup(Composite content) {
        Group group = new Group(content, SWT.NONE);
        group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        group.setText("XML resource to edit");
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        group.setLayout(layout);
        Label label;
        label = new Label(group, SWT.NONE);
        label.setText("&Configuration:");
        mConfigSelector = new ConfigurationSelector(group, false );
        GridData gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL);
        gd.horizontalSpan = 2;
        gd.widthHint = ConfigurationSelector.WIDTH_HINT;
        gd.heightHint = ConfigurationSelector.HEIGHT_HINT;
        mConfigSelector.setLayoutData(gd);
        OnConfigSelectorUpdated onConfigSelectorUpdated = new OnConfigSelectorUpdated();
        mConfigSelector.setOnChangeListener(onConfigSelectorUpdated);
        label = new Label(group, SWT.NONE);
        label.setText("Resource &file:");
        mResFileCombo = new Combo(group, SWT.DROP_DOWN);
        mResFileCombo.select(0);
        mResFileCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        mResFileCombo.addModifyListener(onConfigSelectorUpdated);
        String projPath = mProject.getFullPath().toPortableString();
        String filePath = sLastResFilePath.get(projPath);
        mResFileCombo.setText(filePath != null ? filePath : DEFAULT_RES_FILE_PATH);
        onConfigSelectorUpdated.run();
    }
    private String guessId(String text) {
        if (text == null) {
            return "";  
        }
        text = text.toLowerCase();
        text = text.replaceAll("[^a-zA-Z0-9]+", "_");  
        if (text.length() > 0 && !Character.isJavaIdentifierStart(text.charAt(0))) {
            text = "_" + text;  
        }
        return text;
    }
    private ExtractStringRefactoring getOurRefactoring() {
        return (ExtractStringRefactoring) getRefactoring();
    }
    private boolean validatePage() {
        boolean success = true;
        ExtractStringRefactoring ref = getOurRefactoring();
        String text = mStringIdCombo.getText().trim();
        if (text == null || text.length() < 1) {
            setErrorMessage("Please provide a resource ID.");
            success = false;
        } else {
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                boolean ok = i == 0 ?
                        Character.isJavaIdentifierStart(c) :
                        Character.isJavaIdentifierPart(c);
                if (!ok) {
                    setErrorMessage(String.format(
                            "The resource ID must be a valid Java identifier. The character %1$c at position %2$d is not acceptable.",
                            c, i+1));
                    success = false;
                    break;
                }
            }
            if (success) {
                ref.setNewStringId(text);
            }
        }
        String resFile = mResFileCombo.getText();
        if (success) {
            if (resFile == null || resFile.length() == 0) {
                setErrorMessage("A resource file name is required.");
                success = false;
            } else if (!RES_XML_FILE_REGEX.matcher(resFile).matches()) {
                setErrorMessage("The XML file name is not valid.");
                success = false;
            }
        }
        if (success) {
            setErrorMessage(null);
            ref.setTargetFile(resFile);
            sLastResFilePath.put(mProject.getFullPath().toPortableString(), resFile);
            String idValue = mXmlHelper.valueOfStringId(mProject, resFile, text);
            if (idValue != null) {
                String msg = String.format("%1$s already contains a string ID '%2$s' with value '%3$s'.",
                        resFile,
                        text,
                        idValue);
                if (ref.getMode() == ExtractStringRefactoring.Mode.SELECT_NEW_ID) {
                    setErrorMessage(msg);
                    success = false;
                } else {
                    setMessage(msg, WizardPage.WARNING);
                }
            } else if (mProject.findMember(resFile) == null) {
                setMessage(
                        String.format("File %2$s does not exist and will be created.",
                                text, resFile),
                        WizardPage.INFORMATION);
            } else {
                setMessage(null);
            }
        }
        if (success) {
            ref.setNewStringValue(mStringValueField.getText());
        }
        setPageComplete(success);
        return success;
    }
    private void updateStringValueCombo() {
        String resFile = mResFileCombo.getText();
        Map<String, String> ids = mXmlHelper.getResIdsForFile(mProject, resFile);
        String currText = mStringIdCombo.getText();
        mStringIdCombo.removeAll();
        mStringIdCombo.setItems(ids.keySet().toArray(new String[ids.size()]));
        if (!currText.equals(mStringIdCombo.getText())) {
            mStringIdCombo.setText(currText);
        }
    }
    public class OnConfigSelectorUpdated implements Runnable, ModifyListener {
        private final Pattern mPathRegex = Pattern.compile(
            "(/res/[a-z][a-zA-Z0-9_-]+/)(.+)");  
        private FolderConfiguration mTempConfig = new FolderConfiguration();
        private HashMap<String, TreeSet<String>> mFolderCache =
            new HashMap<String, TreeSet<String>>();
        private String mLastFolderUsedInCombo = null;
        private boolean mInternalConfigChange;
        private boolean mInternalFileComboChange;
        public void run() {
            if (mInternalConfigChange) {
                return;
            }
            String leafName = "";  
            String currPath = mResFileCombo.getText();
            Matcher m = mPathRegex.matcher(currPath);
            if (m.matches()) {
                leafName = m.group(2);
                currPath = m.group(1);
            } else {
                currPath = "";  
            }
            mConfigSelector.getConfiguration(mTempConfig);
            StringBuffer sb = new StringBuffer(RES_FOLDER_ABS);
            sb.append(mTempConfig.getFolderName(ResourceFolderType.VALUES, mProject));
            sb.append('/');
            String newPath = sb.toString();
            if (newPath.equals(currPath) && newPath.equals(mLastFolderUsedInCombo)) {
                return;
            }
            TreeSet<String> filePaths = mFolderCache.get(newPath);
            if (filePaths == null) {
                filePaths = new TreeSet<String>();
                IFolder folder = mProject.getFolder(newPath);
                if (folder != null && folder.exists()) {
                    try {
                        for (IResource res : folder.members()) {
                            String name = res.getName();
                            if (res.getType() == IResource.FILE && name.endsWith(".xml")) {
                                filePaths.add(newPath + name);
                            }
                        }
                    } catch (CoreException e) {
                    }
                }
                mFolderCache.put(newPath, filePaths);
            }
            currPath = newPath + leafName;
            if (leafName.length() > 0 && !filePaths.contains(currPath)) {
                filePaths.add(currPath);
            }
            try {
                mInternalFileComboChange = true;
                mResFileCombo.removeAll();
                for (String filePath : filePaths) {
                    mResFileCombo.add(filePath);
                }
                int index = -1;
                if (leafName.length() > 0) {
                    index = mResFileCombo.indexOf(currPath);
                    if (index >= 0) {
                        mResFileCombo.select(index);
                    }
                }
                if (index == -1) {
                    mResFileCombo.setText(currPath);
                }
                mLastFolderUsedInCombo = newPath;
            } finally {
                mInternalFileComboChange = false;
            }
            updateStringValueCombo();
            validatePage();
        }
        public void modifyText(ModifyEvent e) {
            if (mInternalFileComboChange) {
                return;
            }
            String wsFolderPath = mResFileCombo.getText();
            wsFolderPath = wsFolderPath.replaceAll("/+\\.\\./+|/+\\./+|
            wsFolderPath = wsFolderPath.replaceAll("^\\.\\./+|^\\./+", "");                   
            wsFolderPath = wsFolderPath.replaceAll("/+\\.\\.$|/+\\.$|/+$", "");               
            if (wsFolderPath.startsWith(RES_FOLDER_REL)) {
                wsFolderPath = RES_FOLDER_ABS + wsFolderPath.substring(RES_FOLDER_REL.length());
                mInternalFileComboChange = true;
                mResFileCombo.setText(wsFolderPath);
                mInternalFileComboChange = false;
            }
            if (wsFolderPath.startsWith(RES_FOLDER_ABS)) {
                wsFolderPath = wsFolderPath.substring(RES_FOLDER_ABS.length());
                int pos = wsFolderPath.indexOf(AndroidConstants.WS_SEP_CHAR);
                if (pos >= 0) {
                    wsFolderPath = wsFolderPath.substring(0, pos);
                }
                String[] folderSegments = wsFolderPath.split(FolderConfiguration.QUALIFIER_SEP);
                if (folderSegments.length > 0) {
                    String folderName = folderSegments[0];
                    if (folderName != null && !folderName.equals(wsFolderPath)) {
                        mInternalConfigChange = true;
                        mConfigSelector.setConfiguration(folderSegments);
                        mInternalConfigChange = false;
                    }
                }
            }
            updateStringValueCombo();
            validatePage();
        }
    }
}
