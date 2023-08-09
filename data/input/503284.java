public class ReorderingCursorWrapper extends AbstractCursor {
    private final Cursor mCursor;
    private final int[] mPositionMap;
    public ReorderingCursorWrapper(Cursor cursor, int[] positionMap) {
        if (cursor.getCount() != positionMap.length) {
            throw new IllegalArgumentException("Cursor and position map have different sizes.");
        }
        mCursor = cursor;
        mPositionMap = positionMap;
    }
    @Override
    public void close() {
        super.close();
        mCursor.close();
    }
    @Override
    public boolean onMove(int oldPosition, int newPosition) {
        return mCursor.moveToPosition(mPositionMap[newPosition]);
    }
    @Override
    public String[] getColumnNames() {
        return mCursor.getColumnNames();
    }
    @Override
    public int getCount() {
        return mCursor.getCount();
    }
    @Override
    public double getDouble(int column) {
        return mCursor.getDouble(column);
    }
    @Override
    public float getFloat(int column) {
        return mCursor.getFloat(column);
    }
    @Override
    public int getInt(int column) {
        return mCursor.getInt(column);
    }
    @Override
    public long getLong(int column) {
        return mCursor.getLong(column);
    }
    @Override
    public short getShort(int column) {
        return mCursor.getShort(column);
    }
    @Override
    public String getString(int column) {
        return mCursor.getString(column);
    }
    @Override
    public boolean isNull(int column) {
        return mCursor.isNull(column);
    }
}