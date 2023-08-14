public class SearchEngineInfo {
    private static String TAG = "SearchEngineInfo";
    private static final int FIELD_LABEL = 0;
    private static final int FIELD_KEYWORD = 1;
    private static final int FIELD_FAVICON_URI = 2;
    private static final int FIELD_SEARCH_URI = 3;
    private static final int FIELD_ENCODING = 4;
    private static final int FIELD_SUGGEST_URI = 5;
    private static final int NUM_FIELDS = 6;
    private static final String PARAMETER_LANGUAGE = "{language}";
    private static final String PARAMETER_SEARCH_TERMS = "{searchTerms}";
    private static final String PARAMETER_INPUT_ENCODING = "{inputEncoding}";
    private final String mName;
    private final String[] mSearchEngineData;
    public SearchEngineInfo(Context context, String name) throws IllegalArgumentException {
        mName = name;
        Resources res = context.getResources();
        int id_data = res.getIdentifier(name, "array", context.getPackageName());
        mSearchEngineData = res.getStringArray(id_data);
        if (mSearchEngineData == null) {
            throw new IllegalArgumentException("No data found for " + name);
        }
        if (mSearchEngineData.length != NUM_FIELDS) {
                throw new IllegalArgumentException(
                        name + " has invalid number of fields - " + mSearchEngineData.length);
        }
        if (TextUtils.isEmpty(mSearchEngineData[FIELD_SEARCH_URI])) {
            throw new IllegalArgumentException(name + " has an empty search URI");
        }
        Locale locale = context.getResources().getConfiguration().locale;
        StringBuilder language = new StringBuilder(locale.getLanguage());
        if (!TextUtils.isEmpty(locale.getCountry())) {
            language.append('-');
            language.append(locale.getCountry());
        }
        String language_str = language.toString();
        mSearchEngineData[FIELD_SEARCH_URI] =
                mSearchEngineData[FIELD_SEARCH_URI].replace(PARAMETER_LANGUAGE, language_str);
        mSearchEngineData[FIELD_SUGGEST_URI] =
                mSearchEngineData[FIELD_SUGGEST_URI].replace(PARAMETER_LANGUAGE, language_str);
        String enc = mSearchEngineData[FIELD_ENCODING];
        if (TextUtils.isEmpty(enc)) {
            enc = "UTF-8";
            mSearchEngineData[FIELD_ENCODING] = enc;
        }
        mSearchEngineData[FIELD_SEARCH_URI] =
                mSearchEngineData[FIELD_SEARCH_URI].replace(PARAMETER_INPUT_ENCODING, enc);
        mSearchEngineData[FIELD_SUGGEST_URI] =
                mSearchEngineData[FIELD_SUGGEST_URI].replace(PARAMETER_INPUT_ENCODING, enc);
    }
    public String getName() {
        return mName;
    }
    public String getLabel() {
        return mSearchEngineData[FIELD_LABEL];
    }
    public String getSearchUriForQuery(String query) {
        return getFormattedUri(searchUri(), query);
    }
    public String getSuggestUriForQuery(String query) {
        return getFormattedUri(suggestUri(), query);
    }
    public boolean supportsSuggestions() {
        return !TextUtils.isEmpty(suggestUri());
    }
    public String faviconUri() {
        return mSearchEngineData[FIELD_FAVICON_URI];
    }
    private String suggestUri() {
        return mSearchEngineData[FIELD_SUGGEST_URI];
    }
    private String searchUri() {
        return mSearchEngineData[FIELD_SEARCH_URI];
    }
    private String getFormattedUri(String templateUri, String query) {
        if (TextUtils.isEmpty(templateUri)) {
            return null;
        }
        String enc = mSearchEngineData[FIELD_ENCODING];
        try {
            return templateUri.replace(PARAMETER_SEARCH_TERMS, URLEncoder.encode(query, enc));
        } catch (java.io.UnsupportedEncodingException e) {
            Log.e(TAG, "Exception occured when encoding query " + query + " to " + enc);
            return null;
        }
    }
    @Override
    public String toString() {
        return "SearchEngineInfo{" + Arrays.toString(mSearchEngineData) + "}";
    }
}
