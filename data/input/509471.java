public abstract class SQLiteTransaction {
    protected abstract boolean performTransaction(SQLiteDatabase db);
    public void run(SQLiteDatabase db) {
        db.beginTransaction();
        try {
            if (performTransaction(db)) {
                db.setTransactionSuccessful();
            }
        } finally {
            db.endTransaction();
        }
    }
}
