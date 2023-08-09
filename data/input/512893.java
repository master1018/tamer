public class AllocationPanel extends TablePanel {
    private final static String PREFS_ALLOC_COL_SIZE = "allocPanel.Col0"; 
    private final static String PREFS_ALLOC_COL_CLASS = "allocPanel.Col1"; 
    private final static String PREFS_ALLOC_COL_THREAD = "allocPanel.Col2"; 
    private final static String PREFS_ALLOC_COL_TRACE_CLASS = "allocPanel.Col3"; 
    private final static String PREFS_ALLOC_COL_TRACE_METHOD = "allocPanel.Col4"; 
    private final static String PREFS_ALLOC_SASH = "allocPanel.sash"; 
    private static final String PREFS_STACK_COL_CLASS = "allocPanel.stack.col0"; 
    private static final String PREFS_STACK_COL_METHOD = "allocPanel.stack.col1"; 
    private static final String PREFS_STACK_COL_FILE = "allocPanel.stack.col2"; 
    private static final String PREFS_STACK_COL_LINE = "allocPanel.stack.col3"; 
    private static final String PREFS_STACK_COL_NATIVE = "allocPanel.stack.col4"; 
    private Composite mAllocationBase;
    private Table mAllocationTable;
    private TableViewer mAllocationViewer;
    private StackTracePanel mStackTracePanel;
    private Table mStackTraceTable;
    private Button mEnableButton;
    private Button mRequestButton;
    private static class AllocationContentProvider implements IStructuredContentProvider {
        public Object[] getElements(Object inputElement) {
            if (inputElement instanceof Client) {
                AllocationInfo[] allocs = ((Client)inputElement).getClientData().getAllocations();
                if (allocs != null) {
                    return allocs;
                }
            }
            return new Object[0];
        }
        public void dispose() {
        }
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }
    }
    private static class AllocationLabelProvider implements ITableLabelProvider {
        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }
        public String getColumnText(Object element, int columnIndex) {
            if (element instanceof AllocationInfo) {
                AllocationInfo alloc = (AllocationInfo)element;
                switch (columnIndex) {
                    case 0:
                        return Integer.toString(alloc.getSize());
                    case 1:
                        return alloc.getAllocatedClass();
                    case 2:
                        return Short.toString(alloc.getThreadId());
                    case 3:
                        StackTraceElement[] traces = alloc.getStackTrace();
                        if (traces.length > 0) {
                            return traces[0].getClassName();
                        }
                        break;
                    case 4:
                        traces = alloc.getStackTrace();
                        if (traces.length > 0) {
                            return traces[0].getMethodName();
                        }
                        break;
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
    @Override
    protected Control createControl(Composite parent) {
        final IPreferenceStore store = DdmUiPreferences.getStore();
        mAllocationBase = new Composite(parent, SWT.NONE);
        mAllocationBase.setLayout(new FormLayout());
        Composite topParent = new Composite(mAllocationBase, SWT.NONE);
        topParent.setLayout(new GridLayout(2, false));
        mEnableButton = new Button(topParent, SWT.PUSH);
        mEnableButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Client current = getCurrentClient();
                AllocationTrackingStatus status = current.getClientData().getAllocationStatus();
                if (status == AllocationTrackingStatus.ON) {
                    current.enableAllocationTracker(false);
                } else {
                    current.enableAllocationTracker(true);
                }
                current.requestAllocationStatus();
            }
        });
        mRequestButton = new Button(topParent, SWT.PUSH);
        mRequestButton.setText("Get Allocations");
        mRequestButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                getCurrentClient().requestAllocationDetails();
            }
        });
        setUpButtons(false , AllocationTrackingStatus.OFF);
        mAllocationTable = new Table(topParent, SWT.MULTI | SWT.FULL_SELECTION);
        GridData gridData;
        mAllocationTable.setLayoutData(gridData = new GridData(GridData.FILL_BOTH));
        gridData.horizontalSpan = 2;
        mAllocationTable.setHeaderVisible(true);
        mAllocationTable.setLinesVisible(true);
        TableHelper.createTableColumn(
                mAllocationTable,
                "Allocation Size",
                SWT.RIGHT,
                "888", 
                PREFS_ALLOC_COL_SIZE, store);
        TableHelper.createTableColumn(
                mAllocationTable,
                "Allocated Class",
                SWT.LEFT,
                "Allocated Class", 
                PREFS_ALLOC_COL_CLASS, store);
        TableHelper.createTableColumn(
                mAllocationTable,
                "Thread Id",
                SWT.LEFT,
                "999", 
                PREFS_ALLOC_COL_THREAD, store);
        TableHelper.createTableColumn(
                mAllocationTable,
                "Allocated in",
                SWT.LEFT,
                "utime", 
                PREFS_ALLOC_COL_TRACE_CLASS, store);
        TableHelper.createTableColumn(
                mAllocationTable,
                "Allocated in",
                SWT.LEFT,
                "utime", 
                PREFS_ALLOC_COL_TRACE_METHOD, store);
        mAllocationViewer = new TableViewer(mAllocationTable);
        mAllocationViewer.setContentProvider(new AllocationContentProvider());
        mAllocationViewer.setLabelProvider(new AllocationLabelProvider());
        mAllocationViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
                AllocationInfo selectedAlloc = getAllocationSelection(event.getSelection());
                updateAllocationStackTrace(selectedAlloc);
            }
        });
        final Sash sash = new Sash(mAllocationBase, SWT.HORIZONTAL);
        Color darkGray = parent.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY);
        sash.setBackground(darkGray);
        mStackTracePanel = new StackTracePanel();
        mStackTraceTable = mStackTracePanel.createPanel(mAllocationBase,
                PREFS_STACK_COL_CLASS,
                PREFS_STACK_COL_METHOD,
                PREFS_STACK_COL_FILE,
                PREFS_STACK_COL_LINE,
                PREFS_STACK_COL_NATIVE,
                store);
        FormData data = new FormData();
        data.top = new FormAttachment(0, 0);
        data.bottom = new FormAttachment(sash, 0);
        data.left = new FormAttachment(0, 0);
        data.right = new FormAttachment(100, 0);
        topParent.setLayoutData(data);
        final FormData sashData = new FormData();
        if (store != null && store.contains(PREFS_ALLOC_SASH)) {
            sashData.top = new FormAttachment(0, store.getInt(PREFS_ALLOC_SASH));
        } else {
            sashData.top = new FormAttachment(50,0); 
        }
        sashData.left = new FormAttachment(0, 0);
        sashData.right = new FormAttachment(100, 0);
        sash.setLayoutData(sashData);
        data = new FormData();
        data.top = new FormAttachment(sash, 0);
        data.bottom = new FormAttachment(100, 0);
        data.left = new FormAttachment(0, 0);
        data.right = new FormAttachment(100, 0);
        mStackTraceTable.setLayoutData(data);
        sash.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                Rectangle sashRect = sash.getBounds();
                Rectangle panelRect = mAllocationBase.getClientArea();
                int bottom = panelRect.height - sashRect.height - 100;
                e.y = Math.max(Math.min(e.y, bottom), 100);
                if (e.y != sashRect.y) {
                    sashData.top = new FormAttachment(0, e.y);
                    store.setValue(PREFS_ALLOC_SASH, e.y);
                    mAllocationBase.layout();
                }
            }
        });
        return mAllocationBase;
    }
    @Override
    public void setFocus() {
        mAllocationTable.setFocus();
    }
    public void clientChanged(final Client client, int changeMask) {
        if (client == getCurrentClient()) {
            if ((changeMask & Client.CHANGE_HEAP_ALLOCATIONS) != 0) {
                try {
                    mAllocationTable.getDisplay().asyncExec(new Runnable() {
                        public void run() {
                            mAllocationViewer.refresh();
                            updateAllocationStackCall();
                        }
                    });
                } catch (SWTException e) {
                }
            } else if ((changeMask & Client.CHANGE_HEAP_ALLOCATION_STATUS) != 0) {
                try {
                    mAllocationTable.getDisplay().asyncExec(new Runnable() {
                        public void run() {
                            setUpButtons(true, client.getClientData().getAllocationStatus());
                        }
                    });
                } catch (SWTException e) {
                }
            }
        }
    }
    @Override
    public void deviceSelected() {
    }
    @Override
    public void clientSelected() {
        if (mAllocationTable.isDisposed()) {
            return;
        }
        Client client = getCurrentClient();
        mStackTracePanel.setCurrentClient(client);
        mStackTracePanel.setViewerInput(null); 
        if (client != null) {
            setUpButtons(true , client.getClientData().getAllocationStatus());
        } else {
            setUpButtons(false , AllocationTrackingStatus.OFF);
        }
        mAllocationViewer.setInput(client);
    }
    private void updateAllocationStackCall() {
        Client client = getCurrentClient();
        if (client != null) {
            AllocationInfo selectedAlloc = getAllocationSelection(null);
            if (selectedAlloc != null) {
                updateAllocationStackTrace(selectedAlloc);
            } else {
                updateAllocationStackTrace(null);
            }
        }
    }
    private void updateAllocationStackTrace(AllocationInfo alloc) {
        mStackTracePanel.setViewerInput(alloc);
    }
    @Override
    protected void setTableFocusListener() {
        addTableToFocusListener(mAllocationTable);
        addTableToFocusListener(mStackTraceTable);
    }
    private AllocationInfo getAllocationSelection(ISelection selection) {
        if (selection == null) {
            selection = mAllocationViewer.getSelection();
        }
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection structuredSelection = (IStructuredSelection)selection;
            Object object = structuredSelection.getFirstElement();
            if (object instanceof AllocationInfo) {
                return (AllocationInfo)object;
            }
        }
        return null;
    }
    private void setUpButtons(boolean enabled, AllocationTrackingStatus trackingStatus) {
        if (enabled) {
            switch (trackingStatus) {
                case UNKNOWN:
                    mEnableButton.setText("?");
                    mEnableButton.setEnabled(false);
                    mRequestButton.setEnabled(false);
                    break;
                case OFF:
                    mEnableButton.setText("Start Tracking");
                    mEnableButton.setEnabled(true);
                    mRequestButton.setEnabled(false);
                    break;
                case ON:
                    mEnableButton.setText("Stop Tracking");
                    mEnableButton.setEnabled(true);
                    mRequestButton.setEnabled(true);
                    break;
            }
        } else {
            mEnableButton.setEnabled(false);
            mRequestButton.setEnabled(false);
            mEnableButton.setText("Start Tracking");
        }
    }
}
