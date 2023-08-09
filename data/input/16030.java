public abstract class BaseRow implements Serializable, Cloneable {
    protected Object[] origVals;
    public Object[] getOrigRow() {
        return origVals;
    }
    public abstract Object getColumnObject(int idx) throws SQLException;
    public abstract void setColumnObject(int idx, Object obj) throws SQLException;
}
