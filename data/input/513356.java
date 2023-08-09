public class SuggestionCursorBackedCursor extends AbstractCursor {
    private static final String[] COLUMNS = {
        "_id",  
        SearchManager.SUGGEST_COLUMN_TEXT_1,  
        SearchManager.SUGGEST_COLUMN_TEXT_2,  
        SearchManager.SUGGEST_COLUMN_TEXT_2_URL,  
        SearchManager.SUGGEST_COLUMN_ICON_1,  
        SearchManager.SUGGEST_COLUMN_ICON_2,  
        SearchManager.SUGGEST_COLUMN_INTENT_ACTION,  
        SearchManager.SUGGEST_COLUMN_INTENT_DATA,  
        SearchManager.SUGGEST_COLUMN_INTENT_EXTRA_DATA,  
        SearchManager.SUGGEST_COLUMN_QUERY,  
        SearchManager.SUGGEST_COLUMN_FORMAT,  
        SearchManager.SUGGEST_COLUMN_SHORTCUT_ID,  
        SearchManager.SUGGEST_COLUMN_SPINNER_WHILE_REFRESHING,  
    };
    private static final int COLUMN_INDEX_ID = 0;
    private static final int COLUMN_INDEX_TEXT1 = 1;
    private static final int COLUMN_INDEX_TEXT2 = 2;
    private static final int COLUMN_INDEX_TEXT2_URL = 3;
    private static final int COLUMN_INDEX_ICON1 = 4;
    private static final int COLUMN_INDEX_ICON2 = 5;
    private static final int COLUMN_INDEX_INTENT_ACTION = 6;
    private static final int COLUMN_INDEX_INTENT_DATA = 7;
    private static final int COLUMN_INDEX_INTENT_EXTRA_DATA = 8;
    private static final int COLUMN_INDEX_QUERY = 9;
    private static final int COLUMN_INDEX_FORMAT = 10;
    private static final int COLUMN_INDEX_SHORTCUT_ID = 11;
    private static final int COLUMN_INDEX_SPINNER_WHILE_REFRESHING = 12;
    private final SuggestionCursor mCursor;
    public SuggestionCursorBackedCursor(SuggestionCursor cursor) {
        mCursor = cursor;
    }
    @Override
    public String[] getColumnNames() {
        return COLUMNS;
    }
    @Override
    public int getCount() {
        return mCursor.getCount();
    }
    private Suggestion get() {
        mCursor.moveTo(getPosition());
        return mCursor;
    }
    @Override
    public int getInt(int column) {
        switch (column) {
            case COLUMN_INDEX_ID:
                return getPosition();
            default:
                throw new CursorIndexOutOfBoundsException("Requested column " + column
                        + " of " + COLUMNS.length);
        }
    }
    @Override
    public String getString(int column) {
        switch (column) {
            case COLUMN_INDEX_ID:
                return String.valueOf(getPosition());
            case COLUMN_INDEX_TEXT1:
                return get().getSuggestionText1();
            case COLUMN_INDEX_TEXT2:
                return get().getSuggestionText2();
            case COLUMN_INDEX_TEXT2_URL:
                return get().getSuggestionText2Url();
            case COLUMN_INDEX_ICON1:
                return get().getSuggestionIcon1();
            case COLUMN_INDEX_ICON2:
                return get().getSuggestionIcon2();
            case COLUMN_INDEX_INTENT_ACTION:
                return get().getSuggestionIntentAction();
            case COLUMN_INDEX_INTENT_DATA:
                return get().getSuggestionIntentDataString();
            case COLUMN_INDEX_INTENT_EXTRA_DATA:
                return get().getSuggestionIntentExtraData();
            case COLUMN_INDEX_QUERY:
                return get().getSuggestionQuery();
            case COLUMN_INDEX_FORMAT:
                return get().getSuggestionFormat();
            case COLUMN_INDEX_SHORTCUT_ID:
                return get().getShortcutId();
            case COLUMN_INDEX_SPINNER_WHILE_REFRESHING:
                return String.valueOf(get().isSpinnerWhileRefreshing());
            default:
                throw new CursorIndexOutOfBoundsException("Requested column " + column
                        + " of " + COLUMNS.length);
        }
    }
    @Override
    public long getLong(int column) {
        return getInt(column);
    }
    @Override
    public boolean isNull(int column) {
        return getString(column) == null;
    }
    @Override
    public short getShort(int column) {
        throw new UnsupportedOperationException();
    }
    @Override
    public double getDouble(int column) {
        throw new UnsupportedOperationException();
    }
    @Override
    public float getFloat(int column) {
        throw new UnsupportedOperationException();
    }
}
