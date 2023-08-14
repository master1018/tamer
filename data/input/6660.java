public class ObjectHistogramPanel extends JPanel implements ActionListener {
    private ObjectHistogramTableModel dataModel;
    private ObjectHistogramToolBar toolbar;
    private StatusBar statusBar;
    private JTable     table;
    private java.util.List listeners;
    public ObjectHistogramPanel(ObjectHistogram histo) {
        dataModel = new ObjectHistogramTableModel(histo);
        statusBar = new StatusBar();
        table = new JTable(dataModel, new ObjectHistogramColummModel());
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent evt) {
                    if (evt.getClickCount() == 2) {
                        fireShowObjectsOfType();
                    }
                }
            });
        JTableHeader header = table.getTableHeader();
        header.setDefaultRenderer(new SortHeaderCellRenderer(header, dataModel));
        header.addMouseListener(new SortHeaderMouseAdapter(table, dataModel));
        setLayout(new BorderLayout());
        toolbar = new ObjectHistogramToolBar(statusBar);
        add(toolbar, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(statusBar, BorderLayout.SOUTH);
        registerActions();
    }
    private class ObjectHistogramToolBar extends CommonToolBar {
        private JTextField searchTF;
        public ObjectHistogramToolBar(StatusBar status) {
            super(HSDBActionManager.getInstance(), status);
        }
        protected void addComponents() {
            searchTF = new JTextField();
            searchTF.setToolTipText("Find in Class Description");
            InputMap im = searchTF.getInputMap();
            im.put(KeyStroke.getKeyStroke("ENTER"), "enterPressed");
            ActionMap am = searchTF.getActionMap();
            am.put("enterPressed", manager.getAction(FindAction.VALUE_COMMAND));
            add(searchTF);
            addButton(manager.getAction(FindAction.VALUE_COMMAND));
            addButton(manager.getAction(ShowAction.VALUE_COMMAND));
        }
        public String getFindText() {
            return searchTF.getText();
        }
    }
    private class ObjectHistogramColummModel extends DefaultTableColumnModel {
        private final String LABEL_SIZE = "Size";
        private final String LABEL_COUNT = "Count";
        private final String LABEL_DESC = "Class Description";
        public ObjectHistogramColummModel() {
            int PREF_WIDTH = 80;
            int MAX_WIDTH = 100;
            int HUGE_WIDTH = 140;
            LongCellRenderer lcRender = new LongCellRenderer();
            TableColumn column;
            column = new TableColumn(0, PREF_WIDTH);
            column.setHeaderValue(LABEL_SIZE);
            column.setMaxWidth(MAX_WIDTH);
            column.setResizable(false);
            column.setCellRenderer(lcRender);
            addColumn(column);
            column = new TableColumn(1, PREF_WIDTH);
            column.setHeaderValue(LABEL_COUNT);
            column.setMaxWidth(MAX_WIDTH);
            column.setResizable(false);
            column.setCellRenderer(lcRender);
            addColumn(column);
            column = new TableColumn(2, HUGE_WIDTH);
            column.setHeaderValue(LABEL_DESC);
            addColumn(column);
        }
    }
    private class ObjectHistogramTableModel extends SortableTableModel {
        private String[] columnNames = { "Size", "Count", "Class Description" };
        private Class[] columnClasses = { Long.class, Long.class, String.class };
        public ObjectHistogramTableModel(ObjectHistogram histo) {
            elements = histo.getElements();
            setComparator(new ObjectHistogramComparator(this));
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
        public Class getColumnClass(int col) {
            return columnClasses[col];
        }
        public Object getValueAt(int row, int col) {
            return getValueForColumn(getElement(row), col);
        }
        public Object getValueForColumn(Object obj, int col) {
            ObjectHistogramElement el = (ObjectHistogramElement)obj;
            switch (col) {
            case 0:
                return new Long(el.getSize());
            case 1:
                return new Long(el.getCount());
            case 2:
                return el.getDescription();
            default:
                throw new RuntimeException("Index (" + col + ") out of bounds");
            }
        }
        public ObjectHistogramElement getElement(int index) {
            return (ObjectHistogramElement) elements.get(index);
        }
        private class ObjectHistogramComparator extends TableModelComparator {
            public ObjectHistogramComparator(ObjectHistogramTableModel model) {
                super(model);
            }
            public Object getValueForColumn(Object obj, int column) {
                ObjectHistogramTableModel omodel = (ObjectHistogramTableModel)model;
                return omodel.getValueForColumn(obj, column);
            }
        }
    }
    public void actionPerformed(ActionEvent evt) {
        String command = evt.getActionCommand();
        if (command.equals(ShowAction.VALUE_COMMAND)) {
            fireShowObjectsOfType();
        } else if (command.equals(FindAction.VALUE_COMMAND)) {
            findObject();
        }
    }
    protected void registerActions() {
        registerAction(FindAction.VALUE_COMMAND);
        registerAction(ShowAction.VALUE_COMMAND);
    }
    private void registerAction(String actionName) {
        ActionManager manager = ActionManager.getInstance();
        DelegateAction action = manager.getDelegateAction(actionName);
        action.addActionListener(this);
    }
    public interface Listener {
        public void showObjectsOfType(Klass type);
    }
    public void addPanelListener(Listener listener) {
        if (listeners == null) {
            listeners = new ArrayList();
        }
        listeners.add(listener);
    }
    public void removePanelListener(Listener listener) {
        if (listeners != null) {
            listeners.remove(listener);
        }
    }
    private void findObject() {
        String findText = toolbar.getFindText();
        if (findText == null || findText.equals("")) {
            return;
        }
        TableModel model = table.getModel();
        int row = table.getSelectedRow();
        if (row == model.getRowCount()) {
            row = 0;
        } else {
            row++;
        }
        String value;
        for (int i = row; i < model.getRowCount(); i++) {
            value = (String)model.getValueAt(i, 2);
            if (value != null && value.startsWith(findText)) {
                table.setRowSelectionInterval(i, i);
                Rectangle cellBounds = table.getCellRect(i, 0, true);
                table.scrollRectToVisible(cellBounds);
                return;
            }
        }
        for (int i = 0; i < row; i++) {
            value = (String)model.getValueAt(i, 2);
            if (value != null && value.startsWith(findText)) {
                table.setRowSelectionInterval(i, i);
                Rectangle cellBounds = table.getCellRect(i, 0, true);
                table.scrollRectToVisible(cellBounds);
                return;
            }
        }
    }
    private void fireShowObjectsOfType() {
        int i = table.getSelectedRow();
        if (i < 0) {
            return;
        }
        ObjectHistogramElement el = dataModel.getElement(i);
        for (Iterator iter = listeners.iterator(); iter.hasNext(); ) {
            Listener listener = (Listener) iter.next();
            listener.showObjectsOfType(el.getKlass());
        }
    }
    public static void main(String[] args) {
    }
}
