public class BasicTableHeaderUI extends TableHeaderUI {
    private static Cursor resizeCursor = Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR);
    protected JTableHeader header;
    protected CellRendererPane rendererPane;
    protected MouseInputListener mouseInputListener;
    private int rolloverColumn = -1;
    private int selectedColumnIndex = 0; 
    private static FocusListener focusListener = new FocusListener() {
        public void focusGained(FocusEvent e) {
            repaintHeader(e.getSource());
        }
        public void focusLost(FocusEvent e) {
            repaintHeader(e.getSource());
        }
        private void repaintHeader(Object source) {
            if (source instanceof JTableHeader) {
                JTableHeader th = (JTableHeader)source;
                BasicTableHeaderUI ui =
                   (BasicTableHeaderUI)BasicLookAndFeel.
                                        getUIOfType(th.getUI(),
                                            BasicTableHeaderUI.class);
                if (ui == null) {
                    return;
                }
                th.repaint(th.getHeaderRect(ui.getSelectedColumnIndex()));
            }
        }
    };
    public class MouseInputHandler implements MouseInputListener {
        private int mouseXOffset;
        private Cursor otherCursor = resizeCursor;
        public void mouseClicked(MouseEvent e) {
            if (!header.isEnabled()) {
                return;
            }
            if (e.getClickCount() % 2 == 1 &&
                    SwingUtilities.isLeftMouseButton(e)) {
                JTable table = header.getTable();
                RowSorter sorter;
                if (table != null && (sorter = table.getRowSorter()) != null) {
                    int columnIndex = header.columnAtPoint(e.getPoint());
                    if (columnIndex != -1) {
                        columnIndex = table.convertColumnIndexToModel(
                                columnIndex);
                        sorter.toggleSortOrder(columnIndex);
                    }
                }
            }
        }
        private TableColumn getResizingColumn(Point p) {
            return getResizingColumn(p, header.columnAtPoint(p));
        }
        private TableColumn getResizingColumn(Point p, int column) {
            if (column == -1) {
                 return null;
            }
            Rectangle r = header.getHeaderRect(column);
            r.grow(-3, 0);
            if (r.contains(p)) {
                return null;
            }
            int midPoint = r.x + r.width/2;
            int columnIndex;
            if( header.getComponentOrientation().isLeftToRight() ) {
                columnIndex = (p.x < midPoint) ? column - 1 : column;
            } else {
                columnIndex = (p.x < midPoint) ? column : column - 1;
            }
            if (columnIndex == -1) {
                return null;
            }
            return header.getColumnModel().getColumn(columnIndex);
        }
        public void mousePressed(MouseEvent e) {
            if (!header.isEnabled()) {
                return;
            }
            header.setDraggedColumn(null);
            header.setResizingColumn(null);
            header.setDraggedDistance(0);
            Point p = e.getPoint();
            TableColumnModel columnModel = header.getColumnModel();
            int index = header.columnAtPoint(p);
            if (index != -1) {
                TableColumn resizingColumn = getResizingColumn(p, index);
                if (canResize(resizingColumn, header)) {
                    header.setResizingColumn(resizingColumn);
                    if( header.getComponentOrientation().isLeftToRight() ) {
                        mouseXOffset = p.x - resizingColumn.getWidth();
                    } else {
                        mouseXOffset = p.x + resizingColumn.getWidth();
                    }
                }
                else if (header.getReorderingAllowed()) {
                    TableColumn hitColumn = columnModel.getColumn(index);
                    header.setDraggedColumn(hitColumn);
                    mouseXOffset = p.x;
                }
            }
            if (header.getReorderingAllowed()) {
                int oldRolloverColumn = rolloverColumn;
                rolloverColumn = -1;
                rolloverColumnUpdated(oldRolloverColumn, rolloverColumn);
            }
        }
        private void swapCursor() {
            Cursor tmp = header.getCursor();
            header.setCursor(otherCursor);
            otherCursor = tmp;
        }
        public void mouseMoved(MouseEvent e) {
            if (!header.isEnabled()) {
                return;
            }
            if (canResize(getResizingColumn(e.getPoint()), header) !=
                (header.getCursor() == resizeCursor)) {
                swapCursor();
            }
            updateRolloverColumn(e);
       }
        public void mouseDragged(MouseEvent e) {
            if (!header.isEnabled()) {
                return;
            }
            int mouseX = e.getX();
            TableColumn resizingColumn  = header.getResizingColumn();
            TableColumn draggedColumn  = header.getDraggedColumn();
            boolean headerLeftToRight = header.getComponentOrientation().isLeftToRight();
            if (resizingColumn != null) {
                int oldWidth = resizingColumn.getWidth();
                int newWidth;
                if (headerLeftToRight) {
                    newWidth = mouseX - mouseXOffset;
                } else  {
                    newWidth = mouseXOffset - mouseX;
                }
                mouseXOffset += changeColumnWidth(resizingColumn, header,
                                                  oldWidth, newWidth);
            }
            else if (draggedColumn != null) {
                TableColumnModel cm = header.getColumnModel();
                int draggedDistance = mouseX - mouseXOffset;
                int direction = (draggedDistance < 0) ? -1 : 1;
                int columnIndex = viewIndexForColumn(draggedColumn);
                int newColumnIndex = columnIndex + (headerLeftToRight ? direction : -direction);
                if (0 <= newColumnIndex && newColumnIndex < cm.getColumnCount()) {
                    int width = cm.getColumn(newColumnIndex).getWidth();
                    if (Math.abs(draggedDistance) > (width / 2)) {
                        mouseXOffset = mouseXOffset + direction * width;
                        header.setDraggedDistance(draggedDistance - direction * width);
                        int selectedIndex =
                                SwingUtilities2.convertColumnIndexToModel(
                                        header.getColumnModel(),
                                        getSelectedColumnIndex());
                        cm.moveColumn(columnIndex, newColumnIndex);
                        selectColumn(
                            SwingUtilities2.convertColumnIndexToView(
                                    header.getColumnModel(), selectedIndex),
                            false);
                        return;
                    }
                }
                setDraggedDistance(draggedDistance, columnIndex);
            }
            updateRolloverColumn(e);
        }
        public void mouseReleased(MouseEvent e) {
            if (!header.isEnabled()) {
                return;
            }
            setDraggedDistance(0, viewIndexForColumn(header.getDraggedColumn()));
            header.setResizingColumn(null);
            header.setDraggedColumn(null);
            updateRolloverColumn(e);
        }
        public void mouseEntered(MouseEvent e) {
            if (!header.isEnabled()) {
                return;
            }
            updateRolloverColumn(e);
        }
        public void mouseExited(MouseEvent e) {
            if (!header.isEnabled()) {
                return;
            }
            int oldRolloverColumn = rolloverColumn;
            rolloverColumn = -1;
            rolloverColumnUpdated(oldRolloverColumn, rolloverColumn);
        }
        private void setDraggedDistance(int draggedDistance, int column) {
            header.setDraggedDistance(draggedDistance);
            if (column != -1) {
                header.getColumnModel().moveColumn(column, column);
            }
        }
    }
    protected MouseInputListener createMouseInputListener() {
        return new MouseInputHandler();
    }
    public static ComponentUI createUI(JComponent h) {
        return new BasicTableHeaderUI();
    }
    public void installUI(JComponent c) {
        header = (JTableHeader)c;
        rendererPane = new CellRendererPane();
        header.add(rendererPane);
        installDefaults();
        installListeners();
        installKeyboardActions();
    }
    protected void installDefaults() {
        LookAndFeel.installColorsAndFont(header, "TableHeader.background",
                                         "TableHeader.foreground", "TableHeader.font");
        LookAndFeel.installProperty(header, "opaque", Boolean.TRUE);
    }
    protected void installListeners() {
        mouseInputListener = createMouseInputListener();
        header.addMouseListener(mouseInputListener);
        header.addMouseMotionListener(mouseInputListener);
        header.addFocusListener(focusListener);
    }
    protected void installKeyboardActions() {
        InputMap keyMap = (InputMap)DefaultLookup.get(header, this,
                "TableHeader.ancestorInputMap");
        SwingUtilities.replaceUIInputMap(header,
                                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, keyMap);
        LazyActionMap.installLazyActionMap(header, BasicTableHeaderUI.class,
                "TableHeader.actionMap");
    }
    public void uninstallUI(JComponent c) {
        uninstallDefaults();
        uninstallListeners();
        uninstallKeyboardActions();
        header.remove(rendererPane);
        rendererPane = null;
        header = null;
    }
    protected void uninstallDefaults() {}
    protected void uninstallListeners() {
        header.removeMouseListener(mouseInputListener);
        header.removeMouseMotionListener(mouseInputListener);
        mouseInputListener = null;
    }
    protected void uninstallKeyboardActions() {
        SwingUtilities.replaceUIInputMap(header, JComponent.WHEN_FOCUSED, null);
        SwingUtilities.replaceUIActionMap(header, null);
    }
    static void loadActionMap(LazyActionMap map) {
        map.put(new Actions(Actions.TOGGLE_SORT_ORDER));
        map.put(new Actions(Actions.SELECT_COLUMN_TO_LEFT));
        map.put(new Actions(Actions.SELECT_COLUMN_TO_RIGHT));
        map.put(new Actions(Actions.MOVE_COLUMN_LEFT));
        map.put(new Actions(Actions.MOVE_COLUMN_RIGHT));
        map.put(new Actions(Actions.RESIZE_LEFT));
        map.put(new Actions(Actions.RESIZE_RIGHT));
        map.put(new Actions(Actions.FOCUS_TABLE));
    }
    protected int getRolloverColumn() {
        return rolloverColumn;
    }
    protected void rolloverColumnUpdated(int oldColumn, int newColumn) {
    }
    private void updateRolloverColumn(MouseEvent e) {
        if (header.getDraggedColumn() == null &&
            header.contains(e.getPoint())) {
            int col = header.columnAtPoint(e.getPoint());
            if (col != rolloverColumn) {
                int oldRolloverColumn = rolloverColumn;
                rolloverColumn = col;
                rolloverColumnUpdated(oldRolloverColumn, rolloverColumn);
            }
        }
    }
    private int selectNextColumn(boolean doIt) {
        int newIndex = getSelectedColumnIndex();
        if (newIndex < header.getColumnModel().getColumnCount() - 1) {
            newIndex++;
            if (doIt) {
                selectColumn(newIndex);
            }
        }
        return newIndex;
    }
    private int selectPreviousColumn(boolean doIt) {
        int newIndex = getSelectedColumnIndex();
        if (newIndex > 0) {
            newIndex--;
            if (doIt) {
                selectColumn(newIndex);
            }
        }
        return newIndex;
    }
    void selectColumn(int newColIndex) {
        selectColumn(newColIndex, true);
    }
    void selectColumn(int newColIndex, boolean doScroll) {
        Rectangle repaintRect = header.getHeaderRect(selectedColumnIndex);
        header.repaint(repaintRect);
        selectedColumnIndex = newColIndex;
        repaintRect = header.getHeaderRect(newColIndex);
        header.repaint(repaintRect);
        if (doScroll) {
            scrollToColumn(newColIndex);
        }
        return;
    }
    private void scrollToColumn(int col) {
        Container container;
        JTable table;
        if ((header.getParent() == null) ||
            ((container = header.getParent().getParent()) == null) ||
            !(container instanceof JScrollPane) ||
            ((table = header.getTable()) == null)) {
            return;
        }
        Rectangle vis = table.getVisibleRect();
        Rectangle cellBounds = table.getCellRect(0, col, true);
        vis.x = cellBounds.x;
        vis.width = cellBounds.width;
        table.scrollRectToVisible(vis);
    }
    private int getSelectedColumnIndex() {
        int numCols = header.getColumnModel().getColumnCount();
        if (selectedColumnIndex >= numCols && numCols > 0) {
            selectedColumnIndex = numCols - 1;
        }
        return selectedColumnIndex;
    }
    private static boolean canResize(TableColumn column,
                                     JTableHeader header) {
        return (column != null) && header.getResizingAllowed()
                                && column.getResizable();
    }
    private int changeColumnWidth(TableColumn resizingColumn,
                                  JTableHeader th,
                                  int oldWidth, int newWidth) {
        resizingColumn.setWidth(newWidth);
        Container container;
        JTable table;
        if ((th.getParent() == null) ||
            ((container = th.getParent().getParent()) == null) ||
            !(container instanceof JScrollPane) ||
            ((table = th.getTable()) == null)) {
            return 0;
        }
        if (!container.getComponentOrientation().isLeftToRight() &&
                !th.getComponentOrientation().isLeftToRight()) {
                JViewport viewport = ((JScrollPane)container).getViewport();
                int viewportWidth = viewport.getWidth();
                int diff = newWidth - oldWidth;
                int newHeaderWidth = table.getWidth() + diff;
                Dimension tableSize = table.getSize();
                tableSize.width += diff;
                table.setSize(tableSize);
                if ((newHeaderWidth >= viewportWidth) &&
                    (table.getAutoResizeMode() == JTable.AUTO_RESIZE_OFF)) {
                    Point p = viewport.getViewPosition();
                    p.x = Math.max(0, Math.min(newHeaderWidth - viewportWidth,
                                               p.x + diff));
                    viewport.setViewPosition(p);
                    return diff;
            }
        }
        return 0;
    }
    public int getBaseline(JComponent c, int width, int height) {
        super.getBaseline(c, width, height);
        int baseline = -1;
        TableColumnModel columnModel = header.getColumnModel();
        for(int column = 0; column < columnModel.getColumnCount();
            column++) {
            TableColumn aColumn = columnModel.getColumn(column);
            Component comp = getHeaderRenderer(column);
            Dimension pref = comp.getPreferredSize();
            int columnBaseline = comp.getBaseline(pref.width, height);
            if (columnBaseline >= 0) {
                if (baseline == -1) {
                    baseline = columnBaseline;
                }
                else if (baseline != columnBaseline) {
                    baseline = -1;
                    break;
                }
            }
        }
        return baseline;
    }
    public void paint(Graphics g, JComponent c) {
        if (header.getColumnModel().getColumnCount() <= 0) {
            return;
        }
        boolean ltr = header.getComponentOrientation().isLeftToRight();
        Rectangle clip = g.getClipBounds();
        Point left = clip.getLocation();
        Point right = new Point( clip.x + clip.width - 1, clip.y );
        TableColumnModel cm = header.getColumnModel();
        int cMin = header.columnAtPoint( ltr ? left : right );
        int cMax = header.columnAtPoint( ltr ? right : left );
        if (cMin == -1) {
            cMin =  0;
        }
        if (cMax == -1) {
            cMax = cm.getColumnCount()-1;
        }
        TableColumn draggedColumn = header.getDraggedColumn();
        int columnWidth;
        Rectangle cellRect = header.getHeaderRect(ltr ? cMin : cMax);
        TableColumn aColumn;
        if (ltr) {
            for(int column = cMin; column <= cMax ; column++) {
                aColumn = cm.getColumn(column);
                columnWidth = aColumn.getWidth();
                cellRect.width = columnWidth;
                if (aColumn != draggedColumn) {
                    paintCell(g, cellRect, column);
                }
                cellRect.x += columnWidth;
            }
        } else {
            for(int column = cMax; column >= cMin; column--) {
                aColumn = cm.getColumn(column);
                columnWidth = aColumn.getWidth();
                cellRect.width = columnWidth;
                if (aColumn != draggedColumn) {
                    paintCell(g, cellRect, column);
                }
                cellRect.x += columnWidth;
            }
        }
        if (draggedColumn != null) {
            int draggedColumnIndex = viewIndexForColumn(draggedColumn);
            Rectangle draggedCellRect = header.getHeaderRect(draggedColumnIndex);
            g.setColor(header.getParent().getBackground());
            g.fillRect(draggedCellRect.x, draggedCellRect.y,
                               draggedCellRect.width, draggedCellRect.height);
            draggedCellRect.x += header.getDraggedDistance();
            g.setColor(header.getBackground());
            g.fillRect(draggedCellRect.x, draggedCellRect.y,
                       draggedCellRect.width, draggedCellRect.height);
            paintCell(g, draggedCellRect, draggedColumnIndex);
        }
        rendererPane.removeAll();
    }
    private Component getHeaderRenderer(int columnIndex) {
        TableColumn aColumn = header.getColumnModel().getColumn(columnIndex);
        TableCellRenderer renderer = aColumn.getHeaderRenderer();
        if (renderer == null) {
            renderer = header.getDefaultRenderer();
        }
        boolean hasFocus = !header.isPaintingForPrint()
                           && (columnIndex == getSelectedColumnIndex())
                           && header.hasFocus();
        return renderer.getTableCellRendererComponent(header.getTable(),
                                                aColumn.getHeaderValue(),
                                                false, hasFocus,
                                                -1, columnIndex);
    }
    private void paintCell(Graphics g, Rectangle cellRect, int columnIndex) {
        Component component = getHeaderRenderer(columnIndex);
        rendererPane.paintComponent(g, component, header, cellRect.x, cellRect.y,
                            cellRect.width, cellRect.height, true);
    }
    private int viewIndexForColumn(TableColumn aColumn) {
        TableColumnModel cm = header.getColumnModel();
        for (int column = 0; column < cm.getColumnCount(); column++) {
            if (cm.getColumn(column) == aColumn) {
                return column;
            }
        }
        return -1;
    }
    private int getHeaderHeight() {
        int height = 0;
        boolean accomodatedDefault = false;
        TableColumnModel columnModel = header.getColumnModel();
        for(int column = 0; column < columnModel.getColumnCount(); column++) {
            TableColumn aColumn = columnModel.getColumn(column);
            boolean isDefault = (aColumn.getHeaderRenderer() == null);
            if (!isDefault || !accomodatedDefault) {
                Component comp = getHeaderRenderer(column);
                int rendererHeight = comp.getPreferredSize().height;
                height = Math.max(height, rendererHeight);
                if (isDefault && rendererHeight > 0) {
                    Object headerValue = aColumn.getHeaderValue();
                    if (headerValue != null) {
                        headerValue = headerValue.toString();
                        if (headerValue != null && !headerValue.equals("")) {
                            accomodatedDefault = true;
                        }
                    }
                }
            }
        }
        return height;
    }
    private Dimension createHeaderSize(long width) {
        if (width > Integer.MAX_VALUE) {
            width = Integer.MAX_VALUE;
        }
        return new Dimension((int)width, getHeaderHeight());
    }
    public Dimension getMinimumSize(JComponent c) {
        long width = 0;
        Enumeration enumeration = header.getColumnModel().getColumns();
        while (enumeration.hasMoreElements()) {
            TableColumn aColumn = (TableColumn)enumeration.nextElement();
            width = width + aColumn.getMinWidth();
        }
        return createHeaderSize(width);
    }
    public Dimension getPreferredSize(JComponent c) {
        long width = 0;
        Enumeration enumeration = header.getColumnModel().getColumns();
        while (enumeration.hasMoreElements()) {
            TableColumn aColumn = (TableColumn)enumeration.nextElement();
            width = width + aColumn.getPreferredWidth();
        }
        return createHeaderSize(width);
    }
    public Dimension getMaximumSize(JComponent c) {
        long width = 0;
        Enumeration enumeration = header.getColumnModel().getColumns();
        while (enumeration.hasMoreElements()) {
            TableColumn aColumn = (TableColumn)enumeration.nextElement();
            width = width + aColumn.getMaxWidth();
        }
        return createHeaderSize(width);
    }
    private static class Actions extends UIAction {
        public static final String TOGGLE_SORT_ORDER =
            "toggleSortOrder";
        public static final String SELECT_COLUMN_TO_LEFT =
            "selectColumnToLeft";
        public static final String SELECT_COLUMN_TO_RIGHT =
            "selectColumnToRight";
        public static final String MOVE_COLUMN_LEFT =
            "moveColumnLeft";
        public static final String MOVE_COLUMN_RIGHT =
            "moveColumnRight";
        public static final String RESIZE_LEFT =
            "resizeLeft";
        public static final String RESIZE_RIGHT =
            "resizeRight";
        public static final String FOCUS_TABLE =
            "focusTable";
        public Actions(String name) {
            super(name);
        }
        public boolean isEnabled(Object sender) {
            if (sender instanceof JTableHeader) {
                JTableHeader th = (JTableHeader)sender;
                TableColumnModel cm = th.getColumnModel();
                if (cm.getColumnCount() <= 0) {
                    return false;
                }
                String key = getName();
                BasicTableHeaderUI ui =
                    (BasicTableHeaderUI)BasicLookAndFeel.getUIOfType(th.getUI(),
                                                      BasicTableHeaderUI.class);
                if (ui != null) {
                    if (key == MOVE_COLUMN_LEFT) {
                        return th.getReorderingAllowed()
                            && maybeMoveColumn(true, th, ui, false);
                    } else if (key == MOVE_COLUMN_RIGHT) {
                        return th.getReorderingAllowed()
                            && maybeMoveColumn(false, th, ui, false);
                    } else if (key == RESIZE_LEFT ||
                               key == RESIZE_RIGHT) {
                        return canResize(cm.getColumn(ui.getSelectedColumnIndex()), th);
                    } else if (key == FOCUS_TABLE) {
                        return (th.getTable() != null);
                    }
                }
            }
            return true;
        }
        public void actionPerformed(ActionEvent e) {
            JTableHeader th = (JTableHeader)e.getSource();
            BasicTableHeaderUI ui =
                (BasicTableHeaderUI)BasicLookAndFeel.
                                        getUIOfType(th.getUI(),
                                            BasicTableHeaderUI.class);
            if (ui == null) {
                return;
            }
            String name = getName();
            if (TOGGLE_SORT_ORDER == name) {
                JTable table = th.getTable();
                RowSorter sorter = table == null ? null : table.getRowSorter();
                if (sorter != null) {
                    int columnIndex = ui.getSelectedColumnIndex();
                    columnIndex = table.convertColumnIndexToModel(
                                                      columnIndex);
                    sorter.toggleSortOrder(columnIndex);
                }
            } else if (SELECT_COLUMN_TO_LEFT == name) {
                if (th.getComponentOrientation().isLeftToRight()) {
                    ui.selectPreviousColumn(true);
                } else {
                    ui.selectNextColumn(true);
                }
            } else if (SELECT_COLUMN_TO_RIGHT == name) {
                if (th.getComponentOrientation().isLeftToRight()) {
                    ui.selectNextColumn(true);
                } else {
                    ui.selectPreviousColumn(true);
                }
            } else if (MOVE_COLUMN_LEFT == name) {
                moveColumn(true, th, ui);
            } else if (MOVE_COLUMN_RIGHT == name) {
                moveColumn(false, th, ui);
            } else if (RESIZE_LEFT == name) {
                resize(true, th, ui);
            } else if (RESIZE_RIGHT == name) {
                resize(false, th, ui);
            } else if (FOCUS_TABLE == name) {
                JTable table = th.getTable();
                if (table != null) {
                    table.requestFocusInWindow();
                }
            }
        }
        private void moveColumn(boolean leftArrow, JTableHeader th,
                                BasicTableHeaderUI ui) {
            maybeMoveColumn(leftArrow, th, ui, true);
        }
        private boolean maybeMoveColumn(boolean leftArrow, JTableHeader th,
                                        BasicTableHeaderUI ui, boolean doIt) {
            int oldIndex = ui.getSelectedColumnIndex();
            int newIndex;
            if (th.getComponentOrientation().isLeftToRight()) {
                newIndex = leftArrow ? ui.selectPreviousColumn(doIt)
                                     : ui.selectNextColumn(doIt);
            } else {
                newIndex = leftArrow ? ui.selectNextColumn(doIt)
                                     : ui.selectPreviousColumn(doIt);
            }
            if (newIndex != oldIndex) {
                if (doIt) {
                    th.getColumnModel().moveColumn(oldIndex, newIndex);
                } else {
                    return true; 
                }
            }
            return false;
        }
        private void resize(boolean leftArrow, JTableHeader th,
                            BasicTableHeaderUI ui) {
            int columnIndex = ui.getSelectedColumnIndex();
            TableColumn resizingColumn =
                th.getColumnModel().getColumn(columnIndex);
            th.setResizingColumn(resizingColumn);
            int oldWidth = resizingColumn.getWidth();
            int newWidth = oldWidth;
            if (th.getComponentOrientation().isLeftToRight()) {
                newWidth = newWidth + (leftArrow ? -1 : 1);
            } else {
                newWidth = newWidth + (leftArrow ? 1 : -1);
            }
            ui.changeColumnWidth(resizingColumn, th, oldWidth, newWidth);
        }
    }
}  
