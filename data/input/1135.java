public abstract class Table {
    public abstract Connection getConnection();
    public abstract Column<?>[] getColumns();
    public Column<?> getColumn(String name) {
        for (Column<?> column : getColumns()) if (column.getName().equalsIgnoreCase(name)) return column;
        return null;
    }
    public <T> Column<T> getColumn(String name, Class<T> type) throws PrismsSqlException {
        Column<?> column = getColumn(name);
        if (column == null) return null;
        if (type.isAssignableFrom(column.getDataType().getJavaType())) return (Column<T>) column; else throw new PrismsSqlException("Type " + type.getName() + " is not valid for column " + name + " (type " + column.getDataType().getPrettyName());
    }
    public abstract ForeignKey[] getForeignKeys();
    public ForeignKey[] getForeignKeys(Table table) {
        java.util.ArrayList<ForeignKey> ret = new java.util.ArrayList<ForeignKey>();
        for (ForeignKey key : getForeignKeys()) if (key.getImportKey().getTable().equals(table)) ret.add(key);
        return ret.toArray(new ForeignKey[ret.size()]);
    }
    public abstract void toSQL(StringBuilder ret);
}
