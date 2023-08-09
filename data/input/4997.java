public class XTextFieldEditor extends XTextField implements TableCellEditor {
    protected EventListenerList evtListenerList = new EventListenerList();
    protected ChangeEvent changeEvent = new ChangeEvent(this);
    private FocusListener editorFocusListener = new FocusAdapter() {
        @Override
        public void focusLost(FocusEvent e) {
        }
    };
    public XTextFieldEditor() {
        super();
        textField.addFocusListener(editorFocusListener);
    }
    @Override
    public void  actionPerformed(ActionEvent e) {
        super.actionPerformed(e);
        if ((e.getSource() instanceof JMenuItem) ||
            (e.getSource() instanceof JTextField)) {
            fireEditingStopped();
        }
    }
    protected void dropSuccess() {
        fireEditingStopped();
    }
    public void addCellEditorListener(CellEditorListener listener) {
        evtListenerList.add(CellEditorListener.class,listener);
    }
    public void removeCellEditorListener(CellEditorListener listener) {
        evtListenerList.remove(CellEditorListener.class, listener);
    }
    protected void fireEditingStopped() {
        CellEditorListener listener;
        Object[] listeners = evtListenerList.getListenerList();
        for (int i=0;i< listeners.length;i++) {
            if (listeners[i] == CellEditorListener.class) {
                listener = (CellEditorListener) listeners[i+1];
                listener.editingStopped(changeEvent);
            }
        }
    }
    protected void fireEditingCanceled() {
        CellEditorListener listener;
        Object[] listeners = evtListenerList.getListenerList();
        for (int i=0;i< listeners.length;i++) {
            if (listeners[i] == CellEditorListener.class) {
                listener = (CellEditorListener) listeners[i+1];
                listener.editingCanceled(changeEvent);
            }
        }
    }
    public void cancelCellEditing() {
        fireEditingCanceled();
    }
    public boolean stopCellEditing() {
        fireEditingStopped();
        return true;
    }
    public boolean isCellEditable(EventObject event) {
        return true;
    }
    public boolean shouldSelectCell(EventObject event) {
        return true;
    }
    public Object getCellEditorValue() {
        Object object = getValue();
        if (object instanceof XObject) {
            return ((XObject) object).getObject();
        }
        else {
            return object;
        }
    }
    public Component getTableCellEditorComponent(JTable table,
                                                 Object value,
                                                 boolean isSelected,
                                                 int row,
                                                 int column) {
        String className;
        if (table instanceof XTable) {
            XTable mytable = (XTable) table;
            className = mytable.getClassName(row);
        } else {
            className = String.class.getName();
        }
        try {
            init(value,Utils.getClass(className));
        }
        catch(Exception e) {}
        return this;
    }
}
