public class DeviceExplorer extends Panel {
    private final static String TRACE_KEY_EXT = ".key"; 
    private final static String TRACE_DATA_EXT = ".data"; 
    private static Pattern mKeyFilePattern = Pattern.compile(
            "(.+)\\" + TRACE_KEY_EXT); 
    private static Pattern mDataFilePattern = Pattern.compile(
            "(.+)\\" + TRACE_DATA_EXT); 
    public static String COLUMN_NAME = "android.explorer.name"; 
    public static String COLUMN_SIZE = "android.explorer.size"; 
    public static String COLUMN_DATE = "android.explorer.data"; 
    public static String COLUMN_TIME = "android.explorer.time"; 
    public static String COLUMN_PERMISSIONS = "android.explorer.permissions"; 
    public static String COLUMN_INFO = "android.explorer.info"; 
    private Composite mParent;
    private TreeViewer mTreeViewer;
    private Tree mTree;
    private DeviceContentProvider mContentProvider;
    private ICommonAction mPushAction;
    private ICommonAction mPullAction;
    private ICommonAction mDeleteAction;
    private Image mFileImage;
    private Image mFolderImage;
    private Image mPackageImage;
    private Image mOtherImage;
    private IDevice mCurrentDevice;
    private String mDefaultSave;
    public DeviceExplorer() {
    }
    public void setImages(Image fileImage, Image folderImage, Image packageImage,
            Image otherImage) {
        mFileImage = fileImage;
        mFolderImage = folderImage;
        mPackageImage = packageImage;
        mOtherImage = otherImage;
    }
    public void setActions(ICommonAction pushAction, ICommonAction pullAction,
            ICommonAction deleteAction) {
        mPushAction = pushAction;
        mPullAction = pullAction;
        mDeleteAction = deleteAction;
    }
    @Override
    protected Control createControl(Composite parent) {
        mParent = parent;
        parent.setLayout(new FillLayout());
        mTree = new Tree(parent, SWT.MULTI | SWT.FULL_SELECTION | SWT.VIRTUAL);
        mTree.setHeaderVisible(true);
        IPreferenceStore store = DdmUiPreferences.getStore();
        TableHelper.createTreeColumn(mTree, "Name", SWT.LEFT,
                "0000drwxrwxrwx", COLUMN_NAME, store); 
        TableHelper.createTreeColumn(mTree, "Size", SWT.RIGHT,
                "000000", COLUMN_SIZE, store); 
        TableHelper.createTreeColumn(mTree, "Date", SWT.LEFT,
                "2007-08-14", COLUMN_DATE, store); 
        TableHelper.createTreeColumn(mTree, "Time", SWT.LEFT,
                "20:54", COLUMN_TIME, store); 
        TableHelper.createTreeColumn(mTree, "Permissions", SWT.LEFT,
                "drwxrwxrwx", COLUMN_PERMISSIONS, store); 
        TableHelper.createTreeColumn(mTree, "Info", SWT.LEFT,
                "drwxrwxrwx", COLUMN_INFO, store); 
        mTreeViewer = new TreeViewer(mTree);
        mContentProvider = new DeviceContentProvider();
        mTreeViewer.setContentProvider(mContentProvider);
        mTreeViewer.setLabelProvider(new FileLabelProvider(mFileImage,
                mFolderImage, mPackageImage, mOtherImage));
        mTreeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
                ISelection sel = event.getSelection();
                if (sel.isEmpty()) {
                    mPullAction.setEnabled(false);
                    mPushAction.setEnabled(false);
                    mDeleteAction.setEnabled(false);
                    return;
                }
                if (sel instanceof IStructuredSelection) {
                    IStructuredSelection selection = (IStructuredSelection) sel;
                    Object element = selection.getFirstElement();
                    if (element == null)
                        return;
                    if (element instanceof FileEntry) {
                        mPullAction.setEnabled(true);
                        mPushAction.setEnabled(selection.size() == 1);
                        if (selection.size() == 1) {
                            setDeleteEnabledState((FileEntry)element);
                        } else {
                            mDeleteAction.setEnabled(false);
                        }
                    }
                }
            }
        });
        mTreeViewer.addDoubleClickListener(new IDoubleClickListener() {
            public void doubleClick(DoubleClickEvent event) {
                ISelection sel = event.getSelection();
                if (sel instanceof IStructuredSelection) {
                    IStructuredSelection selection = (IStructuredSelection) sel;
                    if (selection.size() == 1) {
                        FileEntry entry = (FileEntry)selection.getFirstElement();
                        String name = entry.getName();
                        FileEntry parentEntry = entry.getParent();
                        if (parentEntry == null) {
                            return;
                        }
                        Matcher m = mKeyFilePattern.matcher(name);
                        if (m.matches()) {
                            String baseName = m.group(1);
                            String dataName = baseName + TRACE_DATA_EXT;
                            FileEntry dataEntry = parentEntry.findChild(dataName);
                            handleTraceDoubleClick(baseName, entry, dataEntry);
                        } else {
                            m = mDataFilePattern.matcher(name);
                            if (m.matches()) {
                                String baseName = m.group(1);
                                String keyName = baseName + TRACE_KEY_EXT;
                                FileEntry keyEntry = parentEntry.findChild(keyName);
                                handleTraceDoubleClick(baseName, keyEntry, entry);
                            }
                        }
                    }
                }
            }
        });
        mTreeViewer.addDropSupport(DND.DROP_COPY | DND.DROP_MOVE,
                new Transfer[] { FileTransfer.getInstance() },
                new ViewerDropAdapter(mTreeViewer) {
            @Override
            public boolean performDrop(Object data) {
                FileEntry target = (FileEntry)getCurrentTarget();
                if (target == null) {
                    return false;
                }
                if (target.isDirectory() == false) {
                    target = target.getParent();
                }
                if (target == null) {
                    return false;
                }
                String[] files = (String[])data;
                pushFiles(files, target);
                refresh(target);
                return true;
            }
            @Override
            public boolean validateDrop(Object target, int operation, TransferData transferType) {
                if (target == null) {
                    return false;
                }
                FileEntry targetEntry = (FileEntry)target;
                if (targetEntry.isDirectory() == false) {
                    target = targetEntry.getParent();
                }
                if (target == null) {
                    return false;
                }
                return true;
            }
        });
        new Thread("Device Ls refresher") {
            @Override
            public void run() {
                while (true) {
                    try {
                        sleep(FileListingService.REFRESH_RATE);
                    } catch (InterruptedException e) {
                        return;
                    }
                    if (mTree != null && mTree.isDisposed() == false) {
                        Display display = mTree.getDisplay();
                        if (display.isDisposed() == false) {
                            display.asyncExec(new Runnable() {
                                public void run() {
                                    if (mTree.isDisposed() == false) {
                                        mTreeViewer.refresh(true);
                                    }
                                }
                            });
                        } else {
                            return;
                        }
                    } else {
                        return;
                    }
                }
            }
        }.start();
        return mTree;
    }
    @Override
    protected void postCreation() {
    }
    @Override
    public void setFocus() {
        mTree.setFocus();
    }
    private void handleTraceDoubleClick(String baseName, FileEntry keyEntry,
            FileEntry dataEntry) {
        File keyFile;
        File dataFile;
        String path;
        try {
            File f = File.createTempFile(baseName, ".trace");
            f.delete();
            f.mkdir();
            path = f.getAbsolutePath();
            keyFile = new File(path + File.separator + keyEntry.getName());
            dataFile = new File(path + File.separator + dataEntry.getName());
        } catch (IOException e) {
            return;
        }
        try {
            SyncService sync = mCurrentDevice.getSyncService();
            if (sync != null) {
                ISyncProgressMonitor monitor = SyncService.getNullProgressMonitor();
                SyncResult result = sync.pullFile(keyEntry, keyFile.getAbsolutePath(), monitor);
                if (result.getCode() != SyncService.RESULT_OK) {
                    DdmConsole.printErrorToConsole(String.format(
                            "Failed to pull %1$s: %2$s", keyEntry.getName(), result.getMessage()));
                    return;
                }
                result = sync.pullFile(dataEntry, dataFile.getAbsolutePath(), monitor);
                if (result.getCode() != SyncService.RESULT_OK) {
                    DdmConsole.printErrorToConsole(String.format(
                            "Failed to pull %1$s: %2$s", dataEntry.getName(), result.getMessage()));
                    return;
                }
                String[] command = new String[2];
                command[0] = DdmUiPreferences.getTraceview();
                command[1] = path + File.separator + baseName;
                try {
                    final Process p = Runtime.getRuntime().exec(command);
                    new Thread("Traceview output") {
                        @Override
                        public void run() {
                            InputStreamReader is = new InputStreamReader(p.getErrorStream());
                            BufferedReader resultReader = new BufferedReader(is);
                            try {
                                while (true) {
                                    String line = resultReader.readLine();
                                    if (line != null) {
                                        DdmConsole.printErrorToConsole("Traceview: " + line);
                                    } else {
                                        break;
                                    }
                                }
                                p.waitFor();
                            } catch (IOException e) {
                            } catch (InterruptedException e) {
                            }
                        }
                    }.start();
                } catch (IOException e) {
                }
            }
        } catch (IOException e) {
            DdmConsole.printErrorToConsole(String.format(
                    "Failed to pull %1$s: %2$s", keyEntry.getName(), e.getMessage()));
            return;
        }
    }
    public void pullSelection() {
        TreeItem[] items = mTree.getSelection();
        String filePullName = null;
        FileEntry singleEntry = null;
        if (items.length == 1) {
            singleEntry = (FileEntry)items[0].getData();
            if (singleEntry.getType() == FileListingService.TYPE_FILE) {
                filePullName = singleEntry.getName();
            }
        }
        String defaultPath = mDefaultSave;
        if (defaultPath == null) {
            defaultPath = System.getProperty("user.home"); 
        }
        if (filePullName != null) {
            FileDialog fileDialog = new FileDialog(mParent.getShell(), SWT.SAVE);
            fileDialog.setText("Get Device File");
            fileDialog.setFileName(filePullName);
            fileDialog.setFilterPath(defaultPath);
            String fileName = fileDialog.open();
            if (fileName != null) {
                mDefaultSave = fileDialog.getFilterPath();
                pullFile(singleEntry, fileName);
            }
        } else {
            DirectoryDialog directoryDialog = new DirectoryDialog(mParent.getShell(), SWT.SAVE);
            directoryDialog.setText("Get Device Files/Folders");
            directoryDialog.setFilterPath(defaultPath);
            String directoryName = directoryDialog.open();
            if (directoryName != null) {
                pullSelection(items, directoryName);
            }
        }
    }
    public void pushIntoSelection() {
        TreeItem[] items = mTree.getSelection();
        if (items.length == 0) {
            return;
        }
        FileDialog dlg = new FileDialog(mParent.getShell(), SWT.OPEN);
        String fileName;
        dlg.setText("Put File on Device");
        FileEntry entry = (FileEntry)items[0].getData();
        dlg.setFileName(entry.getName());
        String defaultPath = mDefaultSave;
        if (defaultPath == null) {
            defaultPath = System.getProperty("user.home"); 
        }
        dlg.setFilterPath(defaultPath);
        fileName = dlg.open();
        if (fileName != null) {
            mDefaultSave = dlg.getFilterPath();
            String remotePath;
            FileEntry toRefresh = entry;
            if (entry.isDirectory()) {
                remotePath = entry.getFullPath();
            } else {
                toRefresh = entry.getParent();
                remotePath = toRefresh.getFullPath();
            }
            pushFile(fileName, remotePath);
            mTreeViewer.refresh(toRefresh);
        }
    }
    public void deleteSelection() {
        TreeItem[] items = mTree.getSelection();
        if (items.length != 1) {
            return;
        }
        FileEntry entry = (FileEntry)items[0].getData();
        final FileEntry parentEntry = entry.getParent();
        String command = "rm " + entry.getFullEscapedPath(); 
        try {
            mCurrentDevice.executeShellCommand(command, new IShellOutputReceiver() {
                public void addOutput(byte[] data, int offset, int length) {
                }
                public void flush() {
                    mTreeViewer.refresh(parentEntry);
                }
                public boolean isCancelled() {
                    return false;
                }
            });
        } catch (IOException e) {
        }
    }
    public void refresh() {
        mTreeViewer.refresh(true);
    }
    public void switchDevice(final IDevice device) {
        if (device != mCurrentDevice) {
            mCurrentDevice = device;
            if (mTree.isDisposed() == false) {
                Display d = mTree.getDisplay();
                d.asyncExec(new Runnable() {
                    public void run() {
                        if (mTree.isDisposed() == false) {
                            if (mCurrentDevice != null) {
                                FileListingService fls = mCurrentDevice.getFileListingService();
                                mContentProvider.setListingService(fls);
                                mTreeViewer.setInput(fls.getRoot());
                            }
                        }
                    }
                });
            }
        }
    }
    private void refresh(final FileEntry entry) {
        Display d = mTreeViewer.getTree().getDisplay();
        d.asyncExec(new Runnable() {
            public void run() {
                mTreeViewer.refresh(entry);
            }
        });
    }
    private void pullSelection(TreeItem[] items, final String localDirectory) {
        try {
            final SyncService sync = mCurrentDevice.getSyncService();
            if (sync != null) {
                ArrayList<FileEntry> entries = new ArrayList<FileEntry>();
                for (TreeItem item : items) {
                    Object data = item.getData();
                    if (data instanceof FileEntry) {
                        entries.add((FileEntry)data);
                    }
                }
                final FileEntry[] entryArray = entries.toArray(
                        new FileEntry[entries.size()]);
                new ProgressMonitorDialog(mParent.getShell()).run(true, true,
                        new IRunnableWithProgress() {
                    public void run(IProgressMonitor monitor)
                            throws InvocationTargetException,
                            InterruptedException {
                        SyncResult result = sync.pull(entryArray, localDirectory,
                                new SyncProgressMonitor(monitor,
                                        "Pulling file(s) from the device"));
                        if (result.getCode() != SyncService.RESULT_OK) {
                            DdmConsole.printErrorToConsole(String.format(
                                    "Failed to pull selection: %1$s", result.getMessage()));
                        }
                        sync.close();
                    }
                });
            }
        } catch (Exception e) {
            DdmConsole.printErrorToConsole( "Failed to pull selection");
            DdmConsole.printErrorToConsole(e.getMessage());
        }
    }
    private void pullFile(final FileEntry remote, final String local) {
        try {
            final SyncService sync = mCurrentDevice.getSyncService();
            if (sync != null) {
                new ProgressMonitorDialog(mParent.getShell()).run(true, true,
                        new IRunnableWithProgress() {
                    public void run(IProgressMonitor monitor)
                            throws InvocationTargetException,
                            InterruptedException {
                        SyncResult result = sync.pullFile(remote, local, new SyncProgressMonitor(
                                monitor, String.format("Pulling %1$s from the device",
                                        remote.getName())));
                        if (result.getCode() != SyncService.RESULT_OK) {
                            DdmConsole.printErrorToConsole(String.format(
                                    "Failed to pull %1$s: %2$s", remote, result.getMessage()));
                        }
                        sync.close();
                    }
                });
            }
        } catch (Exception e) {
            DdmConsole.printErrorToConsole( "Failed to pull selection");
            DdmConsole.printErrorToConsole(e.getMessage());
        }
    }
    private void pushFiles(final String[] localFiles, final FileEntry remoteDirectory) {
        try {
            final SyncService sync = mCurrentDevice.getSyncService();
            if (sync != null) {
                new ProgressMonitorDialog(mParent.getShell()).run(true, true,
                        new IRunnableWithProgress() {
                    public void run(IProgressMonitor monitor)
                            throws InvocationTargetException,
                            InterruptedException {
                        SyncResult result = sync.push(localFiles, remoteDirectory,
                                    new SyncProgressMonitor(monitor,
                                            "Pushing file(s) to the device"));
                        if (result.getCode() != SyncService.RESULT_OK) {
                            DdmConsole.printErrorToConsole(String.format(
                                    "Failed to push the items: %1$s", result.getMessage()));
                        }
                        sync.close();
                    }
                });
            }
        } catch (Exception e) {
            DdmConsole.printErrorToConsole("Failed to push the items");
            DdmConsole.printErrorToConsole(e.getMessage());
        }
    }
    private void pushFile(final String local, final String remoteDirectory) {
        try {
            final SyncService sync = mCurrentDevice.getSyncService();
            if (sync != null) {
                new ProgressMonitorDialog(mParent.getShell()).run(true, true,
                        new IRunnableWithProgress() {
                    public void run(IProgressMonitor monitor)
                            throws InvocationTargetException,
                            InterruptedException {
                        String[] segs = local.split(Pattern.quote(File.separator));
                        String name = segs[segs.length-1];
                        String remoteFile = remoteDirectory + FileListingService.FILE_SEPARATOR
                                + name;
                        SyncResult result = sync.pushFile(local, remoteFile,
                                    new SyncProgressMonitor(monitor,
                                            String.format("Pushing %1$s to the device.", name)));
                        if (result.getCode() != SyncService.RESULT_OK) {
                            DdmConsole.printErrorToConsole(String.format(
                                    "Failed to push %1$s on %2$s: %3$s",
                                    name, mCurrentDevice.getSerialNumber(), result.getMessage()));
                        }
                        sync.close();
                    }
                });
            }
        } catch (Exception e) {
            DdmConsole.printErrorToConsole("Failed to push the item(s).");
            DdmConsole.printErrorToConsole(e.getMessage());
        }
    }
    protected void setDeleteEnabledState(FileEntry element) {
        mDeleteAction.setEnabled(element.getType() == FileListingService.TYPE_FILE);
    }
}
