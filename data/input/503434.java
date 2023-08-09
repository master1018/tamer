public class SdkTargetSelector {
    private IAndroidTarget[] mTargets;
    private final boolean mAllowSelection;
    private SelectionListener mSelectionListener;
    private Table mTable;
    private Label mDescription;
    private Composite mInnerGroup;
    public SdkTargetSelector(Composite parent, IAndroidTarget[] targets) {
        this(parent, targets, true );
    }
    public SdkTargetSelector(Composite parent, IAndroidTarget[] targets, boolean allowSelection) {
        mInnerGroup = new Composite(parent, SWT.NONE);
        mInnerGroup.setLayout(new GridLayout());
        mInnerGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
        mInnerGroup.setFont(parent.getFont());
        mAllowSelection = allowSelection;
        int style = SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION;
        if (allowSelection) {
            style |= SWT.CHECK;
        }
        mTable = new Table(mInnerGroup, style);
        mTable.setHeaderVisible(true);
        mTable.setLinesVisible(false);
        GridData data = new GridData();
        data.grabExcessVerticalSpace = true;
        data.grabExcessHorizontalSpace = true;
        data.horizontalAlignment = GridData.FILL;
        data.verticalAlignment = GridData.FILL;
        mTable.setLayoutData(data);
        mDescription = new Label(mInnerGroup, SWT.WRAP);
        mDescription.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        final TableColumn column0 = new TableColumn(mTable, SWT.NONE);
        column0.setText("Target Name");
        final TableColumn column1 = new TableColumn(mTable, SWT.NONE);
        column1.setText("Vendor");
        final TableColumn column2 = new TableColumn(mTable, SWT.NONE);
        column2.setText("Platform");
        final TableColumn column3 = new TableColumn(mTable, SWT.NONE);
        column3.setText("API Level");
        adjustColumnsWidth(mTable, column0, column1, column2, column3);
        setupSelectionListener(mTable);
        setTargets(targets);
        setupTooltip(mTable);
    }
    public Object getLayoutData() {
        return mInnerGroup.getLayoutData();
    }
    public IAndroidTarget[] getTargets() {
        return mTargets;
    }
    public void setTargets(IAndroidTarget[] targets) {
        mTargets = targets;
        if (mTargets != null) {
            Arrays.sort(mTargets, new Comparator<IAndroidTarget>() {
                public int compare(IAndroidTarget o1, IAndroidTarget o2) {
                    return o1.compareTo(o2);
                }
            });
        }
        fillTable(mTable);
    }
    public void setSelectionListener(SelectionListener selectionListener) {
        mSelectionListener = selectionListener;
    }
    public boolean setSelection(IAndroidTarget target) {
        if (!mAllowSelection) {
            return false;
        }
        boolean found = false;
        boolean modified = false;
        if (mTable != null && !mTable.isDisposed()) {
            for (TableItem i : mTable.getItems()) {
                if ((IAndroidTarget) i.getData() == target) {
                    found = true;
                    if (!i.getChecked()) {
                        modified = true;
                        i.setChecked(true);
                    }
                } else if (i.getChecked()) {
                    modified = true;
                    i.setChecked(false);
                }
            }
        }
        if (modified && mSelectionListener != null) {
            mSelectionListener.widgetSelected(null);
        }
        return found;
    }
    public IAndroidTarget getSelected() {
        if (mTable == null || mTable.isDisposed()) {
            return null;
        }
        for (TableItem i : mTable.getItems()) {
            if (i.getChecked()) {
                return (IAndroidTarget) i.getData();
            }
        }
        return null;
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
                column0.setWidth(r.width * 30 / 100); 
                column1.setWidth(r.width * 45 / 100); 
                column2.setWidth(r.width * 15 / 100); 
                column3.setWidth(r.width * 10 / 100); 
            }
        });
    }
    private void setupSelectionListener(final Table table) {
        if (!mAllowSelection) {
            return;
        }
        table.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent e) {
                if (e.item instanceof TableItem) {
                    TableItem i = (TableItem) e.item;
                    i.setChecked(!i.getChecked());
                    enforceSingleSelection(i);
                    updateDescription(i);
                }
                if (mSelectionListener != null) {
                    mSelectionListener.widgetDefaultSelected(e);
                }
            }
            public void widgetSelected(SelectionEvent e) {
                if (e.item instanceof TableItem) {
                    TableItem i = (TableItem) e.item;
                    enforceSingleSelection(i);
                    updateDescription(i);
                }
                if (mSelectionListener != null) {
                    mSelectionListener.widgetSelected(e);
                }
            }
            private void enforceSingleSelection(TableItem item) {
                if (item.getChecked()) {
                    Table parentTable = item.getParent();
                    for (TableItem i2 : parentTable.getItems()) {
                        if (i2 != item && i2.getChecked()) {
                            i2.setChecked(false);
                        }
                    }
                }
            }
        });
    }
    private void fillTable(final Table table) {
        if (table == null || table.isDisposed()) {
            return;
        }
        table.removeAll();
        if (mTargets != null && mTargets.length > 0) {
            table.setEnabled(true);
            for (IAndroidTarget target : mTargets) {
                TableItem item = new TableItem(table, SWT.NONE);
                item.setData(target);
                item.setText(0, target.getName());
                item.setText(1, target.getVendor());
                item.setText(2, target.getVersionName());
                item.setText(3, target.getVersion().getApiString());
            }
        } else {
            table.setEnabled(false);
            TableItem item = new TableItem(table, SWT.NONE);
            item.setData(null);
            item.setText(0, "--");
            item.setText(1, "No target available");
            item.setText(2, "--");
            item.setText(3, "--");
        }
    }
    private void setupTooltip(final Table table) {
        if (table == null || table.isDisposed()) {
            return;
        }
        final Listener listener = new Listener() {
            public void handleEvent(Event event) {
                switch(event.type) {
                case SWT.KeyDown:
                case SWT.MouseExit:
                case SWT.MouseDown:
                    return;
                case SWT.MouseHover:
                    updateDescription(table.getItem(new Point(event.x, event.y)));
                    break;
                case SWT.Selection:
                    if (event.item instanceof TableItem) {
                        updateDescription((TableItem) event.item);
                    }
                    break;
                default:
                    return;
                }
            }
        };
        table.addListener(SWT.Dispose, listener);
        table.addListener(SWT.KeyDown, listener);
        table.addListener(SWT.MouseMove, listener);
        table.addListener(SWT.MouseHover, listener);
    }
    private void updateDescription(TableItem item) {
        if (item != null) {
            Object data = item.getData();
            if (data instanceof IAndroidTarget) {
                String newTooltip = ((IAndroidTarget) data).getDescription();
                mDescription.setText(newTooltip == null ? "" : newTooltip);  
            }
        }
    }
    public void setEnabled(boolean enabled) {
        if (mInnerGroup != null && mTable != null && !mTable.isDisposed()) {
            enableControl(mInnerGroup, enabled);
        }
    }
    private void enableControl(Control c, boolean enabled) {
        c.setEnabled(enabled);
        if (c instanceof Composite)
        for (Control c2 : ((Composite) c).getChildren()) {
            enableControl(c2, enabled);
        }
    }
}
