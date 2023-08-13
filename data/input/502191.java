public class DeviceChooserDialog extends Dialog implements IDeviceChangeListener {
    private final static int ICON_WIDTH = 16;
    private Table mDeviceTable;
    private TableViewer mViewer;
    private AvdSelector mPreferredAvdSelector;
    private Image mDeviceImage;
    private Image mEmulatorImage;
    private Image mMatchImage;
    private Image mNoMatchImage;
    private Image mWarningImage;
    private final DeviceChooserResponse mResponse;
    private final String mPackageName;
    private final IAndroidTarget mProjectTarget;
    private final Sdk mSdk;
    private Button mDeviceRadioButton;
    private boolean mDisableAvdSelectionChange = false;
    private class ContentProvider implements IStructuredContentProvider {
        public Object[] getElements(Object inputElement) {
            if (inputElement instanceof AndroidDebugBridge) {
                return ((AndroidDebugBridge)inputElement).getDevices();
            }
            return new Object[0];
        }
        public void dispose() {
        }
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }
    }
    private class LabelProvider implements ITableLabelProvider {
        public Image getColumnImage(Object element, int columnIndex) {
            if (element instanceof IDevice) {
                IDevice device = (IDevice)element;
                switch (columnIndex) {
                    case 0:
                        return device.isEmulator() ? mEmulatorImage : mDeviceImage;
                    case 2:
                        if (device.isEmulator() == false) { 
                            AndroidVersion deviceVersion = Sdk.getDeviceVersion(device);
                            if (deviceVersion == null) {
                                return mWarningImage;
                            } else {
                                if (deviceVersion.canRun(mProjectTarget.getVersion()) == false) {
                                    return mNoMatchImage;
                                }
                                return mProjectTarget.isPlatform() ?
                                        mMatchImage : mWarningImage;
                            }
                        } else {
                            AvdInfo info = mSdk.getAvdManager().getAvd(device.getAvdName(),
                                    true );
                            if (info == null) {
                                return mWarningImage;
                            }
                            return mProjectTarget.canRunOn(info.getTarget()) ?
                                    mMatchImage : mNoMatchImage;
                        }
                }
            }
            return null;
        }
        public String getColumnText(Object element, int columnIndex) {
            if (element instanceof IDevice) {
                IDevice device = (IDevice)element;
                switch (columnIndex) {
                    case 0:
                        return device.getSerialNumber();
                    case 1:
                        if (device.isEmulator()) {
                            return device.getAvdName();
                        } else {
                            return "N/A"; 
                        }
                    case 2:
                        if (device.isEmulator()) {
                            AvdInfo info = mSdk.getAvdManager().getAvd(device.getAvdName(),
                                    true );
                            if (info == null) {
                                return "?";
                            }
                            return info.getTarget().getFullName();
                        } else {
                            String deviceBuild = device.getProperty(IDevice.PROP_BUILD_VERSION);
                            if (deviceBuild == null) {
                                return "unknown";
                            }
                            return deviceBuild;
                        }
                    case 3:
                        String debuggable = device.getProperty(IDevice.PROP_DEBUGGABLE);
                        if (debuggable != null && debuggable.equals("1")) { 
                            return "Yes";
                        } else {
                            return "";
                        }
                    case 4:
                        return getStateString(device);
                }
            }
            return null;
        }
        public void addListener(ILabelProviderListener listener) {
        }
        public void dispose() {
        }
        public boolean isLabelProperty(Object element, String property) {
            return false;
        }
        public void removeListener(ILabelProviderListener listener) {
        }
    }
    public static class DeviceChooserResponse {
        private AvdInfo mAvdToLaunch;
        private IDevice mDeviceToUse;
        public void setDeviceToUse(IDevice d) {
            mDeviceToUse = d;
            mAvdToLaunch = null;
        }
        public void setAvdToLaunch(AvdInfo avd) {
            mAvdToLaunch = avd;
            mDeviceToUse = null;
        }
        public IDevice getDeviceToUse() {
            return mDeviceToUse;
        }
        public AvdInfo getAvdToLaunch() {
            return mAvdToLaunch;
        }
    }
    public DeviceChooserDialog(Shell parent, DeviceChooserResponse response, String packageName,
            IAndroidTarget projectTarget) {
        super(parent);
        mResponse = response;
        mPackageName = packageName;
        mProjectTarget = projectTarget;
        mSdk = Sdk.getCurrent();
        AndroidDebugBridge.addDeviceChangeListener(this);
        loadImages();
    }
    private void cleanup() {
        AndroidDebugBridge.removeDeviceChangeListener(this);
        mEmulatorImage.dispose();
        mDeviceImage.dispose();
        mMatchImage.dispose();
        mNoMatchImage.dispose();
        mWarningImage.dispose();
    }
    @Override
    protected void okPressed() {
        cleanup();
        super.okPressed();
    }
    @Override
    protected void cancelPressed() {
        cleanup();
        super.cancelPressed();
    }
    @Override
    protected Control createContents(Composite parent) {
        Control content = super.createContents(parent);
        updateDefaultSelection();
        return content;
    }
    @Override
    protected Control createDialogArea(Composite parent) {
        getShell().setText("Android Device Chooser");
        Composite top = new Composite(parent, SWT.NONE);
        top.setLayout(new GridLayout(1, true));
        Label label = new Label(top, SWT.NONE);
        label.setText(String.format("Select a device compatible with target %s.",
                mProjectTarget.getFullName()));
        mDeviceRadioButton = new Button(top, SWT.RADIO);
        mDeviceRadioButton.setText("Choose a running Android device");
        mDeviceRadioButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                boolean deviceMode = mDeviceRadioButton.getSelection();
                mDeviceTable.setEnabled(deviceMode);
                mPreferredAvdSelector.setEnabled(!deviceMode);
                if (deviceMode) {
                    handleDeviceSelection();
                } else {
                    mResponse.setAvdToLaunch(mPreferredAvdSelector.getSelected());
                }
                enableOkButton();
            }
        });
        mDeviceRadioButton.setSelection(true);
        Composite offsetComp = new Composite(top, SWT.NONE);
        offsetComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        GridLayout layout = new GridLayout(1, false);
        layout.marginRight = layout.marginHeight = 0;
        layout.marginLeft = 30;
        offsetComp.setLayout(layout);
        mDeviceTable = new Table(offsetComp, SWT.SINGLE | SWT.FULL_SELECTION | SWT.BORDER);
        GridData gd;
        mDeviceTable.setLayoutData(gd = new GridData(GridData.FILL_BOTH));
        gd.heightHint = 100;
        mDeviceTable.setHeaderVisible(true);
        mDeviceTable.setLinesVisible(true);
        TableHelper.createTableColumn(mDeviceTable, "Serial Number",
                SWT.LEFT, "AAA+AAAAAAAAAAAAAAAAAAA", 
                null , null );
        TableHelper.createTableColumn(mDeviceTable, "AVD Name",
                SWT.LEFT, "AAAAAAAAAAAAAAAAAAA", 
                null , null );
        TableHelper.createTableColumn(mDeviceTable, "Target",
                SWT.LEFT, "AAA+Android 9.9.9", 
                null , null );
        TableHelper.createTableColumn(mDeviceTable, "Debug",
                SWT.LEFT, "Debug", 
                null , null );
        TableHelper.createTableColumn(mDeviceTable, "State",
                SWT.LEFT, "bootloader", 
                null , null );
        mViewer = new TableViewer(mDeviceTable);
        mViewer.setContentProvider(new ContentProvider());
        mViewer.setLabelProvider(new LabelProvider());
        mViewer.setInput(AndroidDebugBridge.getBridge());
        mDeviceTable.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleDeviceSelection();
            }
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                handleDeviceSelection();
                if (isOkButtonEnabled()) {
                    okPressed();
                }
            }
        });
        Button radio2 = new Button(top, SWT.RADIO);
        radio2.setText("Launch a new Android Virtual Device");
        offsetComp = new Composite(top, SWT.NONE);
        offsetComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        layout = new GridLayout(1, false);
        layout.marginRight = layout.marginHeight = 0;
        layout.marginLeft = 30;
        offsetComp.setLayout(layout);
        mPreferredAvdSelector = new AvdSelector(offsetComp,
                mSdk.getSdkLocation(),
                mSdk.getAvdManager(),
                new NonRunningAvdFilter(),
                DisplayMode.SIMPLE_SELECTION,
                new AdtConsoleSdkLog());
        mPreferredAvdSelector.setTableHeightHint(100);
        mPreferredAvdSelector.setEnabled(false);
        mPreferredAvdSelector.setSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (mDisableAvdSelectionChange == false) {
                    mResponse.setAvdToLaunch(mPreferredAvdSelector.getSelected());
                    enableOkButton();
                }
            }
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
                if (isOkButtonEnabled()) {
                    okPressed();
                }
            }
        });
        return top;
    }
    private void loadImages() {
        IImageLoader ddmsLoader = DdmsPlugin.getImageLoader();
        Display display = DdmsPlugin.getDisplay();
        IImageLoader adtLoader = AdtPlugin.getImageLoader();
        if (mDeviceImage == null) {
            mDeviceImage = ImageHelper.loadImage(ddmsLoader, display,
                    "device.png", 
                    ICON_WIDTH, ICON_WIDTH,
                    display.getSystemColor(SWT.COLOR_RED));
        }
        if (mEmulatorImage == null) {
            mEmulatorImage = ImageHelper.loadImage(ddmsLoader, display,
                    "emulator.png", ICON_WIDTH, ICON_WIDTH, 
                    display.getSystemColor(SWT.COLOR_BLUE));
        }
        if (mMatchImage == null) {
            mMatchImage = ImageHelper.loadImage(adtLoader, display,
                    "match.png", 
                    ICON_WIDTH, ICON_WIDTH,
                    display.getSystemColor(SWT.COLOR_GREEN));
        }
        if (mNoMatchImage == null) {
            mNoMatchImage = ImageHelper.loadImage(adtLoader, display,
                    "error.png", 
                    ICON_WIDTH, ICON_WIDTH,
                    display.getSystemColor(SWT.COLOR_RED));
        }
        if (mWarningImage == null) {
            mWarningImage = ImageHelper.loadImage(adtLoader, display,
                    "warning.png", 
                    ICON_WIDTH, ICON_WIDTH,
                    display.getSystemColor(SWT.COLOR_YELLOW));
        }
    }
    private static String getStateString(IDevice d) {
        DeviceState deviceState = d.getState();
        if (deviceState == DeviceState.ONLINE) {
            return "Online";
        } else if (deviceState == DeviceState.OFFLINE) {
            return "Offline";
        } else if (deviceState == DeviceState.BOOTLOADER) {
            return "Bootloader";
        }
        return "??";
    }
    public void deviceConnected(IDevice device) {
        final DeviceChooserDialog dialog = this;
        exec(new Runnable() {
            public void run() {
                if (mDeviceTable.isDisposed() == false) {
                    mViewer.refresh();
                    updateDefaultSelection();
                    refillAvdList(false );
                } else {
                    AndroidDebugBridge.removeDeviceChangeListener(dialog);
                }
            }
        });
    }
    public void deviceDisconnected(IDevice device) {
        deviceConnected(device);
    }
    public void deviceChanged(final IDevice device, int changeMask) {
        if ((changeMask & (IDevice.CHANGE_STATE | IDevice.CHANGE_BUILD_INFO)) != 0) {
            final DeviceChooserDialog dialog = this;
            exec(new Runnable() {
                public void run() {
                    if (mDeviceTable.isDisposed() == false) {
                        mViewer.refresh(device);
                        updateDefaultSelection();
                        refillAvdList(false );
                        if (device == mResponse.getDeviceToUse()) {
                            enableOkButton();
                        }
                    } else {
                        AndroidDebugBridge.removeDeviceChangeListener(dialog);
                    }
                }
            });
        }
    }
    private boolean isDeviceMode() {
        return mDeviceRadioButton.getSelection();
    }
    private void enableOkButton() {
        Button okButton = getButton(IDialogConstants.OK_ID);
        if (isDeviceMode()) {
            okButton.setEnabled(mResponse.getDeviceToUse() != null &&
                    mResponse.getDeviceToUse().isOnline());
        } else {
            okButton.setEnabled(mResponse.getAvdToLaunch() != null);
        }
    }
    private boolean isOkButtonEnabled() {
        Button okButton = getButton(IDialogConstants.OK_ID);
        return okButton.isEnabled();
    }
    private void exec(Runnable runnable) {
        try {
            Display display = mDeviceTable.getDisplay();
            display.asyncExec(runnable);
        } catch (SWTException e) {
            AndroidDebugBridge.removeDeviceChangeListener(this);
        }
    }
    private void handleDeviceSelection() {
        int count = mDeviceTable.getSelectionCount();
        if (count != 1) {
            handleSelection(null);
        } else {
            int index = mDeviceTable.getSelectionIndex();
            Object data = mViewer.getElementAt(index);
            if (data instanceof IDevice) {
                handleSelection((IDevice)data);
            } else {
                handleSelection(null);
            }
        }
    }
    private void handleSelection(IDevice device) {
        mResponse.setDeviceToUse(device);
        enableOkButton();
    }
    private void updateDefaultSelection() {
        if (mDeviceTable.getSelectionCount() == 0) {
            AndroidDebugBridge bridge = AndroidDebugBridge.getBridge();
            IDevice[] devices = bridge.getDevices();
            for (IDevice device : devices) {
                Client[] clients = device.getClients();
                for (Client client : clients) {
                    if (mPackageName.equals(client.getClientData().getClientDescription())) {
                        mViewer.setSelection(new StructuredSelection(device));
                        handleSelection(device);
                        return;
                    }
                }
            }
        }
        handleDeviceSelection();
    }
    private final class NonRunningAvdFilter implements IAvdFilter {
        private IDevice[] mDevices;
        public void prepare() {
            mDevices = AndroidDebugBridge.getBridge().getDevices();
        }
        public boolean accept(AvdInfo avd) {
            if (mDevices != null) {
                for (IDevice d : mDevices) {
                    if (mProjectTarget.canRunOn(avd.getTarget()) == false ||
                            avd.getName().equals(d.getAvdName())) {
                        return false;
                    }
                }
            }
            return true;
        }
        public void cleanup() {
            mDevices = null;
        }
    }
    private void refillAvdList(boolean reloadAvds) {
        AvdInfo selected = mPreferredAvdSelector.getSelected();
        mDisableAvdSelectionChange = true;
        mPreferredAvdSelector.refresh(false);
        if (selected != null) {
            if (mPreferredAvdSelector.setSelection(selected) == false) {
                mResponse.setAvdToLaunch(null);
                enableOkButton();
            }
        }
        mDisableAvdSelectionChange = false;
    }
}
