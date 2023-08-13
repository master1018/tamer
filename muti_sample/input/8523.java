public class JavaThreadsPanel extends SAPanel implements ActionListener {
    private JavaThreadsTableModel dataModel;
    private StatusBar statusBar;
    private JTable     threadTable;
    private java.util.List cachedThreads = new ArrayList();
    public JavaThreadsPanel() {
        VM.getVM().registerVMResumedObserver(new Observer() {
                public void update(Observable o, Object data) {
                    decache();
                }
            });
        VM.getVM().registerVMSuspendedObserver(new Observer() {
                public void update(Observable o, Object data) {
                    cache();
                }
            });
        cache();
        setLayout(new BorderLayout());
        dataModel = new JavaThreadsTableModel(cachedThreads);
        statusBar = new StatusBar();
        threadTable = new JTable(dataModel, new JavaThreadsColumnModel());
        threadTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        threadTable.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent evt) {
                    if (evt.getClickCount() == 2) {
                        fireShowThreadOopInspector();
                    }
                }
            });
        add(new JavaThreadsToolBar(statusBar), BorderLayout.NORTH);
        add(new ThreadPanel(threadTable), BorderLayout.CENTER);
        add(statusBar, BorderLayout.SOUTH);
        registerActions();
    }
    private class ThreadPanel extends JPanel {
        private JSplitPane splitPane;
        private JTable threadTable;
        private ThreadInfoPanel threadInfo;
        private int dividerSize;
        private int dividerLocation = -1;
        private boolean actionsEnabled = false;
        public ThreadPanel(JTable table) {
            setLayout(new BorderLayout());
            this.threadInfo = new ThreadInfoPanel();
            this.threadTable = table;
            splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
            splitPane.setOneTouchExpandable(true);
            splitPane.setTopComponent(new JScrollPane(table));
            dividerSize = splitPane.getDividerSize();
            splitPane.setDividerSize(0);
            add(splitPane, BorderLayout.CENTER);
            ActionManager manager = HSDBActionManager.getInstance();
            StateChangeAction action = manager.getStateChangeAction(ThreadInfoAction.VALUE_COMMAND);
            if (action != null) {
                action.setItemListener(new ItemListener() {
                        public void itemStateChanged(ItemEvent evt) {
                            if (evt.getStateChange() == ItemEvent.SELECTED) {
                                showOutputPane();
                            } else {
                                hideOutputPane();
                            }
                        }
                    });
            }
            ListSelectionModel selModel = table.getSelectionModel();
            selModel.addListSelectionListener(new ListSelectionListener() {
                    public void valueChanged(ListSelectionEvent evt) {
                        if (evt.getValueIsAdjusting() == false) {
                            setActionsEnabled(true);
                            if (isInfoVisible()) {
                                showCurrentThreadInfo();
                            }
                        }
                    }
                });
        }
        private boolean isInfoVisible() {
            return (splitPane.getBottomComponent() != null);
        }
        private void showOutputPane()  {
            if (splitPane.getBottomComponent() == null)  {
                splitPane.setBottomComponent(threadInfo);
                if (dividerLocation == -1)  {
                    Dimension pSize = this.getSize();
                    dividerLocation = pSize.height / 2;
                }
                splitPane.setDividerSize(dividerSize);
                splitPane.setDividerLocation(dividerLocation);
                showCurrentThreadInfo();
            }
        }
        private void hideOutputPane()  {
            dividerLocation = splitPane.getDividerLocation();
            splitPane.remove(threadInfo);
            splitPane.setDividerSize(0);
        }
        private void showCurrentThreadInfo() {
            int row = threadTable.getSelectedRow();
            if (row >= 0) {
                threadInfo.setJavaThread(dataModel.getJavaThread(row));
            }
        }
        private void setActionsEnabled(boolean enabled) {
            if (actionsEnabled != enabled) {
                ActionManager manager = ActionManager.getInstance();
                manager.setActionEnabled(InspectAction.VALUE_COMMAND, enabled);
                manager.setActionEnabled(MemoryAction.VALUE_COMMAND, enabled);
                manager.setActionEnabled(JavaStackTraceAction.VALUE_COMMAND, enabled);
                actionsEnabled = enabled;
            }
        }
    } 
    private class JavaThreadsToolBar extends CommonToolBar {
        public JavaThreadsToolBar(StatusBar status) {
            super(HSDBActionManager.getInstance(), status);
        }
        protected void addComponents() {
            addButton(manager.getAction(InspectAction.VALUE_COMMAND));
            addButton(manager.getAction(MemoryAction.VALUE_COMMAND));
            addButton(manager.getAction(JavaStackTraceAction.VALUE_COMMAND));
            addToggleButton(manager.getStateChangeAction(ThreadInfoAction.VALUE_COMMAND));
            addButton(manager.getAction(FindCrashesAction.VALUE_COMMAND));
        }
    }
    private class JavaThreadsColumnModel extends DefaultTableColumnModel {
        private String[] columnNames = { "OS Thread ID", "Java Thread Name" };
        public JavaThreadsColumnModel() {
            int PREF_WIDTH = 80;
            int MAX_WIDTH = 100;
            int HUGE_WIDTH = 140;
            TableColumn column;
            column = new TableColumn(0, MAX_WIDTH);
            column.setHeaderValue(columnNames[0]);
            column.setMaxWidth(MAX_WIDTH);
            column.setResizable(false);
            addColumn(column);
            column = new TableColumn(1, HUGE_WIDTH);
            column.setHeaderValue(columnNames[1]);
            column.setResizable(false);
            addColumn(column);
        }
    } 
    private class JavaThreadsTableModel extends AbstractTableModel {
        private String[] columnNames = { "OS Thread ID", "Java Thread Name" };
        private java.util.List elements;
        public JavaThreadsTableModel(java.util.List threads) {
            this.elements = threads;
        }
        public int getColumnCount() {
            return columnNames.length;
        }
        public int getRowCount() {
            return elements.size();
        }
        public String getColumnName(int col) {
            return columnNames[col];
        }
        public Object getValueAt(int row, int col) {
            CachedThread thread = getRow(row);
            switch (col) {
            case 0:
                return thread.getThreadID();
            case 1:
                return thread.getThreadName();
            default:
                throw new RuntimeException("Index (" + col + ", " + row + ") out of bounds");
            }
        }
        public JavaThread getJavaThread(int index) {
            return getRow(index).getThread();
        }
        private CachedThread getRow(int row) {
            return (CachedThread)elements.get(row);
        }
        private String threadIDAt(int index) {
            return ((CachedThread) cachedThreads.get(index)).getThreadID();
        }
        private String threadNameAt(int index) {
            try {
                return ((CachedThread) cachedThreads.get(index)).getThreadName();
            } catch (AddressException e) {
                return "<Error: AddressException>";
            } catch (NullPointerException e) {
                return "<Error: NullPointerException>";
            }
        }
    } 
    public void actionPerformed(ActionEvent evt) {
        String command = evt.getActionCommand();
        if (command.equals(InspectAction.VALUE_COMMAND)) {
            fireShowThreadOopInspector();
        } else if (command.equals(MemoryAction.VALUE_COMMAND)) {
            fireShowThreadStackMemory();
        } else if (command.equals(ThreadInfoAction.VALUE_COMMAND)) {
            fireShowThreadInfo();
        } else if (command.equals(FindCrashesAction.VALUE_COMMAND)) {
            if (fireShowThreadCrashes()) {
                statusBar.setMessage("Some thread crashes were encountered");
            } else {
                statusBar.setMessage("No thread crashes encountered");
            }
        } else if (command.equals(JavaStackTraceAction.VALUE_COMMAND)) {
           fireShowJavaStackTrace();
        }
    }
    private class CachedThread {
        private JavaThread thread;
        private String     threadID;
        private String     threadName;
        private boolean    computed;
        public CachedThread(JavaThread thread) {
            this.thread = thread;
        }
        public JavaThread getThread() {
            return thread;
        }
        public String getThreadID() {
            if (!computed) {
                compute();
            }
            return threadID;
        }
        public String getThreadName() {
            if (!computed) {
                compute();
            }
            return threadName;
        }
        private void compute() {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            thread.printThreadIDOn(new PrintStream(bos));
            threadID   = bos.toString();
            threadName = thread.getThreadName();
            computed = true;
        }
    }
    protected void registerActions() {
        registerAction(InspectAction.VALUE_COMMAND);
        registerAction(MemoryAction.VALUE_COMMAND);
        registerAction(FindCrashesAction.VALUE_COMMAND);
        registerAction(JavaStackTraceAction.VALUE_COMMAND);
        ActionManager manager = ActionManager.getInstance();
        manager.setActionEnabled(InspectAction.VALUE_COMMAND, false);
        manager.setActionEnabled(MemoryAction.VALUE_COMMAND, false);
        manager.setActionEnabled(JavaStackTraceAction.VALUE_COMMAND, false);
    }
    private void registerAction(String actionName) {
        ActionManager manager = ActionManager.getInstance();
        DelegateAction action = manager.getDelegateAction(actionName);
        action.addActionListener(this);
    }
    private void fireShowThreadOopInspector() {
        int i = threadTable.getSelectedRow();
        if (i < 0) {
            return;
        }
        JavaThread t = dataModel.getJavaThread(i);
        showThreadOopInspector(t);
    }
    private void fireShowThreadStackMemory() {
        int i = threadTable.getSelectedRow();
        if (i < 0) {
            return;
        }
        showThreadStackMemory(dataModel.getJavaThread(i));
    }
    private void fireShowJavaStackTrace() {
        int i = threadTable.getSelectedRow();
        if (i < 0) {
            return;
        }
        showJavaStackTrace(dataModel.getJavaThread(i));
    }
    private void fireShowThreadInfo() {
        int i = threadTable.getSelectedRow();
        if (i < 0) {
            return;
        }
        showThreadInfo(dataModel.getJavaThread(i));
    }
    private boolean fireShowThreadCrashes() {
        boolean crash = false;
        for (Iterator iter = cachedThreads.iterator(); iter.hasNext(); ) {
            JavaThread t = (JavaThread) ((CachedThread) iter.next()).getThread();
            sun.jvm.hotspot.runtime.Frame tmpFrame = t.getCurrentFrameGuess();
            RegisterMap tmpMap = t.newRegisterMap(false);
            while ((tmpFrame != null) && (!tmpFrame.isFirstFrame())) {
                if (tmpFrame.isSignalHandlerFrameDbg()) {
                    showThreadStackMemory(t);
                    crash = true;
                    break;
                }
                tmpFrame = tmpFrame.sender(tmpMap);
            }
        }
        return crash;
    }
    private void cache() {
        Threads threads = VM.getVM().getThreads();
        for (JavaThread t = threads.first(); t != null; t = t.next()) {
            if (t.isJavaThread()) {
                cachedThreads.add(new CachedThread(t));
            }
        }
    }
    private void decache() {
        cachedThreads.clear();
    }
}
