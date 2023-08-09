public class EmulatorConfigTab extends AbstractLaunchConfigurationTab {
    private final static String[][] NETWORK_SPEEDS = new String[][] {
        { "Full", "full" }, 
        { "GSM", "gsm" }, 
        { "HSCSD", "hscsd" }, 
        { "GPRS", "gprs" }, 
        { "EDGE", "edge" }, 
        { "UMTS", "umts" }, 
        { "HSPDA", "hsdpa" }, 
    };
    private final static String[][] NETWORK_LATENCIES = new String[][] {
        { "None", "none" }, 
        { "GPRS", "gprs" }, 
        { "EDGE", "edge" }, 
        { "UMTS", "umts" }, 
    };
    private Button mAutoTargetButton;
    private Button mManualTargetButton;
    private AvdSelector mPreferredAvdSelector;
    private Combo mSpeedCombo;
    private Combo mDelayCombo;
    private Group mEmulatorOptionsGroup;
    private Text mEmulatorCLOptions;
    private Button mWipeDataButton;
    private Button mNoBootAnimButton;
    private Label mPreferredAvdLabel;
    private IAndroidTarget mProjectTarget;
    public static String getSpeed(int value) {
        try {
            return NETWORK_SPEEDS[value][1];
        } catch (ArrayIndexOutOfBoundsException e) {
            return NETWORK_SPEEDS[LaunchConfigDelegate.DEFAULT_SPEED][1];
        }
    }
    public static String getDelay(int value) {
        try {
            return NETWORK_LATENCIES[value][1];
        } catch (ArrayIndexOutOfBoundsException e) {
            return NETWORK_LATENCIES[LaunchConfigDelegate.DEFAULT_DELAY][1];
        }
    }
    public EmulatorConfigTab() {
    }
    public void createControl(Composite parent) {
        Font font = parent.getFont();
        try {
            Sdk.getCurrent().getAvdManager().reloadAvds(NullSdkLog.getLogger());
        } catch (AndroidLocationException e1) {
        }
        Composite topComp = new Composite(parent, SWT.NONE);
        setControl(topComp);
        GridLayout topLayout = new GridLayout();
        topLayout.numColumns = 1;
        topLayout.verticalSpacing = 0;
        topComp.setLayout(topLayout);
        topComp.setFont(font);
        GridData gd;
        GridLayout layout;
        Group targetModeGroup = new Group(topComp, SWT.NONE);
        targetModeGroup.setText("Deployment Target Selection Mode");
        gd = new GridData(GridData.FILL_HORIZONTAL);
        targetModeGroup.setLayoutData(gd);
        layout = new GridLayout();
        layout.numColumns = 1;
        targetModeGroup.setLayout(layout);
        targetModeGroup.setFont(font);
        mManualTargetButton = new Button(targetModeGroup, SWT.RADIO);
        mManualTargetButton.setText("Manual");
        mAutoTargetButton = new Button(targetModeGroup, SWT.RADIO);
        mAutoTargetButton.setText("Automatic");
        mAutoTargetButton.setSelection(true);
        mAutoTargetButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                updateLaunchConfigurationDialog();
                boolean auto = mAutoTargetButton.getSelection();
                mPreferredAvdSelector.setEnabled(auto);
                mPreferredAvdLabel.setEnabled(auto);
            }
        });
        Composite offsetComp = new Composite(targetModeGroup, SWT.NONE);
        offsetComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        layout = new GridLayout(1, false);
        layout.marginRight = layout.marginHeight = 0;
        layout.marginLeft = 30;
        offsetComp.setLayout(layout);
        mPreferredAvdLabel = new Label(offsetComp, SWT.NONE);
        mPreferredAvdLabel.setText("Select a preferred Android Virtual Device for deployment:");
        mPreferredAvdSelector = new AvdSelector(offsetComp,
                Sdk.getCurrent().getSdkLocation(),
                null ,
                DisplayMode.SIMPLE_CHECK,
                new AdtConsoleSdkLog());
        mPreferredAvdSelector.setTableHeightHint(100);
        mPreferredAvdSelector.setSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                updateLaunchConfigurationDialog();
            }
        });
        mEmulatorOptionsGroup = new Group(topComp, SWT.NONE);
        mEmulatorOptionsGroup.setText("Emulator launch parameters:");
        mEmulatorOptionsGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        layout = new GridLayout();
        layout.numColumns = 2;
        mEmulatorOptionsGroup.setLayout(layout);
        mEmulatorOptionsGroup.setFont(font);
        new Label(mEmulatorOptionsGroup, SWT.NONE).setText("Network Speed:");
        mSpeedCombo = new Combo(mEmulatorOptionsGroup, SWT.READ_ONLY);
        for (String[] speed : NETWORK_SPEEDS) {
            mSpeedCombo.add(speed[0]);
        }
        mSpeedCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                updateLaunchConfigurationDialog();
            }
        });
        mSpeedCombo.pack();
        new Label(mEmulatorOptionsGroup, SWT.NONE).setText("Network Latency:");
        mDelayCombo = new Combo(mEmulatorOptionsGroup, SWT.READ_ONLY);
        for (String[] delay : NETWORK_LATENCIES) {
            mDelayCombo.add(delay[0]);
        }
        mDelayCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                updateLaunchConfigurationDialog();
            }
        });
        mDelayCombo.pack();
        mWipeDataButton = new Button(mEmulatorOptionsGroup, SWT.CHECK);
        mWipeDataButton.setText("Wipe User Data");
        mWipeDataButton.setToolTipText("Check this if you want to wipe your user data each time you start the emulator. You will be prompted for confirmation when the emulator starts.");
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        mWipeDataButton.setLayoutData(gd);
        mWipeDataButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                updateLaunchConfigurationDialog();
            }
        });
        mNoBootAnimButton = new Button(mEmulatorOptionsGroup, SWT.CHECK);
        mNoBootAnimButton.setText("Disable Boot Animation");
        mNoBootAnimButton.setToolTipText("Check this if you want to disable the boot animation. This can help the emulator start faster on slow machines.");
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        mNoBootAnimButton.setLayoutData(gd);
        mNoBootAnimButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                updateLaunchConfigurationDialog();
            }
        });
        Label l = new Label(mEmulatorOptionsGroup, SWT.NONE);
        l.setText("Additional Emulator Command Line Options");
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        l.setLayoutData(gd);
        mEmulatorCLOptions = new Text(mEmulatorOptionsGroup, SWT.BORDER);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        mEmulatorCLOptions.setLayoutData(gd);
        mEmulatorCLOptions.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                updateLaunchConfigurationDialog();
            }
        });
    }
    public String getName() {
        return "Target";
    }
    @Override
    public Image getImage() {
        return DdmsPlugin.getImageLoader().loadImage("emulator.png", null); 
    }
    private void updateAvdList(AvdManager avdManager) {
        if (avdManager == null) {
            avdManager = Sdk.getCurrent().getAvdManager();
        }
        mPreferredAvdSelector.setManager(avdManager);
        mPreferredAvdSelector.setFilter(mProjectTarget);
        mPreferredAvdSelector.refresh(false);
    }
    public void initializeFrom(ILaunchConfiguration configuration) {
        AvdManager avdManager = Sdk.getCurrent().getAvdManager();
        TargetMode mode = LaunchConfigDelegate.DEFAULT_TARGET_MODE; 
        try {
            mode = TargetMode.getMode(configuration.getAttribute(
                    LaunchConfigDelegate.ATTR_TARGET_MODE, mode.getValue()));
        } catch (CoreException e) {
        }
        mAutoTargetButton.setSelection(mode.getValue());
        mManualTargetButton.setSelection(!mode.getValue());
        String stringValue = "";
        try {
            stringValue = configuration.getAttribute(
                    IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, stringValue);
        } catch (CoreException ce) {
        }
        IProject project = null;
        IJavaProject[] projects = BaseProjectHelper.getAndroidProjects(null );
        if (projects != null) {
            for (IJavaProject p : projects) {
                if (p.getElementName().equals(stringValue)) {
                    project = p.getProject();
                    break;
                }
            }
        }
        if (project != null) {
            mProjectTarget = Sdk.getCurrent().getTarget(project);
        }
        updateAvdList(avdManager);
        stringValue = "";
        try {
            stringValue = configuration.getAttribute(LaunchConfigDelegate.ATTR_AVD_NAME,
                    stringValue);
        } catch (CoreException e) {
        }
        if (stringValue != null && stringValue.length() > 0 && avdManager != null) {
            AvdInfo targetAvd = avdManager.getAvd(stringValue, true );
            mPreferredAvdSelector.setSelection(targetAvd);
        } else {
            mPreferredAvdSelector.setSelection(null);
        }
        boolean value = LaunchConfigDelegate.DEFAULT_WIPE_DATA;
        try {
            value = configuration.getAttribute(LaunchConfigDelegate.ATTR_WIPE_DATA, value);
        } catch (CoreException e) {
        }
        mWipeDataButton.setSelection(value);
        value = LaunchConfigDelegate.DEFAULT_NO_BOOT_ANIM;
        try {
            value = configuration.getAttribute(LaunchConfigDelegate.ATTR_NO_BOOT_ANIM, value);
        } catch (CoreException e) {
        }
        mNoBootAnimButton.setSelection(value);
        int index = -1;
        index = LaunchConfigDelegate.DEFAULT_SPEED;
        try {
            index = configuration.getAttribute(LaunchConfigDelegate.ATTR_SPEED,
                    index);
        } catch (CoreException e) {
        }
        if (index == -1) {
            mSpeedCombo.clearSelection();
        } else {
            mSpeedCombo.select(index);
        }
        index = LaunchConfigDelegate.DEFAULT_DELAY;
        try {
            index = configuration.getAttribute(LaunchConfigDelegate.ATTR_DELAY,
                    index);
        } catch (CoreException e) {
        }
        if (index == -1) {
            mDelayCombo.clearSelection();
        } else {
            mDelayCombo.select(index);
        }
        String commandLine = null;
        try {
            commandLine = configuration.getAttribute(
                    LaunchConfigDelegate.ATTR_COMMANDLINE, ""); 
        } catch (CoreException e) {
        }
        if (commandLine != null) {
            mEmulatorCLOptions.setText(commandLine);
        }
    }
    public void performApply(ILaunchConfigurationWorkingCopy configuration) {
        configuration.setAttribute(LaunchConfigDelegate.ATTR_TARGET_MODE,
                mAutoTargetButton.getSelection());
        AvdInfo avd = mPreferredAvdSelector.getSelected();
        if (avd != null) {
            configuration.setAttribute(LaunchConfigDelegate.ATTR_AVD_NAME, avd.getName());
        } else {
            configuration.setAttribute(LaunchConfigDelegate.ATTR_AVD_NAME, (String)null);
        }
        configuration.setAttribute(LaunchConfigDelegate.ATTR_SPEED,
                mSpeedCombo.getSelectionIndex());
        configuration.setAttribute(LaunchConfigDelegate.ATTR_DELAY,
                mDelayCombo.getSelectionIndex());
        configuration.setAttribute(LaunchConfigDelegate.ATTR_COMMANDLINE,
                mEmulatorCLOptions.getText());
        configuration.setAttribute(LaunchConfigDelegate.ATTR_WIPE_DATA,
                mWipeDataButton.getSelection());
        configuration.setAttribute(LaunchConfigDelegate.ATTR_NO_BOOT_ANIM,
                mNoBootAnimButton.getSelection());
   }
    public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
        configuration.setAttribute(LaunchConfigDelegate.ATTR_TARGET_MODE,
                LaunchConfigDelegate.DEFAULT_TARGET_MODE.getValue());
        configuration.setAttribute(LaunchConfigDelegate.ATTR_SPEED,
                LaunchConfigDelegate.DEFAULT_SPEED);
        configuration.setAttribute(LaunchConfigDelegate.ATTR_DELAY,
                LaunchConfigDelegate.DEFAULT_DELAY);
        configuration.setAttribute(LaunchConfigDelegate.ATTR_WIPE_DATA,
                LaunchConfigDelegate.DEFAULT_WIPE_DATA);
        configuration.setAttribute(LaunchConfigDelegate.ATTR_NO_BOOT_ANIM,
                LaunchConfigDelegate.DEFAULT_NO_BOOT_ANIM);
        IPreferenceStore store = AdtPlugin.getDefault().getPreferenceStore();
        String emuOptions = store.getString(AdtPrefs.PREFS_EMU_OPTIONS);
        configuration.setAttribute(LaunchConfigDelegate.ATTR_COMMANDLINE, emuOptions);
   }
}
