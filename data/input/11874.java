public class BasicListUI extends ListUI
{
    private static final StringBuilder BASELINE_COMPONENT_KEY =
        new StringBuilder("List.baselineComponent");
    protected JList list = null;
    protected CellRendererPane rendererPane;
    protected FocusListener focusListener;
    protected MouseInputListener mouseInputListener;
    protected ListSelectionListener listSelectionListener;
    protected ListDataListener listDataListener;
    protected PropertyChangeListener propertyChangeListener;
    private Handler handler;
    protected int[] cellHeights = null;
    protected int cellHeight = -1;
    protected int cellWidth = -1;
    protected int updateLayoutStateNeeded = modelChanged;
    private int listHeight;
    private int listWidth;
    private int layoutOrientation;
    private int columnCount;
    private int preferredHeight;
    private int rowsPerColumn;
    private long timeFactor = 1000L;
    private boolean isFileList = false;
    private boolean isLeftToRight = true;
    protected final static int modelChanged = 1 << 0;
    protected final static int selectionModelChanged = 1 << 1;
    protected final static int fontChanged = 1 << 2;
    protected final static int fixedCellWidthChanged = 1 << 3;
    protected final static int fixedCellHeightChanged = 1 << 4;
    protected final static int prototypeCellValueChanged = 1 << 5;
    protected final static int cellRendererChanged = 1 << 6;
    private final static int layoutOrientationChanged = 1 << 7;
    private final static int heightChanged = 1 << 8;
    private final static int widthChanged = 1 << 9;
    private final static int componentOrientationChanged = 1 << 10;
    private static final int DROP_LINE_THICKNESS = 2;
    static void loadActionMap(LazyActionMap map) {
        map.put(new Actions(Actions.SELECT_PREVIOUS_COLUMN));
        map.put(new Actions(Actions.SELECT_PREVIOUS_COLUMN_EXTEND));
        map.put(new Actions(Actions.SELECT_PREVIOUS_COLUMN_CHANGE_LEAD));
        map.put(new Actions(Actions.SELECT_NEXT_COLUMN));
        map.put(new Actions(Actions.SELECT_NEXT_COLUMN_EXTEND));
        map.put(new Actions(Actions.SELECT_NEXT_COLUMN_CHANGE_LEAD));
        map.put(new Actions(Actions.SELECT_PREVIOUS_ROW));
        map.put(new Actions(Actions.SELECT_PREVIOUS_ROW_EXTEND));
        map.put(new Actions(Actions.SELECT_PREVIOUS_ROW_CHANGE_LEAD));
        map.put(new Actions(Actions.SELECT_NEXT_ROW));
        map.put(new Actions(Actions.SELECT_NEXT_ROW_EXTEND));
        map.put(new Actions(Actions.SELECT_NEXT_ROW_CHANGE_LEAD));
        map.put(new Actions(Actions.SELECT_FIRST_ROW));
        map.put(new Actions(Actions.SELECT_FIRST_ROW_EXTEND));
        map.put(new Actions(Actions.SELECT_FIRST_ROW_CHANGE_LEAD));
        map.put(new Actions(Actions.SELECT_LAST_ROW));
        map.put(new Actions(Actions.SELECT_LAST_ROW_EXTEND));
        map.put(new Actions(Actions.SELECT_LAST_ROW_CHANGE_LEAD));
        map.put(new Actions(Actions.SCROLL_UP));
        map.put(new Actions(Actions.SCROLL_UP_EXTEND));
        map.put(new Actions(Actions.SCROLL_UP_CHANGE_LEAD));
        map.put(new Actions(Actions.SCROLL_DOWN));
        map.put(new Actions(Actions.SCROLL_DOWN_EXTEND));
        map.put(new Actions(Actions.SCROLL_DOWN_CHANGE_LEAD));
        map.put(new Actions(Actions.SELECT_ALL));
        map.put(new Actions(Actions.CLEAR_SELECTION));
        map.put(new Actions(Actions.ADD_TO_SELECTION));
        map.put(new Actions(Actions.TOGGLE_AND_ANCHOR));
        map.put(new Actions(Actions.EXTEND_TO));
        map.put(new Actions(Actions.MOVE_SELECTION_TO));
        map.put(TransferHandler.getCutAction().getValue(Action.NAME),
                TransferHandler.getCutAction());
        map.put(TransferHandler.getCopyAction().getValue(Action.NAME),
                TransferHandler.getCopyAction());
        map.put(TransferHandler.getPasteAction().getValue(Action.NAME),
                TransferHandler.getPasteAction());
    }
    protected void paintCell(
        Graphics g,
        int row,
        Rectangle rowBounds,
        ListCellRenderer cellRenderer,
        ListModel dataModel,
        ListSelectionModel selModel,
        int leadIndex)
    {
        Object value = dataModel.getElementAt(row);
        boolean cellHasFocus = list.hasFocus() && (row == leadIndex);
        boolean isSelected = selModel.isSelectedIndex(row);
        Component rendererComponent =
            cellRenderer.getListCellRendererComponent(list, value, row, isSelected, cellHasFocus);
        int cx = rowBounds.x;
        int cy = rowBounds.y;
        int cw = rowBounds.width;
        int ch = rowBounds.height;
        if (isFileList) {
            int w = Math.min(cw, rendererComponent.getPreferredSize().width + 4);
            if (!isLeftToRight) {
                cx += (cw - w);
            }
            cw = w;
        }
        rendererPane.paintComponent(g, rendererComponent, list, cx, cy, cw, ch, true);
    }
    public void paint(Graphics g, JComponent c) {
        Shape clip = g.getClip();
        paintImpl(g, c);
        g.setClip(clip);
        paintDropLine(g);
    }
    private void paintImpl(Graphics g, JComponent c)
    {
        switch (layoutOrientation) {
        case JList.VERTICAL_WRAP:
            if (list.getHeight() != listHeight) {
                updateLayoutStateNeeded |= heightChanged;
                redrawList();
            }
            break;
        case JList.HORIZONTAL_WRAP:
            if (list.getWidth() != listWidth) {
                updateLayoutStateNeeded |= widthChanged;
                redrawList();
            }
            break;
        default:
            break;
        }
        maybeUpdateLayoutState();
        ListCellRenderer renderer = list.getCellRenderer();
        ListModel dataModel = list.getModel();
        ListSelectionModel selModel = list.getSelectionModel();
        int size;
        if ((renderer == null) || (size = dataModel.getSize()) == 0) {
            return;
        }
        Rectangle paintBounds = g.getClipBounds();
        int startColumn, endColumn;
        if (c.getComponentOrientation().isLeftToRight()) {
            startColumn = convertLocationToColumn(paintBounds.x,
                                                  paintBounds.y);
            endColumn = convertLocationToColumn(paintBounds.x +
                                                paintBounds.width,
                                                paintBounds.y);
        } else {
            startColumn = convertLocationToColumn(paintBounds.x +
                                                paintBounds.width,
                                                paintBounds.y);
            endColumn = convertLocationToColumn(paintBounds.x,
                                                  paintBounds.y);
        }
        int maxY = paintBounds.y + paintBounds.height;
        int leadIndex = adjustIndex(list.getLeadSelectionIndex(), list);
        int rowIncrement = (layoutOrientation == JList.HORIZONTAL_WRAP) ?
                           columnCount : 1;
        for (int colCounter = startColumn; colCounter <= endColumn;
             colCounter++) {
            int row = convertLocationToRowInColumn(paintBounds.y, colCounter);
            int rowCount = getRowCount(colCounter);
            int index = getModelIndex(colCounter, row);
            Rectangle rowBounds = getCellBounds(list, index, index);
            if (rowBounds == null) {
                return;
            }
            while (row < rowCount && rowBounds.y < maxY &&
                   index < size) {
                rowBounds.height = getHeight(colCounter, row);
                g.setClip(rowBounds.x, rowBounds.y, rowBounds.width,
                          rowBounds.height);
                g.clipRect(paintBounds.x, paintBounds.y, paintBounds.width,
                           paintBounds.height);
                paintCell(g, index, rowBounds, renderer, dataModel, selModel,
                          leadIndex);
                rowBounds.y += rowBounds.height;
                index += rowIncrement;
                row++;
            }
        }
        rendererPane.removeAll();
    }
    private void paintDropLine(Graphics g) {
        JList.DropLocation loc = list.getDropLocation();
        if (loc == null || !loc.isInsert()) {
            return;
        }
        Color c = DefaultLookup.getColor(list, this, "List.dropLineColor", null);
        if (c != null) {
            g.setColor(c);
            Rectangle rect = getDropLineRect(loc);
            g.fillRect(rect.x, rect.y, rect.width, rect.height);
        }
    }
    private Rectangle getDropLineRect(JList.DropLocation loc) {
        int size = list.getModel().getSize();
        if (size == 0) {
            Insets insets = list.getInsets();
            if (layoutOrientation == JList.HORIZONTAL_WRAP) {
                if (isLeftToRight) {
                    return new Rectangle(insets.left, insets.top, DROP_LINE_THICKNESS, 20);
                } else {
                    return new Rectangle(list.getWidth() - DROP_LINE_THICKNESS - insets.right,
                                         insets.top, DROP_LINE_THICKNESS, 20);
                }
            } else {
                return new Rectangle(insets.left, insets.top,
                                     list.getWidth() - insets.left - insets.right,
                                     DROP_LINE_THICKNESS);
            }
        }
        Rectangle rect = null;
        int index = loc.getIndex();
        boolean decr = false;
        if (layoutOrientation == JList.HORIZONTAL_WRAP) {
            if (index == size) {
                decr = true;
            } else if (index != 0 && convertModelToRow(index)
                                         != convertModelToRow(index - 1)) {
                Rectangle prev = getCellBounds(list, index - 1);
                Rectangle me = getCellBounds(list, index);
                Point p = loc.getDropPoint();
                if (isLeftToRight) {
                    decr = Point2D.distance(prev.x + prev.width,
                                            prev.y + (int)(prev.height / 2.0),
                                            p.x, p.y)
                           < Point2D.distance(me.x,
                                              me.y + (int)(me.height / 2.0),
                                              p.x, p.y);
                } else {
                    decr = Point2D.distance(prev.x,
                                            prev.y + (int)(prev.height / 2.0),
                                            p.x, p.y)
                           < Point2D.distance(me.x + me.width,
                                              me.y + (int)(prev.height / 2.0),
                                              p.x, p.y);
                }
            }
            if (decr) {
                index--;
                rect = getCellBounds(list, index);
                if (isLeftToRight) {
                    rect.x += rect.width;
                } else {
                    rect.x -= DROP_LINE_THICKNESS;
                }
            } else {
                rect = getCellBounds(list, index);
                if (!isLeftToRight) {
                    rect.x += rect.width - DROP_LINE_THICKNESS;
                }
            }
            if (rect.x >= list.getWidth()) {
                rect.x = list.getWidth() - DROP_LINE_THICKNESS;
            } else if (rect.x < 0) {
                rect.x = 0;
            }
            rect.width = DROP_LINE_THICKNESS;
        } else if (layoutOrientation == JList.VERTICAL_WRAP) {
            if (index == size) {
                index--;
                rect = getCellBounds(list, index);
                rect.y += rect.height;
            } else if (index != 0 && convertModelToColumn(index)
                                         != convertModelToColumn(index - 1)) {
                Rectangle prev = getCellBounds(list, index - 1);
                Rectangle me = getCellBounds(list, index);
                Point p = loc.getDropPoint();
                if (Point2D.distance(prev.x + (int)(prev.width / 2.0),
                                     prev.y + prev.height,
                                     p.x, p.y)
                        < Point2D.distance(me.x + (int)(me.width / 2.0),
                                           me.y,
                                           p.x, p.y)) {
                    index--;
                    rect = getCellBounds(list, index);
                    rect.y += rect.height;
                } else {
                    rect = getCellBounds(list, index);
                }
            } else {
                rect = getCellBounds(list, index);
            }
            if (rect.y >= list.getHeight()) {
                rect.y = list.getHeight() - DROP_LINE_THICKNESS;
            }
            rect.height = DROP_LINE_THICKNESS;
        } else {
            if (index == size) {
                index--;
                rect = getCellBounds(list, index);
                rect.y += rect.height;
            } else {
                rect = getCellBounds(list, index);
            }
            if (rect.y >= list.getHeight()) {
                rect.y = list.getHeight() - DROP_LINE_THICKNESS;
            }
            rect.height = DROP_LINE_THICKNESS;
        }
        return rect;
    }
    public int getBaseline(JComponent c, int width, int height) {
        super.getBaseline(c, width, height);
        int rowHeight = list.getFixedCellHeight();
        UIDefaults lafDefaults = UIManager.getLookAndFeelDefaults();
        Component renderer = (Component)lafDefaults.get(
                BASELINE_COMPONENT_KEY);
        if (renderer == null) {
            ListCellRenderer lcr = (ListCellRenderer)UIManager.get(
                    "List.cellRenderer");
            if (lcr == null) {
                lcr = new DefaultListCellRenderer();
            }
            renderer = lcr.getListCellRendererComponent(
                    list, "a", -1, false, false);
            lafDefaults.put(BASELINE_COMPONENT_KEY, renderer);
        }
        renderer.setFont(list.getFont());
        if (rowHeight == -1) {
            rowHeight = renderer.getPreferredSize().height;
        }
        return renderer.getBaseline(Integer.MAX_VALUE, rowHeight) +
                list.getInsets().top;
    }
    public Component.BaselineResizeBehavior getBaselineResizeBehavior(
            JComponent c) {
        super.getBaselineResizeBehavior(c);
        return Component.BaselineResizeBehavior.CONSTANT_ASCENT;
    }
    public Dimension getPreferredSize(JComponent c) {
        maybeUpdateLayoutState();
        int lastRow = list.getModel().getSize() - 1;
        if (lastRow < 0) {
            return new Dimension(0, 0);
        }
        Insets insets = list.getInsets();
        int width = cellWidth * columnCount + insets.left + insets.right;
        int height;
        if (layoutOrientation != JList.VERTICAL) {
            height = preferredHeight;
        }
        else {
            Rectangle bounds = getCellBounds(list, lastRow);
            if (bounds != null) {
                height = bounds.y + bounds.height + insets.bottom;
            }
            else {
                height = 0;
            }
        }
        return new Dimension(width, height);
    }
    protected void selectPreviousIndex() {
        int s = list.getSelectedIndex();
        if(s > 0) {
            s -= 1;
            list.setSelectedIndex(s);
            list.ensureIndexIsVisible(s);
        }
    }
    protected void selectNextIndex()
    {
        int s = list.getSelectedIndex();
        if((s + 1) < list.getModel().getSize()) {
            s += 1;
            list.setSelectedIndex(s);
            list.ensureIndexIsVisible(s);
        }
    }
    protected void installKeyboardActions() {
        InputMap inputMap = getInputMap(JComponent.WHEN_FOCUSED);
        SwingUtilities.replaceUIInputMap(list, JComponent.WHEN_FOCUSED,
                                           inputMap);
        LazyActionMap.installLazyActionMap(list, BasicListUI.class,
                                           "List.actionMap");
    }
    InputMap getInputMap(int condition) {
        if (condition == JComponent.WHEN_FOCUSED) {
            InputMap keyMap = (InputMap)DefaultLookup.get(
                             list, this, "List.focusInputMap");
            InputMap rtlKeyMap;
            if (isLeftToRight ||
                ((rtlKeyMap = (InputMap)DefaultLookup.get(list, this,
                              "List.focusInputMap.RightToLeft")) == null)) {
                    return keyMap;
            } else {
                rtlKeyMap.setParent(keyMap);
                return rtlKeyMap;
            }
        }
        return null;
    }
    protected void uninstallKeyboardActions() {
        SwingUtilities.replaceUIActionMap(list, null);
        SwingUtilities.replaceUIInputMap(list, JComponent.WHEN_FOCUSED, null);
    }
    protected void installListeners()
    {
        TransferHandler th = list.getTransferHandler();
        if (th == null || th instanceof UIResource) {
            list.setTransferHandler(defaultTransferHandler);
            if (list.getDropTarget() instanceof UIResource) {
                list.setDropTarget(null);
            }
        }
        focusListener = createFocusListener();
        mouseInputListener = createMouseInputListener();
        propertyChangeListener = createPropertyChangeListener();
        listSelectionListener = createListSelectionListener();
        listDataListener = createListDataListener();
        list.addFocusListener(focusListener);
        list.addMouseListener(mouseInputListener);
        list.addMouseMotionListener(mouseInputListener);
        list.addPropertyChangeListener(propertyChangeListener);
        list.addKeyListener(getHandler());
        ListModel model = list.getModel();
        if (model != null) {
            model.addListDataListener(listDataListener);
        }
        ListSelectionModel selectionModel = list.getSelectionModel();
        if (selectionModel != null) {
            selectionModel.addListSelectionListener(listSelectionListener);
        }
    }
    protected void uninstallListeners()
    {
        list.removeFocusListener(focusListener);
        list.removeMouseListener(mouseInputListener);
        list.removeMouseMotionListener(mouseInputListener);
        list.removePropertyChangeListener(propertyChangeListener);
        list.removeKeyListener(getHandler());
        ListModel model = list.getModel();
        if (model != null) {
            model.removeListDataListener(listDataListener);
        }
        ListSelectionModel selectionModel = list.getSelectionModel();
        if (selectionModel != null) {
            selectionModel.removeListSelectionListener(listSelectionListener);
        }
        focusListener = null;
        mouseInputListener  = null;
        listSelectionListener = null;
        listDataListener = null;
        propertyChangeListener = null;
        handler = null;
    }
    protected void installDefaults()
    {
        list.setLayout(null);
        LookAndFeel.installBorder(list, "List.border");
        LookAndFeel.installColorsAndFont(list, "List.background", "List.foreground", "List.font");
        LookAndFeel.installProperty(list, "opaque", Boolean.TRUE);
        if (list.getCellRenderer() == null) {
            list.setCellRenderer((ListCellRenderer)(UIManager.get("List.cellRenderer")));
        }
        Color sbg = list.getSelectionBackground();
        if (sbg == null || sbg instanceof UIResource) {
            list.setSelectionBackground(UIManager.getColor("List.selectionBackground"));
        }
        Color sfg = list.getSelectionForeground();
        if (sfg == null || sfg instanceof UIResource) {
            list.setSelectionForeground(UIManager.getColor("List.selectionForeground"));
        }
        Long l = (Long)UIManager.get("List.timeFactor");
        timeFactor = (l!=null) ? l.longValue() : 1000L;
        updateIsFileList();
    }
    private void updateIsFileList() {
        boolean b = Boolean.TRUE.equals(list.getClientProperty("List.isFileList"));
        if (b != isFileList) {
            isFileList = b;
            Font oldFont = list.getFont();
            if (oldFont == null || oldFont instanceof UIResource) {
                Font newFont = UIManager.getFont(b ? "FileChooser.listFont" : "List.font");
                if (newFont != null && newFont != oldFont) {
                    list.setFont(newFont);
                }
            }
        }
    }
    protected void uninstallDefaults()
    {
        LookAndFeel.uninstallBorder(list);
        if (list.getFont() instanceof UIResource) {
            list.setFont(null);
        }
        if (list.getForeground() instanceof UIResource) {
            list.setForeground(null);
        }
        if (list.getBackground() instanceof UIResource) {
            list.setBackground(null);
        }
        if (list.getSelectionBackground() instanceof UIResource) {
            list.setSelectionBackground(null);
        }
        if (list.getSelectionForeground() instanceof UIResource) {
            list.setSelectionForeground(null);
        }
        if (list.getCellRenderer() instanceof UIResource) {
            list.setCellRenderer(null);
        }
        if (list.getTransferHandler() instanceof UIResource) {
            list.setTransferHandler(null);
        }
    }
    public void installUI(JComponent c)
    {
        list = (JList)c;
        layoutOrientation = list.getLayoutOrientation();
        rendererPane = new CellRendererPane();
        list.add(rendererPane);
        columnCount = 1;
        updateLayoutStateNeeded = modelChanged;
        isLeftToRight = list.getComponentOrientation().isLeftToRight();
        installDefaults();
        installListeners();
        installKeyboardActions();
    }
    public void uninstallUI(JComponent c)
    {
        uninstallListeners();
        uninstallDefaults();
        uninstallKeyboardActions();
        cellWidth = cellHeight = -1;
        cellHeights = null;
        listWidth = listHeight = -1;
        list.remove(rendererPane);
        rendererPane = null;
        list = null;
    }
    public static ComponentUI createUI(JComponent list) {
        return new BasicListUI();
    }
    public int locationToIndex(JList list, Point location) {
        maybeUpdateLayoutState();
        return convertLocationToModel(location.x, location.y);
    }
    public Point indexToLocation(JList list, int index) {
        maybeUpdateLayoutState();
        Rectangle rect = getCellBounds(list, index, index);
        if (rect != null) {
            return new Point(rect.x, rect.y);
        }
        return null;
    }
    public Rectangle getCellBounds(JList list, int index1, int index2) {
        maybeUpdateLayoutState();
        int minIndex = Math.min(index1, index2);
        int maxIndex = Math.max(index1, index2);
        if (minIndex >= list.getModel().getSize()) {
            return null;
        }
        Rectangle minBounds = getCellBounds(list, minIndex);
        if (minBounds == null) {
            return null;
        }
        if (minIndex == maxIndex) {
            return minBounds;
        }
        Rectangle maxBounds = getCellBounds(list, maxIndex);
        if (maxBounds != null) {
            if (layoutOrientation == JList.HORIZONTAL_WRAP) {
                int minRow = convertModelToRow(minIndex);
                int maxRow = convertModelToRow(maxIndex);
                if (minRow != maxRow) {
                    minBounds.x = 0;
                    minBounds.width = list.getWidth();
                }
            }
            else if (minBounds.x != maxBounds.x) {
                minBounds.y = 0;
                minBounds.height = list.getHeight();
            }
            minBounds.add(maxBounds);
        }
        return minBounds;
    }
    private Rectangle getCellBounds(JList list, int index) {
        maybeUpdateLayoutState();
        int row = convertModelToRow(index);
        int column = convertModelToColumn(index);
        if (row == -1 || column == -1) {
            return null;
        }
        Insets insets = list.getInsets();
        int x;
        int w = cellWidth;
        int y = insets.top;
        int h;
        switch (layoutOrientation) {
        case JList.VERTICAL_WRAP:
        case JList.HORIZONTAL_WRAP:
            if (isLeftToRight) {
                x = insets.left + column * cellWidth;
            } else {
                x = list.getWidth() - insets.right - (column+1) * cellWidth;
            }
            y += cellHeight * row;
            h = cellHeight;
            break;
        default:
            x = insets.left;
            if (cellHeights == null) {
                y += (cellHeight * row);
            }
            else if (row >= cellHeights.length) {
                y = 0;
            }
            else {
                for(int i = 0; i < row; i++) {
                    y += cellHeights[i];
                }
            }
            w = list.getWidth() - (insets.left + insets.right);
            h = getRowHeight(index);
            break;
        }
        return new Rectangle(x, y, w, h);
    }
    protected int getRowHeight(int row)
    {
        return getHeight(0, row);
    }
    protected int convertYToRow(int y0)
    {
        return convertLocationToRow(0, y0, false);
    }
    protected int convertRowToY(int row)
    {
        if (row >= getRowCount(0) || row < 0) {
            return -1;
        }
        Rectangle bounds = getCellBounds(list, row, row);
        return bounds.y;
    }
    private int getHeight(int column, int row) {
        if (column < 0 || column > columnCount || row < 0) {
            return -1;
        }
        if (layoutOrientation != JList.VERTICAL) {
            return cellHeight;
        }
        if (row >= list.getModel().getSize()) {
            return -1;
        }
        return (cellHeights == null) ? cellHeight :
                           ((row < cellHeights.length) ? cellHeights[row] : -1);
    }
    private int convertLocationToRow(int x, int y0, boolean closest) {
        int size = list.getModel().getSize();
        if (size <= 0) {
            return -1;
        }
        Insets insets = list.getInsets();
        if (cellHeights == null) {
            int row = (cellHeight == 0) ? 0 :
                           ((y0 - insets.top) / cellHeight);
            if (closest) {
                if (row < 0) {
                    row = 0;
                }
                else if (row >= size) {
                    row = size - 1;
                }
            }
            return row;
        }
        else if (size > cellHeights.length) {
            return -1;
        }
        else {
            int y = insets.top;
            int row = 0;
            if (closest && y0 < y) {
                return 0;
            }
            int i;
            for (i = 0; i < size; i++) {
                if ((y0 >= y) && (y0 < y + cellHeights[i])) {
                    return row;
                }
                y += cellHeights[i];
                row += 1;
            }
            return i - 1;
        }
    }
    private int convertLocationToRowInColumn(int y, int column) {
        int x = 0;
        if (layoutOrientation != JList.VERTICAL) {
            if (isLeftToRight) {
                x = column * cellWidth;
            } else {
                x = list.getWidth() - (column+1)*cellWidth - list.getInsets().right;
            }
        }
        return convertLocationToRow(x, y, true);
    }
    private int convertLocationToModel(int x, int y) {
        int row = convertLocationToRow(x, y, true);
        int column = convertLocationToColumn(x, y);
        if (row >= 0 && column >= 0) {
            return getModelIndex(column, row);
        }
        return -1;
    }
    private int getRowCount(int column) {
        if (column < 0 || column >= columnCount) {
            return -1;
        }
        if (layoutOrientation == JList.VERTICAL ||
                  (column == 0 && columnCount == 1)) {
            return list.getModel().getSize();
        }
        if (column >= columnCount) {
            return -1;
        }
        if (layoutOrientation == JList.VERTICAL_WRAP) {
            if (column < (columnCount - 1)) {
                return rowsPerColumn;
            }
            return list.getModel().getSize() - (columnCount - 1) *
                        rowsPerColumn;
        }
        int diff = columnCount - (columnCount * rowsPerColumn -
                                  list.getModel().getSize());
        if (column >= diff) {
            return Math.max(0, rowsPerColumn - 1);
        }
        return rowsPerColumn;
    }
    private int getModelIndex(int column, int row) {
        switch (layoutOrientation) {
        case JList.VERTICAL_WRAP:
            return Math.min(list.getModel().getSize() - 1, rowsPerColumn *
                            column + Math.min(row, rowsPerColumn-1));
        case JList.HORIZONTAL_WRAP:
            return Math.min(list.getModel().getSize() - 1, row * columnCount +
                            column);
        default:
            return row;
        }
    }
    private int convertLocationToColumn(int x, int y) {
        if (cellWidth > 0) {
            if (layoutOrientation == JList.VERTICAL) {
                return 0;
            }
            Insets insets = list.getInsets();
            int col;
            if (isLeftToRight) {
                col = (x - insets.left) / cellWidth;
            } else {
                col = (list.getWidth() - x - insets.right - 1) / cellWidth;
            }
            if (col < 0) {
                return 0;
            }
            else if (col >= columnCount) {
                return columnCount - 1;
            }
            return col;
        }
        return 0;
    }
    private int convertModelToRow(int index) {
        int size = list.getModel().getSize();
        if ((index < 0) || (index >= size)) {
            return -1;
        }
        if (layoutOrientation != JList.VERTICAL && columnCount > 1 &&
                                                   rowsPerColumn > 0) {
            if (layoutOrientation == JList.VERTICAL_WRAP) {
                return index % rowsPerColumn;
            }
            return index / columnCount;
        }
        return index;
    }
    private int convertModelToColumn(int index) {
        int size = list.getModel().getSize();
        if ((index < 0) || (index >= size)) {
            return -1;
        }
        if (layoutOrientation != JList.VERTICAL && rowsPerColumn > 0 &&
                                                   columnCount > 1) {
            if (layoutOrientation == JList.VERTICAL_WRAP) {
                return index / rowsPerColumn;
            }
            return index % columnCount;
        }
        return 0;
    }
    protected void maybeUpdateLayoutState()
    {
        if (updateLayoutStateNeeded != 0) {
            updateLayoutState();
            updateLayoutStateNeeded = 0;
        }
    }
    protected void updateLayoutState()
    {
        int fixedCellHeight = list.getFixedCellHeight();
        int fixedCellWidth = list.getFixedCellWidth();
        cellWidth = (fixedCellWidth != -1) ? fixedCellWidth : -1;
        if (fixedCellHeight != -1) {
            cellHeight = fixedCellHeight;
            cellHeights = null;
        }
        else {
            cellHeight = -1;
            cellHeights = new int[list.getModel().getSize()];
        }
        if ((fixedCellWidth == -1) || (fixedCellHeight == -1)) {
            ListModel dataModel = list.getModel();
            int dataModelSize = dataModel.getSize();
            ListCellRenderer renderer = list.getCellRenderer();
            if (renderer != null) {
                for(int index = 0; index < dataModelSize; index++) {
                    Object value = dataModel.getElementAt(index);
                    Component c = renderer.getListCellRendererComponent(list, value, index, false, false);
                    rendererPane.add(c);
                    Dimension cellSize = c.getPreferredSize();
                    if (fixedCellWidth == -1) {
                        cellWidth = Math.max(cellSize.width, cellWidth);
                    }
                    if (fixedCellHeight == -1) {
                        cellHeights[index] = cellSize.height;
                    }
                }
            }
            else {
                if (cellWidth == -1) {
                    cellWidth = 0;
                }
                if (cellHeights == null) {
                    cellHeights = new int[dataModelSize];
                }
                for(int index = 0; index < dataModelSize; index++) {
                    cellHeights[index] = 0;
                }
            }
        }
        columnCount = 1;
        if (layoutOrientation != JList.VERTICAL) {
            updateHorizontalLayoutState(fixedCellWidth, fixedCellHeight);
        }
    }
    private void updateHorizontalLayoutState(int fixedCellWidth,
                                             int fixedCellHeight) {
        int visRows = list.getVisibleRowCount();
        int dataModelSize = list.getModel().getSize();
        Insets insets = list.getInsets();
        listHeight = list.getHeight();
        listWidth = list.getWidth();
        if (dataModelSize == 0) {
            rowsPerColumn = columnCount = 0;
            preferredHeight = insets.top + insets.bottom;
            return;
        }
        int height;
        if (fixedCellHeight != -1) {
            height = fixedCellHeight;
        }
        else {
            int maxHeight = 0;
            if (cellHeights.length > 0) {
                maxHeight = cellHeights[cellHeights.length - 1];
                for (int counter = cellHeights.length - 2;
                     counter >= 0; counter--) {
                    maxHeight = Math.max(maxHeight, cellHeights[counter]);
                }
            }
            height = cellHeight = maxHeight;
            cellHeights = null;
        }
        rowsPerColumn = dataModelSize;
        if (visRows > 0) {
            rowsPerColumn = visRows;
            columnCount = Math.max(1, dataModelSize / rowsPerColumn);
            if (dataModelSize > 0 && dataModelSize > rowsPerColumn &&
                dataModelSize % rowsPerColumn != 0) {
                columnCount++;
            }
            if (layoutOrientation == JList.HORIZONTAL_WRAP) {
                rowsPerColumn = (dataModelSize / columnCount);
                if (dataModelSize % columnCount > 0) {
                    rowsPerColumn++;
                }
            }
        }
        else if (layoutOrientation == JList.VERTICAL_WRAP && height != 0) {
            rowsPerColumn = Math.max(1, (listHeight - insets.top -
                                         insets.bottom) / height);
            columnCount = Math.max(1, dataModelSize / rowsPerColumn);
            if (dataModelSize > 0 && dataModelSize > rowsPerColumn &&
                dataModelSize % rowsPerColumn != 0) {
                columnCount++;
            }
        }
        else if (layoutOrientation == JList.HORIZONTAL_WRAP && cellWidth > 0 &&
                 listWidth > 0) {
            columnCount = Math.max(1, (listWidth - insets.left -
                                       insets.right) / cellWidth);
            rowsPerColumn = dataModelSize / columnCount;
            if (dataModelSize % columnCount > 0) {
                rowsPerColumn++;
            }
        }
        preferredHeight = rowsPerColumn * cellHeight + insets.top +
                              insets.bottom;
    }
    private Handler getHandler() {
        if (handler == null) {
            handler = new Handler();
        }
        return handler;
    }
    public class MouseInputHandler implements MouseInputListener
    {
        public void mouseClicked(MouseEvent e) {
            getHandler().mouseClicked(e);
        }
        public void mouseEntered(MouseEvent e) {
            getHandler().mouseEntered(e);
        }
        public void mouseExited(MouseEvent e) {
            getHandler().mouseExited(e);
        }
        public void mousePressed(MouseEvent e) {
            getHandler().mousePressed(e);
        }
        public void mouseDragged(MouseEvent e) {
            getHandler().mouseDragged(e);
        }
        public void mouseMoved(MouseEvent e) {
            getHandler().mouseMoved(e);
        }
        public void mouseReleased(MouseEvent e) {
            getHandler().mouseReleased(e);
        }
    }
    protected MouseInputListener createMouseInputListener() {
        return getHandler();
    }
    public class FocusHandler implements FocusListener
    {
        protected void repaintCellFocus()
        {
            getHandler().repaintCellFocus();
        }
        public void focusGained(FocusEvent e) {
            getHandler().focusGained(e);
        }
        public void focusLost(FocusEvent e) {
            getHandler().focusLost(e);
        }
    }
    protected FocusListener createFocusListener() {
        return getHandler();
    }
    public class ListSelectionHandler implements ListSelectionListener
    {
        public void valueChanged(ListSelectionEvent e)
        {
            getHandler().valueChanged(e);
        }
    }
    protected ListSelectionListener createListSelectionListener() {
        return getHandler();
    }
    private void redrawList() {
        list.revalidate();
        list.repaint();
    }
    public class ListDataHandler implements ListDataListener
    {
        public void intervalAdded(ListDataEvent e) {
            getHandler().intervalAdded(e);
        }
        public void intervalRemoved(ListDataEvent e)
        {
            getHandler().intervalRemoved(e);
        }
        public void contentsChanged(ListDataEvent e) {
            getHandler().contentsChanged(e);
        }
    }
    protected ListDataListener createListDataListener() {
        return getHandler();
    }
    public class PropertyChangeHandler implements PropertyChangeListener
    {
        public void propertyChange(PropertyChangeEvent e)
        {
            getHandler().propertyChange(e);
        }
    }
    protected PropertyChangeListener createPropertyChangeListener() {
        return getHandler();
    }
    private static final int CHANGE_LEAD = 0;
    private static final int CHANGE_SELECTION = 1;
    private static final int EXTEND_SELECTION = 2;
    private static class Actions extends UIAction {
        private static final String SELECT_PREVIOUS_COLUMN =
                                    "selectPreviousColumn";
        private static final String SELECT_PREVIOUS_COLUMN_EXTEND =
                                    "selectPreviousColumnExtendSelection";
        private static final String SELECT_PREVIOUS_COLUMN_CHANGE_LEAD =
                                    "selectPreviousColumnChangeLead";
        private static final String SELECT_NEXT_COLUMN = "selectNextColumn";
        private static final String SELECT_NEXT_COLUMN_EXTEND =
                                    "selectNextColumnExtendSelection";
        private static final String SELECT_NEXT_COLUMN_CHANGE_LEAD =
                                    "selectNextColumnChangeLead";
        private static final String SELECT_PREVIOUS_ROW = "selectPreviousRow";
        private static final String SELECT_PREVIOUS_ROW_EXTEND =
                                     "selectPreviousRowExtendSelection";
        private static final String SELECT_PREVIOUS_ROW_CHANGE_LEAD =
                                     "selectPreviousRowChangeLead";
        private static final String SELECT_NEXT_ROW = "selectNextRow";
        private static final String SELECT_NEXT_ROW_EXTEND =
                                     "selectNextRowExtendSelection";
        private static final String SELECT_NEXT_ROW_CHANGE_LEAD =
                                     "selectNextRowChangeLead";
        private static final String SELECT_FIRST_ROW = "selectFirstRow";
        private static final String SELECT_FIRST_ROW_EXTEND =
                                     "selectFirstRowExtendSelection";
        private static final String SELECT_FIRST_ROW_CHANGE_LEAD =
                                     "selectFirstRowChangeLead";
        private static final String SELECT_LAST_ROW = "selectLastRow";
        private static final String SELECT_LAST_ROW_EXTEND =
                                     "selectLastRowExtendSelection";
        private static final String SELECT_LAST_ROW_CHANGE_LEAD =
                                     "selectLastRowChangeLead";
        private static final String SCROLL_UP = "scrollUp";
        private static final String SCROLL_UP_EXTEND =
                                     "scrollUpExtendSelection";
        private static final String SCROLL_UP_CHANGE_LEAD =
                                     "scrollUpChangeLead";
        private static final String SCROLL_DOWN = "scrollDown";
        private static final String SCROLL_DOWN_EXTEND =
                                     "scrollDownExtendSelection";
        private static final String SCROLL_DOWN_CHANGE_LEAD =
                                     "scrollDownChangeLead";
        private static final String SELECT_ALL = "selectAll";
        private static final String CLEAR_SELECTION = "clearSelection";
        private static final String ADD_TO_SELECTION = "addToSelection";
        private static final String TOGGLE_AND_ANCHOR = "toggleAndAnchor";
        private static final String EXTEND_TO = "extendTo";
        private static final String MOVE_SELECTION_TO = "moveSelectionTo";
        Actions(String name) {
            super(name);
        }
        public void actionPerformed(ActionEvent e) {
            String name = getName();
            JList list = (JList)e.getSource();
            BasicListUI ui = (BasicListUI)BasicLookAndFeel.getUIOfType(
                     list.getUI(), BasicListUI.class);
            if (name == SELECT_PREVIOUS_COLUMN) {
                changeSelection(list, CHANGE_SELECTION,
                                getNextColumnIndex(list, ui, -1), -1);
            }
            else if (name == SELECT_PREVIOUS_COLUMN_EXTEND) {
                changeSelection(list, EXTEND_SELECTION,
                                getNextColumnIndex(list, ui, -1), -1);
            }
            else if (name == SELECT_PREVIOUS_COLUMN_CHANGE_LEAD) {
                changeSelection(list, CHANGE_LEAD,
                                getNextColumnIndex(list, ui, -1), -1);
            }
            else if (name == SELECT_NEXT_COLUMN) {
                changeSelection(list, CHANGE_SELECTION,
                                getNextColumnIndex(list, ui, 1), 1);
            }
            else if (name == SELECT_NEXT_COLUMN_EXTEND) {
                changeSelection(list, EXTEND_SELECTION,
                                getNextColumnIndex(list, ui, 1), 1);
            }
            else if (name == SELECT_NEXT_COLUMN_CHANGE_LEAD) {
                changeSelection(list, CHANGE_LEAD,
                                getNextColumnIndex(list, ui, 1), 1);
            }
            else if (name == SELECT_PREVIOUS_ROW) {
                changeSelection(list, CHANGE_SELECTION,
                                getNextIndex(list, ui, -1), -1);
            }
            else if (name == SELECT_PREVIOUS_ROW_EXTEND) {
                changeSelection(list, EXTEND_SELECTION,
                                getNextIndex(list, ui, -1), -1);
            }
            else if (name == SELECT_PREVIOUS_ROW_CHANGE_LEAD) {
                changeSelection(list, CHANGE_LEAD,
                                getNextIndex(list, ui, -1), -1);
            }
            else if (name == SELECT_NEXT_ROW) {
                changeSelection(list, CHANGE_SELECTION,
                                getNextIndex(list, ui, 1), 1);
            }
            else if (name == SELECT_NEXT_ROW_EXTEND) {
                changeSelection(list, EXTEND_SELECTION,
                                getNextIndex(list, ui, 1), 1);
            }
            else if (name == SELECT_NEXT_ROW_CHANGE_LEAD) {
                changeSelection(list, CHANGE_LEAD,
                                getNextIndex(list, ui, 1), 1);
            }
            else if (name == SELECT_FIRST_ROW) {
                changeSelection(list, CHANGE_SELECTION, 0, -1);
            }
            else if (name == SELECT_FIRST_ROW_EXTEND) {
                changeSelection(list, EXTEND_SELECTION, 0, -1);
            }
            else if (name == SELECT_FIRST_ROW_CHANGE_LEAD) {
                changeSelection(list, CHANGE_LEAD, 0, -1);
            }
            else if (name == SELECT_LAST_ROW) {
                changeSelection(list, CHANGE_SELECTION,
                                list.getModel().getSize() - 1, 1);
            }
            else if (name == SELECT_LAST_ROW_EXTEND) {
                changeSelection(list, EXTEND_SELECTION,
                                list.getModel().getSize() - 1, 1);
            }
            else if (name == SELECT_LAST_ROW_CHANGE_LEAD) {
                changeSelection(list, CHANGE_LEAD,
                                list.getModel().getSize() - 1, 1);
            }
            else if (name == SCROLL_UP) {
                changeSelection(list, CHANGE_SELECTION,
                                getNextPageIndex(list, -1), -1);
            }
            else if (name == SCROLL_UP_EXTEND) {
                changeSelection(list, EXTEND_SELECTION,
                                getNextPageIndex(list, -1), -1);
            }
            else if (name == SCROLL_UP_CHANGE_LEAD) {
                changeSelection(list, CHANGE_LEAD,
                                getNextPageIndex(list, -1), -1);
            }
            else if (name == SCROLL_DOWN) {
                changeSelection(list, CHANGE_SELECTION,
                                getNextPageIndex(list, 1), 1);
            }
            else if (name == SCROLL_DOWN_EXTEND) {
                changeSelection(list, EXTEND_SELECTION,
                                getNextPageIndex(list, 1), 1);
            }
            else if (name == SCROLL_DOWN_CHANGE_LEAD) {
                changeSelection(list, CHANGE_LEAD,
                                getNextPageIndex(list, 1), 1);
            }
            else if (name == SELECT_ALL) {
                selectAll(list);
            }
            else if (name == CLEAR_SELECTION) {
                clearSelection(list);
            }
            else if (name == ADD_TO_SELECTION) {
                int index = adjustIndex(
                    list.getSelectionModel().getLeadSelectionIndex(), list);
                if (!list.isSelectedIndex(index)) {
                    int oldAnchor = list.getSelectionModel().getAnchorSelectionIndex();
                    list.setValueIsAdjusting(true);
                    list.addSelectionInterval(index, index);
                    list.getSelectionModel().setAnchorSelectionIndex(oldAnchor);
                    list.setValueIsAdjusting(false);
                }
            }
            else if (name == TOGGLE_AND_ANCHOR) {
                int index = adjustIndex(
                    list.getSelectionModel().getLeadSelectionIndex(), list);
                if (list.isSelectedIndex(index)) {
                    list.removeSelectionInterval(index, index);
                } else {
                    list.addSelectionInterval(index, index);
                }
            }
            else if (name == EXTEND_TO) {
                changeSelection(
                    list, EXTEND_SELECTION,
                    adjustIndex(list.getSelectionModel().getLeadSelectionIndex(), list),
                    0);
            }
            else if (name == MOVE_SELECTION_TO) {
                changeSelection(
                    list, CHANGE_SELECTION,
                    adjustIndex(list.getSelectionModel().getLeadSelectionIndex(), list),
                    0);
            }
        }
        public boolean isEnabled(Object c) {
            Object name = getName();
            if (name == SELECT_PREVIOUS_COLUMN_CHANGE_LEAD ||
                    name == SELECT_NEXT_COLUMN_CHANGE_LEAD ||
                    name == SELECT_PREVIOUS_ROW_CHANGE_LEAD ||
                    name == SELECT_NEXT_ROW_CHANGE_LEAD ||
                    name == SELECT_FIRST_ROW_CHANGE_LEAD ||
                    name == SELECT_LAST_ROW_CHANGE_LEAD ||
                    name == SCROLL_UP_CHANGE_LEAD ||
                    name == SCROLL_DOWN_CHANGE_LEAD) {
                return c != null && ((JList)c).getSelectionModel()
                                        instanceof DefaultListSelectionModel;
            }
            return true;
        }
        private void clearSelection(JList list) {
            list.clearSelection();
        }
        private void selectAll(JList list) {
            int size = list.getModel().getSize();
            if (size > 0) {
                ListSelectionModel lsm = list.getSelectionModel();
                int lead = adjustIndex(lsm.getLeadSelectionIndex(), list);
                if (lsm.getSelectionMode() == ListSelectionModel.SINGLE_SELECTION) {
                    if (lead == -1) {
                        int min = adjustIndex(list.getMinSelectionIndex(), list);
                        lead = (min == -1 ? 0 : min);
                    }
                    list.setSelectionInterval(lead, lead);
                    list.ensureIndexIsVisible(lead);
                } else {
                    list.setValueIsAdjusting(true);
                    int anchor = adjustIndex(lsm.getAnchorSelectionIndex(), list);
                    list.setSelectionInterval(0, size - 1);
                    SwingUtilities2.setLeadAnchorWithoutSelection(lsm, anchor, lead);
                    list.setValueIsAdjusting(false);
                }
            }
        }
        private int getNextPageIndex(JList list, int direction) {
            if (list.getModel().getSize() == 0) {
                return -1;
            }
            int index = -1;
            Rectangle visRect = list.getVisibleRect();
            ListSelectionModel lsm = list.getSelectionModel();
            int lead = adjustIndex(lsm.getLeadSelectionIndex(), list);
            Rectangle leadRect =
                (lead==-1) ? new Rectangle() : list.getCellBounds(lead, lead);
            if (list.getLayoutOrientation() == JList.VERTICAL_WRAP &&
                list.getVisibleRowCount() <= 0) {
                if (!list.getComponentOrientation().isLeftToRight()) {
                    direction = -direction;
                }
                if (direction < 0) {
                    visRect.x = leadRect.x + leadRect.width - visRect.width;
                    Point p = new Point(visRect.x - 1, leadRect.y);
                    index = list.locationToIndex(p);
                    Rectangle cellBounds = list.getCellBounds(index, index);
                    if (visRect.intersects(cellBounds)) {
                        p.x = cellBounds.x - 1;
                        index = list.locationToIndex(p);
                        cellBounds = list.getCellBounds(index, index);
                    }
                    if (cellBounds.y != leadRect.y) {
                        p.x = cellBounds.x + cellBounds.width;
                        index = list.locationToIndex(p);
                    }
                }
                else {
                    visRect.x = leadRect.x;
                    Point p = new Point(visRect.x + visRect.width, leadRect.y);
                    index = list.locationToIndex(p);
                    Rectangle cellBounds = list.getCellBounds(index, index);
                    if (visRect.intersects(cellBounds)) {
                        p.x = cellBounds.x + cellBounds.width;
                        index = list.locationToIndex(p);
                        cellBounds = list.getCellBounds(index, index);
                    }
                    if (cellBounds.y != leadRect.y) {
                        p.x = cellBounds.x - 1;
                        index = list.locationToIndex(p);
                    }
                }
            }
            else {
                if (direction < 0) {
                    Point p = new Point(leadRect.x, visRect.y);
                    index = list.locationToIndex(p);
                    if (lead <= index) {
                        visRect.y = leadRect.y + leadRect.height - visRect.height;
                        p.y = visRect.y;
                        index = list.locationToIndex(p);
                        Rectangle cellBounds = list.getCellBounds(index, index);
                        if (cellBounds.y < visRect.y) {
                            p.y = cellBounds.y + cellBounds.height;
                            index = list.locationToIndex(p);
                            cellBounds = list.getCellBounds(index, index);
                        }
                        if (cellBounds.y >= leadRect.y) {
                            p.y = leadRect.y - 1;
                            index = list.locationToIndex(p);
                        }
                    }
                }
                else {
                    Point p = new Point(leadRect.x,
                                        visRect.y + visRect.height - 1);
                    index = list.locationToIndex(p);
                    Rectangle cellBounds = list.getCellBounds(index, index);
                    if (cellBounds.y + cellBounds.height >
                        visRect.y + visRect.height) {
                        p.y = cellBounds.y - 1;
                        index = list.locationToIndex(p);
                        cellBounds = list.getCellBounds(index, index);
                        index = Math.max(index, lead);
                    }
                    if (lead >= index) {
                        visRect.y = leadRect.y;
                        p.y = visRect.y + visRect.height - 1;
                        index = list.locationToIndex(p);
                        cellBounds = list.getCellBounds(index, index);
                        if (cellBounds.y + cellBounds.height >
                            visRect.y + visRect.height) {
                            p.y = cellBounds.y - 1;
                            index = list.locationToIndex(p);
                            cellBounds = list.getCellBounds(index, index);
                        }
                        if (cellBounds.y <= leadRect.y) {
                            p.y = leadRect.y + leadRect.height;
                            index = list.locationToIndex(p);
                        }
                    }
                }
            }
            return index;
        }
        private void changeSelection(JList list, int type,
                                     int index, int direction) {
            if (index >= 0 && index < list.getModel().getSize()) {
                ListSelectionModel lsm = list.getSelectionModel();
                if (type == CHANGE_LEAD &&
                        list.getSelectionMode()
                            != ListSelectionModel.MULTIPLE_INTERVAL_SELECTION) {
                    type = CHANGE_SELECTION;
                }
                adjustScrollPositionIfNecessary(list, index, direction);
                if (type == EXTEND_SELECTION) {
                    int anchor = adjustIndex(lsm.getAnchorSelectionIndex(), list);
                    if (anchor == -1) {
                        anchor = 0;
                    }
                    list.setSelectionInterval(anchor, index);
                }
                else if (type == CHANGE_SELECTION) {
                    list.setSelectedIndex(index);
                }
                else {
                    ((DefaultListSelectionModel)lsm).moveLeadSelectionIndex(index);
                }
            }
        }
        private void adjustScrollPositionIfNecessary(JList list, int index,
                                                     int direction) {
            if (direction == 0) {
                return;
            }
            Rectangle cellBounds = list.getCellBounds(index, index);
            Rectangle visRect = list.getVisibleRect();
            if (cellBounds != null && !visRect.contains(cellBounds)) {
                if (list.getLayoutOrientation() == JList.VERTICAL_WRAP &&
                    list.getVisibleRowCount() <= 0) {
                    if (list.getComponentOrientation().isLeftToRight()) {
                        if (direction > 0) {
                            int x =Math.max(0,
                                cellBounds.x + cellBounds.width - visRect.width);
                            int startIndex =
                                list.locationToIndex(new Point(x, cellBounds.y));
                            Rectangle startRect = list.getCellBounds(startIndex,
                                                                     startIndex);
                            if (startRect.x < x && startRect.x < cellBounds.x) {
                                startRect.x += startRect.width;
                                startIndex =
                                    list.locationToIndex(startRect.getLocation());
                                startRect = list.getCellBounds(startIndex,
                                                               startIndex);
                            }
                            cellBounds = startRect;
                        }
                        cellBounds.width = visRect.width;
                    }
                    else {
                        if (direction > 0) {
                            int x = cellBounds.x + visRect.width;
                            int rightIndex =
                                list.locationToIndex(new Point(x, cellBounds.y));
                            Rectangle rightRect = list.getCellBounds(rightIndex,
                                                                     rightIndex);
                            if (rightRect.x + rightRect.width > x &&
                                rightRect.x > cellBounds.x) {
                                rightRect.width = 0;
                            }
                            cellBounds.x = Math.max(0,
                                rightRect.x + rightRect.width - visRect.width);
                            cellBounds.width = visRect.width;
                        }
                        else {
                            cellBounds.x += Math.max(0,
                                cellBounds.width - visRect.width);
                            cellBounds.width = Math.min(cellBounds.width,
                                                        visRect.width);
                        }
                    }
                }
                else {
                    if (direction > 0 &&
                            (cellBounds.y < visRect.y ||
                                    cellBounds.y + cellBounds.height
                                            > visRect.y + visRect.height)) {
                        int y = Math.max(0,
                            cellBounds.y + cellBounds.height - visRect.height);
                        int startIndex =
                            list.locationToIndex(new Point(cellBounds.x, y));
                        Rectangle startRect = list.getCellBounds(startIndex,
                                                                 startIndex);
                        if (startRect.y < y && startRect.y < cellBounds.y) {
                            startRect.y += startRect.height;
                            startIndex =
                                list.locationToIndex(startRect.getLocation());
                            startRect =
                                list.getCellBounds(startIndex, startIndex);
                        }
                        cellBounds = startRect;
                        cellBounds.height = visRect.height;
                    }
                    else {
                        cellBounds.height = Math.min(cellBounds.height, visRect.height);
                    }
                }
                list.scrollRectToVisible(cellBounds);
            }
        }
        private int getNextColumnIndex(JList list, BasicListUI ui,
                                       int amount) {
            if (list.getLayoutOrientation() != JList.VERTICAL) {
                int index = adjustIndex(list.getLeadSelectionIndex(), list);
                int size = list.getModel().getSize();
                if (index == -1) {
                    return 0;
                } else if (size == 1) {
                    return 0;
                } else if (ui == null || ui.columnCount <= 1) {
                    return -1;
                }
                int column = ui.convertModelToColumn(index);
                int row = ui.convertModelToRow(index);
                column += amount;
                if (column >= ui.columnCount || column < 0) {
                    return -1;
                }
                int maxRowCount = ui.getRowCount(column);
                if (row >= maxRowCount) {
                    return -1;
                }
                return ui.getModelIndex(column, row);
            }
            return -1;
        }
        private int getNextIndex(JList list, BasicListUI ui, int amount) {
            int index = adjustIndex(list.getLeadSelectionIndex(), list);
            int size = list.getModel().getSize();
            if (index == -1) {
                if (size > 0) {
                    if (amount > 0) {
                        index = 0;
                    }
                    else {
                        index = size - 1;
                    }
                }
            } else if (size == 1) {
                index = 0;
            } else if (list.getLayoutOrientation() == JList.HORIZONTAL_WRAP) {
                if (ui != null) {
                    index += ui.columnCount * amount;
                }
            } else {
                index += amount;
            }
            return index;
        }
    }
    private class Handler implements FocusListener, KeyListener,
                          ListDataListener, ListSelectionListener,
                          MouseInputListener, PropertyChangeListener,
                          BeforeDrag {
        private String prefix = "";
        private String typedString = "";
        private long lastTime = 0L;
        public void keyTyped(KeyEvent e) {
            JList src = (JList)e.getSource();
            ListModel model = src.getModel();
            if (model.getSize() == 0 || e.isAltDown() ||
                    BasicGraphicsUtils.isMenuShortcutKeyDown(e) ||
                    isNavigationKey(e)) {
                return;
            }
            boolean startingFromSelection = true;
            char c = e.getKeyChar();
            long time = e.getWhen();
            int startIndex = adjustIndex(src.getLeadSelectionIndex(), list);
            if (time - lastTime < timeFactor) {
                typedString += c;
                if((prefix.length() == 1) && (c == prefix.charAt(0))) {
                    startIndex++;
                } else {
                    prefix = typedString;
                }
            } else {
                startIndex++;
                typedString = "" + c;
                prefix = typedString;
            }
            lastTime = time;
            if (startIndex < 0 || startIndex >= model.getSize()) {
                startingFromSelection = false;
                startIndex = 0;
            }
            int index = src.getNextMatch(prefix, startIndex,
                                         Position.Bias.Forward);
            if (index >= 0) {
                src.setSelectedIndex(index);
                src.ensureIndexIsVisible(index);
            } else if (startingFromSelection) { 
                index = src.getNextMatch(prefix, 0,
                                         Position.Bias.Forward);
                if (index >= 0) {
                    src.setSelectedIndex(index);
                    src.ensureIndexIsVisible(index);
                }
            }
        }
        public void keyPressed(KeyEvent e) {
            if ( isNavigationKey(e) ) {
                prefix = "";
                typedString = "";
                lastTime = 0L;
            }
        }
        public void keyReleased(KeyEvent e) {
        }
        private boolean isNavigationKey(KeyEvent event) {
            InputMap inputMap = list.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
            KeyStroke key = KeyStroke.getKeyStrokeForEvent(event);
            if (inputMap != null && inputMap.get(key) != null) {
                return true;
            }
            return false;
        }
        public void propertyChange(PropertyChangeEvent e) {
            String propertyName = e.getPropertyName();
            if (propertyName == "model") {
                ListModel oldModel = (ListModel)e.getOldValue();
                ListModel newModel = (ListModel)e.getNewValue();
                if (oldModel != null) {
                    oldModel.removeListDataListener(listDataListener);
                }
                if (newModel != null) {
                    newModel.addListDataListener(listDataListener);
                }
                updateLayoutStateNeeded |= modelChanged;
                redrawList();
            }
            else if (propertyName == "selectionModel") {
                ListSelectionModel oldModel = (ListSelectionModel)e.getOldValue();
                ListSelectionModel newModel = (ListSelectionModel)e.getNewValue();
                if (oldModel != null) {
                    oldModel.removeListSelectionListener(listSelectionListener);
                }
                if (newModel != null) {
                    newModel.addListSelectionListener(listSelectionListener);
                }
                updateLayoutStateNeeded |= modelChanged;
                redrawList();
            }
            else if (propertyName == "cellRenderer") {
                updateLayoutStateNeeded |= cellRendererChanged;
                redrawList();
            }
            else if (propertyName == "font") {
                updateLayoutStateNeeded |= fontChanged;
                redrawList();
            }
            else if (propertyName == "prototypeCellValue") {
                updateLayoutStateNeeded |= prototypeCellValueChanged;
                redrawList();
            }
            else if (propertyName == "fixedCellHeight") {
                updateLayoutStateNeeded |= fixedCellHeightChanged;
                redrawList();
            }
            else if (propertyName == "fixedCellWidth") {
                updateLayoutStateNeeded |= fixedCellWidthChanged;
                redrawList();
            }
            else if (propertyName == "selectionForeground") {
                list.repaint();
            }
            else if (propertyName == "selectionBackground") {
                list.repaint();
            }
            else if ("layoutOrientation" == propertyName) {
                updateLayoutStateNeeded |= layoutOrientationChanged;
                layoutOrientation = list.getLayoutOrientation();
                redrawList();
            }
            else if ("visibleRowCount" == propertyName) {
                if (layoutOrientation != JList.VERTICAL) {
                    updateLayoutStateNeeded |= layoutOrientationChanged;
                    redrawList();
                }
            }
            else if ("componentOrientation" == propertyName) {
                isLeftToRight = list.getComponentOrientation().isLeftToRight();
                updateLayoutStateNeeded |= componentOrientationChanged;
                redrawList();
                InputMap inputMap = getInputMap(JComponent.WHEN_FOCUSED);
                SwingUtilities.replaceUIInputMap(list, JComponent.WHEN_FOCUSED,
                                                 inputMap);
            } else if ("List.isFileList" == propertyName) {
                updateIsFileList();
                redrawList();
            } else if ("dropLocation" == propertyName) {
                JList.DropLocation oldValue = (JList.DropLocation)e.getOldValue();
                repaintDropLocation(oldValue);
                repaintDropLocation(list.getDropLocation());
            }
        }
        private void repaintDropLocation(JList.DropLocation loc) {
            if (loc == null) {
                return;
            }
            Rectangle r;
            if (loc.isInsert()) {
                r = getDropLineRect(loc);
            } else {
                r = getCellBounds(list, loc.getIndex());
            }
            if (r != null) {
                list.repaint(r);
            }
        }
        public void intervalAdded(ListDataEvent e) {
            updateLayoutStateNeeded = modelChanged;
            int minIndex = Math.min(e.getIndex0(), e.getIndex1());
            int maxIndex = Math.max(e.getIndex0(), e.getIndex1());
            ListSelectionModel sm = list.getSelectionModel();
            if (sm != null) {
                sm.insertIndexInterval(minIndex, maxIndex - minIndex+1, true);
            }
            redrawList();
        }
        public void intervalRemoved(ListDataEvent e)
        {
            updateLayoutStateNeeded = modelChanged;
            ListSelectionModel sm = list.getSelectionModel();
            if (sm != null) {
                sm.removeIndexInterval(e.getIndex0(), e.getIndex1());
            }
            redrawList();
        }
        public void contentsChanged(ListDataEvent e) {
            updateLayoutStateNeeded = modelChanged;
            redrawList();
        }
        public void valueChanged(ListSelectionEvent e) {
            maybeUpdateLayoutState();
            int size = list.getModel().getSize();
            int firstIndex = Math.min(size - 1, Math.max(e.getFirstIndex(), 0));
            int lastIndex = Math.min(size - 1, Math.max(e.getLastIndex(), 0));
            Rectangle bounds = getCellBounds(list, firstIndex, lastIndex);
            if (bounds != null) {
                list.repaint(bounds.x, bounds.y, bounds.width, bounds.height);
            }
        }
        public void mouseClicked(MouseEvent e) {
        }
        public void mouseEntered(MouseEvent e) {
        }
        public void mouseExited(MouseEvent e) {
        }
        private boolean dragPressDidSelection;
        public void mousePressed(MouseEvent e) {
            if (SwingUtilities2.shouldIgnore(e, list)) {
                return;
            }
            boolean dragEnabled = list.getDragEnabled();
            boolean grabFocus = true;
            if (dragEnabled) {
                int row = SwingUtilities2.loc2IndexFileList(list, e.getPoint());
                if (row != -1 && DragRecognitionSupport.mousePressed(e)) {
                    dragPressDidSelection = false;
                    if (BasicGraphicsUtils.isMenuShortcutKeyDown(e)) {
                        return;
                    } else if (!e.isShiftDown() && list.isSelectedIndex(row)) {
                        list.addSelectionInterval(row, row);
                        return;
                    }
                    grabFocus = false;
                    dragPressDidSelection = true;
                }
            } else {
                list.setValueIsAdjusting(true);
            }
            if (grabFocus) {
                SwingUtilities2.adjustFocus(list);
            }
            adjustSelection(e);
        }
        private void adjustSelection(MouseEvent e) {
            int row = SwingUtilities2.loc2IndexFileList(list, e.getPoint());
            if (row < 0) {
                if (isFileList &&
                    e.getID() == MouseEvent.MOUSE_PRESSED &&
                    (!e.isShiftDown() ||
                     list.getSelectionMode() == ListSelectionModel.SINGLE_SELECTION)) {
                    list.clearSelection();
                }
            }
            else {
                int anchorIndex = adjustIndex(list.getAnchorSelectionIndex(), list);
                boolean anchorSelected;
                if (anchorIndex == -1) {
                    anchorIndex = 0;
                    anchorSelected = false;
                } else {
                    anchorSelected = list.isSelectedIndex(anchorIndex);
                }
                if (BasicGraphicsUtils.isMenuShortcutKeyDown(e)) {
                    if (e.isShiftDown()) {
                        if (anchorSelected) {
                            list.addSelectionInterval(anchorIndex, row);
                        } else {
                            list.removeSelectionInterval(anchorIndex, row);
                            if (isFileList) {
                                list.addSelectionInterval(row, row);
                                list.getSelectionModel().setAnchorSelectionIndex(anchorIndex);
                            }
                        }
                    } else if (list.isSelectedIndex(row)) {
                        list.removeSelectionInterval(row, row);
                    } else {
                        list.addSelectionInterval(row, row);
                    }
                } else if (e.isShiftDown()) {
                    list.setSelectionInterval(anchorIndex, row);
                } else {
                    list.setSelectionInterval(row, row);
                }
            }
        }
        public void dragStarting(MouseEvent me) {
            if (BasicGraphicsUtils.isMenuShortcutKeyDown(me)) {
                int row = SwingUtilities2.loc2IndexFileList(list, me.getPoint());
                list.addSelectionInterval(row, row);
            }
        }
        public void mouseDragged(MouseEvent e) {
            if (SwingUtilities2.shouldIgnore(e, list)) {
                return;
            }
            if (list.getDragEnabled()) {
                DragRecognitionSupport.mouseDragged(e, this);
                return;
            }
            if (e.isShiftDown() || BasicGraphicsUtils.isMenuShortcutKeyDown(e)) {
                return;
            }
            int row = locationToIndex(list, e.getPoint());
            if (row != -1) {
                if (isFileList) {
                    return;
                }
                Rectangle cellBounds = getCellBounds(list, row, row);
                if (cellBounds != null) {
                    list.scrollRectToVisible(cellBounds);
                    list.setSelectionInterval(row, row);
                }
            }
        }
        public void mouseMoved(MouseEvent e) {
        }
        public void mouseReleased(MouseEvent e) {
            if (SwingUtilities2.shouldIgnore(e, list)) {
                return;
            }
            if (list.getDragEnabled()) {
                MouseEvent me = DragRecognitionSupport.mouseReleased(e);
                if (me != null) {
                    SwingUtilities2.adjustFocus(list);
                    if (!dragPressDidSelection) {
                        adjustSelection(me);
                    }
                }
            } else {
                list.setValueIsAdjusting(false);
            }
        }
        protected void repaintCellFocus()
        {
            int leadIndex = adjustIndex(list.getLeadSelectionIndex(), list);
            if (leadIndex != -1) {
                Rectangle r = getCellBounds(list, leadIndex, leadIndex);
                if (r != null) {
                    list.repaint(r.x, r.y, r.width, r.height);
                }
            }
        }
        public void focusGained(FocusEvent e) {
            repaintCellFocus();
        }
        public void focusLost(FocusEvent e) {
            repaintCellFocus();
        }
    }
    private static int adjustIndex(int index, JList list) {
        return index < list.getModel().getSize() ? index : -1;
    }
    private static final TransferHandler defaultTransferHandler = new ListTransferHandler();
    static class ListTransferHandler extends TransferHandler implements UIResource {
        protected Transferable createTransferable(JComponent c) {
            if (c instanceof JList) {
                JList list = (JList) c;
                Object[] values = list.getSelectedValues();
                if (values == null || values.length == 0) {
                    return null;
                }
                StringBuffer plainBuf = new StringBuffer();
                StringBuffer htmlBuf = new StringBuffer();
                htmlBuf.append("<html>\n<body>\n<ul>\n");
                for (int i = 0; i < values.length; i++) {
                    Object obj = values[i];
                    String val = ((obj == null) ? "" : obj.toString());
                    plainBuf.append(val + "\n");
                    htmlBuf.append("  <li>" + val + "\n");
                }
                plainBuf.deleteCharAt(plainBuf.length() - 1);
                htmlBuf.append("</ul>\n</body>\n</html>");
                return new BasicTransferable(plainBuf.toString(), htmlBuf.toString());
            }
            return null;
        }
        public int getSourceActions(JComponent c) {
            return COPY;
        }
    }
}
