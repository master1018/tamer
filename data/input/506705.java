public class SearchSettings extends PreferenceActivity
        implements OnPreferenceClickListener {
    private static final boolean DBG = false;
    private static final String TAG = "SearchSettings";
    public static final String PREFERENCES_NAME = "SearchSettings";
    public static final String ACTION_SEARCHABLE_ITEMS =
            "com.android.quicksearchbox.action.SEARCHABLE_ITEMS";
    private static final String CLEAR_SHORTCUTS_PREF = "clear_shortcuts";
    private static final String SEARCH_ENGINE_SETTINGS_PREF = "search_engine_settings";
    private static final String SEARCH_CORPORA_PREF = "search_corpora";
    private static final String CORPUS_ENABLED_PREF_PREFIX = "enable_corpus_";
    private Preference mClearShortcutsPreference;
    private PreferenceScreen mSearchEngineSettingsPreference;
    private static final int CLEAR_SHORTCUTS_CONFIRM_DIALOG = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName(PREFERENCES_NAME);
        addPreferencesFromResource(R.xml.preferences);
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        mClearShortcutsPreference = preferenceScreen.findPreference(CLEAR_SHORTCUTS_PREF);
        mSearchEngineSettingsPreference = (PreferenceScreen) preferenceScreen.findPreference(
                SEARCH_ENGINE_SETTINGS_PREF);
        Preference corporaPreference = preferenceScreen.findPreference(SEARCH_CORPORA_PREF);
        corporaPreference.setIntent(getSearchableItemsIntent(this));
        mClearShortcutsPreference.setOnPreferenceClickListener(this);
        updateClearShortcutsPreference();
        populateSearchEnginePreference();
    }
    public static Intent getSearchableItemsIntent(Context context) {
        Intent intent = new Intent(SearchSettings.ACTION_SEARCHABLE_ITEMS);
        intent.setPackage(context.getPackageName());
        return intent;
    }
    public static String getCorpusEnabledPreference(Corpus corpus) {
        return CORPUS_ENABLED_PREF_PREFIX + corpus.getName();
    }
    public static SharedPreferences getSearchPreferences(Context context) {
        return context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
    }
    private ShortcutRepository getShortcuts() {
        return QsbApplication.get(this).getShortcutRepository();
    }
    private Config getConfig() {
        return QsbApplication.get(this).getConfig();
    }
    private void updateClearShortcutsPreference() {
        boolean hasHistory = getShortcuts().hasHistory();
        if (DBG) Log.d(TAG, "hasHistory()=" + hasHistory);
        mClearShortcutsPreference.setEnabled(hasHistory);
    }
    private void populateSearchEnginePreference() {
        Intent intent = new Intent(SearchManager.INTENT_ACTION_WEB_SEARCH_SETTINGS);
        intent.setPackage(getPackageName());
        CharSequence webSearchSettingsLabel = getActivityLabel(intent);
        mSearchEngineSettingsPreference.setTitle(webSearchSettingsLabel);
        mSearchEngineSettingsPreference.setIntent(intent);
    }
    private CharSequence getActivityLabel(Intent intent) {
        PackageManager pm = getPackageManager();
        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(intent, 0);
        if (resolveInfos.size() == 0) {
            Log.e(TAG, "No web search settings activity");
            return null;
        }
        if (resolveInfos.size() > 1) {
            Log.e(TAG, "More than one web search settings activity");
            return null;
        }
        return resolveInfos.get(0).activityInfo.loadLabel(pm);
    }
    public synchronized boolean onPreferenceClick(Preference preference) {
        if (preference == mClearShortcutsPreference) {
            showDialog(CLEAR_SHORTCUTS_CONFIRM_DIALOG);
            return true;
        }
        return false;
    }
    @Override
    protected Dialog onCreateDialog(int id, Bundle args) {
        switch (id) {
            case CLEAR_SHORTCUTS_CONFIRM_DIALOG:
                return new AlertDialog.Builder(this)
                        .setTitle(R.string.clear_shortcuts)
                        .setMessage(R.string.clear_shortcuts_prompt)
                        .setPositiveButton(R.string.agree, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                if (DBG) Log.d(TAG, "Clearing history...");
                                getShortcuts().clearHistory();
                                mClearShortcutsPreference.setEnabled(false);
                            }
                        })
                        .setNegativeButton(R.string.disagree, null).create();
            default:
                Log.e(TAG, "unknown dialog" + id);
                return null;
        }
    }
    public static void broadcastSettingsChanged(Context context) {
        Intent intent = new Intent(SearchManager.INTENT_ACTION_SEARCH_SETTINGS_CHANGED);
        Log.i(TAG, "Broadcasting: " + intent);
        context.sendBroadcast(intent);
    }
    public static boolean getShowWebSuggestions(Context context) {
        return (Settings.System.getInt(context.getContentResolver(),
                Settings.System.SHOW_WEB_SUGGESTIONS,
                1 ) == 1);
    }
    public static void setShowWebSuggestions(Context context, boolean showWebSuggestions) {
        System.putInt(context.getContentResolver(), System.SHOW_WEB_SUGGESTIONS,
            showWebSuggestions ? 1 : 0);
    }
    public static void registerShowWebSuggestionsSettingObserver(
            Context context, ContentObserver observer) {
        context.getContentResolver().registerContentObserver(
                Settings.System.getUriFor(Settings.System.SHOW_WEB_SUGGESTIONS),
                false, observer);
    }
    public static void unregisterShowWebSuggestionsSettingObserver(
            Context context, ContentObserver observer) {
        context.getContentResolver().unregisterContentObserver(observer);
    }
    public static void addSearchSettingsMenuItem(Context context, Menu menu) {
        Intent settings = new Intent(SearchManager.INTENT_ACTION_SEARCH_SETTINGS);
        settings.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        settings.setPackage(context.getPackageName());
        menu.add(Menu.NONE, Menu.NONE, 0, R.string.menu_settings)
                .setIcon(R.drawable.ic_menu_preferences).setAlphabeticShortcut('P')
                .setIntent(settings);
    }
}
