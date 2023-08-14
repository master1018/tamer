public abstract class AbstractGoogleSourceResult implements SourceResult {
    private final Source mSource;
    private final String mUserQuery;
    private int mPos = 0;
    public AbstractGoogleSourceResult(Source source, String userQuery) {
        mSource = source;
        mUserQuery = userQuery;
    }
    public abstract int getCount();
    public abstract String getSuggestionQuery();
    public Source getSource() {
        return mSource;
    }
    public void close() {
    }
    public int getPosition() {
        return mPos;
    }
    public String getUserQuery() {
        return mUserQuery;
    }
    public void moveTo(int pos) {
        mPos = pos;
    }
    public boolean moveToNext() {
        int size = getCount();
        if (mPos >= size) {
            return false;
        }
        mPos++;
        return mPos < size;
    }
    public void registerDataSetObserver(DataSetObserver observer) {
    }
    public void unregisterDataSetObserver(DataSetObserver observer) {
    }
    public String getSuggestionText1() {
        return getSuggestionQuery();
    }
    public Source getSuggestionSource() {
        return mSource;
    }
    public boolean isSuggestionShortcut() {
        return false;
    }
    public String getShortcutId() {
        return null;
    }
    public String getSuggestionFormat() {
        return null;
    }
    public String getSuggestionIcon1() {
        return String.valueOf(R.drawable.magnifying_glass);
    }
    public String getSuggestionIcon2() {
        return null;
    }
    public String getSuggestionIntentAction() {
        return mSource.getDefaultIntentAction();
    }
    public String getSuggestionIntentDataString() {
        return null;
    }
    public String getSuggestionIntentExtraData() {
        return null;
    }
    public String getSuggestionLogType() {
        return null;
    }
    public String getSuggestionText2() {
        return null;
    }
    public String getSuggestionText2Url() {
        return null;
    }
    public boolean isSpinnerWhileRefreshing() {
        return false;
    }
    public boolean isWebSearchSuggestion() {
        return true;
    }
}
