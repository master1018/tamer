public class GoogleSettings extends PreferenceActivity implements OnPreferenceClickListener {
    private static final String SHOW_WEB_SUGGESTIONS_PREF = "show_web_suggestions";
    private CheckBoxPreference mShowWebSuggestionsPreference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.google_preferences);
        mShowWebSuggestionsPreference = (CheckBoxPreference)
                findPreference(SHOW_WEB_SUGGESTIONS_PREF);
        mShowWebSuggestionsPreference.setOnPreferenceClickListener(this);
        updateShowWebSuggestionsPreference();
    }
    private void updateShowWebSuggestionsPreference() {
        boolean showWebSuggestions = SearchSettings.getShowWebSuggestions(this);
        mShowWebSuggestionsPreference.setChecked(showWebSuggestions);
    }
    private void storeShowWebSuggestionsPreference() {
        boolean showWebSuggestions = mShowWebSuggestionsPreference.isChecked();
        SearchSettings.setShowWebSuggestions(this, showWebSuggestions);
    }
    public boolean onPreferenceClick(Preference preference) {
        if (preference == mShowWebSuggestionsPreference) {
            storeShowWebSuggestionsPreference();
            return true;
        }
        return false;
    }
}
