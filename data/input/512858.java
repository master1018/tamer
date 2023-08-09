public class TextAppearanceFactory {
    private final Context mContext;
    public TextAppearanceFactory(Context context) {
        mContext = context;
    }
    public Object[] createSuggestionQueryTextAppearance() {
        return new Object[]{
                new TextAppearanceSpan(mContext, R.style.SuggestionQueryTextAppearance)
        };
    }
    public Object[] createSuggestionSuggestedTextAppearance() {
        return new Object[]{
                new TextAppearanceSpan(mContext, R.style.SuggestionSuggestedTextAppearance)
        };
    }
}
