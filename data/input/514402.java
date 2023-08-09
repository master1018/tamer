public class SuggestionNonFormatter extends SuggestionFormatter {
    public SuggestionNonFormatter(TextAppearanceFactory spanFactory) {
        super(spanFactory);
    }
    @Override
    public CharSequence formatSuggestion(String query, String suggestion) {
        return suggestion;
    }
}
