public class ResultSetDynaClass extends JDBCDynaClass implements DynaClass {
    public ResultSetDynaClass(ResultSet resultSet) throws SQLException {
        this(resultSet, true);
    }
    public ResultSetDynaClass(ResultSet resultSet, boolean lowerCase) throws SQLException {
        if (resultSet == null) {
            throw new NullPointerException();
        }
        this.resultSet = resultSet;
        this.lowerCase = lowerCase;
        introspect(resultSet);
    }
    protected ResultSet resultSet = null;
    public Iterator iterator() {
        return (new ResultSetIterator(this));
    }
    ResultSet getResultSet() {
        return (this.resultSet);
    }
    protected Class loadClass(String className) throws SQLException {
        try {
            return getClass().getClassLoader().loadClass(className);
        } catch (Exception e) {
            throw new SQLException("Cannot load column class '" + className + "': " + e);
        }
    }
}
