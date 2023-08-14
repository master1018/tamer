public class Database {
    protected long handle = 0;
    protected int error_code = 0;
    public void open(String filename, int mode) throws SQLite.Exception {
    synchronized(this) {
        _open(filename, mode);
    }
    }
    private native void _open(String filename, int mode)
    throws SQLite.Exception;
    public void open_aux_file(String filename) throws SQLite.Exception {
    synchronized(this) {
        _open_aux_file(filename);
    }
    }
    private native void _open_aux_file(String filename)
    throws SQLite.Exception;
    protected void finalize() {
    synchronized(this) {
        _finalize();
    }
    }
    private native void _finalize();
    public void close()    throws SQLite.Exception {
    synchronized(this) {
        _close();
    }
    }
    private native void _close()
    throws SQLite.Exception;
    public void exec(String sql, SQLite.Callback cb) throws SQLite.Exception {
    synchronized(this) {
        _exec(sql, cb);
    }
    }
    private native void _exec(String sql, SQLite.Callback cb)
    throws SQLite.Exception;
    public void exec(String sql, SQLite.Callback cb,
             String args[]) throws SQLite.Exception {
    synchronized(this) {
        _exec(sql, cb, args);
    }
    }
    private native void _exec(String sql, SQLite.Callback cb, String args[])
    throws SQLite.Exception;
    public long last_insert_rowid() {
    synchronized(this) {
        return _last_insert_rowid();
    }
    }
    private native long _last_insert_rowid();
    public void interrupt() {
    synchronized(this) {
        _interrupt();
    }
    }
    private native void _interrupt();
    public long changes() {
    synchronized(this) {
        return _changes();
    }
    }
    private native long _changes();
    public void busy_handler(SQLite.BusyHandler bh) {
    synchronized(this) {
        _busy_handler(bh);
    }
    }
    private native void _busy_handler(SQLite.BusyHandler bh);
    public void busy_timeout(int ms) {
    synchronized(this) {
        _busy_timeout(ms);
    }
    }
    private native void _busy_timeout(int ms);
    public TableResult get_table(String sql) throws SQLite.Exception {
    TableResult ret = new TableResult();
    if (!is3()) {
        exec(sql, ret);
    } else {
        synchronized(this) {
        Vm vm = compile(sql);
        set_last_error(vm.error_code);
        while (vm.step(ret)) {
            set_last_error(vm.error_code);
        }
        vm.finalize();
        }
    }
    return ret;
    }
    public TableResult get_table(String sql, String args[])
    throws SQLite.Exception {
    TableResult ret = new TableResult();
    if (!is3()) {
        exec(sql, ret, args);
    } else {
        synchronized(this) {
        Vm vm = compile(sql, args);
        set_last_error(vm.error_code);
        while (vm.step(ret)) {
            set_last_error(vm.error_code);
        }
        vm.finalize();
        }
    }
    return ret;
    }
    public void get_table(String sql, String args[], TableResult tbl)
    throws SQLite.Exception {
    tbl.clear();
    if (!is3()) {
        exec(sql, tbl, args);
    } else {
        synchronized(this) {
        Vm vm = compile(sql, args);
        while (vm.step(tbl)) {
        }
        vm.finalize();
        }
    }
    }
    public synchronized static boolean complete(String sql) {
    return _complete(sql);
    }
    private native static boolean _complete(String sql);
    public native static String version();
    public native String dbversion();
    public void create_function(String name, int nargs, Function f) {
    synchronized(this) {
        _create_function(name, nargs, f);
    }
    }
    private native void _create_function(String name, int nargs, Function f);
    public void create_aggregate(String name, int nargs, Function f) {
    synchronized(this) {
        _create_aggregate(name, nargs, f);
    }
    }
    private native void _create_aggregate(String name, int nargs, Function f);
    public void function_type(String name, int type) {
    synchronized(this) {
        _function_type(name, type);
    }
    }
    private native void _function_type(String name, int type);
    public int last_error() {
    return error_code;
    }
    protected void set_last_error(int error_code) {
    this.error_code = error_code;
    }
    public String error_message() {
    synchronized(this) {
        return _errmsg();
    }
    }
    private native String _errmsg();
    public static native String error_string(int error_code);
    public void set_encoding(String enc) throws SQLite.Exception {
    synchronized(this) {
        _set_encoding(enc);
    }
    }
    private native void _set_encoding(String enc)
    throws SQLite.Exception;
    public void set_authorizer(Authorizer auth) {
    synchronized(this) {
        _set_authorizer(auth);
    }
    }
    private native void _set_authorizer(Authorizer auth);
    public void trace(Trace tr) {
    synchronized(this) {
        _trace(tr);
    }
    }
    private native void _trace(Trace tr);
    public Vm compile(String sql) throws SQLite.Exception {
    synchronized(this) {
        Vm vm = new Vm();
        vm_compile(sql, vm);
        return vm;
    }
    }
    public Vm compile(String sql, String args[]) throws SQLite.Exception {
    synchronized(this) {
        Vm vm = new Vm();
        vm_compile_args(sql, vm, args);
        return vm;
    }
    }
    public Stmt prepare(String sql) throws SQLite.Exception {
    synchronized(this) {
        Stmt stmt = new Stmt();
        stmt_prepare(sql, stmt);
        return stmt;
    }
    }
    public Blob open_blob(String db, String table, String column,
              long row, boolean rw) throws SQLite.Exception {
    synchronized(this) {
        Blob blob = new Blob();
        _open_blob(db, table, column, row, rw, blob);
        return blob;
    }
    }
    public native boolean is3();
    private native void vm_compile(String sql, Vm vm)
    throws SQLite.Exception;
    private native void vm_compile_args(String sql, Vm vm, String args[])
    throws SQLite.Exception;
    private native void stmt_prepare(String sql, Stmt stmt)
    throws SQLite.Exception;
    private native void _open_blob(String db, String table, String column,
                   long row, boolean rw, Blob blob)
    throws SQLite.Exception;
    public void progress_handler(int n, SQLite.ProgressHandler p) {
    synchronized(this) {
        _progress_handler(n, p);
    }
    }
    private native void _progress_handler(int n, SQLite.ProgressHandler p);
    private static native void internal_init();
    static {
    try {
        internal_init();
    } catch (Throwable t) {
        System.err.println("Unable to load sqlite: " + t);
    }
    }
}
