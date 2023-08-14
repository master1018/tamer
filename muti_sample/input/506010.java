public class ConfigManagerDialog extends GridDialog {
    private final static String COL_NAME = AdtPlugin.PLUGIN_ID + ".configmanager.name"; 
    private final static String COL_CONFIG = AdtPlugin.PLUGIN_ID + ".configmanager.config"; 
    private static enum DeviceType {
        DEFAULT("Default"),
        ADDON("Add-on"),
        CUSTOM("Custom");
        private final String mDisplay;
        DeviceType(String display) {
            mDisplay = display;
        }
        String getDisplayString() {
            return mDisplay;
        }
    }
    private static class DeviceSelection {
        public DeviceSelection(DeviceType type, LayoutDevice device,
                Entry<String, FolderConfiguration> entry) {
            this.type = type;
            this.device = device;
            this.entry = entry;
        }
        final DeviceType type;
        final LayoutDevice device;
        final Entry<String, FolderConfiguration> entry;
    }
    private final LayoutDeviceManager mManager;
    private TreeViewer mTreeViewer;
    private Button mNewButton;
    private Button mEditButton;
    private Button mCopyButton;
    private Button mDeleteButton;
    private final static class DeviceContentProvider implements ITreeContentProvider {
        private final static DeviceType[] sCategory = new DeviceType[] {
            DeviceType.DEFAULT, DeviceType.ADDON, DeviceType.CUSTOM
        };
        private LayoutDeviceManager mLayoutDeviceManager;
        public DeviceContentProvider() {
        }
        public Object[] getElements(Object inputElement) {
            return sCategory;
        }
        public Object[] getChildren(Object parentElement) {
            if (parentElement instanceof DeviceType) {
                if (DeviceType.DEFAULT.equals(parentElement)) {
                    return mLayoutDeviceManager.getDefaultLayoutDevices().toArray();
                } else if (DeviceType.ADDON.equals(parentElement)) {
                    return mLayoutDeviceManager.getAddOnLayoutDevice().toArray();
                } else if (DeviceType.CUSTOM.equals(parentElement)) {
                    return mLayoutDeviceManager.getUserLayoutDevices().toArray();
                }
            } else if (parentElement instanceof LayoutDevice) {
                LayoutDevice device = (LayoutDevice)parentElement;
                return device.getConfigs().entrySet().toArray();
            }
            return null;
        }
        public Object getParent(Object element) {
            return null;
        }
        public boolean hasChildren(Object element) {
            if (element instanceof DeviceType) {
                if (DeviceType.DEFAULT.equals(element)) {
                    return mLayoutDeviceManager.getDefaultLayoutDevices().size() > 0;
                } else if (DeviceType.ADDON.equals(element)) {
                    return mLayoutDeviceManager.getAddOnLayoutDevice().size() > 0;
                } else if (DeviceType.CUSTOM.equals(element)) {
                    return mLayoutDeviceManager.getUserLayoutDevices().size() > 0;
                }
            } else if (element instanceof LayoutDevice) {
                LayoutDevice device = (LayoutDevice)element;
                return device.getConfigs().size() > 0;
            }
            return false;
        }
        public void dispose() {
        }
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            if (newInput instanceof LayoutDeviceManager) {
                mLayoutDeviceManager = (LayoutDeviceManager)newInput;
                return;
            }
            if (newInput != null) {
                throw new IllegalArgumentException(
                        "ConfigContentProvider requires input to be LayoutDeviceManager");
            }
        }
    }
    private final static class DeviceLabelProvider implements ITableLabelProvider {
        public String getColumnText(Object element, int columnIndex) {
            if (element instanceof DeviceType) {
                if (columnIndex == 0) {
                    return ((DeviceType)element).getDisplayString();
                }
            } else if (element instanceof LayoutDevice) {
                if (columnIndex == 0) {
                    return ((LayoutDevice)element).getName();
                }
            } else if (element instanceof Entry<?, ?>) {
                if (columnIndex == 0) {
                    return (String)((Entry<?,?>)element).getKey();
                } else {
                    return ((Entry<?,?>)element).getValue().toString();
                }
            }
            return null;
        }
        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }
        public void addListener(ILabelProviderListener listener) {
        }
        public void removeListener(ILabelProviderListener listener) {
        }
        public void dispose() {
        }
        public boolean isLabelProperty(Object element, String property) {
            return false;
        }
    }
    protected ConfigManagerDialog(Shell parentShell) {
        super(parentShell, 2, false);
        mManager = Sdk.getCurrent().getLayoutDeviceManager();
    }
    @Override
    protected int getShellStyle() {
        return super.getShellStyle() | SWT.RESIZE;
    }
    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("Device Configurations");
    }
    @Override
    public void createDialogContent(final Composite parent) {
        GridData gd;
        GridLayout gl;
        Tree tree = new Tree(parent, SWT.SINGLE | SWT.FULL_SELECTION);
        tree.setLayoutData(gd = new GridData(GridData.FILL_BOTH));
        gd.widthHint = 700;
        tree.setHeaderVisible(true);
        tree.setLinesVisible(true);
        TableHelper.createTreeColumn(tree, "Name", SWT.LEFT, 150, COL_NAME,
                AdtPlugin.getDefault().getPreferenceStore());
        TableHelper.createTreeColumn(tree, "Configuration", SWT.LEFT, 500, COL_CONFIG,
                AdtPlugin.getDefault().getPreferenceStore());
        mTreeViewer = new TreeViewer(tree);
        mTreeViewer.setContentProvider(new DeviceContentProvider());
        mTreeViewer.setLabelProvider(new DeviceLabelProvider());
        mTreeViewer.setAutoExpandLevel(TreeViewer.ALL_LEVELS);
        mTreeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
                setEnabled(getSelection());
            }
        });
        Composite buttons = new Composite(parent, SWT.NONE);
        buttons.setLayoutData(new GridData(GridData.FILL_VERTICAL));
        buttons.setLayout(gl = new GridLayout());
        gl.marginHeight = gl.marginWidth = 0;
        mNewButton = new Button(buttons, SWT.PUSH | SWT.FLAT);
        mNewButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        mNewButton.setText("New...");
        mNewButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                DeviceSelection selection = getSelection();
                ConfigEditDialog dlg = new ConfigEditDialog(parent.getShell(), null);
                if (selection.device != null) {
                    dlg.setDeviceName(selection.device.getName());
                    dlg.setXDpi(selection.device.getXDpi());
                    dlg.setYDpi(selection.device.getYDpi());
                }
                if (selection.entry != null) {
                    dlg.setConfigName(selection.entry.getKey());
                    dlg.setConfig(selection.entry.getValue());
                }
                if (dlg.open() == Window.OK) {
                    String deviceName = dlg.getDeviceName();
                    String configName = dlg.getConfigName();
                    FolderConfiguration config = new FolderConfiguration();
                    dlg.getConfig(config);
                    LayoutDevice d;
                    if (selection.device == null) {
                        d = mManager.addUserDevice(deviceName, dlg.getXDpi(), dlg.getYDpi());
                    } else {
                        d = mManager.getUserLayoutDevice(deviceName);
                    }
                    if (d != null) {
                        mManager.addUserConfiguration(d, configName, config);
                        mTreeViewer.refresh();
                        select(d, configName);
                    }
                }
            }
        });
        mEditButton = new Button(buttons, SWT.PUSH | SWT.FLAT);
        mEditButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        mEditButton.setText("Edit...");
        mEditButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                DeviceSelection selection = getSelection();
                ConfigEditDialog dlg = new ConfigEditDialog(parent.getShell(), null);
                dlg.setDeviceName(selection.device.getName());
                dlg.setXDpi(selection.device.getXDpi());
                dlg.setYDpi(selection.device.getYDpi());
                dlg.setConfigName(selection.entry.getKey());
                dlg.setConfig(selection.entry.getValue());
                if (dlg.open() == Window.OK) {
                    String deviceName = dlg.getDeviceName();
                    String configName = dlg.getConfigName();
                    FolderConfiguration config = new FolderConfiguration();
                    dlg.getConfig(config);
                    LayoutDevice d = mManager.replaceUserDevice(selection.device, deviceName,
                            dlg.getXDpi(), dlg.getYDpi());
                    mManager.replaceUserConfiguration(d, selection.entry.getKey(), configName,
                            config);
                    mTreeViewer.refresh();
                    select(d, configName);
                }
            }
        });
        mCopyButton = new Button(buttons, SWT.PUSH | SWT.FLAT);
        mCopyButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        mCopyButton.setText("Copy");
        mCopyButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                DeviceSelection selection = getSelection();
                LayoutDevice targetDevice = selection.device;
                if (selection.type == DeviceType.DEFAULT || selection.type == DeviceType.ADDON ||
                        selection.entry == null) {
                    targetDevice = mManager.addUserDevice(
                            selection.device.getName() + " Copy", 
                            selection.device.getXDpi(),
                            selection.device.getYDpi());
                }
                String newConfigName = null; 
                if (selection.entry == null) {
                    Map<String, FolderConfiguration> configs = selection.device.getConfigs();
                    for (Entry<String, FolderConfiguration> entry : configs.entrySet()) {
                        FolderConfiguration copy = new FolderConfiguration();
                        copy.set(entry.getValue());
                        mManager.addUserConfiguration(targetDevice, entry.getKey(), copy);
                    }
                } else {
                    newConfigName = (selection.device != targetDevice) ?
                            selection.entry.getKey() : selection.entry.getKey() + " Copy";
                    FolderConfiguration copy = new FolderConfiguration();
                    copy.set(selection.entry.getValue());
                    mManager.addUserConfiguration(targetDevice, newConfigName, copy);
                }
                mTreeViewer.refresh();
                select(targetDevice, newConfigName);
            }
        });
        mDeleteButton = new Button(buttons, SWT.PUSH | SWT.FLAT);
        mDeleteButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        mDeleteButton.setText("Delete");
        mDeleteButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                DeviceSelection selection = getSelection();
                if (selection.entry != null) {
                    mManager.removeUserConfiguration(selection.device, selection.entry.getKey());
                } else if (selection.device != null) {
                    mManager.removeUserDevice(selection.device);
                }
                mTreeViewer.refresh();
                select(selection.entry != null ? selection.device : null, null);
            }
        });
        Label separator = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
        separator.setLayoutData(gd = new GridData(GridData.FILL_HORIZONTAL));
        gd.horizontalSpan = 2;
        mTreeViewer.setInput(mManager);
        setEnabled(null); 
    }
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
    }
    @SuppressWarnings("unchecked")
    private DeviceSelection getSelection() {
        TreeSelection selection = (TreeSelection)mTreeViewer.getSelection();
        TreePath[] paths =selection.getPaths();
        if (paths.length == 0) {
            return null;
        }
        TreePath pathSelection = paths[0];
        DeviceType type = (DeviceType)pathSelection.getFirstSegment();
        LayoutDevice device = null;
        Entry<String, FolderConfiguration> entry = null;
        switch (pathSelection.getSegmentCount()) {
            case 2: 
                device = (LayoutDevice)pathSelection.getLastSegment();
                break;
            case 3: 
                device = (LayoutDevice)pathSelection.getSegment(1);
                entry = (Entry<String, FolderConfiguration>)pathSelection.getLastSegment();
        }
        return new DeviceSelection(type, device, entry);
    }
    protected void setEnabled(DeviceSelection selection) {
        if (selection == null) {
            mNewButton.setEnabled(false);
            mEditButton.setEnabled(false);
            mCopyButton.setEnabled(false);
            mDeleteButton.setEnabled(false);
        } else {
            switch (selection.type) {
                case DEFAULT:
                case ADDON:
                    mNewButton.setEnabled(false);
                    mEditButton.setEnabled(false);
                    mDeleteButton.setEnabled(false);
                    mCopyButton.setEnabled(selection.device != null);
                    break;
                case CUSTOM:
                    mNewButton.setEnabled(true); 
                    mEditButton.setEnabled(selection.entry != null); 
                    boolean enabled = selection.device != null; 
                    mDeleteButton.setEnabled(enabled);          
                    mCopyButton.setEnabled(enabled);
                    break;
            }
        }
    }
    private void select(LayoutDevice device, String configName) {
        Object[] path;
        if (device == null) {
            path = new Object[] { DeviceType.CUSTOM };
        } else if (configName == null) {
            path = new Object[] { DeviceType.CUSTOM, device };
        } else {
            Entry<?, ?> match = null;
            for (Entry<?, ?> entry : device.getConfigs().entrySet()) {
                if (entry.getKey().equals(configName)) {
                    match = entry;
                    break;
                }
            }
            if (match != null) {
                path = new Object[] { DeviceType.CUSTOM, device, match };
            } else {
                path = new Object[] { DeviceType.CUSTOM, device };
            }
        }
        mTreeViewer.setSelection(new TreeSelection(new TreePath(path)), true );
    }
}
