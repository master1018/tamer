public class LogPanel extends SelectionDependentPanel {
    private static final int STRING_BUFFER_LENGTH = 10000;
    public static final int FILTER_NONE = 0;
    public static final int FILTER_MANUAL = 1;
    public static final int FILTER_AUTO_PID = 2;
    public static final int FILTER_AUTO_TAG = 3;
    public static final int FILTER_DEBUG = 4;
    public static final int COLUMN_MODE_MANUAL = 0;
    public static final int COLUMN_MODE_AUTO = 1;
    public static String PREFS_TIME;
    public static String PREFS_LEVEL;
    public static String PREFS_PID;
    public static String PREFS_TAG;
    public static String PREFS_MESSAGE;
    private static Pattern sLogPattern = Pattern.compile(
            "^\\[\\s(\\d\\d-\\d\\d\\s\\d\\d:\\d\\d:\\d\\d\\.\\d+)" + 
            "\\s+(\\d*):(0x[0-9a-fA-F]+)\\s([VDIWE])/(.*)\\]$"); 
    public interface ILogFilterStorageManager {
        public LogFilter[] getFilterFromStore();
        public void saveFilters(LogFilter[] filters);
        public boolean requiresDefaultFilter();
    }
    private Composite mParent;
    private IPreferenceStore mStore;
    private TabFolder mFolders;
    private LogColors mColors;
    private ILogFilterStorageManager mFilterStorage;
    private LogCatOuputReceiver mCurrentLogCat;
    private LogMessage[] mBuffer = new LogMessage[STRING_BUFFER_LENGTH];
    private int mBufferStart = -1;
    private int mBufferEnd = -1;
    private LogFilter[] mFilters;
    private LogFilter mDefaultFilter;
    private LogFilter mCurrentFilter;
    private int mFilterMode = FILTER_NONE;
    private IDevice mCurrentLoggedDevice = null;
    private ICommonAction mDeleteFilterAction;
    private ICommonAction mEditFilterAction;
    private ICommonAction[] mLogLevelActions;
    protected static class LogMessageInfo {
        public LogLevel logLevel;
        public int pid;
        public String pidString;
        public String tag;
        public String time;
    }
    private LogMessageInfo mLastMessageInfo = null;
    private boolean mPendingAsyncRefresh = false;
    private IImageLoader mImageLoader;
    private String mDefaultLogSave;
    private int mColumnMode = COLUMN_MODE_MANUAL;
    private Font mDisplayFont;
    private ITableFocusListener mGlobalListener;
    protected static class LogMessage {
        public LogMessageInfo data;
        public String msg;
        @Override
        public String toString() {
            return data.time + ": " 
                + data.logLevel + "/" 
                + data.tag + "(" 
                + data.pidString + "): " 
                + msg;
        }
    }
    private final class LogCatOuputReceiver extends MultiLineReceiver {
        public boolean isCancelled = false;
        public LogCatOuputReceiver() {
            super();
            setTrimLine(false);
        }
        @Override
        public void processNewLines(String[] lines) {
            if (isCancelled == false) {
                processLogLines(lines);
            }
        }
        public boolean isCancelled() {
            return isCancelled;
        }
    }
    private class PsOutputReceiver extends MultiLineReceiver {
        private LogFilter mFilter;
        private TabItem mTabItem;
        private int mPid;
        private boolean mDone = false;
        PsOutputReceiver(int pid, LogFilter filter, TabItem tabItem) {
            mPid = pid;
            mFilter = filter;
            mTabItem = tabItem;
        }
        public boolean isCancelled() {
            return mDone;
        }
        @Override
        public void processNewLines(String[] lines) {
            for (String line : lines) {
                if (line.startsWith("USER")) { 
                    continue;
                }
                int index = line.indexOf(' ');
                if (index == -1) {
                    continue;
                }
                index++;
                while (line.charAt(index) == ' ') {
                    index++;
                }
                int index2 = line.indexOf(' ', index);
                String pidStr = line.substring(index, index2);
                int pid = Integer.parseInt(pidStr);
                if (pid != mPid) {
                    continue;
                } else {
                    index = line.lastIndexOf(' ');
                    final String name = line.substring(index + 1);
                    mFilter.setName(name);
                    Display d = mFolders.getDisplay();
                    d.asyncExec(new Runnable() {
                       public void run() {
                           mTabItem.setText(name);
                       }
                    });
                    mDone = true;
                    return;
                }
            }
        }
    }
    public LogPanel(IImageLoader imageLoader, LogColors colors,
            ILogFilterStorageManager filterStorage, int mode) {
        mImageLoader = imageLoader;
        mColors = colors;
        mFilterMode = mode;
        mFilterStorage = filterStorage;
        mStore = DdmUiPreferences.getStore();
    }
    public void setActions(ICommonAction deleteAction, ICommonAction editAction,
            ICommonAction[] logLevelActions) {
        mDeleteFilterAction = deleteAction;
        mEditFilterAction = editAction;
        mLogLevelActions = logLevelActions;
    }
    public void setColumnMode(int mode) {
        mColumnMode  = mode;
    }
    public void setFont(Font font) {
        mDisplayFont = font;
        if (mFilters != null) {
            for (LogFilter f : mFilters) {
                Table table = f.getTable();
                if (table != null) {
                    table.setFont(font);
                }
            }
        }
        if (mDefaultFilter != null) {
            Table table = mDefaultFilter.getTable();
            if (table != null) {
                table.setFont(font);
            }
        }
    }
    @Override
    public void deviceSelected() {
        startLogCat(getCurrentDevice());
    }
    @Override
    public void clientSelected() {
    }
    @Override
    protected Control createControl(Composite parent) {
        mParent = parent;
        Composite top = new Composite(parent, SWT.NONE);
        top.setLayoutData(new GridData(GridData.FILL_BOTH));
        top.setLayout(new GridLayout(1, false));
        mFolders = new TabFolder(top, SWT.NONE);
        mFolders.setLayoutData(new GridData(GridData.FILL_BOTH));
        mFolders.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (mCurrentFilter != null) {
                    mCurrentFilter.setSelectedState(false);
                }
                mCurrentFilter = getCurrentFilter();
                mCurrentFilter.setSelectedState(true);
                updateColumns(mCurrentFilter.getTable());
                if (mCurrentFilter.getTempFilterStatus()) {
                    initFilter(mCurrentFilter);
                }
                selectionChanged(mCurrentFilter);
            }
        });
        Composite bottom = new Composite(top, SWT.NONE);
        bottom.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        bottom.setLayout(new GridLayout(3, false));
        Label label = new Label(bottom, SWT.NONE);
        label.setText("Filter:");
        final Text filterText = new Text(bottom, SWT.SINGLE | SWT.BORDER);
        filterText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        filterText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                updateFilteringWith(filterText.getText());
            }
        });
        createFilters();
        int index = 0;
        if (mDefaultFilter != null) {
            createTab(mDefaultFilter, index++, false);
        }
        if (mFilters != null) {
            for (LogFilter f : mFilters) {
                createTab(f, index++, false);
            }
        }
        return top;
    }
    @Override
    protected void postCreation() {
    }
    @Override
    public void setFocus() {
        mFolders.setFocus();
    }
    public void startLogCat(final IDevice device) {
        if (device == mCurrentLoggedDevice) {
            return;
        }
        if (mCurrentLoggedDevice != null) {
            stopLogCat(false);
            mCurrentLoggedDevice = null;
        }
        resetUI(false);
        if (device != null) {
            mCurrentLogCat = new LogCatOuputReceiver();
            new Thread("Logcat")  { 
                @Override
                public void run() {
                    while (device.isOnline() == false &&
                            mCurrentLogCat != null &&
                            mCurrentLogCat.isCancelled == false) {
                        try {
                            sleep(2000);
                        } catch (InterruptedException e) {
                            return;
                        }
                    }
                    if (mCurrentLogCat == null || mCurrentLogCat.isCancelled) {
                        return;
                    }
                    try {
                        mCurrentLoggedDevice = device;
                        device.executeShellCommand("logcat -v long", mCurrentLogCat); 
                    } catch (Exception e) {
                        Log.e("Logcat", e);
                    } finally {
                        mCurrentLogCat = null;
                        mCurrentLoggedDevice = null;
                    }
                }
            }.start();
        }
    }
    public void stopLogCat(boolean inUiThread) {
        if (mCurrentLogCat != null) {
            mCurrentLogCat.isCancelled = true;
            mCurrentLogCat = null;
            for (int i = 0 ; i < STRING_BUFFER_LENGTH; i++) {
                mBuffer[i] = null;
            }
            mBufferStart = -1;
            mBufferEnd = -1;
            resetFilters();
            resetUI(inUiThread);
        }
    }
    public void addFilter() {
        EditFilterDialog dlg = new EditFilterDialog(mImageLoader,
                mFolders.getShell());
        if (dlg.open()) {
            synchronized (mBuffer) {
                LogFilter filter = dlg.getFilter();
                addFilterToArray(filter);
                int index = mFilters.length - 1;
                if (mDefaultFilter != null) {
                    index++;
                }
                if (false) {
                    for (LogFilter f : mFilters) {
                        if (f.uiReady()) {
                            f.dispose();
                        }
                    }
                    if (mDefaultFilter != null && mDefaultFilter.uiReady()) {
                        mDefaultFilter.dispose();
                    }
                    int i = 0;
                    if (mFilters != null) {
                        for (LogFilter f : mFilters) {
                            createTab(f, i++, true);
                        }
                    }
                    if (mDefaultFilter != null) {
                        createTab(mDefaultFilter, i++, true);
                    }
                } else {
                    createTab(filter, index, true);
                    if (mDefaultFilter != null) {
                        initDefaultFilter();
                    }
                }
                if (mCurrentFilter != null) {
                    mCurrentFilter.setSelectedState(false);
                }
                mFolders.setSelection(index);
                filter.setSelectedState(true);
                mCurrentFilter = filter;
                selectionChanged(filter);
                if (mFilterMode == FILTER_NONE) {
                    mFilterMode = FILTER_MANUAL;
                }
                mFilterStorage.saveFilters(mFilters);
            }
        }
    }
    public void editFilter() {
        if (mCurrentFilter != null && mCurrentFilter != mDefaultFilter) {
            EditFilterDialog dlg = new EditFilterDialog(mImageLoader,
                    mFolders.getShell(),
                    mCurrentFilter);
            if (dlg.open()) {
                synchronized (mBuffer) {
                    initFilter(mCurrentFilter);
                    if (mDefaultFilter != null) {
                        initDefaultFilter();
                    }
                    mFilterStorage.saveFilters(mFilters);
                }
            }
        }
    }
    public void deleteFilter() {
        synchronized (mBuffer) {
            if (mCurrentFilter != null && mCurrentFilter != mDefaultFilter) {
                removeFilterFromArray(mCurrentFilter);
                mCurrentFilter.dispose();
                mFolders.setSelection(0);
                if (mFilters.length > 0) {
                    mCurrentFilter = mFilters[0];
                } else {
                    mCurrentFilter = mDefaultFilter;
                }
                selectionChanged(mCurrentFilter);
                if (mDefaultFilter != null) {
                    initDefaultFilter();
                }
                mFilterStorage.saveFilters(mFilters);
            }
        }
    }
    public boolean save() {
        synchronized (mBuffer) {
            FileDialog dlg = new FileDialog(mParent.getShell(), SWT.SAVE);
            String fileName;
            dlg.setText("Save log...");
            dlg.setFileName("log.txt");
            String defaultPath = mDefaultLogSave;
            if (defaultPath == null) {
                defaultPath = System.getProperty("user.home"); 
            }
            dlg.setFilterPath(defaultPath);
            dlg.setFilterNames(new String[] {
                "Text Files (*.txt)"
            });
            dlg.setFilterExtensions(new String[] {
                "*.txt"
            });
            fileName = dlg.open();
            if (fileName != null) {
                mDefaultLogSave = dlg.getFilterPath();
                Table currentTable = mCurrentFilter.getTable();
                int[] selection = currentTable.getSelectionIndices();
                Arrays.sort(selection);
                try {
                    FileWriter writer = new FileWriter(fileName);
                    for (int i : selection) {
                        TableItem item = currentTable.getItem(i);
                        LogMessage msg = (LogMessage)item.getData();
                        String line = msg.toString();
                        writer.write(line);
                        writer.write('\n');
                    }
                    writer.flush();
                } catch (IOException e) {
                    return false;
                }
            }
        }
        return true;
    }
    public void clear() {
        synchronized (mBuffer) {
            for (int i = 0 ; i < STRING_BUFFER_LENGTH; i++) {
                mBuffer[i] = null;
            }
            mBufferStart = -1;
            mBufferEnd = -1;
            for (LogFilter filter : mFilters) {
                filter.clear();
            }
            if (mDefaultFilter != null) {
                mDefaultFilter.clear();
            }
        }
    }
    public void copy(Clipboard clipboard) {
        Table currentTable = mCurrentFilter.getTable();
        copyTable(clipboard, currentTable);
    }
    public void selectAll() {
        Table currentTable = mCurrentFilter.getTable();
        currentTable.selectAll();
    }
    public void setTableFocusListener(ITableFocusListener listener) {
        mGlobalListener = listener;
        for (LogFilter filter : mFilters) {
            Table table = filter.getTable();
            addTableToFocusListener(table);
        }
        if (mDefaultFilter != null) {
            addTableToFocusListener(mDefaultFilter.getTable());
        }
    }
    private void addTableToFocusListener(final Table table) {
        final IFocusedTableActivator activator = new IFocusedTableActivator() {
            public void copy(Clipboard clipboard) {
                copyTable(clipboard, table);
            }
            public void selectAll() {
                table.selectAll();
            }
        };
        table.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) {
                mGlobalListener.focusGained(activator);
            }
            public void focusLost(FocusEvent e) {
                mGlobalListener.focusLost(activator);
            }
        });
    }
    private static void copyTable(Clipboard clipboard, Table table) {
        int[] selection = table.getSelectionIndices();
        Arrays.sort(selection);
        StringBuilder sb = new StringBuilder();
        for (int i : selection) {
            TableItem item = table.getItem(i);
            LogMessage msg = (LogMessage)item.getData();
            String line = msg.toString();
            sb.append(line);
            sb.append('\n');
        }
        clipboard.setContents(new Object[] {
            sb.toString()
        }, new Transfer[] {
            TextTransfer.getInstance()
        });
    }
    public void setCurrentFilterLogLevel(int i) {
        LogFilter filter = getCurrentFilter();
        filter.setLogLevel(i);
        initFilter(filter);
    }
    private TabItem createTab(LogFilter filter, int index, boolean fillTable) {
        synchronized (mBuffer) {
            TabItem item = null;
            if (index != -1) {
                item = new TabItem(mFolders, SWT.NONE, index);
            } else {
                item = new TabItem(mFolders, SWT.NONE);
            }
            item.setText(filter.getName());
            Composite top = new Composite(mFolders, SWT.NONE);
            item.setControl(top);
            top.setLayout(new FillLayout());
            final Table t = new Table(top, SWT.MULTI | SWT.FULL_SELECTION);
            if (mDisplayFont != null) {
                t.setFont(mDisplayFont);
            }
            filter.setWidgets(item, t);
            t.setHeaderVisible(true);
            t.setLinesVisible(false);
            if (mGlobalListener != null) {
                addTableToFocusListener(t);
            }
            ControlListener listener = null;
            if (mColumnMode == COLUMN_MODE_AUTO) {
                listener = new ControlListener() {
                    public void controlMoved(ControlEvent e) {
                    }
                    public void controlResized(ControlEvent e) {
                        Rectangle r = t.getClientArea();
                        int total = t.getColumn(0).getWidth();
                        total += t.getColumn(1).getWidth();
                        total += t.getColumn(2).getWidth();
                        total += t.getColumn(3).getWidth();
                        if (r.width > total) {
                            t.getColumn(4).setWidth(r.width-total);
                        }
                    }
                };
                t.addControlListener(listener);
            }
            TableColumn col = TableHelper.createTableColumn(t, "Time", SWT.LEFT,
                    "00-00 00:00:00", 
                    PREFS_TIME, mStore);
            if (mColumnMode == COLUMN_MODE_AUTO) {
                col.addControlListener(listener);
            }
            col = TableHelper.createTableColumn(t, "", SWT.CENTER,
                    "D", 
                    PREFS_LEVEL, mStore);
            if (mColumnMode == COLUMN_MODE_AUTO) {
                col.addControlListener(listener);
            }
            col = TableHelper.createTableColumn(t, "pid", SWT.LEFT,
                    "9999", 
                    PREFS_PID, mStore);
            if (mColumnMode == COLUMN_MODE_AUTO) {
                col.addControlListener(listener);
            }
            col = TableHelper.createTableColumn(t, "tag", SWT.LEFT,
                    "abcdefgh",  
                    PREFS_TAG, mStore);
            if (mColumnMode == COLUMN_MODE_AUTO) {
                col.addControlListener(listener);
            }
            col = TableHelper.createTableColumn(t, "Message", SWT.LEFT,
                    "abcdefghijklmnopqrstuvwxyz0123456789",  
                    PREFS_MESSAGE, mStore);
            if (mColumnMode == COLUMN_MODE_AUTO) {
                col.setResizable(false);
            }
            if (fillTable) {
                initFilter(filter);
            }
            return item;
        }
    }
    protected void updateColumns(Table table) {
        if (table != null) {
            int index = 0;
            TableColumn col;
            col = table.getColumn(index++);
            col.setWidth(mStore.getInt(PREFS_TIME));
            col = table.getColumn(index++);
            col.setWidth(mStore.getInt(PREFS_LEVEL));
            col = table.getColumn(index++);
            col.setWidth(mStore.getInt(PREFS_PID));
            col = table.getColumn(index++);
            col.setWidth(mStore.getInt(PREFS_TAG));
            col = table.getColumn(index++);
            col.setWidth(mStore.getInt(PREFS_MESSAGE));
        }
    }
    public void resetUI(boolean inUiThread) {
        if (mFilterMode == FILTER_AUTO_PID || mFilterMode == FILTER_AUTO_TAG) {
            if (inUiThread) {
                mFolders.dispose();
                mParent.pack(true);
                createControl(mParent);
            } else {
                Display d = mFolders.getDisplay();
                d.syncExec(new Runnable() {
                    public void run() {
                        mFolders.dispose();
                        mParent.pack(true);
                        createControl(mParent);
                    }
                });
            }
        } else  {
            if (mFolders.isDisposed() == false) {
                if (inUiThread) {
                    emptyTables();
                } else {
                    Display d = mFolders.getDisplay();
                    d.syncExec(new Runnable() {
                        public void run() {
                            if (mFolders.isDisposed() == false) {
                                emptyTables();
                            }
                        }
                    });
                }
            }
        }
    }
    protected void processLogLines(String[] lines) {
        if (lines.length > STRING_BUFFER_LENGTH) {
            Log.e("LogCat", "Receiving more lines than STRING_BUFFER_LENGTH");
        }
        final ArrayList<LogMessage> newMessages = new ArrayList<LogMessage>();
        synchronized (mBuffer) {
            for (String line : lines) {
                if (line.length() > 0) {
                    Matcher matcher = sLogPattern.matcher(line);
                    if (matcher.matches()) {
                        mLastMessageInfo = new LogMessageInfo();
                        mLastMessageInfo.time = matcher.group(1);
                        mLastMessageInfo.pidString = matcher.group(2);
                        mLastMessageInfo.pid = Integer.valueOf(mLastMessageInfo.pidString);
                        mLastMessageInfo.logLevel = LogLevel.getByLetterString(matcher.group(4));
                        mLastMessageInfo.tag = matcher.group(5).trim();
                    } else {
                        LogMessage mc = new LogMessage();
                        if (mLastMessageInfo == null) {
                            mLastMessageInfo = new LogMessageInfo();
                            mLastMessageInfo.time = "??-?? ??:??:??.???"; 
                            mLastMessageInfo.pidString = "<unknown>"; 
                            mLastMessageInfo.pid = 0;
                            mLastMessageInfo.logLevel = LogLevel.INFO;
                            mLastMessageInfo.tag = "<unknown>"; 
                        }
                        mc.data = mLastMessageInfo;
                        mc.msg = line.replaceAll("\t", "    "); 
                        processNewMessage(mc);
                        newMessages.add(mc);
                    }
                }
            }
            if (mPendingAsyncRefresh == false) {
                mPendingAsyncRefresh = true;
                try {
                    Display display = mFolders.getDisplay();
                    display.asyncExec(new Runnable() {
                        public void run() {
                            asyncRefresh();
                        }
                    });
                } catch (SWTException e) {
                    stopLogCat(false);
                }
            }
        }
    }
    private void asyncRefresh() {
        if (mFolders.isDisposed() == false) {
            synchronized (mBuffer) {
                try {
                    if (mFilters != null) {
                        for (LogFilter f : mFilters) {
                            f.flush();
                        }
                    }
                    if (mDefaultFilter != null) {
                        mDefaultFilter.flush();
                    }
                } finally {
                    mPendingAsyncRefresh = false;
                }
            }
        } else {
            stopLogCat(true);
        }
    }
    private void processNewMessage(LogMessage newMessage) {
        if (mFilterMode == FILTER_AUTO_PID ||
                mFilterMode == FILTER_AUTO_TAG) {
           checkFilter(newMessage.data);
        }
        int messageIndex = -1;
        if (mBufferStart == -1) {
            messageIndex = mBufferStart = 0;
            mBufferEnd = 1;
        } else {
            messageIndex = mBufferEnd;
            if (mBufferEnd == mBufferStart) {
                mBufferStart = (mBufferStart + 1) % STRING_BUFFER_LENGTH;
            }
            mBufferEnd = (mBufferEnd + 1) % STRING_BUFFER_LENGTH;
        }
        LogMessage oldMessage = null;
        if (mBuffer[messageIndex] != null) {
            oldMessage = mBuffer[messageIndex];
        }
        mBuffer[messageIndex] = newMessage;
        boolean filtered = false;
        if (mFilters != null) {
            for (LogFilter f : mFilters) {
                filtered |= f.addMessage(newMessage, oldMessage);
            }
        }
        if (filtered == false && mDefaultFilter != null) {
            mDefaultFilter.addMessage(newMessage, oldMessage);
        }
    }
    private void createFilters() {
        if (mFilterMode == FILTER_DEBUG || mFilterMode == FILTER_MANUAL) {
            mFilters = mFilterStorage.getFilterFromStore();
            if (mFilters != null) {
                for (LogFilter f : mFilters) {
                    f.setColors(mColors);
                }
            }
            if (mFilterStorage.requiresDefaultFilter()) {
                mDefaultFilter = new LogFilter("Log");
                mDefaultFilter.setColors(mColors);
                mDefaultFilter.setSupportsDelete(false);
                mDefaultFilter.setSupportsEdit(false);
            }
        } else if (mFilterMode == FILTER_NONE) {
            mDefaultFilter = new LogFilter("Log");
            mDefaultFilter.setColors(mColors);
            mDefaultFilter.setSupportsDelete(false);
            mDefaultFilter.setSupportsEdit(false);
        }
    }
    private boolean checkFilter(final LogMessageInfo md) {
        if (true)
            return true;
        if (mFilterMode == FILTER_AUTO_PID) {
            for (LogFilter f : mFilters) {
                if (f.getPidFilter() == md.pid) {
                    return true;
                }
            }
        } else if (mFilterMode == FILTER_AUTO_TAG) {
            for (LogFilter f : mFilters) {
                if (f.getTagFilter().equals(md.tag)) {
                    return true;
                }
            }
        }
        final LogFilter newFilter = new LogFilter(md.pidString);
        String name = null;
        if (mFilterMode == FILTER_AUTO_PID) {
            newFilter.setPidMode(md.pid);
            name = mCurrentLoggedDevice.getClientName(md.pid);
        } else {
            newFilter.setTagMode(md.tag);
            name = md.tag;
        }
        addFilterToArray(newFilter);
        final String fname = name;
        final TabItem newTabItem = createTab(newFilter, -1, true);
        if (fname == null) {
            new Thread("remote PS") { 
                @Override
                public void run() {
                    PsOutputReceiver psor = new PsOutputReceiver(md.pid,
                            newFilter, newTabItem);
                    try {
                        mCurrentLoggedDevice.executeShellCommand("ps", psor); 
                    } catch (IOException e) {
                    }
                }
            }.start();
        }
        return false;
    }
    private void addFilterToArray(LogFilter newFilter) {
        newFilter.setColors(mColors);
        if (mFilters != null && mFilters.length > 0) {
            LogFilter[] newFilters = new LogFilter[mFilters.length+1];
            System.arraycopy(mFilters, 0, newFilters, 0, mFilters.length);
            newFilters[mFilters.length] = newFilter;
            mFilters = newFilters;
        } else {
            mFilters = new LogFilter[1];
            mFilters[0] = newFilter;
        }
    }
    private void removeFilterFromArray(LogFilter oldFilter) {
        int index = -1;
        for (int i = 0 ; i < mFilters.length ; i++) {
            if (mFilters[i] == oldFilter) {
                index = i;
                break;
            }
        }
        if (index != -1) {
            LogFilter[] newFilters = new LogFilter[mFilters.length-1];
            System.arraycopy(mFilters, 0, newFilters, 0, index);
            System.arraycopy(mFilters, index + 1, newFilters, index,
                    newFilters.length-index);
            mFilters = newFilters;
        }
    }
    private void initFilter(LogFilter filter) {
        if (filter.uiReady() == false) {
            return;
        }
        if (filter == mDefaultFilter) {
            initDefaultFilter();
            return;
        }
        filter.clear();
        if (mBufferStart != -1) {
            int max = mBufferEnd;
            if (mBufferEnd < mBufferStart) {
                max += STRING_BUFFER_LENGTH;
            }
            for (int i = mBufferStart; i < max; i++) {
                int realItemIndex = i % STRING_BUFFER_LENGTH;
                filter.addMessage(mBuffer[realItemIndex], null );
            }
        }
        filter.flush();
        filter.resetTempFilteringStatus();
    }
    private void initDefaultFilter() {
        mDefaultFilter.clear();
        if (mBufferStart != -1) {
            int max = mBufferEnd;
            if (mBufferEnd < mBufferStart) {
                max += STRING_BUFFER_LENGTH;
            }
            for (int i = mBufferStart; i < max; i++) {
                int realItemIndex = i % STRING_BUFFER_LENGTH;
                LogMessage msg = mBuffer[realItemIndex];
                boolean filtered = false;
                for (LogFilter f : mFilters) {
                    filtered |= f.accept(msg);
                }
                if (filtered == false) {
                    mDefaultFilter.addMessage(msg, null );
                }
            }
        }
        mDefaultFilter.flush();
        mDefaultFilter.resetTempFilteringStatus();
    }
    private void resetFilters() {
        if (mFilterMode == FILTER_AUTO_PID || mFilterMode == FILTER_AUTO_TAG) {
            mFilters = null;
            createFilters();
        }
    }
    private LogFilter getCurrentFilter() {
        int index = mFolders.getSelectionIndex();
        if (index == 0 || mFilters == null) {
            return mDefaultFilter;
        }
        return mFilters[index-1];
    }
    private void emptyTables() {
        for (LogFilter f : mFilters) {
            f.getTable().removeAll();
        }
        if (mDefaultFilter != null) {
            mDefaultFilter.getTable().removeAll();
        }
    }
    protected void updateFilteringWith(String text) {
        synchronized (mBuffer) {
            for (LogFilter f : mFilters) {
                f.resetTempFiltering();
            }
            if (mDefaultFilter != null) {
                mDefaultFilter.resetTempFiltering();
            }
            String[] segments = text.split(" "); 
            ArrayList<String> keywords = new ArrayList<String>(segments.length);
            int tempPid = -1;
            String tempTag = null;
            for (int i = 0 ; i < segments.length; i++) {
                String s = segments[i];
                if (tempPid == -1 && s.startsWith("pid:")) { 
                    String[] seg = s.split(":"); 
                    if (seg.length == 2) {
                        if (seg[1].matches("^[0-9]*$")) { 
                            tempPid = Integer.valueOf(seg[1]);
                        }
                    }
                } else if (tempTag == null && s.startsWith("tag:")) { 
                    String seg[] = segments[i].split(":"); 
                    if (seg.length == 2) {
                        tempTag = seg[1];
                    }
                } else {
                    keywords.add(s);
                }
            }
            if (tempPid != -1 || tempTag != null || keywords.size() > 0) {
                String[] keywordsArray = keywords.toArray(
                        new String[keywords.size()]);
                for (LogFilter f : mFilters) {
                    if (tempPid != -1) {
                        f.setTempPidFiltering(tempPid);
                    }
                    if (tempTag != null) {
                        f.setTempTagFiltering(tempTag);
                    }
                    f.setTempKeywordFiltering(keywordsArray);
                }
                if (mDefaultFilter != null) {
                    if (tempPid != -1) {
                        mDefaultFilter.setTempPidFiltering(tempPid);
                    }
                    if (tempTag != null) {
                        mDefaultFilter.setTempTagFiltering(tempTag);
                    }
                    mDefaultFilter.setTempKeywordFiltering(keywordsArray);
                }
            }
            initFilter(mCurrentFilter);
        }
    }
    private void selectionChanged(LogFilter selectedFilter) {
        if (mLogLevelActions != null) {
            int level = selectedFilter.getLogLevel();
            for (int i = 0 ; i < mLogLevelActions.length; i++) {
                ICommonAction a = mLogLevelActions[i];
                if (i == level - 2) {
                    a.setChecked(true);
                } else {
                    a.setChecked(false);
                }
            }
        }
        if (mDeleteFilterAction != null) {
            mDeleteFilterAction.setEnabled(selectedFilter.supportsDelete());
        }
        if (mEditFilterAction != null) {
            mEditFilterAction.setEnabled(selectedFilter.supportsEdit());
        }
    }
    public String getSelectedErrorLineMessage() {
        Table table = mCurrentFilter.getTable();
        int[] selection = table.getSelectionIndices();
        if (selection.length == 1) {
            TableItem item = table.getItem(selection[0]);
            LogMessage msg = (LogMessage)item.getData();
            if (msg.data.logLevel == LogLevel.ERROR || msg.data.logLevel == LogLevel.WARN)
                return msg.msg;
        }
        return null;
    }
}
