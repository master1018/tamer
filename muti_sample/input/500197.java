public class SuggestionPosition extends AbstractSuggestionWrapper {
    private final SuggestionCursor mCursor;
    private final int mPosition;
    public SuggestionPosition(SuggestionCursor cursor) {
        this(cursor, cursor.getPosition());
    }
    public SuggestionPosition(SuggestionCursor cursor, int suggestionPos) {
        mCursor = cursor;
        mPosition = suggestionPos;
    }
    protected Suggestion current() {
        mCursor.moveTo(mPosition);
        return mCursor;
    }
    public int getPosition() {
        return mPosition;
    }
    @Override
    public String toString() {
        return mCursor + ":" + mPosition;
    }
}
