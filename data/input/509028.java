public final class NativeHeapPanel extends BaseHeapPanel {
    private static final int NUM_PALETTE_ENTRIES = HeapSegmentElement.KIND_NATIVE+2 +1;
    private static final String[] mMapLegend = new String[NUM_PALETTE_ENTRIES];
    private static final PaletteData mMapPalette = createPalette();
    private static final int ALLOC_DISPLAY_ALL = 0;
    private static final int ALLOC_DISPLAY_PRE_ZYGOTE = 1;
    private static final int ALLOC_DISPLAY_POST_ZYGOTE = 2;
    private Display mDisplay;
    private Composite mBase;
    private Label mUpdateStatus;
    private Combo mAllocDisplayCombo;
    private Button mFullUpdateButton;
    private Combo mDisplayModeCombo;
    private Composite mTopStackComposite;
    private StackLayout mTopStackLayout;
    private Composite mAllocationStackComposite;
    private StackLayout mAllocationStackLayout;
    private Composite mTableModeControl;
    private Control mAllocationModeTop;
    private Control mLibraryModeTopControl;
    private Composite mPageUIComposite;
    private Label mTotalMemoryLabel;
    private Label mPageLabel;
    private Button mPageNextButton;
    private Button mPagePreviousButton;
    private Table mAllocationTable;
    private Table mLibraryTable;
    private Table mLibraryAllocationTable;
    private Table mDetailTable;
    private Label mImage;
    private int mAllocDisplayMode = ALLOC_DISPLAY_ALL;
    private StackCallThread mStackCallThread;
    private FillTableThread mFillTableThread;
    private ClientData mClientData;
    private ClientData mBackUpClientData;
    private final ArrayList<NativeAllocationInfo> mAllocations =
        new ArrayList<NativeAllocationInfo>();
    private final ArrayList<NativeAllocationInfo> mDisplayedAllocations =
        new ArrayList<NativeAllocationInfo>();
    private final ArrayList<NativeAllocationInfo> mBackUpAllocations =
        new ArrayList<NativeAllocationInfo>();
    private int mBackUpTotalMemory;
    private int mCurrentPage = 0;
    private int mPageCount = 0;
    private final ArrayList<LibraryAllocations> mLibraryAllocations =
        new ArrayList<LibraryAllocations>();
    private static final int NOT_SELECTED = 0;
    private static final int NOT_ENABLED = 1;
    private static final int ENABLED = 2;
    private static final int DISPLAY_PER_PAGE = 20;
    private static final String PREFS_ALLOCATION_SASH = "NHallocSash"; 
    private static final String PREFS_LIBRARY_SASH = "NHlibrarySash"; 
    private static final String PREFS_DETAIL_ADDRESS = "NHdetailAddress"; 
    private static final String PREFS_DETAIL_LIBRARY = "NHdetailLibrary"; 
    private static final String PREFS_DETAIL_METHOD = "NHdetailMethod"; 
    private static final String PREFS_DETAIL_FILE = "NHdetailFile"; 
    private static final String PREFS_DETAIL_LINE = "NHdetailLine"; 
    private static final String PREFS_ALLOC_TOTAL = "NHallocTotal"; 
    private static final String PREFS_ALLOC_COUNT = "NHallocCount"; 
    private static final String PREFS_ALLOC_SIZE = "NHallocSize"; 
    private static final String PREFS_ALLOC_LIBRARY = "NHallocLib"; 
    private static final String PREFS_ALLOC_METHOD = "NHallocMethod"; 
    private static final String PREFS_ALLOC_FILE = "NHallocFile"; 
    private static final String PREFS_LIB_LIBRARY = "NHlibLibrary"; 
    private static final String PREFS_LIB_SIZE = "NHlibSize"; 
    private static final String PREFS_LIB_COUNT = "NHlibCount"; 
    private static final String PREFS_LIBALLOC_TOTAL = "NHlibAllocTotal"; 
    private static final String PREFS_LIBALLOC_COUNT = "NHlibAllocCount"; 
    private static final String PREFS_LIBALLOC_SIZE = "NHlibAllocSize"; 
    private static final String PREFS_LIBALLOC_METHOD = "NHlibAllocMethod"; 
    private static DecimalFormat sFormatter;
    static {
        sFormatter = (DecimalFormat)NumberFormat.getInstance();
        if (sFormatter != null)
            sFormatter = new DecimalFormat("#,###");
        else
            sFormatter.applyPattern("#,###");
    }
    private HashMap<Long, NativeStackCallInfo> mSourceCache =
        new HashMap<Long,NativeStackCallInfo>();
    private long mTotalSize;
    private Button mSaveButton;
    private Button mSymbolsButton;
    private class StackCallThread extends BackgroundThread {
        private ClientData mClientData;
        public StackCallThread(ClientData cd) {
            mClientData = cd;
        }
        public ClientData getClientData() {
            return mClientData;
        }
        @Override
        public void run() {
            Iterator<NativeAllocationInfo> iter = mAllocations.iterator();
            int total = mAllocations.size();
            int count = 0;
            while (iter.hasNext()) {
                if (isQuitting())
                    return;
                NativeAllocationInfo info = iter.next();
                if (info.isStackCallResolved() == false) {
                    final Long[] list = info.getStackCallAddresses();
                    final int size = list.length;
                    ArrayList<NativeStackCallInfo> resolvedStackCall =
                        new ArrayList<NativeStackCallInfo>(); 
                    for (int i = 0; i < size; i++) {
                        long addr = list[i];
                        NativeStackCallInfo source = mSourceCache.get(addr);
                        if (source == null) {
                            source = sourceForAddr(addr);
                            mSourceCache.put(addr, source);
                        }
                        resolvedStackCall.add(source);
                    }
                    info.setResolvedStackCall(resolvedStackCall);
                }
                count++;
                if ((count % DISPLAY_PER_PAGE) == 0 && count != total) {
                    if (updateNHAllocationStackCalls(mClientData, count) == false) {
                        return;
                    }
                }
            }
            updateNHAllocationStackCalls(mClientData, count);
        }
        private NativeStackCallInfo sourceForAddr(long addr) {
            NativeLibraryMapInfo library = getLibraryFor(addr);
            if (library != null) {
                Addr2Line process = Addr2Line.getProcess(library.getLibraryName());
                if (process != null) {
                    long value = addr - library.getStartAddress();
                    NativeStackCallInfo info = process.getAddress(value);
                    if (info != null) {
                        return info;
                    }
                }
            }
            return new NativeStackCallInfo(library != null ? library.getLibraryName() : null,
                    Long.toHexString(addr), "");
        }
        private NativeLibraryMapInfo getLibraryFor(long addr) {
            Iterator<NativeLibraryMapInfo> it = mClientData.getNativeLibraryMapInfo();
            while (it.hasNext()) {
                NativeLibraryMapInfo info = it.next();
                if (info.isWithinLibrary(addr)) {
                    return info;
                }
            }
            Log.d("ddm-nativeheap", "Failed finding Library for " + Long.toHexString(addr));
            return null;
        }
        private boolean updateNHAllocationStackCalls(final ClientData clientData, final int count) {
            if (mDisplay.isDisposed() == false) {
                mDisplay.asyncExec(new Runnable() {
                    public void run() {
                        updateAllocationStackCalls(clientData, count);
                    }
                });
                return true;
            }
            return false;
        }
    }
    private class FillTableThread extends BackgroundThread {
        private LibraryAllocations mLibAlloc;
        private int mMax;
        public FillTableThread(LibraryAllocations liballoc, int m) {
            mLibAlloc = liballoc;
            mMax = m;
        }
        @Override
        public void run() {
            for (int i = mMax; i > 0 && isQuitting() == false; i -= 10) {
                updateNHLibraryAllocationTable(mLibAlloc, mMax - i, mMax - i + 10);
            }
        }
        private void updateNHLibraryAllocationTable(final LibraryAllocations libAlloc,
                final int start, final int end) {
            if (mDisplay.isDisposed() == false) {
                mDisplay.asyncExec(new Runnable() {
                    public void run() {
                        updateLibraryAllocationTable(libAlloc, start, end);
                    }
                });
            }
        }
    }
    public static class LibraryAllocations {
        private String mLibrary;
        private final ArrayList<NativeAllocationInfo> mLibAllocations =
            new ArrayList<NativeAllocationInfo>();
        private int mSize;
        private int mCount;
        public LibraryAllocations(final String lib) {
            mLibrary = lib;
        }
        public String getLibrary() {
            return mLibrary;
        }
        public void addAllocation(NativeAllocationInfo info) {
            mLibAllocations.add(info);
        }
        public Iterator<NativeAllocationInfo> getAllocations() {
            return mLibAllocations.iterator();
        }
        public NativeAllocationInfo getAllocation(int index) {
            return mLibAllocations.get(index);
        }
        public int getAllocationSize() {
            return mLibAllocations.size();
        }
        public int getSize() {
            return mSize;
        }
        public int getCount() {
            return mCount;
        }
        public void computeAllocationSizeAndCount() {
            mSize = 0;
            mCount = 0;
            for (NativeAllocationInfo info : mLibAllocations) {
                mCount += info.getAllocationCount();
                mSize += info.getAllocationCount() * info.getSize();
            }
            Collections.sort(mLibAllocations, new Comparator<NativeAllocationInfo>() {
                public int compare(NativeAllocationInfo o1, NativeAllocationInfo o2) {
                    return o2.getAllocationCount() * o2.getSize() -
                        o1.getAllocationCount() * o1.getSize();
                }
            });
        }
    }
    @Override
    protected Control createControl(Composite parent) {
        mDisplay = parent.getDisplay();
        mBase = new Composite(parent, SWT.NONE);
        GridLayout gl = new GridLayout(1, false);
        gl.horizontalSpacing = 0;
        gl.verticalSpacing = 0;
        mBase.setLayout(gl);
        mBase.setLayoutData(new GridData(GridData.FILL_BOTH));
        Composite tmp = new Composite(mBase, SWT.NONE);
        tmp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        tmp.setLayout(gl = new GridLayout(2, false));
        gl.marginWidth = gl.marginHeight = 0;
        mFullUpdateButton = new Button(tmp, SWT.NONE);
        mFullUpdateButton.setText("Full Update");
        mFullUpdateButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                mBackUpClientData = null;
                mDisplayModeCombo.setEnabled(false);
                mSaveButton.setEnabled(false);
                emptyTables();
                if (mStackCallThread != null &&
                        mStackCallThread.getClientData() == mClientData) {
                    mStackCallThread.quit();
                    mStackCallThread = null;
                }
                mLibraryAllocations.clear();
                Client client = getCurrentClient();
                if (client != null) {
                    client.requestNativeHeapInformation();
                }
            }
        });
        mUpdateStatus = new Label(tmp, SWT.NONE);
        mUpdateStatus.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        Composite top_layout = new Composite(mBase, SWT.NONE);
        top_layout.setLayout(gl = new GridLayout(4, false));
        gl.marginWidth = gl.marginHeight = 0;
        new Label(top_layout, SWT.NONE).setText("Show:");
        mAllocDisplayCombo = new Combo(top_layout, SWT.DROP_DOWN | SWT.READ_ONLY);
        mAllocDisplayCombo.setLayoutData(new GridData(
                GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
        mAllocDisplayCombo.add("All Allocations");
        mAllocDisplayCombo.add("Pre-Zygote Allocations");
        mAllocDisplayCombo.add("Zygote Child Allocations (Z)");
        mAllocDisplayCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                onAllocDisplayChange();
            }
        });
        mAllocDisplayCombo.select(0);
        Label separator = new Label(top_layout, SWT.SEPARATOR | SWT.VERTICAL);
        GridData gd;
        separator.setLayoutData(gd = new GridData(
                GridData.VERTICAL_ALIGN_FILL | GridData.GRAB_VERTICAL));
        gd.heightHint = 0;
        gd.verticalSpan = 2;
        mSaveButton = new Button(top_layout, SWT.PUSH);
        mSaveButton.setText("Save...");
        mSaveButton.setEnabled(false);
        mSaveButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                FileDialog fileDialog = new FileDialog(mBase.getShell(), SWT.SAVE);
                fileDialog.setText("Save Allocations");
                fileDialog.setFileName("allocations.txt");
                String fileName = fileDialog.open();
                if (fileName != null) {
                    saveAllocations(fileName);
                }
            }
        });
        Label l = new Label(top_layout, SWT.NONE);
        l.setText("Display:");
        mDisplayModeCombo = new Combo(top_layout, SWT.DROP_DOWN | SWT.READ_ONLY);
        mDisplayModeCombo.setLayoutData(new GridData(
                GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
        mDisplayModeCombo.setItems(new String[] { "Allocation List", "By Libraries" });
        mDisplayModeCombo.select(0);
        mDisplayModeCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                switchDisplayMode();
            }
        });
        mDisplayModeCombo.setEnabled(false);
        mSymbolsButton = new Button(top_layout, SWT.PUSH);
        mSymbolsButton.setText("Load Symbols");
        mSymbolsButton.setEnabled(false);
        mTopStackComposite = new Composite(mBase, SWT.NONE);
        mTopStackComposite.setLayout(mTopStackLayout = new StackLayout());
        mTopStackComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
        createTableDisplay(mTopStackComposite);
        mTopStackLayout.topControl = mTableModeControl;
        mTopStackComposite.layout();
        setUpdateStatus(NOT_SELECTED);
        mBase.pack();
        return mBase;
    }
    @Override
    public void setFocus() {
    }
    public void clientChanged(final Client client, int changeMask) {
        if (client == getCurrentClient()) {
            if ((changeMask & Client.CHANGE_NATIVE_HEAP_DATA) == Client.CHANGE_NATIVE_HEAP_DATA) {
                if (mBase.isDisposed())
                    return;
                mBase.getDisplay().asyncExec(new Runnable() {
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
        if (mBase.isDisposed())
            return;
        Client client = getCurrentClient();
        mDisplayModeCombo.setEnabled(false);
        emptyTables();
        Log.d("ddms", "NativeHeapPanel: changed " + client);
        if (client != null) {
            ClientData cd = client.getClientData();
            mClientData = cd;
            setUpdateStatus(ENABLED);
            initAllocationDisplay();
        } else {
            mClientData = null;
            setUpdateStatus(NOT_SELECTED);
        }
        mBase.pack();
    }
    @WorkerThread
    public void updateAllocationStackCalls(ClientData cd, int count) {
        if (cd == mClientData) {
            int total = mAllocations.size();
            if (count == total) {
                mDisplayModeCombo.setEnabled(true);
                mSaveButton.setEnabled(true);
                mStackCallThread = null;
            } else {
            }
            try {
                int index = mAllocationTable.getSelectionIndex();
                NativeAllocationInfo info = null;
                if (index != -1) {
                    info = (NativeAllocationInfo)mAllocationTable.getItem(index).getData();
                }
                emptyTables();
                fillAllocationTable();
                mAllocationTable.setSelection(index);
                if (info != null) {
                    fillDetailTable(info);
                }
            } catch (SWTException e) {
                if (mAllocationTable.isDisposed()) {
                } else {
                    throw e;
                }
            }
        } else {
        }
    }
    @Override
    protected void setTableFocusListener() {
        addTableToFocusListener(mAllocationTable);
        addTableToFocusListener(mLibraryTable);
        addTableToFocusListener(mLibraryAllocationTable);
        addTableToFocusListener(mDetailTable);
    }
    protected void onAllocDisplayChange() {
        mAllocDisplayMode = mAllocDisplayCombo.getSelectionIndex();
        updateAllocDisplayList();
        updateTotalMemoryDisplay();
        mCurrentPage = 0;
        updatePageUI();
        switchDisplayMode();
    }
    private void updateAllocDisplayList() {
        mTotalSize = 0;
        mDisplayedAllocations.clear();
        for (NativeAllocationInfo info : mAllocations) {
            if (mAllocDisplayMode == ALLOC_DISPLAY_ALL ||
                    (mAllocDisplayMode == ALLOC_DISPLAY_PRE_ZYGOTE ^ info.isZygoteChild())) {
                mDisplayedAllocations.add(info);
                mTotalSize += info.getSize() * info.getAllocationCount();
            } else {
                continue;
            }
        }
        int count = mDisplayedAllocations.size();
        mPageCount = count / DISPLAY_PER_PAGE;
        if ((count % DISPLAY_PER_PAGE) > 0) {
            mPageCount++;
        }
    }
    private void updateTotalMemoryDisplay() {
        switch (mAllocDisplayMode) {
            case ALLOC_DISPLAY_ALL:
                mTotalMemoryLabel.setText(String.format("Total Memory: %1$s Bytes",
                        sFormatter.format(mTotalSize)));
                break;
            case ALLOC_DISPLAY_PRE_ZYGOTE:
                mTotalMemoryLabel.setText(String.format("Zygote Memory: %1$s Bytes",
                        sFormatter.format(mTotalSize)));
                break;
            case ALLOC_DISPLAY_POST_ZYGOTE:
                mTotalMemoryLabel.setText(String.format("Post-zygote Memory: %1$s Bytes",
                        sFormatter.format(mTotalSize)));
                break;
        }
    }
    private void switchDisplayMode() {
        switch (mDisplayModeCombo.getSelectionIndex()) {
            case 0: {
                mTopStackLayout.topControl = mTableModeControl;
                mAllocationStackLayout.topControl = mAllocationModeTop;
                mAllocationStackComposite.layout();
                mTopStackComposite.layout();
                emptyTables();
                fillAllocationTable();
            }
                break;
            case 1: {
                mTopStackLayout.topControl = mTableModeControl;
                mAllocationStackLayout.topControl = mLibraryModeTopControl;
                mAllocationStackComposite.layout();
                mTopStackComposite.layout();
                emptyTables();
                fillLibraryTable();
            }
                break;
        }
    }
    private void initAllocationDisplay() {
        mAllocations.clear();
        mAllocations.addAll(mClientData.getNativeAllocationList());
        updateAllocDisplayList();
        if (mBackUpClientData != null && mBackUpClientData == mClientData) {
            ArrayList<NativeAllocationInfo> add = new ArrayList<NativeAllocationInfo>();
            for (NativeAllocationInfo mi : mAllocations) {
                boolean found = false;
                for (NativeAllocationInfo old_mi : mBackUpAllocations) {
                    if (mi.equals(old_mi)) {
                        found = true;
                        break;
                    }
                }
                if (found == false) {
                    add.add(mi);
                }
            }
            mAllocations.clear();
            mAllocations.addAll(add);
            int count = 0;
            for (NativeAllocationInfo allocInfo : mAllocations) {
                count += allocInfo.getSize() * allocInfo.getAllocationCount();
            }
            mTotalMemoryLabel.setText(String.format("Memory Difference: %1$s Bytes",
                    sFormatter.format(count)));
        }
        else {
            updateTotalMemoryDisplay();
        }
        mTotalMemoryLabel.pack();
        mDisplayModeCombo.select(0);
        mLibraryAllocations.clear();
        mCurrentPage = 0;
        updatePageUI();
        switchDisplayMode();
        if (mAllocations.size() > 0) {
            mStackCallThread = new StackCallThread(mClientData);
            mStackCallThread.start();
        }
    }
    private void updatePageUI() {
        if (mPageCount == 0) {
            mPageLabel.setText("0 of 0 allocations.");
        } else {
            StringBuffer buffer = new StringBuffer();
            int start = (mCurrentPage * DISPLAY_PER_PAGE) + 1;
            int count = mDisplayedAllocations.size();
            int end = Math.min(start + DISPLAY_PER_PAGE - 1, count);
            buffer.append(sFormatter.format(start));
            buffer.append(" - ");
            buffer.append(sFormatter.format(end));
            buffer.append(" of ");
            buffer.append(sFormatter.format(count));
            buffer.append(" allocations.");
            mPageLabel.setText(buffer.toString());
        }
        mPagePreviousButton.setEnabled(mCurrentPage > 0);
        mPageNextButton.setEnabled(mCurrentPage < mPageCount - 1);
        mPageLabel.pack();
        mPageUIComposite.pack();
    }
    private void fillAllocationTable() {
        int count = mDisplayedAllocations.size();
        int start = mCurrentPage * DISPLAY_PER_PAGE;
        int end = start + DISPLAY_PER_PAGE;
        for (int i = start; i < end && i < count; i++) {
            NativeAllocationInfo info = mDisplayedAllocations.get(i);
            TableItem item = null;
            if (mAllocDisplayMode == ALLOC_DISPLAY_ALL)  {
                item = new TableItem(mAllocationTable, SWT.NONE);
                item.setText(0, (info.isZygoteChild() ? "Z " : "") +
                        sFormatter.format(info.getSize() * info.getAllocationCount()));
                item.setText(1, sFormatter.format(info.getAllocationCount()));
                item.setText(2, sFormatter.format(info.getSize()));
            } else if (mAllocDisplayMode == ALLOC_DISPLAY_PRE_ZYGOTE ^ info.isZygoteChild()) {
                item = new TableItem(mAllocationTable, SWT.NONE);
                item.setText(0, sFormatter.format(info.getSize() * info.getAllocationCount()));
                item.setText(1, sFormatter.format(info.getAllocationCount()));
                item.setText(2, sFormatter.format(info.getSize()));
            } else {
                continue;
            }
            item.setData(info);
            NativeStackCallInfo bti = info.getRelevantStackCallInfo();
            if (bti != null) {
                String lib = bti.getLibraryName();
                String method = bti.getMethodName();
                String source = bti.getSourceFile();
                if (lib != null)
                    item.setText(3, lib);
                if (method != null)
                    item.setText(4, method);
                if (source != null)
                    item.setText(5, source);
            }
        }
    }
    private void fillLibraryTable() {
        sortAllocationsPerLibrary();
        for (LibraryAllocations liballoc : mLibraryAllocations) {
            if (liballoc != null) {
                TableItem item = new TableItem(mLibraryTable, SWT.NONE);
                String lib = liballoc.getLibrary();
                item.setText(0, lib != null ? lib : "");
                item.setText(1, sFormatter.format(liballoc.getSize()));
                item.setText(2, sFormatter.format(liballoc.getCount()));
            }
        }
    }
    private void fillLibraryAllocationTable() {
        mLibraryAllocationTable.removeAll();
        mDetailTable.removeAll();
        int index = mLibraryTable.getSelectionIndex();
        if (index != -1) {
            LibraryAllocations liballoc = mLibraryAllocations.get(index);
            if (mFillTableThread != null) {
                mFillTableThread.quit();
            }
            mFillTableThread = new FillTableThread(liballoc,
                    liballoc.getAllocationSize());
            mFillTableThread.start();
        }
    }
    public void updateLibraryAllocationTable(LibraryAllocations liballoc,
            int start, int end) {
        try {
            if (mLibraryTable.isDisposed() == false) {
                int index = mLibraryTable.getSelectionIndex();
                if (index != -1) {
                    LibraryAllocations newliballoc = mLibraryAllocations.get(
                            index);
                    if (newliballoc == liballoc) {
                        int count = liballoc.getAllocationSize();
                        for (int i = start; i < end && i < count; i++) {
                            NativeAllocationInfo info = liballoc.getAllocation(i);
                            TableItem item = new TableItem(
                                    mLibraryAllocationTable, SWT.NONE);
                            item.setText(0, sFormatter.format(
                                    info.getSize() * info.getAllocationCount()));
                            item.setText(1, sFormatter.format(info.getAllocationCount()));
                            item.setText(2, sFormatter.format(info.getSize()));
                            NativeStackCallInfo stackCallInfo = info.getRelevantStackCallInfo();
                            if (stackCallInfo != null) {
                                item.setText(3, stackCallInfo.getMethodName());
                            }
                        }
                    } else {
                        if (mFillTableThread != null) {
                            mFillTableThread.quit();
                            mFillTableThread = null;
                        }
                    }
                }
            }
        } catch (SWTException e) {
            Log.e("ddms", "error when updating the library allocation table");
        }
    }
    private void fillDetailTable(final NativeAllocationInfo mi) {
        mDetailTable.removeAll();
        mDetailTable.setRedraw(false);
        try {
            Long[] addresses = mi.getStackCallAddresses();
            NativeStackCallInfo[] resolvedStackCall = mi.getResolvedStackCall();
            if (resolvedStackCall == null) {
                return;
            }
            for (int i = 0 ; i < resolvedStackCall.length ; i++) {
                if (addresses[i] == null || addresses[i].longValue() == 0) {
                    continue;
                }
                long addr = addresses[i].longValue();
                NativeStackCallInfo source = resolvedStackCall[i];
                TableItem item = new TableItem(mDetailTable, SWT.NONE);
                item.setText(0, String.format("%08x", addr)); 
                String libraryName = source.getLibraryName();
                String methodName = source.getMethodName();
                String sourceFile = source.getSourceFile();
                int lineNumber = source.getLineNumber();
                if (libraryName != null)
                    item.setText(1, libraryName);
                if (methodName != null)
                    item.setText(2, methodName);
                if (sourceFile != null)
                    item.setText(3, sourceFile);
                if (lineNumber != -1)
                    item.setText(4, Integer.toString(lineNumber));
            }
        } finally {
            mDetailTable.setRedraw(true);
        }
    }
    private void setUpdateStatus(int status) {
        switch (status) {
            case NOT_SELECTED:
                mUpdateStatus.setText("Select a client to see heap info");
                mAllocDisplayCombo.setEnabled(false);
                mFullUpdateButton.setEnabled(false);
                break;
            case NOT_ENABLED:
                mUpdateStatus.setText("Heap updates are " + "NOT ENABLED for this client");
                mAllocDisplayCombo.setEnabled(false);
                mFullUpdateButton.setEnabled(false);
                break;
            case ENABLED:
                mUpdateStatus.setText("Press 'Full Update' to retrieve " + "latest data");
                mAllocDisplayCombo.setEnabled(true);
                mFullUpdateButton.setEnabled(true);
                break;
            default:
                throw new RuntimeException();
        }
        mUpdateStatus.pack();
    }
    private void createTableDisplay(Composite base) {
        final int minPanelWidth = 60;
        final IPreferenceStore prefs = DdmUiPreferences.getStore();
        mTableModeControl = new Composite(base, SWT.NONE);
        GridLayout gl = new GridLayout(1, false);
        gl.marginLeft = gl.marginRight = gl.marginTop = gl.marginBottom = 0;
        mTableModeControl.setLayout(gl);
        mTableModeControl.setLayoutData(new GridData(GridData.FILL_BOTH));
        mTotalMemoryLabel = new Label(mTableModeControl, SWT.NONE);
        mTotalMemoryLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        mTotalMemoryLabel.setText("Total Memory: 0 Bytes");
        final Composite sash_composite = new Composite(mTableModeControl,
                SWT.NONE);
        sash_composite.setLayout(new FormLayout());
        sash_composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        mAllocationStackComposite = new Composite(sash_composite, SWT.NONE);
        mAllocationStackLayout = new StackLayout();
        mAllocationStackComposite.setLayout(mAllocationStackLayout);
        mAllocationStackComposite.setLayoutData(new GridData(
                GridData.FILL_BOTH));
        createAllocationTopHalf(mAllocationStackComposite);
        createLibraryTopHalf(mAllocationStackComposite);
        final Sash sash = new Sash(sash_composite, SWT.HORIZONTAL);
        createDetailTable(sash_composite);
        mAllocationStackLayout.topControl = mAllocationModeTop;
        FormData data = new FormData();
        data.top = new FormAttachment(mTotalMemoryLabel, 0);
        data.bottom = new FormAttachment(sash, 0);
        data.left = new FormAttachment(0, 0);
        data.right = new FormAttachment(100, 0);
        mAllocationStackComposite.setLayoutData(data);
        final FormData sashData = new FormData();
        if (prefs != null && prefs.contains(PREFS_ALLOCATION_SASH)) {
            sashData.top = new FormAttachment(0,
                    prefs.getInt(PREFS_ALLOCATION_SASH));
        } else {
            sashData.top = new FormAttachment(50, 0); 
        }
        sashData.left = new FormAttachment(0, 0);
        sashData.right = new FormAttachment(100, 0);
        sash.setLayoutData(sashData);
        data = new FormData();
        data.top = new FormAttachment(sash, 0);
        data.bottom = new FormAttachment(100, 0);
        data.left = new FormAttachment(0, 0);
        data.right = new FormAttachment(100, 0);
        mDetailTable.setLayoutData(data);
        sash.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                Rectangle sashRect = sash.getBounds();
                Rectangle panelRect = sash_composite.getClientArea();
                int bottom = panelRect.height - sashRect.height - minPanelWidth;
                e.y = Math.max(Math.min(e.y, bottom), minPanelWidth);
                if (e.y != sashRect.y) {
                    sashData.top = new FormAttachment(0, e.y);
                    prefs.setValue(PREFS_ALLOCATION_SASH, e.y);
                    sash_composite.layout();
                }
            }
        });
    }
    private void createDetailTable(Composite base) {
        final IPreferenceStore prefs = DdmUiPreferences.getStore();
        mDetailTable = new Table(base, SWT.MULTI | SWT.FULL_SELECTION);
        mDetailTable.setLayoutData(new GridData(GridData.FILL_BOTH));
        mDetailTable.setHeaderVisible(true);
        mDetailTable.setLinesVisible(true);
        TableHelper.createTableColumn(mDetailTable, "Address", SWT.RIGHT,
                "00000000", PREFS_DETAIL_ADDRESS, prefs); 
        TableHelper.createTableColumn(mDetailTable, "Library", SWT.LEFT,
                "abcdefghijklmnopqrst", PREFS_DETAIL_LIBRARY, prefs); 
        TableHelper.createTableColumn(mDetailTable, "Method", SWT.LEFT,
                "abcdefghijklmnopqrst", PREFS_DETAIL_METHOD, prefs); 
        TableHelper.createTableColumn(mDetailTable, "File", SWT.LEFT,
                "abcdefghijklmnopqrstuvwxyz", PREFS_DETAIL_FILE, prefs); 
        TableHelper.createTableColumn(mDetailTable, "Line", SWT.RIGHT,
                "9,999", PREFS_DETAIL_LINE, prefs); 
    }
    private void createAllocationTopHalf(Composite b) {
        final IPreferenceStore prefs = DdmUiPreferences.getStore();
        Composite base = new Composite(b, SWT.NONE);
        mAllocationModeTop = base;
        GridLayout gl = new GridLayout(1, false);
        gl.marginLeft = gl.marginRight = gl.marginTop = gl.marginBottom = 0;
        gl.verticalSpacing = 0;
        base.setLayout(gl);
        base.setLayoutData(new GridData(GridData.FILL_BOTH));
        mPageUIComposite = new Composite(base, SWT.NONE);
        mPageUIComposite.setLayoutData(new GridData(
                GridData.HORIZONTAL_ALIGN_BEGINNING));
        gl = new GridLayout(3, false);
        gl.marginLeft = gl.marginRight = gl.marginTop = gl.marginBottom = 0;
        gl.horizontalSpacing = 0;
        mPageUIComposite.setLayout(gl);
        mPagePreviousButton = new Button(mPageUIComposite, SWT.NONE);
        mPagePreviousButton.setText("<");
        mPagePreviousButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                mCurrentPage--;
                updatePageUI();
                emptyTables();
                fillAllocationTable();
            }
        });
        mPageNextButton = new Button(mPageUIComposite, SWT.NONE);
        mPageNextButton.setText(">");
        mPageNextButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                mCurrentPage++;
                updatePageUI();
                emptyTables();
                fillAllocationTable();
            }
        });
        mPageLabel = new Label(mPageUIComposite, SWT.NONE);
        mPageLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        updatePageUI();
        mAllocationTable = new Table(base, SWT.MULTI | SWT.FULL_SELECTION);
        mAllocationTable.setLayoutData(new GridData(GridData.FILL_BOTH));
        mAllocationTable.setHeaderVisible(true);
        mAllocationTable.setLinesVisible(true);
        TableHelper.createTableColumn(mAllocationTable, "Total", SWT.RIGHT,
                "9,999,999", PREFS_ALLOC_TOTAL, prefs); 
        TableHelper.createTableColumn(mAllocationTable, "Count", SWT.RIGHT,
                "9,999", PREFS_ALLOC_COUNT, prefs); 
        TableHelper.createTableColumn(mAllocationTable, "Size", SWT.RIGHT,
                "999,999", PREFS_ALLOC_SIZE, prefs); 
        TableHelper.createTableColumn(mAllocationTable, "Library", SWT.LEFT,
                "abcdefghijklmnopqrst", PREFS_ALLOC_LIBRARY, prefs); 
        TableHelper.createTableColumn(mAllocationTable, "Method", SWT.LEFT,
                "abcdefghijklmnopqrst", PREFS_ALLOC_METHOD, prefs); 
        TableHelper.createTableColumn(mAllocationTable, "File", SWT.LEFT,
                "abcdefghijklmnopqrstuvwxyz", PREFS_ALLOC_FILE, prefs); 
        mAllocationTable.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                int index = mAllocationTable.getSelectionIndex();
                TableItem item = mAllocationTable.getItem(index);
                if (item != null && item.getData() instanceof NativeAllocationInfo) {
                    fillDetailTable((NativeAllocationInfo)item.getData());
                }
            }
        });
    }
    private void createLibraryTopHalf(Composite base) {
        final int minPanelWidth = 60;
        final IPreferenceStore prefs = DdmUiPreferences.getStore();
        final Composite top = new Composite(base, SWT.NONE);
        mLibraryModeTopControl = top;
        top.setLayout(new FormLayout());
        top.setLayoutData(new GridData(GridData.FILL_BOTH));
        mLibraryTable = new Table(top, SWT.MULTI | SWT.FULL_SELECTION);
        mLibraryTable.setLayoutData(new GridData(GridData.FILL_BOTH));
        mLibraryTable.setHeaderVisible(true);
        mLibraryTable.setLinesVisible(true);
        TableHelper.createTableColumn(mLibraryTable, "Library", SWT.LEFT,
                "abcdefghijklmnopqrstuvwxyz", PREFS_LIB_LIBRARY, prefs); 
        TableHelper.createTableColumn(mLibraryTable, "Size", SWT.RIGHT,
                "9,999,999", PREFS_LIB_SIZE, prefs); 
        TableHelper.createTableColumn(mLibraryTable, "Count", SWT.RIGHT,
                "9,999", PREFS_LIB_COUNT, prefs); 
        mLibraryTable.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                fillLibraryAllocationTable();
            }
        });
        final Sash sash = new Sash(top, SWT.VERTICAL);
        mLibraryAllocationTable = new Table(top, SWT.MULTI | SWT.FULL_SELECTION);
        mLibraryAllocationTable.setLayoutData(new GridData(GridData.FILL_BOTH));
        mLibraryAllocationTable.setHeaderVisible(true);
        mLibraryAllocationTable.setLinesVisible(true);
        TableHelper.createTableColumn(mLibraryAllocationTable, "Total",
                SWT.RIGHT, "9,999,999", PREFS_LIBALLOC_TOTAL, prefs); 
        TableHelper.createTableColumn(mLibraryAllocationTable, "Count",
                SWT.RIGHT, "9,999", PREFS_LIBALLOC_COUNT, prefs); 
        TableHelper.createTableColumn(mLibraryAllocationTable, "Size",
                SWT.RIGHT, "999,999", PREFS_LIBALLOC_SIZE, prefs); 
        TableHelper.createTableColumn(mLibraryAllocationTable, "Method",
                SWT.LEFT, "abcdefghijklmnopqrst", PREFS_LIBALLOC_METHOD, prefs); 
        mLibraryAllocationTable.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                int index1 = mLibraryTable.getSelectionIndex();
                int index2 = mLibraryAllocationTable.getSelectionIndex();
                LibraryAllocations liballoc = mLibraryAllocations.get(index1);
                NativeAllocationInfo info = liballoc.getAllocation(index2);
                fillDetailTable(info);
            }
        });
        FormData data = new FormData();
        data.top = new FormAttachment(0, 0);
        data.bottom = new FormAttachment(100, 0);
        data.left = new FormAttachment(0, 0);
        data.right = new FormAttachment(sash, 0);
        mLibraryTable.setLayoutData(data);
        final FormData sashData = new FormData();
        if (prefs != null && prefs.contains(PREFS_LIBRARY_SASH)) {
            sashData.left = new FormAttachment(0,
                    prefs.getInt(PREFS_LIBRARY_SASH));
        } else {
            sashData.left = new FormAttachment(50, 0);
        }
        sashData.bottom = new FormAttachment(100, 0);
        sashData.top = new FormAttachment(0, 0); 
        sash.setLayoutData(sashData);
        data = new FormData();
        data.top = new FormAttachment(0, 0);
        data.bottom = new FormAttachment(100, 0);
        data.left = new FormAttachment(sash, 0);
        data.right = new FormAttachment(100, 0);
        mLibraryAllocationTable.setLayoutData(data);
        sash.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                Rectangle sashRect = sash.getBounds();
                Rectangle panelRect = top.getClientArea();
                int right = panelRect.width - sashRect.width - minPanelWidth;
                e.x = Math.max(Math.min(e.x, right), minPanelWidth);
                if (e.x != sashRect.x) {
                    sashData.left = new FormAttachment(0, e.x);
                    prefs.setValue(PREFS_LIBRARY_SASH, e.y);
                    top.layout();
                }
            }
        });
    }
    private void emptyTables() {
        mAllocationTable.removeAll();
        mLibraryTable.removeAll();
        mLibraryAllocationTable.removeAll();
        mDetailTable.removeAll();
    }
    private void sortAllocationsPerLibrary() {
        if (mClientData != null) {
            mLibraryAllocations.clear();
            HashMap<String, LibraryAllocations> libcache =
                new HashMap<String, LibraryAllocations>();
            int count = mDisplayedAllocations.size();
            for (int i = 0; i < count; i++) {
                NativeAllocationInfo allocInfo = mDisplayedAllocations.get(i);
                NativeStackCallInfo stackCallInfo = allocInfo.getRelevantStackCallInfo();
                if (stackCallInfo != null) {
                    String libraryName = stackCallInfo.getLibraryName();
                    LibraryAllocations liballoc = libcache.get(libraryName);
                    if (liballoc == null) {
                        liballoc = new LibraryAllocations(libraryName);
                        libcache.put(libraryName, liballoc);
                        mLibraryAllocations.add(liballoc);
                    }
                    liballoc.addAllocation(allocInfo);
                }
            }
            for (LibraryAllocations liballoc : mLibraryAllocations) {
                liballoc.computeAllocationSizeAndCount();
            }
            Collections.sort(mLibraryAllocations,
                    new Comparator<LibraryAllocations>() {
                public int compare(LibraryAllocations o1,
                        LibraryAllocations o2) {
                    return o2.getSize() - o1.getSize();
                }
            });
        }
    }
    private void renderBitmap(ClientData cd) {
        byte[] pixData;
        synchronized (cd) {
            if (serializeHeapData(cd.getVmHeapData()) == false) {
                return;
            }
            pixData = getSerializedData();
            ImageData id = createLinearHeapImage(pixData, 200, mMapPalette);
            Image image = new Image(mBase.getDisplay(), id);
            mImage.setImage(image);
            mImage.pack(true);
        }
    }
    private static PaletteData createPalette() {
        RGB colors[] = new RGB[NUM_PALETTE_ENTRIES];
        colors[0]
                = new RGB(192, 192, 192); 
        mMapLegend[0]
                = "(heap expansion area)";
        colors[1]
                = new RGB(0, 0, 0);       
        mMapLegend[1]
                = "free";
        colors[HeapSegmentElement.KIND_OBJECT + 2]
                = new RGB(0, 0, 255);     
        mMapLegend[HeapSegmentElement.KIND_OBJECT + 2]
                = "data object";
        colors[HeapSegmentElement.KIND_CLASS_OBJECT + 2]
                = new RGB(0, 255, 0);     
        mMapLegend[HeapSegmentElement.KIND_CLASS_OBJECT + 2]
                = "class object";
        colors[HeapSegmentElement.KIND_ARRAY_1 + 2]
                = new RGB(255, 0, 0);     
        mMapLegend[HeapSegmentElement.KIND_ARRAY_1 + 2]
                = "1-byte array (byte[], boolean[])";
        colors[HeapSegmentElement.KIND_ARRAY_2 + 2]
                = new RGB(255, 128, 0);   
        mMapLegend[HeapSegmentElement.KIND_ARRAY_2 + 2]
                = "2-byte array (short[], char[])";
        colors[HeapSegmentElement.KIND_ARRAY_4 + 2]
                = new RGB(255, 255, 0);   
        mMapLegend[HeapSegmentElement.KIND_ARRAY_4 + 2]
                = "4-byte array (object[], int[], float[])";
        colors[HeapSegmentElement.KIND_ARRAY_8 + 2]
                = new RGB(255, 128, 128); 
        mMapLegend[HeapSegmentElement.KIND_ARRAY_8 + 2]
                = "8-byte array (long[], double[])";
        colors[HeapSegmentElement.KIND_UNKNOWN + 2]
                = new RGB(255, 0, 255);   
        mMapLegend[HeapSegmentElement.KIND_UNKNOWN + 2]
                = "unknown object";
        colors[HeapSegmentElement.KIND_NATIVE + 2]
                = new RGB(64, 64, 64);    
        mMapLegend[HeapSegmentElement.KIND_NATIVE + 2]
                = "non-Java object";
        return new PaletteData(colors);
    }
    private void saveAllocations(String fileName) {
        try {
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(fileName)));
            for (NativeAllocationInfo alloc : mAllocations) {
                out.println(alloc.toString());
            }
            out.close();
        } catch (IOException e) {
            Log.e("Native", e);
        }
    }
}
