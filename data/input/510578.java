public class SQLiteDatabaseCorruptException extends SQLiteException {
    public SQLiteDatabaseCorruptException() {}
    public SQLiteDatabaseCorruptException(String error) {
        super(error);
    }
}
