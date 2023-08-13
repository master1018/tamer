public class SelectionBuilder {
    private StringBuilder mSelection = new StringBuilder();
    private ArrayList<String> mSelectionArgs = new ArrayList<String>();
    public SelectionBuilder reset() {
        mSelection.setLength(0);
        mSelectionArgs.clear();
        return this;
    }
    public SelectionBuilder append(String selection, Object... selectionArgs) {
        if (TextUtils.isEmpty(selection)) {
            if (selectionArgs != null && selectionArgs.length > 0) {
                throw new IllegalArgumentException(
                        "Valid selection required when including arguments");
            }
            return this;
        }
        if (mSelection.length() > 0) {
            mSelection.append(" AND ");
        }
        mSelection.append("(").append(selection).append(")");
        if (selectionArgs != null) {
            for (Object arg : selectionArgs) {
                mSelectionArgs.add(String.valueOf(arg));
            }
        }
        return this;
    }
    public String getSelection() {
        return mSelection.toString();
    }
    public String[] getSelectionArgs() {
        return mSelectionArgs.toArray(new String[mSelectionArgs.size()]);
    }
    public Cursor query(SQLiteDatabase db, String table, String[] columns, String orderBy) {
        return query(db, table, columns, null, null, orderBy, null);
    }
    public Cursor query(SQLiteDatabase db, String table, String[] columns, String groupBy,
            String having, String orderBy, String limit) {
        return db.query(table, columns, getSelection(), getSelectionArgs(), groupBy, having,
                orderBy, limit);
    }
    public int update(SQLiteDatabase db, String table, ContentValues values) {
        return db.update(table, values, getSelection(), getSelectionArgs());
    }
    public int delete(SQLiteDatabase db, String table) {
        return db.delete(table, getSelection(), getSelectionArgs());
    }
}
