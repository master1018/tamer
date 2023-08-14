public class ThreadPanel extends TablePanel {
    private final static String PREFS_THREAD_COL_ID = "threadPanel.Col0"; 
    private final static String PREFS_THREAD_COL_TID = "threadPanel.Col1"; 
    private final static String PREFS_THREAD_COL_STATUS = "threadPanel.Col2"; 
    private final static String PREFS_THREAD_COL_UTIME = "threadPanel.Col3"; 
    private final static String PREFS_THREAD_COL_STIME = "threadPanel.Col4"; 
    private final static String PREFS_THREAD_COL_NAME = "threadPanel.Col5"; 
    private final static String PREFS_THREAD_SASH = "threadPanel.sash"; 
    private static final String PREFS_STACK_COL_CLASS = "threadPanel.stack.col0"; 
    private static final String PREFS_STACK_COL_METHOD = "threadPanel.stack.col1"; 
    private static final String PREFS_STACK_COL_FILE = "threadPanel.stack.col2"; 
    private static final String PREFS_STACK_COL_LINE = "threadPanel.stack.col3"; 
    private static final String PREFS_STACK_COL_NATIVE = "threadPanel.stack.col4"; 
    private Display mDisplay;
    private Composite mBase;
    private Label mNotEnabled;
    private Label mNotSelected;
    private Composite mThreadBase;
    private Table mThreadTable;
    private TableViewer mThreadViewer;
    private Composite mStackTraceBase;
    private Button mRefreshStackTraceButton;
    private Label mStackTraceTimeLabel;
    private StackTracePanel mStackTracePanel;
    private Table mStackTraceTable;
    private boolean mMustStopRecurringThreadUpdate = false;
    private boolean mRecurringThreadUpdateRunning = false;
    private Object mLock = new Object();
    private static final String[] THREAD_STATUS = {
        "zombie", "running", "timed-wait", "monitor",
        "wait", "init", "start", "native", "vmwait"
    };
    private static class ThreadContentProvider implements IStructuredContentProvider {
        public Object[] getElements(Object inputElement) {
            if (inputElement instanceof Client) {
                return ((Client)inputElement).getClientData().getThreads();
            }
            return new Object[0];
        }
        public void dispose() {
        }
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }
    }
    private static class ThreadLabelProvider implements ITableLabelProvider {
        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }
        public String getColumnText(Object element, int columnIndex) {
            if (element instanceof ThreadInfo) {
                ThreadInfo thread = (ThreadInfo)element;
                switch (columnIndex) {
                    case 0:
                        return (thread.isDaemon() ? "*" : "") + 
                            String.valueOf(thread.getThreadId());
                    case 1:
                        return String.valueOf(thread.getTid());
                    case 2:
                        if (thread.getStatus() >= 0 && thread.getStatus() < THREAD_STATUS.length)
                            return THREAD_STATUS[thread.getStatus()];
                        return "unknown";
                    case 3:
                        return String.valueOf(thread.getUtime());
                    case 4:
                        return String.valueOf(thread.getStime());
                    case 5:
                        return thread.getThreadName();
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
        mDisplay = parent.getDisplay();
        final IPreferenceStore store = DdmUiPreferences.getStore();
        mBase = new Composite(parent, SWT.NONE);
        mBase.setLayout(new StackLayout());
        mNotEnabled = new Label(mBase, SWT.CENTER | SWT.WRAP);
        mNotEnabled.setText("Thread updates not enabled for selected client\n"
            + "(use toolbar button to enable)");
        mNotSelected = new Label(mBase, SWT.CENTER | SWT.WRAP);
        mNotSelected.setText("no client is selected");
        mThreadBase = new Composite(mBase, SWT.NONE);
        mThreadBase.setLayout(new FormLayout());
        mThreadTable = new Table(mThreadBase, SWT.MULTI | SWT.FULL_SELECTION);
        mThreadTable.setHeaderVisible(true);
        mThreadTable.setLinesVisible(true);
        TableHelper.createTableColumn(
                mThreadTable,
                "ID",
                SWT.RIGHT,
                "888", 
                PREFS_THREAD_COL_ID, store);
        TableHelper.createTableColumn(
                mThreadTable,
                "Tid",
                SWT.RIGHT,
                "88888", 
                PREFS_THREAD_COL_TID, store);
        TableHelper.createTableColumn(
                mThreadTable,
                "Status",
                SWT.LEFT,
                "timed-wait", 
                PREFS_THREAD_COL_STATUS, store);
        TableHelper.createTableColumn(
                mThreadTable,
                "utime",
                SWT.RIGHT,
                "utime", 
                PREFS_THREAD_COL_UTIME, store);
        TableHelper.createTableColumn(
                mThreadTable,
                "stime",
                SWT.RIGHT,
                "utime", 
                PREFS_THREAD_COL_STIME, store);
        TableHelper.createTableColumn(
                mThreadTable,
                "Name",
                SWT.LEFT,
                "android.class.ReallyLongClassName.MethodName", 
                PREFS_THREAD_COL_NAME, store);
        mThreadViewer = new TableViewer(mThreadTable);
        mThreadViewer.setContentProvider(new ThreadContentProvider());
        mThreadViewer.setLabelProvider(new ThreadLabelProvider());
        mThreadViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
                ThreadInfo selectedThread = getThreadSelection(event.getSelection());
                updateThreadStackTrace(selectedThread);
            }
        });
        mThreadViewer.addDoubleClickListener(new IDoubleClickListener() {
            public void doubleClick(DoubleClickEvent event) {
                ThreadInfo selectedThread = getThreadSelection(event.getSelection());
                if (selectedThread != null) {
                    Client client = (Client)mThreadViewer.getInput();
                    if (client != null) {
                        client.requestThreadStackTrace(selectedThread.getThreadId());
                    }
                }
            }
        });
        final Sash sash = new Sash(mThreadBase, SWT.HORIZONTAL);
        Color darkGray = parent.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY);
        sash.setBackground(darkGray);
        mStackTraceBase = new Composite(mThreadBase, SWT.NONE);
        mStackTraceBase.setLayout(new GridLayout(2, false));
        mRefreshStackTraceButton = new Button(mStackTraceBase, SWT.PUSH);
        mRefreshStackTraceButton.setText("Refresh");
        mRefreshStackTraceButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                ThreadInfo selectedThread = getThreadSelection(null);
                if (selectedThread != null) {
                    Client currentClient = getCurrentClient();
                    if (currentClient != null) {
                        currentClient.requestThreadStackTrace(selectedThread.getThreadId());
                    }
                }
            }
        });
        mStackTraceTimeLabel = new Label(mStackTraceBase, SWT.NONE);
        mStackTraceTimeLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        mStackTracePanel = new StackTracePanel();
        mStackTraceTable = mStackTracePanel.createPanel(mStackTraceBase,
                PREFS_STACK_COL_CLASS,
                PREFS_STACK_COL_METHOD,
                PREFS_STACK_COL_FILE,
                PREFS_STACK_COL_LINE,
                PREFS_STACK_COL_NATIVE,
                store);
        GridData gd;
        mStackTraceTable.setLayoutData(gd = new GridData(GridData.FILL_BOTH));
        gd.horizontalSpan = 2;
        FormData data = new FormData();
        data.top = new FormAttachment(0, 0);
        data.bottom = new FormAttachment(sash, 0);
        data.left = new FormAttachment(0, 0);
        data.right = new FormAttachment(100, 0);
        mThreadTable.setLayoutData(data);
        final FormData sashData = new FormData();
        if (store != null && store.contains(PREFS_THREAD_SASH)) {
            sashData.top = new FormAttachment(0, store.getInt(PREFS_THREAD_SASH));
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
        mStackTraceBase.setLayoutData(data);
        sash.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                Rectangle sashRect = sash.getBounds();
                Rectangle panelRect = mThreadBase.getClientArea();
                int bottom = panelRect.height - sashRect.height - 100;
                e.y = Math.max(Math.min(e.y, bottom), 100);
                if (e.y != sashRect.y) {
                    sashData.top = new FormAttachment(0, e.y);
                    store.setValue(PREFS_THREAD_SASH, e.y);
                    mThreadBase.layout();
                }
            }
        });
        ((StackLayout)mBase.getLayout()).topControl = mNotSelected;
        return mBase;
    }
    @Override
    public void setFocus() {
        mThreadTable.setFocus();
    }
    public void clientChanged(final Client client, int changeMask) {
        if (client == getCurrentClient()) {
            if ((changeMask & Client.CHANGE_THREAD_MODE) != 0 ||
                    (changeMask & Client.CHANGE_THREAD_DATA) != 0) {
                try {
                    mThreadTable.getDisplay().asyncExec(new Runnable() {
                        public void run() {
                            clientSelected();
                        }
                    });
                } catch (SWTException e) {
                }
            } else if ((changeMask & Client.CHANGE_THREAD_STACKTRACE) != 0) {
                try {
                    mThreadTable.getDisplay().asyncExec(new Runnable() {
                        public void run() {
                            updateThreadStackCall();
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
        if (mThreadTable.isDisposed()) {
            return;
        }
        Client client = getCurrentClient();
        mStackTracePanel.setCurrentClient(client);
        if (client != null) {
            if (!client.isThreadUpdateEnabled()) {
                ((StackLayout)mBase.getLayout()).topControl = mNotEnabled;
                mThreadViewer.setInput(null);
                mMustStopRecurringThreadUpdate = true;
            } else {
                ((StackLayout)mBase.getLayout()).topControl = mThreadBase;
                mThreadViewer.setInput(client);
                synchronized (mLock) {
                    if (mRecurringThreadUpdateRunning == false) {
                        startRecurringThreadUpdate();
                    } else if (mMustStopRecurringThreadUpdate) {
                        mMustStopRecurringThreadUpdate = false;
                    }
                }
            }
        } else {
            ((StackLayout)mBase.getLayout()).topControl = mNotSelected;
            mThreadViewer.setInput(null);
        }
        mBase.layout();
    }
    private void updateThreadStackCall() {
        Client client = getCurrentClient();
        if (client != null) {
            ThreadInfo selectedThread = getThreadSelection(null);
            if (selectedThread != null) {
                updateThreadStackTrace(selectedThread);
            } else {
                updateThreadStackTrace(null);
            }
        }
    }
    private void updateThreadStackTrace(ThreadInfo thread) {
        mStackTracePanel.setViewerInput(thread);
        if (thread != null) {
            mRefreshStackTraceButton.setEnabled(true);
            long stackcallTime = thread.getStackCallTime();
            if (stackcallTime != 0) {
                String label = new Date(stackcallTime).toString();
                mStackTraceTimeLabel.setText(label);
            } else {
                mStackTraceTimeLabel.setText(""); 
            }
        } else {
            mRefreshStackTraceButton.setEnabled(true);
            mStackTraceTimeLabel.setText(""); 
        }
    }
    @Override
    protected void setTableFocusListener() {
        addTableToFocusListener(mThreadTable);
        addTableToFocusListener(mStackTraceTable);
    }
    private void startRecurringThreadUpdate() {
        mRecurringThreadUpdateRunning = true;
        int initialWait = 1000;
        mDisplay.timerExec(initialWait, new Runnable() {
            public void run() {
                synchronized (mLock) {
                    if (mMustStopRecurringThreadUpdate == false) {
                        Client client = getCurrentClient();
                        if (client != null) {
                            client.requestThreadUpdate();
                            mDisplay.timerExec(
                                    DdmUiPreferences.getThreadRefreshInterval() * 1000, this);
                        } else {
                            mRecurringThreadUpdateRunning = false;
                        }
                    } else {
                        mRecurringThreadUpdateRunning = false;
                        mMustStopRecurringThreadUpdate = false;
                    }
                }
            }
        });
    }
    private ThreadInfo getThreadSelection(ISelection selection) {
        if (selection == null) {
            selection = mThreadViewer.getSelection();
        }
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection structuredSelection = (IStructuredSelection)selection;
            Object object = structuredSelection.getFirstElement();
            if (object instanceof ThreadInfo) {
                return (ThreadInfo)object;
            }
        }
        return null;
    }
}
