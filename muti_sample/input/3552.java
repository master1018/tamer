public  final class RowSetFactoryImpl implements RowSetFactory {
    public CachedRowSet createCachedRowSet() throws SQLException {
        return new com.sun.rowset.CachedRowSetImpl();
    }
    public FilteredRowSet createFilteredRowSet() throws SQLException {
        return new com.sun.rowset.FilteredRowSetImpl();
    }
    public JdbcRowSet createJdbcRowSet() throws SQLException {
        return new com.sun.rowset.JdbcRowSetImpl();
    }
    public JoinRowSet createJoinRowSet() throws SQLException {
        return new com.sun.rowset.JoinRowSetImpl();
    }
    public WebRowSet createWebRowSet() throws SQLException {
        return new com.sun.rowset.WebRowSetImpl();
    }
}
