public class SearchWidgetConfigActivity extends ChoiceActivity {
    private static final boolean DBG = false;
    private static final String TAG = "QSB.SearchWidgetConfigActivity";
    private static final String PREFS_NAME = "SearchWidgetConfig";
    private static final String WIDGET_CORPUS_NAME_PREFIX = "widget_corpus_";
    private static final String WIDGET_CORPUS_SHOWING_HINT_PREFIX = "widget_showing_hint_";
    private CorporaAdapter mAdapter;
    private int mAppWidgetId;
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setHeading(R.string.search_widget);
        setOnItemClickListener(new SourceClickListener());
        Intent intent = getIntent();
        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }
        if (getCorpusRanker().getRankedCorpora().size() <= 1) {
            selectCorpus(null);
        }
    }
    @Override
    protected void onStart() {
        setAdapter(CorporaAdapter.createListAdapter(getViewFactory(), getCorpusRanker()));
        super.onStart();
    }
    @Override
    protected void onStop() {
        setAdapter(null);
        super.onStop();
    }
    @Override
    public void setAdapter(ListAdapter adapter) {
        if (adapter == mAdapter) return;
        if (mAdapter != null) mAdapter.close();
        mAdapter = (CorporaAdapter) adapter;
        super.setAdapter(adapter);
    }
    protected void selectCorpus(Corpus corpus) {
        setWidgetCorpusName(mAppWidgetId, corpus);
        SearchWidgetProvider.updateSearchWidgets(this);
        Intent result = new Intent();
        result.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
        setResult(RESULT_OK, result);
        finish();
    }
    private static SharedPreferences getWidgetPreferences(Context context) {
        return context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
    }
    private static String getCorpusNameKey(int appWidgetId) {
        return WIDGET_CORPUS_NAME_PREFIX + appWidgetId;
    }
    private static String getShowingHintKey(int appWidgetId) {
        return WIDGET_CORPUS_SHOWING_HINT_PREFIX + appWidgetId;
    }
    private void setWidgetCorpusName(int appWidgetId, Corpus corpus) {
        String corpusName = corpus == null ? null : corpus.getName();
        SharedPreferences.Editor prefs = getWidgetPreferences(this).edit();
        prefs.putString(getCorpusNameKey(appWidgetId), corpusName);
        prefs.commit();
    }
    public static String getWidgetCorpusName(Context context, int appWidgetId) {
        SharedPreferences prefs = getWidgetPreferences(context);
        return prefs.getString(getCorpusNameKey(appWidgetId), null);
    }
    public static void setWidgetShowingHint(Context context, int appWidgetId, boolean showing) {
        SharedPreferences.Editor prefs = getWidgetPreferences(context).edit();
        prefs.putBoolean(getShowingHintKey(appWidgetId), showing);
        boolean c = prefs.commit();
        if (DBG) Log.d(TAG, "Widget " + appWidgetId + " set showing hint " + showing + "("+c+")");
    }
    public static boolean getWidgetShowingHint(Context context, int appWidgetId) {
        SharedPreferences prefs = getWidgetPreferences(context);
        boolean r = prefs.getBoolean(getShowingHintKey(appWidgetId), false);
        if (DBG) Log.d(TAG, "Widget " + appWidgetId + " showing hint: " + r);
        return r;
    }
    private CorpusRanker getCorpusRanker() {
        return QsbApplication.get(this).getCorpusRanker();
    }
    private CorpusViewFactory getViewFactory() {
        return QsbApplication.get(this).getCorpusViewFactory();
    }
    private class SourceClickListener implements AdapterView.OnItemClickListener {
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Corpus corpus = (Corpus) parent.getItemAtPosition(position);
            selectCorpus(corpus);
        }
    }
}
