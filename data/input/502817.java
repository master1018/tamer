public class SuggestionUtils {
    private SuggestionUtils() {
    }
    public static Intent getSuggestionIntent(SuggestionCursor suggestion, Bundle appSearchData) {
        Source source = suggestion.getSuggestionSource();
        String action = suggestion.getSuggestionIntentAction();
        String data = suggestion.getSuggestionIntentDataString();
        String query = suggestion.getSuggestionQuery();
        String userQuery = suggestion.getUserQuery();
        String extraData = suggestion.getSuggestionIntentExtraData();
        Intent intent = new Intent(action);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (data != null) {
            intent.setData(Uri.parse(data));
        }
        intent.putExtra(SearchManager.USER_QUERY, userQuery);
        if (query != null) {
            intent.putExtra(SearchManager.QUERY, query);
        }
        if (extraData != null) {
            intent.putExtra(SearchManager.EXTRA_DATA_KEY, extraData);
        }
        if (appSearchData != null) {
            intent.putExtra(SearchManager.APP_DATA, appSearchData);
        }
        intent.setComponent(source.getIntentComponent());
        return intent;
    }
}
