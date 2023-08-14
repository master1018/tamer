public class CursorBackedSourceResult extends CursorBackedSuggestionCursor
        implements SourceResult {
    private final Source mSource;
    public CursorBackedSourceResult(Source source, String userQuery) {
        this(source, userQuery, null);
    }
    public CursorBackedSourceResult(Source source, String userQuery, Cursor cursor) {
        super(userQuery, cursor);
        mSource = source;
    }
    public Source getSource() {
        return mSource;
    }
    @Override
    public Source getSuggestionSource() {
        return mSource;
    }
    public boolean isSuggestionShortcut() {
        return false;
    }
    @Override
    public String toString() {
        return mSource + "[" + getUserQuery() + "]";
    }
}