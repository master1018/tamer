public class ResultSetTableModel implements TableModel {
    protected HashSet m_Listeners;
    protected Object[][] m_Data;
    protected ResultSetHelper m_Helper;
    public ResultSetTableModel(ResultSet rs) {
        this(rs, 0);
    }
    public ResultSetTableModel(ResultSet rs, int rows) {
        super();
        m_Listeners = new HashSet();
        m_Helper = new ResultSetHelper(rs, rows);
        m_Data = m_Helper.getCells();
    }
    public void addTableModelListener(TableModelListener l) {
        m_Listeners.add(l);
    }
    public Class getColumnClass(int columnIndex) {
        Class result;
        result = null;
        if ((m_Helper.getColumnClasses() != null) && (columnIndex >= 0) && (columnIndex < getColumnCount())) {
            if (columnIndex == 0) result = Integer.class; else result = m_Helper.getColumnClasses()[columnIndex - 1];
        }
        return result;
    }
    public int getColumnCount() {
        return m_Helper.getColumnCount() + 1;
    }
    public String getColumnName(int columnIndex) {
        String result;
        result = "";
        if ((m_Helper.getColumnNames() != null) && (columnIndex >= 0) && (columnIndex < getColumnCount())) {
            if (columnIndex == 0) result = "Row"; else result = m_Helper.getColumnNames()[columnIndex - 1];
        }
        return result;
    }
    public int getRowCount() {
        return m_Data.length;
    }
    public Object getValueAt(int rowIndex, int columnIndex) {
        Object result;
        result = null;
        if ((rowIndex >= 0) && (rowIndex < getRowCount()) && (columnIndex >= 0) && (columnIndex < getColumnCount())) {
            if (columnIndex == 0) result = new Integer(rowIndex + 1); else result = m_Data[rowIndex][columnIndex - 1];
        }
        return result;
    }
    public boolean isNullAt(int rowIndex, int columnIndex) {
        return (getValueAt(rowIndex, columnIndex) == null);
    }
    public boolean isNumericAt(int columnIndex) {
        boolean result;
        result = false;
        if ((columnIndex >= 0) && (columnIndex < getColumnCount())) {
            if (columnIndex == 0) {
                result = true;
            } else {
                if (m_Helper.getNumericColumns() == null) result = false; else result = m_Helper.getNumericColumns()[columnIndex - 1];
            }
        }
        return result;
    }
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }
    public void removeTableModelListener(TableModelListener l) {
        m_Listeners.remove(l);
    }
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    }
    public void finalize() throws Throwable {
        try {
            m_Helper.getResultSet().close();
            m_Helper.getResultSet().getStatement().close();
            m_Helper = null;
        } catch (Exception e) {
        }
        m_Data = null;
        super.finalize();
    }
}
