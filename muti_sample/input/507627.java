public final class AvdSelector {
    private static int NUM_COL = 2;
    private final DisplayMode mDisplayMode;
    private AvdManager mAvdManager;
    private final String mOsSdkPath;
    private Table mTable;
    private Button mDeleteButton;
    private Button mDetailsButton;
    private Button mNewButton;
    private Button mRefreshButton;
    private Button mManagerButton;
    private Button mRepairButton;
    private Button mStartButton;
    private SelectionListener mSelectionListener;
    private IAvdFilter mTargetFilter;
    private boolean mIsEnabled = true;
    private ImageFactory mImageFactory;
    private Image mOkImage;
    private Image mBrokenImage;
    private Image mInvalidImage;
    private SettingsController mController;
    private final ISdkLog mSdkLog;
    public static enum DisplayMode {
        MANAGER,
        SIMPLE_CHECK,
        SIMPLE_SELECTION,
    }
    public interface IAvdFilter {
        void prepare();
        boolean accept(AvdInfo avd);
        void cleanup();
    }
    private final static class TargetBasedFilter implements IAvdFilter {
        private final IAndroidTarget mTarget;
        TargetBasedFilter(IAndroidTarget target) {
            mTarget = target;
        }
        public void prepare() {
        }
        public boolean accept(AvdInfo avd) {
            if (avd != null) {
                return mTarget.canRunOn(avd.getTarget());
            }
            return false;
        }
        public void cleanup() {
        }
    }
    public AvdSelector(Composite parent,
            String osSdkPath,
            AvdManager manager,
            IAvdFilter filter,
            DisplayMode displayMode,
            ISdkLog sdkLog) {
        mOsSdkPath = osSdkPath;
        mAvdManager = manager;
        mTargetFilter = filter;
        mDisplayMode = displayMode;
        mSdkLog = sdkLog;
        mImageFactory = new ImageFactory(parent.getDisplay());
        mOkImage = mImageFactory.getImageByName("accept_icon16.png");
        mBrokenImage = mImageFactory.getImageByName("broken_16.png");
        mInvalidImage = mImageFactory.getImageByName("reject_icon16.png");
        Composite group = new Composite(parent, SWT.NONE);
        GridLayout gl;
        group.setLayout(gl = new GridLayout(NUM_COL, false ));
        gl.marginHeight = gl.marginWidth = 0;
        group.setLayoutData(new GridData(GridData.FILL_BOTH));
        group.setFont(parent.getFont());
        group.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent arg0) {
                mImageFactory.dispose();
            }
        });
        int style = SWT.FULL_SELECTION | SWT.SINGLE | SWT.BORDER;
        if (displayMode == DisplayMode.SIMPLE_CHECK) {
            style |= SWT.CHECK;
        }
        mTable = new Table(group, style);
        mTable.setHeaderVisible(true);
        mTable.setLinesVisible(false);
        setTableHeightHint(0);
        Composite buttons = new Composite(group, SWT.NONE);
        buttons.setLayout(gl = new GridLayout(1, false ));
        gl.marginHeight = gl.marginWidth = 0;
        buttons.setLayoutData(new GridData(GridData.FILL_VERTICAL));
        buttons.setFont(group.getFont());
        if (displayMode == DisplayMode.MANAGER) {
            mNewButton = new Button(buttons, SWT.PUSH | SWT.FLAT);
            mNewButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            mNewButton.setText("New...");
            mNewButton.setToolTipText("Creates a new AVD.");
            mNewButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent arg0) {
                    onNew();
                }
            });
            mDeleteButton = new Button(buttons, SWT.PUSH | SWT.FLAT);
            mDeleteButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            mDeleteButton.setText("Delete...");
            mDeleteButton.setToolTipText("Deletes the selected AVD.");
            mDeleteButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent arg0) {
                    onDelete();
                }
            });
            mRepairButton = new Button(buttons, SWT.PUSH | SWT.FLAT);
            mRepairButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            mRepairButton.setText("Repair...");
            mRepairButton.setToolTipText("Repairs the selected AVD.");
            mRepairButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent arg0) {
                    onRepair();
                }
            });
            Label l = new Label(buttons, SWT.SEPARATOR | SWT.HORIZONTAL);
            l.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        }
        mDetailsButton = new Button(buttons, SWT.PUSH | SWT.FLAT);
        mDetailsButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        mDetailsButton.setText("Details...");
        mDetailsButton.setToolTipText("Diplays details of the selected AVD.");
        mDetailsButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                onDetails();
            }
        });
        mStartButton = new Button(buttons, SWT.PUSH | SWT.FLAT);
        mStartButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        mStartButton.setText("Start...");
        mStartButton.setToolTipText("Starts the selected AVD.");
        mStartButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                onStart();
            }
        });
        Composite padding = new Composite(buttons, SWT.NONE);
        padding.setLayoutData(new GridData(GridData.FILL_VERTICAL));
        mRefreshButton = new Button(buttons, SWT.PUSH | SWT.FLAT);
        mRefreshButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        mRefreshButton.setText("Refresh");
        mRefreshButton.setToolTipText("Reloads the list of AVD.\nUse this if you create AVDs from the command line.");
        mRefreshButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                refresh(true);
            }
        });
        if (displayMode != DisplayMode.MANAGER) {
            mManagerButton = new Button(buttons, SWT.PUSH | SWT.FLAT);
            mManagerButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            mManagerButton.setText("Manager...");
            mManagerButton.setToolTipText("Launches the AVD manager.");
            mManagerButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    onManager();
                }
            });
        } else {
            Composite legend = new Composite(group, SWT.NONE);
            legend.setLayout(gl = new GridLayout(4, false ));
            gl.marginHeight = gl.marginWidth = 0;
            legend.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false,
                    NUM_COL, 1));
            legend.setFont(group.getFont());
            new Label(legend, SWT.NONE).setImage(mOkImage);
            new Label(legend, SWT.NONE).setText("A valid Android Virtual Device.");
            new Label(legend, SWT.NONE).setImage(mBrokenImage);
            new Label(legend, SWT.NONE).setText(
                    "A repairable Android Virtual Device.");
            new Label(legend, SWT.NONE).setImage(mInvalidImage);
            Label l = new Label(legend, SWT.NONE);
            l.setText("An Android Virtual Device that failed to load. Click 'Details' to see the error.");
            GridData gd;
            l.setLayoutData(gd = new GridData(GridData.FILL_HORIZONTAL));
            gd.horizontalSpan = 3;
        }
        final TableColumn column0 = new TableColumn(mTable, SWT.NONE);
        column0.setText("AVD Name");
        final TableColumn column1 = new TableColumn(mTable, SWT.NONE);
        column1.setText("Target Name");
        final TableColumn column2 = new TableColumn(mTable, SWT.NONE);
        column2.setText("Platform");
        final TableColumn column3 = new TableColumn(mTable, SWT.NONE);
        column3.setText("API Level");
        adjustColumnsWidth(mTable, column0, column1, column2, column3);
        setupSelectionListener(mTable);
        fillTable(mTable);
        setEnabled(true);
    }
    public AvdSelector(Composite parent,
            String osSdkPath,
            AvdManager manager,
            DisplayMode displayMode,
            ISdkLog sdkLog) {
        this(parent, osSdkPath, manager, (IAvdFilter)null , displayMode, sdkLog);
    }
    public AvdSelector(Composite parent,
            String osSdkPath,
            AvdManager manager,
            IAndroidTarget filter,
            DisplayMode displayMode,
            ISdkLog sdkLog) {
        this(parent, osSdkPath, manager, new TargetBasedFilter(filter), displayMode, sdkLog);
    }
    public void setSettingsController(SettingsController controller) {
        mController = controller;
    }
    public void setTableHeightHint(int heightHint) {
        GridData data = new GridData();
        if (heightHint > 0) {
            data.heightHint = heightHint;
        }
        data.grabExcessVerticalSpace = true;
        data.grabExcessHorizontalSpace = true;
        data.horizontalAlignment = GridData.FILL;
        data.verticalAlignment = GridData.FILL;
        mTable.setLayoutData(data);
    }
    public boolean refresh(boolean reload) {
        if (reload) {
            try {
                mAvdManager.reloadAvds(NullSdkLog.getLogger());
            } catch (AndroidLocationException e) {
                return false;
            }
        }
        AvdInfo selected = getSelected();
        fillTable(mTable);
        setSelection(selected);
        return true;
    }
    public void setManager(AvdManager manager) {
        mAvdManager = manager;
    }
    public void setFilter(IAvdFilter filter) {
        mTargetFilter = filter;
    }
    public void setFilter(IAndroidTarget target) {
        if (target != null) {
            mTargetFilter = new TargetBasedFilter(target);
        } else {
            mTargetFilter = null;
        }
    }
    public void setSelectionListener(SelectionListener selectionListener) {
        mSelectionListener = selectionListener;
    }
    public boolean setSelection(AvdInfo target) {
        boolean found = false;
        boolean modified = false;
        int selIndex = mTable.getSelectionIndex();
        int index = 0;
        for (TableItem i : mTable.getItems()) {
            if (mDisplayMode == DisplayMode.SIMPLE_CHECK) {
                if ((AvdInfo) i.getData() == target) {
                    found = true;
                    if (!i.getChecked()) {
                        modified = true;
                        i.setChecked(true);
                    }
                } else if (i.getChecked()) {
                    modified = true;
                    i.setChecked(false);
                }
            } else {
                if ((AvdInfo) i.getData() == target) {
                    found = true;
                    if (index != selIndex) {
                        mTable.setSelection(index);
                        modified = true;
                    }
                    break;
                }
                index++;
            }
        }
        if (modified && mSelectionListener != null) {
            mSelectionListener.widgetSelected(null);
        }
        enableActionButtons();
        return found;
    }
    public AvdInfo getSelected() {
        if (mDisplayMode == DisplayMode.SIMPLE_CHECK) {
            for (TableItem i : mTable.getItems()) {
                if (i.getChecked()) {
                    return (AvdInfo) i.getData();
                }
            }
        } else {
            int selIndex = mTable.getSelectionIndex();
            if (selIndex >= 0) {
                return (AvdInfo) mTable.getItem(selIndex).getData();
            }
        }
        return null;
    }
    public void setEnabled(boolean enabled) {
        mIsEnabled = enabled && mAvdManager != null;
        mTable.setEnabled(mIsEnabled);
        mRefreshButton.setEnabled(mIsEnabled);
        if (mNewButton != null) {
            mNewButton.setEnabled(mIsEnabled);
        }
        if (mManagerButton != null) {
            mManagerButton.setEnabled(mIsEnabled);
        }
        enableActionButtons();
    }
    public boolean isEnabled() {
        return mIsEnabled;
    }
    private void adjustColumnsWidth(final Table table,
            final TableColumn column0,
            final TableColumn column1,
            final TableColumn column2,
            final TableColumn column3) {
        table.addControlListener(new ControlAdapter() {
            @Override
            public void controlResized(ControlEvent e) {
                Rectangle r = table.getClientArea();
                column0.setWidth(r.width * 25 / 100); 
                column1.setWidth(r.width * 45 / 100); 
                column2.setWidth(r.width * 15 / 100); 
                column3.setWidth(r.width * 15 / 100); 
            }
        });
    }
    private void setupSelectionListener(final Table table) {
        table.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                if (e.item instanceof TableItem) {
                    TableItem i = (TableItem) e.item;
                    enforceSingleSelection(i);
                }
                if (mSelectionListener != null) {
                    mSelectionListener.widgetSelected(e);
                }
                enableActionButtons();
            }
            public void widgetDefaultSelected(SelectionEvent e) {
                if (e.item instanceof TableItem) {
                    TableItem i = (TableItem) e.item;
                    if (mDisplayMode == DisplayMode.SIMPLE_CHECK) {
                        i.setChecked(true);
                    }
                    enforceSingleSelection(i);
                }
                boolean showDetails = mDisplayMode != DisplayMode.SIMPLE_CHECK;
                if (mSelectionListener != null) {
                    mSelectionListener.widgetDefaultSelected(e);
                    showDetails &= e.doit; 
                }
                if (showDetails) {
                    onDetails();
                }
                enableActionButtons();
            }
            private void enforceSingleSelection(TableItem item) {
                if (mDisplayMode == DisplayMode.SIMPLE_CHECK) {
                    if (item.getChecked()) {
                        Table parentTable = item.getParent();
                        for (TableItem i2 : parentTable.getItems()) {
                            if (i2 != item && i2.getChecked()) {
                                i2.setChecked(false);
                            }
                        }
                    }
                } else {
                }
            }
        });
    }
    private void fillTable(final Table table) {
        table.removeAll();
        AvdInfo avds[] = null;
        if (mAvdManager != null) {
            if (mDisplayMode == DisplayMode.MANAGER) {
                avds = mAvdManager.getAllAvds();
            } else {
                avds = mAvdManager.getValidAvds();
            }
        }
        if (avds != null && avds.length > 0) {
            Arrays.sort(avds, new Comparator<AvdInfo>() {
                public int compare(AvdInfo o1, AvdInfo o2) {
                    return o1.compareTo(o2);
                }
            });
            table.setEnabled(true);
            if (mTargetFilter != null) {
                mTargetFilter.prepare();
            }
            for (AvdInfo avd : avds) {
                if (mTargetFilter == null || mTargetFilter.accept(avd)) {
                    TableItem item = new TableItem(table, SWT.NONE);
                    item.setData(avd);
                    item.setText(0, avd.getName());
                    if (mDisplayMode == DisplayMode.MANAGER) {
                        AvdStatus status = avd.getStatus();
                        item.setImage(0, status == AvdStatus.OK ? mOkImage :
                            isAvdRepairable(status) ? mBrokenImage : mInvalidImage);
                    }
                    IAndroidTarget target = avd.getTarget();
                    if (target != null) {
                        item.setText(1, target.getFullName());
                        item.setText(2, target.getVersionName());
                        item.setText(3, target.getVersion().getApiString());
                    } else {
                        item.setText(1, "?");
                        item.setText(2, "?");
                        item.setText(3, "?");
                    }
                }
            }
            if (mTargetFilter != null) {
                mTargetFilter.cleanup();
            }
        }
        if (table.getItemCount() == 0) {
            table.setEnabled(false);
            TableItem item = new TableItem(table, SWT.NONE);
            item.setData(null);
            item.setText(0, "--");
            item.setText(1, "No AVD available");
            item.setText(2, "--");
            item.setText(3, "--");
        }
    }
    private AvdInfo getTableSelection() {
        int selIndex = mTable.getSelectionIndex();
        if (selIndex >= 0) {
            return (AvdInfo) mTable.getItem(selIndex).getData();
        }
        return null;
    }
    private void enableActionButtons() {
        if (mIsEnabled == false) {
            mDetailsButton.setEnabled(false);
            mStartButton.setEnabled(false);
            if (mDeleteButton != null) {
                mDeleteButton.setEnabled(false);
            }
            if (mRepairButton != null) {
                mRepairButton.setEnabled(false);
            }
        } else {
            AvdInfo selection = getTableSelection();
            boolean hasSelection = selection != null;
            mDetailsButton.setEnabled(hasSelection);
            mStartButton.setEnabled(mOsSdkPath != null &&
                    hasSelection &&
                    selection.getStatus() == AvdStatus.OK);
            if (mDeleteButton != null) {
                mDeleteButton.setEnabled(hasSelection);
            }
            if (mRepairButton != null) {
                mRepairButton.setEnabled(hasSelection && isAvdRepairable(selection.getStatus()));
            }
        }
    }
    private void onNew() {
        AvdCreationDialog dlg = new AvdCreationDialog(mTable.getShell(),
                mAvdManager,
                mImageFactory,
                mSdkLog);
        if (dlg.open() == Window.OK) {
            refresh(false );
        }
    }
    private void onDetails() {
        final AvdInfo avdInfo = getTableSelection();
        AvdDetailsDialog dlg = new AvdDetailsDialog(mTable.getShell(), avdInfo);
        dlg.open();
    }
    private void onDelete() {
        final AvdInfo avdInfo = getTableSelection();
        final Display display = mTable.getDisplay();
        if (avdInfo.isRunning()) {
            display.asyncExec(new Runnable() {
                public void run() {
                    Shell shell = display.getActiveShell();
                    MessageDialog.openError(shell,
                            "Delete Android Virtual Device",
                            String.format(
                                    "The Android Virtual Device '%1$s' is currently running in an emulator and cannot be deleted.",
                                    avdInfo.getName()));
                }
            });
            return;
        }
        final boolean[] result = new boolean[1];
        display.syncExec(new Runnable() {
            public void run() {
                Shell shell = display.getActiveShell();
                result[0] = MessageDialog.openQuestion(shell,
                        "Delete Android Virtual Device",
                        String.format(
                                "Please confirm that you want to delete the Android Virtual Device named '%s'. This operation cannot be reverted.",
                                avdInfo.getName()));
            }
        });
        if (result[0] == false) {
            return;
        }
        ISdkLog log = mSdkLog;
        if (log == null || log instanceof MessageBoxLog) {
            log = new MessageBoxLog(
                String.format("Result of deleting AVD '%s':", avdInfo.getName()),
                display,
                false );
        }
        boolean success = mAvdManager.deleteAvd(avdInfo, log);
        if (log instanceof MessageBoxLog) {
            ((MessageBoxLog) log).displayResult(success);
        }
        if (success) {
            refresh(false );
        }
    }
    private void onRepair() {
        final AvdInfo avdInfo = getTableSelection();
        final Display display = mTable.getDisplay();
        ISdkLog log = mSdkLog;
        if (log == null || log instanceof MessageBoxLog) {
            log = new MessageBoxLog(
                String.format("Result of updating AVD '%s':", avdInfo.getName()),
                display,
                false );
        }
        try {
            mAvdManager.updateAvd(avdInfo, log);
            if (log instanceof MessageBoxLog) {
                ((MessageBoxLog) log).displayResult(true );
            }
            refresh(false );
        } catch (IOException e) {
            log.error(e, null);
            if (log instanceof MessageBoxLog) {
                ((MessageBoxLog) log).displayResult(false );
            }
        }
    }
    private void onManager() {
        Display display = mTable.getDisplay();
        ISdkLog log = mSdkLog;
        if (log == null || log instanceof MessageBoxLog) {
            log = new MessageBoxLog("Result of SDK Manager", display, true );
        }
        UpdaterWindow window = new UpdaterWindow(
                mTable.getShell(),
                log,
                mAvdManager.getSdkManager().getLocation(),
                false );
        window.open();
        refresh(true ); 
        if (log instanceof MessageBoxLog) {
            ((MessageBoxLog) log).displayResult(true);
        }
    }
    private void onStart() {
        AvdInfo avdInfo = getTableSelection();
        if (avdInfo == null || mOsSdkPath == null) {
            return;
        }
        AvdStartDialog dialog = new AvdStartDialog(mTable.getShell(), avdInfo, mOsSdkPath,
                mController);
        if (dialog.open() == Window.OK) {
            String path = mOsSdkPath +
                File.separator +
                SdkConstants.OS_SDK_TOOLS_FOLDER +
                SdkConstants.FN_EMULATOR;
            final String avdName = avdInfo.getName();
            ArrayList<String> list = new ArrayList<String>();
            list.add(path);
            list.add("-avd");                             
            list.add(avdName);
            if (dialog.getWipeData()) {
                list.add("-wipe-data");                   
            }
            float scale = dialog.getScale();
            if (scale != 0.f) {
                scale = Math.round(scale * 100);
                scale /=  100.f;
                list.add("-scale");                       
                list.add(String.format("%.2f", scale));   
            }
            final String[] command = list.toArray(new String[list.size()]);
            new ProgressTask(mTable.getShell(),
                    "Starting Android Emulator",
                    new ITask() {
                        public void run(ITaskMonitor monitor) {
                            try {
                                monitor.setDescription("Starting emulator for AVD '%1$s'",
                                        avdName);
                                int n = 10;
                                monitor.setProgressMax(n);
                                Process process = Runtime.getRuntime().exec(command);
                                grabEmulatorOutput(process, monitor);
                                for (int i = 0; i < n; i++) {
                                    try {
                                        Thread.sleep(100);
                                        monitor.incProgress(1);
                                    } catch (InterruptedException e) {
                                    }
                                }
                            } catch (IOException e) {
                                monitor.setResult("Failed to start emulator: %1$s",
                                        e.getMessage());
                            }
                        }
            });
        }
    }
    private void grabEmulatorOutput(final Process process, final ITaskMonitor monitor) {
        new Thread("emu-stderr") { 
            @Override
            public void run() {
                InputStreamReader is = new InputStreamReader(process.getErrorStream());
                BufferedReader errReader = new BufferedReader(is);
                try {
                    while (true) {
                        String line = errReader.readLine();
                        if (line != null) {
                            monitor.setResult("%1$s", line);    
                        } else {
                            break;
                        }
                    }
                } catch (IOException e) {
                }
            }
        }.start();
        new Thread("emu-stdout") { 
            @Override
            public void run() {
                InputStreamReader is = new InputStreamReader(process.getInputStream());
                BufferedReader outReader = new BufferedReader(is);
                try {
                    while (true) {
                        String line = outReader.readLine();
                        if (line != null) {
                            monitor.setResult("%1$s", line);    
                        } else {
                            break;
                        }
                    }
                } catch (IOException e) {
                }
            }
        }.start();
    }
    private boolean isAvdRepairable(AvdStatus avdStatus) {
        return avdStatus == AvdStatus.ERROR_IMAGE_DIR;
    }
}
