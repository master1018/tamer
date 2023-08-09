public class WebCorpus extends MultiSourceCorpus {
    private static final String WEB_CORPUS_NAME = "web";
    private final Source mWebSearchSource;
    private final Source mBrowserSource;
    public WebCorpus(Context context, Config config, Executor executor,
            Source webSearchSource, Source browserSource) {
        super(context, config, executor, webSearchSource, browserSource);
        mWebSearchSource = webSearchSource;
        mBrowserSource = browserSource;
    }
    public CharSequence getLabel() {
        return getContext().getText(R.string.corpus_label_web);
    }
    public CharSequence getHint() {
        return null;
    }
    private boolean isUrl(String query) {
       return Patterns.WEB_URL.matcher(query).matches();
    }
    public Intent createSearchIntent(String query, Bundle appData) {
        if (isUrl(query)) {
            return createBrowseIntent(query);
        } else if (mWebSearchSource != null){
            return mWebSearchSource.createSearchIntent(query, appData);
        } else {
            return null;
        }
    }
    private Intent createBrowseIntent(String query) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        String url = URLUtil.guessUrl(query);
        intent.setData(Uri.parse(url));
        return intent;
    }
    public SuggestionData createSearchShortcut(String query) {
        SuggestionData shortcut = new SuggestionData(mWebSearchSource);
        if (isUrl(query)) {
            shortcut.setIntentAction(Intent.ACTION_VIEW);
            shortcut.setIcon1(String.valueOf(R.drawable.globe));
            shortcut.setText1(query);
            shortcut.setSuggestionQuery(query);
            shortcut.setIntentData(URLUtil.guessUrl(query));
        } else {
            shortcut.setIntentAction(Intent.ACTION_WEB_SEARCH);
            shortcut.setIcon1(String.valueOf(R.drawable.magnifying_glass));
            shortcut.setText1(query);
            shortcut.setSuggestionQuery(query);
        }
        return shortcut;
    }
    public Intent createVoiceSearchIntent(Bundle appData) {
        if (mWebSearchSource != null){
            return mWebSearchSource.createVoiceSearchIntent(appData);
        } else {
            return null;
        }
    }
    private int getCorpusIconResource() {
        return R.drawable.corpus_icon_web;
    }
    public Drawable getCorpusIcon() {
        return getContext().getResources().getDrawable(getCorpusIconResource());
    }
    public Uri getCorpusIconUri() {
        return Util.getResourceUri(getContext(), getCorpusIconResource());
    }
    public String getName() {
        return WEB_CORPUS_NAME;
    }
    @Override
    public int getQueryThreshold() {
        return 0;
    }
    @Override
    public boolean queryAfterZeroResults() {
        return true;
    }
    @Override
    public boolean voiceSearchEnabled() {
        return true;
    }
    public boolean isWebCorpus() {
        return true;
    }
    public CharSequence getSettingsDescription() {
        return getContext().getText(R.string.corpus_description_web);
    }
    @Override
    protected List<Source> getSourcesToQuery(String query, boolean onlyCorpus) {
        ArrayList<Source> sourcesToQuery = new ArrayList<Source>(2);
        if (mWebSearchSource != null
                && SearchSettings.getShowWebSuggestions(getContext())) {
            sourcesToQuery.add(mWebSearchSource);
        }
        if (mBrowserSource != null && query.length() > 0) {
            sourcesToQuery.add(mBrowserSource);
        }
        return sourcesToQuery;
    }
    @Override
    protected Result createResult(String query, ArrayList<SourceResult> results, int latency) {
        return new WebResult(query, results, latency);
    }
    protected class WebResult extends Result {
        public WebResult(String query, ArrayList<SourceResult> results, int latency) {
            super(query, results, latency);
        }
        @Override
        public void fill() {
            SourceResult webSearchResult = null;
            SourceResult browserResult = null;
            for (SourceResult result : getResults()) {
                if (result.getSource().equals(mWebSearchSource)) {
                    webSearchResult = result;
                } else {
                    browserResult = result;
                }
            }
            if (browserResult != null && browserResult.getCount() > 0) {
                add(new SuggestionPosition(browserResult, 0));
            }
            if (webSearchResult != null) {
                int count = webSearchResult.getCount();
                for (int i = 0; i < count; i++) {
                    add(new SuggestionPosition(webSearchResult, i));
                }
            }
        }
    }
}
