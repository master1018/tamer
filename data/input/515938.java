public class CursorWrapper implements Cursor {
    public CursorWrapper(Cursor cursor) {
        mCursor = cursor;
    }
    public void abortUpdates() {
        mCursor.abortUpdates();
    }
    public void close() {
        mCursor.close(); 
    }
    public boolean isClosed() {
        return mCursor.isClosed();
    }
    public boolean commitUpdates() {
        return mCursor.commitUpdates();
    }
    public boolean commitUpdates(
            Map<? extends Long, ? extends Map<String, Object>> values) {
        return mCursor.commitUpdates(values);
    }
    public int getCount() {
        return mCursor.getCount();
    }
    public void deactivate() {
        mCursor.deactivate();
    }
    public boolean deleteRow() {
        return mCursor.deleteRow();
    }
    public boolean moveToFirst() {
        return mCursor.moveToFirst();
    }
    public int getColumnCount() {
        return mCursor.getColumnCount();
    }
    public int getColumnIndex(String columnName) {
        return mCursor.getColumnIndex(columnName);
    }
    public int getColumnIndexOrThrow(String columnName)
            throws IllegalArgumentException {
        return mCursor.getColumnIndexOrThrow(columnName);
    }
    public String getColumnName(int columnIndex) {
         return mCursor.getColumnName(columnIndex);
    }
    public String[] getColumnNames() {
        return mCursor.getColumnNames();
    }
    public double getDouble(int columnIndex) {
        return mCursor.getDouble(columnIndex);
    }
    public Bundle getExtras() {
        return mCursor.getExtras();
    }
    public float getFloat(int columnIndex) {
        return mCursor.getFloat(columnIndex);
    }
    public int getInt(int columnIndex) {
        return mCursor.getInt(columnIndex);
    }
    public long getLong(int columnIndex) {
        return mCursor.getLong(columnIndex);
    }
    public short getShort(int columnIndex) {
        return mCursor.getShort(columnIndex);
    }
    public String getString(int columnIndex) {
        return mCursor.getString(columnIndex);
    }
    public void copyStringToBuffer(int columnIndex, CharArrayBuffer buffer) {
        mCursor.copyStringToBuffer(columnIndex, buffer);
    }
    public byte[] getBlob(int columnIndex) {
        return mCursor.getBlob(columnIndex);
    }
    public boolean getWantsAllOnMoveCalls() {
        return mCursor.getWantsAllOnMoveCalls();
    }
    public boolean hasUpdates() {
        return mCursor.hasUpdates();
    }
    public boolean isAfterLast() {
        return mCursor.isAfterLast();
    }
    public boolean isBeforeFirst() {
        return mCursor.isBeforeFirst();
    }
    public boolean isFirst() {
        return mCursor.isFirst();
    }
    public boolean isLast() {
        return mCursor.isLast();
    }
    public boolean isNull(int columnIndex) {
        return mCursor.isNull(columnIndex);
    }
    public boolean moveToLast() {
        return mCursor.moveToLast();
    }
    public boolean move(int offset) {
        return mCursor.move(offset);
    }
    public boolean moveToPosition(int position) {
        return mCursor.moveToPosition(position);
    }
    public boolean moveToNext() {
        return mCursor.moveToNext();
    }
    public int getPosition() {
        return mCursor.getPosition();
    }
    public boolean moveToPrevious() {
        return mCursor.moveToPrevious();
    }
    public void registerContentObserver(ContentObserver observer) {
        mCursor.registerContentObserver(observer);   
    }
    public void registerDataSetObserver(DataSetObserver observer) {
        mCursor.registerDataSetObserver(observer);   
    }
    public boolean requery() {
        return mCursor.requery();
    }
    public Bundle respond(Bundle extras) {
        return mCursor.respond(extras);
    }
    public void setNotificationUri(ContentResolver cr, Uri uri) {
        mCursor.setNotificationUri(cr, uri);        
    }
    public boolean supportsUpdates() {
        return mCursor.supportsUpdates();
    }
    public void unregisterContentObserver(ContentObserver observer) {
        mCursor.unregisterContentObserver(observer);        
    }
    public void unregisterDataSetObserver(DataSetObserver observer) {
        mCursor.unregisterDataSetObserver(observer);
    }
    public boolean updateDouble(int columnIndex, double value) {
        return mCursor.updateDouble(columnIndex, value);
    }
    public boolean updateFloat(int columnIndex, float value) {
        return mCursor.updateFloat(columnIndex, value);
    }
    public boolean updateInt(int columnIndex, int value) {
        return mCursor.updateInt(columnIndex, value);
    }
    public boolean updateLong(int columnIndex, long value) {
        return mCursor.updateLong(columnIndex, value);
    }
    public boolean updateShort(int columnIndex, short value) {
        return mCursor.updateShort(columnIndex, value);
    }
    public boolean updateString(int columnIndex, String value) {
        return mCursor.updateString(columnIndex, value);
    }
    public boolean updateBlob(int columnIndex, byte[] value) {
        return mCursor.updateBlob(columnIndex, value);
    }
    public boolean updateToNull(int columnIndex) {
        return mCursor.updateToNull(columnIndex);
    }
    private Cursor mCursor;
}
