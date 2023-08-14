public class Row extends BaseRow implements Serializable, Cloneable {
    private Object[] currentVals;
    private BitSet colsChanged;
    private boolean deleted;
    private boolean updated;
    private boolean inserted;
    private int numCols;
    public Row(int numCols) {
        origVals = new Object[numCols];
        currentVals = new Object[numCols];
        colsChanged = new BitSet(numCols);
        this.numCols = numCols;
    }
    public Row(int numCols, Object[] vals) {
        origVals = new Object[numCols];
        for (int i=0; i < numCols; i++) {
            origVals[i] = vals[i];
        }
        currentVals = new Object[numCols];
        colsChanged = new BitSet(numCols);
        this.numCols = numCols;
    }
    public void initColumnObject(int idx, Object val) {
        origVals[idx - 1] = val;
    }
    public void setColumnObject(int idx, Object val) {
            currentVals[idx - 1] = val;
            setColUpdated(idx - 1);
    }
    public Object getColumnObject(int columnIndex) throws SQLException {
        if (getColUpdated(columnIndex - 1)) {
            return(currentVals[columnIndex - 1]); 
        } else {
            return(origVals[columnIndex - 1]); 
        }
    }
    public boolean getColUpdated(int idx) {
        return colsChanged.get(idx);
    }
    public void setDeleted() { 
        deleted = true;
    }
    public boolean getDeleted() {
        return(deleted);
    }
    public void clearDeleted() {
        deleted = false;
    }
    public void setInserted() {
        inserted = true;
    }
    public boolean getInserted() {
        return(inserted);
    }
    public void clearInserted() { 
        inserted = false;
    }
    public boolean getUpdated() {
        return(updated);
    }
    public void setUpdated() {
        for (int i = 0; i < numCols; i++) {
            if (getColUpdated(i) == true) {
                updated = true;
                return;
            }
        }
    }
    private void setColUpdated(int idx) {
        colsChanged.set(idx);
    }
    public void clearUpdated() {
        updated = false;
        for (int i = 0; i < numCols; i++) {
            currentVals[i] = null;
            colsChanged.clear(i);
        }
    }
    public void moveCurrentToOrig() {
        for (int i = 0; i < numCols; i++) {
            if (getColUpdated(i) == true) {
                origVals[i] = currentVals[i];
                currentVals[i] = null;
                colsChanged.clear(i);
            }
        }
        updated = false;
    }
    public BaseRow getCurrentRow() {
        return null;
    }
}
