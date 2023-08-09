public class Stmt {
    private long handle = 0;
    protected int error_code = 0;
    public native boolean prepare() throws SQLite.Exception;
    public native boolean step() throws SQLite.Exception;
    public native void close() throws SQLite.Exception;
    public native void reset() throws SQLite.Exception;
    public native void clear_bindings() throws SQLite.Exception;
    public native void bind(int pos, int value) throws SQLite.Exception;
    public native void bind(int pos, long value) throws SQLite.Exception;
    public native void bind(int pos, double value) throws SQLite.Exception;
    public native void bind(int pos, byte[] value) throws SQLite.Exception;
    public native void bind(int pos, String value) throws SQLite.Exception;
    public native void bind(int pos) throws SQLite.Exception;
    public native void bind_zeroblob(int pos, int length)
    throws SQLite.Exception;
    public native int bind_parameter_count() throws SQLite.Exception;
    public native String bind_parameter_name(int pos) throws SQLite.Exception;
    public native int bind_parameter_index(String name)
    throws SQLite.Exception;
    public native int column_int(int col) throws SQLite.Exception;
    public native long column_long(int col) throws SQLite.Exception;
    public native double column_double(int col) throws SQLite.Exception;
    public native byte[] column_bytes(int col) throws SQLite.Exception;
    public native String column_string(int col) throws SQLite.Exception;
    public native int column_type(int col) throws SQLite.Exception;
    public native int column_count() throws SQLite.Exception;
    public Object column(int col) throws SQLite.Exception {
        switch (column_type(col)) {
    case Constants.SQLITE_INTEGER:
        return new Long(column_long(col));
    case Constants.SQLITE_FLOAT:
        return new Double(column_double(col));
    case Constants.SQLITE_BLOB:
        return column_bytes(col);
    case Constants.SQLITE3_TEXT:
        return column_string(col);
    }
    return null;
    }
    public native String column_table_name(int col) throws SQLite.Exception;
    public native String column_database_name(int col) throws SQLite.Exception;
    public native String column_decltype(int col) throws SQLite.Exception;
    public native String column_origin_name(int col) throws SQLite.Exception;
    protected native void finalize();
    private static native void internal_init();
    static {
    internal_init();
    }
}
