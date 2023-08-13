final class AvdCreationDialog extends GridDialog {
    private final AvdManager mAvdManager;
    private final TreeMap<String, IAndroidTarget> mCurrentTargets =
        new TreeMap<String, IAndroidTarget>();
    private final Map<String, HardwareProperty> mHardwareMap;
    private final Map<String, String> mProperties = new HashMap<String, String>();
    private final ArrayList<String> mEditedProperties = new ArrayList<String>();
    private final ImageFactory mImageFactory;
    private final ISdkLog mSdkLog;
    private Text mAvdName;
    private Combo mTargetCombo;
    private Button mSdCardSizeRadio;
    private Text mSdCardSize;
    private Combo mSdCardSizeCombo;
    private Text mSdCardFile;
    private Button mBrowseSdCard;
    private Button mSdCardFileRadio;
    private Button mSkinListRadio;
    private Combo mSkinCombo;
    private Button mSkinSizeRadio;
    private Text mSkinSizeWidth;
    private Text mSkinSizeHeight;
    private TableViewer mHardwareViewer;
    private Button mDeleteHardwareProp;
    private Button mForceCreation;
    private Button mOkButton;
    private Label mStatusIcon;
    private Label mStatusLabel;
    private Composite mStatusComposite;
    private final VerifyListener mDigitVerifier = new VerifyListener() {
        public void verifyText(VerifyEvent event) {
            int count = event.text.length();
            for (int i = 0 ; i < count ; i++) {
                char c = event.text.charAt(i);
                if (c < '0' || c > '9') {
                    event.doit = false;
                    return;
                }
            }
        }
    };
    private class CreateNameModifyListener implements ModifyListener {
        public void modifyText(ModifyEvent e) {
            String name = mAvdName.getText().trim();
            AvdInfo avdMatch = mAvdManager.getAvd(name, false );
            if (avdMatch != null) {
                mForceCreation.setEnabled(true);
            } else {
                mForceCreation.setEnabled(false);
                mForceCreation.setSelection(false);
            }
            validatePage();
        }
    }
    private class ValidateListener extends SelectionAdapter implements ModifyListener {
        public void modifyText(ModifyEvent e) {
            validatePage();
        }
        @Override
        public void widgetSelected(SelectionEvent e) {
            super.widgetSelected(e);
            validatePage();
        }
    }
    protected AvdCreationDialog(Shell parentShell,
            AvdManager avdManager,
            ImageFactory imageFactory,
            ISdkLog log) {
        super(parentShell, 2, false);
        mAvdManager = avdManager;
        mImageFactory = imageFactory;
        mSdkLog = log;
        File hardwareDefs = null;
        SdkManager sdkMan = avdManager.getSdkManager();
        if (sdkMan != null) {
            String sdkPath = sdkMan.getLocation();
            if (sdkPath != null) {
                hardwareDefs = new File (sdkPath + File.separator +
                        SdkConstants.OS_SDK_TOOLS_LIB_FOLDER, SdkConstants.FN_HARDWARE_INI);
            }
        }
        if (hardwareDefs == null) {
            log.error(null, "Failed to load file %s from SDK", SdkConstants.FN_HARDWARE_INI);
            mHardwareMap = new HashMap<String, HardwareProperty>();
        } else {
            mHardwareMap = HardwareProperties.parseHardwareDefinitions(
                hardwareDefs, null );
        }
    }
    @Override
    public void create() {
        super.create();
        Point p = getShell().getSize();
        if (p.x < 400) {
            p.x = 400;
        }
        getShell().setSize(p);
    }
    @Override
    protected Control createContents(Composite parent) {
        Control control = super.createContents(parent);
        getShell().setText("Create new Android Virtual Device (AVD)");
        mOkButton = getButton(IDialogConstants.OK_ID);
        validatePage();
        return control;
    }
    @Override
    public void createDialogContent(final Composite parent) {
        GridData gd;
        GridLayout gl;
        Label label = new Label(parent, SWT.NONE);
        label.setText("Name:");
        String tooltip = "Name of the new Android Virtual Device";
        label.setToolTipText(tooltip);
        mAvdName = new Text(parent, SWT.BORDER);
        mAvdName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        mAvdName.addModifyListener(new CreateNameModifyListener());
        mAvdName.setToolTipText(tooltip);
        label = new Label(parent, SWT.NONE);
        label.setText("Target:");
        tooltip = "The version of Android to use in the virtual device";
        label.setToolTipText(tooltip);
        mTargetCombo = new Combo(parent, SWT.READ_ONLY | SWT.DROP_DOWN);
        mTargetCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        mTargetCombo.setToolTipText(tooltip);
        mTargetCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                super.widgetSelected(e);
                reloadSkinCombo();
                validatePage();
            }
        });
        label = new Label(parent, SWT.NONE);
        label.setText("SD Card:");
        label.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING,
                false, false));
        final Group sdCardGroup = new Group(parent, SWT.NONE);
        sdCardGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        sdCardGroup.setLayout(new GridLayout(3, false));
        mSdCardSizeRadio = new Button(sdCardGroup, SWT.RADIO);
        mSdCardSizeRadio.setText("Size:");
        mSdCardSizeRadio.setToolTipText("Create a new SD Card file");
        mSdCardSizeRadio.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                boolean sizeMode = mSdCardSizeRadio.getSelection();
                enableSdCardWidgets(sizeMode);
                validatePage();
            }
        });
        ValidateListener validateListener = new ValidateListener();
        mSdCardSize = new Text(sdCardGroup, SWT.BORDER);
        mSdCardSize.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        mSdCardSize.addVerifyListener(mDigitVerifier);
        mSdCardSize.addModifyListener(validateListener);
        mSdCardSize.setToolTipText("Size of the new SD Card file (must be at least 9 MiB)");
        mSdCardSizeCombo = new Combo(sdCardGroup, SWT.DROP_DOWN | SWT.READ_ONLY);
        mSdCardSizeCombo.add("KiB");
        mSdCardSizeCombo.add("MiB");
        mSdCardSizeCombo.select(1);
        mSdCardSizeCombo.addSelectionListener(validateListener);
        mSdCardFileRadio = new Button(sdCardGroup, SWT.RADIO);
        mSdCardFileRadio.setText("File:");
        mSdCardFileRadio.setToolTipText("Use an existing file for the SD Card");
        mSdCardFile = new Text(sdCardGroup, SWT.BORDER);
        mSdCardFile.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        mSdCardFile.addModifyListener(validateListener);
        mSdCardFile.setToolTipText("File to use for the SD Card");
        mBrowseSdCard = new Button(sdCardGroup, SWT.PUSH);
        mBrowseSdCard.setText("Browse...");
        mBrowseSdCard.setToolTipText("Select the file to use for the SD Card");
        mBrowseSdCard.addSelectionListener(new SelectionAdapter() {
           @Override
            public void widgetSelected(SelectionEvent arg0) {
               onBrowseSdCard();
               validatePage();
            }
        });
        mSdCardSizeRadio.setSelection(true);
        enableSdCardWidgets(true);
        label = new Label(parent, SWT.NONE);
        label.setText("Skin:");
        label.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING,
                false, false));
        final Group skinGroup = new Group(parent, SWT.NONE);
        skinGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        skinGroup.setLayout(new GridLayout(4, false));
        mSkinListRadio = new Button(skinGroup, SWT.RADIO);
        mSkinListRadio.setText("Built-in:");
        mSkinListRadio.setToolTipText("Select an emulated screen size provided by the current Android target");
        mSkinListRadio.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                boolean listMode = mSkinListRadio.getSelection();
                enableSkinWidgets(listMode);
                validatePage();
            }
        });
        mSkinCombo = new Combo(skinGroup, SWT.READ_ONLY | SWT.DROP_DOWN);
        mSkinCombo.setLayoutData(gd = new GridData(GridData.FILL_HORIZONTAL));
        gd.horizontalSpan = 3;
        mSkinCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                loadSkin();
            }
        });
        mSkinSizeRadio = new Button(skinGroup, SWT.RADIO);
        mSkinSizeRadio.setText("Resolution:");
        mSkinSizeRadio.setToolTipText("Select a custom emulated screen size");
        mSkinSizeWidth = new Text(skinGroup, SWT.BORDER);
        mSkinSizeWidth.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        mSkinSizeWidth.addVerifyListener(mDigitVerifier);
        mSkinSizeWidth.addModifyListener(validateListener);
        mSkinSizeWidth.setToolTipText("Width in pixels of the emulated screen size");
        new Label(skinGroup, SWT.NONE).setText("x");
        mSkinSizeHeight = new Text(skinGroup, SWT.BORDER);
        mSkinSizeHeight.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        mSkinSizeHeight.addVerifyListener(mDigitVerifier);
        mSkinSizeHeight.addModifyListener(validateListener);
        mSkinSizeHeight.setToolTipText("Height in pixels of the emulated screen size");
        mSkinListRadio.setSelection(true);
        enableSkinWidgets(true);
        label = new Label(parent, SWT.NONE);
        label.setText("Hardware:");
        label.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING,
                false, false));
        final Group hwGroup = new Group(parent, SWT.NONE);
        hwGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        hwGroup.setLayout(new GridLayout(2, false));
        createHardwareTable(hwGroup);
        Composite hwButtons = new Composite(hwGroup, SWT.NONE);
        hwButtons.setLayoutData(new GridData(GridData.FILL_VERTICAL));
        hwButtons.setLayout(gl = new GridLayout(1, false));
        gl.marginHeight = gl.marginWidth = 0;
        Button b = new Button(hwButtons, SWT.PUSH | SWT.FLAT);
        b.setText("New...");
        b.setToolTipText("Add a new hardware property");
        b.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        b.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                HardwarePropertyChooser dialog = new HardwarePropertyChooser(parent.getShell(),
                        mHardwareMap, mProperties.keySet());
                if (dialog.open() == Window.OK) {
                    HardwareProperty choice = dialog.getProperty();
                    if (choice != null) {
                        mProperties.put(choice.getName(), choice.getDefault());
                        mHardwareViewer.refresh();
                    }
                }
            }
        });
        mDeleteHardwareProp = new Button(hwButtons, SWT.PUSH | SWT.FLAT);
        mDeleteHardwareProp.setText("Delete");
        mDeleteHardwareProp.setToolTipText("Delete the selected hardware property");
        mDeleteHardwareProp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        mDeleteHardwareProp.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                ISelection selection = mHardwareViewer.getSelection();
                if (selection instanceof IStructuredSelection) {
                    String hwName = (String)((IStructuredSelection)selection).getFirstElement();
                    mProperties.remove(hwName);
                    mHardwareViewer.refresh();
                }
            }
        });
        mDeleteHardwareProp.setEnabled(false);
        mForceCreation = new Button(parent, SWT.CHECK);
        mForceCreation.setText("Override the existing AVD with the same name");
        mForceCreation.setToolTipText("There's already an AVD with the same name. Check this to delete it and replace it by the new AVD.");
        mForceCreation.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER,
                true, false, 2, 1));
        mForceCreation.setEnabled(false);
        mForceCreation.addSelectionListener(validateListener);
        label = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
        label.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 3, 1));
        mStatusComposite = new Composite(parent, SWT.NONE);
        mStatusComposite.setLayoutData(new GridData(GridData.FILL, GridData.CENTER,
                true, false, 3, 1));
        mStatusComposite.setLayout(gl = new GridLayout(2, false));
        gl.marginHeight = gl.marginWidth = 0;
        mStatusIcon = new Label(mStatusComposite, SWT.NONE);
        mStatusIcon.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING,
                false, false));
        mStatusLabel = new Label(mStatusComposite, SWT.NONE);
        mStatusLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        mStatusLabel.setText(" \n "); 
        reloadTargetCombo();
    }
    private void createHardwareTable(Composite parent) {
        final Table hardwareTable = new Table(parent, SWT.SINGLE | SWT.FULL_SELECTION);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL);
        gd.widthHint = 200;
        gd.heightHint = 100;
        hardwareTable.setLayoutData(gd);
        hardwareTable.setHeaderVisible(true);
        hardwareTable.setLinesVisible(true);
        mHardwareViewer = new TableViewer(hardwareTable);
        mHardwareViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
                mDeleteHardwareProp.setEnabled(hardwareTable.getSelectionIndex() != -1);
            }
        });
        mHardwareViewer.setContentProvider(new IStructuredContentProvider() {
            public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            }
            public Object[] getElements(Object arg0) {
                return mProperties.keySet().toArray();
            }
            public void dispose() {
            }
        });
        TableColumn col1 = new TableColumn(hardwareTable, SWT.LEFT);
        col1.setText("Property");
        col1.setWidth(150);
        TableViewerColumn tvc1 = new TableViewerColumn(mHardwareViewer, col1);
        tvc1.setLabelProvider(new CellLabelProvider() {
            @Override
            public void update(ViewerCell cell) {
                HardwareProperty prop = mHardwareMap.get(cell.getElement());
                cell.setText(prop != null ? prop.getAbstract() : "");
            }
        });
        TableColumn col2 = new TableColumn(hardwareTable, SWT.LEFT);
        col2.setText("Value");
        col2.setWidth(50);
        TableViewerColumn tvc2 = new TableViewerColumn(mHardwareViewer, col2);
        tvc2.setLabelProvider(new CellLabelProvider() {
            @Override
            public void update(ViewerCell cell) {
                String value = mProperties.get(cell.getElement());
                cell.setText(value != null ? value : "");
            }
        });
        tvc2.setEditingSupport(new EditingSupport(mHardwareViewer) {
            @Override
            protected void setValue(Object element, Object value) {
                String hardwareName = (String)element;
                HardwareProperty property = mHardwareMap.get(hardwareName);
                switch (property.getType()) {
                    case INTEGER:
                        mProperties.put((String)element, (String)value);
                        break;
                    case DISKSIZE:
                        if (HardwareProperties.DISKSIZE_PATTERN.matcher((String)value).matches()) {
                            mProperties.put((String)element, (String)value);
                        }
                        break;
                    case BOOLEAN:
                        int index = (Integer)value;
                        mProperties.put((String)element, HardwareProperties.BOOLEAN_VALUES[index]);
                        break;
                }
                mHardwareViewer.refresh(element);
            }
            @Override
            protected Object getValue(Object element) {
                String hardwareName = (String)element;
                HardwareProperty property = mHardwareMap.get(hardwareName);
                String value = mProperties.get(hardwareName);
                switch (property.getType()) {
                    case INTEGER:
                    case DISKSIZE:
                        return value;
                    case BOOLEAN:
                        return HardwareProperties.getBooleanValueIndex(value);
                }
                return null;
            }
            @Override
            protected CellEditor getCellEditor(Object element) {
                String hardwareName = (String)element;
                HardwareProperty property = mHardwareMap.get(hardwareName);
                switch (property.getType()) {
                    case INTEGER:
                    case DISKSIZE:
                        return new TextCellEditor(hardwareTable);
                    case BOOLEAN:
                        return new ComboBoxCellEditor(hardwareTable,
                                HardwareProperties.BOOLEAN_VALUES,
                                SWT.READ_ONLY | SWT.DROP_DOWN);
                }
                return null;
            }
            @Override
            protected boolean canEdit(Object element) {
                String hardwareName = (String)element;
                HardwareProperty property = mHardwareMap.get(hardwareName);
                return property != null;
            }
        });
        mHardwareViewer.setInput(mProperties);
    }
    @Override
    protected Button createButton(Composite parent, int id, String label, boolean defaultButton) {
        if (id == IDialogConstants.OK_ID) {
            label = "Create AVD";
        }
        return super.createButton(parent, id, label, defaultButton);
    }
    @Override
    protected void okPressed() {
        if (createAvd()) {
            super.okPressed();
        }
    }
    private void enableSdCardWidgets(boolean sizeMode) {
        mSdCardSize.setEnabled(sizeMode);
        mSdCardSizeCombo.setEnabled(sizeMode);
        mSdCardFile.setEnabled(!sizeMode);
        mBrowseSdCard.setEnabled(!sizeMode);
    }
    private void enableSkinWidgets(boolean listMode) {
        mSkinCombo.setEnabled(listMode);
        mSkinSizeWidth.setEnabled(!listMode);
        mSkinSizeHeight.setEnabled(!listMode);
    }
    private void onBrowseSdCard() {
        FileDialog dlg = new FileDialog(getContents().getShell(), SWT.OPEN);
        dlg.setText("Choose SD Card image file.");
        String fileName = dlg.open();
        if (fileName != null) {
            mSdCardFile.setText(fileName);
        }
    }
    private void reloadTargetCombo() {
        String selected = null;
        int index = mTargetCombo.getSelectionIndex();
        if (index >= 0) {
            selected = mTargetCombo.getItem(index);
        }
        mCurrentTargets.clear();
        mTargetCombo.removeAll();
        boolean found = false;
        index = -1;
        SdkManager sdkManager = mAvdManager.getSdkManager();
        if (sdkManager != null) {
            for (IAndroidTarget target : sdkManager.getTargets()) {
                String name;
                if (target.isPlatform()) {
                    name = String.format("%s - API Level %s",
                            target.getName(),
                            target.getVersion().getApiString());
                } else {
                    name = String.format("%s (%s) - API Level %s",
                            target.getName(),
                            target.getVendor(),
                            target.getVersion().getApiString());
                }
                mCurrentTargets.put(name, target);
                mTargetCombo.add(name);
                if (!found) {
                    index++;
                    found = name.equals(selected);
                }
            }
        }
        mTargetCombo.setEnabled(mCurrentTargets.size() > 0);
        if (found) {
            mTargetCombo.select(index);
        }
        reloadSkinCombo();
    }
    private void reloadSkinCombo() {
        String selected = null;
        int index = mSkinCombo.getSelectionIndex();
        if (index >= 0) {
            selected = mSkinCombo.getItem(index);
        }
        mSkinCombo.removeAll();
        mSkinCombo.setEnabled(false);
        index = mTargetCombo.getSelectionIndex();
        if (index >= 0) {
            String targetName = mTargetCombo.getItem(index);
            boolean found = false;
            IAndroidTarget target = mCurrentTargets.get(targetName);
            if (target != null) {
                mSkinCombo.add(String.format("Default (%s)", target.getDefaultSkin()));
                index = -1;
                for (String skin : target.getSkins()) {
                    mSkinCombo.add(skin);
                    if (!found) {
                        index++;
                        found = skin.equals(selected);
                    }
                }
                mSkinCombo.setEnabled(true);
                if (found) {
                    mSkinCombo.select(index);
                } else {
                    mSkinCombo.select(0);  
                    loadSkin();
                }
            }
        }
    }
    private void validatePage() {
        String error = null;
        String avdName = mAvdName.getText().trim();
        boolean hasAvdName = avdName.length() > 0;
        if (hasAvdName && !AvdManager.RE_AVD_NAME.matcher(avdName).matches()) {
            error = String.format(
                "AVD name '%1$s' contains invalid characters.\nAllowed characters are: %2$s",
                avdName, AvdManager.CHARS_AVD_NAME);
        }
        if (hasAvdName && error == null && mTargetCombo.getSelectionIndex() < 0) {
            error = "A target must be selected in order to create an AVD.";
        }
        if (error == null) {
            boolean sdcardFileMode = mSdCardFileRadio.getSelection();
            if (sdcardFileMode) {
                String sdName = mSdCardFile.getText().trim();
                if (sdName.length() > 0 && !new File(sdName).isFile()) {
                    error = "SD Card path isn't valid.";
                }
            } else {
                String valueString = mSdCardSize.getText();
                if (valueString.length() > 0) {
                    if (valueString.length() >= 10 +
                            (mSdCardSizeCombo.getSelectionIndex() == 0 ? 3 : 0)) {
                        error = "SD Card size is too big!";
                    } else {
                        try {
                            long value = Long.parseLong(valueString);
                            switch (mSdCardSizeCombo.getSelectionIndex()) {
                                case 0:
                                    value *= 1024L;
                                    break;
                                case 1:
                                    value *= 1024L * 1024L;
                                    break;
                            }
                            if (value < 9 * 1024 * 1024) {
                                error = "SD Card size must be at least 9 MiB";
                            }
                        } catch (NumberFormatException e) {
                        }
                    }
                }
            }
        }
        if (error == null) {
            if (mSkinSizeRadio.getSelection()) {
                String width = mSkinSizeWidth.getText();   
                String height = mSkinSizeHeight.getText(); 
                if (width.length() == 0 || height.length() == 0) {
                    error = "Skin size is incorrect.\nBoth dimensions must be > 0";
                }
            }
        }
        if (hasAvdName && error == null) {
            AvdInfo avdMatch = mAvdManager.getAvd(avdName, false );
            if (avdMatch != null && !mForceCreation.getSelection()) {
                error = String.format(
                        "The AVD name '%s' is already used.\n" +
                        "Check \"Override the existing AVD\" to delete the existing one.",
                        avdName);
            }
        }
        boolean can_create = hasAvdName && error == null;
        if (can_create) {
            can_create &= mTargetCombo.getSelectionIndex() >= 0;
        }
        mOkButton.setEnabled(can_create);
        if (error != null) {
            mStatusIcon.setImage(mImageFactory.getImageByName("reject_icon16.png"));
            mStatusLabel.setText(error);
        } else {
            mStatusIcon.setImage(null);
            mStatusLabel.setText(" \n "); 
        }
        mStatusComposite.pack(true);
    }
    private void loadSkin() {
        int targetIndex = mTargetCombo.getSelectionIndex();
        if (targetIndex < 0) {
            return;
        }
        String targetName = mTargetCombo.getItem(targetIndex);
        IAndroidTarget target = mCurrentTargets.get(targetName);
        if (target == null) {
            return;
        }
        String skinName = null;
        int skinIndex = mSkinCombo.getSelectionIndex();
        if (skinIndex < 0) {
            return;
        } else if (skinIndex == 0) { 
            skinName = target.getDefaultSkin();
        } else {
            skinName = mSkinCombo.getItem(skinIndex);
        }
        String path = target.getPath(IAndroidTarget.SKINS);
        File skin = new File(path, skinName);
        if (skin.isDirectory() == false && target.isPlatform() == false) {
            path = target.getParent().getPath(IAndroidTarget.SKINS);
            skin = new File(path, skinName);
        }
        if (skin.isDirectory() == false) {
            return;
        }
        HashMap<String, String> hardwareValues = new HashMap<String, String>();
        if (target.isPlatform() == false) {
            File targetHardwareFile = new File(target.getLocation(), AvdManager.HARDWARE_INI);
            if (targetHardwareFile.isFile()) {
                Map<String, String> targetHardwareConfig = SdkManager.parsePropertyFile(
                        targetHardwareFile, null );
                if (targetHardwareConfig != null) {
                    hardwareValues.putAll(targetHardwareConfig);
                }
            }
        }
        File skinHardwareFile = new File(skin, AvdManager.HARDWARE_INI);
        if (skinHardwareFile.isFile()) {
            Map<String, String> skinHardwareConfig = SdkManager.parsePropertyFile(
                    skinHardwareFile, null );
            if (skinHardwareConfig != null) {
                hardwareValues.putAll(skinHardwareConfig);
            }
        }
        for (Entry<String, String> entry : hardwareValues.entrySet()) {
            if (mEditedProperties.contains(entry.getKey()) == false) {
                mProperties.put(entry.getKey(), entry.getValue());
            }
        }
        mHardwareViewer.refresh();
    }
    private boolean createAvd() {
        String avdName = mAvdName.getText().trim();
        int targetIndex = mTargetCombo.getSelectionIndex();
        if (avdName.length() == 0 || targetIndex < 0) {
            return false;
        }
        String targetName = mTargetCombo.getItem(targetIndex);
        IAndroidTarget target = mCurrentTargets.get(targetName);
        if (target == null) {
            return false;
        }
        String sdName = null;
        if (mSdCardSizeRadio.getSelection()) {
            String value = mSdCardSize.getText().trim();
            if (value.length() > 0) {
                sdName = value;
                switch (mSdCardSizeCombo.getSelectionIndex()) {
                    case 0:
                        sdName += "K";
                        break;
                    case 1:
                        sdName += "M";
                        break;
                    default:
                        assert false;
                }
            }
        } else {
            sdName = mSdCardFile.getText().trim();
        }
        String skinName = null;
        if (mSkinListRadio.getSelection()) {
            int skinIndex = mSkinCombo.getSelectionIndex();
            if (skinIndex > 0) {
                skinName = mSkinCombo.getItem(skinIndex);
            }
        } else {
            skinName = mSkinSizeWidth.getText() + "x" + mSkinSizeHeight.getText(); 
        }
        ISdkLog log = mSdkLog;
        if (log == null || log instanceof MessageBoxLog) {
            log = new MessageBoxLog(
                    String.format("Result of creating AVD '%s':", avdName),
                    getContents().getDisplay(),
                    false );
        }
        File avdFolder = null;
        try {
            avdFolder = new File(
                    AndroidLocation.getFolder() + AndroidLocation.FOLDER_AVD,
                    avdName + AvdManager.AVD_FOLDER_EXTENSION);
        } catch (AndroidLocationException e) {
            return false;
        }
        boolean force = mForceCreation.getSelection();
        boolean success = false;
        AvdInfo avdInfo = mAvdManager.createAvd(
                avdFolder,
                avdName,
                target,
                skinName,
                sdName,
                mProperties,
                force,
                log);
        success = avdInfo != null;
        if (log instanceof MessageBoxLog) {
            ((MessageBoxLog) log).displayResult(success);
        }
        return success;
    }
}
