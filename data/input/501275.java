class SearchEnginePreference extends ListPreference {
    private static final String TAG = "SearchEnginePreference";
    public SearchEnginePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        ArrayList<CharSequence> entryValues = new ArrayList<CharSequence>();
        ArrayList<CharSequence> entries = new ArrayList<CharSequence>();
        SearchEngine defaultSearchEngine = SearchEngines.getDefaultSearchEngine(context);
        String defaultSearchEngineName = null;
        if (defaultSearchEngine != null) {
            defaultSearchEngineName = defaultSearchEngine.getName();
            entryValues.add(defaultSearchEngineName);
            entries.add(defaultSearchEngine.getLabel());
        }
        for (SearchEngineInfo searchEngineInfo : SearchEngines.getSearchEngineInfos(context)) {
            String name = searchEngineInfo.getName();
            if (!name.equals(defaultSearchEngineName)) {
                entryValues.add(name);
                entries.add(searchEngineInfo.getLabel());
            }
        }
        setEntryValues(entryValues.toArray(new CharSequence[entryValues.size()]));
        setEntries(entries.toArray(new CharSequence[entries.size()]));
    }
}
