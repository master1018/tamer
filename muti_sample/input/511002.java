public class SuggestionCursorWrapper extends AbstractSuggestionCursorWrapper {
    private final SuggestionCursor mCursor;
    public SuggestionCursorWrapper(String userQuery, SuggestionCursor cursor) {
        super(userQuery);
        mCursor = cursor;
    }
    public void close() {
        if (mCursor != null) {
            mCursor.close();
        }
    }
    public int getCount() {
        return mCursor == null ? 0 : mCursor.getCount();
    }
    public int getPosition() {
        return mCursor == null ? 0 : mCursor.getPosition();
    }
    public void moveTo(int pos) {
        if (mCursor != null) {
            mCursor.moveTo(pos);
        }
    }
    public boolean moveToNext() {
        if (mCursor != null) {
            return mCursor.moveToNext();
        } else {
            return false;
        }
    }
    public void registerDataSetObserver(DataSetObserver observer) {
        if (mCursor != null) {
            mCursor.registerDataSetObserver(observer);
        }
    }
    public void unregisterDataSetObserver(DataSetObserver observer) {
        if (mCursor != null) {
            mCursor.unregisterDataSetObserver(observer);
        }
    }
    @Override
    protected SuggestionCursor current() {
        return mCursor;
    }
}
