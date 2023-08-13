public abstract class AbstractSuggestionCursorWrapper extends AbstractSuggestionWrapper
        implements SuggestionCursor {
    private final String mUserQuery;
    public AbstractSuggestionCursorWrapper(String userQuery) {
        mUserQuery = userQuery;
    }
    public String getUserQuery() {
        return mUserQuery;
    }
}
