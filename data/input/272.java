public class OldJTable extends JTable
{
    public int getColumnIndex(Object identifier) {
        return getColumnModel().getColumnIndex(identifier);
    }
    public TableColumn addColumn(Object columnIdentifier, int width) {
        return addColumn(columnIdentifier, width, null, null, null);
    }
    public TableColumn addColumn(Object columnIdentifier, List columnData) {
        return addColumn(columnIdentifier, -1, null, null, columnData);
    }
    public TableColumn addColumn(Object columnIdentifier, int width,
                                 TableCellRenderer renderer,
                                 TableCellEditor editor) {
        return addColumn(columnIdentifier, width, renderer, editor, null);
    }
    public TableColumn addColumn(Object columnIdentifier, int width,
                                 TableCellRenderer renderer,
                                 TableCellEditor editor, List columnData) {
        checkDefaultTableModel();
        DefaultTableModel m = (DefaultTableModel)getModel();
        m.addColumn(columnIdentifier, columnData.toArray());
        TableColumn newColumn = new TableColumn(
                m.getColumnCount()-1, width, renderer, editor);
        super.addColumn(newColumn);
        return newColumn;
    }
    public void removeColumn(Object columnIdentifier) {
        super.removeColumn(getColumn(columnIdentifier));
    }
    public void addRow(Object[] rowData) {
        checkDefaultTableModel();
        ((DefaultTableModel)getModel()).addRow(rowData);
    }
    public void addRow(List rowData) {
        checkDefaultTableModel();
        ((DefaultTableModel)getModel()).addRow(rowData.toArray());
    }
    public void removeRow(int rowIndex) {
        checkDefaultTableModel();
        ((DefaultTableModel)getModel()).removeRow(rowIndex);
    }
    public void moveRow(int startIndex, int endIndex, int toIndex) {
        checkDefaultTableModel();
        ((DefaultTableModel)getModel()).moveRow(startIndex, endIndex, toIndex);
    }
    public void insertRow(int rowIndex, Object[] rowData) {
        checkDefaultTableModel();
        ((DefaultTableModel)getModel()).insertRow(rowIndex, rowData);
    }
    public void insertRow(int rowIndex, List rowData) {
        checkDefaultTableModel();
        ((DefaultTableModel)getModel()).insertRow(rowIndex, rowData.toArray());
    }
    public void setNumRows(int newSize) {
        checkDefaultTableModel();
        ((DefaultTableModel)getModel()).setNumRows(newSize);
    }
    public void setDataVector(Object[][] newData, List columnIds) {
        checkDefaultTableModel();
        ((DefaultTableModel)getModel()).setDataVector(
                newData, columnIds.toArray());
    }
    public void setDataVector(Object[][] newData, Object[] columnIds) {
        checkDefaultTableModel();
        ((DefaultTableModel)getModel()).setDataVector(newData, columnIds);
    }
    protected void checkDefaultTableModel() {
        if(!(dataModel instanceof DefaultTableModel))
            throw new InternalError("In order to use this method, the data model must be an instance of DefaultTableModel.");
    }
    public Object getValueAt(Object columnIdentifier, int rowIndex) {
        return super.getValueAt(rowIndex, getColumnIndex(columnIdentifier));
    }
    public boolean isCellEditable(Object columnIdentifier, int rowIndex) {
        return super.isCellEditable(rowIndex, getColumnIndex(columnIdentifier));
    }
    public void setValueAt(Object aValue, Object columnIdentifier, int rowIndex) {
        super.setValueAt(aValue, rowIndex, getColumnIndex(columnIdentifier));
    }
    public boolean editColumnRow(Object identifier, int row) {
        return super.editCellAt(row, getColumnIndex(identifier));
    }
    public void moveColumn(Object columnIdentifier, Object targetColumnIdentifier) {
        moveColumn(getColumnIndex(columnIdentifier),
                   getColumnIndex(targetColumnIdentifier));
    }
    public boolean isColumnSelected(Object identifier) {
        return isColumnSelected(getColumnIndex(identifier));
    }
    public TableColumn addColumn(int modelColumn, int width) {
        return addColumn(modelColumn, width, null, null);
    }
    public TableColumn addColumn(int modelColumn) {
        return addColumn(modelColumn, 75, null, null);
    }
    public TableColumn addColumn(int modelColumn, int width,
                                 TableCellRenderer renderer,
                                 TableCellEditor editor) {
        TableColumn newColumn = new TableColumn(
                modelColumn, width, renderer, editor);
        addColumn(newColumn);
        return newColumn;
    }
    public boolean editColumnRow(int columnIndex, int rowIndex) {
        return super.editCellAt(rowIndex, columnIndex);
    }
    public boolean editColumnRow(int columnIndex, int rowIndex, EventObject e){
        return super.editCellAt(rowIndex, columnIndex, e);
    }
}  
