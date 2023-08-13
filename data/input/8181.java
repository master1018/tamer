public class JTable extends JComponent implements TableModelListener, Scrollable,
    TableColumnModelListener, ListSelectionListener, CellEditorListener,
    Accessible, RowSorterListener
{
    private static final String uiClassID = "TableUI";
    public static final int     AUTO_RESIZE_OFF = 0;
    public static final int     AUTO_RESIZE_NEXT_COLUMN = 1;
    public static final int     AUTO_RESIZE_SUBSEQUENT_COLUMNS = 2;
    public static final int     AUTO_RESIZE_LAST_COLUMN = 3;
    public static final int     AUTO_RESIZE_ALL_COLUMNS = 4;
    public enum PrintMode {
        NORMAL,
        FIT_WIDTH
    }
    protected TableModel        dataModel;
    protected TableColumnModel  columnModel;
    protected ListSelectionModel selectionModel;
    protected JTableHeader      tableHeader;
    protected int               rowHeight;
    protected int               rowMargin;
    protected Color             gridColor;
    protected boolean           showHorizontalLines;
    protected boolean           showVerticalLines;
    protected int               autoResizeMode;
    protected boolean           autoCreateColumnsFromModel;
    protected Dimension         preferredViewportSize;
    protected boolean           rowSelectionAllowed;
    protected boolean           cellSelectionEnabled;
    transient protected Component       editorComp;
    transient protected TableCellEditor cellEditor;
    transient protected int             editingColumn;
    transient protected int             editingRow;
    transient protected Hashtable defaultRenderersByColumnClass;
    transient protected Hashtable defaultEditorsByColumnClass;
    protected Color selectionForeground;
    protected Color selectionBackground;
    private SizeSequence rowModel;
    private boolean dragEnabled;
    private boolean surrendersFocusOnKeystroke;
    private PropertyChangeListener editorRemover = null;
    private boolean columnSelectionAdjusting;
    private boolean rowSelectionAdjusting;
    private Throwable printError;
    private boolean isRowHeightSet;
    private boolean updateSelectionOnSort;
    private transient SortManager sortManager;
    private boolean ignoreSortChange;
    private boolean sorterChanged;
    private boolean autoCreateRowSorter;
    private boolean fillsViewportHeight;
    private DropMode dropMode = DropMode.USE_SELECTION;
    private transient DropLocation dropLocation;
    public static final class DropLocation extends TransferHandler.DropLocation {
        private final int row;
        private final int col;
        private final boolean isInsertRow;
        private final boolean isInsertCol;
        private DropLocation(Point p, int row, int col,
                             boolean isInsertRow, boolean isInsertCol) {
            super(p);
            this.row = row;
            this.col = col;
            this.isInsertRow = isInsertRow;
            this.isInsertCol = isInsertCol;
        }
        public int getRow() {
            return row;
        }
        public int getColumn() {
            return col;
        }
        public boolean isInsertRow() {
            return isInsertRow;
        }
        public boolean isInsertColumn() {
            return isInsertCol;
        }
        public String toString() {
            return getClass().getName()
                   + "[dropPoint=" + getDropPoint() + ","
                   + "row=" + row + ","
                   + "column=" + col + ","
                   + "insertRow=" + isInsertRow + ","
                   + "insertColumn=" + isInsertCol + "]";
        }
    }
    public JTable() {
        this(null, null, null);
    }
    public JTable(TableModel dm) {
        this(dm, null, null);
    }
    public JTable(TableModel dm, TableColumnModel cm) {
        this(dm, cm, null);
    }
    public JTable(TableModel dm, TableColumnModel cm, ListSelectionModel sm) {
        super();
        setLayout(null);
        setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,
                           JComponent.getManagingFocusForwardTraversalKeys());
        setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS,
                           JComponent.getManagingFocusBackwardTraversalKeys());
        if (cm == null) {
            cm = createDefaultColumnModel();
            autoCreateColumnsFromModel = true;
        }
        setColumnModel(cm);
        if (sm == null) {
            sm = createDefaultSelectionModel();
        }
        setSelectionModel(sm);
        if (dm == null) {
            dm = createDefaultDataModel();
        }
        setModel(dm);
        initializeLocalVars();
        updateUI();
    }
    public JTable(int numRows, int numColumns) {
        this(new DefaultTableModel(numRows, numColumns));
    }
    public JTable(Vector rowData, Vector columnNames) {
        this(new DefaultTableModel(rowData, columnNames));
    }
    public JTable(final Object[][] rowData, final Object[] columnNames) {
        this(new AbstractTableModel() {
            public String getColumnName(int column) { return columnNames[column].toString(); }
            public int getRowCount() { return rowData.length; }
            public int getColumnCount() { return columnNames.length; }
            public Object getValueAt(int row, int col) { return rowData[row][col]; }
            public boolean isCellEditable(int row, int column) { return true; }
            public void setValueAt(Object value, int row, int col) {
                rowData[row][col] = value;
                fireTableCellUpdated(row, col);
            }
        });
    }
    public void addNotify() {
        super.addNotify();
        configureEnclosingScrollPane();
    }
    protected void configureEnclosingScrollPane() {
        Container parent = SwingUtilities.getUnwrappedParent(this);
        if (parent instanceof JViewport) {
            JViewport port = (JViewport) parent;
            Container gp = port.getParent();
            if (gp instanceof JScrollPane) {
                JScrollPane scrollPane = (JScrollPane)gp;
                JViewport viewport = scrollPane.getViewport();
                if (viewport == null ||
                        SwingUtilities.getUnwrappedView(viewport) != this) {
                    return;
                }
                scrollPane.setColumnHeaderView(getTableHeader());
                configureEnclosingScrollPaneUI();
            }
        }
    }
    private void configureEnclosingScrollPaneUI() {
        Container parent = SwingUtilities.getUnwrappedParent(this);
        if (parent instanceof JViewport) {
            JViewport port = (JViewport) parent;
            Container gp = port.getParent();
            if (gp instanceof JScrollPane) {
                JScrollPane scrollPane = (JScrollPane)gp;
                JViewport viewport = scrollPane.getViewport();
                if (viewport == null ||
                        SwingUtilities.getUnwrappedView(viewport) != this) {
                    return;
                }
                Border border = scrollPane.getBorder();
                if (border == null || border instanceof UIResource) {
                    Border scrollPaneBorder =
                        UIManager.getBorder("Table.scrollPaneBorder");
                    if (scrollPaneBorder != null) {
                        scrollPane.setBorder(scrollPaneBorder);
                    }
                }
                Component corner =
                        scrollPane.getCorner(JScrollPane.UPPER_TRAILING_CORNER);
                if (corner == null || corner instanceof UIResource){
                    corner = null;
                    Object componentClass = UIManager.get(
                            "Table.scrollPaneCornerComponent");
                    if (componentClass instanceof Class){
                        try {
                            corner = (Component)
                                    ((Class)componentClass).newInstance();
                        } catch (Exception e) {
                        }
                    }
                    scrollPane.setCorner(JScrollPane.UPPER_TRAILING_CORNER,
                            corner);
                }
            }
        }
    }
    public void removeNotify() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().
            removePropertyChangeListener("permanentFocusOwner", editorRemover);
        editorRemover = null;
        unconfigureEnclosingScrollPane();
        super.removeNotify();
    }
    protected void unconfigureEnclosingScrollPane() {
        Container parent = SwingUtilities.getUnwrappedParent(this);
        if (parent instanceof JViewport) {
            JViewport port = (JViewport) parent;
            Container gp = port.getParent();
            if (gp instanceof JScrollPane) {
                JScrollPane scrollPane = (JScrollPane)gp;
                JViewport viewport = scrollPane.getViewport();
                if (viewport == null ||
                        SwingUtilities.getUnwrappedView(viewport) != this) {
                    return;
                }
                scrollPane.setColumnHeaderView(null);
                Component corner =
                        scrollPane.getCorner(JScrollPane.UPPER_TRAILING_CORNER);
                if (corner instanceof UIResource){
                    scrollPane.setCorner(JScrollPane.UPPER_TRAILING_CORNER,
                            null);
                }
            }
        }
    }
    void setUIProperty(String propertyName, Object value) {
        if (propertyName == "rowHeight") {
            if (!isRowHeightSet) {
                setRowHeight(((Number)value).intValue());
                isRowHeightSet = false;
            }
            return;
        }
        super.setUIProperty(propertyName, value);
    }
    @Deprecated
    static public JScrollPane createScrollPaneForTable(JTable aTable) {
        return new JScrollPane(aTable);
    }
    public void setTableHeader(JTableHeader tableHeader) {
        if (this.tableHeader != tableHeader) {
            JTableHeader old = this.tableHeader;
            if (old != null) {
                old.setTable(null);
            }
            this.tableHeader = tableHeader;
            if (tableHeader != null) {
                tableHeader.setTable(this);
            }
            firePropertyChange("tableHeader", old, tableHeader);
        }
    }
    public JTableHeader getTableHeader() {
        return tableHeader;
    }
    public void setRowHeight(int rowHeight) {
        if (rowHeight <= 0) {
            throw new IllegalArgumentException("New row height less than 1");
        }
        int old = this.rowHeight;
        this.rowHeight = rowHeight;
        rowModel = null;
        if (sortManager != null) {
            sortManager.modelRowSizes = null;
        }
        isRowHeightSet = true;
        resizeAndRepaint();
        firePropertyChange("rowHeight", old, rowHeight);
    }
    public int getRowHeight() {
        return rowHeight;
    }
    private SizeSequence getRowModel() {
        if (rowModel == null) {
            rowModel = new SizeSequence(getRowCount(), getRowHeight());
        }
        return rowModel;
    }
    public void setRowHeight(int row, int rowHeight) {
        if (rowHeight <= 0) {
            throw new IllegalArgumentException("New row height less than 1");
        }
        getRowModel().setSize(row, rowHeight);
        if (sortManager != null) {
            sortManager.setViewRowHeight(row, rowHeight);
        }
        resizeAndRepaint();
    }
    public int getRowHeight(int row) {
        return (rowModel == null) ? getRowHeight() : rowModel.getSize(row);
    }
    public void setRowMargin(int rowMargin) {
        int old = this.rowMargin;
        this.rowMargin = rowMargin;
        resizeAndRepaint();
        firePropertyChange("rowMargin", old, rowMargin);
    }
    public int getRowMargin() {
        return rowMargin;
    }
    public void setIntercellSpacing(Dimension intercellSpacing) {
        setRowMargin(intercellSpacing.height);
        getColumnModel().setColumnMargin(intercellSpacing.width);
        resizeAndRepaint();
    }
    public Dimension getIntercellSpacing() {
        return new Dimension(getColumnModel().getColumnMargin(), rowMargin);
    }
    public void setGridColor(Color gridColor) {
        if (gridColor == null) {
            throw new IllegalArgumentException("New color is null");
        }
        Color old = this.gridColor;
        this.gridColor = gridColor;
        firePropertyChange("gridColor", old, gridColor);
        repaint();
    }
    public Color getGridColor() {
        return gridColor;
    }
    public void setShowGrid(boolean showGrid) {
        setShowHorizontalLines(showGrid);
        setShowVerticalLines(showGrid);
        repaint();
    }
    public void setShowHorizontalLines(boolean showHorizontalLines) {
        boolean old = this.showHorizontalLines;
        this.showHorizontalLines = showHorizontalLines;
        firePropertyChange("showHorizontalLines", old, showHorizontalLines);
        repaint();
    }
    public void setShowVerticalLines(boolean showVerticalLines) {
        boolean old = this.showVerticalLines;
        this.showVerticalLines = showVerticalLines;
        firePropertyChange("showVerticalLines", old, showVerticalLines);
        repaint();
    }
    public boolean getShowHorizontalLines() {
        return showHorizontalLines;
    }
    public boolean getShowVerticalLines() {
        return showVerticalLines;
    }
    public void setAutoResizeMode(int mode) {
        if ((mode == AUTO_RESIZE_OFF) ||
            (mode == AUTO_RESIZE_NEXT_COLUMN) ||
            (mode == AUTO_RESIZE_SUBSEQUENT_COLUMNS) ||
            (mode == AUTO_RESIZE_LAST_COLUMN) ||
            (mode == AUTO_RESIZE_ALL_COLUMNS)) {
            int old = autoResizeMode;
            autoResizeMode = mode;
            resizeAndRepaint();
            if (tableHeader != null) {
                tableHeader.resizeAndRepaint();
            }
            firePropertyChange("autoResizeMode", old, autoResizeMode);
        }
    }
    public int getAutoResizeMode() {
        return autoResizeMode;
    }
    public void setAutoCreateColumnsFromModel(boolean autoCreateColumnsFromModel) {
        if (this.autoCreateColumnsFromModel != autoCreateColumnsFromModel) {
            boolean old = this.autoCreateColumnsFromModel;
            this.autoCreateColumnsFromModel = autoCreateColumnsFromModel;
            if (autoCreateColumnsFromModel) {
                createDefaultColumnsFromModel();
            }
            firePropertyChange("autoCreateColumnsFromModel", old, autoCreateColumnsFromModel);
        }
    }
    public boolean getAutoCreateColumnsFromModel() {
        return autoCreateColumnsFromModel;
    }
    public void createDefaultColumnsFromModel() {
        TableModel m = getModel();
        if (m != null) {
            TableColumnModel cm = getColumnModel();
            while (cm.getColumnCount() > 0) {
                cm.removeColumn(cm.getColumn(0));
            }
            for (int i = 0; i < m.getColumnCount(); i++) {
                TableColumn newColumn = new TableColumn(i);
                addColumn(newColumn);
            }
        }
    }
    public void setDefaultRenderer(Class<?> columnClass, TableCellRenderer renderer) {
        if (renderer != null) {
            defaultRenderersByColumnClass.put(columnClass, renderer);
        }
        else {
            defaultRenderersByColumnClass.remove(columnClass);
        }
    }
    public TableCellRenderer getDefaultRenderer(Class<?> columnClass) {
        if (columnClass == null) {
            return null;
        }
        else {
            Object renderer = defaultRenderersByColumnClass.get(columnClass);
            if (renderer != null) {
                return (TableCellRenderer)renderer;
            }
            else {
                Class c = columnClass.getSuperclass();
                if (c == null && columnClass != Object.class) {
                    c = Object.class;
                }
                return getDefaultRenderer(c);
            }
        }
    }
    public void setDefaultEditor(Class<?> columnClass, TableCellEditor editor) {
        if (editor != null) {
            defaultEditorsByColumnClass.put(columnClass, editor);
        }
        else {
            defaultEditorsByColumnClass.remove(columnClass);
        }
    }
    public TableCellEditor getDefaultEditor(Class<?> columnClass) {
        if (columnClass == null) {
            return null;
        }
        else {
            Object editor = defaultEditorsByColumnClass.get(columnClass);
            if (editor != null) {
                return (TableCellEditor)editor;
            }
            else {
                return getDefaultEditor(columnClass.getSuperclass());
            }
        }
    }
    public void setDragEnabled(boolean b) {
        if (b && GraphicsEnvironment.isHeadless()) {
            throw new HeadlessException();
        }
        dragEnabled = b;
    }
    public boolean getDragEnabled() {
        return dragEnabled;
    }
    public final void setDropMode(DropMode dropMode) {
        if (dropMode != null) {
            switch (dropMode) {
                case USE_SELECTION:
                case ON:
                case INSERT:
                case INSERT_ROWS:
                case INSERT_COLS:
                case ON_OR_INSERT:
                case ON_OR_INSERT_ROWS:
                case ON_OR_INSERT_COLS:
                    this.dropMode = dropMode;
                    return;
            }
        }
        throw new IllegalArgumentException(dropMode + ": Unsupported drop mode for table");
    }
    public final DropMode getDropMode() {
        return dropMode;
    }
    DropLocation dropLocationForPoint(Point p) {
        DropLocation location = null;
        int row = rowAtPoint(p);
        int col = columnAtPoint(p);
        boolean outside = Boolean.TRUE == getClientProperty("Table.isFileList")
                          && SwingUtilities2.pointOutsidePrefSize(this, row, col, p);
        Rectangle rect = getCellRect(row, col, true);
        Section xSection, ySection;
        boolean between = false;
        boolean ltr = getComponentOrientation().isLeftToRight();
        switch(dropMode) {
            case USE_SELECTION:
            case ON:
                if (row == -1 || col == -1 || outside) {
                    location = new DropLocation(p, -1, -1, false, false);
                } else {
                    location = new DropLocation(p, row, col, false, false);
                }
                break;
            case INSERT:
                if (row == -1 && col == -1) {
                    location = new DropLocation(p, 0, 0, true, true);
                    break;
                }
                xSection = SwingUtilities2.liesInHorizontal(rect, p, ltr, true);
                if (row == -1) {
                    if (xSection == LEADING) {
                        location = new DropLocation(p, getRowCount(), col, true, true);
                    } else if (xSection == TRAILING) {
                        location = new DropLocation(p, getRowCount(), col + 1, true, true);
                    } else {
                        location = new DropLocation(p, getRowCount(), col, true, false);
                    }
                } else if (xSection == LEADING || xSection == TRAILING) {
                    ySection = SwingUtilities2.liesInVertical(rect, p, true);
                    if (ySection == LEADING) {
                        between = true;
                    } else if (ySection == TRAILING) {
                        row++;
                        between = true;
                    }
                    location = new DropLocation(p, row,
                                                xSection == TRAILING ? col + 1 : col,
                                                between, true);
                } else {
                    if (SwingUtilities2.liesInVertical(rect, p, false) == TRAILING) {
                        row++;
                    }
                    location = new DropLocation(p, row, col, true, false);
                }
                break;
            case INSERT_ROWS:
                if (row == -1 && col == -1) {
                    location = new DropLocation(p, -1, -1, false, false);
                    break;
                }
                if (row == -1) {
                    location = new DropLocation(p, getRowCount(), col, true, false);
                    break;
                }
                if (SwingUtilities2.liesInVertical(rect, p, false) == TRAILING) {
                    row++;
                }
                location = new DropLocation(p, row, col, true, false);
                break;
            case ON_OR_INSERT_ROWS:
                if (row == -1 && col == -1) {
                    location = new DropLocation(p, -1, -1, false, false);
                    break;
                }
                if (row == -1) {
                    location = new DropLocation(p, getRowCount(), col, true, false);
                    break;
                }
                ySection = SwingUtilities2.liesInVertical(rect, p, true);
                if (ySection == LEADING) {
                    between = true;
                } else if (ySection == TRAILING) {
                    row++;
                    between = true;
                }
                location = new DropLocation(p, row, col, between, false);
                break;
            case INSERT_COLS:
                if (row == -1) {
                    location = new DropLocation(p, -1, -1, false, false);
                    break;
                }
                if (col == -1) {
                    location = new DropLocation(p, getColumnCount(), col, false, true);
                    break;
                }
                if (SwingUtilities2.liesInHorizontal(rect, p, ltr, false) == TRAILING) {
                    col++;
                }
                location = new DropLocation(p, row, col, false, true);
                break;
            case ON_OR_INSERT_COLS:
                if (row == -1) {
                    location = new DropLocation(p, -1, -1, false, false);
                    break;
                }
                if (col == -1) {
                    location = new DropLocation(p, row, getColumnCount(), false, true);
                    break;
                }
                xSection = SwingUtilities2.liesInHorizontal(rect, p, ltr, true);
                if (xSection == LEADING) {
                    between = true;
                } else if (xSection == TRAILING) {
                    col++;
                    between = true;
                }
                location = new DropLocation(p, row, col, false, between);
                break;
            case ON_OR_INSERT:
                if (row == -1 && col == -1) {
                    location = new DropLocation(p, 0, 0, true, true);
                    break;
                }
                xSection = SwingUtilities2.liesInHorizontal(rect, p, ltr, true);
                if (row == -1) {
                    if (xSection == LEADING) {
                        location = new DropLocation(p, getRowCount(), col, true, true);
                    } else if (xSection == TRAILING) {
                        location = new DropLocation(p, getRowCount(), col + 1, true, true);
                    } else {
                        location = new DropLocation(p, getRowCount(), col, true, false);
                    }
                    break;
                }
                ySection = SwingUtilities2.liesInVertical(rect, p, true);
                if (ySection == LEADING) {
                    between = true;
                } else if (ySection == TRAILING) {
                    row++;
                    between = true;
                }
                location = new DropLocation(p, row,
                                            xSection == TRAILING ? col + 1 : col,
                                            between,
                                            xSection != MIDDLE);
                break;
            default:
                assert false : "Unexpected drop mode";
        }
        return location;
    }
    Object setDropLocation(TransferHandler.DropLocation location,
                           Object state,
                           boolean forDrop) {
        Object retVal = null;
        DropLocation tableLocation = (DropLocation)location;
        if (dropMode == DropMode.USE_SELECTION) {
            if (tableLocation == null) {
                if (!forDrop && state != null) {
                    clearSelection();
                    int[] rows = ((int[][])state)[0];
                    int[] cols = ((int[][])state)[1];
                    int[] anchleads = ((int[][])state)[2];
                    for (int row : rows) {
                        addRowSelectionInterval(row, row);
                    }
                    for (int col : cols) {
                        addColumnSelectionInterval(col, col);
                    }
                    SwingUtilities2.setLeadAnchorWithoutSelection(
                            getSelectionModel(), anchleads[1], anchleads[0]);
                    SwingUtilities2.setLeadAnchorWithoutSelection(
                            getColumnModel().getSelectionModel(),
                            anchleads[3], anchleads[2]);
                }
            } else {
                if (dropLocation == null) {
                    retVal = new int[][]{
                        getSelectedRows(),
                        getSelectedColumns(),
                        {getAdjustedIndex(getSelectionModel()
                             .getAnchorSelectionIndex(), true),
                         getAdjustedIndex(getSelectionModel()
                             .getLeadSelectionIndex(), true),
                         getAdjustedIndex(getColumnModel().getSelectionModel()
                             .getAnchorSelectionIndex(), false),
                         getAdjustedIndex(getColumnModel().getSelectionModel()
                             .getLeadSelectionIndex(), false)}};
                } else {
                    retVal = state;
                }
                if (tableLocation.getRow() == -1) {
                    clearSelectionAndLeadAnchor();
                } else {
                    setRowSelectionInterval(tableLocation.getRow(),
                                            tableLocation.getRow());
                    setColumnSelectionInterval(tableLocation.getColumn(),
                                               tableLocation.getColumn());
                }
            }
        }
        DropLocation old = dropLocation;
        dropLocation = tableLocation;
        firePropertyChange("dropLocation", old, dropLocation);
        return retVal;
    }
    public final DropLocation getDropLocation() {
        return dropLocation;
    }
    public void setAutoCreateRowSorter(boolean autoCreateRowSorter) {
        boolean oldValue = this.autoCreateRowSorter;
        this.autoCreateRowSorter = autoCreateRowSorter;
        if (autoCreateRowSorter) {
            setRowSorter(new TableRowSorter<TableModel>(getModel()));
        }
        firePropertyChange("autoCreateRowSorter", oldValue,
                           autoCreateRowSorter);
    }
    public boolean getAutoCreateRowSorter() {
        return autoCreateRowSorter;
    }
    public void setUpdateSelectionOnSort(boolean update) {
        if (updateSelectionOnSort != update) {
            updateSelectionOnSort = update;
            firePropertyChange("updateSelectionOnSort", !update, update);
        }
    }
    public boolean getUpdateSelectionOnSort() {
        return updateSelectionOnSort;
    }
    public void setRowSorter(RowSorter<? extends TableModel> sorter) {
        RowSorter<? extends TableModel> oldRowSorter = null;
        if (sortManager != null) {
            oldRowSorter = sortManager.sorter;
            sortManager.dispose();
            sortManager = null;
        }
        rowModel = null;
        clearSelectionAndLeadAnchor();
        if (sorter != null) {
            sortManager = new SortManager(sorter);
        }
        resizeAndRepaint();
        firePropertyChange("rowSorter", oldRowSorter, sorter);
        firePropertyChange("sorter", oldRowSorter, sorter);
    }
    public RowSorter<? extends TableModel> getRowSorter() {
        return (sortManager != null) ? sortManager.sorter : null;
    }
    public void setSelectionMode(int selectionMode) {
        clearSelection();
        getSelectionModel().setSelectionMode(selectionMode);
        getColumnModel().getSelectionModel().setSelectionMode(selectionMode);
    }
    public void setRowSelectionAllowed(boolean rowSelectionAllowed) {
        boolean old = this.rowSelectionAllowed;
        this.rowSelectionAllowed = rowSelectionAllowed;
        if (old != rowSelectionAllowed) {
            repaint();
        }
        firePropertyChange("rowSelectionAllowed", old, rowSelectionAllowed);
    }
    public boolean getRowSelectionAllowed() {
        return rowSelectionAllowed;
    }
    public void setColumnSelectionAllowed(boolean columnSelectionAllowed) {
        boolean old = columnModel.getColumnSelectionAllowed();
        columnModel.setColumnSelectionAllowed(columnSelectionAllowed);
        if (old != columnSelectionAllowed) {
            repaint();
        }
        firePropertyChange("columnSelectionAllowed", old, columnSelectionAllowed);
    }
    public boolean getColumnSelectionAllowed() {
        return columnModel.getColumnSelectionAllowed();
    }
    public void setCellSelectionEnabled(boolean cellSelectionEnabled) {
        setRowSelectionAllowed(cellSelectionEnabled);
        setColumnSelectionAllowed(cellSelectionEnabled);
        boolean old = this.cellSelectionEnabled;
        this.cellSelectionEnabled = cellSelectionEnabled;
        firePropertyChange("cellSelectionEnabled", old, cellSelectionEnabled);
    }
    public boolean getCellSelectionEnabled() {
        return getRowSelectionAllowed() && getColumnSelectionAllowed();
    }
    public void selectAll() {
        if (isEditing()) {
            removeEditor();
        }
        if (getRowCount() > 0 && getColumnCount() > 0) {
            int oldLead;
            int oldAnchor;
            ListSelectionModel selModel;
            selModel = selectionModel;
            selModel.setValueIsAdjusting(true);
            oldLead = getAdjustedIndex(selModel.getLeadSelectionIndex(), true);
            oldAnchor = getAdjustedIndex(selModel.getAnchorSelectionIndex(), true);
            setRowSelectionInterval(0, getRowCount()-1);
            SwingUtilities2.setLeadAnchorWithoutSelection(selModel, oldLead, oldAnchor);
            selModel.setValueIsAdjusting(false);
            selModel = columnModel.getSelectionModel();
            selModel.setValueIsAdjusting(true);
            oldLead = getAdjustedIndex(selModel.getLeadSelectionIndex(), false);
            oldAnchor = getAdjustedIndex(selModel.getAnchorSelectionIndex(), false);
            setColumnSelectionInterval(0, getColumnCount()-1);
            SwingUtilities2.setLeadAnchorWithoutSelection(selModel, oldLead, oldAnchor);
            selModel.setValueIsAdjusting(false);
        }
    }
    public void clearSelection() {
        selectionModel.clearSelection();
        columnModel.getSelectionModel().clearSelection();
    }
    private void clearSelectionAndLeadAnchor() {
        selectionModel.setValueIsAdjusting(true);
        columnModel.getSelectionModel().setValueIsAdjusting(true);
        clearSelection();
        selectionModel.setAnchorSelectionIndex(-1);
        selectionModel.setLeadSelectionIndex(-1);
        columnModel.getSelectionModel().setAnchorSelectionIndex(-1);
        columnModel.getSelectionModel().setLeadSelectionIndex(-1);
        selectionModel.setValueIsAdjusting(false);
        columnModel.getSelectionModel().setValueIsAdjusting(false);
    }
    private int getAdjustedIndex(int index, boolean row) {
        int compare = row ? getRowCount() : getColumnCount();
        return index < compare ? index : -1;
    }
    private int boundRow(int row) throws IllegalArgumentException {
        if (row < 0 || row >= getRowCount()) {
            throw new IllegalArgumentException("Row index out of range");
        }
        return row;
    }
    private int boundColumn(int col) {
        if (col< 0 || col >= getColumnCount()) {
            throw new IllegalArgumentException("Column index out of range");
        }
        return col;
    }
    public void setRowSelectionInterval(int index0, int index1) {
        selectionModel.setSelectionInterval(boundRow(index0), boundRow(index1));
    }
    public void setColumnSelectionInterval(int index0, int index1) {
        columnModel.getSelectionModel().setSelectionInterval(boundColumn(index0), boundColumn(index1));
    }
    public void addRowSelectionInterval(int index0, int index1) {
        selectionModel.addSelectionInterval(boundRow(index0), boundRow(index1));
    }
    public void addColumnSelectionInterval(int index0, int index1) {
        columnModel.getSelectionModel().addSelectionInterval(boundColumn(index0), boundColumn(index1));
    }
    public void removeRowSelectionInterval(int index0, int index1) {
        selectionModel.removeSelectionInterval(boundRow(index0), boundRow(index1));
    }
    public void removeColumnSelectionInterval(int index0, int index1) {
        columnModel.getSelectionModel().removeSelectionInterval(boundColumn(index0), boundColumn(index1));
    }
    public int getSelectedRow() {
        return selectionModel.getMinSelectionIndex();
    }
    public int getSelectedColumn() {
        return columnModel.getSelectionModel().getMinSelectionIndex();
    }
    public int[] getSelectedRows() {
        int iMin = selectionModel.getMinSelectionIndex();
        int iMax = selectionModel.getMaxSelectionIndex();
        if ((iMin == -1) || (iMax == -1)) {
            return new int[0];
        }
        int[] rvTmp = new int[1+ (iMax - iMin)];
        int n = 0;
        for(int i = iMin; i <= iMax; i++) {
            if (selectionModel.isSelectedIndex(i)) {
                rvTmp[n++] = i;
            }
        }
        int[] rv = new int[n];
        System.arraycopy(rvTmp, 0, rv, 0, n);
        return rv;
    }
    public int[] getSelectedColumns() {
        return columnModel.getSelectedColumns();
    }
    public int getSelectedRowCount() {
        int iMin = selectionModel.getMinSelectionIndex();
        int iMax = selectionModel.getMaxSelectionIndex();
        int count = 0;
        for(int i = iMin; i <= iMax; i++) {
            if (selectionModel.isSelectedIndex(i)) {
                count++;
            }
        }
        return count;
    }
    public int getSelectedColumnCount() {
        return columnModel.getSelectedColumnCount();
    }
    public boolean isRowSelected(int row) {
        return selectionModel.isSelectedIndex(row);
    }
    public boolean isColumnSelected(int column) {
        return columnModel.getSelectionModel().isSelectedIndex(column);
    }
    public boolean isCellSelected(int row, int column) {
        if (!getRowSelectionAllowed() && !getColumnSelectionAllowed()) {
            return false;
        }
        return (!getRowSelectionAllowed() || isRowSelected(row)) &&
               (!getColumnSelectionAllowed() || isColumnSelected(column));
    }
    private void changeSelectionModel(ListSelectionModel sm, int index,
                                      boolean toggle, boolean extend, boolean selected,
                                      int anchor, boolean anchorSelected) {
        if (extend) {
            if (toggle) {
                if (anchorSelected) {
                    sm.addSelectionInterval(anchor, index);
                } else {
                    sm.removeSelectionInterval(anchor, index);
                    if (Boolean.TRUE == getClientProperty("Table.isFileList")) {
                        sm.addSelectionInterval(index, index);
                        sm.setAnchorSelectionIndex(anchor);
                    }
                }
            }
            else {
                sm.setSelectionInterval(anchor, index);
            }
        }
        else {
            if (toggle) {
                if (selected) {
                    sm.removeSelectionInterval(index, index);
                }
                else {
                    sm.addSelectionInterval(index, index);
                }
            }
            else {
                sm.setSelectionInterval(index, index);
            }
        }
    }
    public void changeSelection(int rowIndex, int columnIndex, boolean toggle, boolean extend) {
        ListSelectionModel rsm = getSelectionModel();
        ListSelectionModel csm = getColumnModel().getSelectionModel();
        int anchorRow = getAdjustedIndex(rsm.getAnchorSelectionIndex(), true);
        int anchorCol = getAdjustedIndex(csm.getAnchorSelectionIndex(), false);
        boolean anchorSelected = true;
        if (anchorRow == -1) {
            if (getRowCount() > 0) {
                anchorRow = 0;
            }
            anchorSelected = false;
        }
        if (anchorCol == -1) {
            if (getColumnCount() > 0) {
                anchorCol = 0;
            }
            anchorSelected = false;
        }
        boolean selected = isCellSelected(rowIndex, columnIndex);
        anchorSelected = anchorSelected && isCellSelected(anchorRow, anchorCol);
        changeSelectionModel(csm, columnIndex, toggle, extend, selected,
                             anchorCol, anchorSelected);
        changeSelectionModel(rsm, rowIndex, toggle, extend, selected,
                             anchorRow, anchorSelected);
        if (getAutoscrolls()) {
            Rectangle cellRect = getCellRect(rowIndex, columnIndex, false);
            if (cellRect != null) {
                scrollRectToVisible(cellRect);
            }
        }
    }
    public Color getSelectionForeground() {
        return selectionForeground;
    }
    public void setSelectionForeground(Color selectionForeground) {
        Color old = this.selectionForeground;
        this.selectionForeground = selectionForeground;
        firePropertyChange("selectionForeground", old, selectionForeground);
        repaint();
    }
    public Color getSelectionBackground() {
        return selectionBackground;
    }
    public void setSelectionBackground(Color selectionBackground) {
        Color old = this.selectionBackground;
        this.selectionBackground = selectionBackground;
        firePropertyChange("selectionBackground", old, selectionBackground);
        repaint();
    }
    public TableColumn getColumn(Object identifier) {
        TableColumnModel cm = getColumnModel();
        int columnIndex = cm.getColumnIndex(identifier);
        return cm.getColumn(columnIndex);
    }
    public int convertColumnIndexToModel(int viewColumnIndex) {
        return SwingUtilities2.convertColumnIndexToModel(
                getColumnModel(), viewColumnIndex);
    }
    public int convertColumnIndexToView(int modelColumnIndex) {
        return SwingUtilities2.convertColumnIndexToView(
                getColumnModel(), modelColumnIndex);
    }
    public int convertRowIndexToView(int modelRowIndex) {
        RowSorter sorter = getRowSorter();
        if (sorter != null) {
            return sorter.convertRowIndexToView(modelRowIndex);
        }
        return modelRowIndex;
    }
    public int convertRowIndexToModel(int viewRowIndex) {
        RowSorter sorter = getRowSorter();
        if (sorter != null) {
            return sorter.convertRowIndexToModel(viewRowIndex);
        }
        return viewRowIndex;
    }
    public int getRowCount() {
        RowSorter sorter = getRowSorter();
        if (sorter != null) {
            return sorter.getViewRowCount();
        }
        return getModel().getRowCount();
    }
    public int getColumnCount() {
        return getColumnModel().getColumnCount();
    }
    public String getColumnName(int column) {
        return getModel().getColumnName(convertColumnIndexToModel(column));
    }
    public Class<?> getColumnClass(int column) {
        return getModel().getColumnClass(convertColumnIndexToModel(column));
    }
    public Object getValueAt(int row, int column) {
        return getModel().getValueAt(convertRowIndexToModel(row),
                                     convertColumnIndexToModel(column));
    }
    public void setValueAt(Object aValue, int row, int column) {
        getModel().setValueAt(aValue, convertRowIndexToModel(row),
                              convertColumnIndexToModel(column));
    }
    public boolean isCellEditable(int row, int column) {
        return getModel().isCellEditable(convertRowIndexToModel(row),
                                         convertColumnIndexToModel(column));
    }
    public void addColumn(TableColumn aColumn) {
        if (aColumn.getHeaderValue() == null) {
            int modelColumn = aColumn.getModelIndex();
            String columnName = getModel().getColumnName(modelColumn);
            aColumn.setHeaderValue(columnName);
        }
        getColumnModel().addColumn(aColumn);
    }
    public void removeColumn(TableColumn aColumn) {
        getColumnModel().removeColumn(aColumn);
    }
    public void moveColumn(int column, int targetColumn) {
        getColumnModel().moveColumn(column, targetColumn);
    }
    public int columnAtPoint(Point point) {
        int x = point.x;
        if( !getComponentOrientation().isLeftToRight() ) {
            x = getWidth() - x - 1;
        }
        return getColumnModel().getColumnIndexAtX(x);
    }
    public int rowAtPoint(Point point) {
        int y = point.y;
        int result = (rowModel == null) ?  y/getRowHeight() : rowModel.getIndex(y);
        if (result < 0) {
            return -1;
        }
        else if (result >= getRowCount()) {
            return -1;
        }
        else {
            return result;
        }
    }
    public Rectangle getCellRect(int row, int column, boolean includeSpacing) {
        Rectangle r = new Rectangle();
        boolean valid = true;
        if (row < 0) {
            valid = false;
        }
        else if (row >= getRowCount()) {
            r.y = getHeight();
            valid = false;
        }
        else {
            r.height = getRowHeight(row);
            r.y = (rowModel == null) ? row * r.height : rowModel.getPosition(row);
        }
        if (column < 0) {
            if( !getComponentOrientation().isLeftToRight() ) {
                r.x = getWidth();
            }
            valid = false;
        }
        else if (column >= getColumnCount()) {
            if( getComponentOrientation().isLeftToRight() ) {
                r.x = getWidth();
            }
            valid = false;
        }
        else {
            TableColumnModel cm = getColumnModel();
            if( getComponentOrientation().isLeftToRight() ) {
                for(int i = 0; i < column; i++) {
                    r.x += cm.getColumn(i).getWidth();
                }
            } else {
                for(int i = cm.getColumnCount()-1; i > column; i--) {
                    r.x += cm.getColumn(i).getWidth();
                }
            }
            r.width = cm.getColumn(column).getWidth();
        }
        if (valid && !includeSpacing) {
            int rm = Math.min(getRowMargin(), r.height);
            int cm = Math.min(getColumnModel().getColumnMargin(), r.width);
            r.setBounds(r.x + cm/2, r.y + rm/2, r.width - cm, r.height - rm);
        }
        return r;
    }
    private int viewIndexForColumn(TableColumn aColumn) {
        TableColumnModel cm = getColumnModel();
        for (int column = 0; column < cm.getColumnCount(); column++) {
            if (cm.getColumn(column) == aColumn) {
                return column;
            }
        }
        return -1;
    }
    public void doLayout() {
        TableColumn resizingColumn = getResizingColumn();
        if (resizingColumn == null) {
            setWidthsFromPreferredWidths(false);
        }
        else {
            int columnIndex = viewIndexForColumn(resizingColumn);
            int delta = getWidth() - getColumnModel().getTotalColumnWidth();
            accommodateDelta(columnIndex, delta);
            delta = getWidth() - getColumnModel().getTotalColumnWidth();
            if (delta != 0) {
                resizingColumn.setWidth(resizingColumn.getWidth() + delta);
            }
            setWidthsFromPreferredWidths(true);
        }
        super.doLayout();
    }
    private TableColumn getResizingColumn() {
        return (tableHeader == null) ? null
                                     : tableHeader.getResizingColumn();
    }
    @Deprecated
    public void sizeColumnsToFit(boolean lastColumnOnly) {
        int oldAutoResizeMode = autoResizeMode;
        setAutoResizeMode(lastColumnOnly ? AUTO_RESIZE_LAST_COLUMN
                                         : AUTO_RESIZE_ALL_COLUMNS);
        sizeColumnsToFit(-1);
        setAutoResizeMode(oldAutoResizeMode);
    }
    public void sizeColumnsToFit(int resizingColumn) {
        if (resizingColumn == -1) {
            setWidthsFromPreferredWidths(false);
        }
        else {
            if (autoResizeMode == AUTO_RESIZE_OFF) {
                TableColumn aColumn = getColumnModel().getColumn(resizingColumn);
                aColumn.setPreferredWidth(aColumn.getWidth());
            }
            else {
                int delta = getWidth() - getColumnModel().getTotalColumnWidth();
                accommodateDelta(resizingColumn, delta);
                setWidthsFromPreferredWidths(true);
            }
        }
    }
    private void setWidthsFromPreferredWidths(final boolean inverse) {
        int totalWidth     = getWidth();
        int totalPreferred = getPreferredSize().width;
        int target = !inverse ? totalWidth : totalPreferred;
        final TableColumnModel cm = columnModel;
        Resizable3 r = new Resizable3() {
            public int  getElementCount()      { return cm.getColumnCount(); }
            public int  getLowerBoundAt(int i) { return cm.getColumn(i).getMinWidth(); }
            public int  getUpperBoundAt(int i) { return cm.getColumn(i).getMaxWidth(); }
            public int  getMidPointAt(int i)  {
                if (!inverse) {
                    return cm.getColumn(i).getPreferredWidth();
                }
                else {
                    return cm.getColumn(i).getWidth();
                }
            }
            public void setSizeAt(int s, int i) {
                if (!inverse) {
                    cm.getColumn(i).setWidth(s);
                }
                else {
                    cm.getColumn(i).setPreferredWidth(s);
                }
            }
        };
        adjustSizes(target, r, inverse);
    }
    private void accommodateDelta(int resizingColumnIndex, int delta) {
        int columnCount = getColumnCount();
        int from = resizingColumnIndex;
        int to;
        switch(autoResizeMode) {
            case AUTO_RESIZE_NEXT_COLUMN:
                from = from + 1;
                to = Math.min(from + 1, columnCount); break;
            case AUTO_RESIZE_SUBSEQUENT_COLUMNS:
                from = from + 1;
                to = columnCount; break;
            case AUTO_RESIZE_LAST_COLUMN:
                from = columnCount - 1;
                to = from + 1; break;
            case AUTO_RESIZE_ALL_COLUMNS:
                from = 0;
                to = columnCount; break;
            default:
                return;
        }
        final int start = from;
        final int end = to;
        final TableColumnModel cm = columnModel;
        Resizable3 r = new Resizable3() {
            public int  getElementCount()       { return end-start; }
            public int  getLowerBoundAt(int i)  { return cm.getColumn(i+start).getMinWidth(); }
            public int  getUpperBoundAt(int i)  { return cm.getColumn(i+start).getMaxWidth(); }
            public int  getMidPointAt(int i)    { return cm.getColumn(i+start).getWidth(); }
            public void setSizeAt(int s, int i) {        cm.getColumn(i+start).setWidth(s); }
        };
        int totalWidth = 0;
        for(int i = from; i < to; i++) {
            TableColumn aColumn = columnModel.getColumn(i);
            int input = aColumn.getWidth();
            totalWidth = totalWidth + input;
        }
        adjustSizes(totalWidth + delta, r, false);
    }
    private interface Resizable2 {
        public int  getElementCount();
        public int  getLowerBoundAt(int i);
        public int  getUpperBoundAt(int i);
        public void setSizeAt(int newSize, int i);
    }
    private interface Resizable3 extends Resizable2 {
        public int  getMidPointAt(int i);
    }
    private void adjustSizes(long target, final Resizable3 r, boolean inverse) {
        int N = r.getElementCount();
        long totalPreferred = 0;
        for(int i = 0; i < N; i++) {
            totalPreferred += r.getMidPointAt(i);
        }
        Resizable2 s;
        if ((target < totalPreferred) == !inverse) {
            s = new Resizable2() {
                public int  getElementCount()      { return r.getElementCount(); }
                public int  getLowerBoundAt(int i) { return r.getLowerBoundAt(i); }
                public int  getUpperBoundAt(int i) { return r.getMidPointAt(i); }
                public void setSizeAt(int newSize, int i) { r.setSizeAt(newSize, i); }
            };
        }
        else {
            s = new Resizable2() {
                public int  getElementCount()      { return r.getElementCount(); }
                public int  getLowerBoundAt(int i) { return r.getMidPointAt(i); }
                public int  getUpperBoundAt(int i) { return r.getUpperBoundAt(i); }
                public void setSizeAt(int newSize, int i) { r.setSizeAt(newSize, i); }
            };
        }
        adjustSizes(target, s, !inverse);
    }
    private void adjustSizes(long target, Resizable2 r, boolean limitToRange) {
        long totalLowerBound = 0;
        long totalUpperBound = 0;
        for(int i = 0; i < r.getElementCount(); i++) {
            totalLowerBound += r.getLowerBoundAt(i);
            totalUpperBound += r.getUpperBoundAt(i);
        }
        if (limitToRange) {
            target = Math.min(Math.max(totalLowerBound, target), totalUpperBound);
        }
        for(int i = 0; i < r.getElementCount(); i++) {
            int lowerBound = r.getLowerBoundAt(i);
            int upperBound = r.getUpperBoundAt(i);
            int newSize;
            if (totalLowerBound == totalUpperBound) {
                newSize = lowerBound;
            }
            else {
                double f = (double)(target - totalLowerBound)/(totalUpperBound - totalLowerBound);
                newSize = (int)Math.round(lowerBound+f*(upperBound - lowerBound));
            }
            r.setSizeAt(newSize, i);
            target -= newSize;
            totalLowerBound -= lowerBound;
            totalUpperBound -= upperBound;
        }
    }
    public String getToolTipText(MouseEvent event) {
        String tip = null;
        Point p = event.getPoint();
        int hitColumnIndex = columnAtPoint(p);
        int hitRowIndex = rowAtPoint(p);
        if ((hitColumnIndex != -1) && (hitRowIndex != -1)) {
            TableCellRenderer renderer = getCellRenderer(hitRowIndex, hitColumnIndex);
            Component component = prepareRenderer(renderer, hitRowIndex, hitColumnIndex);
            if (component instanceof JComponent) {
                Rectangle cellRect = getCellRect(hitRowIndex, hitColumnIndex, false);
                p.translate(-cellRect.x, -cellRect.y);
                MouseEvent newEvent = new MouseEvent(component, event.getID(),
                                          event.getWhen(), event.getModifiers(),
                                          p.x, p.y,
                                          event.getXOnScreen(),
                                          event.getYOnScreen(),
                                          event.getClickCount(),
                                          event.isPopupTrigger(),
                                          MouseEvent.NOBUTTON);
                tip = ((JComponent)component).getToolTipText(newEvent);
            }
        }
        if (tip == null)
            tip = getToolTipText();
        return tip;
    }
    public void setSurrendersFocusOnKeystroke(boolean surrendersFocusOnKeystroke) {
        this.surrendersFocusOnKeystroke = surrendersFocusOnKeystroke;
    }
    public boolean getSurrendersFocusOnKeystroke() {
        return surrendersFocusOnKeystroke;
    }
    public boolean editCellAt(int row, int column) {
        return editCellAt(row, column, null);
    }
    public boolean editCellAt(int row, int column, EventObject e){
        if (cellEditor != null && !cellEditor.stopCellEditing()) {
            return false;
        }
        if (row < 0 || row >= getRowCount() ||
            column < 0 || column >= getColumnCount()) {
            return false;
        }
        if (!isCellEditable(row, column))
            return false;
        if (editorRemover == null) {
            KeyboardFocusManager fm =
                KeyboardFocusManager.getCurrentKeyboardFocusManager();
            editorRemover = new CellEditorRemover(fm);
            fm.addPropertyChangeListener("permanentFocusOwner", editorRemover);
        }
        TableCellEditor editor = getCellEditor(row, column);
        if (editor != null && editor.isCellEditable(e)) {
            editorComp = prepareEditor(editor, row, column);
            if (editorComp == null) {
                removeEditor();
                return false;
            }
            editorComp.setBounds(getCellRect(row, column, false));
            add(editorComp);
            editorComp.validate();
            editorComp.repaint();
            setCellEditor(editor);
            setEditingRow(row);
            setEditingColumn(column);
            editor.addCellEditorListener(this);
            return true;
        }
        return false;
    }
    public boolean isEditing() {
        return cellEditor != null;
    }
    public Component getEditorComponent() {
        return editorComp;
    }
    public int getEditingColumn() {
        return editingColumn;
    }
    public int getEditingRow() {
        return editingRow;
    }
    public TableUI getUI() {
        return (TableUI)ui;
    }
    public void setUI(TableUI ui) {
        if (this.ui != ui) {
            super.setUI(ui);
            repaint();
        }
    }
    public void updateUI() {
        TableColumnModel cm = getColumnModel();
        for(int column = 0; column < cm.getColumnCount(); column++) {
            TableColumn aColumn = cm.getColumn(column);
            SwingUtilities.updateRendererOrEditorUI(aColumn.getCellRenderer());
            SwingUtilities.updateRendererOrEditorUI(aColumn.getCellEditor());
            SwingUtilities.updateRendererOrEditorUI(aColumn.getHeaderRenderer());
        }
        Enumeration defaultRenderers = defaultRenderersByColumnClass.elements();
        while (defaultRenderers.hasMoreElements()) {
            SwingUtilities.updateRendererOrEditorUI(defaultRenderers.nextElement());
        }
        Enumeration defaultEditors = defaultEditorsByColumnClass.elements();
        while (defaultEditors.hasMoreElements()) {
            SwingUtilities.updateRendererOrEditorUI(defaultEditors.nextElement());
        }
        if (tableHeader != null && tableHeader.getParent() == null) {
            tableHeader.updateUI();
        }
        configureEnclosingScrollPaneUI();
        setUI((TableUI)UIManager.getUI(this));
    }
    public String getUIClassID() {
        return uiClassID;
    }
    public void setModel(TableModel dataModel) {
        if (dataModel == null) {
            throw new IllegalArgumentException("Cannot set a null TableModel");
        }
        if (this.dataModel != dataModel) {
            TableModel old = this.dataModel;
            if (old != null) {
                old.removeTableModelListener(this);
            }
            this.dataModel = dataModel;
            dataModel.addTableModelListener(this);
            tableChanged(new TableModelEvent(dataModel, TableModelEvent.HEADER_ROW));
            firePropertyChange("model", old, dataModel);
            if (getAutoCreateRowSorter()) {
                setRowSorter(new TableRowSorter<TableModel>(dataModel));
            }
        }
    }
    public TableModel getModel() {
        return dataModel;
    }
    public void setColumnModel(TableColumnModel columnModel) {
        if (columnModel == null) {
            throw new IllegalArgumentException("Cannot set a null ColumnModel");
        }
        TableColumnModel old = this.columnModel;
        if (columnModel != old) {
            if (old != null) {
                old.removeColumnModelListener(this);
            }
            this.columnModel = columnModel;
            columnModel.addColumnModelListener(this);
            if (tableHeader != null) {
                tableHeader.setColumnModel(columnModel);
            }
            firePropertyChange("columnModel", old, columnModel);
            resizeAndRepaint();
        }
    }
    public TableColumnModel getColumnModel() {
        return columnModel;
    }
    public void setSelectionModel(ListSelectionModel newModel) {
        if (newModel == null) {
            throw new IllegalArgumentException("Cannot set a null SelectionModel");
        }
        ListSelectionModel oldModel = selectionModel;
        if (newModel != oldModel) {
            if (oldModel != null) {
                oldModel.removeListSelectionListener(this);
            }
            selectionModel = newModel;
            newModel.addListSelectionListener(this);
            firePropertyChange("selectionModel", oldModel, newModel);
            repaint();
        }
    }
    public ListSelectionModel getSelectionModel() {
        return selectionModel;
    }
    public void sorterChanged(RowSorterEvent e) {
        if (e.getType() == RowSorterEvent.Type.SORT_ORDER_CHANGED) {
            JTableHeader header = getTableHeader();
            if (header != null) {
                header.repaint();
            }
        }
        else if (e.getType() == RowSorterEvent.Type.SORTED) {
            sorterChanged = true;
            if (!ignoreSortChange) {
                sortedTableChanged(e, null);
            }
        }
    }
    private final class SortManager {
        RowSorter<? extends TableModel> sorter;
        private ListSelectionModel modelSelection;
        private int modelLeadIndex;
        private boolean syncingSelection;
        private int[] lastModelSelection;
        private SizeSequence modelRowSizes;
        SortManager(RowSorter<? extends TableModel> sorter) {
            this.sorter = sorter;
            sorter.addRowSorterListener(JTable.this);
        }
        public void dispose() {
            if (sorter != null) {
                sorter.removeRowSorterListener(JTable.this);
            }
        }
        public void setViewRowHeight(int viewIndex, int rowHeight) {
            if (modelRowSizes == null) {
                modelRowSizes = new SizeSequence(getModel().getRowCount(),
                                                 getRowHeight());
            }
            modelRowSizes.setSize(convertRowIndexToModel(viewIndex),rowHeight);
        }
        public void allChanged() {
            modelLeadIndex = -1;
            modelSelection = null;
            modelRowSizes = null;
        }
        public void viewSelectionChanged(ListSelectionEvent e) {
            if (!syncingSelection && modelSelection != null) {
                modelSelection = null;
            }
        }
        public void prepareForChange(RowSorterEvent sortEvent,
                                     ModelChange change) {
            if (getUpdateSelectionOnSort()) {
                cacheSelection(sortEvent, change);
            }
        }
        private void cacheSelection(RowSorterEvent sortEvent,
                                    ModelChange change) {
            if (sortEvent != null) {
                if (modelSelection == null &&
                        sorter.getViewRowCount() != getModel().getRowCount()) {
                    modelSelection = new DefaultListSelectionModel();
                    ListSelectionModel viewSelection = getSelectionModel();
                    int min = viewSelection.getMinSelectionIndex();
                    int max = viewSelection.getMaxSelectionIndex();
                    int modelIndex;
                    for (int viewIndex = min; viewIndex <= max; viewIndex++) {
                        if (viewSelection.isSelectedIndex(viewIndex)) {
                            modelIndex = convertRowIndexToModel(
                                    sortEvent, viewIndex);
                            if (modelIndex != -1) {
                                modelSelection.addSelectionInterval(
                                    modelIndex, modelIndex);
                            }
                        }
                    }
                    modelIndex = convertRowIndexToModel(sortEvent,
                            viewSelection.getLeadSelectionIndex());
                    SwingUtilities2.setLeadAnchorWithoutSelection(
                            modelSelection, modelIndex, modelIndex);
                } else if (modelSelection == null) {
                    cacheModelSelection(sortEvent);
                }
            } else if (change.allRowsChanged) {
                modelSelection = null;
            } else if (modelSelection != null) {
                switch(change.type) {
                case TableModelEvent.DELETE:
                    modelSelection.removeIndexInterval(change.startModelIndex,
                                                       change.endModelIndex);
                    break;
                case TableModelEvent.INSERT:
                    modelSelection.insertIndexInterval(change.startModelIndex,
                                                       change.endModelIndex,
                                                       true);
                    break;
                default:
                    break;
                }
            } else {
                cacheModelSelection(null);
            }
        }
        private void cacheModelSelection(RowSorterEvent sortEvent) {
            lastModelSelection = convertSelectionToModel(sortEvent);
            modelLeadIndex = convertRowIndexToModel(sortEvent,
                        selectionModel.getLeadSelectionIndex());
        }
        public void processChange(RowSorterEvent sortEvent,
                                  ModelChange change,
                                  boolean sorterChanged) {
            if (change != null) {
                if (change.allRowsChanged) {
                    modelRowSizes = null;
                    rowModel = null;
                } else if (modelRowSizes != null) {
                    if (change.type == TableModelEvent.INSERT) {
                        modelRowSizes.insertEntries(change.startModelIndex,
                                                    change.endModelIndex -
                                                    change.startModelIndex + 1,
                                                    getRowHeight());
                    } else if (change.type == TableModelEvent.DELETE) {
                        modelRowSizes.removeEntries(change.startModelIndex,
                                                    change.endModelIndex -
                                                    change.startModelIndex +1 );
                    }
                }
            }
            if (sorterChanged) {
                setViewRowHeightsFromModel();
                restoreSelection(change);
            }
        }
        private void setViewRowHeightsFromModel() {
            if (modelRowSizes != null) {
                rowModel.setSizes(getRowCount(), getRowHeight());
                for (int viewIndex = getRowCount() - 1; viewIndex >= 0;
                         viewIndex--) {
                    int modelIndex = convertRowIndexToModel(viewIndex);
                    rowModel.setSize(viewIndex,
                                     modelRowSizes.getSize(modelIndex));
                }
            }
        }
        private void restoreSelection(ModelChange change) {
            syncingSelection = true;
            if (lastModelSelection != null) {
                restoreSortingSelection(lastModelSelection,
                                        modelLeadIndex, change);
                lastModelSelection = null;
            } else if (modelSelection != null) {
                ListSelectionModel viewSelection = getSelectionModel();
                viewSelection.setValueIsAdjusting(true);
                viewSelection.clearSelection();
                int min = modelSelection.getMinSelectionIndex();
                int max = modelSelection.getMaxSelectionIndex();
                int viewIndex;
                for (int modelIndex = min; modelIndex <= max; modelIndex++) {
                    if (modelSelection.isSelectedIndex(modelIndex)) {
                        viewIndex = convertRowIndexToView(modelIndex);
                        if (viewIndex != -1) {
                            viewSelection.addSelectionInterval(viewIndex,
                                                               viewIndex);
                        }
                    }
                }
                int viewLeadIndex = modelSelection.getLeadSelectionIndex();
                if (viewLeadIndex != -1) {
                    viewLeadIndex = convertRowIndexToView(viewLeadIndex);
                }
                SwingUtilities2.setLeadAnchorWithoutSelection(
                        viewSelection, viewLeadIndex, viewLeadIndex);
                viewSelection.setValueIsAdjusting(false);
            }
            syncingSelection = false;
        }
    }
    private final class ModelChange {
        int startModelIndex;
        int endModelIndex;
        int type;
        int modelRowCount;
        TableModelEvent event;
        int length;
        boolean allRowsChanged;
        ModelChange(TableModelEvent e) {
            startModelIndex = Math.max(0, e.getFirstRow());
            endModelIndex = e.getLastRow();
            modelRowCount = getModel().getRowCount();
            if (endModelIndex < 0) {
                endModelIndex = Math.max(0, modelRowCount - 1);
            }
            length = endModelIndex - startModelIndex + 1;
            type = e.getType();
            event = e;
            allRowsChanged = (e.getLastRow() == Integer.MAX_VALUE);
        }
    }
    private void sortedTableChanged(RowSorterEvent sortedEvent,
                                    TableModelEvent e) {
        int editingModelIndex = -1;
        ModelChange change = (e != null) ? new ModelChange(e) : null;
        if ((change == null || !change.allRowsChanged) &&
                this.editingRow != -1) {
            editingModelIndex = convertRowIndexToModel(sortedEvent,
                                                       this.editingRow);
        }
        sortManager.prepareForChange(sortedEvent, change);
        if (e != null) {
            if (change.type == TableModelEvent.UPDATE) {
                repaintSortedRows(change);
            }
            notifySorter(change);
            if (change.type != TableModelEvent.UPDATE) {
                sorterChanged = true;
            }
        }
        else {
            sorterChanged = true;
        }
        sortManager.processChange(sortedEvent, change, sorterChanged);
        if (sorterChanged) {
            if (this.editingRow != -1) {
                int newIndex = (editingModelIndex == -1) ? -1 :
                        convertRowIndexToView(editingModelIndex,change);
                restoreSortingEditingRow(newIndex);
            }
            if (e == null || change.type != TableModelEvent.UPDATE) {
                resizeAndRepaint();
            }
        }
        if (change != null && change.allRowsChanged) {
            clearSelectionAndLeadAnchor();
            resizeAndRepaint();
        }
    }
    private void repaintSortedRows(ModelChange change) {
        if (change.startModelIndex > change.endModelIndex ||
                change.startModelIndex + 10 < change.endModelIndex) {
            repaint();
            return;
        }
        int eventColumn = change.event.getColumn();
        int columnViewIndex = eventColumn;
        if (columnViewIndex == TableModelEvent.ALL_COLUMNS) {
            columnViewIndex = 0;
        }
        else {
            columnViewIndex = convertColumnIndexToView(columnViewIndex);
            if (columnViewIndex == -1) {
                return;
            }
        }
        int modelIndex = change.startModelIndex;
        while (modelIndex <= change.endModelIndex) {
            int viewIndex = convertRowIndexToView(modelIndex++);
            if (viewIndex != -1) {
                Rectangle dirty = getCellRect(viewIndex, columnViewIndex,
                                              false);
                int x = dirty.x;
                int w = dirty.width;
                if (eventColumn == TableModelEvent.ALL_COLUMNS) {
                    x = 0;
                    w = getWidth();
                }
                repaint(x, dirty.y, w, dirty.height);
            }
        }
    }
    private void restoreSortingSelection(int[] selection, int lead,
            ModelChange change) {
        for (int i = selection.length - 1; i >= 0; i--) {
            selection[i] = convertRowIndexToView(selection[i], change);
        }
        lead = convertRowIndexToView(lead, change);
        if (selection.length == 0 ||
            (selection.length == 1 && selection[0] == getSelectedRow())) {
            return;
        }
        selectionModel.setValueIsAdjusting(true);
        selectionModel.clearSelection();
        for (int i = selection.length - 1; i >= 0; i--) {
            if (selection[i] != -1) {
                selectionModel.addSelectionInterval(selection[i],
                                                    selection[i]);
            }
        }
        SwingUtilities2.setLeadAnchorWithoutSelection(
                selectionModel, lead, lead);
        selectionModel.setValueIsAdjusting(false);
    }
    private void restoreSortingEditingRow(int editingRow) {
        if (editingRow == -1) {
            TableCellEditor editor = getCellEditor();
            if (editor != null) {
                editor.cancelCellEditing();
                if (getCellEditor() != null) {
                    removeEditor();
                }
            }
        }
        else {
            this.editingRow = editingRow;
            repaint();
        }
    }
    private void notifySorter(ModelChange change) {
        try {
            ignoreSortChange = true;
            sorterChanged = false;
            switch(change.type) {
            case TableModelEvent.UPDATE:
                if (change.event.getLastRow() == Integer.MAX_VALUE) {
                    sortManager.sorter.allRowsChanged();
                } else if (change.event.getColumn() ==
                           TableModelEvent.ALL_COLUMNS) {
                    sortManager.sorter.rowsUpdated(change.startModelIndex,
                                       change.endModelIndex);
                } else {
                    sortManager.sorter.rowsUpdated(change.startModelIndex,
                                       change.endModelIndex,
                                       change.event.getColumn());
                }
                break;
            case TableModelEvent.INSERT:
                sortManager.sorter.rowsInserted(change.startModelIndex,
                                    change.endModelIndex);
                break;
            case TableModelEvent.DELETE:
                sortManager.sorter.rowsDeleted(change.startModelIndex,
                                   change.endModelIndex);
                break;
            }
        } finally {
            ignoreSortChange = false;
        }
    }
    private int convertRowIndexToView(int modelIndex, ModelChange change) {
        if (modelIndex < 0) {
            return -1;
        }
        if (change != null && modelIndex >= change.startModelIndex) {
            if (change.type == TableModelEvent.INSERT) {
                if (modelIndex + change.length >= change.modelRowCount) {
                    return -1;
                }
                return sortManager.sorter.convertRowIndexToView(
                        modelIndex + change.length);
            }
            else if (change.type == TableModelEvent.DELETE) {
                if (modelIndex <= change.endModelIndex) {
                    return -1;
                }
                else {
                    if (modelIndex - change.length >= change.modelRowCount) {
                        return -1;
                    }
                    return sortManager.sorter.convertRowIndexToView(
                            modelIndex - change.length);
                }
            }
        }
        if (modelIndex >= getModel().getRowCount()) {
            return -1;
        }
        return sortManager.sorter.convertRowIndexToView(modelIndex);
    }
    private int[] convertSelectionToModel(RowSorterEvent e) {
        int[] selection = getSelectedRows();
        for (int i = selection.length - 1; i >= 0; i--) {
            selection[i] = convertRowIndexToModel(e, selection[i]);
        }
        return selection;
    }
    private int convertRowIndexToModel(RowSorterEvent e, int viewIndex) {
        if (e != null) {
            if (e.getPreviousRowCount() == 0) {
                return viewIndex;
            }
            return e.convertPreviousRowIndexToModel(viewIndex);
        }
        if (viewIndex < 0 || viewIndex >= getRowCount()) {
            return -1;
        }
        return convertRowIndexToModel(viewIndex);
    }
    public void tableChanged(TableModelEvent e) {
        if (e == null || e.getFirstRow() == TableModelEvent.HEADER_ROW) {
            clearSelectionAndLeadAnchor();
            rowModel = null;
            if (sortManager != null) {
                try {
                    ignoreSortChange = true;
                    sortManager.sorter.modelStructureChanged();
                } finally {
                    ignoreSortChange = false;
                }
                sortManager.allChanged();
            }
            if (getAutoCreateColumnsFromModel()) {
                createDefaultColumnsFromModel();
                return;
            }
            resizeAndRepaint();
            return;
        }
        if (sortManager != null) {
            sortedTableChanged(null, e);
            return;
        }
        if (rowModel != null) {
            repaint();
        }
        if (e.getType() == TableModelEvent.INSERT) {
            tableRowsInserted(e);
            return;
        }
        if (e.getType() == TableModelEvent.DELETE) {
            tableRowsDeleted(e);
            return;
        }
        int modelColumn = e.getColumn();
        int start = e.getFirstRow();
        int end = e.getLastRow();
        Rectangle dirtyRegion;
        if (modelColumn == TableModelEvent.ALL_COLUMNS) {
            dirtyRegion = new Rectangle(0, start * getRowHeight(),
                                        getColumnModel().getTotalColumnWidth(), 0);
        }
        else {
            int column = convertColumnIndexToView(modelColumn);
            dirtyRegion = getCellRect(start, column, false);
        }
        if (end != Integer.MAX_VALUE) {
            dirtyRegion.height = (end-start+1)*getRowHeight();
            repaint(dirtyRegion.x, dirtyRegion.y, dirtyRegion.width, dirtyRegion.height);
        }
        else {
            clearSelectionAndLeadAnchor();
            resizeAndRepaint();
            rowModel = null;
        }
    }
    private void tableRowsInserted(TableModelEvent e) {
        int start = e.getFirstRow();
        int end = e.getLastRow();
        if (start < 0) {
            start = 0;
        }
        if (end < 0) {
            end = getRowCount()-1;
        }
        int length = end - start + 1;
        selectionModel.insertIndexInterval(start, length, true);
        if (rowModel != null) {
            rowModel.insertEntries(start, length, getRowHeight());
        }
        int rh = getRowHeight() ;
        Rectangle drawRect = new Rectangle(0, start * rh,
                                        getColumnModel().getTotalColumnWidth(),
                                           (getRowCount()-start) * rh);
        revalidate();
        repaint(drawRect);
    }
    private void tableRowsDeleted(TableModelEvent e) {
        int start = e.getFirstRow();
        int end = e.getLastRow();
        if (start < 0) {
            start = 0;
        }
        if (end < 0) {
            end = getRowCount()-1;
        }
        int deletedCount = end - start + 1;
        int previousRowCount = getRowCount() + deletedCount;
        selectionModel.removeIndexInterval(start, end);
        if (rowModel != null) {
            rowModel.removeEntries(start, deletedCount);
        }
        int rh = getRowHeight();
        Rectangle drawRect = new Rectangle(0, start * rh,
                                        getColumnModel().getTotalColumnWidth(),
                                        (previousRowCount - start) * rh);
        revalidate();
        repaint(drawRect);
    }
    public void columnAdded(TableColumnModelEvent e) {
        if (isEditing()) {
            removeEditor();
        }
        resizeAndRepaint();
    }
    public void columnRemoved(TableColumnModelEvent e) {
        if (isEditing()) {
            removeEditor();
        }
        resizeAndRepaint();
    }
    public void columnMoved(TableColumnModelEvent e) {
        if (isEditing() && !getCellEditor().stopCellEditing()) {
            getCellEditor().cancelCellEditing();
        }
        repaint();
    }
    public void columnMarginChanged(ChangeEvent e) {
        if (isEditing() && !getCellEditor().stopCellEditing()) {
            getCellEditor().cancelCellEditing();
        }
        TableColumn resizingColumn = getResizingColumn();
        if (resizingColumn != null && autoResizeMode == AUTO_RESIZE_OFF) {
            resizingColumn.setPreferredWidth(resizingColumn.getWidth());
        }
        resizeAndRepaint();
    }
    private int limit(int i, int a, int b) {
        return Math.min(b, Math.max(i, a));
    }
    public void columnSelectionChanged(ListSelectionEvent e) {
        boolean isAdjusting = e.getValueIsAdjusting();
        if (columnSelectionAdjusting && !isAdjusting) {
            columnSelectionAdjusting = false;
            return;
        }
        columnSelectionAdjusting = isAdjusting;
        if (getRowCount() <= 0 || getColumnCount() <= 0) {
            return;
        }
        int firstIndex = limit(e.getFirstIndex(), 0, getColumnCount()-1);
        int lastIndex = limit(e.getLastIndex(), 0, getColumnCount()-1);
        int minRow = 0;
        int maxRow = getRowCount() - 1;
        if (getRowSelectionAllowed()) {
            minRow = selectionModel.getMinSelectionIndex();
            maxRow = selectionModel.getMaxSelectionIndex();
            int leadRow = getAdjustedIndex(selectionModel.getLeadSelectionIndex(), true);
            if (minRow == -1 || maxRow == -1) {
                if (leadRow == -1) {
                    return;
                }
                minRow = maxRow = leadRow;
            } else {
                if (leadRow != -1) {
                    minRow = Math.min(minRow, leadRow);
                    maxRow = Math.max(maxRow, leadRow);
                }
            }
        }
        Rectangle firstColumnRect = getCellRect(minRow, firstIndex, false);
        Rectangle lastColumnRect = getCellRect(maxRow, lastIndex, false);
        Rectangle dirtyRegion = firstColumnRect.union(lastColumnRect);
        repaint(dirtyRegion);
    }
    public void valueChanged(ListSelectionEvent e) {
        if (sortManager != null) {
            sortManager.viewSelectionChanged(e);
        }
        boolean isAdjusting = e.getValueIsAdjusting();
        if (rowSelectionAdjusting && !isAdjusting) {
            rowSelectionAdjusting = false;
            return;
        }
        rowSelectionAdjusting = isAdjusting;
        if (getRowCount() <= 0 || getColumnCount() <= 0) {
            return;
        }
        int firstIndex = limit(e.getFirstIndex(), 0, getRowCount()-1);
        int lastIndex = limit(e.getLastIndex(), 0, getRowCount()-1);
        Rectangle firstRowRect = getCellRect(firstIndex, 0, false);
        Rectangle lastRowRect = getCellRect(lastIndex, getColumnCount()-1, false);
        Rectangle dirtyRegion = firstRowRect.union(lastRowRect);
        repaint(dirtyRegion);
    }
    public void editingStopped(ChangeEvent e) {
        TableCellEditor editor = getCellEditor();
        if (editor != null) {
            Object value = editor.getCellEditorValue();
            setValueAt(value, editingRow, editingColumn);
            removeEditor();
        }
    }
    public void editingCanceled(ChangeEvent e) {
        removeEditor();
    }
    public void setPreferredScrollableViewportSize(Dimension size) {
        preferredViewportSize = size;
    }
    public Dimension getPreferredScrollableViewportSize() {
        return preferredViewportSize;
    }
    public int getScrollableUnitIncrement(Rectangle visibleRect,
                                          int orientation,
                                          int direction) {
        int leadingRow;
        int leadingCol;
        Rectangle leadingCellRect;
        int leadingVisibleEdge;
        int leadingCellEdge;
        int leadingCellSize;
        leadingRow = getLeadingRow(visibleRect);
        leadingCol = getLeadingCol(visibleRect);
        if (orientation == SwingConstants.VERTICAL && leadingRow < 0) {
            return getRowHeight();
        }
        else if (orientation == SwingConstants.HORIZONTAL && leadingCol < 0) {
            return 100;
        }
        leadingCellRect = getCellRect(leadingRow, leadingCol, true);
        leadingVisibleEdge = leadingEdge(visibleRect, orientation);
        leadingCellEdge = leadingEdge(leadingCellRect, orientation);
        if (orientation == SwingConstants.VERTICAL) {
            leadingCellSize = leadingCellRect.height;
        }
        else {
            leadingCellSize = leadingCellRect.width;
        }
        if (leadingVisibleEdge == leadingCellEdge) { 
            if (direction < 0) {
                int retVal = 0;
                if (orientation == SwingConstants.VERTICAL) {
                    while (--leadingRow >= 0) {
                        retVal = getRowHeight(leadingRow);
                        if (retVal != 0) {
                            break;
                        }
                    }
                }
                else { 
                    while (--leadingCol >= 0) {
                        retVal = getCellRect(leadingRow, leadingCol, true).width;
                        if (retVal != 0) {
                            break;
                        }
                    }
                }
                return retVal;
            }
            else { 
                return leadingCellSize;
            }
        }
        else { 
            int hiddenAmt = Math.abs(leadingVisibleEdge - leadingCellEdge);
            int visibleAmt = leadingCellSize - hiddenAmt;
            if (direction > 0) {
                return visibleAmt;
            }
            else { 
                return hiddenAmt;
            }
        }
    }
    public int getScrollableBlockIncrement(Rectangle visibleRect,
            int orientation, int direction) {
        if (getRowCount() == 0) {
            if (SwingConstants.VERTICAL == orientation) {
                int rh = getRowHeight();
                return (rh > 0) ? Math.max(rh, (visibleRect.height / rh) * rh) :
                                  visibleRect.height;
            }
            else {
                return visibleRect.width;
            }
        }
        if (null == rowModel && SwingConstants.VERTICAL == orientation) {
            int row = rowAtPoint(visibleRect.getLocation());
            assert row != -1;
            int col = columnAtPoint(visibleRect.getLocation());
            Rectangle cellRect = getCellRect(row, col, true);
            if (cellRect.y == visibleRect.y) {
                int rh = getRowHeight();
                assert rh > 0;
                return Math.max(rh, (visibleRect.height / rh) * rh);
            }
        }
        if (direction < 0) {
            return getPreviousBlockIncrement(visibleRect, orientation);
        }
        else {
            return getNextBlockIncrement(visibleRect, orientation);
        }
    }
    private int getPreviousBlockIncrement(Rectangle visibleRect,
                                          int orientation) {
        int row;
        int col;
        int   newEdge;
        Point newCellLoc;
        int visibleLeadingEdge = leadingEdge(visibleRect, orientation);
        boolean leftToRight = getComponentOrientation().isLeftToRight();
        int newLeadingEdge;
        if (orientation == SwingConstants.VERTICAL) {
            newEdge = visibleLeadingEdge - visibleRect.height;
            int x = visibleRect.x + (leftToRight ? 0 : visibleRect.width);
            newCellLoc = new Point(x, newEdge);
        }
        else if (leftToRight) {
            newEdge = visibleLeadingEdge - visibleRect.width;
            newCellLoc = new Point(newEdge, visibleRect.y);
        }
        else { 
            newEdge = visibleLeadingEdge + visibleRect.width;
            newCellLoc = new Point(newEdge - 1, visibleRect.y);
        }
        row = rowAtPoint(newCellLoc);
        col = columnAtPoint(newCellLoc);
        if (orientation == SwingConstants.VERTICAL & row < 0) {
            newLeadingEdge = 0;
        }
        else if (orientation == SwingConstants.HORIZONTAL & col < 0) {
            if (leftToRight) {
                newLeadingEdge = 0;
            }
            else {
                newLeadingEdge = getWidth();
            }
        }
        else {
            Rectangle newCellRect = getCellRect(row, col, true);
            int newCellLeadingEdge = leadingEdge(newCellRect, orientation);
            int newCellTrailingEdge = trailingEdge(newCellRect, orientation);
            if ((orientation == SwingConstants.VERTICAL || leftToRight) &&
                (newCellTrailingEdge >= visibleLeadingEdge)) {
                newLeadingEdge = newCellLeadingEdge;
            }
            else if (orientation == SwingConstants.HORIZONTAL &&
                     !leftToRight &&
                     newCellTrailingEdge <= visibleLeadingEdge) {
                newLeadingEdge = newCellLeadingEdge;
            }
            else if (newEdge == newCellLeadingEdge) {
                newLeadingEdge = newCellLeadingEdge;
            }
            else {
                newLeadingEdge = newCellTrailingEdge;
            }
        }
        return Math.abs(visibleLeadingEdge - newLeadingEdge);
    }
    private int getNextBlockIncrement(Rectangle visibleRect,
                                      int orientation) {
        int trailingRow = getTrailingRow(visibleRect);
        int trailingCol = getTrailingCol(visibleRect);
        Rectangle cellRect;
        boolean cellFillsVis;
        int cellLeadingEdge;
        int cellTrailingEdge;
        int newLeadingEdge;
        int visibleLeadingEdge = leadingEdge(visibleRect, orientation);
        if (orientation == SwingConstants.VERTICAL && trailingRow < 0) {
            return visibleRect.height;
        }
        else if (orientation == SwingConstants.HORIZONTAL && trailingCol < 0) {
            return visibleRect.width;
        }
        cellRect = getCellRect(trailingRow, trailingCol, true);
        cellLeadingEdge = leadingEdge(cellRect, orientation);
        cellTrailingEdge = trailingEdge(cellRect, orientation);
        if (orientation == SwingConstants.VERTICAL ||
            getComponentOrientation().isLeftToRight()) {
            cellFillsVis = cellLeadingEdge <= visibleLeadingEdge;
        }
        else { 
            cellFillsVis = cellLeadingEdge >= visibleLeadingEdge;
        }
        if (cellFillsVis) {
            newLeadingEdge = cellTrailingEdge;
        }
        else if (cellTrailingEdge == trailingEdge(visibleRect, orientation)) {
            newLeadingEdge = cellTrailingEdge;
        }
        else {
            newLeadingEdge = cellLeadingEdge;
        }
        return Math.abs(newLeadingEdge - visibleLeadingEdge);
    }
    private int getLeadingRow(Rectangle visibleRect) {
        Point leadingPoint;
        if (getComponentOrientation().isLeftToRight()) {
            leadingPoint = new Point(visibleRect.x, visibleRect.y);
        }
        else {
            leadingPoint = new Point(visibleRect.x + visibleRect.width - 1,
                                     visibleRect.y);
        }
        return rowAtPoint(leadingPoint);
    }
    private int getLeadingCol(Rectangle visibleRect) {
        Point leadingPoint;
        if (getComponentOrientation().isLeftToRight()) {
            leadingPoint = new Point(visibleRect.x, visibleRect.y);
        }
        else {
            leadingPoint = new Point(visibleRect.x + visibleRect.width - 1,
                                     visibleRect.y);
        }
        return columnAtPoint(leadingPoint);
    }
    private int getTrailingRow(Rectangle visibleRect) {
        Point trailingPoint;
        if (getComponentOrientation().isLeftToRight()) {
            trailingPoint = new Point(visibleRect.x,
                                      visibleRect.y + visibleRect.height - 1);
        }
        else {
            trailingPoint = new Point(visibleRect.x + visibleRect.width - 1,
                                      visibleRect.y + visibleRect.height - 1);
        }
        return rowAtPoint(trailingPoint);
    }
    private int getTrailingCol(Rectangle visibleRect) {
        Point trailingPoint;
        if (getComponentOrientation().isLeftToRight()) {
            trailingPoint = new Point(visibleRect.x + visibleRect.width - 1,
                                      visibleRect.y);
        }
        else {
            trailingPoint = new Point(visibleRect.x, visibleRect.y);
        }
        return columnAtPoint(trailingPoint);
    }
    private int leadingEdge(Rectangle rect, int orientation) {
        if (orientation == SwingConstants.VERTICAL) {
            return rect.y;
        }
        else if (getComponentOrientation().isLeftToRight()) {
            return rect.x;
        }
        else { 
            return rect.x + rect.width;
        }
    }
    private int trailingEdge(Rectangle rect, int orientation) {
        if (orientation == SwingConstants.VERTICAL) {
            return rect.y + rect.height;
        }
        else if (getComponentOrientation().isLeftToRight()) {
            return rect.x + rect.width;
        }
        else { 
            return rect.x;
        }
    }
    public boolean getScrollableTracksViewportWidth() {
        return !(autoResizeMode == AUTO_RESIZE_OFF);
    }
    public boolean getScrollableTracksViewportHeight() {
        Container parent = SwingUtilities.getUnwrappedParent(this);
        return getFillsViewportHeight()
               && parent instanceof JViewport
               && parent.getHeight() > getPreferredSize().height;
    }
    public void setFillsViewportHeight(boolean fillsViewportHeight) {
        boolean old = this.fillsViewportHeight;
        this.fillsViewportHeight = fillsViewportHeight;
        resizeAndRepaint();
        firePropertyChange("fillsViewportHeight", old, fillsViewportHeight);
    }
    public boolean getFillsViewportHeight() {
        return fillsViewportHeight;
    }
    protected boolean processKeyBinding(KeyStroke ks, KeyEvent e,
                                        int condition, boolean pressed) {
        boolean retValue = super.processKeyBinding(ks, e, condition, pressed);
        if (!retValue && condition == WHEN_ANCESTOR_OF_FOCUSED_COMPONENT &&
            isFocusOwner() &&
            !Boolean.FALSE.equals(getClientProperty("JTable.autoStartsEdit"))) {
            Component editorComponent = getEditorComponent();
            if (editorComponent == null) {
                if (e == null || e.getID() != KeyEvent.KEY_PRESSED) {
                    return false;
                }
                int code = e.getKeyCode();
                if (code == KeyEvent.VK_SHIFT || code == KeyEvent.VK_CONTROL ||
                    code == KeyEvent.VK_ALT) {
                    return false;
                }
                int leadRow = getSelectionModel().getLeadSelectionIndex();
                int leadColumn = getColumnModel().getSelectionModel().
                                   getLeadSelectionIndex();
                if (leadRow != -1 && leadColumn != -1 && !isEditing()) {
                    if (!editCellAt(leadRow, leadColumn, e)) {
                        return false;
                    }
                }
                editorComponent = getEditorComponent();
                if (editorComponent == null) {
                    return false;
                }
            }
            if (editorComponent instanceof JComponent) {
                retValue = ((JComponent)editorComponent).processKeyBinding
                                        (ks, e, WHEN_FOCUSED, pressed);
                if (getSurrendersFocusOnKeystroke()) {
                    editorComponent.requestFocus();
                }
            }
        }
        return retValue;
    }
    private void setLazyValue(Hashtable h, Class c, String s) {
        h.put(c, new SwingLazyValue(s));
    }
    private void setLazyRenderer(Class c, String s) {
        setLazyValue(defaultRenderersByColumnClass, c, s);
    }
    protected void createDefaultRenderers() {
        defaultRenderersByColumnClass = new UIDefaults(8, 0.75f);
        setLazyRenderer(Object.class, "javax.swing.table.DefaultTableCellRenderer$UIResource");
        setLazyRenderer(Number.class, "javax.swing.JTable$NumberRenderer");
        setLazyRenderer(Float.class, "javax.swing.JTable$DoubleRenderer");
        setLazyRenderer(Double.class, "javax.swing.JTable$DoubleRenderer");
        setLazyRenderer(Date.class, "javax.swing.JTable$DateRenderer");
        setLazyRenderer(Icon.class, "javax.swing.JTable$IconRenderer");
        setLazyRenderer(ImageIcon.class, "javax.swing.JTable$IconRenderer");
        setLazyRenderer(Boolean.class, "javax.swing.JTable$BooleanRenderer");
    }
    static class NumberRenderer extends DefaultTableCellRenderer.UIResource {
        public NumberRenderer() {
            super();
            setHorizontalAlignment(JLabel.RIGHT);
        }
    }
    static class DoubleRenderer extends NumberRenderer {
        NumberFormat formatter;
        public DoubleRenderer() { super(); }
        public void setValue(Object value) {
            if (formatter == null) {
                formatter = NumberFormat.getInstance();
            }
            setText((value == null) ? "" : formatter.format(value));
        }
    }
    static class DateRenderer extends DefaultTableCellRenderer.UIResource {
        DateFormat formatter;
        public DateRenderer() { super(); }
        public void setValue(Object value) {
            if (formatter==null) {
                formatter = DateFormat.getDateInstance();
            }
            setText((value == null) ? "" : formatter.format(value));
        }
    }
    static class IconRenderer extends DefaultTableCellRenderer.UIResource {
        public IconRenderer() {
            super();
            setHorizontalAlignment(JLabel.CENTER);
        }
        public void setValue(Object value) { setIcon((value instanceof Icon) ? (Icon)value : null); }
    }
    static class BooleanRenderer extends JCheckBox implements TableCellRenderer, UIResource
    {
        private static final Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);
        public BooleanRenderer() {
            super();
            setHorizontalAlignment(JLabel.CENTER);
            setBorderPainted(true);
        }
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            if (isSelected) {
                setForeground(table.getSelectionForeground());
                super.setBackground(table.getSelectionBackground());
            }
            else {
                setForeground(table.getForeground());
                setBackground(table.getBackground());
            }
            setSelected((value != null && ((Boolean)value).booleanValue()));
            if (hasFocus) {
                setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
            } else {
                setBorder(noFocusBorder);
            }
            return this;
        }
    }
    private void setLazyEditor(Class c, String s) {
        setLazyValue(defaultEditorsByColumnClass, c, s);
    }
    protected void createDefaultEditors() {
        defaultEditorsByColumnClass = new UIDefaults(3, 0.75f);
        setLazyEditor(Object.class, "javax.swing.JTable$GenericEditor");
        setLazyEditor(Number.class, "javax.swing.JTable$NumberEditor");
        setLazyEditor(Boolean.class, "javax.swing.JTable$BooleanEditor");
    }
    static class GenericEditor extends DefaultCellEditor {
        Class[] argTypes = new Class[]{String.class};
        java.lang.reflect.Constructor constructor;
        Object value;
        public GenericEditor() {
            super(new JTextField());
            getComponent().setName("Table.editor");
        }
        public boolean stopCellEditing() {
            String s = (String)super.getCellEditorValue();
            if ("".equals(s)) {
                if (constructor.getDeclaringClass() == String.class) {
                    value = s;
                }
                super.stopCellEditing();
            }
            try {
                value = constructor.newInstance(new Object[]{s});
            }
            catch (Exception e) {
                ((JComponent)getComponent()).setBorder(new LineBorder(Color.red));
                return false;
            }
            return super.stopCellEditing();
        }
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                 boolean isSelected,
                                                 int row, int column) {
            this.value = null;
            ((JComponent)getComponent()).setBorder(new LineBorder(Color.black));
            try {
                Class<?> type = table.getColumnClass(column);
                if (type == Object.class) {
                    type = String.class;
                }
                constructor = type.getConstructor(argTypes);
            }
            catch (Exception e) {
                return null;
            }
            return super.getTableCellEditorComponent(table, value, isSelected, row, column);
        }
        public Object getCellEditorValue() {
            return value;
        }
    }
    static class NumberEditor extends GenericEditor {
        public NumberEditor() {
            ((JTextField)getComponent()).setHorizontalAlignment(JTextField.RIGHT);
        }
    }
    static class BooleanEditor extends DefaultCellEditor {
        public BooleanEditor() {
            super(new JCheckBox());
            JCheckBox checkBox = (JCheckBox)getComponent();
            checkBox.setHorizontalAlignment(JCheckBox.CENTER);
        }
    }
    protected void initializeLocalVars() {
        updateSelectionOnSort = true;
        setOpaque(true);
        createDefaultRenderers();
        createDefaultEditors();
        setTableHeader(createDefaultTableHeader());
        setShowGrid(true);
        setAutoResizeMode(AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        setRowHeight(16);
        isRowHeightSet = false;
        setRowMargin(1);
        setRowSelectionAllowed(true);
        setCellEditor(null);
        setEditingColumn(-1);
        setEditingRow(-1);
        setSurrendersFocusOnKeystroke(false);
        setPreferredScrollableViewportSize(new Dimension(450, 400));
        ToolTipManager toolTipManager = ToolTipManager.sharedInstance();
        toolTipManager.registerComponent(this);
        setAutoscrolls(true);
    }
    protected TableModel createDefaultDataModel() {
        return new DefaultTableModel();
    }
    protected TableColumnModel createDefaultColumnModel() {
        return new DefaultTableColumnModel();
    }
    protected ListSelectionModel createDefaultSelectionModel() {
        return new DefaultListSelectionModel();
    }
    protected JTableHeader createDefaultTableHeader() {
        return new JTableHeader(columnModel);
    }
    protected void resizeAndRepaint() {
        revalidate();
        repaint();
    }
    public TableCellEditor getCellEditor() {
        return cellEditor;
    }
    public void setCellEditor(TableCellEditor anEditor) {
        TableCellEditor oldEditor = cellEditor;
        cellEditor = anEditor;
        firePropertyChange("tableCellEditor", oldEditor, anEditor);
    }
    public void setEditingColumn(int aColumn) {
        editingColumn = aColumn;
    }
    public void setEditingRow(int aRow) {
        editingRow = aRow;
    }
    public TableCellRenderer getCellRenderer(int row, int column) {
        TableColumn tableColumn = getColumnModel().getColumn(column);
        TableCellRenderer renderer = tableColumn.getCellRenderer();
        if (renderer == null) {
            renderer = getDefaultRenderer(getColumnClass(column));
        }
        return renderer;
    }
    public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
        Object value = getValueAt(row, column);
        boolean isSelected = false;
        boolean hasFocus = false;
        if (!isPaintingForPrint()) {
            isSelected = isCellSelected(row, column);
            boolean rowIsLead =
                (selectionModel.getLeadSelectionIndex() == row);
            boolean colIsLead =
                (columnModel.getSelectionModel().getLeadSelectionIndex() == column);
            hasFocus = (rowIsLead && colIsLead) && isFocusOwner();
        }
        return renderer.getTableCellRendererComponent(this, value,
                                                      isSelected, hasFocus,
                                                      row, column);
    }
    public TableCellEditor getCellEditor(int row, int column) {
        TableColumn tableColumn = getColumnModel().getColumn(column);
        TableCellEditor editor = tableColumn.getCellEditor();
        if (editor == null) {
            editor = getDefaultEditor(getColumnClass(column));
        }
        return editor;
    }
    public Component prepareEditor(TableCellEditor editor, int row, int column) {
        Object value = getValueAt(row, column);
        boolean isSelected = isCellSelected(row, column);
        Component comp = editor.getTableCellEditorComponent(this, value, isSelected,
                                                  row, column);
        if (comp instanceof JComponent) {
            JComponent jComp = (JComponent)comp;
            if (jComp.getNextFocusableComponent() == null) {
                jComp.setNextFocusableComponent(this);
            }
        }
        return comp;
    }
    public void removeEditor() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().
            removePropertyChangeListener("permanentFocusOwner", editorRemover);
        editorRemover = null;
        TableCellEditor editor = getCellEditor();
        if(editor != null) {
            editor.removeCellEditorListener(this);
            if (editorComp != null) {
                Component focusOwner =
                        KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
                boolean isFocusOwnerInTheTable = focusOwner != null?
                        SwingUtilities.isDescendingFrom(focusOwner, this):false;
                remove(editorComp);
                if(isFocusOwnerInTheTable) {
                    requestFocusInWindow();
                }
            }
            Rectangle cellRect = getCellRect(editingRow, editingColumn, false);
            setCellEditor(null);
            setEditingColumn(-1);
            setEditingRow(-1);
            editorComp = null;
            repaint(cellRect);
        }
    }
    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        if (getUIClassID().equals(uiClassID)) {
            byte count = JComponent.getWriteObjCounter(this);
            JComponent.setWriteObjCounter(this, --count);
            if (count == 0 && ui != null) {
                ui.installUI(this);
            }
        }
    }
    private void readObject(ObjectInputStream s)
        throws IOException, ClassNotFoundException
    {
        s.defaultReadObject();
        if ((ui != null) && (getUIClassID().equals(uiClassID))) {
            ui.installUI(this);
        }
        createDefaultRenderers();
        createDefaultEditors();
        if (getToolTipText() == null) {
            ToolTipManager.sharedInstance().registerComponent(this);
         }
    }
    void compWriteObjectNotify() {
        super.compWriteObjectNotify();
        if (getToolTipText() == null) {
            ToolTipManager.sharedInstance().unregisterComponent(this);
        }
    }
    protected String paramString() {
        String gridColorString = (gridColor != null ?
                                  gridColor.toString() : "");
        String showHorizontalLinesString = (showHorizontalLines ?
                                            "true" : "false");
        String showVerticalLinesString = (showVerticalLines ?
                                          "true" : "false");
        String autoResizeModeString;
        if (autoResizeMode == AUTO_RESIZE_OFF) {
            autoResizeModeString = "AUTO_RESIZE_OFF";
        } else if (autoResizeMode == AUTO_RESIZE_NEXT_COLUMN) {
            autoResizeModeString = "AUTO_RESIZE_NEXT_COLUMN";
        } else if (autoResizeMode == AUTO_RESIZE_SUBSEQUENT_COLUMNS) {
            autoResizeModeString = "AUTO_RESIZE_SUBSEQUENT_COLUMNS";
        } else if (autoResizeMode == AUTO_RESIZE_LAST_COLUMN) {
            autoResizeModeString = "AUTO_RESIZE_LAST_COLUMN";
        } else if (autoResizeMode == AUTO_RESIZE_ALL_COLUMNS)  {
            autoResizeModeString = "AUTO_RESIZE_ALL_COLUMNS";
        } else autoResizeModeString = "";
        String autoCreateColumnsFromModelString = (autoCreateColumnsFromModel ?
                                                   "true" : "false");
        String preferredViewportSizeString = (preferredViewportSize != null ?
                                              preferredViewportSize.toString()
                                              : "");
        String rowSelectionAllowedString = (rowSelectionAllowed ?
                                            "true" : "false");
        String cellSelectionEnabledString = (cellSelectionEnabled ?
                                            "true" : "false");
        String selectionForegroundString = (selectionForeground != null ?
                                            selectionForeground.toString() :
                                            "");
        String selectionBackgroundString = (selectionBackground != null ?
                                            selectionBackground.toString() :
                                            "");
        return super.paramString() +
        ",autoCreateColumnsFromModel=" + autoCreateColumnsFromModelString +
        ",autoResizeMode=" + autoResizeModeString +
        ",cellSelectionEnabled=" + cellSelectionEnabledString +
        ",editingColumn=" + editingColumn +
        ",editingRow=" + editingRow +
        ",gridColor=" + gridColorString +
        ",preferredViewportSize=" + preferredViewportSizeString +
        ",rowHeight=" + rowHeight +
        ",rowMargin=" + rowMargin +
        ",rowSelectionAllowed=" + rowSelectionAllowedString +
        ",selectionBackground=" + selectionBackgroundString +
        ",selectionForeground=" + selectionForegroundString +
        ",showHorizontalLines=" + showHorizontalLinesString +
        ",showVerticalLines=" + showVerticalLinesString;
    }
    class CellEditorRemover implements PropertyChangeListener {
        KeyboardFocusManager focusManager;
        public CellEditorRemover(KeyboardFocusManager fm) {
            this.focusManager = fm;
        }
        public void propertyChange(PropertyChangeEvent ev) {
            if (!isEditing() || getClientProperty("terminateEditOnFocusLost") != Boolean.TRUE) {
                return;
            }
            Component c = focusManager.getPermanentFocusOwner();
            while (c != null) {
                if (c == JTable.this) {
                    return;
                } else if ((c instanceof Window) ||
                           (c instanceof Applet && c.getParent() == null)) {
                    if (c == SwingUtilities.getRoot(JTable.this)) {
                        if (!getCellEditor().stopCellEditing()) {
                            getCellEditor().cancelCellEditing();
                        }
                    }
                    break;
                }
                c = c.getParent();
            }
        }
    }
    public boolean print() throws PrinterException {
        return print(PrintMode.FIT_WIDTH);
    }
    public boolean print(PrintMode printMode) throws PrinterException {
        return print(printMode, null, null);
    }
    public boolean print(PrintMode printMode,
                         MessageFormat headerFormat,
                         MessageFormat footerFormat) throws PrinterException {
        boolean showDialogs = !GraphicsEnvironment.isHeadless();
        return print(printMode, headerFormat, footerFormat,
                     showDialogs, null, showDialogs);
    }
    public boolean print(PrintMode printMode,
                         MessageFormat headerFormat,
                         MessageFormat footerFormat,
                         boolean showPrintDialog,
                         PrintRequestAttributeSet attr,
                         boolean interactive) throws PrinterException,
                                                     HeadlessException {
        return print(printMode,
                     headerFormat,
                     footerFormat,
                     showPrintDialog,
                     attr,
                     interactive,
                     null);
    }
    public boolean print(PrintMode printMode,
                         MessageFormat headerFormat,
                         MessageFormat footerFormat,
                         boolean showPrintDialog,
                         PrintRequestAttributeSet attr,
                         boolean interactive,
                         PrintService service) throws PrinterException,
                                                      HeadlessException {
        boolean isHeadless = GraphicsEnvironment.isHeadless();
        if (isHeadless) {
            if (showPrintDialog) {
                throw new HeadlessException("Can't show print dialog.");
            }
            if (interactive) {
                throw new HeadlessException("Can't run interactively.");
            }
        }
        final PrinterJob job = PrinterJob.getPrinterJob();
        if (isEditing()) {
            if (!getCellEditor().stopCellEditing()) {
                getCellEditor().cancelCellEditing();
            }
        }
        if (attr == null) {
            attr = new HashPrintRequestAttributeSet();
        }
        final PrintingStatus printingStatus;
        Printable printable =
             getPrintable(printMode, headerFormat, footerFormat);
        if (interactive) {
            printable = new ThreadSafePrintable(printable);
            printingStatus = PrintingStatus.createPrintingStatus(this, job);
            printable = printingStatus.createNotificationPrintable(printable);
        } else {
            printingStatus = null;
        }
        job.setPrintable(printable);
        if (service != null) {
            job.setPrintService(service);
        }
        if (showPrintDialog && !job.printDialog(attr)) {
            return false;
        }
        if (!interactive) {
            job.print(attr);
            return true;
        }
        printError = null;
        final Object lock = new Object();
        final PrintRequestAttributeSet copyAttr = attr;
        Runnable runnable = new Runnable() {
            public void run() {
                try {
                    job.print(copyAttr);
                } catch (Throwable t) {
                    synchronized(lock) {
                        printError = t;
                    }
                } finally {
                    printingStatus.dispose();
                }
            }
        };
        Thread th = new Thread(runnable);
        th.start();
        printingStatus.showModal(true);
        Throwable pe;
        synchronized(lock) {
            pe = printError;
            printError = null;
        }
        if (pe != null) {
            if (pe instanceof PrinterAbortException) {
                return false;
            } else if (pe instanceof PrinterException) {
                throw (PrinterException)pe;
            } else if (pe instanceof RuntimeException) {
                throw (RuntimeException)pe;
            } else if (pe instanceof Error) {
                throw (Error)pe;
            }
            throw new AssertionError(pe);
        }
        return true;
    }
    public Printable getPrintable(PrintMode printMode,
                                  MessageFormat headerFormat,
                                  MessageFormat footerFormat) {
        return new TablePrintable(this, printMode, headerFormat, footerFormat);
    }
    private class ThreadSafePrintable implements Printable {
        private Printable printDelegate;
        private int retVal;
        private Throwable retThrowable;
        public ThreadSafePrintable(Printable printDelegate) {
            this.printDelegate = printDelegate;
        }
        public int print(final Graphics graphics,
                         final PageFormat pageFormat,
                         final int pageIndex) throws PrinterException {
            Runnable runnable = new Runnable() {
                public synchronized void run() {
                    try {
                        retVal = printDelegate.print(graphics, pageFormat, pageIndex);
                    } catch (Throwable throwable) {
                        retThrowable = throwable;
                    } finally {
                        notifyAll();
                    }
                }
            };
            synchronized(runnable) {
                retVal = -1;
                retThrowable = null;
                SwingUtilities.invokeLater(runnable);
                while (retVal == -1 && retThrowable == null) {
                    try {
                        runnable.wait();
                    } catch (InterruptedException ie) {
                    }
                }
                if (retThrowable != null) {
                    if (retThrowable instanceof PrinterException) {
                        throw (PrinterException)retThrowable;
                    } else if (retThrowable instanceof RuntimeException) {
                        throw (RuntimeException)retThrowable;
                    } else if (retThrowable instanceof Error) {
                        throw (Error)retThrowable;
                    }
                    throw new AssertionError(retThrowable);
                }
                return retVal;
            }
        }
    }
    public AccessibleContext getAccessibleContext() {
        if (accessibleContext == null) {
            accessibleContext = new AccessibleJTable();
        }
        return accessibleContext;
    }
    protected class AccessibleJTable extends AccessibleJComponent
    implements AccessibleSelection, ListSelectionListener, TableModelListener,
    TableColumnModelListener, CellEditorListener, PropertyChangeListener,
    AccessibleExtendedTable {
        int lastSelectedRow;
        int lastSelectedCol;
        protected AccessibleJTable() {
            super();
            JTable.this.addPropertyChangeListener(this);
            JTable.this.getSelectionModel().addListSelectionListener(this);
            TableColumnModel tcm = JTable.this.getColumnModel();
            tcm.addColumnModelListener(this);
            tcm.getSelectionModel().addListSelectionListener(this);
            JTable.this.getModel().addTableModelListener(this);
            lastSelectedRow = JTable.this.getSelectedRow();
            lastSelectedCol = JTable.this.getSelectedColumn();
        }
        public void propertyChange(PropertyChangeEvent e) {
            String name = e.getPropertyName();
            Object oldValue = e.getOldValue();
            Object newValue = e.getNewValue();
            if (name.compareTo("model") == 0) {
                if (oldValue != null && oldValue instanceof TableModel) {
                    ((TableModel) oldValue).removeTableModelListener(this);
                }
                if (newValue != null && newValue instanceof TableModel) {
                    ((TableModel) newValue).addTableModelListener(this);
                }
            } else if (name.compareTo("selectionModel") == 0) {
                Object source = e.getSource();
                if (source == JTable.this) {    
                    if (oldValue != null &&
                        oldValue instanceof ListSelectionModel) {
                        ((ListSelectionModel) oldValue).removeListSelectionListener(this);
                    }
                    if (newValue != null &&
                        newValue instanceof ListSelectionModel) {
                        ((ListSelectionModel) newValue).addListSelectionListener(this);
                    }
                } else if (source == JTable.this.getColumnModel()) {
                    if (oldValue != null &&
                        oldValue instanceof ListSelectionModel) {
                        ((ListSelectionModel) oldValue).removeListSelectionListener(this);
                    }
                    if (newValue != null &&
                        newValue instanceof ListSelectionModel) {
                        ((ListSelectionModel) newValue).addListSelectionListener(this);
                    }
                } else {
                }
            } else if (name.compareTo("columnModel") == 0) {
                if (oldValue != null && oldValue instanceof TableColumnModel) {
                    TableColumnModel tcm = (TableColumnModel) oldValue;
                    tcm.removeColumnModelListener(this);
                    tcm.getSelectionModel().removeListSelectionListener(this);
                }
                if (newValue != null && newValue instanceof TableColumnModel) {
                    TableColumnModel tcm = (TableColumnModel) newValue;
                    tcm.addColumnModelListener(this);
                    tcm.getSelectionModel().addListSelectionListener(this);
                }
            } else if (name.compareTo("tableCellEditor") == 0) {
                if (oldValue != null && oldValue instanceof TableCellEditor) {
                    ((TableCellEditor) oldValue).removeCellEditorListener(this);
                }
                if (newValue != null && newValue instanceof TableCellEditor) {
                    ((TableCellEditor) newValue).addCellEditorListener(this);
                }
            }
        }
        protected class AccessibleJTableModelChange
            implements AccessibleTableModelChange {
            protected int type;
            protected int firstRow;
            protected int lastRow;
            protected int firstColumn;
            protected int lastColumn;
            protected AccessibleJTableModelChange(int type, int firstRow,
                                                  int lastRow, int firstColumn,
                                                  int lastColumn) {
                this.type = type;
                this.firstRow = firstRow;
                this.lastRow = lastRow;
                this.firstColumn = firstColumn;
                this.lastColumn = lastColumn;
            }
            public int getType() {
                return type;
            }
            public int getFirstRow() {
                return firstRow;
            }
            public int getLastRow() {
                return lastRow;
            }
            public int getFirstColumn() {
                return firstColumn;
            }
            public int getLastColumn() {
                return lastColumn;
            }
        }
        public void tableChanged(TableModelEvent e) {
           firePropertyChange(AccessibleContext.ACCESSIBLE_VISIBLE_DATA_PROPERTY,
                              null, null);
           if (e != null) {
               int firstColumn = e.getColumn();
               int lastColumn = e.getColumn();
               if (firstColumn == TableModelEvent.ALL_COLUMNS) {
                   firstColumn = 0;
                   lastColumn = getColumnCount() - 1;
               }
               AccessibleJTableModelChange change =
                   new AccessibleJTableModelChange(e.getType(),
                                                   e.getFirstRow(),
                                                   e.getLastRow(),
                                                   firstColumn,
                                                   lastColumn);
               firePropertyChange(AccessibleContext.ACCESSIBLE_TABLE_MODEL_CHANGED,
                                  null, change);
            }
        }
        public void tableRowsInserted(TableModelEvent e) {
           firePropertyChange(AccessibleContext.ACCESSIBLE_VISIBLE_DATA_PROPERTY,
                              null, null);
           int firstColumn = e.getColumn();
           int lastColumn = e.getColumn();
           if (firstColumn == TableModelEvent.ALL_COLUMNS) {
               firstColumn = 0;
               lastColumn = getColumnCount() - 1;
           }
           AccessibleJTableModelChange change =
               new AccessibleJTableModelChange(e.getType(),
                                               e.getFirstRow(),
                                               e.getLastRow(),
                                               firstColumn,
                                               lastColumn);
           firePropertyChange(AccessibleContext.ACCESSIBLE_TABLE_MODEL_CHANGED,
                              null, change);
        }
        public void tableRowsDeleted(TableModelEvent e) {
           firePropertyChange(AccessibleContext.ACCESSIBLE_VISIBLE_DATA_PROPERTY,
                              null, null);
           int firstColumn = e.getColumn();
           int lastColumn = e.getColumn();
           if (firstColumn == TableModelEvent.ALL_COLUMNS) {
               firstColumn = 0;
               lastColumn = getColumnCount() - 1;
           }
           AccessibleJTableModelChange change =
               new AccessibleJTableModelChange(e.getType(),
                                               e.getFirstRow(),
                                               e.getLastRow(),
                                               firstColumn,
                                               lastColumn);
           firePropertyChange(AccessibleContext.ACCESSIBLE_TABLE_MODEL_CHANGED,
                              null, change);
        }
        public void columnAdded(TableColumnModelEvent e) {
           firePropertyChange(AccessibleContext.ACCESSIBLE_VISIBLE_DATA_PROPERTY,
                              null, null);
           int type = AccessibleTableModelChange.INSERT;
           AccessibleJTableModelChange change =
               new AccessibleJTableModelChange(type,
                                               0,
                                               0,
                                               e.getFromIndex(),
                                               e.getToIndex());
           firePropertyChange(AccessibleContext.ACCESSIBLE_TABLE_MODEL_CHANGED,
                              null, change);
        }
        public void columnRemoved(TableColumnModelEvent e) {
           firePropertyChange(AccessibleContext.ACCESSIBLE_VISIBLE_DATA_PROPERTY,
                              null, null);
           int type = AccessibleTableModelChange.DELETE;
           AccessibleJTableModelChange change =
               new AccessibleJTableModelChange(type,
                                               0,
                                               0,
                                               e.getFromIndex(),
                                               e.getToIndex());
           firePropertyChange(AccessibleContext.ACCESSIBLE_TABLE_MODEL_CHANGED,
                              null, change);
        }
        public void columnMoved(TableColumnModelEvent e) {
           firePropertyChange(AccessibleContext.ACCESSIBLE_VISIBLE_DATA_PROPERTY,
                              null, null);
           int type = AccessibleTableModelChange.DELETE;
           AccessibleJTableModelChange change =
               new AccessibleJTableModelChange(type,
                                               0,
                                               0,
                                               e.getFromIndex(),
                                               e.getFromIndex());
           firePropertyChange(AccessibleContext.ACCESSIBLE_TABLE_MODEL_CHANGED,
                              null, change);
           int type2 = AccessibleTableModelChange.INSERT;
           AccessibleJTableModelChange change2 =
               new AccessibleJTableModelChange(type2,
                                               0,
                                               0,
                                               e.getToIndex(),
                                               e.getToIndex());
           firePropertyChange(AccessibleContext.ACCESSIBLE_TABLE_MODEL_CHANGED,
                              null, change2);
        }
        public void columnMarginChanged(ChangeEvent e) {
           firePropertyChange(AccessibleContext.ACCESSIBLE_VISIBLE_DATA_PROPERTY,
                              null, null);
        }
        public void columnSelectionChanged(ListSelectionEvent e) {
        }
        public void editingStopped(ChangeEvent e) {
           firePropertyChange(AccessibleContext.ACCESSIBLE_VISIBLE_DATA_PROPERTY,
                              null, null);
        }
        public void editingCanceled(ChangeEvent e) {
        }
        public void valueChanged(ListSelectionEvent e) {
            firePropertyChange(AccessibleContext.ACCESSIBLE_SELECTION_PROPERTY,
                               Boolean.valueOf(false), Boolean.valueOf(true));
            int selectedRow = JTable.this.getSelectedRow();
            int selectedCol = JTable.this.getSelectedColumn();
            if (selectedRow != lastSelectedRow ||
                selectedCol != lastSelectedCol) {
                Accessible oldA = getAccessibleAt(lastSelectedRow,
                                                  lastSelectedCol);
                Accessible newA = getAccessibleAt(selectedRow, selectedCol);
                firePropertyChange(AccessibleContext.ACCESSIBLE_ACTIVE_DESCENDANT_PROPERTY,
                                   oldA, newA);
                 lastSelectedRow = selectedRow;
                 lastSelectedCol = selectedCol;
             }
        }
        public AccessibleSelection getAccessibleSelection() {
            return this;
        }
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.TABLE;
        }
        public Accessible getAccessibleAt(Point p) {
            int column = columnAtPoint(p);
            int row = rowAtPoint(p);
            if ((column != -1) && (row != -1)) {
                TableColumn aColumn = getColumnModel().getColumn(column);
                TableCellRenderer renderer = aColumn.getCellRenderer();
                if (renderer == null) {
                    Class<?> columnClass = getColumnClass(column);
                    renderer = getDefaultRenderer(columnClass);
                }
                Component component = renderer.getTableCellRendererComponent(
                                  JTable.this, null, false, false,
                                  row, column);
                return new AccessibleJTableCell(JTable.this, row, column,
                      getAccessibleIndexAt(row, column));
            }
            return null;
        }
        public int getAccessibleChildrenCount() {
            return (JTable.this.getColumnCount() * JTable.this.getRowCount());
        }
        public Accessible getAccessibleChild(int i) {
            if (i < 0 || i >= getAccessibleChildrenCount()) {
                return null;
            } else {
                int column = getAccessibleColumnAtIndex(i);
                int row = getAccessibleRowAtIndex(i);
                TableColumn aColumn = getColumnModel().getColumn(column);
                TableCellRenderer renderer = aColumn.getCellRenderer();
                if (renderer == null) {
                    Class<?> columnClass = getColumnClass(column);
                    renderer = getDefaultRenderer(columnClass);
                }
                Component component = renderer.getTableCellRendererComponent(
                                  JTable.this, null, false, false,
                                  row, column);
                return new AccessibleJTableCell(JTable.this, row, column,
                      getAccessibleIndexAt(row, column));
            }
        }
        public int getAccessibleSelectionCount() {
            int rowsSel = JTable.this.getSelectedRowCount();
            int colsSel = JTable.this.getSelectedColumnCount();
            if (JTable.this.cellSelectionEnabled) { 
                return rowsSel * colsSel;
            } else {
                if (JTable.this.getRowSelectionAllowed() &&
                    JTable.this.getColumnSelectionAllowed()) {
                    return rowsSel * JTable.this.getColumnCount() +
                           colsSel * JTable.this.getRowCount() -
                           rowsSel * colsSel;
                } else if (JTable.this.getRowSelectionAllowed()) {
                    return rowsSel * JTable.this.getColumnCount();
                } else if (JTable.this.getColumnSelectionAllowed()) {
                    return colsSel * JTable.this.getRowCount();
                } else {
                    return 0;    
                }
            }
        }
        public Accessible getAccessibleSelection(int i) {
            if (i < 0 || i > getAccessibleSelectionCount()) {
                return null;
            }
            int rowsSel = JTable.this.getSelectedRowCount();
            int colsSel = JTable.this.getSelectedColumnCount();
            int rowIndicies[] = getSelectedRows();
            int colIndicies[] = getSelectedColumns();
            int ttlCols = JTable.this.getColumnCount();
            int ttlRows = JTable.this.getRowCount();
            int r;
            int c;
            if (JTable.this.cellSelectionEnabled) { 
                r = rowIndicies[i / colsSel];
                c = colIndicies[i % colsSel];
                return getAccessibleChild((r * ttlCols) + c);
            } else {
                if (JTable.this.getRowSelectionAllowed() &&
                    JTable.this.getColumnSelectionAllowed()) {
                    int curIndex = i;
                    final int IN_ROW = 0;
                    final int NOT_IN_ROW = 1;
                    int state = (rowIndicies[0] == 0 ? IN_ROW : NOT_IN_ROW);
                    int j = 0;
                    int prevRow = -1;
                    while (j < rowIndicies.length) {
                        switch (state) {
                        case IN_ROW:   
                            if (curIndex < ttlCols) { 
                                c = curIndex % ttlCols;
                                r = rowIndicies[j];
                                return getAccessibleChild((r * ttlCols) + c);
                            } else {                               
                                curIndex -= ttlCols;
                            }
                            if (j + 1 == rowIndicies.length ||
                                rowIndicies[j] != rowIndicies[j+1] - 1) {
                                state = NOT_IN_ROW;
                                prevRow = rowIndicies[j];
                            }
                            j++;  
                            break;
                        case NOT_IN_ROW:  
                            if (curIndex <
                                (colsSel * (rowIndicies[j] -
                                (prevRow == -1 ? 0 : (prevRow + 1))))) {
                                c = colIndicies[curIndex % colsSel];
                                r = (j > 0 ? rowIndicies[j-1] + 1 : 0)
                                    + curIndex / colsSel;
                                return getAccessibleChild((r * ttlCols) + c);
                            } else {                               
                                curIndex -= colsSel * (rowIndicies[j] -
                                (prevRow == -1 ? 0 : (prevRow + 1)));
                            }
                            state = IN_ROW;
                            break;
                        }
                    }
                    if (curIndex <
                        (colsSel * (ttlRows -
                        (prevRow == -1 ? 0 : (prevRow + 1))))) { 
                        c = colIndicies[curIndex % colsSel];
                        r = rowIndicies[j-1] + curIndex / colsSel + 1;
                        return getAccessibleChild((r * ttlCols) + c);
                    } else {                               
                    }
                } else if (JTable.this.getRowSelectionAllowed()) {
                    c = i % ttlCols;
                    r = rowIndicies[i / ttlCols];
                    return getAccessibleChild((r * ttlCols) + c);
                } else if (JTable.this.getColumnSelectionAllowed()) {
                    c = colIndicies[i % colsSel];
                    r = i / colsSel;
                    return getAccessibleChild((r * ttlCols) + c);
                }
            }
            return null;
        }
        public boolean isAccessibleChildSelected(int i) {
            int column = getAccessibleColumnAtIndex(i);
            int row = getAccessibleRowAtIndex(i);
            return JTable.this.isCellSelected(row, column);
        }
        public void addAccessibleSelection(int i) {
            int column = getAccessibleColumnAtIndex(i);
            int row = getAccessibleRowAtIndex(i);
            JTable.this.changeSelection(row, column, true, false);
        }
        public void removeAccessibleSelection(int i) {
            if (JTable.this.cellSelectionEnabled) {
                int column = getAccessibleColumnAtIndex(i);
                int row = getAccessibleRowAtIndex(i);
                JTable.this.removeRowSelectionInterval(row, row);
                JTable.this.removeColumnSelectionInterval(column, column);
            }
        }
        public void clearAccessibleSelection() {
            JTable.this.clearSelection();
        }
        public void selectAllAccessibleSelection() {
            if (JTable.this.cellSelectionEnabled) {
                JTable.this.selectAll();
            }
        }
        public int getAccessibleRow(int index) {
            return getAccessibleRowAtIndex(index);
        }
        public int getAccessibleColumn(int index) {
            return getAccessibleColumnAtIndex(index);
        }
        public int getAccessibleIndex(int r, int c) {
            return getAccessibleIndexAt(r, c);
        }
        private Accessible caption;
        private Accessible summary;
        private Accessible [] rowDescription;
        private Accessible [] columnDescription;
        public AccessibleTable getAccessibleTable() {
            return this;
        }
        public Accessible getAccessibleCaption() {
            return this.caption;
        }
        public void setAccessibleCaption(Accessible a) {
            Accessible oldCaption = caption;
            this.caption = a;
            firePropertyChange(AccessibleContext.ACCESSIBLE_TABLE_CAPTION_CHANGED,
                               oldCaption, this.caption);
        }
        public Accessible getAccessibleSummary() {
            return this.summary;
        }
        public void setAccessibleSummary(Accessible a) {
            Accessible oldSummary = summary;
            this.summary = a;
            firePropertyChange(AccessibleContext.ACCESSIBLE_TABLE_SUMMARY_CHANGED,
                               oldSummary, this.summary);
        }
        public int getAccessibleRowCount() {
            return JTable.this.getRowCount();
        }
        public int getAccessibleColumnCount() {
            return JTable.this.getColumnCount();
        }
        public Accessible getAccessibleAt(int r, int c) {
            return getAccessibleChild((r * getAccessibleColumnCount()) + c);
        }
        public int getAccessibleRowExtentAt(int r, int c) {
            return 1;
        }
        public int getAccessibleColumnExtentAt(int r, int c) {
            return 1;
        }
        public AccessibleTable getAccessibleRowHeader() {
            return null;
        }
        public void setAccessibleRowHeader(AccessibleTable a) {
        }
        public AccessibleTable getAccessibleColumnHeader() {
            JTableHeader header = JTable.this.getTableHeader();
            return header == null ? null : new AccessibleTableHeader(header);
        }
        private class AccessibleTableHeader implements AccessibleTable {
            private JTableHeader header;
            private TableColumnModel headerModel;
            AccessibleTableHeader(JTableHeader header) {
                this.header = header;
                this.headerModel = header.getColumnModel();
            }
            public Accessible getAccessibleCaption() { return null; }
            public void setAccessibleCaption(Accessible a) {}
            public Accessible getAccessibleSummary() { return null; }
            public void setAccessibleSummary(Accessible a) {}
            public int getAccessibleRowCount() { return 1; }
            public int getAccessibleColumnCount() {
                return headerModel.getColumnCount();
            }
            public Accessible getAccessibleAt(int row, int column) {
                TableColumn aColumn = headerModel.getColumn(column);
                TableCellRenderer renderer = aColumn.getHeaderRenderer();
                if (renderer == null) {
                    renderer = header.getDefaultRenderer();
                }
                Component component = renderer.getTableCellRendererComponent(
                                  header.getTable(),
                                  aColumn.getHeaderValue(), false, false,
                                  -1, column);
                return new AccessibleJTableHeaderCell(row, column,
                                                      JTable.this.getTableHeader(),
                                                      component);
            }
            public int getAccessibleRowExtentAt(int r, int c) { return 1; }
            public int getAccessibleColumnExtentAt(int r, int c) { return 1; }
            public AccessibleTable getAccessibleRowHeader() { return null; }
            public void setAccessibleRowHeader(AccessibleTable table) {}
            public AccessibleTable getAccessibleColumnHeader() { return null; }
            public void setAccessibleColumnHeader(AccessibleTable table) {}
            public Accessible getAccessibleRowDescription(int r) { return null; }
            public void setAccessibleRowDescription(int r, Accessible a) {}
            public Accessible getAccessibleColumnDescription(int c) { return null; }
            public void setAccessibleColumnDescription(int c, Accessible a) {}
            public boolean isAccessibleSelected(int r, int c) { return false; }
            public boolean isAccessibleRowSelected(int r) { return false; }
            public boolean isAccessibleColumnSelected(int c) { return false; }
            public int [] getSelectedAccessibleRows() { return new int[0]; }
            public int [] getSelectedAccessibleColumns() { return new int[0]; }
        }
        public void setAccessibleColumnHeader(AccessibleTable a) {
        }
        public Accessible getAccessibleRowDescription(int r) {
            if (r < 0 || r >= getAccessibleRowCount()) {
                throw new IllegalArgumentException(Integer.toString(r));
            }
            if (rowDescription == null) {
                return null;
            } else {
                return rowDescription[r];
            }
        }
        public void setAccessibleRowDescription(int r, Accessible a) {
            if (r < 0 || r >= getAccessibleRowCount()) {
                throw new IllegalArgumentException(Integer.toString(r));
            }
            if (rowDescription == null) {
                int numRows = getAccessibleRowCount();
                rowDescription = new Accessible[numRows];
            }
            rowDescription[r] = a;
        }
        public Accessible getAccessibleColumnDescription(int c) {
            if (c < 0 || c >= getAccessibleColumnCount()) {
                throw new IllegalArgumentException(Integer.toString(c));
            }
            if (columnDescription == null) {
                return null;
            } else {
                return columnDescription[c];
            }
        }
        public void setAccessibleColumnDescription(int c, Accessible a) {
            if (c < 0 || c >= getAccessibleColumnCount()) {
                throw new IllegalArgumentException(Integer.toString(c));
            }
            if (columnDescription == null) {
                int numColumns = getAccessibleColumnCount();
                columnDescription = new Accessible[numColumns];
            }
            columnDescription[c] = a;
        }
        public boolean isAccessibleSelected(int r, int c) {
            return JTable.this.isCellSelected(r, c);
        }
        public boolean isAccessibleRowSelected(int r) {
            return JTable.this.isRowSelected(r);
        }
        public boolean isAccessibleColumnSelected(int c) {
            return JTable.this.isColumnSelected(c);
        }
        public int [] getSelectedAccessibleRows() {
            return JTable.this.getSelectedRows();
        }
        public int [] getSelectedAccessibleColumns() {
            return JTable.this.getSelectedColumns();
        }
        public int getAccessibleRowAtIndex(int i) {
            int columnCount = getAccessibleColumnCount();
            if (columnCount == 0) {
                return -1;
            } else {
                return (i / columnCount);
            }
        }
        public int getAccessibleColumnAtIndex(int i) {
            int columnCount = getAccessibleColumnCount();
            if (columnCount == 0) {
                return -1;
            } else {
                return (i % columnCount);
            }
        }
        public int getAccessibleIndexAt(int r, int c) {
            return ((r * getAccessibleColumnCount()) + c);
        }
        protected class AccessibleJTableCell extends AccessibleContext
            implements Accessible, AccessibleComponent {
            private JTable parent;
            private int row;
            private int column;
            private int index;
            public AccessibleJTableCell(JTable t, int r, int c, int i) {
                parent = t;
                row = r;
                column = c;
                index = i;
                this.setAccessibleParent(parent);
            }
            public AccessibleContext getAccessibleContext() {
                return this;
            }
            protected AccessibleContext getCurrentAccessibleContext() {
                TableColumn aColumn = getColumnModel().getColumn(column);
                TableCellRenderer renderer = aColumn.getCellRenderer();
                if (renderer == null) {
                    Class<?> columnClass = getColumnClass(column);
                    renderer = getDefaultRenderer(columnClass);
                }
                Component component = renderer.getTableCellRendererComponent(
                                  JTable.this, getValueAt(row, column),
                                  false, false, row, column);
                if (component instanceof Accessible) {
                    return component.getAccessibleContext();
                } else {
                    return null;
                }
            }
            protected Component getCurrentComponent() {
                TableColumn aColumn = getColumnModel().getColumn(column);
                TableCellRenderer renderer = aColumn.getCellRenderer();
                if (renderer == null) {
                    Class<?> columnClass = getColumnClass(column);
                    renderer = getDefaultRenderer(columnClass);
                }
                return renderer.getTableCellRendererComponent(
                                  JTable.this, null, false, false,
                                  row, column);
            }
            public String getAccessibleName() {
                AccessibleContext ac = getCurrentAccessibleContext();
                if (ac != null) {
                    String name = ac.getAccessibleName();
                    if ((name != null) && (name != "")) {
                        return name;
                    }
                }
                if ((accessibleName != null) && (accessibleName != "")) {
                    return accessibleName;
                } else {
                    return (String)getClientProperty(AccessibleContext.ACCESSIBLE_NAME_PROPERTY);
                }
            }
            public void setAccessibleName(String s) {
                AccessibleContext ac = getCurrentAccessibleContext();
                if (ac != null) {
                    ac.setAccessibleName(s);
                } else {
                    super.setAccessibleName(s);
                }
            }
            public String getAccessibleDescription() {
                AccessibleContext ac = getCurrentAccessibleContext();
                if (ac != null) {
                    return ac.getAccessibleDescription();
                } else {
                    return super.getAccessibleDescription();
                }
            }
            public void setAccessibleDescription(String s) {
                AccessibleContext ac = getCurrentAccessibleContext();
                if (ac != null) {
                    ac.setAccessibleDescription(s);
                } else {
                    super.setAccessibleDescription(s);
                }
            }
            public AccessibleRole getAccessibleRole() {
                AccessibleContext ac = getCurrentAccessibleContext();
                if (ac != null) {
                    return ac.getAccessibleRole();
                } else {
                    return AccessibleRole.UNKNOWN;
                }
            }
            public AccessibleStateSet getAccessibleStateSet() {
                AccessibleContext ac = getCurrentAccessibleContext();
                AccessibleStateSet as = null;
                if (ac != null) {
                    as = ac.getAccessibleStateSet();
                }
                if (as == null) {
                    as = new AccessibleStateSet();
                }
                Rectangle rjt = JTable.this.getVisibleRect();
                Rectangle rcell = JTable.this.getCellRect(row, column, false);
                if (rjt.intersects(rcell)) {
                    as.add(AccessibleState.SHOWING);
                } else {
                    if (as.contains(AccessibleState.SHOWING)) {
                         as.remove(AccessibleState.SHOWING);
                    }
                }
                if (parent.isCellSelected(row, column)) {
                    as.add(AccessibleState.SELECTED);
                } else if (as.contains(AccessibleState.SELECTED)) {
                    as.remove(AccessibleState.SELECTED);
                }
                if ((row == getSelectedRow()) && (column == getSelectedColumn())) {
                    as.add(AccessibleState.ACTIVE);
                }
                as.add(AccessibleState.TRANSIENT);
                return as;
            }
            public Accessible getAccessibleParent() {
                return parent;
            }
            public int getAccessibleIndexInParent() {
                return index;
            }
            public int getAccessibleChildrenCount() {
                AccessibleContext ac = getCurrentAccessibleContext();
                if (ac != null) {
                    return ac.getAccessibleChildrenCount();
                } else {
                    return 0;
                }
            }
            public Accessible getAccessibleChild(int i) {
                AccessibleContext ac = getCurrentAccessibleContext();
                if (ac != null) {
                    Accessible accessibleChild = ac.getAccessibleChild(i);
                    ac.setAccessibleParent(this);
                    return accessibleChild;
                } else {
                    return null;
                }
            }
            public Locale getLocale() {
                AccessibleContext ac = getCurrentAccessibleContext();
                if (ac != null) {
                    return ac.getLocale();
                } else {
                    return null;
                }
            }
            public void addPropertyChangeListener(PropertyChangeListener l) {
                AccessibleContext ac = getCurrentAccessibleContext();
                if (ac != null) {
                    ac.addPropertyChangeListener(l);
                } else {
                    super.addPropertyChangeListener(l);
                }
            }
            public void removePropertyChangeListener(PropertyChangeListener l) {
                AccessibleContext ac = getCurrentAccessibleContext();
                if (ac != null) {
                    ac.removePropertyChangeListener(l);
                } else {
                    super.removePropertyChangeListener(l);
                }
            }
            public AccessibleAction getAccessibleAction() {
                return getCurrentAccessibleContext().getAccessibleAction();
            }
            public AccessibleComponent getAccessibleComponent() {
                return this; 
            }
            public AccessibleSelection getAccessibleSelection() {
                return getCurrentAccessibleContext().getAccessibleSelection();
            }
            public AccessibleText getAccessibleText() {
                return getCurrentAccessibleContext().getAccessibleText();
            }
            public AccessibleValue getAccessibleValue() {
                return getCurrentAccessibleContext().getAccessibleValue();
            }
            public Color getBackground() {
                AccessibleContext ac = getCurrentAccessibleContext();
                if (ac instanceof AccessibleComponent) {
                    return ((AccessibleComponent) ac).getBackground();
                } else {
                    Component c = getCurrentComponent();
                    if (c != null) {
                        return c.getBackground();
                    } else {
                        return null;
                    }
                }
            }
            public void setBackground(Color c) {
                AccessibleContext ac = getCurrentAccessibleContext();
                if (ac instanceof AccessibleComponent) {
                    ((AccessibleComponent) ac).setBackground(c);
                } else {
                    Component cp = getCurrentComponent();
                    if (cp != null) {
                        cp.setBackground(c);
                    }
                }
            }
            public Color getForeground() {
                AccessibleContext ac = getCurrentAccessibleContext();
                if (ac instanceof AccessibleComponent) {
                    return ((AccessibleComponent) ac).getForeground();
                } else {
                    Component c = getCurrentComponent();
                    if (c != null) {
                        return c.getForeground();
                    } else {
                        return null;
                    }
                }
            }
            public void setForeground(Color c) {
                AccessibleContext ac = getCurrentAccessibleContext();
                if (ac instanceof AccessibleComponent) {
                    ((AccessibleComponent) ac).setForeground(c);
                } else {
                    Component cp = getCurrentComponent();
                    if (cp != null) {
                        cp.setForeground(c);
                    }
                }
            }
            public Cursor getCursor() {
                AccessibleContext ac = getCurrentAccessibleContext();
                if (ac instanceof AccessibleComponent) {
                    return ((AccessibleComponent) ac).getCursor();
                } else {
                    Component c = getCurrentComponent();
                    if (c != null) {
                        return c.getCursor();
                    } else {
                        Accessible ap = getAccessibleParent();
                        if (ap instanceof AccessibleComponent) {
                            return ((AccessibleComponent) ap).getCursor();
                        } else {
                            return null;
                        }
                    }
                }
            }
            public void setCursor(Cursor c) {
                AccessibleContext ac = getCurrentAccessibleContext();
                if (ac instanceof AccessibleComponent) {
                    ((AccessibleComponent) ac).setCursor(c);
                } else {
                    Component cp = getCurrentComponent();
                    if (cp != null) {
                        cp.setCursor(c);
                    }
                }
            }
            public Font getFont() {
                AccessibleContext ac = getCurrentAccessibleContext();
                if (ac instanceof AccessibleComponent) {
                    return ((AccessibleComponent) ac).getFont();
                } else {
                    Component c = getCurrentComponent();
                    if (c != null) {
                        return c.getFont();
                    } else {
                        return null;
                    }
                }
            }
            public void setFont(Font f) {
                AccessibleContext ac = getCurrentAccessibleContext();
                if (ac instanceof AccessibleComponent) {
                    ((AccessibleComponent) ac).setFont(f);
                } else {
                    Component c = getCurrentComponent();
                    if (c != null) {
                        c.setFont(f);
                    }
                }
            }
            public FontMetrics getFontMetrics(Font f) {
                AccessibleContext ac = getCurrentAccessibleContext();
                if (ac instanceof AccessibleComponent) {
                    return ((AccessibleComponent) ac).getFontMetrics(f);
                } else {
                    Component c = getCurrentComponent();
                    if (c != null) {
                        return c.getFontMetrics(f);
                    } else {
                        return null;
                    }
                }
            }
            public boolean isEnabled() {
                AccessibleContext ac = getCurrentAccessibleContext();
                if (ac instanceof AccessibleComponent) {
                    return ((AccessibleComponent) ac).isEnabled();
                } else {
                    Component c = getCurrentComponent();
                    if (c != null) {
                        return c.isEnabled();
                    } else {
                        return false;
                    }
                }
            }
            public void setEnabled(boolean b) {
                AccessibleContext ac = getCurrentAccessibleContext();
                if (ac instanceof AccessibleComponent) {
                    ((AccessibleComponent) ac).setEnabled(b);
                } else {
                    Component c = getCurrentComponent();
                    if (c != null) {
                        c.setEnabled(b);
                    }
                }
            }
            public boolean isVisible() {
                AccessibleContext ac = getCurrentAccessibleContext();
                if (ac instanceof AccessibleComponent) {
                    return ((AccessibleComponent) ac).isVisible();
                } else {
                    Component c = getCurrentComponent();
                    if (c != null) {
                        return c.isVisible();
                    } else {
                        return false;
                    }
                }
            }
            public void setVisible(boolean b) {
                AccessibleContext ac = getCurrentAccessibleContext();
                if (ac instanceof AccessibleComponent) {
                    ((AccessibleComponent) ac).setVisible(b);
                } else {
                    Component c = getCurrentComponent();
                    if (c != null) {
                        c.setVisible(b);
                    }
                }
            }
            public boolean isShowing() {
                AccessibleContext ac = getCurrentAccessibleContext();
                if (ac instanceof AccessibleComponent) {
                    if (ac.getAccessibleParent() != null) {
                        return ((AccessibleComponent) ac).isShowing();
                    } else {
                        return isVisible();
                    }
                } else {
                    Component c = getCurrentComponent();
                    if (c != null) {
                        return c.isShowing();
                    } else {
                        return false;
                    }
                }
            }
            public boolean contains(Point p) {
                AccessibleContext ac = getCurrentAccessibleContext();
                if (ac instanceof AccessibleComponent) {
                    Rectangle r = ((AccessibleComponent) ac).getBounds();
                    return r.contains(p);
                } else {
                    Component c = getCurrentComponent();
                    if (c != null) {
                        Rectangle r = c.getBounds();
                        return r.contains(p);
                    } else {
                        return getBounds().contains(p);
                    }
                }
            }
            public Point getLocationOnScreen() {
                if (parent != null) {
                    Point parentLocation = parent.getLocationOnScreen();
                    Point componentLocation = getLocation();
                    componentLocation.translate(parentLocation.x, parentLocation.y);
                    return componentLocation;
                } else {
                    return null;
                }
            }
            public Point getLocation() {
                if (parent != null) {
                    Rectangle r = parent.getCellRect(row, column, false);
                    if (r != null) {
                        return r.getLocation();
                    }
                }
                return null;
            }
            public void setLocation(Point p) {
            }
            public Rectangle getBounds() {
                if (parent != null) {
                    return parent.getCellRect(row, column, false);
                } else {
                    return null;
                }
            }
            public void setBounds(Rectangle r) {
                AccessibleContext ac = getCurrentAccessibleContext();
                if (ac instanceof AccessibleComponent) {
                    ((AccessibleComponent) ac).setBounds(r);
                } else {
                    Component c = getCurrentComponent();
                    if (c != null) {
                        c.setBounds(r);
                    }
                }
            }
            public Dimension getSize() {
                if (parent != null) {
                    Rectangle r = parent.getCellRect(row, column, false);
                    if (r != null) {
                        return r.getSize();
                    }
                }
                return null;
            }
            public void setSize (Dimension d) {
                AccessibleContext ac = getCurrentAccessibleContext();
                if (ac instanceof AccessibleComponent) {
                    ((AccessibleComponent) ac).setSize(d);
                } else {
                    Component c = getCurrentComponent();
                    if (c != null) {
                        c.setSize(d);
                    }
                }
            }
            public Accessible getAccessibleAt(Point p) {
                AccessibleContext ac = getCurrentAccessibleContext();
                if (ac instanceof AccessibleComponent) {
                    return ((AccessibleComponent) ac).getAccessibleAt(p);
                } else {
                    return null;
                }
            }
            public boolean isFocusTraversable() {
                AccessibleContext ac = getCurrentAccessibleContext();
                if (ac instanceof AccessibleComponent) {
                    return ((AccessibleComponent) ac).isFocusTraversable();
                } else {
                    Component c = getCurrentComponent();
                    if (c != null) {
                        return c.isFocusTraversable();
                    } else {
                        return false;
                    }
                }
            }
            public void requestFocus() {
                AccessibleContext ac = getCurrentAccessibleContext();
                if (ac instanceof AccessibleComponent) {
                    ((AccessibleComponent) ac).requestFocus();
                } else {
                    Component c = getCurrentComponent();
                    if (c != null) {
                        c.requestFocus();
                    }
                }
            }
            public void addFocusListener(FocusListener l) {
                AccessibleContext ac = getCurrentAccessibleContext();
                if (ac instanceof AccessibleComponent) {
                    ((AccessibleComponent) ac).addFocusListener(l);
                } else {
                    Component c = getCurrentComponent();
                    if (c != null) {
                        c.addFocusListener(l);
                    }
                }
            }
            public void removeFocusListener(FocusListener l) {
                AccessibleContext ac = getCurrentAccessibleContext();
                if (ac instanceof AccessibleComponent) {
                    ((AccessibleComponent) ac).removeFocusListener(l);
                } else {
                    Component c = getCurrentComponent();
                    if (c != null) {
                        c.removeFocusListener(l);
                    }
                }
            }
        } 
        private class AccessibleJTableHeaderCell extends AccessibleContext
            implements Accessible, AccessibleComponent {
            private int row;
            private int column;
            private JTableHeader parent;
            private Component rendererComponent;
            public AccessibleJTableHeaderCell(int row, int column,
                                              JTableHeader parent,
                                              Component rendererComponent) {
                this.row = row;
                this.column = column;
                this.parent = parent;
                this.rendererComponent = rendererComponent;
                this.setAccessibleParent(parent);
            }
            public AccessibleContext getAccessibleContext() {
                return this;
            }
            private AccessibleContext getCurrentAccessibleContext() {
                return rendererComponent.getAccessibleContext();
            }
            private Component getCurrentComponent() {
                return rendererComponent;
            }
            public String getAccessibleName() {
                AccessibleContext ac = getCurrentAccessibleContext();
                if (ac != null) {
                    String name = ac.getAccessibleName();
                    if ((name != null) && (name != "")) {
                        return ac.getAccessibleName();
                    }
                }
                if ((accessibleName != null) && (accessibleName != "")) {
                    return accessibleName;
                } else {
                    return null;
                }
            }
            public void setAccessibleName(String s) {
                AccessibleContext ac = getCurrentAccessibleContext();
                if (ac != null) {
                    ac.setAccessibleName(s);
                } else {
                    super.setAccessibleName(s);
                }
            }
            public String getAccessibleDescription() {
                AccessibleContext ac = getCurrentAccessibleContext();
                if (ac != null) {
                    return ac.getAccessibleDescription();
                } else {
                    return super.getAccessibleDescription();
                }
            }
            public void setAccessibleDescription(String s) {
                AccessibleContext ac = getCurrentAccessibleContext();
                if (ac != null) {
                    ac.setAccessibleDescription(s);
                } else {
                    super.setAccessibleDescription(s);
                }
            }
            public AccessibleRole getAccessibleRole() {
                AccessibleContext ac = getCurrentAccessibleContext();
                if (ac != null) {
                    return ac.getAccessibleRole();
                } else {
                    return AccessibleRole.UNKNOWN;
                }
            }
            public AccessibleStateSet getAccessibleStateSet() {
                AccessibleContext ac = getCurrentAccessibleContext();
                AccessibleStateSet as = null;
                if (ac != null) {
                    as = ac.getAccessibleStateSet();
                }
                if (as == null) {
                    as = new AccessibleStateSet();
                }
                Rectangle rjt = JTable.this.getVisibleRect();
                Rectangle rcell = JTable.this.getCellRect(row, column, false);
                if (rjt.intersects(rcell)) {
                    as.add(AccessibleState.SHOWING);
                } else {
                    if (as.contains(AccessibleState.SHOWING)) {
                         as.remove(AccessibleState.SHOWING);
                    }
                }
                if (JTable.this.isCellSelected(row, column)) {
                    as.add(AccessibleState.SELECTED);
                } else if (as.contains(AccessibleState.SELECTED)) {
                    as.remove(AccessibleState.SELECTED);
                }
                if ((row == getSelectedRow()) && (column == getSelectedColumn())) {
                    as.add(AccessibleState.ACTIVE);
                }
                as.add(AccessibleState.TRANSIENT);
                return as;
            }
            public Accessible getAccessibleParent() {
                return parent;
            }
            public int getAccessibleIndexInParent() {
                return column;
            }
            public int getAccessibleChildrenCount() {
                AccessibleContext ac = getCurrentAccessibleContext();
                if (ac != null) {
                    return ac.getAccessibleChildrenCount();
                } else {
                    return 0;
                }
            }
            public Accessible getAccessibleChild(int i) {
                AccessibleContext ac = getCurrentAccessibleContext();
                if (ac != null) {
                    Accessible accessibleChild = ac.getAccessibleChild(i);
                    ac.setAccessibleParent(this);
                    return accessibleChild;
                } else {
                    return null;
                }
            }
            public Locale getLocale() {
                AccessibleContext ac = getCurrentAccessibleContext();
                if (ac != null) {
                    return ac.getLocale();
                } else {
                    return null;
                }
            }
            public void addPropertyChangeListener(PropertyChangeListener l) {
                AccessibleContext ac = getCurrentAccessibleContext();
                if (ac != null) {
                    ac.addPropertyChangeListener(l);
                } else {
                    super.addPropertyChangeListener(l);
                }
            }
            public void removePropertyChangeListener(PropertyChangeListener l) {
                AccessibleContext ac = getCurrentAccessibleContext();
                if (ac != null) {
                    ac.removePropertyChangeListener(l);
                } else {
                    super.removePropertyChangeListener(l);
                }
            }
            public AccessibleAction getAccessibleAction() {
                return getCurrentAccessibleContext().getAccessibleAction();
            }
            public AccessibleComponent getAccessibleComponent() {
                return this; 
            }
            public AccessibleSelection getAccessibleSelection() {
                return getCurrentAccessibleContext().getAccessibleSelection();
            }
            public AccessibleText getAccessibleText() {
                return getCurrentAccessibleContext().getAccessibleText();
            }
            public AccessibleValue getAccessibleValue() {
                return getCurrentAccessibleContext().getAccessibleValue();
            }
            public Color getBackground() {
                AccessibleContext ac = getCurrentAccessibleContext();
                if (ac instanceof AccessibleComponent) {
                    return ((AccessibleComponent) ac).getBackground();
                } else {
                    Component c = getCurrentComponent();
                    if (c != null) {
                        return c.getBackground();
                    } else {
                        return null;
                    }
                }
            }
            public void setBackground(Color c) {
                AccessibleContext ac = getCurrentAccessibleContext();
                if (ac instanceof AccessibleComponent) {
                    ((AccessibleComponent) ac).setBackground(c);
                } else {
                    Component cp = getCurrentComponent();
                    if (cp != null) {
                        cp.setBackground(c);
                    }
                }
            }
            public Color getForeground() {
                AccessibleContext ac = getCurrentAccessibleContext();
                if (ac instanceof AccessibleComponent) {
                    return ((AccessibleComponent) ac).getForeground();
                } else {
                    Component c = getCurrentComponent();
                    if (c != null) {
                        return c.getForeground();
                    } else {
                        return null;
                    }
                }
            }
            public void setForeground(Color c) {
                AccessibleContext ac = getCurrentAccessibleContext();
                if (ac instanceof AccessibleComponent) {
                    ((AccessibleComponent) ac).setForeground(c);
                } else {
                    Component cp = getCurrentComponent();
                    if (cp != null) {
                        cp.setForeground(c);
                    }
                }
            }
            public Cursor getCursor() {
                AccessibleContext ac = getCurrentAccessibleContext();
                if (ac instanceof AccessibleComponent) {
                    return ((AccessibleComponent) ac).getCursor();
                } else {
                    Component c = getCurrentComponent();
                    if (c != null) {
                        return c.getCursor();
                    } else {
                        Accessible ap = getAccessibleParent();
                        if (ap instanceof AccessibleComponent) {
                            return ((AccessibleComponent) ap).getCursor();
                        } else {
                            return null;
                        }
                    }
                }
            }
            public void setCursor(Cursor c) {
                AccessibleContext ac = getCurrentAccessibleContext();
                if (ac instanceof AccessibleComponent) {
                    ((AccessibleComponent) ac).setCursor(c);
                } else {
                    Component cp = getCurrentComponent();
                    if (cp != null) {
                        cp.setCursor(c);
                    }
                }
            }
            public Font getFont() {
                AccessibleContext ac = getCurrentAccessibleContext();
                if (ac instanceof AccessibleComponent) {
                    return ((AccessibleComponent) ac).getFont();
                } else {
                    Component c = getCurrentComponent();
                    if (c != null) {
                        return c.getFont();
                    } else {
                        return null;
                    }
                }
            }
            public void setFont(Font f) {
                AccessibleContext ac = getCurrentAccessibleContext();
                if (ac instanceof AccessibleComponent) {
                    ((AccessibleComponent) ac).setFont(f);
                } else {
                    Component c = getCurrentComponent();
                    if (c != null) {
                        c.setFont(f);
                    }
                }
            }
            public FontMetrics getFontMetrics(Font f) {
                AccessibleContext ac = getCurrentAccessibleContext();
                if (ac instanceof AccessibleComponent) {
                    return ((AccessibleComponent) ac).getFontMetrics(f);
                } else {
                    Component c = getCurrentComponent();
                    if (c != null) {
                        return c.getFontMetrics(f);
                    } else {
                        return null;
                    }
                }
            }
            public boolean isEnabled() {
                AccessibleContext ac = getCurrentAccessibleContext();
                if (ac instanceof AccessibleComponent) {
                    return ((AccessibleComponent) ac).isEnabled();
                } else {
                    Component c = getCurrentComponent();
                    if (c != null) {
                        return c.isEnabled();
                    } else {
                        return false;
                    }
                }
            }
            public void setEnabled(boolean b) {
                AccessibleContext ac = getCurrentAccessibleContext();
                if (ac instanceof AccessibleComponent) {
                    ((AccessibleComponent) ac).setEnabled(b);
                } else {
                    Component c = getCurrentComponent();
                    if (c != null) {
                        c.setEnabled(b);
                    }
                }
            }
            public boolean isVisible() {
                AccessibleContext ac = getCurrentAccessibleContext();
                if (ac instanceof AccessibleComponent) {
                    return ((AccessibleComponent) ac).isVisible();
                } else {
                    Component c = getCurrentComponent();
                    if (c != null) {
                        return c.isVisible();
                    } else {
                        return false;
                    }
                }
            }
            public void setVisible(boolean b) {
                AccessibleContext ac = getCurrentAccessibleContext();
                if (ac instanceof AccessibleComponent) {
                    ((AccessibleComponent) ac).setVisible(b);
                } else {
                    Component c = getCurrentComponent();
                    if (c != null) {
                        c.setVisible(b);
                    }
                }
            }
            public boolean isShowing() {
                AccessibleContext ac = getCurrentAccessibleContext();
                if (ac instanceof AccessibleComponent) {
                    if (ac.getAccessibleParent() != null) {
                        return ((AccessibleComponent) ac).isShowing();
                    } else {
                        return isVisible();
                    }
                } else {
                    Component c = getCurrentComponent();
                    if (c != null) {
                        return c.isShowing();
                    } else {
                        return false;
                    }
                }
            }
            public boolean contains(Point p) {
                AccessibleContext ac = getCurrentAccessibleContext();
                if (ac instanceof AccessibleComponent) {
                    Rectangle r = ((AccessibleComponent) ac).getBounds();
                    return r.contains(p);
                } else {
                    Component c = getCurrentComponent();
                    if (c != null) {
                        Rectangle r = c.getBounds();
                        return r.contains(p);
                    } else {
                        return getBounds().contains(p);
                    }
                }
            }
            public Point getLocationOnScreen() {
                if (parent != null) {
                    Point parentLocation = parent.getLocationOnScreen();
                    Point componentLocation = getLocation();
                    componentLocation.translate(parentLocation.x, parentLocation.y);
                    return componentLocation;
                } else {
                    return null;
                }
            }
            public Point getLocation() {
                if (parent != null) {
                    Rectangle r = parent.getHeaderRect(column);
                    if (r != null) {
                        return r.getLocation();
                    }
                }
                return null;
            }
            public void setLocation(Point p) {
            }
            public Rectangle getBounds() {
                if (parent != null) {
                    return parent.getHeaderRect(column);
                } else {
                    return null;
                }
            }
            public void setBounds(Rectangle r) {
                AccessibleContext ac = getCurrentAccessibleContext();
                if (ac instanceof AccessibleComponent) {
                    ((AccessibleComponent) ac).setBounds(r);
                } else {
                    Component c = getCurrentComponent();
                    if (c != null) {
                        c.setBounds(r);
                    }
                }
            }
            public Dimension getSize() {
                if (parent != null) {
                    Rectangle r = parent.getHeaderRect(column);
                    if (r != null) {
                        return r.getSize();
                    }
                }
                return null;
            }
            public void setSize (Dimension d) {
                AccessibleContext ac = getCurrentAccessibleContext();
                if (ac instanceof AccessibleComponent) {
                    ((AccessibleComponent) ac).setSize(d);
                } else {
                    Component c = getCurrentComponent();
                    if (c != null) {
                        c.setSize(d);
                    }
                }
            }
            public Accessible getAccessibleAt(Point p) {
                AccessibleContext ac = getCurrentAccessibleContext();
                if (ac instanceof AccessibleComponent) {
                    return ((AccessibleComponent) ac).getAccessibleAt(p);
                } else {
                    return null;
                }
            }
            public boolean isFocusTraversable() {
                AccessibleContext ac = getCurrentAccessibleContext();
                if (ac instanceof AccessibleComponent) {
                    return ((AccessibleComponent) ac).isFocusTraversable();
                } else {
                    Component c = getCurrentComponent();
                    if (c != null) {
                        return c.isFocusTraversable();
                    } else {
                        return false;
                    }
                }
            }
            public void requestFocus() {
                AccessibleContext ac = getCurrentAccessibleContext();
                if (ac instanceof AccessibleComponent) {
                    ((AccessibleComponent) ac).requestFocus();
                } else {
                    Component c = getCurrentComponent();
                    if (c != null) {
                        c.requestFocus();
                    }
                }
            }
            public void addFocusListener(FocusListener l) {
                AccessibleContext ac = getCurrentAccessibleContext();
                if (ac instanceof AccessibleComponent) {
                    ((AccessibleComponent) ac).addFocusListener(l);
                } else {
                    Component c = getCurrentComponent();
                    if (c != null) {
                        c.addFocusListener(l);
                    }
                }
            }
            public void removeFocusListener(FocusListener l) {
                AccessibleContext ac = getCurrentAccessibleContext();
                if (ac instanceof AccessibleComponent) {
                    ((AccessibleComponent) ac).removeFocusListener(l);
                } else {
                    Component c = getCurrentComponent();
                    if (c != null) {
                        c.removeFocusListener(l);
                    }
                }
            }
        } 
    }  
}  
