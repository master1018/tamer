public class SearchSuggestionSampleProvider extends SearchRecentSuggestionsProvider {
    final static String AUTHORITY = "com.example.android.apis.SuggestionProvider";
    final static int MODE = DATABASE_MODE_QUERIES;
    public SearchSuggestionSampleProvider() {
        super();
        setupSuggestions(AUTHORITY, MODE);
    }
}
