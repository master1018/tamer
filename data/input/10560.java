public class JDBCAdapter extends AbstractTableModel {
    Connection connection;
    Statement statement;
    ResultSet resultSet;
    String[] columnNames = {};
    List<List<Object>> rows = new ArrayList<List<Object>>();
    ResultSetMetaData metaData;
    public JDBCAdapter(String url, String driverName,
            String user, String passwd) {
        try {
            Class.forName(driverName);
            System.out.println("Opening db connection");
            connection = DriverManager.getConnection(url, user, passwd);
            statement = connection.createStatement();
        } catch (ClassNotFoundException ex) {
            System.err.println("Cannot find the database driver classes.");
            System.err.println(ex);
        } catch (SQLException ex) {
            System.err.println("Cannot connect to this database.");
            System.err.println(ex);
        }
    }
    public void executeQuery(String query) {
        if (connection == null || statement == null) {
            System.err.println("There is no database to execute the query.");
            return;
        }
        try {
            resultSet = statement.executeQuery(query);
            metaData = resultSet.getMetaData();
            int numberOfColumns = metaData.getColumnCount();
            columnNames = new String[numberOfColumns];
            for (int column = 0; column < numberOfColumns; column++) {
                columnNames[column] = metaData.getColumnLabel(column + 1);
            }
            rows = new ArrayList<List<Object>>();
            while (resultSet.next()) {
                List<Object> newRow = new ArrayList<Object>();
                for (int i = 1; i <= getColumnCount(); i++) {
                    newRow.add(resultSet.getObject(i));
                }
                rows.add(newRow);
            }
            fireTableChanged(null);
        } catch (SQLException ex) {
            System.err.println(ex);
        }
    }
    public void close() throws SQLException {
        System.out.println("Closing db connection");
        resultSet.close();
        statement.close();
        connection.close();
    }
    @Override
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }
    @Override
    public String getColumnName(int column) {
        if (columnNames[column] != null) {
            return columnNames[column];
        } else {
            return "";
        }
    }
    @Override
    public Class<?> getColumnClass(int column) {
        int type;
        try {
            type = metaData.getColumnType(column + 1);
        } catch (SQLException e) {
            return super.getColumnClass(column);
        }
        switch (type) {
            case Types.CHAR:
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
                return String.class;
            case Types.BIT:
                return Boolean.class;
            case Types.TINYINT:
            case Types.SMALLINT:
            case Types.INTEGER:
                return Integer.class;
            case Types.BIGINT:
                return Long.class;
            case Types.FLOAT:
            case Types.DOUBLE:
                return Double.class;
            case Types.DATE:
                return java.sql.Date.class;
            default:
                return Object.class;
        }
    }
    @Override
    public boolean isCellEditable(int row, int column) {
        try {
            return metaData.isWritable(column + 1);
        } catch (SQLException e) {
            return false;
        }
    }
    public int getColumnCount() {
        return columnNames.length;
    }
    public int getRowCount() {
        return rows.size();
    }
    public Object getValueAt(int aRow, int aColumn) {
        List<Object> row = rows.get(aRow);
        return row.get(aColumn);
    }
    public String dbRepresentation(int column, Object value) {
        int type;
        if (value == null) {
            return "null";
        }
        try {
            type = metaData.getColumnType(column + 1);
        } catch (SQLException e) {
            return value.toString();
        }
        switch (type) {
            case Types.INTEGER:
            case Types.DOUBLE:
            case Types.FLOAT:
                return value.toString();
            case Types.BIT:
                return ((Boolean) value).booleanValue() ? "1" : "0";
            case Types.DATE:
                return value.toString(); 
            default:
                return "\"" + value.toString() + "\"";
        }
    }
    @Override
    public void setValueAt(Object value, int row, int column) {
        try {
            String tableName = metaData.getTableName(column + 1);
            if (tableName == null) {
                System.out.println("Table name returned null.");
            }
            String columnName = getColumnName(column);
            String query =
                    "update " + tableName + " set " + columnName + " = "
                    + dbRepresentation(column, value) + " where ";
            for (int col = 0; col < getColumnCount(); col++) {
                String colName = getColumnName(col);
                if (colName.equals("")) {
                    continue;
                }
                if (col != 0) {
                    query = query + " and ";
                }
                query = query + colName + " = " + dbRepresentation(col,
                        getValueAt(row, col));
            }
            System.out.println(query);
            System.out.println("Not sending update to database");
        } catch (SQLException e) {
            System.err.println("Update failed");
        }
        List<Object> dataRow = rows.get(row);
        dataRow.set(column, value);
    }
}
