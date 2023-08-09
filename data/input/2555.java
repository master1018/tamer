public abstract class SyncProvider {
    public SyncProvider() {
    }
    public abstract String getProviderID();
    public abstract RowSetReader getRowSetReader();
    public abstract RowSetWriter getRowSetWriter();
    public abstract int getProviderGrade();
    public abstract void setDataSourceLock(int datasource_lock)
        throws SyncProviderException;
    public abstract int getDataSourceLock()
        throws SyncProviderException;
    public abstract int supportsUpdatableView();
    public abstract String getVersion();
    public abstract String getVendor();
    public static final int GRADE_NONE = 1;
    public static final int GRADE_CHECK_MODIFIED_AT_COMMIT = 2;
    public static final int GRADE_CHECK_ALL_AT_COMMIT = 3;
    public static final int GRADE_LOCK_WHEN_MODIFIED = 4;
    public static final int GRADE_LOCK_WHEN_LOADED = 5;
    public static final int DATASOURCE_NO_LOCK = 1;
    public static final int DATASOURCE_ROW_LOCK = 2;
    public static final int DATASOURCE_TABLE_LOCK = 3;
    public static final int DATASOURCE_DB_LOCK = 4;
    public static final int UPDATABLE_VIEW_SYNC = 5;
    public static final int NONUPDATABLE_VIEW_SYNC = 6;
}
