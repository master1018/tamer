public final class DevicePanel extends Panel implements IDebugBridgeChangeListener,
        IDeviceChangeListener, IClientChangeListener {
    private final static String PREFS_COL_NAME_SERIAL = "devicePanel.Col0"; 
    private final static String PREFS_COL_PID_STATE = "devicePanel.Col1"; 
    private final static String PREFS_COL_PORT_BUILD = "devicePanel.Col4"; 
    private final static int DEVICE_COL_SERIAL = 0;
    private final static int DEVICE_COL_STATE = 1;
    private final static int DEVICE_COL_BUILD = 4;
    private final static int CLIENT_COL_NAME = 0;
    private final static int CLIENT_COL_PID = 1;
    private final static int CLIENT_COL_THREAD = 2;
    private final static int CLIENT_COL_HEAP = 3;
    private final static int CLIENT_COL_PORT = 4;
    public final static int ICON_WIDTH = 16;
    public final static String ICON_THREAD = "thread.png"; 
    public final static String ICON_HEAP = "heap.png"; 
    public final static String ICON_HALT = "halt.png"; 
    public final static String ICON_GC = "gc.png"; 
    public final static String ICON_HPROF = "hprof.png"; 
    public final static String ICON_TRACING_START = "tracing_start.png"; 
    public final static String ICON_TRACING_STOP = "tracing_stop.png"; 
    private IDevice mCurrentDevice;
    private Client mCurrentClient;
    private Tree mTree;
    private TreeViewer mTreeViewer;
    private Image mDeviceImage;
    private Image mEmulatorImage;
    private Image mThreadImage;
    private Image mHeapImage;
    private Image mWaitingImage;
    private Image mDebuggerImage;
    private Image mDebugErrorImage;
    private final ArrayList<IUiSelectionListener> mListeners = new ArrayList<IUiSelectionListener>();
    private final ArrayList<IDevice> mDevicesToExpand = new ArrayList<IDevice>();
    private IImageLoader mLoader;
    private boolean mAdvancedPortSupport;
    private class ContentProvider implements ITreeContentProvider {
        public Object[] getChildren(Object parentElement) {
            if (parentElement instanceof IDevice) {
                return ((IDevice)parentElement).getClients();
            }
            return new Object[0];
        }
        public Object getParent(Object element) {
            if (element instanceof Client) {
                return ((Client)element).getDevice();
            }
            return null;
        }
        public boolean hasChildren(Object element) {
            if (element instanceof IDevice) {
                return ((IDevice)element).hasClients();
            }
            return false;
        }
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
            if (columnIndex == DEVICE_COL_SERIAL && element instanceof IDevice) {
                IDevice device = (IDevice)element;
                if (device.isEmulator()) {
                    return mEmulatorImage;
                }
                return mDeviceImage;
            } else if (element instanceof Client) {
                Client client = (Client)element;
                ClientData cd = client.getClientData();
                switch (columnIndex) {
                    case CLIENT_COL_NAME:
                        switch (cd.getDebuggerConnectionStatus()) {
                            case DEFAULT:
                                return null;
                            case WAITING:
                                return mWaitingImage;
                            case ATTACHED:
                                return mDebuggerImage;
                            case ERROR:
                                return mDebugErrorImage;
                        }
                        return null;
                    case CLIENT_COL_THREAD:
                        if (client.isThreadUpdateEnabled()) {
                            return mThreadImage;
                        }
                        return null;
                    case CLIENT_COL_HEAP:
                        if (client.isHeapUpdateEnabled()) {
                            return mHeapImage;
                        }
                        return null;
                }
            }
            return null;
        }
        public String getColumnText(Object element, int columnIndex) {
            if (element instanceof IDevice) {
                IDevice device = (IDevice)element;
                switch (columnIndex) {
                    case DEVICE_COL_SERIAL:
                        return device.getSerialNumber();
                    case DEVICE_COL_STATE:
                        return getStateString(device);
                    case DEVICE_COL_BUILD: {
                        String version = device.getProperty(IDevice.PROP_BUILD_VERSION);
                        if (version != null) {
                            String debuggable = device.getProperty(IDevice.PROP_DEBUGGABLE);
                            if (device.isEmulator()) {
                                String avdName = device.getAvdName();
                                if (avdName == null) {
                                    avdName = "?"; 
                                }
                                if (debuggable != null && debuggable.equals("1")) { 
                                    return String.format("%1$s [%2$s, debug]", avdName,
                                            version);
                                } else {
                                    return String.format("%1$s [%2$s]", avdName, version); 
                                }
                            } else {
                                if (debuggable != null && debuggable.equals("1")) { 
                                    return String.format("%1$s, debug", version);
                                } else {
                                    return String.format("%1$s", version); 
                                }
                            }
                        } else {
                            return "unknown";
                        }
                    }
                }
            } else if (element instanceof Client) {
                Client client = (Client)element;
                ClientData cd = client.getClientData();
                switch (columnIndex) {
                    case CLIENT_COL_NAME:
                        String name = cd.getClientDescription();
                        if (name != null) {
                            return name;
                        }
                        return "?";
                    case CLIENT_COL_PID:
                        return Integer.toString(cd.getPid());
                    case CLIENT_COL_PORT:
                        if (mAdvancedPortSupport) {
                            int port = client.getDebuggerListenPort();
                            String portString = "?";
                            if (port != 0) {
                                portString = Integer.toString(port);
                            }
                            if (client.isSelectedClient()) {
                                return String.format("%1$s / %2$d", portString, 
                                        DdmPreferences.getSelectedDebugPort());
                            }
                            return portString;
                        }
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
    public interface IUiSelectionListener {
        public void selectionChanged(IDevice selectedDevice, Client selectedClient);
    }
    public DevicePanel(IImageLoader loader, boolean advancedPortSupport) {
        mLoader = loader;
        mAdvancedPortSupport = advancedPortSupport;
    }
    public void addSelectionListener(IUiSelectionListener listener) {
        mListeners.add(listener);
    }
    public void removeSelectionListener(IUiSelectionListener listener) {
        mListeners.remove(listener);
    }
    @Override
    protected Control createControl(Composite parent) {
        loadImages(parent.getDisplay(), mLoader);
        parent.setLayout(new FillLayout());
        mTree = new Tree(parent, SWT.SINGLE | SWT.FULL_SELECTION);
        mTree.setHeaderVisible(true);
        mTree.setLinesVisible(true);
        IPreferenceStore store = DdmUiPreferences.getStore();
        TableHelper.createTreeColumn(mTree, "Name", SWT.LEFT,
                "com.android.home", 
                PREFS_COL_NAME_SERIAL, store);
        TableHelper.createTreeColumn(mTree, "", SWT.LEFT, 
                "Offline", 
                PREFS_COL_PID_STATE, store);
        TreeColumn col = new TreeColumn(mTree, SWT.NONE);
        col.setWidth(ICON_WIDTH + 8);
        col.setResizable(false);
        col = new TreeColumn(mTree, SWT.NONE);
        col.setWidth(ICON_WIDTH + 8);
        col.setResizable(false);
        TableHelper.createTreeColumn(mTree, "", SWT.LEFT, 
                "9999-9999", 
                PREFS_COL_PORT_BUILD, store);
        mTreeViewer = new TreeViewer(mTree);
        mTreeViewer.setAutoExpandLevel(TreeViewer.ALL_LEVELS);
        mTreeViewer.setContentProvider(new ContentProvider());
        mTreeViewer.setLabelProvider(new LabelProvider());
        mTree.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                notifyListeners();
            }
        });
        return mTree;
    }
    @Override
    public void setFocus() {
        mTree.setFocus();
    }
    @Override
    protected void postCreation() {
        AndroidDebugBridge.addDebugBridgeChangeListener(this);
        AndroidDebugBridge.addDeviceChangeListener(this);
        AndroidDebugBridge.addClientChangeListener(this);
    }
    public void dispose() {
        AndroidDebugBridge.removeDebugBridgeChangeListener(this);
        AndroidDebugBridge.removeDeviceChangeListener(this);
        AndroidDebugBridge.removeClientChangeListener(this);
    }
    public Client getSelectedClient() {
        return mCurrentClient;
    }
    public IDevice getSelectedDevice() {
        return mCurrentDevice;
    }
    public void killSelectedClient() {
        if (mCurrentClient != null) {
            Client client = mCurrentClient;
            TreePath treePath = new TreePath(new Object[] { mCurrentDevice });
            TreeSelection treeSelection = new TreeSelection(treePath);
            mTreeViewer.setSelection(treeSelection);
            client.kill();
        }
    }
    public void forceGcOnSelectedClient() {
        if (mCurrentClient != null) {
            mCurrentClient.executeGarbageCollector();
        }
    }
    public void dumpHprof() {
        if (mCurrentClient != null) {
            mCurrentClient.dumpHprof();
        }
    }
    public void toggleMethodProfiling() {
        if (mCurrentClient != null) {
            mCurrentClient.toggleMethodProfiling();
        }
    }
    public void setEnabledHeapOnSelectedClient(boolean enable) {
        if (mCurrentClient != null) {
            mCurrentClient.setHeapUpdateEnabled(enable);
        }
    }
    public void setEnabledThreadOnSelectedClient(boolean enable) {
        if (mCurrentClient != null) {
            mCurrentClient.setThreadUpdateEnabled(enable);
        }
    }
    public void bridgeChanged(final AndroidDebugBridge bridge) {
        if (mTree.isDisposed() == false) {
            exec(new Runnable() {
                public void run() {
                    if (mTree.isDisposed() == false) {
                        mTreeViewer.setInput(bridge);
                        notifyListeners();
                    } else {
                        AndroidDebugBridge.removeDebugBridgeChangeListener(DevicePanel.this);
                        AndroidDebugBridge.removeDeviceChangeListener(DevicePanel.this);
                        AndroidDebugBridge.removeClientChangeListener(DevicePanel.this);
                    }
                }
            });
        }
        synchronized (mDevicesToExpand) {
            mDevicesToExpand.clear();
        }
    }
    public void deviceConnected(IDevice device) {
        exec(new Runnable() {
            public void run() {
                if (mTree.isDisposed() == false) {
                    mTreeViewer.refresh();
                    notifyListeners();
                } else {
                    AndroidDebugBridge.removeDebugBridgeChangeListener(DevicePanel.this);
                    AndroidDebugBridge.removeDeviceChangeListener(DevicePanel.this);
                    AndroidDebugBridge.removeClientChangeListener(DevicePanel.this);
                }
            }
        });
        if (device.hasClients() == false) {
            synchronized (mDevicesToExpand) {
                mDevicesToExpand.add(device);
            }
        }
    }
    public void deviceDisconnected(IDevice device) {
        deviceConnected(device);
        synchronized (mDevicesToExpand) {
            mDevicesToExpand.remove(device);
        }
    }
    public void deviceChanged(final IDevice device, int changeMask) {
        boolean expand = false;
        synchronized (mDevicesToExpand) {
            int index = mDevicesToExpand.indexOf(device);
            if (device.hasClients() && index != -1) {
                mDevicesToExpand.remove(index);
                expand = true;
            }
        }
        final boolean finalExpand = expand;
        exec(new Runnable() {
            public void run() {
                if (mTree.isDisposed() == false) {
                    IDevice selectedDevice = getSelectedDevice();
                    mTreeViewer.refresh(device);
                    if (selectedDevice == device && mTreeViewer.getSelection().isEmpty()) {
                        mTreeViewer.setSelection(new TreeSelection(new TreePath(
                                new Object[] { device })));
                    }
                    notifyListeners();
                    if (finalExpand) {
                        mTreeViewer.setExpandedState(device, true);
                    }
                } else {
                    AndroidDebugBridge.removeDebugBridgeChangeListener(DevicePanel.this);
                    AndroidDebugBridge.removeDeviceChangeListener(DevicePanel.this);
                    AndroidDebugBridge.removeClientChangeListener(DevicePanel.this);
                }
            }
        });
    }
    public void clientChanged(final Client client, final int changeMask) {
        exec(new Runnable() {
            public void run() {
                if (mTree.isDisposed() == false) {
                    mTreeViewer.refresh(client);
                    if ((changeMask & Client.CHANGE_DEBUGGER_STATUS) ==
                            Client.CHANGE_DEBUGGER_STATUS &&
                            client.getClientData().getDebuggerConnectionStatus() ==
                                DebuggerStatus.WAITING) {
                        IDevice device = client.getDevice();
                        if (mTreeViewer.getExpandedState(device) == false) {
                            mTreeViewer.setExpandedState(device, true);
                        }
                        TreePath treePath = new TreePath(new Object[] { device, client});
                        TreeSelection treeSelection = new TreeSelection(treePath);
                        mTreeViewer.setSelection(treeSelection);
                        if (mAdvancedPortSupport) {
                            client.setAsSelectedClient();
                        }
                        notifyListeners(device, client);
                    }
                } else {
                    AndroidDebugBridge.removeDebugBridgeChangeListener(DevicePanel.this);
                    AndroidDebugBridge.removeDeviceChangeListener(DevicePanel.this);
                    AndroidDebugBridge.removeClientChangeListener(DevicePanel.this);
                }
            }
        });
    }
    private void loadImages(Display display, IImageLoader loader) {
        if (mDeviceImage == null) {
            mDeviceImage = ImageHelper.loadImage(loader, display, "device.png", 
                    ICON_WIDTH, ICON_WIDTH,
                    display.getSystemColor(SWT.COLOR_RED));
        }
        if (mEmulatorImage == null) {
            mEmulatorImage = ImageHelper.loadImage(loader, display,
                    "emulator.png", ICON_WIDTH, ICON_WIDTH, 
                    display.getSystemColor(SWT.COLOR_BLUE));
        }
        if (mThreadImage == null) {
            mThreadImage = ImageHelper.loadImage(loader, display, ICON_THREAD,
                    ICON_WIDTH, ICON_WIDTH,
                    display.getSystemColor(SWT.COLOR_YELLOW));
        }
        if (mHeapImage == null) {
            mHeapImage = ImageHelper.loadImage(loader, display, ICON_HEAP,
                    ICON_WIDTH, ICON_WIDTH,
                    display.getSystemColor(SWT.COLOR_BLUE));
        }
        if (mWaitingImage == null) {
            mWaitingImage = ImageHelper.loadImage(loader, display,
                    "debug-wait.png", ICON_WIDTH, ICON_WIDTH, 
                    display.getSystemColor(SWT.COLOR_RED));
        }
        if (mDebuggerImage == null) {
            mDebuggerImage = ImageHelper.loadImage(loader, display,
                    "debug-attach.png", ICON_WIDTH, ICON_WIDTH, 
                    display.getSystemColor(SWT.COLOR_GREEN));
        }
        if (mDebugErrorImage == null) {
            mDebugErrorImage = ImageHelper.loadImage(loader, display,
                    "debug-error.png", ICON_WIDTH, ICON_WIDTH, 
                    display.getSystemColor(SWT.COLOR_RED));
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
    private void exec(Runnable runnable) {
        try {
            Display display = mTree.getDisplay();
            display.asyncExec(runnable);
        } catch (SWTException e) {
            AndroidDebugBridge.removeDebugBridgeChangeListener(this);
            AndroidDebugBridge.removeDeviceChangeListener(this);
            AndroidDebugBridge.removeClientChangeListener(this);
        }
    }
    private void notifyListeners() {
        TreeItem[] items = mTree.getSelection();
        Client client = null;
        IDevice device = null;
        if (items.length == 1) {
            Object object = items[0].getData();
            if (object instanceof Client) {
                client = (Client)object;
                device = client.getDevice();
            } else if (object instanceof IDevice) {
                device = (IDevice)object;
            }
        }
        notifyListeners(device, client);
    }
    private void notifyListeners(IDevice selectedDevice, Client selectedClient) {
        if (selectedDevice != mCurrentDevice || selectedClient != mCurrentClient) {
            mCurrentDevice = selectedDevice;
            mCurrentClient = selectedClient;
            for (IUiSelectionListener listener : mListeners) {
                try {
                    listener.selectionChanged(selectedDevice, selectedClient);
                } catch (Exception e) {
                }
            }
        }
    }
}
