public class InsertRow extends BaseRow implements Serializable, Cloneable {
    private BitSet colsInserted;
    private int cols;
    private JdbcRowSetResourceBundle resBundle;
    public InsertRow(int numCols) {
        origVals = new Object[numCols];
        colsInserted = new BitSet(numCols);
        cols = numCols;
        try {
           resBundle = JdbcRowSetResourceBundle.getJdbcRowSetResourceBundle();
        } catch(IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }
    protected void markColInserted(int col) {
        colsInserted.set(col);
    }
    public boolean isCompleteRow(RowSetMetaData RowSetMD) throws SQLException {
        for (int i = 0; i < cols; i++) {
            if (colsInserted.get(i) == false &&
                RowSetMD.isNullable(i + 1) ==
                ResultSetMetaData.columnNoNulls) {
                return false;
            }
        }
        return true;
    }
    public void initInsertRow() {
        for (int i = 0; i < cols; i++) {
            colsInserted.clear(i);
        }
    }
    public Object getColumnObject(int idx) throws SQLException {
        if (colsInserted.get(idx - 1) == false) {
            throw new SQLException(resBundle.handleGetObject("insertrow.novalue").toString());
        }
        return (origVals[idx - 1]);
    }
    public void setColumnObject(int idx, Object val) {
        origVals[idx - 1] = val;
        markColInserted(idx - 1);
    }
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        try {
           resBundle = JdbcRowSetResourceBundle.getJdbcRowSetResourceBundle();
        } catch(IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }
    static final long serialVersionUID = 1066099658102869344L;
}
