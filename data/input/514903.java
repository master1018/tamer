public abstract class GoogleSource extends AbstractSource {
    private static final String GOOGLE_SOURCE_NAME =
        "com.android.quicksearchbox/.google.GoogleSearch";
    public GoogleSource(Context context) {
        super(context);
    }
    public abstract ComponentName getIntentComponent();
    public abstract SuggestionCursor refreshShortcut(String shortcutId, String extraData);
    public abstract boolean isLocationAware();
    protected abstract SourceResult queryInternal(String query);
    public abstract SourceResult queryExternal(String query);
    public boolean canRead() {
        return true;
    }
    public Intent createVoiceSearchIntent(Bundle appData) {
        return createVoiceWebSearchIntent(appData);
    }
    public String getDefaultIntentAction() {
        return Intent.ACTION_WEB_SEARCH;
    }
    public String getDefaultIntentData() {
        return null;
    }
    public CharSequence getHint() {
        return getContext().getString(R.string.google_search_hint);
    }
    @Override
    protected String getIconPackage() {
        return getContext().getPackageName();
    }
    public CharSequence getLabel() {
        return getContext().getString(R.string.google_search_label);
    }
    public String getName() {
        return GOOGLE_SOURCE_NAME;
    }
    public int getQueryThreshold() {
        return 0;
    }
    public CharSequence getSettingsDescription() {
        return getContext().getString(R.string.google_search_description);
    }
    public Drawable getSourceIcon() {
        return getContext().getResources().getDrawable(getSourceIconResource());
    }
    public Uri getSourceIconUri() {
        return Uri.parse("android.resource:
                + "/" +  getSourceIconResource());
    }
    private int getSourceIconResource() {
        return R.drawable.google_icon;
    }
    public SourceResult getSuggestions(String query, int queryLimit, boolean onlySource) {
        return emptyIfNull(queryInternal(query), query);
    }
    public SourceResult getSuggestionsExternal(String query) {
        return emptyIfNull(queryExternal(query), query);
    }
    private SourceResult emptyIfNull(SourceResult result, String query) {
        return result == null ? new CursorBackedSourceResult(this, query) : result;
    }
    public int getVersionCode() {
        return QsbApplication.get(getContext()).getVersionCode();
    }
    @Override
    public boolean isVersionCodeCompatible(int version) {
        return true;
    }
    public boolean queryAfterZeroResults() {
        return true;
    }
    public boolean voiceSearchEnabled() {
        return true;
    }
    public boolean isWebSuggestionSource() {
        return true;
    }
}
