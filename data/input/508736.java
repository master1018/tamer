public class InfoPanel extends TablePanel {
    private Table mTable;
    private TableColumn mCol2;
    private static final String mLabels[] = {
        "DDM-aware?",
        "App description:",
        "VM version:",
        "Process ID:",
        "Supports Profiling Control:",
        "Supports HPROF Control:",
    };
    private static final int ENT_DDM_AWARE          = 0;
    private static final int ENT_APP_DESCR          = 1;
    private static final int ENT_VM_VERSION         = 2;
    private static final int ENT_PROCESS_ID         = 3;
    private static final int ENT_SUPPORTS_PROFILING = 4;
    private static final int ENT_SUPPORTS_HPROF     = 5;
    @Override
    protected Control createControl(Composite parent) {
        mTable = new Table(parent, SWT.MULTI | SWT.FULL_SELECTION);
        mTable.setHeaderVisible(false);
        mTable.setLinesVisible(false);
        TableColumn col1 = new TableColumn(mTable, SWT.RIGHT);
        col1.setText("name");
        mCol2 = new TableColumn(mTable, SWT.LEFT);
        mCol2.setText("PlaceHolderContentForWidth");
        TableItem item;
        for (int i = 0; i < mLabels.length; i++) {
            item = new TableItem(mTable, SWT.NONE);
            item.setText(0, mLabels[i]);
            item.setText(1, "-");
        }
        col1.pack();
        mCol2.pack();
        return mTable;
    }
    @Override
    public void setFocus() {
        mTable.setFocus();
    }
    public void clientChanged(final Client client, int changeMask) {
        if (client == getCurrentClient()) {
            if ((changeMask & Client.CHANGE_INFO) == Client.CHANGE_INFO) {
                if (mTable.isDisposed())
                    return;
                mTable.getDisplay().asyncExec(new Runnable() {
                    public void run() {
                        clientSelected();
                    }
                });
            }
        }
    }
    @Override
    public void deviceSelected() {
    }
    @Override
    public void clientSelected() {
        if (mTable.isDisposed())
            return;
        Client client = getCurrentClient();
        if (client == null) {
            for (int i = 0; i < mLabels.length; i++) {
                TableItem item = mTable.getItem(i);
                item.setText(1, "-");
            }
        } else {
            TableItem item;
            String clientDescription, vmIdentifier, isDdmAware,
                pid;
            ClientData cd = client.getClientData();
            synchronized (cd) {
                clientDescription = (cd.getClientDescription() != null) ?
                        cd.getClientDescription() : "?";
                vmIdentifier = (cd.getVmIdentifier() != null) ?
                        cd.getVmIdentifier() : "?";
                isDdmAware = cd.isDdmAware() ?
                        "yes" : "no";
                pid = (cd.getPid() != 0) ?
                        String.valueOf(cd.getPid()) : "?";
            }
            item = mTable.getItem(ENT_APP_DESCR);
            item.setText(1, clientDescription);
            item = mTable.getItem(ENT_VM_VERSION);
            item.setText(1, vmIdentifier);
            item = mTable.getItem(ENT_DDM_AWARE);
            item.setText(1, isDdmAware);
            item = mTable.getItem(ENT_PROCESS_ID);
            item.setText(1, pid);
            item = mTable.getItem(ENT_SUPPORTS_PROFILING);
            if (cd.hasFeature(ClientData.FEATURE_PROFILING_STREAMING)) {
                item.setText(1, "Yes");
            } else if (cd.hasFeature(ClientData.FEATURE_PROFILING)) {
                item.setText(1, "Yes (Application must be able to write on the SD Card)");
            } else {
                item.setText(1, "No");
            }
            item = mTable.getItem(ENT_SUPPORTS_HPROF);
            if (cd.hasFeature(ClientData.FEATURE_HPROF_STREAMING)) {
                item.setText(1, "Yes");
            } else if (cd.hasFeature(ClientData.FEATURE_HPROF)) {
                item.setText(1, "Yes (Application must be able to write on the SD Card)");
            } else {
                item.setText(1, "No");
            }
        }
        mCol2.pack();
    }
    @Override
    protected void setTableFocusListener() {
        addTableToFocusListener(mTable);
    }
}
