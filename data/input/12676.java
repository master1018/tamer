public class JTableHeader extends JComponent implements TableColumnModelListener, Accessible
{
    private static final String uiClassID = "TableHeaderUI";
    protected JTable table;
    protected TableColumnModel  columnModel;
    protected boolean   reorderingAllowed;
    protected boolean   resizingAllowed;
    protected boolean   updateTableInRealTime;
    transient protected TableColumn     resizingColumn;
    transient protected TableColumn     draggedColumn;
    transient protected int     draggedDistance;
    private TableCellRenderer defaultRenderer;
    public JTableHeader() {
        this(null);
    }
    public JTableHeader(TableColumnModel cm) {
        super();
        if (cm == null)
            cm = createDefaultColumnModel();
        setColumnModel(cm);
        initializeLocalVars();
        updateUI();
    }
    public void setTable(JTable table) {
        JTable old = this.table;
        this.table = table;
        firePropertyChange("table", old, table);
    }
    public JTable getTable() {
        return table;
    }
    public void setReorderingAllowed(boolean reorderingAllowed) {
        boolean old = this.reorderingAllowed;
        this.reorderingAllowed = reorderingAllowed;
        firePropertyChange("reorderingAllowed", old, reorderingAllowed);
    }
    public boolean getReorderingAllowed() {
        return reorderingAllowed;
    }
    public void setResizingAllowed(boolean resizingAllowed) {
        boolean old = this.resizingAllowed;
        this.resizingAllowed = resizingAllowed;
        firePropertyChange("resizingAllowed", old, resizingAllowed);
    }
    public boolean getResizingAllowed() {
        return resizingAllowed;
    }
    public TableColumn getDraggedColumn() {
        return draggedColumn;
    }
    public int getDraggedDistance() {
        return draggedDistance;
    }
    public TableColumn getResizingColumn() {
        return resizingColumn;
    }
    public void setUpdateTableInRealTime(boolean flag) {
        updateTableInRealTime = flag;
    }
    public boolean getUpdateTableInRealTime() {
        return updateTableInRealTime;
    }
    public void setDefaultRenderer(TableCellRenderer defaultRenderer) {
        this.defaultRenderer = defaultRenderer;
    }
    @Transient
    public TableCellRenderer getDefaultRenderer() {
        return defaultRenderer;
    }
    public int columnAtPoint(Point point) {
        int x = point.x;
        if (!getComponentOrientation().isLeftToRight()) {
            x = getWidthInRightToLeft() - x - 1;
        }
        return getColumnModel().getColumnIndexAtX(x);
    }
    public Rectangle getHeaderRect(int column) {
        Rectangle r = new Rectangle();
        TableColumnModel cm = getColumnModel();
        r.height = getHeight();
        if (column < 0) {
            if( !getComponentOrientation().isLeftToRight() ) {
                r.x = getWidthInRightToLeft();
            }
        }
        else if (column >= cm.getColumnCount()) {
            if( getComponentOrientation().isLeftToRight() ) {
                r.x = getWidth();
            }
        }
        else {
            for(int i = 0; i < column; i++) {
                r.x += cm.getColumn(i).getWidth();
            }
            if( !getComponentOrientation().isLeftToRight() ) {
                r.x = getWidthInRightToLeft() - r.x - cm.getColumn(column).getWidth();
            }
            r.width = cm.getColumn(column).getWidth();
        }
        return r;
    }
    public String getToolTipText(MouseEvent event) {
        String tip = null;
        Point p = event.getPoint();
        int column;
        if ((column = columnAtPoint(p)) != -1) {
            TableColumn aColumn = columnModel.getColumn(column);
            TableCellRenderer renderer = aColumn.getHeaderRenderer();
            if (renderer == null) {
                renderer = defaultRenderer;
            }
            Component component = renderer.getTableCellRendererComponent(
                              getTable(), aColumn.getHeaderValue(), false, false,
                              -1, column);
            if (component instanceof JComponent) {
                MouseEvent newEvent;
                Rectangle cellRect = getHeaderRect(column);
                p.translate(-cellRect.x, -cellRect.y);
                newEvent = new MouseEvent(component, event.getID(),
                                          event.getWhen(), event.getModifiers(),
                                          p.x, p.y, event.getXOnScreen(), event.getYOnScreen(),
                                          event.getClickCount(),
                                          event.isPopupTrigger(), MouseEvent.NOBUTTON);
                tip = ((JComponent)component).getToolTipText(newEvent);
            }
        }
        if (tip == null)
            tip = getToolTipText();
        return tip;
    }
    public TableHeaderUI getUI() {
        return (TableHeaderUI)ui;
    }
    public void setUI(TableHeaderUI ui){
        if (this.ui != ui) {
            super.setUI(ui);
            repaint();
        }
    }
    public void updateUI(){
        setUI((TableHeaderUI)UIManager.getUI(this));
        TableCellRenderer renderer = getDefaultRenderer();
        if (renderer instanceof Component) {
            SwingUtilities.updateComponentTreeUI((Component)renderer);
        }
    }
    public String getUIClassID() {
        return uiClassID;
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
            firePropertyChange("columnModel", old, columnModel);
            resizeAndRepaint();
        }
    }
    public TableColumnModel getColumnModel() {
        return columnModel;
    }
    public void columnAdded(TableColumnModelEvent e) { resizeAndRepaint(); }
    public void columnRemoved(TableColumnModelEvent e) { resizeAndRepaint(); }
    public void columnMoved(TableColumnModelEvent e) { repaint(); }
    public void columnMarginChanged(ChangeEvent e) { resizeAndRepaint(); }
    public void columnSelectionChanged(ListSelectionEvent e) { } 
    protected TableColumnModel createDefaultColumnModel() {
        return new DefaultTableColumnModel();
    }
    protected TableCellRenderer createDefaultRenderer() {
        return new DefaultTableCellHeaderRenderer();
    }
    protected void initializeLocalVars() {
        setOpaque(true);
        table = null;
        reorderingAllowed = true;
        resizingAllowed = true;
        draggedColumn = null;
        draggedDistance = 0;
        resizingColumn = null;
        updateTableInRealTime = true;
        ToolTipManager toolTipManager = ToolTipManager.sharedInstance();
        toolTipManager.registerComponent(this);
        setDefaultRenderer(createDefaultRenderer());
    }
    public void resizeAndRepaint() {
        revalidate();
        repaint();
    }
    public void setDraggedColumn(TableColumn aColumn) {
        draggedColumn = aColumn;
    }
    public void setDraggedDistance(int distance) {
        draggedDistance = distance;
    }
    public void setResizingColumn(TableColumn aColumn) {
        resizingColumn = aColumn;
    }
    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        if ((ui != null) && (getUIClassID().equals(uiClassID))) {
            ui.installUI(this);
        }
    }
    private int getWidthInRightToLeft() {
        if ((table != null) &&
            (table.getAutoResizeMode() != JTable.AUTO_RESIZE_OFF)) {
            return table.getWidth();
        }
        return super.getWidth();
    }
    protected String paramString() {
        String reorderingAllowedString = (reorderingAllowed ?
                                          "true" : "false");
        String resizingAllowedString = (resizingAllowed ?
                                        "true" : "false");
        String updateTableInRealTimeString = (updateTableInRealTime ?
                                              "true" : "false");
        return super.paramString() +
        ",draggedDistance=" + draggedDistance +
        ",reorderingAllowed=" + reorderingAllowedString +
        ",resizingAllowed=" + resizingAllowedString +
        ",updateTableInRealTime=" + updateTableInRealTimeString;
    }
    public AccessibleContext getAccessibleContext() {
        if (accessibleContext == null) {
            accessibleContext = new AccessibleJTableHeader();
        }
        return accessibleContext;
    }
    protected class AccessibleJTableHeader extends AccessibleJComponent {
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.PANEL;
        }
        public Accessible getAccessibleAt(Point p) {
            int column;
            if ((column = JTableHeader.this.columnAtPoint(p)) != -1) {
                TableColumn aColumn = JTableHeader.this.columnModel.getColumn(column);
                TableCellRenderer renderer = aColumn.getHeaderRenderer();
                if (renderer == null) {
                    if (defaultRenderer != null) {
                        renderer = defaultRenderer;
                    } else {
                        return null;
                    }
                }
                Component component = renderer.getTableCellRendererComponent(
                                  JTableHeader.this.getTable(),
                                  aColumn.getHeaderValue(), false, false,
                                  -1, column);
                return new AccessibleJTableHeaderEntry(column, JTableHeader.this, JTableHeader.this.table);
            } else {
                return null;
            }
        }
        public int getAccessibleChildrenCount() {
            return JTableHeader.this.columnModel.getColumnCount();
        }
        public Accessible getAccessibleChild(int i) {
            if (i < 0 || i >= getAccessibleChildrenCount()) {
                return null;
            } else {
                TableColumn aColumn = JTableHeader.this.columnModel.getColumn(i)
;
                TableCellRenderer renderer = aColumn.getHeaderRenderer();
                if (renderer == null) {
                    if (defaultRenderer != null) {
                        renderer = defaultRenderer;
                    } else {
                        return null;
                    }
                }
                Component component = renderer.getTableCellRendererComponent(
                                  JTableHeader.this.getTable(),
                                  aColumn.getHeaderValue(), false, false,
                                  -1, i);
                return new AccessibleJTableHeaderEntry(i, JTableHeader.this, JTableHeader.this.table);
            }
        }
        protected class AccessibleJTableHeaderEntry extends AccessibleContext
            implements Accessible, AccessibleComponent  {
            private JTableHeader parent;
            private int column;
            private JTable table;
            public AccessibleJTableHeaderEntry(int c, JTableHeader p, JTable t) {
                parent = p;
                column = c;
                table = t;
                this.setAccessibleParent(parent);
            }
            public AccessibleContext getAccessibleContext() {
                return this;
            }
            private AccessibleContext getCurrentAccessibleContext() {
                TableColumnModel tcm = table.getColumnModel();
                if (tcm != null) {
                    if (column < 0 || column >= tcm.getColumnCount()) {
                        return null;
                    }
                    TableColumn aColumn = tcm.getColumn(column);
                    TableCellRenderer renderer = aColumn.getHeaderRenderer();
                    if (renderer == null) {
                        if (defaultRenderer != null) {
                            renderer = defaultRenderer;
                        } else {
                            return null;
                        }
                    }
                    Component c = renderer.getTableCellRendererComponent(
                                      JTableHeader.this.getTable(),
                                      aColumn.getHeaderValue(), false, false,
                                      -1, column);
                    if (c instanceof Accessible) {
                        return ((Accessible) c).getAccessibleContext();
                    }
                }
                return null;
            }
            private Component getCurrentComponent() {
                TableColumnModel tcm = table.getColumnModel();
                if (tcm != null) {
                    if (column < 0 || column >= tcm.getColumnCount()) {
                        return null;
                    }
                    TableColumn aColumn = tcm.getColumn(column);
                    TableCellRenderer renderer = aColumn.getHeaderRenderer();
                    if (renderer == null) {
                        if (defaultRenderer != null) {
                            renderer = defaultRenderer;
                        } else {
                            return null;
                        }
                    }
                    return renderer.getTableCellRendererComponent(
                                      JTableHeader.this.getTable(),
                                      aColumn.getHeaderValue(), false, false,
                                      -1, column);
                } else {
                    return null;
                }
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
                    String name = (String)getClientProperty(AccessibleContext.ACCESSIBLE_NAME_PROPERTY);
                    if (name != null) {
                        return name;
                    } else {
                        return table.getColumnName(column);
                    }
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
                    return AccessibleRole.COLUMN_HEADER;
                }
            }
            public AccessibleStateSet getAccessibleStateSet() {
                AccessibleContext ac = getCurrentAccessibleContext();
                if (ac != null) {
                    AccessibleStateSet states = ac.getAccessibleStateSet();
                    if (isShowing()) {
                        states.add(AccessibleState.SHOWING);
                    }
                    return states;
                } else {
                    return new AccessibleStateSet();  
                }
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
                if (isVisible() && JTableHeader.this.isShowing()) {
                    return true;
                } else {
                    return false;
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
                AccessibleContext ac = getCurrentAccessibleContext();
                if (ac instanceof AccessibleComponent) {
                    Rectangle r = ((AccessibleComponent) ac).getBounds();
                    return r.getLocation();
                } else {
                    Component c = getCurrentComponent();
                    if (c != null) {
                        Rectangle r = c.getBounds();
                        return r.getLocation();
                    } else {
                        return getBounds().getLocation();
                    }
                }
            }
            public void setLocation(Point p) {
            }
            public Rectangle getBounds() {
                  Rectangle r = table.getCellRect(-1, column, false);
                  r.y = 0;
                  return r;
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
                return getBounds().getSize();
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
