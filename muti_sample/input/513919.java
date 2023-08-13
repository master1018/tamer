public class ListSuggestionCursorNoDuplicates extends ListSuggestionCursor {
    private static final boolean DBG = false;
    private static final String TAG = "QSB.ListSuggestionCursorNoDuplicates";
    private final HashSet<String> mSuggestionKeys;
    public ListSuggestionCursorNoDuplicates(String userQuery) {
        super(userQuery);
        mSuggestionKeys = new HashSet<String>();
    }
    @Override
    public boolean add(Suggestion suggestion) {
        String key = getSuggestionKey(suggestion);
        if (mSuggestionKeys.add(key)) {
            return super.add(suggestion);
        } else {
            if (DBG) Log.d(TAG, "Rejecting duplicate " + key);
            return false;
        }
    }
    private String getSuggestionKey(Suggestion suggestion) {
        String action = makeKeyComponent(suggestion.getSuggestionIntentAction());
        String data = makeKeyComponent(normalizeUrl(suggestion.getSuggestionIntentDataString()));
        String query = makeKeyComponent(normalizeUrl(suggestion.getSuggestionQuery()));
        int size = action.length() + 2 + data.length() + query.length();
        return new StringBuilder(size)
                .append(action)
                .append('#')
                .append(data)
                .append('#')
                .append(query)
                .toString();
    }
    private String makeKeyComponent(String str) {
        return str == null ? "" : str;
    }
    private static String normalizeUrl(String url) {
        if (url != null && url.startsWith("http:
            int start = 7;   
            int end = url.length();
            if (url.indexOf('/', start) == end - 1) {
                end--;
            }
            return url.substring(start, end);
        }
        return url;
    }
}
