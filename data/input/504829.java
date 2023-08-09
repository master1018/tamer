public abstract class SuggestionFormatter {
    private final TextAppearanceFactory mSpanFactory;
    protected SuggestionFormatter(TextAppearanceFactory spanFactory) {
        mSpanFactory = spanFactory;
    }
    public abstract CharSequence formatSuggestion(String query, String suggestion);
    protected void applyQueryTextStyle(Spannable text, int start, int end) {
        if (start == end) return;
        setSpans(text, start, end, mSpanFactory.createSuggestionQueryTextAppearance());
    }
    protected void applySuggestedTextStyle(Spannable text, int start, int end) {
        if (start == end) return;
        setSpans(text, start, end, mSpanFactory.createSuggestionSuggestedTextAppearance());
    }
    private void setSpans(Spannable text, int start, int end, Object[] spans) {
        for (Object span : spans) {
            text.setSpan(span, start, end, 0);
        }
    }
}
