final class AvdStartDialog extends GridDialog {
    private static boolean sWipeData = false;
    private static int sMonitorDpi = 72; 
    private static final Map<String, String> sSkinScaling = new HashMap<String, String>();
    private static final Pattern sScreenSizePattern = Pattern.compile("\\d*(\\.\\d?)?");
    private final AvdInfo mAvd;
    private final String mSdkLocation;
    private final SettingsController mSettingsController;
    private Text mScreenSize;
    private Text mMonitorDpi;
    private Button mScaleButton;
    private float mScale = 0.f;
    private boolean mWipeData = false;
    private int mDensity = 160; 
    private int mSize1 = -1;
    private int mSize2 = -1;
    private String mSkinDisplay;
    private boolean mEnableScaling = true;
    private Label mScaleField;
    AvdStartDialog(Shell parentShell, AvdInfo avd, String sdkLocation,
            SettingsController settingsController) {
        super(parentShell, 2, false);
        mAvd = avd;
        mSdkLocation = sdkLocation;
        mSettingsController = settingsController;
        if (mAvd == null) {
            throw new IllegalArgumentException("avd cannot be null");
        }
        if (mSdkLocation == null) {
            throw new IllegalArgumentException("sdkLocation cannot be null");
        }
        computeSkinData();
    }
    public boolean getWipeData() {
        return mWipeData;
    }
    public float getScale() {
        return mScale;
    }
    @Override
    public void createDialogContent(final Composite parent) {
        GridData gd;
        Label l = new Label(parent, SWT.NONE);
        l.setText("Skin:");
        l = new Label(parent, SWT.NONE);
        l.setText(mSkinDisplay == null ? "None" : mSkinDisplay);
        l.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        l = new Label(parent, SWT.NONE);
        l.setText("Density:");
        l = new Label(parent, SWT.NONE);
        l.setText(getDensityText());
        l.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        mScaleButton = new Button(parent, SWT.CHECK);
        mScaleButton.setText("Scale display to real size");
        mScaleButton.setEnabled(mEnableScaling);
        boolean defaultState = mEnableScaling && sSkinScaling.get(mAvd.getName()) != null;
        mScaleButton.setSelection(defaultState);
        mScaleButton.setLayoutData(gd = new GridData(GridData.FILL_HORIZONTAL));
        gd.horizontalSpan = 2;
        final Group scaleGroup = new Group(parent, SWT.NONE);
        scaleGroup.setLayoutData(gd = new GridData(GridData.FILL_HORIZONTAL));
        gd.horizontalIndent = 30;
        gd.horizontalSpan = 2;
        scaleGroup.setLayout(new GridLayout(3, false));
        l = new Label(scaleGroup, SWT.NONE);
        l.setText("Screen Size (in):");
        mScreenSize = new Text(scaleGroup, SWT.BORDER);
        mScreenSize.setText(getScreenSize());
        mScreenSize.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        mScreenSize.addVerifyListener(new VerifyListener() {
            public void verifyText(VerifyEvent event) {
                String text = mScreenSize.getText();
                text = text.substring(0, event.start) + event.text + text.substring(event.end);
                event.doit = sScreenSizePattern.matcher(text).matches();
            }
        });
        mScreenSize.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                onScaleChange();
            }
        });
        new Composite(scaleGroup, SWT.NONE).setLayoutData(gd = new GridData());
        gd.widthHint = gd.heightHint = 0;
        l = new Label(scaleGroup, SWT.NONE);
        l.setText("Monitor dpi:");
        mMonitorDpi = new Text(scaleGroup, SWT.BORDER);
        mMonitorDpi.setText(Integer.toString(getMonitorDpi()));
        mMonitorDpi.setLayoutData(gd = new GridData(GridData.FILL_HORIZONTAL));
        gd.widthHint = 50;
        mMonitorDpi.addVerifyListener(new VerifyListener() {
            public void verifyText(VerifyEvent event) {
                for (int i = 0 ; i < event.text.length(); i++) {
                    char letter = event.text.charAt(i);
                    if (letter < '0' || letter > '9') {
                        event.doit = false;
                        return;
                    }
                }
            }
        });
        mMonitorDpi.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                onScaleChange();
            }
        });
        Button button = new Button(scaleGroup, SWT.PUSH | SWT.FLAT);
        button.setText("?");
        button.setToolTipText("Click to figure out your monitor's pixel density");
        button.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                ResolutionChooserDialog dialog = new ResolutionChooserDialog(parent.getShell());
                if (dialog.open() == Window.OK) {
                    mMonitorDpi.setText(Integer.toString(dialog.getDensity()));
                }
            }
        });
        l = new Label(scaleGroup, SWT.NONE);
        l.setText("Scale:");
        mScaleField = new Label(scaleGroup, SWT.NONE);
        mScaleField.setLayoutData(new GridData(GridData.FILL, GridData.CENTER,
                true ,
                true ,
                2 ,
                1 ));
        setScale(mScale); 
        enableGroup(scaleGroup, defaultState);
        mScaleButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                boolean enabled = mScaleButton.getSelection();
                enableGroup(scaleGroup, enabled);
                if (enabled) {
                    onScaleChange();
                } else {
                    setScale(0);
                }
            }
        });
        final Button wipeButton = new Button(parent, SWT.CHECK);
        wipeButton.setLayoutData(gd = new GridData(GridData.FILL_HORIZONTAL));
        gd.horizontalSpan = 2;
        wipeButton.setText("Wipe user data");
        wipeButton.setSelection(mWipeData = sWipeData);
        wipeButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                mWipeData = wipeButton.getSelection();
            }
        });
        l = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
        l.setLayoutData(gd = new GridData(GridData.FILL_HORIZONTAL));
        gd.horizontalSpan = 2;
        if (defaultState) {
            onScaleChange();
        }
    }
    private void enableGroup(final Group group, boolean enabled) {
        group.setEnabled(enabled);
        for (Control c : group.getChildren()) {
            c.setEnabled(enabled);
        }
    }
    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("Launch Options");
    }
    @Override
    protected Button createButton(Composite parent, int id, String label, boolean defaultButton) {
        if (id == IDialogConstants.OK_ID) {
            label = "Launch";
        }
        return super.createButton(parent, id, label, defaultButton);
    }
    @Override
    protected void okPressed() {
        String dpi = mMonitorDpi.getText();
        if (dpi.length() > 0) {
            sMonitorDpi = Integer.parseInt(dpi);
            if (mSettingsController != null) {
                mSettingsController.setMonitorDensity(sMonitorDpi);
                mSettingsController.saveSettings();
            }
        }
        String key = mAvd.getName();
        sSkinScaling.remove(key);
        if (mScaleButton.getSelection()) {
            String size = mScreenSize.getText();
            if (size.length() > 0) {
                sSkinScaling.put(key, size);
            }
        }
        sWipeData = mWipeData;
        super.okPressed();
    }
    private void computeSkinData() {
        Map<String, String> prop = mAvd.getProperties();
        String dpi = prop.get("hw.lcd.density");
        if (dpi != null && dpi.length() > 0) {
            mDensity  = Integer.parseInt(dpi);
        }
        findSkinResolution();
    }
    private void onScaleChange() {
        String sizeStr = mScreenSize.getText();
        if (sizeStr.length() == 0) {
            setScale(0);
            return;
        }
        String dpiStr = mMonitorDpi.getText();
        if (dpiStr.length() == 0) {
            setScale(0);
            return;
        }
        int dpi = Integer.parseInt(dpiStr);
        float size = Float.parseFloat(sizeStr);
        float diagonalPx = (float)Math.sqrt(mSize1*mSize1+mSize2*mSize2);
        setScale((size * dpi) / diagonalPx);
    }
    private void setScale(float scale) {
        mScale = scale;
        scale = Math.round(scale * 100);
        scale /=  100.f;
        if (scale == 0.f) {
            mScaleField.setText("default");  
        } else {
            mScaleField.setText(String.format("%.2f", scale));  
        }
    }
    private int getMonitorDpi() {
        if (mSettingsController != null) {
            sMonitorDpi = mSettingsController.getMonitorDensity();
        }
        if (sMonitorDpi == -1) { 
            sMonitorDpi = Toolkit.getDefaultToolkit().getScreenResolution();
        }
        return sMonitorDpi;
    }
    private String getScreenSize() {
        String size = sSkinScaling.get(mAvd.getName());
        if (size != null) {
            return size;
        }
        return "3";
    }
    private String getDensityText() {
        switch (mDensity) {
            case 120:
                return "Low (120)";
            case 160:
                return "Medium (160)";
            case 240:
                return "High (240)";
        }
        return Integer.toString(mDensity);
    }
    private void findSkinResolution() {
        Map<String, String> prop = mAvd.getProperties();
        String skinName = prop.get(AvdManager.AVD_INI_SKIN_NAME);
        if (skinName != null) {
            Matcher m = AvdManager.NUMERIC_SKIN_SIZE.matcher(skinName);
            if (m != null && m.matches()) {
                mSize1 = Integer.parseInt(m.group(1));
                mSize2 = Integer.parseInt(m.group(2));
                mSkinDisplay = skinName;
                mEnableScaling = true;
                return;
            }
        }
        mEnableScaling = false; 
        String skinPath = prop.get(AvdManager.AVD_INI_SKIN_PATH);
        if (skinPath != null) {
            File skinFolder = new File(mSdkLocation, skinPath);
            if (skinFolder.isDirectory()) {
                File layoutFile = new File(skinFolder, "layout");
                if (layoutFile.isFile()) {
                    if (parseLayoutFile(layoutFile)) {
                        mSkinDisplay = String.format("%1$s (%2$dx%3$d)", skinName, mSize1, mSize2);
                        mEnableScaling = true;
                    } else {
                        mSkinDisplay = skinName;
                    }
                }
            }
        }
    }
    private boolean parseLayoutFile(File layoutFile) {
        try {
            BufferedReader input = new BufferedReader(new FileReader(layoutFile));
            String line;
            while ((line = input.readLine()) != null) {
                line = line.trim();
                int len = line.length();
                if (len == 0) continue;
                if (line.charAt(len-1) == '{') {
                    String[] tokens = line.split(" ");
                    if ("display".equals(tokens[0])) {
                        while ((mSize1 == -1 || mSize2 == -1) &&
                                (line = input.readLine()) != null) {
                            line = line.trim();
                            len = line.length();
                            if (len == 0) continue;
                            if ("}".equals(line)) { 
                                break;
                            }
                            tokens = line.split(" ");
                            if (tokens.length >= 2) {
                                if ("width".equals(tokens[0])) {
                                    mSize1 = Integer.parseInt(tokens[tokens.length-1]);
                                } else if ("height".equals(tokens[0])) {
                                    mSize2 = Integer.parseInt(tokens[tokens.length-1]);
                                }
                            }
                        }
                        return mSize1 != -1 && mSize2 != -1;
                    }
                }
            }
        } catch (IOException e) {
        }
        return false;
    }
}
