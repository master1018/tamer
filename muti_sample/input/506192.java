public class SuggestionViewInflater implements SuggestionViewFactory {
    private static final Class<?>[] SUGGESTION_VIEW_CLASSES = {
            DefaultSuggestionView.class,
            ContactSuggestionView.class,
    };
    private static final int[] SUGGESTION_VIEW_LAYOUTS = {
            R.layout.suggestion,
            R.layout.contact_suggestion,
    };
    private static final String CONTACT_LOOKUP_URI
            = ContactsContract.Contacts.CONTENT_LOOKUP_URI.toString();
    private final Context mContext;
    public SuggestionViewInflater(Context context) {
        mContext = context;
    }
    protected LayoutInflater getInflater() {
        return (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    public int getSuggestionViewTypeCount() {
        return SUGGESTION_VIEW_CLASSES.length;
    }
    public int getSuggestionViewType(Suggestion suggestion) {
        return isContactSuggestion(suggestion) ? 1 : 0;
    }
    public SuggestionView getSuggestionView(int viewType, View convertView,
            ViewGroup parentViewType) {
        if (convertView == null || !convertView.getClass().equals(
                SUGGESTION_VIEW_CLASSES[viewType])) {
            int layoutId = SUGGESTION_VIEW_LAYOUTS[viewType];
            convertView = getInflater().inflate(layoutId, parentViewType, false);
        }
        return (SuggestionView) convertView;
    }
    private boolean isContactSuggestion(Suggestion suggestion) {
        String intentData = suggestion.getSuggestionIntentDataString();
        return intentData != null && intentData.startsWith(CONTACT_LOOKUP_URI);
    }
}
