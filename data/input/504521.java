public class SearchableItemsSettings extends PreferenceActivity
        implements OnPreferenceChangeListener {
    private static final boolean DBG = false;
    private static final String TAG = "QSB.SearchableItemsSettings";
    private static final String SEARCH_CORPORA_PREF = "search_corpora";
    private PreferenceGroup mCorporaPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName(SearchSettings.PREFERENCES_NAME);
        addPreferencesFromResource(R.xml.preferences_searchable_items);
        mCorporaPreferences = (PreferenceGroup) getPreferenceScreen().findPreference(
                SEARCH_CORPORA_PREF);
        populateSourcePreference();
    }
    private Corpora getCorpora() {
        return QsbApplication.get(this).getCorpora();
    }
    private void populateSourcePreference() {
        mCorporaPreferences.setOrderingAsAdded(false);
        for (Corpus corpus : getCorpora().getAllCorpora()) {
            Preference pref = createCorpusPreference(corpus);
            if (pref != null) {
                if (DBG) Log.d(TAG, "Adding corpus: " + corpus);
                mCorporaPreferences.addPreference(pref);
            }
        }
    }
    private Preference createCorpusPreference(Corpus corpus) {
        SearchableItemPreference sourcePref = new SearchableItemPreference(this);
        sourcePref.setKey(SearchSettings.getCorpusEnabledPreference(corpus));
        if (corpus.isWebCorpus()) {
            sourcePref.setOrder(0);
        }
        sourcePref.setDefaultValue(corpus.isCorpusDefaultEnabled());
        sourcePref.setOnPreferenceChangeListener(this);
        CharSequence label = corpus.getLabel();
        sourcePref.setTitle(label);
        CharSequence description = corpus.getSettingsDescription();
        sourcePref.setSummaryOn(description);
        sourcePref.setSummaryOff(description);
        sourcePref.setIcon(corpus.getCorpusIcon());
        return sourcePref;
    }
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        SearchSettings.broadcastSettingsChanged(this);
        return true;
    }
}
