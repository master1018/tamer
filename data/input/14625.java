public class RowSetMetaDataImpl implements RowSetMetaData,  Serializable {
    private int colCount;
    private ColInfo[] colInfo;
    private void checkColRange(int col) throws SQLException {
        if (col <= 0 || col > colCount) {
            throw new SQLException("Invalid column index :"+col);
        }
    }
    private void checkColType(int SQLType) throws SQLException {
        try {
            Class c = java.sql.Types.class;
            Field[] publicFields = c.getFields();
            int fieldValue = 0;
            for (int i = 0; i < publicFields.length; i++) {
                fieldValue = publicFields[i].getInt(c);
                if (fieldValue == SQLType) {
                    return;
                 }
            }
        } catch (Exception e) {
            throw new SQLException(e.getMessage());
        }
        throw new SQLException("Invalid SQL type for column");
    }
    public void setColumnCount(int columnCount) throws SQLException {
        if (columnCount <= 0) {
            throw new SQLException("Invalid column count. Cannot be less " +
                "or equal to zero");
            }
       colCount = columnCount;
       if(!(colCount == Integer.MAX_VALUE)) {
            colInfo = new ColInfo[colCount + 1];
           for (int i=1; i <= colCount; i++) {
                 colInfo[i] = new ColInfo();
           }
       }
    }
    public void setAutoIncrement(int columnIndex, boolean property) throws SQLException {
        checkColRange(columnIndex);
        colInfo[columnIndex].autoIncrement = property;
    }
    public void setCaseSensitive(int columnIndex, boolean property) throws SQLException {
        checkColRange(columnIndex);
        colInfo[columnIndex].caseSensitive = property;
    }
    public void setSearchable(int columnIndex, boolean property)
        throws SQLException {
        checkColRange(columnIndex);
        colInfo[columnIndex].searchable = property;
    }
    public void setCurrency(int columnIndex, boolean property)
        throws SQLException {
        checkColRange(columnIndex);
        colInfo[columnIndex].currency = property;
    }
    public void setNullable(int columnIndex, int property) throws SQLException {
        if ((property < ResultSetMetaData.columnNoNulls) ||
            property > ResultSetMetaData.columnNullableUnknown) {
                throw new SQLException("Invalid nullable constant set. Must be " +
                    "either columnNoNulls, columnNullable or columnNullableUnknown");
        }
        checkColRange(columnIndex);
        colInfo[columnIndex].nullable = property;
    }
    public void setSigned(int columnIndex, boolean property) throws SQLException {
        checkColRange(columnIndex);
        colInfo[columnIndex].signed = property;
    }
    public void setColumnDisplaySize(int columnIndex, int size) throws SQLException {
        if (size < 0) {
            throw new SQLException("Invalid column display size. Cannot be less " +
                "than zero");
        }
        checkColRange(columnIndex);
        colInfo[columnIndex].columnDisplaySize = size;
    }
    public void setColumnLabel(int columnIndex, String label) throws SQLException {
        checkColRange(columnIndex);
        if (label != null) {
            colInfo[columnIndex].columnLabel = label;
        } else {
            colInfo[columnIndex].columnLabel = "";
        }
    }
    public void setColumnName(int columnIndex, String columnName) throws SQLException {
        checkColRange(columnIndex);
        if (columnName != null) {
            colInfo[columnIndex].columnName = columnName;
        } else {
            colInfo[columnIndex].columnName = "";
        }
    }
    public void setSchemaName(int columnIndex, String schemaName) throws SQLException {
        checkColRange(columnIndex);
        if (schemaName != null ) {
            colInfo[columnIndex].schemaName = schemaName;
        } else {
            colInfo[columnIndex].schemaName = "";
        }
    }
    public void setPrecision(int columnIndex, int precision) throws SQLException {
        if (precision < 0) {
            throw new SQLException("Invalid precision value. Cannot be less " +
                "than zero");
        }
        checkColRange(columnIndex);
        colInfo[columnIndex].colPrecision = precision;
    }
    public void setScale(int columnIndex, int scale) throws SQLException {
        if (scale < 0) {
            throw new SQLException("Invalid scale size. Cannot be less " +
                "than zero");
        }
        checkColRange(columnIndex);
        colInfo[columnIndex].colScale = scale;
    }
    public void setTableName(int columnIndex, String tableName) throws SQLException {
        checkColRange(columnIndex);
        if (tableName != null) {
            colInfo[columnIndex].tableName = tableName;
        } else {
            colInfo[columnIndex].tableName = "";
        }
    }
    public void setCatalogName(int columnIndex, String catalogName) throws SQLException {
        checkColRange(columnIndex);
        if (catalogName != null)
            colInfo[columnIndex].catName = catalogName;
        else
            colInfo[columnIndex].catName = "";
    }
    public void setColumnType(int columnIndex, int SQLType) throws SQLException {
        checkColType(SQLType);
        checkColRange(columnIndex);
        colInfo[columnIndex].colType = SQLType;
    }
    public void setColumnTypeName(int columnIndex, String typeName)
        throws SQLException {
        checkColRange(columnIndex);
        if (typeName != null) {
            colInfo[columnIndex].colTypeName = typeName;
        } else {
            colInfo[columnIndex].colTypeName = "";
        }
    }
    public int getColumnCount() throws SQLException {
        return colCount;
    }
    public boolean isAutoIncrement(int columnIndex) throws SQLException {
        checkColRange(columnIndex);
        return colInfo[columnIndex].autoIncrement;
    }
    public boolean isCaseSensitive(int columnIndex) throws SQLException {
        checkColRange(columnIndex);
        return colInfo[columnIndex].caseSensitive;
    }
    public boolean isSearchable(int columnIndex) throws SQLException {
        checkColRange(columnIndex);
        return colInfo[columnIndex].searchable;
    }
    public boolean isCurrency(int columnIndex) throws SQLException {
        checkColRange(columnIndex);
        return colInfo[columnIndex].currency;
    }
    public int isNullable(int columnIndex) throws SQLException {
        checkColRange(columnIndex);
        return colInfo[columnIndex].nullable;
    }
    public boolean isSigned(int columnIndex) throws SQLException {
        checkColRange(columnIndex);
        return colInfo[columnIndex].signed;
    }
    public int getColumnDisplaySize(int columnIndex) throws SQLException {
        checkColRange(columnIndex);
        return colInfo[columnIndex].columnDisplaySize;
    }
    public String getColumnLabel(int columnIndex) throws SQLException {
        checkColRange(columnIndex);
        return colInfo[columnIndex].columnLabel;
    }
    public String getColumnName(int columnIndex) throws SQLException {
        checkColRange(columnIndex);
        return colInfo[columnIndex].columnName;
    }
    public String getSchemaName(int columnIndex) throws SQLException {
        checkColRange(columnIndex);
        String str ="";
        if(colInfo[columnIndex].schemaName == null){
        } else {
              str = colInfo[columnIndex].schemaName;
        }
        return str;
    }
    public int getPrecision(int columnIndex) throws SQLException {
        checkColRange(columnIndex);
        return colInfo[columnIndex].colPrecision;
    }
    public int getScale(int columnIndex) throws SQLException {
        checkColRange(columnIndex);
        return colInfo[columnIndex].colScale;
    }
    public String getTableName(int columnIndex) throws SQLException {
        checkColRange(columnIndex);
        return colInfo[columnIndex].tableName;
    }
    public String getCatalogName(int columnIndex) throws SQLException {
        checkColRange(columnIndex);
        String str ="";
        if(colInfo[columnIndex].catName == null){
        } else {
           str = colInfo[columnIndex].catName;
        }
        return str;
    }
    public int getColumnType(int columnIndex) throws SQLException {
        checkColRange(columnIndex);
        return colInfo[columnIndex].colType;
    }
    public String getColumnTypeName(int columnIndex) throws SQLException {
        checkColRange(columnIndex);
        return colInfo[columnIndex].colTypeName;
    }
    public boolean isReadOnly(int columnIndex) throws SQLException {
        checkColRange(columnIndex);
        return colInfo[columnIndex].readOnly;
    }
    public boolean isWritable(int columnIndex) throws SQLException {
        checkColRange(columnIndex);
        return colInfo[columnIndex].writable;
    }
    public  boolean isDefinitelyWritable(int columnIndex)
        throws SQLException { return true;}
    public String getColumnClassName(int columnIndex) throws SQLException {
        String className = String.class.getName();
        int sqlType = getColumnType(columnIndex);
        switch (sqlType) {
        case Types.NUMERIC:
        case Types.DECIMAL:
            className = java.math.BigDecimal.class.getName();
            break;
        case Types.BIT:
            className = java.lang.Boolean.class.getName();
            break;
        case Types.TINYINT:
            className = java.lang.Byte.class.getName();
            break;
        case Types.SMALLINT:
            className = java.lang.Short.class.getName();
            break;
        case Types.INTEGER:
            className = java.lang.Integer.class.getName();
            break;
        case Types.BIGINT:
            className = java.lang.Long.class.getName();
            break;
        case Types.REAL:
            className = java.lang.Float.class.getName();
            break;
        case Types.FLOAT:
        case Types.DOUBLE:
            className = java.lang.Double.class.getName();
            break;
        case Types.BINARY:
        case Types.VARBINARY:
        case Types.LONGVARBINARY:
            className = "byte[]";
            break;
        case Types.DATE:
            className = java.sql.Date.class.getName();
            break;
        case Types.TIME:
            className = java.sql.Time.class.getName();
            break;
        case Types.TIMESTAMP:
            className = java.sql.Timestamp.class.getName();
            break;
        case Types.BLOB:
            className = java.sql.Blob.class.getName();
            break;
        case Types.CLOB:
            className = java.sql.Clob.class.getName();
            break;
        }
        return className;
    }
    public <T> T unwrap(java.lang.Class<T> iface) throws java.sql.SQLException {
        if(isWrapperFor(iface)) {
            return iface.cast(this);
        } else {
            throw new SQLException("unwrap failed for:"+ iface);
        }
    }
    public boolean isWrapperFor(Class<?> interfaces) throws SQLException {
        return interfaces.isInstance(this);
    }
    static final long serialVersionUID = 6893806403181801867L;
    private class ColInfo implements Serializable {
        public boolean autoIncrement;
        public boolean caseSensitive;
        public boolean currency;
        public int nullable;
        public boolean signed;
        public boolean searchable;
        public int columnDisplaySize;
        public String columnLabel;
        public  String columnName;
        public String schemaName;
        public int colPrecision;
        public int colScale;
        public String tableName ="";
        public String catName;
        public int colType;
        public String colTypeName;
        public boolean readOnly = false;
        public boolean writable = true;
    }
}
