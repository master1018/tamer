public class StaticPortConfigDialog extends Dialog {
    private static final String PREFS_DEVICE_COL = "spcd.deviceColumn"; 
    private static final String PREFS_APP_COL = "spcd.AppColumn"; 
    private static final String PREFS_PORT_COL = "spcd.PortColumn"; 
    private static final int COL_DEVICE = 0;
    private static final int COL_APPLICATION = 1;
    private static final int COL_PORT = 2;
    private static final int DLG_WIDTH = 500;
    private static final int DLG_HEIGHT = 300;
    private Shell mShell;
    private Shell mParent;
    private Table mPortTable;
    private ArrayList<Integer> mPorts = new ArrayList<Integer>();
    public StaticPortConfigDialog(Shell parent) {
        super(parent, SWT.DIALOG_TRIM | SWT.BORDER | SWT.APPLICATION_MODAL);
    }
    public void open() {
        createUI();
        if (mParent == null || mShell == null) {
            return;
        }
        updateFromStore();
        mShell.setMinimumSize(DLG_WIDTH, DLG_HEIGHT);
        Rectangle r = mParent.getBounds();
        int cx = r.x + r.width/2;
        int x = cx - DLG_WIDTH / 2;
        int cy = r.y + r.height/2;
        int y = cy - DLG_HEIGHT / 2;
        mShell.setBounds(x, y, DLG_WIDTH, DLG_HEIGHT);
        mShell.pack();
        mShell.open();
        Display display = mParent.getDisplay();
        while (!mShell.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }
    }
    private void createUI() {
        mParent = getParent();
        mShell = new Shell(mParent, getStyle());
        mShell.setText("Static Port Configuration");
        mShell.setLayout(new GridLayout(1, true));
        mShell.addListener(SWT.Close, new Listener() {
            public void handleEvent(Event event) {
                event.doit = true;
            }
        });
        Composite main = new Composite(mShell, SWT.NONE);
        main.setLayoutData(new GridData(GridData.FILL_BOTH));
        main.setLayout(new GridLayout(2, false));
        mPortTable = new Table(main, SWT.SINGLE | SWT.FULL_SELECTION);
        mPortTable.setLayoutData(new GridData(GridData.FILL_BOTH));
        mPortTable.setHeaderVisible(true);
        mPortTable.setLinesVisible(true);
        TableHelper.createTableColumn(mPortTable, "Device Serial Number",
                SWT.LEFT, "emulator-5554", 
                PREFS_DEVICE_COL, PrefsDialog.getStore());
        TableHelper.createTableColumn(mPortTable, "Application Package",
                SWT.LEFT, "com.android.samples.phone", 
                PREFS_APP_COL, PrefsDialog.getStore());
        TableHelper.createTableColumn(mPortTable, "Debug Port",
                SWT.RIGHT, "Debug Port", 
                PREFS_PORT_COL, PrefsDialog.getStore());
        Composite buttons = new Composite(main, SWT.NONE);
        buttons.setLayoutData(new GridData(GridData.FILL_VERTICAL));
        buttons.setLayout(new GridLayout(1, true));
        Button newButton = new Button(buttons, SWT.NONE);
        newButton.setText("New...");
        newButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                StaticPortEditDialog dlg = new StaticPortEditDialog(mShell,
                        mPorts);
                if (dlg.open()) {
                    String device = dlg.getDeviceSN();
                    String app = dlg.getAppName();
                    int port = dlg.getPortNumber();
                    addEntry(device, app, port);
                }
            }
        });
        final Button editButton = new Button(buttons, SWT.NONE);
        editButton.setText("Edit...");
        editButton.setEnabled(false);
        editButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                int index = mPortTable.getSelectionIndex();
                String oldDeviceName = getDeviceName(index);
                String oldAppName = getAppName(index);
                String oldPortNumber = getPortNumber(index);
                StaticPortEditDialog dlg = new StaticPortEditDialog(mShell,
                        mPorts, oldDeviceName, oldAppName, oldPortNumber);
                if (dlg.open()) {
                    String deviceName = dlg.getDeviceSN();
                    String app = dlg.getAppName();
                    int port = dlg.getPortNumber();
                    replaceEntry(index, deviceName, app, port);
                }
            }
        });
        final Button deleteButton = new Button(buttons, SWT.NONE);
        deleteButton.setText("Delete");
        deleteButton.setEnabled(false);
        deleteButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                int index = mPortTable.getSelectionIndex();
                removeEntry(index);
            }
        });
        Composite bottomComp = new Composite(mShell, SWT.NONE);
        bottomComp.setLayoutData(new GridData(
                GridData.HORIZONTAL_ALIGN_CENTER));
        bottomComp.setLayout(new GridLayout(2, true));
        Button okButton = new Button(bottomComp, SWT.NONE);
        okButton.setText("OK");
        okButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                updateStore();
                mShell.close();
            }
        });
        Button cancelButton = new Button(bottomComp, SWT.NONE);
        cancelButton.setText("Cancel");
        cancelButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                mShell.close();
            }
        });
        mPortTable.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                int index = mPortTable.getSelectionIndex();
                boolean enabled = index != -1;
                editButton.setEnabled(enabled);
                deleteButton.setEnabled(enabled);
            }
        });
        mShell.pack();
    }
    private void addEntry(String deviceName, String appName, int portNumber) {
        TableItem item = new TableItem(mPortTable, SWT.NONE);
        item.setText(COL_DEVICE, deviceName);
        item.setText(COL_APPLICATION, appName);
        item.setText(COL_PORT, Integer.toString(portNumber));
        mPorts.add(portNumber);
    }
    private void removeEntry(int index) {
        mPortTable.remove(index);
        mPorts.remove(index);
    }
    private void replaceEntry(int index, String deviceName, String appName, int portNumber) {
        TableItem item = mPortTable.getItem(index);
        item.setText(COL_DEVICE, deviceName);
        item.setText(COL_APPLICATION, appName);
        item.setText(COL_PORT, Integer.toString(portNumber));
        mPorts.set(index, portNumber);
    }
    private String getDeviceName(int index) {
        TableItem item = mPortTable.getItem(index);
        return item.getText(COL_DEVICE);
    }
    private String getAppName(int index) {
        TableItem item = mPortTable.getItem(index);
        return item.getText(COL_APPLICATION);
    }
    private String getPortNumber(int index) {
        TableItem item = mPortTable.getItem(index);
        return item.getText(COL_PORT);
    }
    private void updateFromStore() {
        DebugPortProvider provider = DebugPortProvider.getInstance();
        Map<String, Map<String, Integer>> map = provider.getPortList();
        Set<String> deviceKeys = map.keySet();
        for (String deviceKey : deviceKeys) {
            Map<String, Integer> deviceMap = map.get(deviceKey);
            if (deviceMap != null) {
                Set<String> appKeys = deviceMap.keySet();
                for (String appKey : appKeys) {
                    Integer port = deviceMap.get(appKey);
                    if (port != null) {
                        addEntry(deviceKey, appKey, port);
                    }
                }
            }
        }
    }
    private void updateStore() {
        HashMap<String, Map<String, Integer>> map = new HashMap<String, Map<String, Integer>>();
        int count = mPortTable.getItemCount();
        for (int i = 0 ; i < count ; i++) {
            TableItem item = mPortTable.getItem(i);
            String deviceName = item.getText(COL_DEVICE);
            Map<String, Integer> deviceMap = map.get(deviceName);
            if (deviceMap == null) {
                deviceMap = new HashMap<String, Integer>();
                map.put(deviceName, deviceMap);
            }
            deviceMap.put(item.getText(COL_APPLICATION), Integer.valueOf(item.getText(COL_PORT)));
        }
        DebugPortProvider provider = DebugPortProvider.getInstance();
        provider.setPortList(map);
    }
}
