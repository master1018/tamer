public class COLUMNS implements TableNameMapping {
    @dbVar
    @length("512")
    String TABLE_SCHEMA;
    @dbVar
    @length("64")
    String TABLE_NAME;
    @dbVar
    @length("64")
    public String COLUMN_NAME;
    @dbVar
    String COLUMN_DEFAULT;
    @dbVar
    Boolean IS_NULLABLE;
    @dbVar
    Long CHARACTER_MAXIMUM_LENGTH;
    @dbVar
    String COLUMN_KEY;
    @dbVar
    String DATA_TYPE;
    @dbVar
    String EXTRA;
    @dbVar
    String COLUMN_TYPE;
    public boolean isPrimaryKey() {
        return (COLUMN_KEY != null && COLUMN_KEY.equals("PRI"));
    }
    public boolean isAutoIncrement() {
        return (EXTRA != null && EXTRA.contains("auto_increment"));
    }
    public String getDefault(FIELDTYPES type) {
        String result = "null";
        if (COLUMN_DEFAULT != null && !COLUMN_DEFAULT.equals("NULL")) {
            switch(type) {
                case BOOL:
                    if (COLUMN_DEFAULT.equals("0")) {
                        result = "new Boolean(false)";
                    } else {
                        result = "new Boolean(true)";
                    }
                    break;
                case DOUBLE:
                    result = "new Double(" + COLUMN_DEFAULT + ")";
                    break;
                case DECIMAL:
                    result = "new BigDecimal(" + COLUMN_DEFAULT + ")";
                    break;
                case INT:
                    result = "new Integer( " + COLUMN_DEFAULT + ")";
            }
        }
        return result;
    }
    public String getRealTableName() {
        return this.getClass().getSimpleName();
    }
}
