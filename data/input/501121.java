public final class SQLiteDebug {
    public static final boolean DEBUG_SQL_STATEMENTS =
            Log.isLoggable("SQLiteStatements", Log.VERBOSE);
    public static final boolean DEBUG_SQL_TIME =
            Log.isLoggable("SQLiteTime", Log.VERBOSE);
    public static final boolean DEBUG_SQL_CACHE =
            Log.isLoggable("SQLiteCompiledSql", Log.VERBOSE);
    public static final boolean DEBUG_ACTIVE_CURSOR_FINALIZATION =
            Log.isLoggable("SQLiteCursorClosing", Log.VERBOSE);
    public static final boolean DEBUG_LOCK_TIME_TRACKING =
            Log.isLoggable("SQLiteLockTime", Log.VERBOSE);
    public static final boolean DEBUG_LOCK_TIME_TRACKING_STACK_TRACE =
            Log.isLoggable("SQLiteLockStackTrace", Log.VERBOSE);
    public static class PagerStats {
        @Deprecated
        public long totalBytes;
        @Deprecated
        public long referencedBytes;
        @Deprecated
        public long databaseBytes;
        @Deprecated
        public int numPagers;
        public int memoryUsed;
        public int pageCacheOverflo;
        public int largestMemAlloc;
        public ArrayList<DbStats> dbStats;
    }
    public static class DbStats {
        public String dbName;
        public long pageSize;
        public long dbSize;
    public static PagerStats getDatabaseInfo() {
        PagerStats stats = new PagerStats();
        getPagerStats(stats);
        stats.dbStats = SQLiteDatabase.getDbStats();
        return stats;
    }
    public static native void getPagerStats(PagerStats stats);
    public static native long getHeapSize();
    public static native long getHeapAllocatedSize();
    public static native long getHeapFreeSize();
    public static native void getHeapDirtyPages(int[] pages);
    private static int sNumActiveCursorsFinalized = 0;
    public static int getNumActiveCursorsFinalized() {
        return sNumActiveCursorsFinalized;
    }
    static synchronized void notifyActiveCursorFinalized() {
        sNumActiveCursorsFinalized++;
    }
}
